const { onValueCreated } = require("firebase-functions/v2/database");
const { onDocumentUpdated, onDocumentCreated } = require("firebase-functions/v2/firestore");
const { onSchedule } = require("firebase-functions/v2/scheduler");
const { onRequest, onCall, HttpsError } = require("firebase-functions/v2/https");
const { initializeApp } = require("firebase-admin/app");
const { getFirestore } = require("firebase-admin/firestore");
const { getDatabase } = require("firebase-admin/database");
const { getMessaging } = require("firebase-admin/messaging");
const { getStorage } = require("firebase-admin/storage");
const crypto = require("crypto");

initializeApp();

const STORAGE_BUCKET = "startdrive-573fa.firebasestorage.app";
const AVATAR_PATH = (uid) => `users/${uid}/chat_avatar.png`;
const GROUP_AVATAR_PATH = (groupId) => `chats/group_avatars/${groupId}/avatar.png`;

function sleep(ms) {
  return new Promise((r) => setTimeout(r, ms));
}

/** Тот же формат, что у клиентского getDownloadURL (картинки в <img> стабильно грузятся). */
function buildFirebaseDownloadUrl(bucketName, objectPath, token) {
  return `https://firebasestorage.googleapis.com/v0/b/${bucketName}/o/${encodeURIComponent(objectPath)}?alt=media&token=${token}`;
}

/**
 * Загрузка PNG и постоянный download URL для Firebase Storage.
 * Ставим metadata.firebaseStorageDownloadTokens — без GCS signed URL / makePublic (лишние мутации и часто не открывается в img).
 */
async function savePngAndGetReadUrl(bucket, objectPath, buffer) {
  const file = bucket.file(objectPath);
  const maxAttempts = 5;
  for (let attempt = 0; attempt < maxAttempts; attempt++) {
    try {
      const token = crypto.randomUUID();
      await file.save(buffer, {
        metadata: {
          contentType: "image/png",
          metadata: {
            firebaseStorageDownloadTokens: token,
          },
        },
        resumable: false,
      });
      return buildFirebaseDownloadUrl(bucket.name, objectPath, token);
    } catch (err) {
      const msg = (err && err.message) || "";
      const is429 = /429|rate limit|exceeded the rate limit/i.test(msg);
      const retryable =
        attempt < maxAttempts - 1 &&
        (is429 ||
          /metadata|edited during|precondition|412|ECONNRESET|ETIMEDOUT/i.test(msg));
      if (!retryable) throw err;
      if (is429) {
        await sleep(2500 + attempt * 2500);
      } else {
        await sleep(400 * (attempt + 1));
      }
    }
  }
  throw new Error("savePngAndGetReadUrl: исчерпаны попытки");
}

/**
 * Произвольный файл + постоянный download URL (токен в metadata).
 */
async function saveBufferAndGetReadUrl(bucket, objectPath, buffer, contentType) {
  const ct =
    contentType && typeof contentType === "string" && contentType.length > 0
      ? contentType
      : "application/octet-stream";
  const file = bucket.file(objectPath);
  const maxAttempts = 5;
  for (let attempt = 0; attempt < maxAttempts; attempt++) {
    try {
      const token = crypto.randomUUID();
      await file.save(buffer, {
        metadata: {
          contentType: ct,
          metadata: {
            firebaseStorageDownloadTokens: token,
          },
        },
        resumable: false,
      });
      return buildFirebaseDownloadUrl(bucket.name, objectPath, token);
    } catch (err) {
      const msg = (err && err.message) || "";
      const is429 = /429|rate limit|exceeded the rate limit/i.test(msg);
      const retryable =
        attempt < maxAttempts - 1 &&
        (is429 ||
          /metadata|edited during|precondition|412|ECONNRESET|ETIMEDOUT/i.test(msg));
      if (!retryable) throw err;
      if (is429) {
        await sleep(2500 + attempt * 2500);
      } else {
        await sleep(400 * (attempt + 1));
      }
    }
  }
  throw new Error("saveBufferAndGetReadUrl: исчерпаны попытки");
}

function sanitizeChatFileName(name) {
  const base = String(name || "file").replace(/[/\\]/g, "_").replace(/\.\./g, "_");
  const cleaned = base.replace(/[^a-zA-Z0-9._\-()\s\u0400-\u04FF]/g, "_").slice(0, 120);
  return cleaned || "file";
}

/** Доступ к загрузке файла: личная комната uid1_uid2 (uid участвует) или группа group_* (uid в memberIds). */
async function assertUserCanUploadToChatRoom(roomId, uid) {
  if (!roomId || typeof roomId !== "string") {
    throw new HttpsError("invalid-argument", "Неверная комната");
  }
  if (roomId.startsWith("group_")) {
    const groupId = roomId.slice("group_".length);
    if (!groupId) {
      throw new HttpsError("invalid-argument", "Неверная группа");
    }
    const firestore = getFirestore();
    const g = await firestore.collection("chat_groups").doc(groupId).get();
    if (!g.exists) {
      throw new HttpsError("not-found", "Группа не найдена");
    }
    const members = g.data()?.memberIds;
    if (!Array.isArray(members) || !members.includes(uid)) {
      throw new HttpsError("permission-denied", "Нет доступа к этой группе");
    }
    return;
  }
  const parts = roomId.split("_");
  if (parts.length !== 2 || !parts.includes(uid)) {
    throw new HttpsError("permission-denied", "Нет доступа к этой переписке");
  }
}

