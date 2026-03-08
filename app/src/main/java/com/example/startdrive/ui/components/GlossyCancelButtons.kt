package com.example.startdrive.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Чуть более светлый красный для кнопок "Отменить/Удалить". */
val CancelRedLight = Color(0xFFFF2D2D)

fun Modifier.glossySheen(
    glossAlpha: Float = 0.32f,
): Modifier = drawWithCache {
    val top = Brush.verticalGradient(
        colors = listOf(Color.White.copy(alpha = glossAlpha), Color.Transparent),
        startY = 0f,
        endY = size.height * 0.65f,
    )
    val diag = Brush.linearGradient(
        colors = listOf(Color.White.copy(alpha = glossAlpha * 0.55f), Color.Transparent),
        start = Offset(0f, 0f),
        end = Offset(size.width * 0.75f, size.height * 0.75f),
    )
    onDrawWithContent {
        drawContent()
        drawRect(top)
        drawRect(diag)
    }
}

@Composable
fun GlossyCancelIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    disabledAppearance: Boolean = false,
    size: Dp = 44.dp,
    icon: ImageVector,
    contentDescription: String = "Отменить",
    iconSize: Dp = 22.dp,
) {
    val colorScheme = MaterialTheme.colorScheme
    val useGray = disabledAppearance || !enabled
    val containerColor = if (useGray) colorScheme.surfaceVariant else CancelRedLight
    val iconTint = if (useGray) colorScheme.onSurfaceVariant else Color.White
    Box(
        modifier = modifier
            .size(size)
            .shadow(
                elevation = 6.dp,
                shape = CircleShape,
                spotColor = if (useGray) Color.Black.copy(alpha = 0.12f) else CancelRedLight.copy(alpha = 0.35f),
                ambientColor = Color.Black.copy(alpha = 0.12f),
            )
            .clip(CircleShape)
            .background(containerColor)
            .glossySheen()
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = iconTint,
            modifier = Modifier.size(iconSize),
        )
    }
}

@Composable
fun GlossyButton(
    onClick: () -> Unit,
    text: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    trailingIcon: ImageVector? = null,
    isOutlined: Boolean = false,
    enabled: Boolean = true,
    usePrimaryBlue: Boolean = false,
    useRedCancel: Boolean = false,
) {
    val shape = remember { RoundedCornerShape(12.dp) }
    val colorScheme = MaterialTheme.colorScheme
    val btnModifier = modifier
        .defaultMinSize(minWidth = 88.dp)
        .shadow(
            6.dp,
            shape,
            spotColor = (if (useRedCancel) CancelRedLight else colorScheme.primary).copy(alpha = 0.25f),
            ambientColor = Color.Black.copy(alpha = 0.10f),
        )
        .clip(shape)
        .glossySheen()
    val (containerColor, contentColor) = when {
        useRedCancel -> CancelRedLight to Color.White
        usePrimaryBlue -> colorScheme.primary to Color.White
        else -> colorScheme.primaryContainer to colorScheme.onPrimaryContainer
    }
    if (isOutlined && !usePrimaryBlue && !useRedCancel) {
        OutlinedButton(
            onClick = onClick,
            modifier = modifier
                .defaultMinSize(minWidth = 100.dp)
                .shadow(
                    4.dp,
                    shape,
                    spotColor = colorScheme.outline.copy(alpha = 0.25f),
                    ambientColor = Color.Black.copy(alpha = 0.08f),
                )
                .clip(shape)
                .glossySheen(glossAlpha = 0.22f),
            shape = shape,
            contentPadding = ButtonDefaults.ContentPadding,
        ) {
            Icon(icon, contentDescription = null, Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                text,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis,
            )
        }
    } else {
        Button(
            onClick = onClick,
            modifier = btnModifier,
            shape = shape,
            colors = ButtonDefaults.buttonColors(
                containerColor = containerColor,
                contentColor = contentColor,
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp, pressedElevation = 8.dp),
            contentPadding = ButtonDefaults.ContentPadding,
            enabled = enabled,
        ) {
            Icon(icon, contentDescription = null, Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                text,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis,
            )
            trailingIcon?.let {
                Spacer(Modifier.width(4.dp))
                Icon(it, contentDescription = null, Modifier.size(18.dp))
            }
        }
    }
}

@Composable
fun GlossyCancelButton(
    onClick: () -> Unit,
    text: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    disabledAppearance: Boolean = false,
) {
    val shape = remember { RoundedCornerShape(10.dp) }
    val colorScheme = MaterialTheme.colorScheme
    val useGray = disabledAppearance
    Button(
        onClick = onClick,
        modifier = modifier
            .defaultMinSize(minHeight = 40.dp)
            .shadow(
                elevation = 6.dp,
                shape = shape,
                spotColor = if (useGray) Color.Black.copy(alpha = 0.10f) else CancelRedLight.copy(alpha = 0.30f),
                ambientColor = Color.Black.copy(alpha = 0.10f),
            )
            .clip(shape)
            .glossySheen(),
        shape = shape,
        enabled = true,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (useGray) colorScheme.surfaceVariant else CancelRedLight,
            contentColor = if (useGray) colorScheme.onSurfaceVariant else Color.White,
            disabledContainerColor = colorScheme.surfaceVariant,
            disabledContentColor = colorScheme.onSurfaceVariant,
        ),
        contentPadding = ButtonDefaults.ContentPadding,
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp, pressedElevation = 0.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                text,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

