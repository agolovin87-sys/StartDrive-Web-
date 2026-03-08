package com.example.startdrive.ui.theme

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

/** Reference width (dp) for scale = 1f. */
private const val REFERENCE_WIDTH_DP = 360f

/** Scale factor limits so UI doesn't get too small or too large. */
private const val MIN_SCALE = 0.82f
private const val MAX_SCALE = 1.15f

/**
 * Screen-based dimensions so layout, buttons and text don't distort on different phone widths.
 * Use [LocalScreenDimensions.current] inside [StartDriveTheme].
 */
data class ScreenDimensions(
    val screenWidthDp: Int,
    val scale: Float,
    val horizontalPadding: Dp,
    val verticalPadding: Dp,
    val smallPadding: Dp,
    val mediumPadding: Dp,
    val navIconSize: Dp,
    val navBadgeSize: Dp,
    val listIconSize: Dp,
    val titleFontScale: Float,
) {
    /** Scaled dp from base value (for one-off use). */
    fun scaledDp(base: Float): Dp = (base * scale).dp
}

val LocalScreenDimensions = compositionLocalOf<ScreenDimensions> {
    error("LocalScreenDimensions not provided. Use inside StartDriveTheme.")
}

@Composable
fun rememberScreenDimensions(): ScreenDimensions {
    val config = LocalConfiguration.current
    val widthDp = config.screenWidthDp
    return remember(widthDp) {
        val scale = (widthDp / REFERENCE_WIDTH_DP).coerceIn(MIN_SCALE, MAX_SCALE)
        val horizontalPadding = (24f * scale).dp
        val verticalPadding = (16f * scale).dp
        val smallPadding = (8f * scale).dp
        val mediumPadding = (12f * scale).dp
        val navIconSize = (62f * scale).coerceIn(48f, 68f).dp
        val navBadgeSize = (18f * scale).coerceIn(14f, 22f).dp
        val listIconSize = (32f * scale).coerceIn(28f, 40f).dp
        val titleFontScale = (widthDp / REFERENCE_WIDTH_DP).coerceIn(0.9f, 1.1f)
        ScreenDimensions(
            screenWidthDp = widthDp,
            scale = scale,
            horizontalPadding = horizontalPadding,
            verticalPadding = verticalPadding,
            smallPadding = smallPadding,
            mediumPadding = mediumPadding,
            navIconSize = navIconSize,
            navBadgeSize = navBadgeSize,
            listIconSize = listIconSize,
            titleFontScale = titleFontScale,
        )
    }
}
