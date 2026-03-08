package com.example.startdrive.ui.pdd

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.startdrive.data.repository.PddRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun PddTopicsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var sections by remember { mutableStateOf<List<PddRepository.TopicSection>>(emptyList()) }
    var selectedSection by remember { mutableStateOf<PddRepository.TopicSection?>(null) }
    var selectedQuestionIndex by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(Unit) {
        sections = withContext(Dispatchers.IO) {
            PddRepository.loadQuestionsByTopic(context, "A_B")
        }
    }

    var fullyCorrectSections by remember { mutableStateOf<Set<String>>(emptySet()) }
    LaunchedEffect(sections) {
        if (sections.isEmpty()) return@LaunchedEffect
        val prefs = context.getSharedPreferences("pdd_stats", Context.MODE_PRIVATE)
        val set = mutableSetOf<String>()
        sections.forEach { sec ->
            val key = "pdd_by_topic_${sec.name}"
            val saved = prefs.getString(key, null) ?: return@forEach
            val pairs = saved.split(',').mapNotNull { part ->
                val p = part.split(':')
                if (p.size == 2) p[0].toIntOrNull() to (p[1] == "t") else null
            }
            if (pairs.size == sec.questions.size && pairs.all { it.second }) set.add(sec.name)
        }
        fullyCorrectSections = set
    }

    var showResetConfirm by remember { mutableStateOf(false) }

    if (showResetConfirm) {
        AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            title = { Text("Обнулить статистику") },
            text = { Text("Вы уверены обнулить статистику решений экзаменационных билетов?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        val prefs = context.getSharedPreferences("pdd_stats", Context.MODE_PRIVATE)
                        prefs.edit().apply {
                            prefs.all.keys.filter { it.startsWith("pdd_by_topic_") }.forEach { remove(it) }
                            apply()
                        }
                        fullyCorrectSections = emptySet()
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

    val section = selectedSection
    val questionIndex = selectedQuestionIndex

    when {
        section != null && questionIndex != null -> PddQuestionsScreen(
            questions = section.questions,
            title = section.name,
            onBack = { selectedQuestionIndex = null },
            initialIndex = questionIndex,
            sectionName = section.name,
        )
        section != null -> {
            val sec = section
            val sectionQuestions = sec.questions
            var sectionAnswerResults by remember(sec.name) { mutableStateOf<Map<Int, Boolean>>(emptyMap()) }
            LaunchedEffect(sec.name) {
                val prefs = context.getSharedPreferences("pdd_stats", Context.MODE_PRIVATE)
                val key = "pdd_by_topic_${sec.name}"
                val saved = prefs.getString(key, null) ?: run { sectionAnswerResults = emptyMap(); return@LaunchedEffect }
                val map = mutableMapOf<Int, Boolean>()
                saved.split(',').forEach { part ->
                    val pair = part.split(':')
                    if (pair.size == 2) pair[0].toIntOrNull()?.let { idx -> map[idx] = pair[1] == "t" }
                }
                sectionAnswerResults = map
            }
            Column(modifier = modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = { selectedSection = null }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                    Text(
                        sec.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(
                        sectionQuestions.size,
                        key = { index -> index },
                    ) { index ->
                        val q = sectionQuestions.getOrNull(index) ?: return@items
                        val result = sectionAnswerResults[index]
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedQuestionIndex = index },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(12.dp),
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    Icons.Default.Quiz,
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp),
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    "Вопрос ${index + 1}. ${q.question.take(80)}${if (q.question.length > 80) "…" else ""}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f),
                                )
                                when (result) {
                                    true -> Box(
                                        modifier = Modifier
                                            .size(26.dp)
                                            .background(Color(0xFF4CAF50).copy(alpha = 0.2f), RoundedCornerShape(13.dp)),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Icon(Icons.Filled.Check, contentDescription = "Верно", modifier = Modifier.size(22.dp), tint = Color(0xFF4CAF50))
                                    }
                                    false -> Box(
                                        modifier = Modifier
                                            .size(26.dp)
                                            .background(Color(0xFFE53935).copy(alpha = 0.2f), RoundedCornerShape(13.dp)),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Icon(Icons.Filled.Close, contentDescription = "Неверно", modifier = Modifier.size(22.dp), tint = Color(0xFFE53935))
                                    }
                                    null -> Spacer(Modifier.size(26.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
        else -> Column(modifier = modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                }
                Text(
                    "Вопросы по разделам",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = { showResetConfirm = true }) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Обнулить статистику",
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(sections, key = { it.name }) { sec ->
                    val allCorrect = sec.name in fullyCorrectSections
                    val cardColor = if (allCorrect) Color(0xFF1B5E20) else MaterialTheme.colorScheme.surfaceVariant
                    val contentColor = if (allCorrect) Color.White else MaterialTheme.colorScheme.onSurface
                    val secondaryColor = if (allCorrect) Color.White.copy(alpha = 0.9f) else MaterialTheme.colorScheme.onSurfaceVariant
                    val iconTint = if (allCorrect) Color.White else MaterialTheme.colorScheme.primary
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedSection = sec },
                        colors = CardDefaults.cardColors(containerColor = cardColor),
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
                                tint = iconTint,
                            )
                            Spacer(Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    sec.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = contentColor,
                                )
                                Text(
                                    "Вопросов: ${sec.questions.size}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = secondaryColor,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
