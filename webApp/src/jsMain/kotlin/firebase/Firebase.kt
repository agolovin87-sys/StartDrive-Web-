package firebase

import ChatContactStorageStats
import ChatGroupStorageStats
import StorageBucketUsageBreakdown
import com.example.startdrive.shared.FirebasePaths
import com.example.startdrive.shared.model.User
import kotlinx.browser.window
import kotlin.js.Promise
import kotlin.js.json

private var authInstance: dynamic = null
private var firestoreInstance: dynamic = null
/** Ссылка на firebase compat (firebase.firestore.Timestamp). */
private var firebaseCompat: dynamic = null

/**
 * Конфиг Firebase — подставь свои значения из Firebase Console (Project settings).
 * Либо задай в консоли браузера: window.__FIREBASE_CONFIG__ = { apiKey: "...", ... }
 */
fun getFirebaseConfig(): dynamic {
    val w = window.asDynamic()
    if (w.__FIREBASE_CONFIG__ != undefined) return w.__FIREBASE_CONFIG__
    return json(
        "apiKey" to "YOUR_WEB_API_KEY",
        "authDomain" to "startdrive-573fa.firebaseapp.com",
        "projectId" to "startdrive-573fa",
        "databaseURL" to "https://startdrive-573fa-default-rtdb.europe-west1.firebasedatabase.app",
        "storageBucket" to "startdrive-573fa.firebasestorage.app",
        "messagingSenderId" to "YOUR_SENDER_ID",
        "appId" to "YOUR_APP_ID",
    )
}

private var databaseInstance: dynamic = null
private var storageInstance: dynamic = null
private var functionsInstance: dynamic = null

fun initFirebase() {
    if (authInstance != null) return
    val firebase = js("require('firebase/compat/app')")
    js("require('firebase/compat/auth')")
    js("require('firebase/compat/firestore')")
    js("require('firebase/compat/database')")
    js("require('firebase/compat/storage')")
    js("require('firebase/compat/functions')")
    firebaseCompat = firebase
    val config = getFirebaseConfig()
    val app = firebase.initializeApp(config)
    authInstance = firebase.auth(app)
    firestoreInstance = firebase.firestore(app)
    databaseInstance = firebase.database(app)
    storageInstance = firebase.storage(app)
    // Регион europe-west1 — Callable uploadChatAvatar задеплоена там. Compat app.functions(region) может игнорировать регион, используем модульный getFunctions.
    functionsInstance = try {
        val getFunctionsModular = js("require('firebase/functions').getFunctions")
        (getFunctionsModular as (dynamic, String) -> dynamic)(app, "europe-west1")
    } catch (_: Throwable) {
        (app.asDynamic()).functions("europe-west1")
    }
}

/** Текущее время как Firestore Timestamp (через firebase.firestore.Timestamp). */
fun getFirestoreTimestampNow(): dynamic {
    val ts = firebaseCompat?.firestore?.Timestamp
    return (ts?.now?.unsafeCast<dynamic>())?.call(ts) ?: js("new Date()")
}

private val newDateFromMillis = js("(function(ms){ return new Date(ms); })").unsafeCast<(Long) -> dynamic>()

/** Firestore Timestamp из миллисекунд. */
fun getFirestoreTimestampFromMillis(ms: Long): dynamic {
    val ts = firebaseCompat?.firestore?.Timestamp
    return (ts?.fromMillis?.unsafeCast<dynamic>())?.call(ts, ms) ?: newDateFromMillis(ms)
}

fun getAuth(): dynamic = authInstance
fun getFirestore(): dynamic = firestoreInstance
fun getDatabase(): dynamic = databaseInstance
fun getStorage(): dynamic = storageInstance
fun getFunctions(): dynamic? = functionsInstance

/** Плейсхолдер времени сервера для Realtime Database (подставить в timestamp при записи). */
fun getDatabaseServerTimestamp(): dynamic =
    (firebaseCompat?.database?.unsafeCast<dynamic>()?.ServerValue)?.TIMESTAMP
        ?: js("Date.now()")

fun onAuthStateChanged(callback: (String?) -> Unit) {
    getAuth().onAuthStateChanged { user: dynamic ->
        callback(user?.uid as? String)
    }
}

fun signIn(email: String, password: String): Promise<Unit> =
    getAuth().signInWithEmailAndPassword(email, password).then { js("undefined") }

fun signOut(): Promise<Unit> = getAuth().signOut()

/** Выход с предварительным сбросом статуса «онлайн» в Realtime Database. */
fun signOutAndClearPresence(): Promise<Unit> {
    getCurrentUserId()?.let { setPresence(it, false) }
    return signOut()
}

fun register(
    fullName: String,
    email: String,
    phone: String,
    password: String,
    role: String,
): Promise<Unit> {
    val auth = getAuth()
    val firestore = getFirestore()
    return auth.createUserWithEmailAndPassword(email, password).then { result: dynamic ->
        val uid = result?.user?.uid ?: throw js("Error('No user id')")
        val isActive = role == "admin"
        val data = json(
            "fullName" to fullName,
            "email" to email,
            "phone" to phone,
            "role" to role,
            "balance" to 0,
            "isActive" to isActive,
            "createdAt" to getFirestoreTimestampNow(),
        )
        firestore.collection(FirebasePaths.USERS).doc(uid).set(data)
    }.then { js("undefined") }
}

fun getCurrentUserId(): String? = getAuth().currentUser?.uid as? String

fun updateProfile(uid: String, fullName: String, phone: String, callback: (String?) -> Unit) {
    getFirestore().collection(FirebasePaths.USERS).doc(uid)
        .update(kotlin.js.json("fullName" to fullName, "phone" to phone))
        .then { callback(null) }
        .catch { e: Throwable -> callback((e.asDynamic().message as? String) ?: "Ошибка") }
}

fun changePassword(newPassword: String): Promise<Unit> {
    val user = getAuth().currentUser ?: return Promise.reject(js("Error('Not signed in')"))
    return user.updatePassword(newPassword).then { js("undefined") }
}

