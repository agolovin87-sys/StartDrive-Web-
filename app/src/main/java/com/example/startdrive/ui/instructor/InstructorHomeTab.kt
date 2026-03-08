package com.example.startdrive.ui.instructor

import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Card
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.TextButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.border
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import com.example.startdrive.ui.theme.CardOutlineWhite
import com.example.startdrive.ui.components.GlossyCancelIconButton
import com.example.startdrive.ui.components.AnimatedGlossOverlay
import com.example.startdrive.ui.components.TopLeftCornerShimmer
import com.example.startdrive.ui.components.BalanceBadge
import com.example.startdrive.ui.components.glossySheen
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.startdrive.ui.chat.colorForContactId
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import com.example.startdrive.R
import com.example.startdrive.data.model.DrivingSession
import com.example.startdrive.data.model.User
import com.example.startdrive.data.repository.DrivingRepository
import com.example.startdrive.data.repository.UserRepository
import com.example.startdrive.ui.components.CollapsibleCard
import com.example.startdrive.ui.components.CollapsibleSection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val scheduleFormat = SimpleDateFormat("dd.MM EEE HH:mm", Locale.getDefault())

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
private val dateFormatShort = SimpleDateFormat("dd.MM.yy", Locale.getDefault())
private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
private const val START_ALLOWED_MINUTES_BEFORE = 15L
/** Ожидание подтверждения курсанта (мин); после этого вождение стартует автоматически. */
private const val AUTO_START_WAIT_MINUTES = 5L

@Composable
fun InstructorProfileTab(
    instructor: User,
    balanceBadgeScale: Float = 1f,
    onSettingsClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
) {
    val drivingRepo = remember { DrivingRepository() }
    val sessions by drivingRepo.sessionsForInstructor(instructor.id).collectAsState(initial = emptyList())
    val balanceHistory by drivingRepo.balanceHistory(instructor.id).collectAsState(initial = emptyList())
    val completedCount = remember(sessions) { sessions.count { it.status == "completed" } }
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            ProfileCard(instructor = instructor, balanceBadgeScale = balanceBadgeScale)
        }
        item {
            InstructorTotalEarnedCard(balanceHistory = balanceHistory)
        }
        item {
            InstructorCompletedDrivingsCard(completedCount = completedCount)
        }
        item {
            InstructorCadetRatingsPieChart(sessions = sessions)
        }
        item {
            InstructorWeeklyWorkloadChart(sessions = sessions)
        }
        item {
            InstructorCancelledByCadetsChart(sessions = sessions)
        }
        item {
            Spacer(Modifier.height(8.dp))
        }
        item {
            Button(
                onClick = onSettingsClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                ),
            ) {
                Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Настройки")
            }
        }
        item {
            Button(
                onClick = onLogoutClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                ),
            ) {
                Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Выйти")
            }
        }
        item {
            Spacer(Modifier.height(24.dp))
        }
    }
}
private const val DRIVING_DURATION_MINUTES = 90L

/** Порядок дней недели: понедельник .. воскресенье; значение Calendar.DAY_OF_WEEK */
private val WEEKDAY_ORDER = listOf(
    Calendar.MONDAY,
    Calendar.TUESDAY,
    Calendar.WEDNESDAY,
    Calendar.THURSDAY,
    Calendar.FRIDAY,
    Calendar.SATURDAY,
    Calendar.SUNDAY,
)
private val WEEKDAY_NAMES = listOf(
    "Понедельник",
    "Вторник",
    "Среда",
    "Четверг",
    "Пятница",
    "Суббота",
    "Воскресенье",
)