const CHAT_ADMIN_FILE_MAX_BYTES = 8 * 1024 * 1024;

/** Бакет по умолчанию из конфига проекта (поддержка и *.appspot.com, и *.firebasestorage.app). */
function getDefaultStorageBucket() {
  const storage = getStorage();
  try {
    if (process.env.FIREBASE_CONFIG) {
      const cfg = JSON.parse(process.env.FIREBASE_CONFIG);
      if (cfg && cfg.storageBucket) {
        return storage.bucket(cfg.storageBucket);
      }
    }
  } catch (e) {
    console.error("getDefaultStorageBucket FIREBASE_CONFIG:", e);
  }
  try {
    return storage.bucket();
  } catch (e) {
    console.error("getDefaultStorage.bucket() no-arg:", e);
  }
  const projectId = process.env.GCLOUD_PROJECT || process.env.GCP_PROJECT;
  if (projectId) {
    return storage.bucket(`${projectId}.appspot.com`);
  }
  return storage.bucket(STORAGE_BUCKET);
}

const AUTO_START_WAIT_MS = 5 * 60 * 1000;

function getInstructorFcmToken(firestore, instructorId) {
  return firestore.collection("users").doc(instructorId).get()
    .then((doc) => doc.data()?.fcmToken || null);
}

function sendFcmToInstructor(firestore, instructorId, title, body, data = {}) {
  return getInstructorFcmToken(firestore, instructorId).then((fcmToken) => {
    if (!fcmToken) return Promise.resolve();
    const messaging = getMessaging();
    return messaging.send({
      token: fcmToken,
      notification: { title, body },
      data: { title, body, ...data },
      android: {
        priority: "high",
        notification: { sound: "default", channelId: data.channelId || "startdrive_general" },
      },
    });
  });
}

// ——— Чат: входящее сообщение (уже есть; получатель = инструктор, если пишет курсант)
exports.onNewChatMessage = onValueCreated(
  {
    ref: "/chats/{roomId}/messages/{messageId}",
    region: "europe-west1",
  },
  async (event) => {
    const snapshot = event.data;
    if (!snapshot) return;
    const roomId = event.params.roomId;
    const msg = typeof snapshot.val === "function" ? snapshot.val() : snapshot;
    const senderId = msg && msg.senderId;
    const text = (msg && msg.text) || "";
    if (!senderId || !roomId) return;

    const parts = roomId.split("_");
    const recipientId = parts[0] === senderId ? parts[1] : parts[0];
    if (!recipientId) return;

    const firestore = getFirestore();
    const recipientDoc = await firestore.collection("users").doc(recipientId).get();
    const fcmToken = recipientDoc.data()?.fcmToken;
    if (!fcmToken) return;

    const messaging = getMessaging();
    await messaging.send({
      token: fcmToken,
      notification: {
        title: "Новое сообщение в чате",
        body: text.length > 80 ? text.slice(0, 77) + "…" : text,
      },
      data: {
        title: "Новое сообщение в чате",
        body: text,
        type: "chat",
        channelId: "startdrive_chat",
        roomId,
      },
      android: {
        priority: "high",
        notification: { sound: "default", channelId: "startdrive_chat" },
      },
    });
  }
);

// ——— Вождение: обновление сессии — уведомления инструктору
exports.onDrivingSessionUpdated = onDocumentUpdated(
  { document: "driving_sessions/{sessionId}", region: "europe-west1" },
  async (event) => {
    const change = event.data;
    if (!change || !change.after) return;
    const before = change.before.data();
    const after = change.after.data();
    const instructorId = after?.instructorId;
    if (!instructorId) return;

    const firestore = getFirestore();
    const statusBefore = before?.status;
    const statusAfter = after?.status;
    const sessionBefore = before?.session || {};
    const sessionAfter = after?.session || {};
    const cadetConfirmedBefore = sessionBefore.cadetConfirmed === true;
    const cadetConfirmedAfter = sessionAfter.cadetConfirmed === true;

    // Подтверждение начала вождения курсантом
    if (!cadetConfirmedBefore && cadetConfirmedAfter) {
      await sendFcmToInstructor(firestore, instructorId,
        "Подтверждение вождения",
        "Курсант подтвердил начало вождения.",
        { type: "driving_confirm", channelId: "startdrive_driving" }
      );
      return;
    }

    // Отмена вождения курсантом
    if (statusAfter === "cancelledByCadet" && statusBefore !== "cancelledByCadet") {
      await sendFcmToInstructor(firestore, instructorId,
        "Отмена вождения",
        "Курсант отменил запланированное вождение.",
        { type: "driving_cancel", channelId: "startdrive_driving" }
      );
      return;
    }

    // Завершение вождения
    if (statusAfter === "completed" && statusBefore !== "completed") {
      await sendFcmToInstructor(firestore, instructorId,
        "Вождение завершено",
        "Сессия вождения успешно завершена.",
        { type: "driving_complete", channelId: "startdrive_driving" }
      );
    }
  }
);