private fun parseUserFromDoc(doc: dynamic, d: dynamic): User {
    val data = d?.unsafeCast<dynamic>() ?: js("{}")
    val id = doc.id as String
    val fullName = (data.fullName as? String) ?: ""
    val email = (data.email as? String) ?: ""
    val phone = (data.phone as? String) ?: ""
    val role = (data.role as? String) ?: ""
    val balance = (data.balance as? Number)?.toInt() ?: 0
    val isActive = data.isActive as? Boolean ?: false
    val assignedInstructorId = data.assignedInstructorId as? String
    val assignedCadetsRaw = data.assignedCadets
    val assignedCadets = (assignedCadetsRaw as? Array<*>)?.mapNotNull { it?.toString() } ?: emptyList<String>()
    @Suppress("UNCHECKED_CAST_TO_NATIVE_INTERFACE")
    val chatAvatarUrl = (data["chatAvatarUrl"] as? String) ?: (data.chatAvatarUrl as? String)
    val cadetGroupIdRaw = (data["cadetGroupId"] as? String) ?: (data.cadetGroupId as? String)
    val cadetGroupId = cadetGroupIdRaw?.takeIf { it.isNotBlank() }
    val trainingVehicleRaw = (data["trainingVehicle"] as? String) ?: (data.trainingVehicle as? String)
    val trainingVehicle = trainingVehicleRaw?.trim()?.takeIf { it.isNotBlank() }
    return User(
        id = id,
        fullName = fullName,
        email = email,
        phone = phone,
        role = role,
        balance = balance,
        isActive = isActive,
        createdAtMillis = null,
        assignedInstructorId = assignedInstructorId,
        assignedCadets = assignedCadets,
        chatAvatarUrl = chatAvatarUrl,
        cadetGroupId = cadetGroupId,
        trainingVehicle = trainingVehicle,
    )
}

/** Учебное ТС для инструктора (только админ в UI). */
fun setInstructorTrainingVehicle(instructorId: String, value: String?, callback: (String?) -> Unit) {
    val firestore = getFirestore()
    val payload = js("{}").unsafeCast<dynamic>()
    if (value.isNullOrBlank()) {
        payload.trainingVehicle = js("firebase.firestore.FieldValue.delete()")
    } else {
        payload.trainingVehicle = value.trim()
    }
    firestore.collection(FirebasePaths.USERS).doc(instructorId)
        .update(payload)
        .then { callback(null) }
        .catch { e: Throwable -> callback((e.asDynamic().message as? String) ?: "Ошибка") }
}

/** Обновить привязку курсанта к группе (null — снять группу). */
fun setUserCadetGroup(cadetId: String, groupId: String?, callback: (String?) -> Unit) {
    val firestore = getFirestore()
    val payload = js("{}").unsafeCast<dynamic>()
    if (groupId.isNullOrBlank()) {
        payload.cadetGroupId = js("firebase.firestore.FieldValue.delete()")
    } else {
        payload.cadetGroupId = groupId
    }
    firestore.collection(FirebasePaths.USERS).doc(cadetId)
        .update(payload)
        .then { callback(null) }
        .catch { e: Throwable -> callback((e.asDynamic().message as? String) ?: "Ошибка") }
}

fun getCurrentUser(callback: (User?, errorMessage: String?) -> Unit) {
    val uid = getCurrentUserId()
    if (uid == null) {
        callback(null, null)
        return
    }
    getFirestore()
        .collection(FirebasePaths.USERS)
        .doc(uid)
        .get()
        .then { doc: dynamic ->
            val exists = doc?.exists
            if (exists != true) {
                callback(null, "Профиль не найден в базе. Войдите через приложение или зарегистрируйтесь на сайте.")
                return@then
            }
            val d = (doc.unsafeCast<dynamic>()).data()
            if (d == null) {
                callback(null, "Данные профиля пусты.")
                return@then
            }
            val user = parseUserFromDoc(doc, d)
            callback(user, null)
        }
        .catch { e: Throwable ->
            val code = e.asDynamic().code as? String
            val msg = when (code) {
                "permission-denied" -> "Нет доступа к базе. Проверьте правила Firestore для коллекции users."
                else -> (e.asDynamic().message as? String) ?: "Ошибка Firestore: ${e.message}"
            }
            callback(null, msg)
        }
}

/**
 * Подписка на изменения документа текущего пользователя в Firestore (баланс, ФИО и т.д.).
 * Нужна, чтобы баланс в профиле обновлялся сразу после списания/зачисления с другого клиента или функции.
 * Возвращает функцию отписки; вызывать при выходе из аккаунта.
 */
fun subscribeCurrentUserDocument(onUpdate: (User) -> Unit): () -> Unit {
    val uid = getCurrentUserId() ?: return {}
    val ref = getFirestore().collection(FirebasePaths.USERS).doc(uid)
    val unsubscribe = ref.onSnapshot { snap: dynamic ->
        if (snap?.exists != true) return@onSnapshot
        val docD = snap.unsafeCast<dynamic>()
        val d = docD.data()
        if (d == null) return@onSnapshot
        onUpdate(parseUserFromDoc(docD, d))
    }
    return {
        try {
            unsubscribe.unsafeCast<dynamic>().invoke()
        } catch (_: Throwable) {
            try {
                js("(function(u){ try { if (typeof u === 'function') u(); } catch(e) {} })").unsafeCast<(dynamic) -> Unit>().invoke(unsubscribe)
            } catch (_: Throwable) { }
        }
    }
}

/**
 * Лента для администратора: документы пишут только Cloud Functions ([FirebasePaths.ADMIN_EVENTS]).
 * Нужна, чтобы веб-админка получала те же уведомления, что и FCM на устройстве (без токена в браузере).
 */
fun subscribeAdminEventsFeed(onMessage: (String) -> Unit): () -> Unit {
    val q = getFirestore().collection(FirebasePaths.ADMIN_EVENTS)
        .orderBy("createdAt", "desc")
        .limit(40)
    /** Первый снимок — только запоминаем id, без уведомлений (история). Дальше — любой новый id. */
    var firstSnapshot = true
    val knownIds = mutableSetOf<String>()
    val unsubscribe = q.onSnapshot({ snap: dynamic ->
        val snapD = snap.unsafeCast<dynamic>()
        val docs = snapD.docs
        val len = (docs.length as? Int) ?: 0
        if (firstSnapshot) {
            for (i in 0 until len) {
                val doc = docs[i].unsafeCast<dynamic>()
                val id = doc.id as? String ?: continue
                knownIds.add(id)
            }
            firstSnapshot = false
            return@onSnapshot
        }
        for (i in 0 until len) {
            val doc = docs[i].unsafeCast<dynamic>()
            val id = doc.id as? String ?: continue
            if (id in knownIds) continue
            knownIds.add(id)
            val dataFn = doc.data
            val d = dataFn.unsafeCast<dynamic>().call(doc) ?: continue
            val dd = d.unsafeCast<dynamic>()
            val title = (dd.title as? String) ?: ""
            val body = (dd.body as? String) ?: ""
            val text = if (title.isNotBlank()) "$title: $body" else body
            if (text.isNotBlank()) onMessage(text)
        }
    }) { err: dynamic ->
        val msg = (err?.message as? String) ?: "$err"
        js("console.error").unsafeCast<(Any?) -> Unit>().invoke("subscribeAdminEventsFeed: $msg")
    }
    return {
        try {
            unsubscribe.unsafeCast<dynamic>().invoke()
        } catch (_: Throwable) {
            try {
                js("(function(u){ try { if (typeof u === 'function') u(); } catch(e) {} })").unsafeCast<(dynamic) -> Unit>().invoke(unsubscribe)
            } catch (_: Throwable) { }
        }
    }
}

