package com.example.startdrive.ui.cadet

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.startdrive.data.model.DrivingSession
import java.util.Calendar
import java.util.Locale

private const val MAX_DRIVINGS_FOR_100 = 30
private val ROLE_THRESHOLDS = listOf(8, 16, 24, 30) // Новичок 0-7, Любитель 8-15, Профи 16-23, Эксперт 24-30
private val ROLE_NAMES = listOf("Новичок", "Любитель", "Профи", "Эксперт")
private val ROLE_COLORS = listOf(
    Color(0xFFFFC107), // сочный желтый
    Color(0xFF2E7D32), // сочный зеленый
    Color(0xFF1565C0), // сочный синий
    Color(0xFFC62828), // сочный красный
)

private fun getCurrentRole(completedCount: Int): Pair<Int, String> {
    for (i in ROLE_NAMES.indices) {
        if (completedCount < ROLE_THRESHOLDS[i]) return i to ROLE_NAMES[i]
    }
    return 3 to ROLE_NAMES[3]
}

@Composable
fun CadetProgressPieChart(
    completedDrivingsCount: Int,
    modifier: Modifier = Modifier,
) {
    val (currentRoleIndex, currentRoleName) = getCurrentRole(completedDrivingsCount)
    val capped = completedDrivingsCount.coerceAtMost(MAX_DRIVINGS_FOR_100)
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
                "Прогресс вождений:",
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
                    Canvas(modifier = Modifier.matchParentSize()) {
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val radius = size.minDimension / 2f - 8.dp.toPx()
                        val strokeWidth = 24.dp.toPx()
                        var startAngle = -90f
                        for (i in 0 until 4) {
                            val segmentStart = ROLE_THRESHOLDS.getOrNull(i - 1)?.toFloat() ?: 0f
                            val segmentEnd = ROLE_THRESHOLDS[i].toFloat()
                            val segmentCount = (segmentEnd - segmentStart).toInt()
                            val sweepAngle = 360f * segmentCount / MAX_DRIVINGS_FOR_100
                            val filled = (capped - segmentStart.toInt()).coerceIn(0, segmentCount)
                            val filledAngle = 360f * filled / MAX_DRIVINGS_FOR_100
                            drawArc(
                                color = ROLE_COLORS[i].copy(alpha = 0.4f),
                                startAngle = startAngle,
                                sweepAngle = sweepAngle,
                                useCenter = false,
                                topLeft = Offset(center.x - radius, center.y - radius),
                                size = Size(radius * 2, radius * 2),
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Butt),
                            )
                            if (filledAngle > 0f) {
                                drawArc(
                                    color = ROLE_COLORS[i],
                                    startAngle = startAngle,
                                    sweepAngle = filledAngle,
                                    useCenter = false,
                                    topLeft = Offset(center.x - radius, center.y - radius),
                                    size = Size(radius * 2, radius * 2),
                                    style = Stroke(width = strokeWidth, cap = StrokeCap.Butt),
                                )
                            }
                            startAngle += sweepAngle
                        }
                    }
                    // Белый круг в центре с количеством завершенных вождений
                    Box(
                        modifier = Modifier
                            .size(66.dp)
                            .background(Color.White, CircleShape)
                            .padding(6.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            "$completedDrivingsCount",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                        )
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Роль справа от значения (в скобках)
                    Text(
                        "(${currentRoleName})",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = ROLE_COLORS[currentRoleIndex],
                    )
                    Spacer(Modifier.height(4.dp))
                    ROLE_NAMES.forEachIndexed { i, name ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 2.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .background(ROLE_COLORS[i], RoundedCornerShape(2.dp)),
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                name,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (i == currentRoleIndex) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                fontWeight = if (i == currentRoleIndex) FontWeight.Bold else FontWeight.Normal,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CadetInstructorRatingsHistogram(
    sessions: List<DrivingSession>,
    modifier: Modifier = Modifier,
) {
    val completed = sessions.filter { it.status == "completed" && it.instructorRating in 1..5 }
    val counts = (1..5).map { r -> completed.count { it.instructorRating == r } }
    val maxCount = counts.maxOrNull()?.coerceAtLeast(1) ?: 1
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
                "Оценки инструктора:",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom,
            ) {
                counts.forEachIndexed { i, count ->
                    val barHeight = if (maxCount > 0) (count.toFloat() / maxCount * 100).dp else 0.dp
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.6f)
                                .height(barHeight.coerceAtLeast(4.dp))
                                .background(
                                    MaterialTheme.colorScheme.primary,
                                    RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp),
                                ),
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "${i + 1}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            "$count",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CadetWeeklyFrequencyHistogram(
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

                        // Линия между точками
                        for (i in 0 until pts.size - 1) {
                            drawLine(
                                color = lineColor,
                                start = pts[i],
                                end = pts[i + 1],
                                strokeWidth = 3.dp.toPx(),
                                cap = StrokeCap.Round,
                            )
                        }
                        // Точки
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
        }
    }
}

@Composable
fun CadetCancelledSessionsChart(
    sessions: List<DrivingSession>,
    modifier: Modifier = Modifier,
) {
    val cancelled = sessions.filter { it.status == "cancelledByCadet" }
    val cal = Calendar.getInstance(Locale.getDefault())
    val weekCounts = mutableMapOf<String, Int>()
    cancelled.forEach { s ->
        val t = s.cancelledAt?.toDate()?.time ?: s.startTime?.toDate()?.time ?: return@forEach
        cal.timeInMillis = t
        cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
        val weekKey = "${cal.get(Calendar.YEAR)}-W${cal.get(Calendar.WEEK_OF_YEAR)}"
        weekCounts[weekKey] = (weekCounts[weekKey] ?: 0) + 1
    }
    val sortedWeeks = weekCounts.keys.sorted().takeLast(2)
    val counts = sortedWeeks.map { weekCounts[it] ?: 0 }
    val maxCount = counts.maxOrNull()?.coerceAtLeast(1) ?: 1
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
                "Отменённые курсантом вождения:",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                "Всего отменено: ${cancelled.size}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (sortedWeeks.isEmpty()) {
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
                    counts.forEachIndexed { i, count ->
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
                                        Color(0xFFE53935),
                                        RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp),
                                    ),
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                sortedWeeks[i].takeLast(4),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                "$count",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFE53935),
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
            }
        }
    }
}