// ——— По расписанию: автостарт вождения через 5 мин после назначенного времени
// (если инструктор не нажал «Начать» или нажал, но курсант не подтвердил)
exports.autoStartDrivingSessions = onSchedule(
  { schedule: "every 1 minutes", region: "europe-west1" },
  async () => {
    const firestore = getFirestore();
    const now = Date.now();
    const snapshot = await firestore
      .collection("driving_sessions")
      .where("status", "==", "scheduled")
      .where("instructorConfirmed", "==", true)
      .get();
    for (const doc of snapshot.docs) {
      const data = doc.data();
      const startTime = data.startTime;
      if (!startTime) continue;
      const startMillis = startTime.toMillis ? startTime.toMillis() : (startTime.seconds || 0) * 1000;
      const autoStartAt = startMillis + AUTO_START_WAIT_MS;
      if (now < autoStartAt) continue;
      const session = data.session || {};
      if (data.startRequestedByInstructor === true && session.cadetConfirmed === true) continue;
      await doc.ref.update({
        status: "inProgress",
        session: {
          startTime: now,
          pausedTime: 0,
          isActive: true,
          cadetConfirmed: false,
        },
      });
    }
  }
);

// ——— Баланс: новая запись в balance_history — уведомление владельцу (инструктору)
exports.onBalanceHistoryCreated = onDocumentCreated(
  { document: "users/{userId}/balance_history/{entryId}", region: "europe-west1" },
  async (event) => {
    const snap = event.data;
    if (!snap) return;
    const data = snap.data();
    const userId = event.params.userId;
    const type = data?.type;
    const amount = data?.amount != null ? Number(data.amount) : 0;
    if (!userId || !type) return;

    const firestore = getFirestore();
    const userDoc = await firestore.collection("users").doc(userId).get();
    const fcmToken = userDoc.data()?.fcmToken;
    if (!fcmToken) return;

    let title; let body;
    if (type === "credit") {
      title = "Поступление на баланс";
      body = `Зачисление: +${amount}`;
    } else if (type === "debit") {
      title = "Списание с баланса";
      body = `Списание: −${amount}`;
    } else return;

    const messaging = getMessaging();
    await messaging.send({
      token: fcmToken,
      notification: { title, body },
      data: {
        title,
        body,
        type: type === "credit" ? "balance_credit" : "balance_debit",
        channelId: "startdrive_balance",
      },
      android: {
        priority: "high",
        notification: { sound: "default", channelId: "startdrive_balance" },
      },
    });
  }
);

// ——— Один раз: установить CORS для Storage (чтобы загрузка аватара с веба работала)
const STORAGE_CORS = [
  {
    origin: ["*"],
    method: ["GET", "HEAD", "PUT", "POST", "OPTIONS", "DELETE"],
    responseHeader: ["Content-Type", "Content-Length", "x-goog-resumable", "x-goog-meta-*"],
    maxAgeSeconds: 3600,
  },
];

exports.setStorageCors = onRequest(
  { region: "europe-west1" },
  async (req, res) => {
    try {
      const bucket = getStorage().bucket();
      await bucket.setCorsConfiguration(STORAGE_CORS);
      res.status(200).json({ ok: true, message: "CORS для Storage установлен." });
    } catch (err) {
      console.error("setStorageCors:", err);
      res.status(500).json({ ok: false, error: (err && err.message) || "Ошибка" });
    }
  }
);

// Загрузка аватара через Callable — обходит CORS (браузер обращается к функции, не к Storage).
exports.uploadChatAvatar = onCall(
  {
    region: "europe-west1",
    cors: true, // разрешить вызов с localhost и любого origin (Callable с авторизацией)
  },
  async (request) => {
    if (!request.auth || !request.auth.uid) {
      throw new HttpsError("unauthenticated", "Войдите в аккаунт");
    }
    const uid = request.data?.uid;
    const dataUrl = request.data?.dataUrl;
    if (!uid || uid !== request.auth.uid || typeof dataUrl !== "string" || !dataUrl.startsWith("data:image")) {
      throw new HttpsError("invalid-argument", "Неверные данные");
    }
    try {
      const base64 = dataUrl.replace(/^data:image\/\w+;base64,/, "");
      const buffer = Buffer.from(base64, "base64");
      const bucket = getStorage().bucket(STORAGE_BUCKET);
      const url = await savePngAndGetReadUrl(bucket, AVATAR_PATH(uid), buffer);

      const firestore = getFirestore();
      await firestore.collection("users").doc(uid).update({ chatAvatarUrl: url });
      return { url };
    } catch (err) {
      console.error("uploadChatAvatar:", err);
      throw new HttpsError("internal", (err && err.message) || "Ошибка загрузки");
    }
  }
);

/** Аватар группы — через Admin SDK (клиентский PUT в Storage даёт 403, если правила не совпадают с бакетом). */
exports.uploadChatGroupAvatar = onCall(
  { region: "europe-west1", cors: true },
  async (request) => {
    if (!request.auth || !request.auth.uid) {
      throw new HttpsError("unauthenticated", "Войдите в аккаунт");
    }
    const groupId = request.data?.groupId;
    const dataUrl = request.data?.dataUrl;
    if (!groupId || typeof groupId !== "string" || typeof dataUrl !== "string" || !dataUrl.startsWith("data:image")) {
      throw new HttpsError("invalid-argument", "Неверные данные");
    }
    const firestore = getFirestore();
    const userSnap = await firestore.collection("users").doc(request.auth.uid).get();
    if (userSnap.data()?.role !== "admin") {
      throw new HttpsError("permission-denied", "Только администратор может менять аватар группы");
    }
    const groupSnap = await firestore.collection("chat_groups").doc(groupId).get();
    if (!groupSnap.exists) {
      throw new HttpsError("not-found", "Группа не найдена");
    }
    try {
      const base64 = dataUrl.replace(/^data:image\/\w+;base64,/, "");
      const buffer = Buffer.from(base64, "base64");
      const bucket = getStorage().bucket(STORAGE_BUCKET);
      const url = await savePngAndGetReadUrl(bucket, GROUP_AVATAR_PATH(groupId), buffer);

      await firestore.collection("chat_groups").doc(groupId).update({ chatAvatarUrl: url });
      return { url };
    } catch (err) {
      console.error("uploadChatGroupAvatar:", err);
      throw new HttpsError("internal", (err && err.message) || "Ошибка загрузки");
    }
  }
);