private fun adminEventCreatedAtToMillis(ts: dynamic): Long {
    if (ts == null || ts == undefined) return js("Date.now()").unsafeCast<Double>().toLong()
    return try {
        val toMillis = ts.toMillis?.unsafeCast<dynamic>()
        if (toMillis != null) (toMillis.call(ts) as? Number)?.toLong()
            ?: ((ts.seconds as? Number)?.toLong() ?: 0L) * 1000L
        else ((ts.seconds as? Number)?.toLong() ?: 0L) * 1000L
    } catch (_: Throwable) {
        js("Date.now()").unsafeCast<Double>().toLong()
    }
}

/**
 * Одна выгрузка последних событий (для экрана «Уведомления» и подстраховки, если live-подписка не успела).
 */
fun fetchAdminEventsForNotifications(callback: (List<Pair<Long, String>>) -> Unit) {
    getFirestore().collection(FirebasePaths.ADMIN_EVENTS)
        .orderBy("createdAt", "desc")
        .limit(50)
        .get()
        .then { snap: dynamic ->
            try {
                val docs = snap?.docs ?: js("[]")
                val len = (docs.length as? Int) ?: 0
                val list = mutableListOf<Pair<Long, String>>()
                for (i in 0 until len) {
                    val doc = docs[i].unsafeCast<dynamic>()
                    val dataFn = doc.data
                    val d = dataFn.unsafeCast<dynamic>().call(doc) ?: continue
                    val dd = d.unsafeCast<dynamic>()
                    val title = (dd.title as? String) ?: ""
                    val body = (dd.body as? String) ?: ""
                    val text = if (title.isNotBlank()) "$title: $body" else body
                    val ms = adminEventCreatedAtToMillis(dd.createdAt)
                    if (text.isNotBlank()) list.add(Pair(ms, text))
                }
                callback(list)
            } catch (_: Throwable) {
                callback(emptyList())
            }
        }
        .catch { _ -> callback(emptyList()) }
}

fun getUsers(callback: (List<User>) -> Unit) {
    getUsersWithError { list, _ -> callback(list) }
}

/** Вызов getUsers с передачей ошибки (для админки: показать сообщение при permission-denied и т.д.). */
fun getUsersWithError(callback: (List<User>, errorMessage: String?) -> Unit) {
    getFirestore().collection(FirebasePaths.USERS).get()
        .then { snap: dynamic ->
            try {
                val rawDocs = snap?.docs ?: js("[]")
                val len = (rawDocs.length as? Int) ?: 0
                val list = (0 until len).mapNotNull { i ->
                    val doc = rawDocs[i]
                    val d = (doc.unsafeCast<dynamic>()).data()
                    if (d == null) return@mapNotNull null
                    parseUserFromDoc(doc.unsafeCast<dynamic>(), d)
                }
                callback(list, null)
            } catch (e: Throwable) {
                callback(emptyList(), (e.asDynamic().message as? String) ?: "Ошибка чтения списка")
            }
        }
        .catch { e: Throwable ->
            val code = e.asDynamic().code as? String
            val msg = when (code) {
                "permission-denied" -> "Нет доступа к Firestore. Проверьте правила: чтение коллекции users для авторизованных."
                "unavailable" -> "Firestore недоступен. Проверьте интернет."
                else -> (e.asDynamic().message as? String) ?: "Ошибка загрузки списка пользователей"
            }
            callback(emptyList(), msg)
        }
}

fun getUserById(userId: String, callback: (User?) -> Unit) {
    getFirestore().collection(FirebasePaths.USERS).doc(userId).get()
        .then { doc: dynamic ->
            if (doc?.exists != true) {
                callback(null)
                return@then
            }
            val docD = doc.unsafeCast<dynamic>()
            val d = docD.data()
            if (d == null) {
                callback(null)
                return@then
            }
            callback(parseUserFromDoc(docD, d))
        }
        .catch { _ -> callback(null) }
}

fun setActive(userId: String, active: Boolean, callback: (String?) -> Unit) {
    getFirestore().collection(FirebasePaths.USERS).doc(userId)
        .update("isActive", active)
        .then { callback(null) }
        .catch { e: Throwable -> callback((e.asDynamic().message as? String) ?: "Ошибка") }
}

fun assignCadetToInstructor(instructorId: String, cadetId: String, callback: (String?) -> Unit) {
    val firestore = getFirestore()
    val instructorRef = firestore.collection(FirebasePaths.USERS).doc(instructorId)
    val cadetRef = firestore.collection(FirebasePaths.USERS).doc(cadetId)
    firestore.runTransaction { transaction: dynamic ->
        transaction.get(instructorRef).then { snap: dynamic ->
            val data = (snap.unsafeCast<dynamic>()).data()
            val existing = (data?.assignedCadets as? Array<*>)?.toList()?.mapNotNull { it?.toString() } ?: emptyList<String>()
            val list = existing.toMutableList()
            if (!list.contains(cadetId)) list.add(cadetId)
            transaction.update(instructorRef, kotlin.js.json("assignedCadets" to list.toTypedArray()))
            transaction.update(cadetRef, kotlin.js.json("assignedInstructorId" to instructorId))
        }
    }.then { callback(null) }
     .catch { e: Throwable -> callback((e.asDynamic().message as? String) ?: "Ошибка") }
}

fun removeCadetFromInstructor(instructorId: String, cadetId: String, callback: (String?) -> Unit) {
    val firestore = getFirestore()
    val instructorRef = firestore.collection(FirebasePaths.USERS).doc(instructorId)
    val cadetRef = firestore.collection(FirebasePaths.USERS).doc(cadetId)
    firestore.runTransaction { transaction: dynamic ->
        transaction.get(instructorRef).then { snap: dynamic ->
            val data = (snap.unsafeCast<dynamic>()).data()
            val list = ((data?.assignedCadets as? Array<*>)?.toList()?.mapNotNull { it?.toString() } ?: emptyList<String>()).toMutableList()
            list.remove(cadetId)
            transaction.update(instructorRef, kotlin.js.json("assignedCadets" to list.toTypedArray()))
            transaction.update(cadetRef, kotlin.js.json("assignedInstructorId" to null))
        }
    }.then { callback(null) }
     .catch { e: Throwable -> callback((e.asDynamic().message as? String) ?: "Ошибка") }
}

