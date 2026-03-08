package com.example.startdrive.ui.pdd

import android.graphics.BitmapFactory
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import android.content.Context
import com.example.startdrive.data.model.PddQuestion
import com.example.startdrive.data.repository.PddRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Экран одного или нескольких вопросов по списку (для режима «по разделам»). */
@Composable
fun PddQuestionsScreen(
    questions: List<PddQuestion>,
    title: String,
    onBack: () -> Unit,
    initialIndex: Int = 0,
    sectionName: String? = null,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var currentIndex by remember { mutableIntStateOf(initialIndex.coerceIn(0, (questions.size - 1).coerceAtLeast(0))) }
    var selectedAnswer by remember { mutableIntStateOf(-1) }
    var answered by remember { mutableStateOf(false) }
    var answerResults by remember(sectionName) { mutableStateOf<Map<Int, Boolean>>(emptyMap()) }

    LaunchedEffect(title, questions.size, initialIndex) {
        currentIndex = initialIndex.coerceIn(0, (questions.size - 1).coerceAtLeast(0))
        selectedAnswer = -1
        answered = false
    }

    LaunchedEffect(questions, sectionName) {
        if (sectionName == null || questions.isEmpty()) return@LaunchedEffect
        val prefs = context.getSharedPreferences("pdd_stats", Context.MODE_PRIVATE)
        val key = "pdd_by_topic_$sectionName"
        val saved = prefs.getString(key, null) ?: return@LaunchedEffect
        val map = mutableMapOf<Int, Boolean>()
        saved.split(',').forEach { part ->
            val pair = part.split(':')
            if (pair.size == 2) pair[0].toIntOrNull()?.let { idx -> map[idx] = pair[1] == "t" }
        }
        if (map.isNotEmpty()) answerResults = map
    }

    LaunchedEffect(answerResults, sectionName) {
        if (sectionName == null || questions.isEmpty()) return@LaunchedEffect
        val prefs = context.getSharedPreferences("pdd_stats", Context.MODE_PRIVATE)
        val key = "pdd_by_topic_$sectionName"
        val value = answerResults.entries.sortedBy { it.key }.joinToString(",") { "${it.key}:${if (it.value) "t" else "f"}" }
        prefs.edit().putString(key, value).apply()
    }

    if (questions.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Нет вопросов", style = MaterialTheme.typography.bodyLarge)
        }
        return
    }

    val q = questions[currentIndex]
    val isLast = currentIndex == questions.lastIndex

    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
            }
            Text(
                "$title — Вопрос ${currentIndex + 1} из ${questions.size}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                q.question,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            PddQuestionImage(imagePath = q.imagePath, context = context)
            q.answers.forEachIndexed { index, answer ->
                val isSelected = selectedAnswer == index
                val isCorrect = answer.isCorrect
                val showResult = answered
                val tint = when {
                    !showResult -> MaterialTheme.colorScheme.surface
                    isCorrect -> Color(0xFF4CAF50)
                    isSelected && !isCorrect -> Color(0xFFE53935)
                    else -> MaterialTheme.colorScheme.surface
                }
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(
                            if (!answered) Modifier
                            else Modifier.border(2.dp, if (isCorrect) Color(0xFF4CAF50) else if (isSelected) Color(0xFFE53935) else Color.Transparent, RoundedCornerShape(12.dp))
                        )
                        .then(if (!answered) Modifier.clickable {
                            selectedAnswer = index
                            answered = true
                            if (sectionName != null) answerResults = answerResults + (currentIndex to answer.isCorrect)
                        } else Modifier),
                    colors = CardDefaults.cardColors(containerColor = tint),
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = null,
                            enabled = !answered,
                            colors = RadioButtonDefaults.colors(
                                selectedColor = MaterialTheme.colorScheme.primary,
                                unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            ),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            answer.answerText,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
            if (answered) {
                Spacer(Modifier.height(8.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            q.correctAnswer,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                        if (q.answerTip.isNotBlank()) {
                            Spacer(Modifier.height(6.dp))
                            Text(
                                q.answerTip,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                        }
                    }
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.End,
        ) {
            if (answered) {
                Button(
                    onClick = {
                        if (isLast) onBack()
                        else {
                            currentIndex++
                            selectedAnswer = -1
                            answered = false
                        }
                    },
                ) {
                    Text(if (isLast) "Завершить" else "Далее")
                }
            }
        }
    }
}

@Composable
fun PddTicketScreen(
    categoryId: String,
    ticketName: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var questions by remember(categoryId, ticketName) { mutableStateOf<List<PddQuestion>?>(null) }
    var currentIndex by remember { mutableIntStateOf(0) }
    var selectedAnswer by remember { mutableIntStateOf(-1) }
    var answered by remember { mutableStateOf(false) }
    var answerResults by remember { mutableStateOf<Map<Int, Boolean>>(emptyMap()) }
    var showQuestionView by remember { mutableStateOf(false) }

    LaunchedEffect(categoryId, ticketName) {
        questions = null
        currentIndex = 0
        selectedAnswer = -1
        answered = false
        showQuestionView = false
        questions = withContext(Dispatchers.IO) {
            PddRepository.loadTicket(context, categoryId, ticketName)
        }
    }

    LaunchedEffect(questions, categoryId, ticketName) {
        val list = questions ?: return@LaunchedEffect
        if (list.isEmpty()) return@LaunchedEffect
        val prefs = context.getSharedPreferences("pdd_stats", Context.MODE_PRIVATE)
        val key = "pdd_${categoryId}_${ticketName}"
        val saved = prefs.getString(key, null) ?: return@LaunchedEffect
        val map = mutableMapOf<Int, Boolean>()
        saved.split(',').forEach { part ->
            val pair = part.split(':')
            if (pair.size == 2) {
                pair[0].toIntOrNull()?.let { idx ->
                    map[idx] = pair[1] == "t"
                }
            }
        }
        if (map.isNotEmpty()) answerResults = map
    }

    LaunchedEffect(answerResults, categoryId, ticketName) {
        val list = questions ?: return@LaunchedEffect
        if (list.isEmpty()) return@LaunchedEffect
        val prefs = context.getSharedPreferences("pdd_stats", Context.MODE_PRIVATE)
        val key = "pdd_${categoryId}_${ticketName}"
        val value = answerResults.entries.sortedBy { it.key }.joinToString(",") { "${it.key}:${if (it.value) "t" else "f"}" }
        prefs.edit().putString(key, value).apply()
    }

    when {
        questions == null -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Загрузка…", style = MaterialTheme.typography.bodyLarge)
            }
            return
        }
        questions!!.isEmpty() -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Нет вопросов", style = MaterialTheme.typography.bodyLarge)
            }
            Row(
                Modifier.fillMaxWidth().padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                }
            }
            return
        }
    }
    val questionsList = questions!!

    val q = questionsList[currentIndex]
    val isLast = currentIndex == questionsList.lastIndex
    val currentTopic = q.topic.firstOrNull()?.takeIf { it.isNotBlank() } ?: "Прочее"
    val isNewTopic = currentIndex == 0 || questionsList[currentIndex - 1].topic.firstOrNull() != q.topic.firstOrNull()

    val sectionsByTopic = remember(questionsList, answerResults) {
        questionsList.mapIndexed { index, question ->
            val topicName = question.topic.firstOrNull()?.takeIf { it.isNotBlank() } ?: "Прочее"
            Triple(index, topicName, answerResults[index])
        }.groupBy { it.second }.mapValues { (_, items) -> items.sortedBy { it.first } }
    }

    Column(modifier = modifier.fillMaxSize()) {
        if (!showQuestionView) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                }
                Text(
                    "$ticketName — Подразделы",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
                    .padding(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                sectionsByTopic.forEach { (topicName, items) ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                topicName,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(bottom = 8.dp),
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                items.forEach { (index, _, result) ->
                                    val isCurrent = index == currentIndex
                                    Row(
                                        modifier = Modifier
                                            .clickable {
                                                currentIndex = index
                                                selectedAnswer = -1
                                                answered = false
                                                showQuestionView = true
                                            }
                                            .border(
                                                width = if (isCurrent) 2.dp else 1.dp,
                                                color = when (result) {
                                                    true -> Color(0xFF4CAF50)
                                                    false -> Color(0xFFE53935)
                                                    null -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                                },
                                                shape = RoundedCornerShape(6.dp),
                                            )
                                            .padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    ) {
                                        Text(
                                            "${index + 1}",
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurface,
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
            }
        } else {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
            }
            Button(
                onClick = { showQuestionView = false },
                modifier = Modifier.padding(horizontal = 4.dp),
            ) {
                Text("Подразделы")
            }
            Spacer(Modifier.width(8.dp))
            Text(
                "$ticketName — Вопрос ${currentIndex + 1} из ${questionsList.size}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .padding(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (isNewTopic) {
                Text(
                    currentTopic,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.height(4.dp))
            }
            Text(
                q.question,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            PddQuestionImage(imagePath = q.imagePath, context = context)
            q.answers.forEachIndexed { index, answer ->
                val isSelected = selectedAnswer == index
                val isCorrect = answer.isCorrect
                val showResult = answered
                val tint = when {
                    !showResult -> MaterialTheme.colorScheme.surface
                    isCorrect -> Color(0xFF4CAF50)
                    isSelected && !isCorrect -> Color(0xFFE53935)
                    else -> MaterialTheme.colorScheme.surface
                }
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(
                            if (!answered) Modifier
                            else Modifier.border(2.dp, if (isCorrect) Color(0xFF4CAF50) else if (isSelected) Color(0xFFE53935) else Color.Transparent, RoundedCornerShape(12.dp))
                        )
                        .then(if (!answered) Modifier.clickable {
                            selectedAnswer = index
                            answered = true
                            answerResults = answerResults + (currentIndex to answer.isCorrect)
                        } else Modifier),
                    colors = CardDefaults.cardColors(containerColor = tint),
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = null,
                            enabled = !answered,
                            colors = RadioButtonDefaults.colors(
                                selectedColor = MaterialTheme.colorScheme.primary,
                                unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            ),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            answer.answerText,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
            if (answered) {
                Spacer(Modifier.height(8.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            q.correctAnswer,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                        if (q.answerTip.isNotBlank()) {
                            Spacer(Modifier.height(6.dp))
                            Text(
                                q.answerTip,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                        }
                    }
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.End,
        ) {
            if (answered) {
                Button(
                    onClick = {
                        if (isLast) {
                            showQuestionView = false
                        } else {
                            currentIndex++
                            selectedAnswer = -1
                            answered = false
                        }
                    },
                ) {
                    Text(if (isLast) "К подразделам" else "Далее")
                }
            }
        }
        }
    }
}

@Composable
private fun PddQuestionImage(
    imagePath: String?,
    context: android.content.Context,
    modifier: Modifier = Modifier,
) {
    var bitmap by remember(imagePath) { mutableStateOf<android.graphics.Bitmap?>(null) }
    LaunchedEffect(imagePath) {
        bitmap = if (!imagePath.isNullOrBlank()) {
            withContext(Dispatchers.IO) {
                try {
                    context.assets.open(imagePath).use { BitmapFactory.decodeStream(it) }
                } catch (_: Exception) { null }
            }
        } else null
    }
    if (bitmap != null) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp)),
        ) {
            Image(
                bitmap = bitmap!!.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillWidth,
            )
        }
    }
}