exports.removeChatGroupAvatar = onCall(
  { region: "europe-west1", cors: true },
  async (request) => {
    if (!request.auth || !request.auth.uid) {
      throw new HttpsError("unauthenticated", "Войдите в аккаунт");
    }
    const groupId = request.data?.groupId;
    if (!groupId || typeof groupId !== "string") {
      throw new HttpsError("invalid-argument", "Неверные данные");
    }
    const firestore = getFirestore();
    const userSnap = await firestore.collection("users").doc(request.auth.uid).get();
    if (userSnap.data()?.role !== "admin") {
      throw new HttpsError("permission-denied", "Только администратор");
    }
    try {
      const bucket = getStorage().bucket(STORAGE_BUCKET);
      const file = bucket.file(GROUP_AVATAR_PATH(groupId));
      await file.delete().catch(() => {});
      await firestore.collection("chat_groups").doc(groupId).update({ chatAvatarUrl: "" });
      return { ok: true };
    } catch (err) {
      console.error("removeChatGroupAvatar:", err);
      throw new HttpsError("internal", (err && err.message) || "Ошибка");
    }
  }
);

/** Вложение в чат — участники комнаты (личный чат или группа); загрузка через Admin SDK. */
exports.uploadChatAdminFile = onCall(
  {
    region: "europe-west1",
    cors: true,
    timeoutSeconds: 120,
    memory: "512MiB",
  },
  async (request) => {
    try {
      if (!request.auth || !request.auth.uid) {
        throw new HttpsError("unauthenticated", "Войдите в аккаунт");
      }
      const uid = request.auth.uid;
      const roomId = request.data?.roomId;
      const fileName = request.data?.fileName;
      const contentType = request.data?.contentType;
      const base64Raw = request.data?.base64;
      if (
        !roomId ||
        typeof roomId !== "string" ||
        typeof fileName !== "string" ||
        typeof base64Raw !== "string" ||
        base64Raw.length === 0
      ) {
        throw new HttpsError("invalid-argument", "Неверные данные");
      }
      const base64 = base64Raw.replace(/\s/g, "");
      await assertUserCanUploadToChatRoom(roomId, uid);
      let buffer;
      try {
        buffer = Buffer.from(base64, "base64");
      } catch (_e) {
        throw new HttpsError("invalid-argument", "Неверный формат файла");
      }
      if (buffer.length > CHAT_ADMIN_FILE_MAX_BYTES) {
        throw new HttpsError("invalid-argument", "Файл больше 8 МБ");
      }
      if (buffer.length === 0) {
        throw new HttpsError("invalid-argument", "Пустой файл");
      }
      const safeName = sanitizeChatFileName(fileName);
      const roomSafe = roomId.replace(/[/\\]/g, "_");
      const objectPath = `chats/files/${roomSafe}/${Date.now()}_${safeName}`;
      const ct =
        typeof contentType === "string" && contentType.length > 0 ? contentType : "application/octet-stream";
      const bucket = getDefaultStorageBucket();
      const url = await saveBufferAndGetReadUrl(bucket, objectPath, buffer, ct);
      return { url };
    } catch (err) {
      if (err instanceof HttpsError) {
        throw err;
      }
      const msg = (err && err.message) || String(err);
      const code = err && err.code;
      console.error("uploadChatAdminFile error:", msg, code, err && err.stack);
      throw new HttpsError("internal", msg || "Ошибка загрузки");
    }
  }
);

function chatRoomIdSorted(id1, id2) {
  const a = String(id1);
  const b = String(id2);
  return a < b ? `${a}_${b}` : `${b}_${a}`;
}

/**
 * Удалить голосовые в одной личной комнате: RTDB (voiceUrl) + объекты Storage chats/voice/{roomId}/.
 */
async function clearVoiceMessagesInRoom(roomId, db, bucket) {
  const messagesRef = db.ref(`chats/${roomId}/messages`);
  let removedMessages = 0;
  let removedFiles = 0;
  try {
    const snap = await messagesRef.once("value");
    const payload = {};
    snap.forEach((child) => {
      const v = child.val();
      if (v && v.voiceUrl) {
        payload[child.key] = null;
      }
    });
    const keys = Object.keys(payload);
    if (keys.length > 0) {
      await messagesRef.update(payload);
    }
    removedMessages = keys.length;
  } catch (e) {
    console.error("clearVoiceMessagesInRoom RTDB", roomId, e);
  }
  const voicePrefix = `chats/voice/${roomId}/`;
  try {
    const [files] = await bucket.getFiles({ prefix: voicePrefix });
    for (const f of files) {
      const name = f.name || "";
      if (!name.startsWith(voicePrefix)) continue;
      await f.delete().catch((err) => console.error("clearVoiceMessagesInRoom delete", name, err));
      removedFiles += 1;
    }
  } catch (e) {
    console.error("clearVoiceMessagesInRoom Storage", roomId, e);
  }
  return { removedMessages, removedFiles };
}

/**
 * Удалить вложения-файлы в одной комнате: RTDB (fileUrl) + объекты Storage chats/files/{roomSafe}/.
 */
