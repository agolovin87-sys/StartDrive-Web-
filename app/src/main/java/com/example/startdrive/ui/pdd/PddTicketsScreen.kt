package com.example.startdrive.ui.pdd

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.background
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.startdrive.data.repository.PddCategory
import com.example.startdrive.data.repository.PddRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PddTicketsScreen(
    selectedCategoryId: String?,
    onBack: () -> Unit,
    onCategoryClick: (String) -> Unit,
    onTicketClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val categories = remember { PddRepository.getCategories() }
    val tickets = remember(selectedCategoryId) {
        if (selectedCategoryId != null) PddRepository.getTicketNumbers(selectedCategoryId) else emptyList()
    }
    val categoryTitle = remember(selectedCategoryId) {
        categories.find { it.id == selectedCategoryId }?.title ?: ""
    }
    var showResetConfirm by remember { mutableStateOf(false) }
    var fullyCorrectTickets by remember { mutableStateOf<Set<String>>(emptySet()) }
    var ticketStats by remember { mutableStateOf<Map<String, Triple<Int, Int, Int>>>(emptyMap()) } // ticketName -> (correct, incorrect, noAnswer)
    val isAbOrCd = selectedCategoryId == "A_B" || selectedCategoryId == "C_D"

    LaunchedEffect(selectedCategoryId) {
        if (selectedCategoryId != null && isAbOrCd) {
            val prefs = context.getSharedPreferences("pdd_stats", Context.MODE_PRIVATE)
            val list = PddRepository.getTicketNumbers(selectedCategoryId)
            val set = mutableSetOf<String>()
            val stats = mutableMapOf<String, Triple<Int, Int, Int>>()
            val questionsPerTicket = 20
            list.forEach { ticketName ->
                val key = "pdd_${selectedCategoryId}_$ticketName"
                val saved = prefs.getString(key, null)
                val pairs = saved?.split(',')?.mapNotNull { part ->
                    val p = part.split(':')
                    if (p.size == 2) p[0].toIntOrNull() to (p[1] == "t") else null
                } ?: emptyList()
                if (pairs.size == questionsPerTicket && pairs.all { it.second }) set.add(ticketName)
                val correct = pairs.count { it.second }
                val incorrect = pairs.count { !it.second }
                val noAnswer = questionsPerTicket - pairs.size
                stats[ticketName] = Triple(correct, incorrect, noAnswer)
            }
            fullyCorrectTickets = set
            ticketStats = stats
        } else {
            fullyCorrectTickets = emptySet()
            ticketStats = emptyMap()
        }
    }

    if (showResetConfirm && isAbOrCd) {
        AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            title = { Text("Обнулить статистику") },
            text = { Text("Вы уверены обнулить статистику решений экзаменационных билетов?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        val prefs = context.getSharedPreferences("pdd_stats", Context.MODE_PRIVATE)
                        prefs.edit().apply {
                            prefs.all.keys
                                .filter { it.startsWith("pdd_${selectedCategoryId}_") }
                                .forEach { remove(it) }
                            apply()
                        }
                        fullyCorrectTickets = emptySet()
                        ticketStats = emptyMap()
                        showResetConfirm = false
                    },
                ) {
                    Text("Да", color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirm = false }) {
                    Text("Нет", color = MaterialTheme.colorScheme.onSurface)
                }
            },
        )
    }

    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (selectedCategoryId != null) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                }
                Spacer(Modifier.width(8.dp))
            }
            Text(
                if (selectedCategoryId != null) categoryTitle else "Билеты ПДД",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            if (isAbOrCd) {
                IconButton(onClick = { showResetConfirm = true }) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Обнулить статистику",
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (selectedCategoryId == null) {
                items(categories, key = { it.id }) { category ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onCategoryClick(category.id) },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                Icons.Default.Quiz,
                                contentDescription = null,
                                modifier = Modifier.size(32.dp),
                                tint = MaterialTheme.colorScheme.primary,
                            )
                            Spacer(Modifier.width(16.dp))
                            Text(
                                category.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                }
            } else {
                items(tickets, key = { it }) { ticketName ->
                    val allCorrect = ticketName in fullyCorrectTickets
                    val cardColor = if (allCorrect) Color(0xFF1B5E20) else MaterialTheme.colorScheme.surfaceVariant
                    val contentColor = if (allCorrect) Color.White else MaterialTheme.colorScheme.onSurface
                    val iconTint = if (allCorrect) Color.White else MaterialTheme.colorScheme.primary
                    val stats = ticketStats[ticketName] ?: Triple(0, 0, 20)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onTicketClick(ticketName) },
                        colors = CardDefaults.cardColors(containerColor = cardColor),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                Icons.Default.Quiz,
                                contentDescription = null,
                                modifier = Modifier.size(32.dp),
                                tint = iconTint,
                            )
                            Spacer(Modifier.width(16.dp))
                            Text(
                                ticketName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium,
                                color = contentColor,
                                modifier = Modifier.weight(1f),
                            )
                            if (isAbOrCd) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(14.dp)
                                                .background(Color(0xFF4CAF50), RectangleShape),
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Text(
                                            "${stats.first}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = contentColor,
                                        )
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(14.dp)
                                                .background(Color(0xFFE53935), RectangleShape),
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Text(
                                            "${stats.second}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = contentColor,
                                        )
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(14.dp)
                                                .background(Color(0xFF424242), RectangleShape),
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Text(
                                            "${stats.third}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = contentColor,
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
}
