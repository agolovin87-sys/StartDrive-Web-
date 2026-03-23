package com.example.startdrive.ui.instructor

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.startdrive.data.model.BalanceHistory
import com.example.startdrive.data.model.DrivingSession
import java.util.Calendar
import java.util.Locale

private val WEEKDAY_NAMES_SHORT = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")
private val RATING_COLORS = listOf(
    Color(0xFFE53935), // 1 - красный
    Color(0xFFFF9800), // 2 - оранжевый
    Color(0xFFFFEB3B), // 3 - жёлтый
    Color(0xFF8BC34A), // 4 - светло-зелёный
    Color(0xFF4CAF50), // 5 - зелёный
)

@Composable
fun InstructorTotalEarnedCard(
    balanceHistory: List<BalanceHistory>,
    modifier: Modifier = Modifier,
) {
    val totalEarned = balanceHistory.filter { it.type == "credit" }.sumOf { it.amount }
    val now = System.currentTimeMillis()
    val monthAgo = now - 30L * 24 * 60 * 60 * 1000
    val earnedLastMonth = balanceHistory
        .filter { it.type == "credit" && (it.timestamp?.toDate()?.time ?: 0L) >= monthAgo }
        .sumOf { it.amount }
    var tokensStr by remember(totalEarned) { mutableStateOf(totalEarned.toString()) }
    var rubPerTokenStr by remember { mutableStateOf("") }
    val tokens = tokensStr.toIntOrNull() ?: 0
    val rubPerToken = rubPerTokenStr.replace(",", ".").toDoubleOrNull() ?: 0.0
    val totalRub = tokens * rubPerToken

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.AccountBalance,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(
                            "Всего заработано талонов",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            "$totalEarned",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
                if (earnedLastMonth != 0) {
                    Text(
                        if (earnedLastMonth > 0) "+$earnedLastMonth за 30 дн." else "$earnedLastMonth за 30 дн.",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (earnedLastMonth > 0) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error,
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFB3D4FC))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                    value = tokensStr,
                    onValueChange = { tokensStr = it.filter { c -> c.isDigit() } },
                    label = { Text("талоны", style = MaterialTheme.typography.labelSmall) },
                    modifier = Modifier.widthIn(min = 56.dp, max = 80.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    textStyle = MaterialTheme.typography.bodySmall,
                )
                OutlinedTextField(
                    value = rubPerTokenStr,
                    onValueChange = { rubPerTokenStr = it.filter { c -> c.isDigit() || c == '.' || c == ',' } },
                    label = { Text("руб.", style = MaterialTheme.typography.labelSmall) },
                    modifier = Modifier.widthIn(min = 56.dp, max = 80.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    textStyle = MaterialTheme.typography.bodySmall,
                )
                Text(
                    text = "%.2f руб.".format(totalRub),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

@Composable
fun InstructorCompletedDrivingsCard(
    completedCount: Int,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.Default.DirectionsCar,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.width(16.dp))
            Column {
                Text(
                    "Всего завершённых вождений",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    "$completedCount",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

@Composable
fun InstructorCadetRatingsPieChart(
    sessions: List<DrivingSession>,
    modifier: Modifier = Modifier,
) {
    val rated = sessions.filter { it.status == "completed" && it.cadetRating in 1..5 }
    val counts = (1..5).map { r -> rated.count { it.cadetRating == r } }
    val total = counts.sum()
    val average = if (total > 0) "%.1f".format(Locale.US, rated.sumOf { it.cadetRating }.toDouble() / total) else "—"
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        val lineColor = MaterialTheme.colorScheme.secondary
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                "Ваш рейтинг:",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier.size(140.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    if (total > 0) {
                        Canvas(modifier = Modifier.matchParentSize()) {
                            val center = Offset(size.width / 2f, size.height / 2f)
                            val radius = size.minDimension / 2f - 8.dp.toPx()
                            var startAngle = -90f
                            counts.forEachIndexed { i, count ->
                                if (count > 0) {
                                    val sweepAngle = 360f * count / total
                                    drawArc(
                                        color = RATING_COLORS[i],
                                        startAngle = startAngle,
                                        sweepAngle = sweepAngle,
                                        useCenter = true,
                                        topLeft = Offset(center.x - radius, center.y - radius),
                                        size = Size(radius * 2, radius * 2),
                                    )
                                    startAngle += sweepAngle
                                }
                            }
                        }
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(88.dp),
                        ) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                modifier = Modifier.size(88.dp),
                                tint = Color.Black,
                            )
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                modifier = Modifier.size(84.dp),
                                tint = Color(0xFFFFEB3B),
                            )
                            Text(
                                average,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    } else {
                        Text(
                            "Нет оценок",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    (1..5).reversed().forEach { r ->
                        val i = r - 1
                        val count = counts[i]
                        val pct = if (total > 0) (100f * count / total).toInt() else 0
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 2.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .background(RATING_COLORS[i], RoundedCornerShape(2.dp)),
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "$r ★: $pct%",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InstructorWeeklyWorkloadChart(
    sessions: List<DrivingSession>,
    modifier: Modifier = Modifier,
) {
    val completed = sessions.filter { it.status == "completed" }
    val cal = Calendar.getInstance(Locale.getDefault())
    val weekCounts = mutableMapOf<String, Int>()
    completed.forEach { s ->
        val t = s.completedAt?.toDate()?.time ?: s.startTime?.toDate()?.time ?: return@forEach
        cal.timeInMillis = t
        cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
        val weekKey = "${cal.get(Calendar.YEAR)}-W${cal.get(Calendar.WEEK_OF_YEAR)}"
        weekCounts[weekKey] = (weekCounts[weekKey] ?: 0) + 1
    }
    val sortedWeeks = weekCounts.keys.sorted().takeLast(2)
    val counts = sortedWeeks.map { weekCounts[it] ?: 0 }
    val maxCount = counts.maxOrNull()?.coerceAtLeast(1) ?: 1
    val lineColor = MaterialTheme.colorScheme.secondary
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                "Частота вождений в неделю:",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (sortedWeeks.isEmpty()) {
                Text(
                    "Нет данных",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                Column {
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(90.dp),
                    ) {
                        val leftPad = 14.dp.toPx()
                        val rightPad = 8.dp.toPx()
                        val topPad = 10.dp.toPx()
                        val bottomPad = 24.dp.toPx()
                        val w = size.width
                        val h = size.height

                        val plotW = (w - leftPad - rightPad).coerceAtLeast(0f)
                        val plotH = (h - topPad - bottomPad).coerceAtLeast(0f)

                        val denom = if (counts.size <= 1) 1 else (counts.size - 1)
                        val pts = counts.mapIndexed { i, count ->
                            val x = leftPad + plotW * (i.toFloat() / denom.toFloat())
                            val yFrac = if (maxCount > 0) count.toFloat() / maxCount.toFloat() else 0f
                            val y = topPad + plotH * (1f - yFrac)
                            Offset(x, y)
                        }

                        for (i in 0 until pts.size - 1) {
                            drawLine(
                                color = lineColor,
                                start = pts[i],
                                end = pts[i + 1],
                                strokeWidth = 3.dp.toPx(),
                            )
                        }
                        pts.forEach { p ->
                            drawCircle(
                                color = lineColor,
                                radius = 3.dp.toPx(),
                                center = p,
                            )
                        }
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        counts.forEachIndexed { i, count ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.weight(1f),
                            ) {
                                Text(
                                    sortedWeeks[i].takeLast(4),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Text(
                                    "$count",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.secondary,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                    }
                }
            }
            Text(
                "Всего завершено: ${completed.size}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
fun InstructorCancelledByCadetsChart(
    sessions: List<DrivingSession>,
    modifier: Modifier = Modifier,
) {
    val cancelled = sessions.filter { it.status == "cancelledByCadet" }
    val cal = Calendar.getInstance(Locale.getDefault())
    val dayCounts = IntArray(7)
    cancelled.forEach { s ->
        val t = s.cancelledAt?.toDate()?.time ?: s.startTime?.toDate()?.time ?: return@forEach
        cal.timeInMillis = t
        val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        val index = when (dayOfWeek) {
            Calendar.SUNDAY -> 6
            else -> dayOfWeek - Calendar.MONDAY
        }.coerceIn(0, 6)
        dayCounts[index]++
    }
    val maxCount = dayCounts.maxOrNull()?.coerceAtLeast(1) ?: 1
    val cancelledRed = Color(0xFFE53935)
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                "График отмененных вождений:",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                "Всего отменено: ${cancelled.size}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (cancelled.isEmpty()) {
                Text(
                    "Нет отменённых вождений",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Bottom,
                ) {
                    dayCounts.forEachIndexed { i, count ->
                        val barHeight = if (maxCount > 0) (count.toFloat() / maxCount * 80).dp else 0.dp
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f),
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.6f)
                                    .height(barHeight.coerceAtLeast(4.dp))
                                    .background(
                                        cancelledRed,
                                        RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp),
                                    ),
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                WEEKDAY_NAMES_SHORT[i],
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                "$count",
                                style = MaterialTheme.typography.labelSmall,
                                color = cancelledRed,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
            }
        }
    }
}