fun deleteUser(userId: String, callback: (String?) -> Unit) {
    getFirestore().collection(FirebasePaths.USERS).doc(userId).delete()
        .then { callback(null) }
        .catch { e: Throwable -> callback((e.asDynamic().message as? String) ?: "Ошибка") }
}

/** Путь к аватару пользователя в Firebase Storage (правила: storage.rules). */
private fun chatAvatarStoragePath(uid: String): String = "users/$uid/chat_avatar.png"

/** После загрузки в Storage записывает chatAvatarUrl в Firestore, чтобы аватар видели другие пользователи. */
private fun syncChatAvatarUrlToFirestore(uid: String, onDone: (String?) -> Unit) {
    val storage = getStorage() ?: run { onDone(null); return }
    val firestore = getFirestore()
    val path = chatAvatarStoragePath(uid)
    storage.ref(path).getDownloadURL()
        .then { url: dynamic ->
            firestore.collection(FirebasePaths.USERS).doc(uid).update(kotlin.js.json("chatAvatarUrl" to (url as String)))
        }
        .then { onDone(null) }
        .catch { e: dynamic ->
            val msg = (e?.message as? String) ?: "Ошибка записи URL аватара"
            js("if(typeof console!=='undefined'&&console.error)console.error('StartDrive syncChatAvatarUrlToFirestore:', msg)")
            onDone(null)
        }
}

/** Загружает аватар: сначала через Callable (без CORS), при ошибке — напрямую в Storage. */
fun uploadChatAvatar(uid: String, dataUrl: String, callback: (String?) -> Unit) {
    fun friendlyMessage(msg: String): String = when {
        msg.contains("Failed to fetch", ignoreCase = true) || msg.contains("NetworkError", ignoreCase = true) ->
            "Нет соединения с интернетом. Проверьте сеть и повторите."
        msg.contains("unauthenticated", ignoreCase = true) -> "Войдите в аккаунт и повторите."
        msg.contains("invalid-argument", ignoreCase = true) -> "Неверные данные. Попробуйте другую фотографию."
        else -> msg
    }
    val functions = getFunctions()
    // Модульный getFunctions() не имеет .httpsCallable — используем модульный httpsCallable из firebase/functions
    val callable = if (functions != null) {
        val httpsCallableFn = js("require('firebase/functions').httpsCallable")
        (httpsCallableFn as (dynamic, String) -> dynamic)(functions, "uploadChatAvatar")
    } else null
    if (callable != null) {
        js("if(typeof console!=='undefined'&&console.log)console.log('StartDrive: загрузка аватара через Callable')")
        val payload = kotlin.js.json("uid" to uid, "dataUrl" to dataUrl)
        (callable as (dynamic) -> dynamic)(payload).then { _: dynamic ->
            syncChatAvatarUrlToFirestore(uid, callback)
        }.catch { e: dynamic ->
            val code = (e?.code as? String) ?: ""
            val msg = (e?.message as? String) ?: "Ошибка загрузки"
            js("if(typeof console!=='undefined'&&console.error)console.error('StartDrive uploadChatAvatar callable error:', msg, e)")
            // Не переходим на прямую загрузку — показываем реальную ошибку пользователю
            callback(friendlyMessage("$code $msg".trim()))
        }
    } else {
        js("if(typeof console!=='undefined'&&console.log)console.log('StartDrive: Callable недоступен, загрузка напрямую в Storage')")
        uploadChatAvatarDirect(uid, dataUrl, callback)
    }
}

/** Конвертирует data URL в Blob без fetch (чтобы не нарушать CSP connect-src). */
private fun dataUrlToBlob(dataUrl: String): dynamic =
    js("(function(dataUrl){ var base64 = dataUrl.replace(/^data:image\\/\\w+;base64,/, ''); var binary = atob(base64); var len = binary.length; var arr = new Uint8Array(len); for(var i=0;i<len;i++) arr[i]=binary.charCodeAt(i); return new Blob([arr], { type: 'image/png' }); })").unsafeCast<(String) -> dynamic>()(dataUrl)

private fun uploadChatAvatarDirect(uid: String, dataUrl: String, callback: (String?) -> Unit) {
    val storage = getStorage() ?: run { callback("Storage не доступен"); return }
    val firestore = getFirestore()
    val path = chatAvatarStoragePath(uid)
    val storageRef = storage.ref(path)
    val blob = dataUrlToBlob(dataUrl)
    storageRef.put(blob, kotlin.js.json("contentType" to "image/png")).then { _: dynamic ->
        storageRef.getDownloadURL()
    }.then { url: dynamic ->
        firestore.collection(FirebasePaths.USERS).doc(uid).update(kotlin.js.json("chatAvatarUrl" to (url as String)))
    }.then { callback(null) }.catch { e: dynamic ->
        val msg = (e?.message as? String) ?: "Ошибка загрузки аватара"
        js("if(typeof console!=='undefined'&&console.error)console.error('StartDrive avatar upload error:', msg)")
        val friendly = when {
            msg.contains("Failed to fetch", ignoreCase = true) || msg.contains("NetworkError", ignoreCase = true) ->
                "Нет соединения с интернетом. Проверьте сеть и повторите."
            else -> msg
        }
        callback(friendly)
    }
}

/** Путь к аватару группового чата (storage.rules: chats/group_avatars/{groupId}/). */
private fun chatGroupAvatarStoragePath(groupId: String): String = "chats/group_avatars/$groupId/avatar.png"

/**
 * Обновляет ID-токен перед Storage — иначе правила с `request.auth` часто дают storage/unauthorized.
 */
private fun withFreshAuthTokenForStorage(onReady: () -> Unit, onError: (String?) -> Unit) {
    val user = getAuth()?.unsafeCast<dynamic>()?.currentUser
    if (user == null) {
        onError("Войдите в аккаунт — без Firebase Auth Storage вернёт «нет доступа».")
        return
    }
    val promise = user.getIdToken(true).unsafeCast<dynamic>()
    promise.then { _: dynamic -> onReady() }.unsafeCast<dynamic>().`catch` { e: dynamic ->
        onError((e?.message as? String) ?: "Не удалось обновить токен для Storage")
    }
}

