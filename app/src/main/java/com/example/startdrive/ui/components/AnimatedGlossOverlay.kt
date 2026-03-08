package com.example.startdrive.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.sqrt

@Composable
fun AnimatedGlossOverlay(
    modifier: Modifier = Modifier,
    highlightAlpha: Float = 0.22f,
    durationMillis: Int = 2400,
) {
    val transition = rememberInfiniteTransition(label = "gloss")
    val p by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = durationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "glossProgress",
    )
    Box(
        modifier = modifier.drawWithCache {
            val travel = size.width + size.height
            val x = (p * 2f - 1f) * travel
            val band = size.width * 0.55f
            val start = Offset(x, 0f)
            val end = Offset(x + band, size.height)
            val brush = Brush.linearGradient(
                colors = listOf(
                    Color.Transparent,
                    Color.White.copy(alpha = highlightAlpha),
                    Color.Transparent,
                ),
                start = start,
                end = end,
            )
            onDrawBehind {
                drawRect(brush = brush, blendMode = BlendMode.Screen)
            }
        },
    )
}

/**
 * Анимация блика в левом верхнем углу (для карточек профиля).
 */
@Composable
fun TopLeftCornerShimmer(
    modifier: Modifier = Modifier,
    cornerSizeDp: Dp = 140.dp,
    highlightAlpha: Float = 0.35f,
    durationMillis: Int = 2200,
) {
    val density = LocalDensity.current
    val radiusPx = with(density) { cornerSizeDp.toPx() * sqrt(2f) }
    val transition = rememberInfiniteTransition(label = "cornerShimmer")
    val alpha by transition.animateFloat(
        initialValue = 0.15f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = durationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "cornerShimmerAlpha",
    )
    Box(
        modifier = modifier
            .size(cornerSizeDp)
            .drawWithCache {
                onDrawBehind {
                    val brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = highlightAlpha * alpha),
                            Color.White.copy(alpha = highlightAlpha * 0.3f * alpha),
                            Color.Transparent,
                        ),
                        center = Offset(0f, 0f),
                        radius = radiusPx,
                    )
                    drawRect(brush = brush, blendMode = BlendMode.Screen)
                }
            },
        contentAlignment = Alignment.TopStart,
    ) {}
}

