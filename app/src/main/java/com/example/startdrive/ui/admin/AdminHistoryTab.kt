package com.example.startdrive.ui.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.startdrive.data.model.BalanceHistory
import com.example.startdrive.data.repository.DrivingRepository
import com.example.startdrive.data.repository.UserRepository
import com.example.startdrive.ui.components.CollapsibleCard
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())

@Composable
fun AdminHistoryTab(adminId: String) {
    val userRepo = remember { UserRepository() }
    val drivingRepo = remember { DrivingRepository() }
    val instructors by userRepo.usersByRole("instructor").collectAsState(initial = emptyList())
    val cadets by userRepo.usersByRole("cadet").collectAsState(initial = emptyList())
    val balanceHistory by drivingRepo.allBalanceHistory().collectAsState(initial = emptyList())
    val userIdToName = remember(instructors, cadets) {
        (instructors + cadets).associate { it.id to it.fullName }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            CollapsibleCard(title = "История: Зачисления и списания", count = balanceHistory.size, defaultExpanded = false) {
                if (balanceHistory.isEmpty()) {
                    Text("Нет операций", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(8.dp))
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        balanceHistory.forEach { h ->
                            BalanceHistoryRow(entry = h, userName = userIdToName[h.userId] ?: h.userId)
                        }
                    }
                }
            }
        }
        item {
            CollapsibleCard(title = "История: Вождение", defaultExpanded = false) {
                Text(
                    "Завершённые и отменённые вождения — driving_sessions",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start,
                )
            }
        }
        item {
            CollapsibleCard(title = "История: Чат", defaultExpanded = false) {
                Text("Просмотр переписки — выбрать контакт во вкладке Чат", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun BalanceHistoryRow(entry: BalanceHistory, userName: String) {
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant),
        shape = MaterialTheme.shapes.small,
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(userName, style = MaterialTheme.typography.titleSmall)
                Text("$typeLabel · ${entry.timestamp?.toDate()?.let { dateFormat.format(it) } ?: "—"}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(
                "$sign${entry.amount} тал.",
                style = MaterialTheme.typography.bodyMedium,
                color = when (entry.type) {
                    "credit" -> MaterialTheme.colorScheme.primary
                    "debit" -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.onSurface
                },
            )
        }
    }
}