async function clearFileMessagesInRoom(roomId, db, bucket) {
  const messagesRef = db.ref(`chats/${roomId}/messages`);
  let removedMessages = 0;
  let removedFiles = 0;
  try {
    const snap = await messagesRef.once("value");
    const payload = {};
    snap.forEach((child) => {
      const v = child.val();
      if (v && v.fileUrl) {
        payload[child.key] = null;
      }
    });
    const keys = Object.keys(payload);
    if (keys.length > 0) {
      await messagesRef.update(payload);
    }
    removedMessages = keys.length;
  } catch (e) {
    console.error("clearFileMessagesInRoom RTDB", roomId, e);
  }
  const roomSafe = roomId.replace(/[/\\]/g, "_");
  const filesPrefix = `chats/files/${roomSafe}/`;
  try {
    const [files] = await bucket.getFiles({ prefix: filesPrefix });
    for (const f of files) {
      const name = f.name || "";
      if (!name.startsWith(filesPrefix)) continue;
      await f.delete().catch((err) => console.error("clearFileMessagesInRoom delete", name, err));
      removedFiles += 1;
    }
  } catch (e) {
    console.error("clearFileMessagesInRoom Storage", roomId, e);
  }
  return { removedMessages, removedFiles };
}

/**
 * Статистика Storage по личным чатам админа с контактами: голосовые chats/voice/{roomId}/,
 * вложения chats/files/{roomId}/, аватар users/{id}/chat_avatar.png
 */
exports.getAdminChatStorageStats = onCall(
  {
    region: "europe-west1",
    cors: true,
    timeoutSeconds: 120,
    memory: "512MiB",
  },
  async (request) => {
    if (!request.auth || !request.auth.uid) {
      throw new HttpsError("unauthenticated", "Войдите в аккаунт");
    }
    const adminUid = request.auth.uid;
    const firestore = getFirestore();
    const userSnap = await firestore.collection("users").doc(adminUid).get();
    if (userSnap.data()?.role !== "admin") {
      throw new HttpsError("permission-denied", "Только администратор");
    }
    let contactIds = request.data?.contactIds;
    if (!Array.isArray(contactIds)) {
      throw new HttpsError("invalid-argument", "Неверные данные");
    }
    contactIds = contactIds.map((id) => String(id).trim()).filter(Boolean);
    if (contactIds.length === 0) {
      return { stats: [] };
    }
    if (contactIds.length > 400) {
      throw new HttpsError("invalid-argument", "Слишком много контактов (макс. 400)");
    }
    const bucket = getDefaultStorageBucket();
    const stats = [];
    for (const contactId of contactIds) {
      const roomId = chatRoomIdSorted(adminUid, contactId);
      const voicePrefix = `chats/voice/${roomId}/`;
      let voiceFileCount = 0;
      let voiceTotalBytes = 0;
      try {
        const [files] = await bucket.getFiles({ prefix: voicePrefix });
        for (const f of files) {
          const name = f.name || "";
          if (!name.startsWith(voicePrefix)) continue;
          let size = 0;
          if (f.metadata && f.metadata.size != null) {
            size = parseInt(String(f.metadata.size), 10) || 0;
          } else {
            const [meta] = await f.getMetadata();
            size = parseInt(meta.size || "0", 10) || 0;
          }
          voiceTotalBytes += size;
          voiceFileCount += 1;
        }
      } catch (e) {
        console.error("getAdminChatStorageStats voice", roomId, e);
      }
      const roomSafe = roomId.replace(/[/\\]/g, "_");
      const chatFilesPrefix = `chats/files/${roomSafe}/`;
      let chatFileCount = 0;
      let chatFileTotalBytes = 0;
      try {
        const [chatFiles] = await bucket.getFiles({ prefix: chatFilesPrefix });
        for (const f of chatFiles) {
          const name = f.name || "";
          if (!name.startsWith(chatFilesPrefix)) continue;
          let size = 0;
          if (f.metadata && f.metadata.size != null) {
            size = parseInt(String(f.metadata.size), 10) || 0;
          } else {
            const [meta] = await f.getMetadata();
            size = parseInt(meta.size || "0", 10) || 0;
          }
          chatFileTotalBytes += size;
          chatFileCount += 1;
        }
      } catch (e) {
        console.error("getAdminChatStorageStats chat files", roomId, e);
      }
      const avatarPath = `users/${contactId}/chat_avatar.png`;
      let avatarBytes = 0;
      try {
        const af = bucket.file(avatarPath);
        const [exists] = await af.exists();
        if (exists) {
          const [meta] = await af.getMetadata();
          avatarBytes = parseInt(meta.size || "0", 10) || 0;
        }
      } catch (e) {
        console.error("getAdminChatStorageStats avatar", contactId, e);
      }
      stats.push({
        userId: contactId,
        voiceFileCount,
        voiceTotalBytes,
        chatFileCount,
        chatFileTotalBytes,
        avatarBytes,
      });
    }
    return { stats };
  }
);

/**
 * Статистика Storage по групповым чатам: голосовые chats/voice/group_{id}/, файлы chats/files/group_{id}/,
 * аватар chats/group_avatars/{groupId}/avatar.png
 */
