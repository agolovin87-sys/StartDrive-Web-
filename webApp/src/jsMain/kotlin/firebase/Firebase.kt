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

fun initFirebase() {
    if (authInstance != null) return
    val firebase = js("require('firebase/compat/app')")
    js("require('firebase/compat/auth')")
    js("require('firebase/compat/firestore')")
    js("require('firebase/compat/database')")
    js("require('firebase/compat/storage')")
    firebaseCompat = firebase
    val config = getFirebaseConfig()
    val app = firebase.initializeApp(config)
    authInstance = firebase.auth(app)
    firestoreInstance = firebase.firestore(app)
    databaseInstance = firebase.database(app)
    storageInstance = firebase.storage(app)
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
    val id = doc.id as String
    val fullName = (d?.fullName as? String) ?: ""
    val email = (d?.email as? String) ?: ""
    val phone = (d?.phone as? String) ?: ""
    val role = (d?.role as? String) ?: ""
    val balance = (d?.balance as? Number)?.toInt() ?: 0
    val isActive = d?.isActive as? Boolean ?: false
    val assignedInstructorId = d?.assignedInstructorId as? String
    val assignedCadetsRaw = d?.assignedCadets
    val assignedCadets = (assignedCadetsRaw as? Array<*>)?.mapNotNull { it?.toString() } ?: emptyList<String>()
    val chatAvatarUrl = d?.chatAvatarUrl as? String
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
