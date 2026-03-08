package com.example.startdrive.data.repository

import com.example.startdrive.data.FirebasePaths
import com.example.startdrive.data.model.ChatMessage
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import java.io.File
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.tasks.await

object ChatRepository {
    private val db = FirebasePaths.REALTIME_DATABASE_URL?.let { url ->
        FirebaseDatabase.getInstance(url).reference
    } ?: FirebaseDatabase.getInstance().reference
    private val storage = FirebaseStorage.getInstance().reference

    /** StateFlow по chatRoomId — при возврате на вкладку чата сразу видно последний список. */
    private val messageFlows = mutableMapOf<String, MutableStateFlow<List<ChatMessage>>>()
    /** Оптимистичные сообщения по chatRoomId — не теряются при смене вкладки. */
    private val optimisticFlows = mutableMapOf<String, MutableStateFlow<List<ChatMessage>>>()
    private val messageListeners = mutableMapOf<String, ValueEventListener>()
    private val roomLock = Any()

    fun chatRoomId(userId1: String, userId2: String): String {
        val sorted = listOf(userId1, userId2).sorted()
        return "${sorted[0]}_${sorted[1]}"
    }

    fun messages(chatRoomId: String): StateFlow<List<ChatMessage>> {
        synchronized(roomLock) {
            val flow = messageFlows.getOrPut(chatRoomId) {
                val state = MutableStateFlow<List<ChatMessage>>(emptyList())
                val ref = db.child(FirebasePaths.CHATS).child(chatRoomId).child(FirebasePaths.MESSAGES)
                    .orderByChild("timestamp")
                val listener = object : ValueEventListener {
                    override fun onDataChange(snap: DataSnapshot) {
                        @Suppress("UNCHECKED_CAST")
                        val list = snap.children.mapNotNull { child ->
                            val map = child.value as? Map<String, Any?> ?: return@mapNotNull null
                            ChatMessage.fromMap(map, id = child.key ?: "")
                        }.sortedBy { it.timestamp }
                        state.value = list
                        // убираем из оптимистичных те, что уже пришли с сервера (совпадение по отправителю и тексту)
                        synchronized(roomLock) {
                            optimisticFlows[chatRoomId]?.let { optFlow ->
                                val kept = optFlow.value.filter { opt ->
                                    !list.any { f ->
                                        f.senderId == opt.senderId && (
                                            (opt.text.isNotEmpty() && f.text == opt.text) ||
                                            (opt.voiceDurationSec != null && opt.voiceUrl.isNullOrBlank() && f.voiceUrl != null && f.voiceDurationSec == opt.voiceDurationSec)
                                        )
                                    }
                                }
                                if (kept.size != optFlow.value.size) optFlow.value = kept
                            }
                        }
                    }
                    override fun onCancelled(e: DatabaseError) {}
                }
                messageListeners[chatRoomId] = listener
                ref.addValueEventListener(listener)
                state
            }
            return flow.asStateFlow()
        }
    }

    /** Добавить оптимистичное сообщение (сохраняется при смене вкладки). */
    fun addOptimisticMessage(chatRoomId: String, msg: ChatMessage) {
        synchronized(roomLock) {
            val flow = optimisticFlows.getOrPut(chatRoomId) { MutableStateFlow(emptyList()) }
            flow.value = flow.value + msg
        }
    }

    /** Убрать оптимистичное сообщение (при ошибке отправки). */
    fun removeOptimisticMessage(chatRoomId: String, msg: ChatMessage) {
        synchronized(roomLock) {
            optimisticFlows[chatRoomId]?.let { flow ->
                flow.value = flow.value.filter {
                    if (msg.voiceDurationSec != null) it.senderId != msg.senderId || it.voiceDurationSec != msg.voiceDurationSec || it.timestamp != msg.timestamp
                    else it.text != msg.text || it.timestamp != msg.timestamp
                }
            }
        }
    }

    /** Оптимистичные сообщения по комнате (StateFlow, не теряются при смене вкладки). */
    fun optimisticMessages(chatRoomId: String): StateFlow<List<ChatMessage>> {
        synchronized(roomLock) {
            val flow = optimisticFlows.getOrPut(chatRoomId) { MutableStateFlow(emptyList()) }
            return flow.asStateFlow()
        }
    }

    /** Отправка с серверной меткой времени. replyToMessageId/replyToText — при ответе на сообщение. */
    suspend fun sendMessage(
        chatRoomId: String,
        senderId: String,
        text: String,
        replyToMessageId: String? = null,
        replyToText: String? = null,
    ) {
        val ref = db.child(FirebasePaths.CHATS).child(chatRoomId).child(FirebasePaths.MESSAGES).push()
        val payload = buildMap<String, Any?> {
            put("senderId", senderId)
            put("text", text)
            put("timestamp", ServerValue.TIMESTAMP)
            put("status", "sent")
            if (replyToMessageId != null) put("replyToMessageId", replyToMessageId)
            if (replyToText != null) put("replyToText", replyToText)
        }
        ref.setValue(payload).await()
    }

    /** Удалить сообщение из чата (для всех). Для голосового также удаляет файл из Storage. */
    suspend fun deleteMessage(chatRoomId: String, messageId: String, isVoice: Boolean) {
        db.child(FirebasePaths.CHATS).child(chatRoomId).child(FirebasePaths.MESSAGES).child(messageId).removeValue().await()
        if (isVoice) {
            try {
                storage.child("chats").child("voice").child(chatRoomId).child("$messageId.m4a").delete().await()
            } catch (_: Exception) { }
        }
    }