exports.getAdminGroupChatStorageStats = onCall(
  {
    region: "europe-west1",
    cors: true,
    timeoutSeconds: 120,
    memory: "512MiB",
  },
  async (request) => {
    if (!request.auth || !request.auth.uid) {
      throw new HttpsError("unauthenticated", "Войдите в аккаунт");
    }
    const adminUid = request.auth.uid;
    const firestore = getFirestore();
    const userSnap = await firestore.collection("users").doc(adminUid).get();
    if (userSnap.data()?.role !== "admin") {
      throw new HttpsError("permission-denied", "Только администратор");
    }
    let groupIds = request.data?.groupIds;
    if (!Array.isArray(groupIds)) {
      throw new HttpsError("invalid-argument", "Неверные данные");
    }
    groupIds = groupIds.map((id) => String(id).trim()).filter(Boolean);
    if (groupIds.length === 0) {
      return { stats: [] };
    }
    if (groupIds.length > 200) {
      throw new HttpsError("invalid-argument", "Слишком много групп (макс. 200)");
    }
    const bucket = getDefaultStorageBucket();
    const stats = [];
    for (const groupId of groupIds) {
      const roomId = `group_${groupId}`;
      const voicePrefix = `chats/voice/${roomId}/`;
      let voiceFileCount = 0;
      let voiceTotalBytes = 0;
      try {
        const [files] = await bucket.getFiles({ prefix: voicePrefix });
        for (const f of files) {
          const name = f.name || "";
          if (!name.startsWith(voicePrefix)) continue;
          let size = 0;
          if (f.metadata && f.metadata.size != null) {
            size = parseInt(String(f.metadata.size), 10) || 0;
          } else {
            const [meta] = await f.getMetadata();
            size = parseInt(meta.size || "0", 10) || 0;
          }
          voiceTotalBytes += size;
          voiceFileCount += 1;
        }
      } catch (e) {
        console.error("getAdminGroupChatStorageStats voice", roomId, e);
      }
      const roomSafe = roomId.replace(/[/\\]/g, "_");
      const chatFilesPrefix = `chats/files/${roomSafe}/`;
      let chatFileCount = 0;
      let chatFileTotalBytes = 0;
      try {
        const [chatFiles] = await bucket.getFiles({ prefix: chatFilesPrefix });
        for (const f of chatFiles) {
          const name = f.name || "";
          if (!name.startsWith(chatFilesPrefix)) continue;
          let size = 0;
          if (f.metadata && f.metadata.size != null) {
            size = parseInt(String(f.metadata.size), 10) || 0;
          } else {
            const [meta] = await f.getMetadata();
            size = parseInt(meta.size || "0", 10) || 0;
          }
          chatFileTotalBytes += size;
          chatFileCount += 1;
        }
      } catch (e) {
        console.error("getAdminGroupChatStorageStats chat files", roomId, e);
      }
      const avatarPath = `chats/group_avatars/${groupId}/avatar.png`;
      let avatarBytes = 0;
      try {
        const af = bucket.file(avatarPath);
        const [exists] = await af.exists();
        if (exists) {
          const [meta] = await af.getMetadata();
          avatarBytes = parseInt(meta.size || "0", 10) || 0;
        }
      } catch (e) {
        console.error("getAdminGroupChatStorageStats avatar", groupId, e);
      }
      stats.push({
        groupId,
        voiceFileCount,
        voiceTotalBytes,
        chatFileCount,
        chatFileTotalBytes,
        avatarBytes,
      });
    }
    return { stats };
  }
);

/** Два UID личной комнаты `uid1_uid2` (без group_). */
function parseRoomPair(roomId) {
  if (!roomId || typeof roomId !== "string") return null;
  if (roomId.startsWith("group_")) return null;
  const idx = roomId.indexOf("_");
  if (idx <= 0 || idx >= roomId.length - 1) return null;
  const a = roomId.slice(0, idx);
  const b = roomId.slice(idx + 1);
  if (!a || !b) return null;
  return [a, b];
}

/**
 * Полный объём бакета + разбивка: инструктор / курсант / администратор (users) / групповой чат / прочее.
 * users/{uid}/ — по роли из Firestore; chats/voice и chats/files — пополам в личных комнатах;
 * roomId group_* и chats/group_avatars/ — в groupBytes; adminBytes — папка users/ у роли admin; прочее — в otherBytes.
 */