/** Загружает аватар группы (Callable → Admin SDK, как uploadChatAvatar; иначе прямой PUT в Storage). */
fun uploadChatGroupAvatar(groupId: String, dataUrl: String, callback: (String?) -> Unit) {
    fun friendlyMessage(msg: String): String = when {
        msg.contains("Failed to fetch", ignoreCase = true) || msg.contains("NetworkError", ignoreCase = true) ->
            "Нет соединения с интернетом. Проверьте сеть и повторите."
        msg.contains("unauthenticated", ignoreCase = true) -> "Войдите в аккаунт и повторите."
        msg.contains("invalid-argument", ignoreCase = true) -> "Неверные данные. Попробуйте другую фотографию."
        msg.contains("permission-denied", ignoreCase = true) -> "Нет прав на изменение аватара группы."
        msg.contains("not-found", ignoreCase = true) -> "Группа не найдена."
        else -> msg
    }
    val functions = getFunctions()
    val callable = if (functions != null) {
        val httpsCallableFn = js("require('firebase/functions').httpsCallable")
        (httpsCallableFn as (dynamic, String) -> dynamic)(functions, "uploadChatGroupAvatar")
    } else null
    if (callable != null) {
        js("if(typeof console!=='undefined'&&console.log)console.log('StartDrive: аватар группы через Callable')")
        val payload = kotlin.js.json("groupId" to groupId, "dataUrl" to dataUrl)
        (callable as (dynamic) -> dynamic)(payload).then { _: dynamic ->
            callback(null)
        }.catch { e: dynamic ->
            val code = (e?.code as? String) ?: ""
            val msg = (e?.message as? String) ?: "Ошибка загрузки"
            js("if(typeof console!=='undefined'&&console.error)console.error('StartDrive uploadChatGroupAvatar callable:', msg, e)")
            callback(friendlyMessage("$code $msg".trim()))
        }
    } else {
        uploadChatGroupAvatarDirect(groupId, dataUrl, callback)
    }
}

private fun uploadChatGroupAvatarDirect(groupId: String, dataUrl: String, callback: (String?) -> Unit) {
    withFreshAuthTokenForStorage(
        onReady = {
            val storage = getStorage() ?: run { callback("Storage не доступен"); return@withFreshAuthTokenForStorage }
            val firestore = getFirestore()
            val path = chatGroupAvatarStoragePath(groupId)
            val storageRef = storage.ref(path)
            val blob = dataUrlToBlob(dataUrl)
            storageRef.put(blob, kotlin.js.json("contentType" to "image/png")).then { _: dynamic ->
                storageRef.getDownloadURL()
            }.then { url: dynamic ->
                firestore.collection(FirebasePaths.CHAT_GROUPS).doc(groupId).update(kotlin.js.json("chatAvatarUrl" to (url as String)))
            }.then { callback(null) }.catch { e: dynamic ->
                val msg = (e?.message as? String) ?: "Ошибка загрузки аватара группы"
                callback(
                    when {
                        msg.contains("Failed to fetch", ignoreCase = true) || msg.contains("NetworkError", ignoreCase = true) ->
                            "Нет соединения с интернетом. Проверьте сеть и повторите."
                        else -> msg
                    }
                )
            }
        },
        onError = callback,
    )
}

/** Удаляет файл аватара группы и обнуляет chatAvatarUrl в Firestore. */
fun removeChatGroupAvatar(groupId: String, callback: (String?) -> Unit) {
    fun friendlyMessage(msg: String): String = when {
        msg.contains("Failed to fetch", ignoreCase = true) || msg.contains("NetworkError", ignoreCase = true) ->
            "Нет соединения с интернетом."
        msg.contains("permission-denied", ignoreCase = true) -> "Нет прав."
        else -> msg
    }
    val functions = getFunctions()
    val callable = if (functions != null) {
        val httpsCallableFn = js("require('firebase/functions').httpsCallable")
        (httpsCallableFn as (dynamic, String) -> dynamic)(functions, "removeChatGroupAvatar")
    } else null
    if (callable != null) {
        val payload = kotlin.js.json("groupId" to groupId)
        (callable as (dynamic) -> dynamic)(payload).then { _: dynamic ->
            callback(null)
        }.catch { e: dynamic ->
            val code = (e?.code as? String) ?: ""
            val msg = (e?.message as? String) ?: "Ошибка"
            callback(friendlyMessage("$code $msg".trim()))
        }
    } else {
        withFreshAuthTokenForStorage(
            onReady = {
                val firestore = getFirestore()
                firestore.collection(FirebasePaths.CHAT_GROUPS).doc(groupId).update(kotlin.js.json("chatAvatarUrl" to ""))
                    .then {
                        val storage = getStorage()
                        if (storage != null) {
                            val ref = storage.ref(chatGroupAvatarStoragePath(groupId))
                            ref.delete().catch { _: dynamic -> js("undefined") }
                        } else
                            js("undefined")
                    }
                    .then { callback(null) }
                    .catch { e: Throwable ->
                        val msg = (e.asDynamic().message as? String) ?: "Ошибка"
                        callback(if (msg.contains("Failed to fetch", ignoreCase = true)) "Нет соединения с интернетом." else msg)
                    }
            },
            onError = callback,
        )
    }
}

/**
 * Загрузка файла в чат (участники комнаты: личный чат или группа). Callable → Admin SDK → URL с download token.
 * [callback]: (ошибка или null, url или null).
 */
