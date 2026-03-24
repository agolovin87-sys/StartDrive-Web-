package com.example.startdrive.ui.instructor

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.item
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Reply
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import com.example.startdrive.R
import com.example.startdrive.data.model.ChatMessage
import coil.compose.AsyncImage
import com.example.startdrive.data.model.User
import com.example.startdrive.data.repository.ChatRepository
import com.example.startdrive.data.repository.HiddenChatMessages
import com.example.startdrive.data.repository.UserRepository
import com.example.startdrive.ui.chat.ChatBubble
import com.example.startdrive.ui.chat.ChatVoiceInput
import com.example.startdrive.ui.chat.ChatNotificationSound
import com.example.startdrive.ui.chat.colorForContactId
import com.example.startdrive.ui.theme.OnlineGreen

@Composable
fun InstructorChatTab(
    currentUserId: String,
    selectedContactId: String?,
    onSelectedContactIdChange: (String?) -> Unit,
) {
    val userRepo = remember { UserRepository() }
    val chatRepo = ChatRepository
    var cadets by remember { mutableStateOf<List<User>>(emptyList()) }
    val adminUsers by userRepo.usersByRole("admin").collectAsState(initial = emptyList())
    val allInstructors by userRepo.usersByRole("instructor").collectAsState(initial = emptyList())
    LaunchedEffect(currentUserId) {
        val inst = userRepo.getUser(currentUserId)
        cadets = inst?.assignedCadets?.mapNotNull { userRepo.getUser(it) } ?: emptyList()
    }
    val peerInstructors = remember(allInstructors, currentUserId) {
        allInstructors.filter { it.id != currentUserId }.sortedBy { it.fullName }
    }
    val admin = adminUsers.firstOrNull()
    val contacts = remember(admin, peerInstructors, cadets) {
        (listOfNotNull(admin) + peerInstructors + cadets).distinctBy { it.id }
    }
    var messageText by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val selectedContact = remember(selectedContactId, contacts) { selectedContactId?.let { id -> contacts.find { it.id == id } } }

    LaunchedEffect(contacts.map { it.id }, currentUserId) {
        contacts.forEach { c ->
            chatRepo.messages(chatRepo.chatRoomId(currentUserId, c.id))
        }
    }

    Column(Modifier.fillMaxSize()) {
        if (selectedContactId == null) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(0.dp),
            ) {
                if (peerInstructors.isNotEmpty()) {
                    item(key = "section_instructors") {
                        InstructorChatContactsSectionTitle("Инструкторы")
                    }
                    items(peerInstructors, key = { it.id }) { u ->
                        InstructorChatContactRow(
                            user = u,
                            currentUserId = currentUserId,
                            onSelectContact = onSelectedContactIdChange,
                        )
                    }
                }
                if (cadets.isNotEmpty()) {
                    item(key = "section_cadets") {
                        InstructorChatContactsSectionTitle("Курсанты")
                    }
                    items(cadets, key = { it.id }) { u ->
                        InstructorChatContactRow(
                            user = u,
                            currentUserId = currentUserId,
                            onSelectContact = onSelectedContactIdChange,
                        )
                    }
                }
                if (admin != null) {
                    item(key = "section_other") {
                        InstructorChatContactsSectionTitle("Другие")
                    }
                    item(key = admin.id) {
                        InstructorChatContactRow(
                            user = admin,
                            currentUserId = currentUserId,
                            onSelectContact = onSelectedContactIdChange,
                        )
                    }
                }
            }
        } else {
            val contact = selectedContact
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(vertical = 8.dp, horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = { onSelectedContactIdChange(null) }) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Назад к списку",
                        tint = MaterialTheme.colorScheme.onPrimary,
                    )
                }
                if (contact != null) {
                    val avatarColor = colorForContactId(contact.id)
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .then(
                                if (!contact.chatAvatarUrl.isNullOrBlank()) Modifier
                                else Modifier.background(avatarColor)
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (!contact.chatAvatarUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = contact.chatAvatarUrl,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop,
                            )
                        } else {
                            Text(
                                contact.initials(),
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                            )
                        }
                    }
                    Text(
                        contact.fullName,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(start = 12.dp),
                        maxLines = 1,
                    )
                }
            }
        Box(Modifier.weight(1f).fillMaxSize()) {
            if (selectedContactId != null) {
                val chatRoomId = remember(selectedContactId, currentUserId) {
                    chatRepo.chatRoomId(currentUserId, selectedContactId!!)
                }
                val messagesFlow = remember(chatRoomId) { chatRepo.messages(chatRoomId) }
                val messages by messagesFlow.collectAsState()
                val optimisticFlow = remember(chatRoomId) { chatRepo.optimisticMessages(chatRoomId) }
                val optimisticMessages by optimisticFlow.collectAsState()
                var menuMessage by remember { mutableStateOf<ChatMessage?>(null) }
                var replyingTo by remember { mutableStateOf<ChatMessage?>(null) }
                var hiddenKey by remember { mutableStateOf(0) }
                val hiddenIds = remember(chatRoomId, currentUserId, hiddenKey) {
                    HiddenChatMessages.getSet(context, currentUserId, chatRoomId)
                }
                val displayList = remember(messages, optimisticMessages, hiddenIds) {
                    val fromServer = messages.filter { it.id !in hiddenIds }
                    val merged = fromServer + optimisticMessages.filter { opt ->
                        opt.id !in hiddenIds && !fromServer.any { f ->
                            f.senderId == opt.senderId && f.text == opt.text
                        }
                    }
                    merged.sortedBy { it.timestamp }
                }
                ChatNotificationSound(messages = messages, currentUserId = currentUserId, chatRoomId = chatRoomId)
                LaunchedEffect(chatRoomId, currentUserId) {
                    chatRepo.markRead(chatRoomId, currentUserId)
                }
                val listState = rememberLazyListState()
                val focusManager = LocalFocusManager.current
                LaunchedEffect(displayList.size) {
                    if (displayList.isNotEmpty()) listState.animateScrollToItem(0)
                }
                Box(Modifier.fillMaxSize()) {
                    Image(
                        painter = painterResource(R.drawable.chat_background),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(start = 8.dp, top = 8.dp, end = 8.dp, bottom = 72.dp)
                            .pointerInput(Unit) { detectTapGestures(onTap = { focusManager.clearFocus() }) },
                        reverseLayout = true,
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        items(displayList.reversed(), key = { it.id.ifEmpty { "opt_${it.timestamp}" } }) { msg ->
                            ChatBubble(message = msg, isOutgoing = msg.senderId == currentUserId, onLongPress = { menuMessage = msg })
                        }
                    }
                    if (menuMessage != null) {
                        val msg = menuMessage!!
                        AlertDialog(
                            onDismissRequest = { menuMessage = null },
                            title = { Text("Сообщение") },
                            text = {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    if (!msg.isVoice) {
                                        TextButton(onClick = { replyingTo = msg; menuMessage = null }) {
                                            Icon(Icons.Default.Reply, contentDescription = null, modifier = Modifier.size(20.dp), tint = Color(0xFF212121))
                                            Text("Ответить", color = Color(0xFF212121), modifier = Modifier.padding(start = 8.dp))
                                        }
                                        TextButton(onClick = {
                                            (context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(ClipData.newPlainText(null, msg.text))
                                            menuMessage = null
                                        }) {
                                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(20.dp), tint = Color(0xFF212121))
                                            Text("Копировать", color = Color(0xFF212121), modifier = Modifier.padding(start = 8.dp))
                                        }
                                    }
                                    TextButton(onClick = {
                                        if (msg.isVoice) scope.launch { chatRepo.deleteMessage(chatRoomId, msg.id, isVoice = true) }
                                        else { HiddenChatMessages.add(context, currentUserId, chatRoomId, msg.id); hiddenKey++ }
                                        menuMessage = null
                                    }) {
                                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(20.dp), tint = Color(0xFF212121))
                                        Text("Удалить", color = Color(0xFF212121), modifier = Modifier.padding(start = 8.dp))
                                    }
                                }
                            },
                            confirmButton = { TextButton(onClick = { menuMessage = null }) { Text("Отмена") } },
                        )
                    }
                    Column(modifier = Modifier.align(Alignment.BottomCenter)) {
                        if (replyingTo != null) {
                            Row(
                                Modifier.fillMaxWidth().background(Color.White.copy(alpha = 0.9f)).padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text("Ответ на: ${(replyingTo?.text?.take(50) ?: "голосовое").let { if (it.length == 50) "$it…" else it }}", style = MaterialTheme.typography.bodySmall, color = Color(0xFF212121), modifier = Modifier.weight(1f), maxLines = 1)
                                IconButton(onClick = { replyingTo = null }, modifier = Modifier.size(36.dp)) {
                                    Icon(Icons.Default.Delete, contentDescription = "Отменить ответ", modifier = Modifier.size(20.dp), tint = Color(0xFF212121))
                                }
                            }
                        }
                        ChatVoiceInput(
                            messageText = messageText,
                            onMessageTextChange = { messageText = it },
                            onSendText = {
                                if (messageText.isNotBlank()) {
                                    val text = messageText
                                    val replyTo = replyingTo
                                    messageText = ""
                                    replyingTo = null
                                    val optMsg = ChatMessage(senderId = currentUserId, text = text, timestamp = System.currentTimeMillis(), status = "sent", replyToMessageId = replyTo?.id, replyToText = replyTo?.text?.take(100))
                                    chatRepo.addOptimisticMessage(chatRoomId, optMsg)
                                    scope.launch {
                                        try {
                                            chatRepo.sendMessage(chatRoomId, currentUserId, text, replyTo?.id, replyTo?.text?.take(100))
                                        } catch (e: Exception) {
                                            messageText = text
                                            replyingTo = replyTo
                                            chatRepo.removeOptimisticMessage(chatRoomId, optMsg)
                                            Toast.makeText(context, "Не удалось отправить: ${e.message ?: "ошибка сети или доступа"}", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                }
                            },
                            onSendVoice = { file, durationSec ->
                                val optMsg = ChatMessage(senderId = currentUserId, text = "", timestamp = System.currentTimeMillis(), status = "sent", voiceUrl = null, voiceDurationSec = durationSec)
                                chatRepo.addOptimisticMessage(chatRoomId, optMsg)
                                scope.launch {
                                    try {
                                        chatRepo.sendVoiceMessage(chatRoomId, currentUserId, file, durationSec)
                                    } catch (e: Exception) {
                                        chatRepo.removeOptimisticMessage(chatRoomId, optMsg)
                                        Toast.makeText(context, "Не удалось отправить голосовое: ${e.message ?: "ошибка"}", Toast.LENGTH_LONG).show()
                                    }
                                }
                            },
                        )
                    }
                }
            } else {
                Text(
                    "Выберите контакт",
                    modifier = Modifier.align(Alignment.Center).padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
        }
    }
}

@Composable
private fun InstructorChatContactsSectionTitle(title: String) {
    Text(
        title,
        modifier = Modifier.padding(horizontal = 16.dp, top = 12.dp, bottom = 4.dp),
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
    )
}

@Composable
private fun InstructorChatContactRow(
    user: User,
    currentUserId: String,
    onSelectContact: (String?) -> Unit,
) {
    val chatRepo = ChatRepository
    val online by chatRepo.presence(user.id).collectAsState(initial = false)
    val unread by chatRepo.unreadCount(chatRepo.chatRoomId(currentUserId, user.id), currentUserId).collectAsState(0)
    val avatarColor = colorForContactId(user.id)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelectContact(user.id) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .then(
                    if (!user.chatAvatarUrl.isNullOrBlank()) Modifier
                    else Modifier.background(avatarColor),
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (!user.chatAvatarUrl.isNullOrBlank()) {
                AsyncImage(
                    model = user.chatAvatarUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            } else {
                Text(
                    user.initials(),
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                )
            }
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp),
        ) {
            Text(
                user.fullName,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
            )
            Text(
                if (online) "в сети" else "не в сети",
                style = MaterialTheme.typography.bodySmall,
                color = if (online) OnlineGreen else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (unread > 0) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.error),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    if (unread > 99) "99+" else "$unread",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onError,
                )
            }
        }
    }
}