exports.getFirebaseStorageBucketUsage = onCall(
  {
    region: "europe-west1",
    cors: true,
    timeoutSeconds: 540,
    memory: "512MiB",
  },
  async (request) => {
    if (!request.auth || !request.auth.uid) {
      throw new HttpsError("unauthenticated", "Войдите в аккаунт");
    }
    const uid = request.auth.uid;
    const firestore = getFirestore();
    const userSnap = await firestore.collection("users").doc(uid).get();
    if (userSnap.data()?.role !== "admin") {
      throw new HttpsError("permission-denied", "Только администратор");
    }
    const roleByUid = {};
    const usersSnap = await firestore.collection("users").get();
    usersSnap.forEach((doc) => {
      const r = doc.data()?.role;
      roleByUid[doc.id] = typeof r === "string" && r.length > 0 ? r : "";
    });

    function normRole(r) {
      if (r === "instructor" || r === "cadet" || r === "admin") return r;
      return "other";
    }

    let instructorBytes = 0;
    let cadetBytes = 0;
    let groupBytes = 0;
    let adminBytes = 0;
    let otherBytes = 0;

    function addForRole(role, n) {
      const x = Number(n) || 0;
      if (x <= 0) return;
      const nr = normRole(role);
      if (nr === "instructor") instructorBytes += x;
      else if (nr === "cadet") cadetBytes += x;
      else if (nr === "admin") adminBytes += x;
      else otherBytes += x;
    }

    const bucket = getDefaultStorageBucket();
    let totalBytes = 0;
    let query = { maxResults: 500, autoPaginate: false };
    while (true) {
      const [files, nextQuery] = await bucket.getFiles(query);
      for (const f of files) {
        let size = 0;
        if (f.metadata && f.metadata.size != null) {
          size = parseInt(String(f.metadata.size), 10) || 0;
        } else {
          try {
            const [meta] = await f.getMetadata();
            size = parseInt(meta.size || "0", 10) || 0;
          } catch (e) {
            console.error("getFirebaseStorageBucketUsage file meta", f.name, e);
          }
        }
        totalBytes += size;
        const name = f.name || "";

        if (name.startsWith("users/")) {
          const m = /^users\/([^/]+)\//.exec(name);
          if (m) {
            const u = m[1];
            const role = roleByUid[u] || "";
            addForRole(role, size);
          } else {
            otherBytes += size;
          }
          continue;
        }

        if (name.startsWith("chats/voice/") || name.startsWith("chats/files/")) {
          const prefix = name.startsWith("chats/voice/") ? "chats/voice/" : "chats/files/";
          const rest = name.slice(prefix.length);
          const slash = rest.indexOf("/");
          const roomId = slash < 0 ? rest : rest.slice(0, slash);
          if (roomId.startsWith("group_")) {
            groupBytes += size;
          } else {
            const pair = parseRoomPair(roomId);
            if (pair) {
              const h1 = Math.floor(size / 2);
              const h2 = size - h1;
              addForRole(roleByUid[pair[0]] || "", h1);
              addForRole(roleByUid[pair[1]] || "", h2);
            } else {
              otherBytes += size;
            }
          }
          continue;
        }

        if (name.startsWith("chats/group_avatars/")) {
          groupBytes += size;
          continue;
        }

        otherBytes += size;
      }
      if (!nextQuery) break;
      query = nextQuery;
    }

    return {
      totalBytes,
      instructorBytes,
      cadetBytes,
      groupBytes,
      adminBytes,
      otherBytes,
    };
  }
);

/**
 * Админ: удалить все голосовые у выбранного пользователя во всех личных чатах
 * (со всеми собеседниками из коллекции users), не только чат с админом.
 */
exports.adminClearContactVoice = onCall(
  {
    region: "europe-west1",
    cors: true,
    timeoutSeconds: 540,
    memory: "512MiB",
  },
  async (request) => {
    if (!request.auth || !request.auth.uid) {
      throw new HttpsError("unauthenticated", "Войдите в аккаунт");
    }
    const adminUid = request.auth.uid;
    const firestore = getFirestore();
    const adminSnap = await firestore.collection("users").doc(adminUid).get();
    if (adminSnap.data()?.role !== "admin") {
      throw new HttpsError("permission-denied", "Только администратор");
    }
    const contactId = request.data?.contactId;
    if (!contactId || typeof contactId !== "string") {
      throw new HttpsError("invalid-argument", "Неверный контакт");
    }
    const cid = String(contactId).trim();
    const usersSnap = await firestore.collection("users").get();
    const userIds = [];
    usersSnap.forEach((doc) => userIds.push(doc.id));
    if (userIds.length > 5000) {
      throw new HttpsError("resource-exhausted", "Слишком много пользователей для операции");
    }
    const db = getDatabase();
    const bucket = getDefaultStorageBucket();
    const others = userIds.filter((id) => id !== cid);
    /** Параллель небольшими пачками — быстрее и укладываемся в timeout. */
    const BATCH = 6;
    let totalMessages = 0;
    let totalFiles = 0;
    const roomsProcessed = others.length;
    for (let i = 0; i < others.length; i += BATCH) {
      const slice = others.slice(i, i + BATCH);
      const results = await Promise.all(
        slice.map((otherId) => clearVoiceMessagesInRoom(chatRoomIdSorted(cid, otherId), db, bucket))
      );
      for (const r of results) {
        totalMessages += r.removedMessages;
        totalFiles += r.removedFiles;
      }
    }
    return {
      ok: true,
      removedMessages: totalMessages,
      removedFiles: totalFiles,
      roomsProcessed,
    };
  }
);

/**
 * Админ: удалить аватар контакта из Storage и обнулить chatAvatarUrl в Firestore.
 */
exports.adminClearContactAvatar = onCall(
  {
    region: "europe-west1",
    cors: true,
    timeoutSeconds: 60,
    memory: "256MiB",
  },
  async (request) => {
    if (!request.auth || !request.auth.uid) {
      throw new HttpsError("unauthenticated", "Войдите в аккаунт");
    }
    const adminUid = request.auth.uid;
    const firestore = getFirestore();
    const userSnap = await firestore.collection("users").doc(adminUid).get();
    if (userSnap.data()?.role !== "admin") {
      throw new HttpsError("permission-denied", "Только администратор");
    }
    const contactId = request.data?.contactId;
    if (!contactId || typeof contactId !== "string") {
      throw new HttpsError("invalid-argument", "Неверный контакт");
    }
    const bucket = getDefaultStorageBucket();
    const avatarPath = `users/${contactId}/chat_avatar.png`;
    await bucket.file(avatarPath).delete().catch(() => {});
    await firestore.collection("users").doc(contactId).update({ chatAvatarUrl: "" });
    return { ok: true };
  }
);

/**
 * Админ: очистить вложения-файлы в личном чате с контактом (только комната админ↔контакт: RTDB + Storage chats/files/).
 */