@Composable
fun InstructorHomeTab(
    instructor: User,
    onOpenChatWith: (String) -> Unit = {},
    triggerBalancePulse: Boolean = false,
    onBalancePulseDone: () -> Unit = {},
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val userRepo = remember { UserRepository() }
    val drivingRepo = remember { DrivingRepository() }
    val balanceScale = remember { androidx.compose.animation.core.Animatable(1f) }
    LaunchedEffect(triggerBalancePulse) {
        if (triggerBalancePulse) {
            try {
                MediaPlayer.create(context, R.raw.coin)?.apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    setVolume(1f, 1f)
                    setOnCompletionListener { release() }
                    start()
                }
            } catch (_: Exception) { }
            balanceScale.animateTo(1.3f, androidx.compose.animation.core.tween(150))
            balanceScale.animateTo(1f, androidx.compose.animation.core.tween(200))
            onBalancePulseDone()
        }
    }
    var cadets by remember { mutableStateOf<List<User>>(emptyList()) }
    var cadetMap by remember { mutableStateOf<Map<String, User>>(emptyMap()) }
    var profileInstructor by remember(instructor.id) { mutableStateOf(instructor) }
    val sessions by drivingRepo.sessionsForInstructor(instructor.id).collectAsState(initial = emptyList())
    val upcomingSessions = sessions.filter { it.status == "scheduled" || it.status == "inProgress" }
    val ctx = context
    val scope = rememberCoroutineScope()
    var sessionIdToConfirmCancel by remember { mutableStateOf<String?>(null) }
    var startDrivingConfirm by remember { mutableStateOf<Pair<String, Long>?>(null) } // sessionId to scheduledStartTimeMillis
    var runningLateSessionId by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(instructor.id) {
        cadets = instructor.assignedCadets.mapNotNull { id -> userRepo.getUser(id) }
        profileInstructor = userRepo.getUser(instructor.id) ?: instructor
    }
    LaunchedEffect(instructor) {
        profileInstructor = instructor
    }
    LaunchedEffect(sessions) {
        scope.launch {
            userRepo.getUser(instructor.id)?.let { profileInstructor = it }
        }
    }
    LaunchedEffect(upcomingSessions.map { it.cadetId }) {
        cadetMap = upcomingSessions.map { it.cadetId }.distinct().associateWith { id -> userRepo.getUser(id) }.filterValues { it != null }.mapValues { it.value!! }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            ProfileCard(instructor = profileInstructor, balanceBadgeScale = balanceScale.value)
        }
        item {
            val hasInProgressSession = upcomingSessions.any { it.status == "inProgress" }
            CollapsibleCard(title = "Мои курсанты", count = cadets.size, icon = Icons.Default.Person, iconTint = sectionIconBlue, containerColor = Color(0xFFDDD5E8), titleColor = sectionIconBlue, titleFontWeight = FontWeight.Bold, modifier = Modifier.glossySheen(glossAlpha = 0.22f), defaultExpanded = !hasInProgressSession) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    cadets.forEach { c ->
                        val completedDrivingsCount = sessions.count { it.cadetId == c.id && it.status == "completed" }
                        CadetCard(
                            cadet = c,
                            completedDrivingsCount = completedDrivingsCount,
                            onChat = { onOpenChatWith(c.id) },
                            onCall = {
                                ctx.startActivity(Intent(Intent.ACTION_DIAL).setData(Uri.parse("tel:${c.phone}")))
                            },
                        )
                    }
                }
            }
        }
        item {
            val inProgressSession = upcomingSessions.find { it.status == "inProgress" }
            if (inProgressSession != null) {
                DrivingTimerBlock(
                    session = inProgressSession,
                    cadetName = formatSurnameWithInitials(cadetMap[inProgressSession.cadetId]?.fullName ?: inProgressSession.cadetId),
                    drivingRepo = drivingRepo,
                    onSessionCompleted = { },
                )
            }
        }
        item {
            val sessionsByWeekday = remember(upcomingSessions) {
                upcomingSessions.groupBy { s ->
                    s.startTime?.toDate()?.let { date ->
                        Calendar.getInstance().apply { time = date }.get(Calendar.DAY_OF_WEEK)
                    } ?: 0
                }
            }
            val hasInProgressSession = upcomingSessions.any { it.status == "inProgress" }
            CollapsibleCard(title = "Мой график", count = upcomingSessions.size, icon = Icons.Default.Schedule, iconTint = sectionIconBlue, containerColor = Color(0xFFDDD5E8), titleColor = sectionIconBlue, titleFontWeight = FontWeight.Bold, modifier = Modifier.glossySheen(glossAlpha = 0.22f), defaultExpanded = !hasInProgressSession) {
                if (upcomingSessions.isEmpty()) {
                    Text(
                        "Нет записанных на вождение курсантов",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp),
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        WEEKDAY_ORDER.forEachIndexed { index, dayOfWeek ->
                            val daySessions = sessionsByWeekday[dayOfWeek].orEmpty()
                                .sortedBy { it.startTime?.toDate()?.time ?: 0L }
                            val dayName = WEEKDAY_NAMES[index]
                            CollapsibleSection(
                                title = dayName,
                                count = daySessions.size,
                                icon = Icons.Default.Schedule,
                                iconTint = MaterialTheme.colorScheme.primary,
                                defaultExpanded = !hasInProgressSession && daySessions.isNotEmpty(),
                                modifier = if (index == 0) Modifier.padding(top = 4.dp) else Modifier,
                            ) {
                                if (daySessions.isEmpty()) {
                                    Text(
                                        "Нет занятий",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(start = 22.dp),
                                    )
                                } else {
                                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        daySessions.forEach { s ->
                                            ScheduleCard(
                                                dateStr = s.startTime?.toDate()?.let { dateFormatShort.format(it) } ?: "",
                                                timeStr = s.startTime?.toDate()?.let { timeFormat.format(it) } ?: "",
                                                cadetName = cadetMap[s.cadetId]?.fullName?.takeIf { it.isNotBlank() }?.let(::formatSurnameWithInitials) ?: s.cadetId,
                                                statusStr = when {
                                                    s.status == "scheduled" && s.openWindowId.isNotEmpty() && !s.instructorConfirmed -> "курсант забронировал — ожидает вашего подтверждения"
                                                    s.status == "scheduled" && !s.instructorConfirmed -> "ожидает подтверждения записи курсантом"
                                                    s.status == "scheduled" && s.startRequestedByInstructor -> "ожидает подтверждения начала курсантом"
                                                    s.status == "scheduled" && s.instructorConfirmed -> "подтверждён"
                                                    s.status == "inProgress" -> "в процессе"
                                                    else -> s.status
                                                },
                                                scheduledStartTimeMillis = s.startTime?.toDate()?.time,
                                                showConfirmBookingButton = s.status == "scheduled" && s.openWindowId.isNotEmpty() && !s.instructorConfirmed,
                                                onConfirmBooking = { scope.launch { drivingRepo.confirmBookingByInstructor(s.id) } },
                                                showStartButton = s.status == "scheduled" && s.instructorConfirmed && !s.startRequestedByInstructor,
                                                waitingForCadetConfirm = s.status == "scheduled" && s.startRequestedByInstructor,
                                                showCancelButton = true,
                                                cancelEnabled = when {
                                                    s.status == "inProgress" -> true
                                                    else -> s.session?.cadetConfirmed != true
                                                },
                                                onRequestStart = {
                                                    startDrivingConfirm = Pair(s.id, s.startTime?.toDate()?.time ?: 0L)
                                                },
                                                onCancel = { sessionIdToConfirmCancel = s.id },
                                                showRunningLateButton = s.status == "scheduled",
                                                onRunningLateClick = { runningLateSessionId = s.id },
                                                sessionKey = s.id,
                                                onAutoStart = { scope.launch { drivingRepo.startSession(s.id) } },
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
    sessionIdToConfirmCancel?.let { sessionId: String ->
        val cancelReasons = listOf("Курсант не явился", "На ремонте")
        var dropdownExpanded by remember { mutableStateOf(false) }
        var selectedReason by remember { mutableStateOf<String?>(null) }
        AlertDialog(
            onDismissRequest = { sessionIdToConfirmCancel = null },
            title = { Text("Отмена вождения") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Выберите причину:")
                    Box(modifier = Modifier.fillMaxWidth()) {
                        TextButton(
                            onClick = { dropdownExpanded = true },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(
                                selectedReason ?: "Выберите причину",
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Start,
                            )
                            Icon(Icons.Default.ExpandMore, contentDescription = null)
                        }
                        DropdownMenu(
                            expanded = dropdownExpanded,
                            onDismissRequest = { dropdownExpanded = false },
                        ) {
                            cancelReasons.forEach { reason ->
                                DropdownMenuItem(
                                    text = { Text(reason) },
                                    onClick = {
                                        selectedReason = reason
                                        dropdownExpanded = false
                                    },
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedReason?.let { reason ->
                            scope.launch { drivingRepo.cancelByInstructor(sessionId, reason) }
                            sessionIdToConfirmCancel = null
                        }
                    },
                    enabled = selectedReason != null,
                ) { Text("Подтвердить") }
            },
            dismissButton = {
                TextButton(onClick = { sessionIdToConfirmCancel = null }) { Text("Отмена") }
            },
        )
    }
    runningLateSessionId?.let { sessionId ->
        var selectedDelay by remember { mutableStateOf<Int?>(null) }
        AlertDialog(
            onDismissRequest = { runningLateSessionId = null },
            title = { Text("Опаздываю") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Выберите задержку:")
                    listOf(5, 10, 15).forEach { minutes ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            androidx.compose.material3.RadioButton(
                                selected = selectedDelay == minutes,
                                onClick = { selectedDelay = minutes },
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("$minutes мин.")
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedDelay?.let { delay ->
                            scope.launch {
                                drivingRepo.setInstructorRunningLate(sessionId, delay)
                                runningLateSessionId = null
                            }
                        }
                    },
                    enabled = selectedDelay != null,
                ) { Text("Подтвердить") }
            },
            dismissButton = {
                TextButton(onClick = { runningLateSessionId = null }) { Text("Отмена") }
            },
        )
    }
    startDrivingConfirm?.let { (sessionId, scheduledStartTimeMillis) ->
        var now by remember { mutableStateOf(System.currentTimeMillis()) }
        LaunchedEffect(Unit) {
            while (true) {
                delay(1000)
                now = System.currentTimeMillis()
            }
        }
        val minutesRemaining = ((scheduledStartTimeMillis - now) / 60_000).toInt().coerceAtLeast(0)
        AlertDialog(
            onDismissRequest = { startDrivingConfirm = null },
            title = { Text("Начать вождение") },
            text = {
                Text(
                    "Вы уверены начать вождение раньше? До начала вождения ещё: $minutesRemaining мин.",
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        try {
                            MediaPlayer.create(context, R.raw.zvuk_starter)?.apply {
                                setAudioAttributes(
                                    AudioAttributes.Builder()
                                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                                        .build()
                                )
                                setOnCompletionListener { release() }
                                start()
                            }
                        } catch (_: Exception) { }
                        scope.launch { drivingRepo.requestStartByInstructor(sessionId) }
                        startDrivingConfirm = null
                    },
                ) { Text("Да") }
            },
            dismissButton = {
                TextButton(onClick = { startDrivingConfirm = null }) { Text("Нет") }
            },
        )
    }
}

@Composable
private fun ProfileCard(instructor: User, balanceBadgeScale: Float = 1f) {
    val shape = RoundedCornerShape(16.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
    ) {
        Image(
            painter = painterResource(R.drawable.cadet_profile_bg),
            contentDescription = null,
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.Crop,
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(Color.White.copy(alpha = 0.35f))
        )
        Box(modifier = Modifier.matchParentSize()) {
            TopLeftCornerShimmer(
                modifier = Modifier.align(Alignment.TopStart),
                cornerSizeDp = 140.dp,
                highlightAlpha = 0.32f,
                durationMillis = 2200,
            )
        }
        AnimatedGlossOverlay(
            modifier = Modifier.matchParentSize(),
            highlightAlpha = 0.18f,
            durationMillis = 2600,
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            shape = shape,
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(28.dp),
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "Профиль инструктора",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                    )
                }
                ProfileRow(label = "ФИО:", value = instructor.fullName.ifBlank { "—" }, icon = Icons.Default.Person)
                ProfileRow(label = "Email:", value = instructor.email.ifBlank { "—" }, icon = Icons.Default.Email)
                ProfileRow(label = "Тел.:", value = instructor.phone.ifBlank { "—" }, icon = Icons.Default.Phone)
                ProfileRow(label = "Роль:", value = "Инструктор", icon = Icons.Default.Badge)
                ProfileRow(
                    label = "Баланс талонов:",
                    value = instructor.balance.toString(),
                    icon = Icons.Default.ConfirmationNumber,
                    valueFontSizeScale = 1.2f,
                    valueAsGreenBadge = true,
                    badgeScale = balanceBadgeScale,
                )
            }
        }
    }
}

@Composable
private fun ProfileRow(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    valueFontSizeScale: Float = 1f,
    valueAsGreenBadge: Boolean = false,
    badgeScale: Float = 1f,
) {
    val baseStyle = MaterialTheme.typography.bodyMedium
    val valueStyle = if (valueFontSizeScale != 1f) baseStyle.copy(
        fontSize = TextUnit(baseStyle.fontSize.value * valueFontSizeScale, TextUnitType.Sp),
        fontWeight = FontWeight.Bold,
    ) else baseStyle.copy(fontWeight = FontWeight.Bold)
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = Color.Black.copy(alpha = 0.8f),
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(8.dp))
            }
            Text(
                label,
                style = baseStyle.copy(fontWeight = FontWeight.Bold),
                color = Color.Black.copy(alpha = 0.85f),
            )
        }
        if (valueAsGreenBadge) {
            BalanceBadge(
                value = value,
                scale = badgeScale,
                valueStyle = valueStyle,
            )
        } else {
            Text(
                value,
                style = valueStyle,
                color = Color.Black,
            )
        }
    }
}

private val cadetCardLightBlue = Color(0xFFD0E3F5)
private val greenCircleButton = Color(0xFF4CAF50)
private val sectionIconBlue = Color(0xFF1976D2)

@Composable
private fun CadetCard(
    cadet: User,
    completedDrivingsCount: Int = 0,
    onChat: () -> Unit,
    onCall: () -> Unit,
) {
    val shape = RoundedCornerShape(16.dp)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, shape)
            .border(3.dp, CardOutlineWhite, shape)
            .clip(shape),
        colors = CardDefaults.cardColors(containerColor = cadetCardLightBlue),
        shape = shape,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                "Карточка курсанта:",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
            ) {
                val avatarColor = colorForContactId(cadet.id)
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .then(
                            if (!cadet.chatAvatarUrl.isNullOrBlank()) Modifier
                            else Modifier.background(avatarColor)
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    if (!cadet.chatAvatarUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = cadet.chatAvatarUrl,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                        )
                    } else {
                        Text(
                            cadet.initials(),
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                        )
                    }
                }
                Spacer(Modifier.width(16.dp))
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .widthIn(min = 0.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        cadet.fullName,
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        maxLines = 3,
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Phone,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "Телефон: ",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            cadet.phone.ifBlank { "—" },
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Black,
                            maxLines = 2,
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.DirectionsCar,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "Вождений: ",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            "$completedDrivingsCount",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Black,
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.ConfirmationNumber,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "Баланс: ",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            "${cadet.balance} талонов",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Black,
                        )
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                GreenCircleIconButton(
                    onClick = onChat,
                    icon = Icons.Default.Chat,
                    contentDescription = "Чат",
                )
                Spacer(Modifier.width(10.dp))
                GreenCircleIconButton(
                    onClick = onCall,
                    icon = Icons.Default.Phone,
                    contentDescription = "Позвонить",
                )
            }
        }
    }
}