    /** Загрузка голосового в Storage и запись сообщения в Realtime Database. */
    suspend fun sendVoiceMessage(chatRoomId: String, senderId: String, audioFile: File, durationSec: Int) {
        val ref = db.child(FirebasePaths.CHATS).child(chatRoomId).child(FirebasePaths.MESSAGES).push()
        val messageId = ref.key ?: return
        val storageRef = storage.child("chats").child("voice").child(chatRoomId).child("$messageId.m4a")
        storageRef.putBytes(audioFile.readBytes()).await()
        var voiceUrl: String? = null
        for (attempt in 0 until 4) {
            try {
                voiceUrl = storageRef.getDownloadUrl().await().toString()
                break
            } catch (_: Exception) {
                if (attempt < 3) delay(1000L * (attempt + 1))
            }
        }
        val url = voiceUrl ?: throw Exception("Не удалось получить URL голосового сообщения")
        val payload = mapOf<String, Any?>(
            "senderId" to senderId,
            "text" to "",
            "voiceUrl" to url,
            "voiceDurationSec" to durationSec,
            "timestamp" to ServerValue.TIMESTAMP,
            "status" to "sent",
        )
        ref.setValue(payload).await()
    }

    suspend fun markDelivered(chatRoomId: String, myUserId: String) {
        val ref = db.child(FirebasePaths.CHATS).child(chatRoomId).child(FirebasePaths.MESSAGES)
        ref.get().addOnSuccessListener { snap ->
            snap.children.forEach { child ->
                @Suppress("UNCHECKED_CAST")
                val map = child.value as? Map<String, Any?> ?: return@forEach
                val msg = ChatMessage.fromMap(map)
                if (msg.senderId != myUserId && msg.status != "read") {
                    child.ref.child("status").setValue("delivered")
                }
            }
        }
    }

    suspend fun markRead(chatRoomId: String, myUserId: String) {
        val ref = db.child(FirebasePaths.CHATS).child(chatRoomId).child(FirebasePaths.MESSAGES)
        val snap = ref.get().await()
        snap.children.forEach { child ->
            @Suppress("UNCHECKED_CAST")
            val map = child.value as? Map<String, Any?> ?: return@forEach
            val msg = ChatMessage.fromMap(map)
            if (msg.senderId != myUserId) {
                child.ref.child("status").setValue("read").await()
            }
        }
    }

    fun unreadCount(chatRoomId: String, myUserId: String): Flow<Int> = callbackFlow {
        val ref = db.child(FirebasePaths.CHATS).child(chatRoomId).child(FirebasePaths.MESSAGES)
        val listener = object : ValueEventListener {
            override fun onDataChange(snap: DataSnapshot) {
                var count = 0
                snap.children.forEach { child ->
                    @Suppress("UNCHECKED_CAST")
                    val map = child.value as? Map<String, Any?> ?: return@forEach
                    val m = ChatMessage.fromMap(map)
                    if (m.senderId != myUserId && m.status != "read") count++
                }
                trySend(count)
            }
            override fun onCancelled(e: DatabaseError) {}
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    /** Сумма непрочитанных по всем указанным контактам (для бейджа на вкладке «Чат»). */
    fun totalUnreadCount(myUserId: String, contactIds: List<String>): Flow<Int> {
        if (contactIds.isEmpty()) return flowOf(0)
        val flows = contactIds.map { id -> unreadCount(chatRoomId(myUserId, id), myUserId) }
        return combine(*flows.toTypedArray()) { counts -> counts.sum() }
    }

    suspend fun clearChat(chatRoomId: String) {
        db.child(FirebasePaths.CHATS).child(chatRoomId).removeValue().await()
    }

    /** Удаляет всю историю переписки во всех чатах, где участвует пользователь [userId]. */
    suspend fun clearAllChatHistoryForUser(userId: String) {
        val chatsSnap = db.child(FirebasePaths.CHATS).get().await()
        val roomIds = chatsSnap.children.mapNotNull { it.key }.filter { roomId ->
            roomId.split("_").size == 2 && userId in roomId.split("_")
        }
        for (roomId in roomIds) {
            clearChat(roomId)
            synchronized(roomLock) {
                optimisticFlows[roomId]?.value = emptyList()
            }
        }
    }

    fun setPresence(userId: String, online: Boolean) {
        val presenceRef = db.child(FirebasePaths.PRESENCE).child(userId)
        if (online) {
            presenceRef.setValue(
                mapOf("status" to "online", "lastSeen" to System.currentTimeMillis())
            )
            // Когда клиент отключится (закрытие приложения, сеть пропала) — сервер сам поставит offline
            presenceRef.onDisconnect().setValue(
                mapOf("status" to "offline", "lastSeen" to ServerValue.TIMESTAMP)
            )
        } else {
            presenceRef.setValue(
                mapOf("status" to "offline", "lastSeen" to System.currentTimeMillis())
            )
        }
    }

    fun presence(userId: String): Flow<Boolean> = callbackFlow {
        val ref = db.child(FirebasePaths.PRESENCE).child(userId).child("status")
        val listener = object : ValueEventListener {
            override fun onDataChange(snap: DataSnapshot) {
                val status = snap.getValue(String::class.java)
                trySend(status == "online")
            }
            override fun onCancelled(e: DatabaseError) {
                trySend(false)
            }
            }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }
}
