const { onValueCreated } = require("firebase-functions/v2/database");
const { onDocumentUpdated, onDocumentCreated } = require("firebase-functions/v2/firestore");
const { onSchedule } = require("firebase-functions/v2/scheduler");
const { onRequest, onCall, HttpsError } = require("firebase-functions/v2/https");
const { initializeApp } = require("firebase-admin/app");
const { getFirestore } = require("firebase-admin/firestore");
const { getMessaging } = require("firebase-admin/messaging");
const { getStorage } = require("firebase-admin/storage");

initializeApp();

const STORAGE_BUCKET = "startdrive-573fa.firebasestorage.app";
const AVATAR_PATH = (uid) => `users/${uid}/chat_avatar.png`;

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
      const file = bucket.file(AVATAR_PATH(uid));
      await file.save(buffer, { metadata: { contentType: "image/png" } });

      let url;
      try {
        const [signedUrl] = await file.getSignedUrl({
          action: "read",
          expires: Date.now() + 10 * 365 * 24 * 60 * 60 * 1000,
        });
        url = signedUrl;
      } catch (signErr) {
        // getSignedUrl часто падает без роли "Service Account Token Creator" в IAM
        const msg = (signErr && signErr.message) || "";
        if (signErr.code === 403 || /permission|sign|credentials/i.test(msg)) {
          await file.makePublic();
          url = `https://firebasestorage.googleapis.com/v0/b/${bucket.name}/o/${encodeURIComponent(file.name)}?alt=media`;
        } else {
          throw signErr;
        }
      }

      const firestore = getFirestore();
      await firestore.collection("users").doc(uid).update({ chatAvatarUrl: url });
      return { url };
    } catch (err) {
      console.error("uploadChatAvatar:", err);
      throw new HttpsError("internal", (err && err.message) || "Ошибка загрузки");
    }
  }
);