@Composable
private fun GreenCircleIconButton(
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
) {
    Box(
        modifier = Modifier
            .size(42.dp)
            .border(2.dp, Color.White, CircleShape)
            .shadow(
                7.dp,
                CircleShape,
                spotColor = greenCircleButton.copy(alpha = 0.45f),
                ambientColor = Color.Black.copy(alpha = 0.12f),
            )
            .clip(CircleShape)
            .background(greenCircleButton)
            .glossySheen(glossAlpha = 0.22f)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            icon,
            contentDescription = contentDescription,
            tint = Color.White,
            modifier = Modifier.size(21.dp),
        )
    }
}

@Composable
private fun DrivingTimerBlock(
    session: DrivingSession,
    cadetName: String,
    drivingRepo: DrivingRepository,
    onSessionCompleted: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val startTimeMillis = session.session?.startTime ?: 0L
    val serverPausedTime = session.session?.pausedTime ?: 0L
    val serverIsActive = session.session?.isActive != false
    var now by remember { mutableStateOf(System.currentTimeMillis()) }
    var pausedAt by remember { mutableStateOf<Long?>(null) }
    var effectivePausedTime by remember(session.id) { mutableStateOf(serverPausedTime) }
    var showStopDialog by remember { mutableStateOf(false) }
    var showRatingDialog by remember { mutableStateOf(false) }
    var pendingCompletionTimerStr by remember { mutableStateOf<String?>(null) }
    var selectedRating by remember { mutableStateOf<Int?>(null) }
    var autoCompleted by remember { mutableStateOf(false) }

    if (pausedAt == null) {
        effectivePausedTime = serverPausedTime
    }

    DisposableEffect(Unit) {
        onDispose {
            if (pausedAt != null) {
                val totalPaused = effectivePausedTime + (System.currentTimeMillis() - pausedAt!!)
                val sid = session.id
                val repo = drivingRepo
                CoroutineScope(SupervisorJob() + Dispatchers.Default).launch {
                    try {
                        repo.updateSessionTimer(sid, totalPaused, false)
                    } catch (e: Exception) {
                        android.util.Log.e("DrivingTimerBlock", "Failed to save paused time", e)
                    }
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            now = System.currentTimeMillis()
        }
    }

    val currentPauseSegment = if (pausedAt != null) (now - pausedAt!!) else 0L
    val scheduledStartMillis = session.startTime?.toDate()?.time ?: 0L
    val waitingMinutes = if (session.session?.cadetConfirmed == false && scheduledStartMillis > 0L && startTimeMillis > 0L) {
        ((startTimeMillis - scheduledStartMillis) / 60_000).toInt().coerceIn(0, 90)
    } else 0
    val durationMinutes = if (session.session?.cadetConfirmed == false) (DRIVING_DURATION_MINUTES - waitingMinutes).toInt().coerceAtLeast(0) else DRIVING_DURATION_MINUTES.toInt()
    val totalMs = durationMinutes * 60 * 1000L
    val elapsed = ((now - startTimeMillis) - effectivePausedTime - currentPauseSegment).coerceIn(0, totalMs)
    val remainingMs = (totalMs - elapsed).coerceAtLeast(0)
    val minutes = (remainingMs / 60_000).toInt()
    val seconds = ((remainingMs % 60_000) / 1000).toInt()
    val timeStr = "%d:%02d".format(minutes, seconds)
    val isPaused = !serverIsActive || pausedAt != null

    LaunchedEffect(remainingMs) {
        if (remainingMs <= 0 && !autoCompleted) {
            autoCompleted = true
            pendingCompletionTimerStr = timeStr
            showRatingDialog = true
        }
    }

    if (showStopDialog) {
        AlertDialog(
            onDismissRequest = { showStopDialog = false },
            title = { Text("Завершить вождение досрочно?") },
            text = { Text("Вы уверены завершить вождение досрочно?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showStopDialog = false
                        pendingCompletionTimerStr = "%d:%02d".format(
                            (remainingMs / 60_000).toInt(),
                            ((remainingMs % 60_000) / 1000).toInt(),
                        )
                        selectedRating = null
                        showRatingDialog = true
                    },
                ) { Text("Да") }
            },
            dismissButton = {
                TextButton(onClick = { showStopDialog = false }) { Text("Нет") }
            },
        )
    }

    if (showRatingDialog && pendingCompletionTimerStr != null) {
        val ratingDialogLightBlue = Color(0xFFD0E3F5)
        val ratingButtonGreen = Color(0xFF4CAF50)
        val ratingButtonYellow = Color(0xFFFFEB3B)
        AlertDialog(
            onDismissRequest = { },
            containerColor = ratingDialogLightBlue,
            title = {
                Text(
                    "Поставьте оценку курсанту:",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                    ) {
                        listOf(3, 4, 5).forEach { value ->
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .shadow(6.dp, CircleShape)
                                    .clip(CircleShape)
                                    .border(2.dp, Color.Black, CircleShape)
                                    .background(if (selectedRating == value) ratingButtonYellow else ratingButtonGreen)
                                    .clickable { selectedRating = value },
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    "$value",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black,
                                )
                            }
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        TextButton(
                            onClick = {
                                val rating = selectedRating ?: return@TextButton
                                scope.launch {
                                    drivingRepo.completeSession(session.id, rating, 0, session.instructorId, pendingCompletionTimerStr)
                                    showRatingDialog = false
                                    pendingCompletionTimerStr = null
                                    selectedRating = null
                                    onSessionCompleted()
                                }
                            },
                            enabled = selectedRating != null,
                        ) { Text("Подтвердить") }
                    }
                }
            },
            confirmButton = { },
        )
    }

    val timerProgressBlue = Color(0xFF1976D2)
    val timerBlockShape = RoundedCornerShape(16.dp)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(3.dp, Color.White, timerBlockShape),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = timerBlockShape,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(timerBlockShape),
        ) {
            Image(
                painter = painterResource(R.drawable.timer_background),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .matchParentSize(),
                contentScale = ContentScale.Crop,
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.White.copy(alpha = 0.55f)),
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                if (isPaused) "Вождение: на паузе" else "Вождение: в процессе",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
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
                )
                if (startTimeMillis > 0L) {
                    val startDate = java.util.Date(startTimeMillis)
                    Text(
                        " (Начало: ${dateFormatShort.format(startDate)}, ${timeFormat.format(startDate)})",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            if (session.session?.cadetConfirmed == false && startTimeMillis > 0L) {
                val scheduledStartMillis = session.startTime?.toDate()?.time ?: 0L
                val waitingMs = (startTimeMillis - scheduledStartMillis).coerceAtLeast(0L)
                val waitingMin = (waitingMs / 60_000).toInt()
                val waitingSec = ((waitingMs % 60_000) / 1000).toInt()
                val waitingStr = "%d:%02d".format(waitingMin, waitingSec)
                Text(
                    "С учётом времени ожидания: $waitingStr мин.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Red,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                )
            }
            Text(
                "Осталось: $timeStr",
                style = MaterialTheme.typography.headlineSmall,
                color = if (remainingMs <= 10 * 60 * 1000) Color.Red else MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
            val progressBarColor = when {
                remainingMs >= 45 * 60 * 1000 -> Color(0xFF2E7D32)
                remainingMs <= 15 * 60 * 1000 -> Color.Red
                else -> timerProgressBlue
            }
            LinearProgressIndicator(
                progress = { (remainingMs.toFloat() / totalMs).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = progressBarColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    onClick = {
                        if (!isPaused) {
                            pausedAt = System.currentTimeMillis()
                            scope.launch {
                                drivingRepo.updateSessionTimer(session.id, effectivePausedTime, false)
                            }
                        }
                    },
                    modifier = Modifier.size(48.dp),
                ) {
                    Image(
                        painter = painterResource(if (isPaused) R.drawable.pause2 else R.drawable.pause1),
                        contentDescription = "Пауза",
                        modifier = Modifier.size(48.dp),
                        contentScale = ContentScale.Fit,
                    )
                }
                Spacer(Modifier.size(8.dp))
                IconButton(
                    onClick = {
                        if (isPaused) {
                            if (pausedAt != null) {
                                val added = System.currentTimeMillis() - pausedAt!!
                                effectivePausedTime = effectivePausedTime + added
                                pausedAt = null
                            }
                            scope.launch {
                                drivingRepo.updateSessionTimer(session.id, effectivePausedTime, true)
                            }
                        }
                    },
                    modifier = Modifier.size(48.dp),
                ) {
                    Image(
                        painter = painterResource(if (showStopDialog || isPaused) R.drawable.play1 else R.drawable.play2),
                        contentDescription = "Продолжить",
                        modifier = Modifier.size(48.dp),
                        contentScale = ContentScale.Fit,
                    )
                }
                Spacer(Modifier.size(8.dp))
                IconButton(
                    onClick = { showStopDialog = true },
                    modifier = Modifier.size(48.dp),
                ) {
                    Image(
                        painter = painterResource(if (showStopDialog) R.drawable.stop2 else R.drawable.stop1),
                        contentDescription = "Остановить",
                        modifier = Modifier.size(48.dp),
                        contentScale = ContentScale.Fit,
                    )
                }
            }
        }
        }
    }
}