exports.adminClearContactFiles = onCall(
  {
    region: "europe-west1",
    cors: true,
    timeoutSeconds: 300,
    memory: "512MiB",
  },
  async (request) => {
    if (!request.auth || !request.auth.uid) {
      throw new HttpsError("unauthenticated", "Войдите в аккаунт");
    }
    const adminUid = request.auth.uid;
    const firestore = getFirestore();
    const userSnap = await firestore.collection("users").doc(adminUid).get();
    if (userSnap.data()?.role !== "admin") {
      throw new HttpsError("permission-denied", "Только администратор");
    }
    const contactId = request.data?.contactId;
    if (!contactId || typeof contactId !== "string") {
      throw new HttpsError("invalid-argument", "Неверный контакт");
    }
    const cid = String(contactId).trim();
    const roomId = chatRoomIdSorted(adminUid, cid);
    const db = getDatabase();
    const bucket = getDefaultStorageBucket();
    const r = await clearFileMessagesInRoom(roomId, db, bucket);
    return { ok: true, ...r };
  }
);

/**
 * Админ: очистить голосовые в групповом чате (RTDB chats/group_{id}/messages + Storage chats/voice/group_{id}/).
 */
exports.adminClearGroupVoice = onCall(
  {
    region: "europe-west1",
    cors: true,
    timeoutSeconds: 300,
    memory: "512MiB",
  },
  async (request) => {
    if (!request.auth || !request.auth.uid) {
      throw new HttpsError("unauthenticated", "Войдите в аккаунт");
    }
    const adminUid = request.auth.uid;
    const firestore = getFirestore();
    const userSnap = await firestore.collection("users").doc(adminUid).get();
    if (userSnap.data()?.role !== "admin") {
      throw new HttpsError("permission-denied", "Только администратор");
    }
    const groupId = request.data?.groupId;
    if (!groupId || typeof groupId !== "string") {
      throw new HttpsError("invalid-argument", "Неверная группа");
    }
    const gid = String(groupId).trim();
    if (!gid) {
      throw new HttpsError("invalid-argument", "Неверная группа");
    }
    const gDoc = await firestore.collection("chat_groups").doc(gid).get();
    if (!gDoc.exists) {
      throw new HttpsError("not-found", "Группа не найдена");
    }
    const roomId = `group_${gid}`;
    const db = getDatabase();
    const bucket = getDefaultStorageBucket();
    const r = await clearVoiceMessagesInRoom(roomId, db, bucket);
    return { ok: true, ...r };
  }
);

/**
 * Админ: очистить файлы в групповом чате (RTDB + Storage chats/files/group_{id}/).
 */
exports.adminClearGroupFiles = onCall(
  {
    region: "europe-west1",
    cors: true,
    timeoutSeconds: 300,
    memory: "512MiB",
  },
  async (request) => {
    if (!request.auth || !request.auth.uid) {
      throw new HttpsError("unauthenticated", "Войдите в аккаунт");
    }
    const adminUid = request.auth.uid;
    const firestore = getFirestore();
    const userSnap = await firestore.collection("users").doc(adminUid).get();
    if (userSnap.data()?.role !== "admin") {
      throw new HttpsError("permission-denied", "Только администратор");
    }
    const groupId = request.data?.groupId;
    if (!groupId || typeof groupId !== "string") {
      throw new HttpsError("invalid-argument", "Неверная группа");
    }
    const gid = String(groupId).trim();
    if (!gid) {
      throw new HttpsError("invalid-argument", "Неверная группа");
    }
    const gDoc = await firestore.collection("chat_groups").doc(gid).get();
    if (!gDoc.exists) {
      throw new HttpsError("not-found", "Группа не найдена");
    }
    const roomId = `group_${gid}`;
    const db = getDatabase();
    const bucket = getDefaultStorageBucket();
    const r = await clearFileMessagesInRoom(roomId, db, bucket);
    return { ok: true, ...r };
  }
);

/**
 * Админ: удалить аватар группы из Storage и обнулить chatAvatarUrl в Firestore.
 */
exports.adminClearGroupAvatar = onCall(
  {
    region: "europe-west1",
    cors: true,
    timeoutSeconds: 60,
    memory: "256MiB",
  },
  async (request) => {
    if (!request.auth || !request.auth.uid) {
      throw new HttpsError("unauthenticated", "Войдите в аккаунт");
    }
    const adminUid = request.auth.uid;
    const firestore = getFirestore();
    const userSnap = await firestore.collection("users").doc(adminUid).get();
    if (userSnap.data()?.role !== "admin") {
      throw new HttpsError("permission-denied", "Только администратор");
    }
    const groupId = request.data?.groupId;
    if (!groupId || typeof groupId !== "string") {
      throw new HttpsError("invalid-argument", "Неверная группа");
    }
    const gid = String(groupId).trim();
    if (!gid) {
      throw new HttpsError("invalid-argument", "Неверная группа");
    }
    const gDoc = await firestore.collection("chat_groups").doc(gid).get();
    if (!gDoc.exists) {
      throw new HttpsError("not-found", "Группа не найдена");
    }
    const bucket = getDefaultStorageBucket();
    const avatarPath = `chats/group_avatars/${gid}/avatar.png`;
    await bucket.file(avatarPath).delete().catch(() => {});
    await firestore.collection("chat_groups").doc(gid).update({ chatAvatarUrl: "" });
    return { ok: true };
  }
);
