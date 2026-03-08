package com.example.startdrive.data.model

data class ChatMessage(
    val id: String = "",
    val senderId: String = "",
    val text: String = "",
    val timestamp: Long = 0L,
    val status: String = "sent", // sent, delivered, read
    val voiceUrl: String? = null,
    val voiceDurationSec: Int? = null,
    val replyToMessageId: String? = null,
    val replyToText: String? = null,
) {
    fun toMap(): Map<String, Any?> = buildMap {
        put("senderId", senderId)
        put("text", text)
        put("timestamp", timestamp)
        put("status", status)
        if (voiceUrl != null) put("voiceUrl", voiceUrl)
        if (voiceDurationSec != null) put("voiceDurationSec", voiceDurationSec)
        if (replyToMessageId != null) put("replyToMessageId", replyToMessageId)
        if (replyToText != null) put("replyToText", replyToText)
    }

    val isVoice: Boolean get() = !voiceUrl.isNullOrBlank() || (voiceDurationSec != null && voiceDurationSec > 0)

    companion object {
        fun fromMap(map: Map<String, Any?>, id: String = ""): ChatMessage {
            val ts = map["timestamp"]
            val timestamp = when (ts) {
                is Number -> ts.toLong()
                else -> (ts as? Long) ?: 0L
            }
            val dur = map["voiceDurationSec"]
            val durationSec = when (dur) {
                is Number -> dur.toInt()
                else -> null
            }
            return ChatMessage(
                id = (map["id"] as? String) ?: id,
                senderId = (map["senderId"] as? String).orEmpty(),
                text = (map["text"] as? String).orEmpty(),
                timestamp = timestamp,
                status = (map["status"] as? String) ?: "sent",
                voiceUrl = map["voiceUrl"] as? String,
                voiceDurationSec = durationSec,
                replyToMessageId = map["replyToMessageId"] as? String,
                replyToText = map["replyToText"] as? String,
            )
        }
    }
}