fun uploadChatAdminFile(
    roomId: String,
    fileName: String,
    contentType: String,
    base64: String,
    callback: (String?, String?) -> Unit,
) {
    fun friendlyMessage(msg: String): String = when {
        msg.contains("Failed to fetch", ignoreCase = true) || msg.contains("NetworkError", ignoreCase = true) ->
            "Нет соединения с интернетом. Проверьте сеть и повторите."
        msg.contains("unauthenticated", ignoreCase = true) -> "Войдите в аккаунт и повторите."
        msg.contains("permission-denied", ignoreCase = true) -> "Нет доступа к этой переписке или группе."
        msg.contains("invalid-argument", ignoreCase = true) -> "Неверные данные или файл слишком большой (макс. 8 МБ)."
        else -> msg
    }
    val functions = getFunctions()
    val callable = if (functions != null) {
        val httpsCallableFn = js("require('firebase/functions').httpsCallable")
        (httpsCallableFn as (dynamic, String) -> dynamic)(functions, "uploadChatAdminFile")
    } else null
    if (callable == null) {
        callback("Cloud Functions недоступны", null)
        return
    }
    val payload = kotlin.js.json(
        "roomId" to roomId,
        "fileName" to fileName,
        "contentType" to contentType,
        "base64" to base64,
    )
    (callable as (dynamic) -> dynamic)(payload).then { res: dynamic ->
        // res — HttpsCallableResult из JS; не вызывать .asDynamic() у dynamic — иначе "asDynamic is not a function"
        val url = res.data?.url as? String
        if (url.isNullOrBlank()) callback("Пустой ответ сервера", null)
        else callback(null, url)
    }.catch { e: dynamic ->
        val code = (e?.code as? String) ?: ""
        val msg = (e?.message as? String) ?: "Ошибка загрузки"
        val details = e.details
        js("if(typeof console!=='undefined'&&console.error)console.error('StartDrive uploadChatAdminFile:', code, msg, details, e)")
        val detailSuffix = try {
            val d = details
            if (d == null) ""
            else {
                val s = js("JSON.stringify")(d) as? String ?: ""
                if (s.isNotBlank() && s != "undefined") " $s" else ""
            }
        } catch (_: Throwable) {
            ""
        }
        callback(friendlyMessage("$code $msg$detailSuffix".trim()), null)
    }
}

/** Статистика Storage по контактам чата (только админ). [callback]: ошибка или null, список или null. */
fun getAdminChatStorageStats(contactIds: List<String>, callback: (String?, List<ChatContactStorageStats>?) -> Unit) {
    fun friendly(msg: String): String = when {
        msg.contains("permission-denied", ignoreCase = true) -> "Нет доступа."
        msg.contains("unauthenticated", ignoreCase = true) -> "Войдите в аккаунт."
        else -> msg
    }
    val functions = getFunctions()
    val callable = if (functions != null) {
        val httpsCallableFn = js("require('firebase/functions').httpsCallable")
        (httpsCallableFn as (dynamic, String) -> dynamic)(functions, "getAdminChatStorageStats")
    } else null
    if (callable == null) {
        callback("Cloud Functions недоступны", null)
        return
    }
    val payload = js("{}").unsafeCast<dynamic>()
    val arr = js("[]").unsafeCast<dynamic>()
    contactIds.forEachIndexed { i, id -> arr[i] = id }
    payload.contactIds = arr
    (callable as (dynamic) -> dynamic)(payload).then { res: dynamic ->
        val data = res.data
        val statsDyn = data?.unsafeCast<dynamic>()?.stats
        val out = mutableListOf<ChatContactStorageStats>()
        if (statsDyn != null && statsDyn != undefined) {
            val len = js("(function(a){ return a && a.length ? a.length : 0; })")(statsDyn).unsafeCast<Number>().toInt()
            for (i in 0 until len) {
                val it = statsDyn[i].unsafeCast<dynamic>()
                val uid = it.userId as? String ?: continue
                out.add(
                    ChatContactStorageStats(
                        userId = uid,
                        voiceFileCount = (it.voiceFileCount as? Number)?.toInt() ?: 0,
                        voiceTotalBytes = (it.voiceTotalBytes as? Number)?.toLong() ?: 0L,
                        chatFileCount = (it.chatFileCount as? Number)?.toInt() ?: 0,
                        chatFileTotalBytes = (it.chatFileTotalBytes as? Number)?.toLong() ?: 0L,
                        avatarBytes = (it.avatarBytes as? Number)?.toLong() ?: 0L,
                    ),
                )
            }
        }
        callback(null, out)
    }.catch { e: dynamic ->
        val code = (e?.code as? String) ?: ""
        val msg = (e?.message as? String) ?: "Ошибка"
        js("if(typeof console!=='undefined'&&console.error)console.error('getAdminChatStorageStats:', code, msg, e)")
        callback(friendly("$code $msg".trim()), null)
    }
}

/** Статистика Storage по групповым чатам (только админ). */
fun getAdminGroupChatStorageStats(groupIds: List<String>, callback: (String?, List<ChatGroupStorageStats>?) -> Unit) {
    fun friendly(msg: String): String = when {
        msg.contains("permission-denied", ignoreCase = true) -> "Нет доступа."
        msg.contains("unauthenticated", ignoreCase = true) -> "Войдите в аккаунт."
        else -> msg
    }
    val functions = getFunctions()
    val callable = if (functions != null) {
        val httpsCallableFn = js("require('firebase/functions').httpsCallable")
        (httpsCallableFn as (dynamic, String) -> dynamic)(functions, "getAdminGroupChatStorageStats")
    } else null
    if (callable == null) {
        callback("Cloud Functions недоступны", null)
        return
    }
    val payload = js("{}").unsafeCast<dynamic>()
    val arr = js("[]").unsafeCast<dynamic>()
    groupIds.forEachIndexed { i, id -> arr[i] = id }
    payload.groupIds = arr
    (callable as (dynamic) -> dynamic)(payload).then { res: dynamic ->
        val data = res.data
        val statsDyn = data?.unsafeCast<dynamic>()?.stats
        val out = mutableListOf<ChatGroupStorageStats>()
        if (statsDyn != null && statsDyn != undefined) {
            val len = js("(function(a){ return a && a.length ? a.length : 0; })")(statsDyn).unsafeCast<Number>().toInt()
            for (i in 0 until len) {
                val it = statsDyn[i].unsafeCast<dynamic>()
                val gid = it.groupId as? String ?: continue
                out.add(
                    ChatGroupStorageStats(
                        groupId = gid,
                        voiceFileCount = (it.voiceFileCount as? Number)?.toInt() ?: 0,
                        voiceTotalBytes = (it.voiceTotalBytes as? Number)?.toLong() ?: 0L,
                        chatFileCount = (it.chatFileCount as? Number)?.toInt() ?: 0,
                        chatFileTotalBytes = (it.chatFileTotalBytes as? Number)?.toLong() ?: 0L,
                        avatarBytes = (it.avatarBytes as? Number)?.toLong() ?: 0L,
                    ),
                )
            }
        }
        callback(null, out)
    }.catch { e: dynamic ->
        val code = (e?.code as? String) ?: ""
        val msg = (e?.message as? String) ?: "Ошибка"
        js("if(typeof console!=='undefined'&&console.error)console.error('getAdminGroupChatStorageStats:', code, msg, e)")
        callback(friendly("$code $msg".trim()), null)
    }
}

