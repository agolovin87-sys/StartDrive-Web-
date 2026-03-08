package com.example.startdrive.ui.cadet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.startdrive.data.model.BalanceHistory
import com.example.startdrive.data.model.DrivingSession
import com.example.startdrive.data.repository.DrivingRepository
import com.example.startdrive.data.repository.UserRepository
import com.example.startdrive.ui.components.CollapsibleCard
import com.example.startdrive.ui.components.CollapsibleSection
import java.text.SimpleDateFormat
import java.util.Locale

private val dateTimeFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())

/** Формат: Фамилия И.О. */
private fun formatSurnameWithInitials(fullName: String): String {
    if (fullName.isBlank()) return fullName
    val parts = fullName.trim().split(Regex("\\s+"))
    return when {
        parts.size >= 2 -> parts[0] + " " + parts.drop(1).map { it.firstOrNull()?.uppercaseChar()?.let { "$it." } ?: "" }.joinToString("")
        parts.size == 1 -> parts[0]
        else -> fullName
    }
}

@Composable
fun CadetHistoryTab(cadetId: String) {
    val drivingRepo = remember { DrivingRepository() }
    val userRepo = remember { UserRepository() }
    val sessions by drivingRepo.drivingHistory(cadetId, false).collectAsState(initial = emptyList())
    val balanceHistory by drivingRepo.balanceHistory(cadetId).collectAsState(initial = emptyList())
    var performedByNameMap by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var instructorNamesMap by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var cadetName by remember { mutableStateOf("") }

    LaunchedEffect(balanceHistory) {
        val ids = balanceHistory.map { it.performedBy }.distinct().filter { it.isNotBlank() }
        performedByNameMap = ids.associateWith { id -> userRepo.getUser(id)?.fullName?.let(::formatSurnameWithInitials) ?: id }
    }
    LaunchedEffect(sessions) {
        cadetName = userRepo.getUser(cadetId)?.fullName?.takeIf { it.isNotBlank() }?.let(::formatSurnameWithInitials) ?: "—"
        val instructorIds = sessions.map { it.instructorId }.distinct()
        instructorNamesMap = instructorIds.associateWith { id -> userRepo.getUser(id)?.fullName?.takeIf { it.isNotBlank() }?.let(::formatSurnameWithInitials) ?: "—" }
    }

    LazyColumn(
        Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            CollapsibleCard(title = "Баланс", count = balanceHistory.size, icon = Icons.Default.AccountBalance, iconTint = MaterialTheme.colorScheme.primary) {
                val credits = balanceHistory.filter { it.type == "credit" }
                val debits = balanceHistory.filter { it.type == "debit" }
                val others = balanceHistory.filter { it.type !in listOf("credit", "debit") }
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    CollapsibleSection(
                        title = "Зачисления",
                        count = credits.size,
                        icon = Icons.Default.ArrowDownward,
                        iconTint = Color(0xFF2E7D32),
                        modifier = Modifier.padding(top = 4.dp),
                        defaultExpanded = false,
                    ) {
                        if (credits.isEmpty()) {
                            Text(
                                "Нет зачислений",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(start = 22.dp),
                            )
                        } else {
                            credits.forEach { h ->
                                BalanceEntryCard(
                                    entry = h,
                                    performedByName = performedByNameMap[h.performedBy] ?: h.performedBy.ifBlank { "—" },
                                )
                            }
                        }
                    }
                    CollapsibleSection(
                        title = "Списания",
                        count = debits.size,
                        icon = Icons.Default.ArrowUpward,
                        iconTint = MaterialTheme.colorScheme.error,
                        defaultExpanded = false,
                    ) {
                        if (debits.isEmpty()) {
                            Text(
                                "Нет списаний",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(start = 22.dp),
                            )
                        } else {
                            debits.forEach { h ->
                                BalanceEntryCard(
                                    entry = h,
                                    performedByName = performedByNameMap[h.performedBy] ?: h.performedBy.ifBlank { "—" },
                                )
                            }
                        }
                    }
                    if (others.isNotEmpty()) {
                        Text(
                            "Прочее (${others.size})",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(start = 8.dp)) {
                            others.forEach { h ->
                                BalanceEntryCard(
                                    entry = h,
                                    performedByName = performedByNameMap[h.performedBy] ?: h.performedBy.ifBlank { "—" },
                                )
                            }
                        }
                    }
                }
            }
        }
        item {
            val completedSessions = sessions.filter { it.status == "completed" }
            val cancelledSessions = sessions.filter { it.status in listOf("cancelledByInstructor", "cancelledByCadet") }
            CollapsibleCard(title = "Вождение", count = completedSessions.size + cancelledSessions.size, icon = Icons.Default.DirectionsCar, iconTint = MaterialTheme.colorScheme.primary) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    CollapsibleSection(
                        title = "Завершенное вождение",
                        count = completedSessions.size,
                        icon = Icons.Default.Check,
                        iconTint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 4.dp),
                        defaultExpanded = false,
                    ) {
                        if (completedSessions.isEmpty()) {
                            Text(
                                "Нет завершённых занятий",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(start = 22.dp),
                            )
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                completedSessions.forEach { s ->
                                    CompletedSessionCard(
                                        session = s,
                                        cadetName = cadetName,
                                        instructorName = instructorNamesMap[s.instructorId] ?: "—",
                                    )
                                }
                            }
                        }
                    }
                    CollapsibleSection(
                        title = "Отменённые",
                        count = cancelledSessions.size,
                        icon = Icons.Default.Close,
                        iconTint = MaterialTheme.colorScheme.error,
                        defaultExpanded = false,
                    ) {
                        if (cancelledSessions.isEmpty()) {
                            Text(
                                "Нет отменённых занятий",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(start = 22.dp),
                            )
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                cancelledSessions.forEach { s ->
                                    CancelledSessionCard(
                                        session = s,
                                        cadetName = cadetName,
                                        instructorName = instructorNamesMap[s.instructorId] ?: "—",
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private val completedStatusGreen = Color(0xFF2E7D32)

@Composable
private fun CompletedSessionCard(
    session: DrivingSession,
    cadetName: String,
    instructorName: String,
) {
    val drivingDateTime = session.startTime?.toDate()?.let { dateTimeFormat.format(it) } ?: "—"
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Icon(
                Icons.Default.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp),
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                    Text(
                        "Дата: ",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        drivingDateTime,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Start,
                    )
                }
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                    Text(
                        "Курсант: ",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        cadetName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Start,
                    )
                }
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                    Text(
                        "Инструктор: ",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        instructorName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Start,
                    )
                }
                if (session.instructorRating in 3..5) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                        Text(
                            "Оценка инструктора: ",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            session.instructorRating.toString(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Start,
                        )
                    }
                }
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                    Text(
                        "Статус: ",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        "Завершен",
                        style = MaterialTheme.typography.bodyMedium,
                        color = completedStatusGreen,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Start,
                    )
                }
            }
        }
    }
}