@Composable
private fun ScheduleCard(
    dateStr: String,
    timeStr: String,
    cadetName: String,
    statusStr: String,
    scheduledStartTimeMillis: Long?,
    showConfirmBookingButton: Boolean = false,
    onConfirmBooking: () -> Unit = {},
    showStartButton: Boolean,
    waitingForCadetConfirm: Boolean = false,
    showCancelButton: Boolean,
    cancelEnabled: Boolean = true,
    showRunningLateButton: Boolean = false,
    onRunningLateClick: () -> Unit = {},
    onRequestStart: () -> Unit,
    onCancel: () -> Unit,
    sessionKey: Any? = null,
    onAutoStart: () -> Unit = {},
) {
    var now by remember { mutableStateOf(System.currentTimeMillis()) }
    var showTooEarlyDialog by remember { mutableStateOf(false) }
    var autoStartTriggered by remember(sessionKey) { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            now = System.currentTimeMillis()
        }
    }
    val startAllowedThreshold = scheduledStartTimeMillis?.minus(START_ALLOWED_MINUTES_BEFORE * 60 * 1000) ?: 0L
    val startButtonEnabled = showStartButton && scheduledStartTimeMillis != null && now >= startAllowedThreshold
    val autoStartAt = scheduledStartTimeMillis?.plus(AUTO_START_WAIT_MINUTES * 60 * 1000) ?: 0L
    val inAutoStartWindow = (waitingForCadetConfirm || showStartButton) && scheduledStartTimeMillis != null && now >= scheduledStartTimeMillis && now < autoStartAt
    val inCountdownWindow = inAutoStartWindow
    val remainingSeconds = if (inCountdownWindow) ((autoStartAt - now) / 1000).toInt().coerceAtLeast(0) else 0
    val countdownStrUnderButton = if ((waitingForCadetConfirm || showStartButton) && remainingSeconds > 0) "До автоматического начала: %d:%02d".format(remainingSeconds / 60, remainingSeconds % 60) else null

    LaunchedEffect(sessionKey, waitingForCadetConfirm, showStartButton, scheduledStartTimeMillis, now) {
        if (scheduledStartTimeMillis == null) return@LaunchedEffect
        if (!waitingForCadetConfirm && !showStartButton) return@LaunchedEffect
        if (autoStartTriggered) return@LaunchedEffect
        if (now >= autoStartAt) {
            autoStartTriggered = true
            onAutoStart()
        }
    }

    if (showTooEarlyDialog) {
        AlertDialog(
            onDismissRequest = { showTooEarlyDialog = false },
            title = { Text("Начать вождение") },
            text = {
                Text("Ещё рано! Кнопка активируется за 15 мин. до назначенного вождения.")
            },
            confirmButton = {
                TextButton(onClick = { showTooEarlyDialog = false }) {
                    Text("OK")
                }
            },
        )
    }

    val shape = RoundedCornerShape(14.dp)
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val compact = maxWidth < 400.dp
        val iconSize = if (compact) 28.dp else 36.dp
        val smallIconSize = if (compact) 14.dp else 18.dp
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(8.dp, shape)
                .border(3.dp, CardOutlineWhite, shape)
                .clip(shape),
            colors = CardDefaults.cardColors(containerColor = cadetCardLightBlue),
            shape = shape,
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(if (compact) 12.dp else 16.dp),
                verticalArrangement = Arrangement.spacedBy(if (compact) 8.dp else 12.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        Icons.Default.Schedule,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(iconSize),
                    )
                    Spacer(Modifier.width(if (compact) 8.dp else 12.dp))
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .widthIn(min = 0.dp),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(smallIconSize),
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "Дата: ",
                                style = if (compact) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                dateStr,
                                style = if (compact) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Schedule,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(smallIconSize),
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "Время: ",
                                style = if (compact) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                timeStr,
                                style = if (compact) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(smallIconSize),
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "Курсант: ",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                cadetName,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val statusColor = if (statusStr.contains("ожидает подтверждения")) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                            Text(
                                "Статус: ",
                                style = MaterialTheme.typography.labelSmall,
                                color = statusColor,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                statusStr,
                                style = MaterialTheme.typography.labelSmall,
                                color = statusColor,
                                maxLines = 2,
                            )
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val buttonSize = if (compact) 38.dp else 44.dp
                    val iconSizeBtn = if (compact) 20.dp else 22.dp
                    if (showConfirmBookingButton) {
                        FilledTonalIconButton(
                            onClick = onConfirmBooking,
                            modifier = Modifier
                                .size(buttonSize)
                                .border(2.dp, Color.White, CircleShape),
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = greenCircleButton,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                            ),
                        ) {
                            Icon(Icons.Default.Check, contentDescription = "Подтвердить бронь", Modifier.size(iconSizeBtn))
                        }
                    }
                    if (showConfirmBookingButton && (showStartButton || showCancelButton)) Spacer(Modifier.width(10.dp))
                    if (showStartButton) {
                        Box(modifier = Modifier.size(buttonSize)) {
                            FilledTonalIconButton(
                                onClick = onRequestStart,
                                modifier = Modifier.size(buttonSize).border(2.dp, Color.White, CircleShape),
                            enabled = startButtonEnabled,
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = if (startButtonEnabled) greenCircleButton else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (startButtonEnabled) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            ),
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Начать вождение", Modifier.size(iconSizeBtn))
                        }
                        if (!startButtonEnabled) {
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .clickable { showTooEarlyDialog = true },
                            )
                        }
                        }
                    }
                    if (showStartButton && showCancelButton) Spacer(Modifier.width(10.dp))
                    if (showCancelButton) {
                        GlossyCancelIconButton(
                            onClick = onCancel,
                            modifier = Modifier.border(2.dp, Color.White, CircleShape),
                            icon = Icons.Default.Delete,
                            contentDescription = "Отменить",
                            size = buttonSize,
                            iconSize = iconSizeBtn,
                            enabled = cancelEnabled,
                        )
                    }
                    if (showRunningLateButton) {
                        if (showCancelButton) Spacer(Modifier.width(10.dp))
                        FilledTonalIconButton(
                            onClick = onRunningLateClick,
                            modifier = Modifier
                                .size(buttonSize)
                                .border(2.dp, Color.White, CircleShape),
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = Color(0xFFFF9800),
                                contentColor = Color.White,
                            ),
                        ) {
                            Icon(Icons.Default.Schedule, contentDescription = "Опаздываю", modifier = Modifier.size(iconSizeBtn))
                        }
                    }
                }
                if (countdownStrUnderButton != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            countdownStrUnderButton,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }
    }
}