/** Админ: объём бакета и разбивка по ролям. [callback]: ошибка или null, данные или null. */
fun getFirebaseStorageBucketUsage(callback: (String?, StorageBucketUsageBreakdown?) -> Unit) {
    fun friendly(msg: String): String = when {
        msg.contains("permission-denied", ignoreCase = true) -> "Нет доступа."
        msg.contains("unauthenticated", ignoreCase = true) -> "Войдите в аккаунт."
        else -> msg
    }
    val functions = getFunctions()
    val callable = if (functions != null) {
        val httpsCallableFn = js("require('firebase/functions').httpsCallable")
        (httpsCallableFn as (dynamic, String) -> dynamic)(functions, "getFirebaseStorageBucketUsage")
    } else null
    if (callable == null) {
        callback("Cloud Functions недоступны", null)
        return
    }
    val payload = js("{}").unsafeCast<dynamic>()
    (callable as (dynamic) -> dynamic)(payload).then { res: dynamic ->
        val d = res.data?.unsafeCast<dynamic>()
        fun n(field: String): Long {
            val v = js("(function(o,k){ return o ? o[k] : null; })")(d, field)
            return (v as? Number)?.toLong() ?: 0L
        }
        val breakdown = StorageBucketUsageBreakdown(
            totalBytes = n("totalBytes"),
            instructorBytes = n("instructorBytes"),
            cadetBytes = n("cadetBytes"),
            adminBytes = n("adminBytes"),
            groupBytes = n("groupBytes"),
            otherBytes = n("otherBytes"),
        )
        callback(null, breakdown)
    }.catch { e: dynamic ->
        val code = (e?.code as? String) ?: ""
        val msg = (e?.message as? String) ?: "Ошибка"
        js("if(typeof console!=='undefined'&&console.error)console.error('getFirebaseStorageBucketUsage:', code, msg, e)")
        callback(friendly("$code $msg".trim()), null)
    }
}

/** Админ: очистить файлы в личном чате с контактом (комната админ↔контакт). [callback]: ошибка или null. */
fun adminClearContactFiles(contactId: String, callback: (String?) -> Unit) {
    fun friendly(msg: String): String = when {
        msg.contains("permission-denied", ignoreCase = true) -> "Нет доступа."
        msg.contains("unauthenticated", ignoreCase = true) -> "Войдите в аккаунт."
        else -> msg
    }
    val functions = getFunctions()
    val callable = if (functions != null) {
        val httpsCallableFn = js("require('firebase/functions').httpsCallable")
        (httpsCallableFn as (dynamic, String) -> dynamic)(functions, "adminClearContactFiles")
    } else null
    if (callable == null) {
        callback("Cloud Functions недоступны")
        return
    }
    val payload = kotlin.js.json("contactId" to contactId)
    (callable as (dynamic) -> dynamic)(payload).then { _: dynamic ->
        callback(null)
    }.catch { e: dynamic ->
        val code = (e?.code as? String) ?: ""
        val msg = (e?.message as? String) ?: "Ошибка"
        js("if(typeof console!=='undefined'&&console.error)console.error('adminClearContactFiles:', code, msg, e)")
        callback(friendly("$code $msg".trim()))
    }
}

/** Админ: очистить голосовые в личном чате с контактом (RTDB + Storage). [callback]: ошибка или null. */
fun adminClearContactVoice(contactId: String, callback: (String?) -> Unit) {
    fun friendly(msg: String): String = when {
        msg.contains("permission-denied", ignoreCase = true) -> "Нет доступа."
        msg.contains("unauthenticated", ignoreCase = true) -> "Войдите в аккаунт."
        else -> msg
    }
    val functions = getFunctions()
    val callable = if (functions != null) {
        val httpsCallableFn = js("require('firebase/functions').httpsCallable")
        (httpsCallableFn as (dynamic, String) -> dynamic)(functions, "adminClearContactVoice")
    } else null
    if (callable == null) {
        callback("Cloud Functions недоступны")
        return
    }
    val payload = kotlin.js.json("contactId" to contactId)
    (callable as (dynamic) -> dynamic)(payload).then { _: dynamic ->
        callback(null)
    }.catch { e: dynamic ->
        val code = (e?.code as? String) ?: ""
        val msg = (e?.message as? String) ?: "Ошибка"
        js("if(typeof console!=='undefined'&&console.error)console.error('adminClearContactVoice:', code, msg, e)")
        callback(friendly("$code $msg".trim()))
    }
}

/** Админ: удалить аватар контакта (Storage + Firestore). [callback]: ошибка или null. */
fun adminClearContactAvatar(contactId: String, callback: (String?) -> Unit) {
    fun friendly(msg: String): String = when {
        msg.contains("permission-denied", ignoreCase = true) -> "Нет доступа."
        msg.contains("unauthenticated", ignoreCase = true) -> "Войдите в аккаунт."
        else -> msg
    }
    val functions = getFunctions()
    val callable = if (functions != null) {
        val httpsCallableFn = js("require('firebase/functions').httpsCallable")
        (httpsCallableFn as (dynamic, String) -> dynamic)(functions, "adminClearContactAvatar")
    } else null
    if (callable == null) {
        callback("Cloud Functions недоступны")
        return
    }
    val payload = kotlin.js.json("contactId" to contactId)
    (callable as (dynamic) -> dynamic)(payload).then { _: dynamic ->
        callback(null)
    }.catch { e: dynamic ->
        val code = (e?.code as? String) ?: ""
        val msg = (e?.message as? String) ?: "Ошибка"
        js("if(typeof console!=='undefined'&&console.error)console.error('adminClearContactAvatar:', code, msg, e)")
        callback(friendly("$code $msg".trim()))
    }
}

/** Админ: очистить голосовые в групповом чате (RTDB + Storage). */
fun adminClearGroupVoice(groupId: String, callback: (String?) -> Unit) {
    fun friendly(msg: String): String = when {
        msg.contains("permission-denied", ignoreCase = true) -> "Нет доступа."
        msg.contains("unauthenticated", ignoreCase = true) -> "Войдите в аккаунт."
        msg.contains("not-found", ignoreCase = true) -> "Группа не найдена."
        else -> msg
    }
    val functions = getFunctions()
    val callable = if (functions != null) {
        val httpsCallableFn = js("require('firebase/functions').httpsCallable")
        (httpsCallableFn as (dynamic, String) -> dynamic)(functions, "adminClearGroupVoice")
    } else null
    if (callable == null) {
        callback("Cloud Functions недоступны")
        return
    }
    val payload = kotlin.js.json("groupId" to groupId)
    (callable as (dynamic) -> dynamic)(payload).then { _: dynamic ->
        callback(null)
    }.catch { e: dynamic ->
        val code = (e?.code as? String) ?: ""
        val msg = (e?.message as? String) ?: "Ошибка"
        js("if(typeof console!=='undefined'&&console.error)console.error('adminClearGroupVoice:', code, msg, e)")
        callback(friendly("$code $msg".trim()))
    }
}

