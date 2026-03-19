package firebase

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
    )
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
            callback(null)
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

/** Удаляет аватар: обнуляет chatAvatarUrl в Firestore и удаляет файл из Firebase Storage.
 * 404 при DELETE — нормально (файла могло не быть). Ошибку delete не пробрасываем. */
fun removeChatAvatar(uid: String, callback: (String?) -> Unit) {
    val firestore = getFirestore()
    firestore.collection(FirebasePaths.USERS).doc(uid).update(kotlin.js.json("chatAvatarUrl" to ""))
        .then {
            val storage = getStorage()
            if (storage != null) {
                val ref = storage.ref(chatAvatarStoragePath(uid))
                ref.delete().catch { _: dynamic -> js("undefined") }
            } else
                js("undefined")
        }
        .then { callback(null) }
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
