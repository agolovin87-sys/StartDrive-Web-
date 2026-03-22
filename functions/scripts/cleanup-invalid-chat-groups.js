/**
 * Удаляет документы chat_groups с невалидным memberIds (не массив / пусто).
 * Запуск: из папки functions, с GOOGLE_APPLICATION_CREDENTIALS на JSON ключа сервисного аккаунта.
 *
 *   node scripts/cleanup-invalid-chat-groups.js
 */

const admin = require("firebase-admin");

if (!admin.apps.length) {
  admin.initializeApp();
}

const db = admin.firestore();
const COL = "chat_groups";

function isValidMemberIds(v) {
  if (v == null) return false;
  if (!Array.isArray(v)) return false;
  if (v.length === 0) return false;
  return v.every((x) => typeof x === "string" && x.length > 0);
}

async function main() {
  if (!process.env.GOOGLE_APPLICATION_CREDENTIALS) {
    console.error(
      "Укажите GOOGLE_APPLICATION_CREDENTIALS на JSON файл ключа сервисного аккаунта."
    );
    process.exit(1);
  }

  const snap = await db.collection(COL).get();
  let removed = 0;
  let kept = 0;

  for (const doc of snap.docs) {
    const data = doc.data();
    const mid = data.memberIds;
    if (!isValidMemberIds(mid)) {
      console.log("Удаляю (невалидный memberIds):", doc.id, mid);
      await doc.ref.delete();
      removed++;
    } else {
      kept++;
    }
  }

  console.log("Готово. Оставлено:", kept, "удалено:", removed);
}

main().catch((e) => {
  console.error(e);
  process.exit(1);
});
