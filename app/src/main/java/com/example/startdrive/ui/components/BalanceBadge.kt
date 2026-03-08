package com.example.startdrive.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Синий цвет для бейджа баланса талонов. */
private val balanceBadgeBlue = Color(0xFF1976D2)
private val balanceBadgeBlueLight = Color(0xFF42A5F5)
private val balanceBadgeBlueDark = Color(0xFF0D47A1)

/**
 * Кружок с балансом талонов: синий цвет, эффект переливания и объёмный глянцевый вид.
 */
@Composable
fun BalanceBadge(
    value: String,
    modifier: Modifier = Modifier,
    scale: Float = 1f,
    size: Dp = 44.dp,
    valueStyle: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.bodyMedium.copy(
        fontWeight = FontWeight.Bold,
    ),
) {
    val display = value.toIntOrNull()?.let { if (it > 999) "999+" else value } ?: value
    Box(
        modifier = modifier
            .scale(scale)
            .shadow(6.dp, CircleShape, ambientColor = balanceBadgeBlueDark.copy(alpha = 0.5f))
            .shadow(4.dp, CircleShape, spotColor = balanceBadgeBlueDark.copy(alpha = 0.3f))
            .size(size)
            .clip(CircleShape)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        balanceBadgeBlueLight,
                        balanceBadgeBlue,
                        balanceBadgeBlueDark,
                    ),
                    startY = 0f,
                    endY = 200f,
                ),
            )
            .border(2.dp, Color.White.copy(alpha = 0.9f), CircleShape)
            .clip(CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Box(modifier = Modifier.matchParentSize()) {
            AnimatedGlossOverlay(
                modifier = Modifier.matchParentSize(),
                highlightAlpha = 0.35f,
                durationMillis = 2000,
            )
        }
        Text(
            text = display,
            style = valueStyle,
            color = Color.White,
            maxLines = 1,
        )
    }
}
