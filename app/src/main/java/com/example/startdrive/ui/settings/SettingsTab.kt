package com.example.startdrive.ui.settings

import android.app.Activity
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import com.example.startdrive.data.repository.AppSettings
import com.example.startdrive.data.repository.HiddenChatMessages
import com.example.startdrive.data.repository.UserRepository
import com.example.startdrive.shared.di.SharedFactory
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

@Composable
fun SettingsTab(
    onBack: () -> Unit,
    currentUserId: String? = null,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val userRepo = remember { UserRepository() }
    var soundEnabled by remember {
        mutableStateOf(AppSettings.getSoundNotificationsEnabled(context))
    }
    var textEnabled by remember {
        mutableStateOf(AppSettings.getTextNotificationsEnabled(context))
    }
    val currentTheme = remember { AppSettings.getTheme(context) }
    var avatarCropUri by remember { mutableStateOf<Uri?>(null) }
    var avatarListKey by remember { mutableStateOf(0) }
    var chatAvatarUrlFromCloud by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(currentUserId, avatarListKey) {
        if (currentUserId != null) {
            val u = userRepo.getUser(currentUserId)
            chatAvatarUrlFromCloud = u?.chatAvatarUrl
        } else {
            chatAvatarUrlFromCloud = null
        }
    }
    val hasLocalAvatar = remember(avatarListKey) { AppSettings.getChatAvatarPath(context) != null }
    val hasChatAvatar = hasLocalAvatar || !chatAvatarUrlFromCloud.isNullOrBlank()
    var showDeleteAvatarConfirm by remember { mutableStateOf(false) }
    var showClearHistoryConfirm by remember { mutableStateOf(false) }
    var showAbout by remember { mutableStateOf(false) }
    val pickImage = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri -> avatarCropUri = uri }

    if (avatarCropUri != null) {
        AvatarCropScreen(
            imageUri = avatarCropUri!!,
            onDone = { avatarCropUri = null; avatarListKey++ },
            onCancel = { avatarCropUri = null },
            currentUserId = currentUserId,
            modifier = modifier.fillMaxSize(),
        )
        return
    }

    if (showAbout) {
        AboutAppScreen(onBack = { showAbout = false }, modifier = modifier.fillMaxSize())
        return
    }

    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
            }
            Spacer(Modifier.width(8.dp))
            Text(
                "Настройки",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        HorizontalDivider()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            // ——— Уведомления ———
            Text(
                "Уведомления",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 8.dp, bottom = 12.dp),
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "Звуковые уведомления (голос)",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Switch(
                    checked = soundEnabled,
                    onCheckedChange = {
                        soundEnabled = it
                        AppSettings.setSoundNotificationsEnabled(context, it)
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary,
                        uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                        uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                    ),
                )
            }
            HorizontalDivider()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Текстовые уведомления",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        "При выключении не показываются на экране, но сохраняются во вкладке «Уведомления»",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(Modifier.width(16.dp))
                Switch(
                    checked = textEnabled,
                    onCheckedChange = {
                        textEnabled = it
                        AppSettings.setTextNotificationsEnabled(context, it)
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary,
                        uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                        uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                    ),
                )
            }
            HorizontalDivider()
            Spacer(Modifier.height(16.dp))

            // ——— Тема ———
            Text(
                "Тема",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 12.dp),
            )
            listOf(
                AppSettings.THEME_LIGHT to "Светлая",
                AppSettings.THEME_DARK to "Тёмная (демо режим)",
            ).forEach { (themeKey, label) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = currentTheme == themeKey,
                            onClick = {
                                AppSettings.setTheme(context, themeKey)
                                (context as? Activity)?.recreate()
                            },
                        )
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(
                        selected = currentTheme == themeKey,
                        onClick = {
                            AppSettings.setTheme(context, themeKey)
                            (context as? Activity)?.recreate()
                        },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = MaterialTheme.colorScheme.primary,
                            unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        label,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
            HorizontalDivider()
            Spacer(Modifier.height(16.dp))

            // ——— Чат ———
            Text(
                "Чат",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 8.dp),
            )
            Text(
                "Сменить аватар в чате",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 4.dp),
            )
            Text(
                "При нажатии можно загрузить своё фото или картинку — оно будет отображаться как ваша аватарка в чате. При выборе фото можно настроить обрезку по кругу.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 12.dp),
            )
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    "Текущий аватар:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                val avatarSize = 56.dp
                Box(
                    modifier = Modifier
                        .size(avatarSize)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { pickImage.launch("image/*") },
                    contentAlignment = Alignment.Center,
                ) {
                    when {
                        hasLocalAvatar -> {
                            val path = AppSettings.getChatAvatarPath(context)
                            if (path != null) {
                                AsyncImage(
                                    model = java.io.File(path),
                                    contentDescription = "Нажмите для выбора фото",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop,
                                )
                            } else {
                                Text(
                                    "+",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                        !chatAvatarUrlFromCloud.isNullOrBlank() -> {
                            AsyncImage(
                                model = chatAvatarUrlFromCloud,
                                contentDescription = "Нажмите для выбора фото",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop,
                            )
                        }
                        else -> {
                            Text(
                                "+",
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
                if (hasChatAvatar) {
                    Text(
                        "Удалить аватар",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.clickable { showDeleteAvatarConfirm = true },
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            Text(
                "История чата — очистить",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 4.dp),
            )
            Text(
                "Удаляет всю переписку в чате со всеми контактами.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp),
            )
            Text(
                "Очистить историю",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.clickable { showClearHistoryConfirm = true },
            )
            Spacer(Modifier.height(32.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = { showAbout = true }) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "О приложении",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
        if (showDeleteAvatarConfirm) {
            AlertDialog(
                onDismissRequest = { showDeleteAvatarConfirm = false },
                title = { Text("Удалить аватар?") },
                text = { Text("Аватар будет удалён везде: из настроек, из чата у контактов. В кружочке контакта снова будет цветной фон с инициалами.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showDeleteAvatarConfirm = false
                            AppSettings.clearChatAvatar(context)
                            chatAvatarUrlFromCloud = null
                            avatarListKey++
                            currentUserId?.let { uid ->
                                scope.launch {
                                    try {
                                        userRepo.deleteChatAvatarFromStorage(uid)
                                        userRepo.updateChatAvatarUrl(uid, null)
                                        android.widget.Toast.makeText(context, "Аватар удалён", android.widget.Toast.LENGTH_SHORT).show()
                                    } catch (e: Exception) {
                                        android.widget.Toast.makeText(context, "Не удалось удалить: ${e.message ?: "ошибка"}", android.widget.Toast.LENGTH_LONG).show()
                                    }
                                }
                            }
                        },
                    ) {
                        Text("Да", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteAvatarConfirm = false }) {
                        Text("Нет")
                    }
                },
            )
        }
        if (showClearHistoryConfirm) {
            AlertDialog(
                onDismissRequest = { showClearHistoryConfirm = false },
                title = { Text("Очистить историю чата?") },
                text = null,
                confirmButton = {
                    TextButton(
                        onClick = {
                            showClearHistoryConfirm = false
                            currentUserId?.let { uid ->
                                HiddenChatMessages.clearAllForUser(context, uid)
                                android.widget.Toast.makeText(context, "Локальная история очищена", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        },
                    ) {
                        Text("Да", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showClearHistoryConfirm = false }) {
                        Text("Нет")
                    }
                },
            )
        }
    }
}

private val aboutAppTitle = "StartDrive — Цифровая платформа для автошкол"

private fun aboutAppParagraphs(appInfo: com.example.startdrive.shared.model.AppInfo) = listOf(
    "StartDrive — это современное приложение, созданное для автоматизации работы автошкол и повышения качества обучения. Оно объединяет администраторов, инструкторов и курсантов в едином цифровом пространстве, делая процесс обучения прозрачным, удобным и эффективным.",
    "Ключевые возможности приложения:",
    "Удобная запись на вождение — курсанты могут видеть свободные окна инструкторов и бронировать занятия, а инструкторы — управлять своим расписанием.",
    "Учет баланса талонов — автоматическое списание талонов за проведенные занятия и контроль остатка.",
    "Статистика успеваемости для курсантов — детальный разбор каждого занятия, оценки от инструктора, динамика прогресса.",
    "Аналитика и рейтинг для инструкторов — общее количество проведенных занятий, средняя оценка от курсантов, круговая диаграмма распределения оценок и недельные графики загруженности.",
    "Встроенный чат с уведомлениями — возможность оперативно связаться с инструктором или администратором, обсудить детали занятий.",
    "Подготовка к теоретическому экзамену — решение экзаменационных билетов ПДД прямо в приложении с возможностью отслеживать прогресс и работать над ошибками.",
    "StartDrive — это ваш надежный помощник на пути к получению водительского удостоверения.",
    "Версия: ${appInfo.version}",
    "Разработчик: ООО \"Старт-Авто\"",
    "Платформа: ${appInfo.platform}",
    "По всем вопросам обращайтесь: start-auto13@mail.ru",
)

@Composable
private fun AboutAppScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val appInfo = remember { SharedFactory.getAppInfoRepository().getAppInfo() }
    val paragraphs = remember(appInfo) { aboutAppParagraphs(appInfo) }
    val scrollState = rememberScrollState()
    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
            }
            Spacer(Modifier.width(8.dp))
            Text(
                "О приложении",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        HorizontalDivider()
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                aboutAppTitle,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            paragraphs.forEachIndexed { index, paragraph ->
                val annotated = buildAnnotatedString {
                    when {
                        paragraph == "Ключевые возможности приложения:" -> {
                            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(paragraph) }
                        }
                        paragraph.startsWith("StartDrive — это ваш надежный помощник") -> {
                            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(paragraph) }
                        }
                        paragraph.contains(" — ") -> {
                            val (boldPart, rest) = paragraph.split(" — ", limit = 2)
                            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(boldPart) }
                            append(" — $rest")
                        }
                        paragraph.startsWith("Версия:") -> {
                            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("Версия:") }
                            append(paragraph.removePrefix("Версия:"))
                        }
                        paragraph.startsWith("Разработчик:") -> {
                            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("Разработчик:") }
                            append(paragraph.removePrefix("Разработчик:"))
                        }
                        paragraph.startsWith("Платформа:") -> {
                            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("Платформа:") }
                            append(paragraph.removePrefix("Платформа:"))
                        }
                        paragraph.startsWith("По всем вопросам") -> {
                            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                                append("По всем вопросам обращайтесь:")
                            }
                            append(paragraph.removePrefix("По всем вопросам обращайтесь:"))
                        }
                        else -> append(paragraph)
                    }
                }
                Text(
                    text = annotated,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Start,
                )
            }
        }
    }
}
