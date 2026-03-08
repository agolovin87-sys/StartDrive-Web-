package com.example.startdrive.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding

@Composable
fun CollapsibleCard(
    title: String,
    count: Int? = null,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    iconTint: Color? = null,
    containerColor: Color? = null,
    defaultExpanded: Boolean = true,
    titleColor: Color? = null,
    titleFontWeight: FontWeight? = null,
    content: @Composable () -> Unit,
) {
    var expanded by remember(defaultExpanded) { mutableStateOf(defaultExpanded) }
    val cardColors = if (containerColor != null) {
        CardDefaults.cardColors(containerColor = containerColor, contentColor = MaterialTheme.colorScheme.onSurface)
    } else {
        CardDefaults.cardColors(MaterialTheme.colorScheme.surface)
    }
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = cardColors,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint ?: MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                }
                Text(
                    text = title + if (count != null) " ($count)" else "",
                    style = MaterialTheme.typography.titleMedium,
                    color = titleColor ?: MaterialTheme.colorScheme.onSurface,
                    fontWeight = titleFontWeight ?: FontWeight.Normal,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (expanded) "Свернуть" else "Развернуть",
                    )
                }
            }
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(),
                exit = shrinkVertically(),
            ) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    content()
                }
            }
        }
    }
}