/** Админ: очистить файлы в групповом чате (RTDB + Storage). */
fun adminClearGroupFiles(groupId: String, callback: (String?) -> Unit) {
    fun friendly(msg: String): String = when {
        msg.contains("permission-denied", ignoreCase = true) -> "Нет доступа."
        msg.contains("unauthenticated", ignoreCase = true) -> "Войдите в аккаунт."
        msg.contains("not-found", ignoreCase = true) -> "Группа не найдена."
        else -> msg
    }
    val functions = getFunctions()
    val callable = if (functions != null) {
        val httpsCallableFn = js("require('firebase/functions').httpsCallable")
        (httpsCallableFn as (dynamic, String) -> dynamic)(functions, "adminClearGroupFiles")
    } else null
    if (callable == null) {
        callback("Cloud Functions недоступны")
        return
    }
    val payload = kotlin.js.json("groupId" to groupId)
    (callable as (dynamic) -> dynamic)(payload).then { _: dynamic ->
        callback(null)
    }.catch { e: dynamic ->
        val code = (e?.code as? String) ?: ""
        val msg = (e?.message as? String) ?: "Ошибка"
        js("if(typeof console!=='undefined'&&console.error)console.error('adminClearGroupFiles:', code, msg, e)")
        callback(friendly("$code $msg".trim()))
    }
}

/** Админ: удалить аватар группы (Storage + Firestore). */
fun adminClearGroupAvatar(groupId: String, callback: (String?) -> Unit) {
    fun friendly(msg: String): String = when {
        msg.contains("permission-denied", ignoreCase = true) -> "Нет доступа."
        msg.contains("unauthenticated", ignoreCase = true) -> "Войдите в аккаунт."
        msg.contains("not-found", ignoreCase = true) -> "Группа не найдена."
        else -> msg
    }
    val functions = getFunctions()
    val callable = if (functions != null) {
        val httpsCallableFn = js("require('firebase/functions').httpsCallable")
        (httpsCallableFn as (dynamic, String) -> dynamic)(functions, "adminClearGroupAvatar")
    } else null
    if (callable == null) {
        callback("Cloud Functions недоступны")
        return
    }
    val payload = kotlin.js.json("groupId" to groupId)
    (callable as (dynamic) -> dynamic)(payload).then { _: dynamic ->
        callback(null)
    }.catch { e: dynamic ->
        val code = (e?.code as? String) ?: ""
        val msg = (e?.message as? String) ?: "Ошибка"
        js("if(typeof console!=='undefined'&&console.error)console.error('adminClearGroupAvatar:', code, msg, e)")
        callback(friendly("$code $msg".trim()))
    }
}

/** Удаляет аватар: сначала файл в Firebase Storage, затем обнуляет chatAvatarUrl в Firestore.
 * 404 при DELETE в Storage — нормально (файла могло не быть). */
fun removeChatAvatar(uid: String, callback: (String?) -> Unit) {
    val firestore = getFirestore()
    val storage = getStorage()
    val path = chatAvatarStoragePath(uid)
    val deleteFile: kotlin.js.Promise<dynamic> = if (storage != null) {
        storage.ref(path).delete().catch { _: dynamic -> js("undefined") }
    } else {
        kotlin.js.Promise.resolve(js("undefined"))
    }
    deleteFile
        .then { _: dynamic ->
            firestore.collection(FirebasePaths.USERS).doc(uid).update(kotlin.js.json("chatAvatarUrl" to ""))
        }
        .then { _: dynamic ->
            callback(null)
        }
        .catch { e: Throwable ->
            val msg = (e.asDynamic().message as? String) ?: "Ошибка"
            callback(if (msg.contains("Failed to fetch", ignoreCase = true)) "Нет соединения с интернетом." else msg)
        }
}

/** Подписка на статус «в сети» пользователя. Возвращает функцию отписки. */
fun subscribePresence(userId: String, callback: (Boolean) -> Unit): () -> Unit {
    val db = getDatabase() ?: return { }
    val ref = db.ref("${FirebasePaths.PRESENCE}/$userId")
    val handler: (dynamic) -> Unit = { snap ->
        val v = snap?.`val`()
        val status = (v?.status as? String)
        callback(status == "online")
    }
    ref.on("value", handler)
    return {
        ref.off("value")
    }
}

/** Установить свой статус «в сети» / «не в сети» (для веб-клиента). */
fun setPresence(userId: String, online: Boolean) {
    val db = getDatabase() ?: return
    val ref = db.ref("${FirebasePaths.PRESENCE}/$userId")
    if (online) {
        ref.set(js("({ status: 'online', lastSeen: Date.now() })"))
        ref.onDisconnect().set(js("({ status: 'offline', lastSeen: { '.sv': 'timestamp' } })"))
    } else {
        ref.set(js("({ status: 'offline', lastSeen: Date.now() })"))
    }
}

/** Путь в Realtime Database для настроек приложения (чат: показывать аватары других пользователей). */
const val APP_CONFIG_CHAT_SHOW_OTHER_AVATARS = "app_config/chat_show_other_avatars"

/** Читает настройку «показывать аватары других пользователей в чате». По умолчанию true. */
fun getAppConfigChatShowOtherAvatars(callback: (Boolean) -> Unit) {
    val db = getDatabase() ?: run { callback(true); return }
    db.ref(APP_CONFIG_CHAT_SHOW_OTHER_AVATARS).once("value").then { snap: dynamic ->
        val v = snap?.`val`()
        callback(v != false && v != "false")
    }.catch { _: Throwable -> callback(true) }
}

fun getUsersForChat(currentUser: User, callback: (List<User>) -> Unit) {
    getUsers { all ->
        val list = when (currentUser.role) {
            "admin" -> all.filter { it.role != "admin" }
            "instructor" -> {
                val admin = all.firstOrNull { it.role == "admin" }
                val cadets = currentUser.assignedCadets.mapNotNull { id -> all.find { it.id == id } }
                listOfNotNull(admin) + cadets
            }
            "cadet" -> {
                val admin = all.firstOrNull { it.role == "admin" }
                val instructor = currentUser.assignedInstructorId?.let { id -> all.find { it.id == id } }
                listOfNotNull(admin, instructor).distinct()
            }
            else -> emptyList()
        }
        callback(list)
    }
}