@Composable
private fun CancelledSessionCard(
    session: DrivingSession,
    cadetName: String,
    instructorName: String,
) {
    val cancelledDateTime = session.cancelledAt?.toDate()?.let { dateTimeFormat.format(it) } ?: "—"
    val plannedDateTime = session.startTime?.toDate()?.let { dateTimeFormat.format(it) } ?: "—"
    val statusText = when (session.status) {
        "cancelledByInstructor" -> "отменен инструктором"
        "cancelledByCadet" -> "отменен курсантом"
        else -> "отменен"
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(32.dp),
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                    Text(
                        "Дата отмены: ",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        cancelledDateTime,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Start,
                    )
                }
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                    Text(
                        "Запланировано на: ",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        plannedDateTime,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Start,
                    )
                }
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                    Text(
                        "Курсант: ",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        cadetName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Start,
                    )
                }
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                    Text(
                        "Инструктор: ",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        instructorName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Start,
                    )
                }
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                    Text(
                        "Статус: ",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        statusText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Start,
                    )
                }
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                    Text(
                        "Причина отмены: ",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        session.cancelReason?.takeIf { it.isNotBlank() } ?: "—",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Start,
                    )
                }
            }
        }
    }
}

private val balanceCreditGreen = Color(0xFF2E7D32)
private val balanceDebitRed = Color(0xFFC62828)

@Composable
private fun BalanceEntryCard(entry: BalanceHistory, performedByName: String) {
    val typeLabel = when (entry.type) {
        "credit" -> "Зачисление"
        "debit" -> "Списание"
        "set" -> "Установка"
        else -> entry.type
    }
    val sign = when (entry.type) {
        "credit" -> "+"
        "debit" -> "−"
        else -> ""
    }
    val typeLabelColor = when (entry.type) {
        "credit" -> balanceCreditGreen
        "debit" -> balanceDebitRed
        else -> MaterialTheme.colorScheme.onSurface
    }
    val badgeColor = when (entry.type) {
        "credit" -> balanceCreditGreen
        "debit" -> balanceDebitRed
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val dateTimeStr = entry.timestamp?.toDate()?.let { dateTimeFormat.format(it) } ?: "—"
    val kemLabel = if (entry.type == "credit") "Администратор" else performedByName

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalAlignment = Alignment.Start,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    typeLabel,
                    style = MaterialTheme.typography.titleSmall,
                    color = typeLabelColor,
                    modifier = Modifier.weight(1f),
                )
                Surface(
                    shape = CircleShape,
                    color = badgeColor,
                ) {
                    Text(
                        "$sign${entry.amount} талонов",
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    )
                }
            }
            Text(
                "Кем: $kemLabel",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(1f),
            )
            Text(
                "Дата и время: $dateTimeStr",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(1f),
            )
        }
    }
}
