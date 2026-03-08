package com.example.startdrive.ui.chat

import androidx.compose.ui.graphics.Color

/** Палитра цветов для кружков контактов в чате — один и тот же контакт всегда получает один цвет. */
private val CONTACT_AVATAR_PALETTE = listOf(
    Color(0xFF4DB6AC), // teal
    Color(0xFF7986CB), // indigo
    Color(0xFF81C784), // green
    Color(0xFFF06292), // pink
    Color(0xFF64B5F6), // blue
    Color(0xFFFFB74D), // orange
    Color(0xFFBA68C8), // purple
    Color(0xFF4DD0E1), // cyan
    Color(0xFFFF8A65), // deep orange
    Color(0xFF9575CD), // deep purple
    Color(0xFF4FC3F7), // light blue
    Color(0xFFA1887F), // brown
)

fun colorForContactId(contactId: String): Color {
    val index = contactId.hashCode().and(0x7FFFFFFF) % CONTACT_AVATAR_PALETTE.size
    return CONTACT_AVATAR_PALETTE[index]
}
