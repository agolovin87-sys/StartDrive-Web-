package com.example.startdrive.ui.cadet

import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import com.example.startdrive.R
import com.example.startdrive.data.model.DrivingSession
import com.example.startdrive.data.model.User
import com.example.startdrive.data.repository.DrivingRepository
import com.example.startdrive.data.repository.UserRepository
import com.example.startdrive.ui.components.CollapsibleCard
import com.example.startdrive.ui.components.CollapsibleSection
import com.example.startdrive.ui.components.GlossyButton
import com.example.startdrive.ui.components.AnimatedGlossOverlay
import com.example.startdrive.ui.components.glossySheen
import com.example.startdrive.ui.theme.CardOutlineWhite
import com.example.startdrive.ui.components.TopLeftCornerShimmer
import com.example.startdrive.ui.components.BalanceBadge
import coil.compose.AsyncImage
import com.example.startdrive.ui.chat.colorForContactId
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private val dateFormatShort = SimpleDateFormat("dd.MM.yy", Locale.getDefault())

/** Кнопка «Подтвердить» у курсанта активна за это количество минут до назначенного времени. */
private const val CADET_CONFIRM_ACTIVE_MINUTES_BEFORE = 15L
/** От назначенного времени даётся столько минут на подтверждение; затем вождение стартует автоматически. */
private const val AUTO_START_WAIT_MINUTES = 5L

@Composable
fun CadetProfileTab(
    cadet: User,
    completedDrivingsCount: Int = 0,
    balanceBadgeScale: Float = 1f,
    onSettingsClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
) {
    val drivingRepo = remember { DrivingRepository() }
    val sessions by drivingRepo.sessionsForCadet(cadet.id).collectAsState(initial = emptyList())
    val completed = remember(sessions) { sessions.count { it.status == "completed" } }
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            CadetProfileCard(cadet = cadet, completedDrivingsCount = completed, balanceBadgeScale = balanceBadgeScale)
        }
        item {
            CadetProgressPieChart(completedDrivingsCount = completed)
        }
        item {
            CadetInstructorRatingsHistogram(sessions = sessions)
        }
        item {
            CadetWeeklyFrequencyHistogram(sessions = sessions)
        }
        item {
            CadetCancelledSessionsChart(sessions = sessions)
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

private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
private val sectionIconBlue = Color(0xFF1976D2)
private val instructorCardLightBlue = Color(0xFFD0E3F5)
private val greenCircleButton = Color(0xFF4CAF50)

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
fun CadetHomeTab(
    cadet: User,
    onOpenChatWith: (String) -> Unit = {},
    triggerBalancePulse: Boolean = false,
    onBalancePulseDone: () -> Unit = {},
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val userRepo = remember { UserRepository() }
    val balanceScale = remember { androidx.compose.animation.core.Animatable(1f) }
    androidx.compose.runtime.LaunchedEffect(triggerBalancePulse) {
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
    val drivingRepo = remember { DrivingRepository() }
    val scope = rememberCoroutineScope()
    var profileCadet by remember(cadet.id) { mutableStateOf(cadet) }
    var instructor by remember { mutableStateOf<User?>(null) }
    val sessions by drivingRepo.sessionsForCadet(cadet.id).collectAsState(initial = emptyList())
    val completedDrivingsCount = remember(sessions) { sessions.count { it.status == "completed" } }
    val upcomingSessions = remember(sessions) {
        sessions.filter { it.status == "scheduled" || it.status == "inProgress" }
            .sortedBy { it.startTime?.toDate()?.time ?: 0L }
    }
    val ctx = context
    LaunchedEffect(cadet.id) {
        profileCadet = userRepo.getUser(cadet.id) ?: cadet
        cadet.assignedInstructorId?.let { id -> instructor = userRepo.getUser(id) }
    }
    LaunchedEffect(cadet) {
        profileCadet = cadet
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            CadetProfileCard(cadet = profileCadet, completedDrivingsCount = completedDrivingsCount, balanceBadgeScale = balanceScale.value)
        }
        item {
            instructor?.let { inst ->
                CollapsibleCard(
                    title = "Мой инструктор",
                    count = 1,
                    icon = Icons.Default.Person,
                    iconTint = sectionIconBlue,
                    containerColor = Color(0xFFDDD5E8),
                    titleColor = sectionIconBlue,
                    titleFontWeight = FontWeight.Bold,
                    modifier = Modifier.glossySheen(glossAlpha = 0.22f),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        InstructorCardInStyle(
                            instructor = inst,
                            onChat = { onOpenChatWith(inst.id) },
                            onCall = {
                                ctx.startActivity(Intent(Intent.ACTION_DIAL).setData(Uri.parse("tel:${inst.phone}")))
                            },
                        )
                    }
                }
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
            CollapsibleCard(
                title = "Мое вождение",
                count = upcomingSessions.size,
                icon = Icons.Default.Schedule,
                iconTint = sectionIconBlue,
                containerColor = Color(0xFFDDD5E8),
                titleColor = sectionIconBlue,
                titleFontWeight = FontWeight.Bold,
                defaultExpanded = true,
                modifier = Modifier.glossySheen(glossAlpha = 0.22f),
            ) {
                if (upcomingSessions.isEmpty()) {
                    Text(
                        "Нет запланированных занятий",
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
                                defaultExpanded = daySessions.isNotEmpty(),
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
                                        daySessions.forEach { session ->
                                            CadetScheduleCard(
                                                session = session,
                                                instructorName = instructor?.fullName?.takeIf { it.isNotBlank() }?.let(::formatSurnameWithInitials) ?: "—",
                                                onConfirmBooking = { scope.launch { drivingRepo.confirmBookingByCadet(session.id) } },
                                                onConfirmStart = {
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
                                                    scope.launch {
                                                        drivingRepo.startSession(session.id)
                                                        drivingRepo.confirmSessionByCadet(session.id)
                                                    }
                                                },
                                                onAutoStart = { scope.launch { drivingRepo.startSession(session.id) } },
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
}

private val cadetCardLightBlue = Color(0xFFD0E3F5)

@Composable
private fun CadetScheduleCard(
    session: DrivingSession,
    instructorName: String,
    onConfirmBooking: () -> Unit,
    onConfirmStart: () -> Unit,
    onAutoStart: () -> Unit = {},
) {
    var now by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            now = System.currentTimeMillis()
        }
    }
    val scheduledStartTimeMillis = session.startTime?.toDate()?.time
    val confirmActiveFrom = scheduledStartTimeMillis?.minus(CADET_CONFIRM_ACTIVE_MINUTES_BEFORE * 60 * 1000) ?: 0L
    val autoStartAt = scheduledStartTimeMillis?.plus(AUTO_START_WAIT_MINUTES * 60 * 1000) ?: 0L
    val confirmButtonEnabled = scheduledStartTimeMillis != null && now >= confirmActiveFrom
    val inCountdownWindow = scheduledStartTimeMillis != null && now >= scheduledStartTimeMillis && now < autoStartAt
    val remainingSeconds = if (inCountdownWindow) ((autoStartAt - now) / 1000).toInt().coerceAtLeast(0) else 0
    val countdownStr = if (remainingSeconds > 0) {
        val m = remainingSeconds / 60
        val s = remainingSeconds % 60
        "До автоматического начала: %d:%02d".format(m, s)
    } else null

    var autoStartTriggered by remember(session.id) { mutableStateOf(false) }
    val canAutoStart = session.status == "scheduled" && session.instructorConfirmed &&
        (session.startRequestedByInstructor && session.session?.cadetConfirmed != true || !session.startRequestedByInstructor)
    LaunchedEffect(session.id, session.status, session.startRequestedByInstructor, session.instructorConfirmed, session.session?.cadetConfirmed, scheduledStartTimeMillis, now) {
        if (scheduledStartTimeMillis == null) return@LaunchedEffect
        if (!canAutoStart) return@LaunchedEffect
        if (autoStartTriggered) return@LaunchedEffect
        if (now >= autoStartAt) {
            autoStartTriggered = true
            onAutoStart()
        }
    }

    val dateStr = session.startTime?.toDate()?.let { dateFormatShort.format(it) } ?: ""
    val timeStr = session.startTime?.toDate()?.let { timeFormat.format(it) } ?: ""
    val statusStr = when {
        session.status == "inProgress" -> "в процессе"
        session.startRequestedByInstructor -> "Инструктор ожидает подтверждения начала вождения"
        session.status == "scheduled" && session.instructorConfirmed -> "подтверждён"
        session.status == "scheduled" && session.openWindowId.isNotEmpty() -> "Ожидает подтверждения инструктором"
        session.status == "scheduled" -> "Ожидает вашего подтверждения"
        else -> session.status
    }
    val showConfirmBookingButton = !session.instructorConfirmed && session.openWindowId.isEmpty()
    /** Кнопка «Подтвердить» (начало вождения) только после нажатия инструктором «Начать вождение». */
    val showConfirmStartButton = session.startRequestedByInstructor
    val cadetAlreadyConfirmed = session.status == "inProgress" || session.session?.cadetConfirmed == true
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
                        modifier = Modifier.weight(1f).widthIn(min = 0.dp),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(smallIconSize))
                            Spacer(Modifier.width(6.dp))
                            Text("Дата: ", style = if (compact) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                            Text(dateStr, style = if (compact) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Schedule, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(smallIconSize))
                            Spacer(Modifier.width(6.dp))
                            Text("Время: ", style = if (compact) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                            Text(timeStr, style = if (compact) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(smallIconSize))
                            Spacer(Modifier.width(6.dp))
                            Text("Инструктор: ", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                            Text(instructorName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val statusColor = if (statusStr.contains("ожидает подтверждения") || statusStr.contains("Ожидает")) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                            Text("Статус: ", style = MaterialTheme.typography.labelSmall, color = statusColor, fontWeight = FontWeight.Bold)
                            Text(statusStr, style = MaterialTheme.typography.labelSmall, color = statusColor, maxLines = 2)
                        }
                        if (showConfirmStartButton && !cadetAlreadyConfirmed && scheduledStartTimeMillis != null && now < confirmActiveFrom) {
                            Text(
                                "Кнопка «Подтвердить» будет доступна за 15 мин до начала вождения",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                        GlossyButton(
                            onClick = onConfirmBooking,
                            text = "Подтвердить запись",
                            icon = Icons.Default.Check,
                            modifier = Modifier.wrapContentWidth(),
                            usePrimaryBlue = true,
                        )
                    }
                    if (showConfirmBookingButton && showConfirmStartButton) Spacer(Modifier.width(10.dp))
                    if (showConfirmStartButton) {
                        FilledTonalIconButton(
                            onClick = if (cadetAlreadyConfirmed) { {} } else onConfirmStart,
                            enabled = !cadetAlreadyConfirmed && confirmButtonEnabled,
                            modifier = Modifier.size(buttonSize).border(2.dp, Color.White, CircleShape),
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = when {
                                    cadetAlreadyConfirmed -> MaterialTheme.colorScheme.surfaceVariant
                                    confirmButtonEnabled -> greenCircleButton
                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                },
                                contentColor = when {
                                    cadetAlreadyConfirmed -> MaterialTheme.colorScheme.onSurfaceVariant
                                    confirmButtonEnabled -> MaterialTheme.colorScheme.onPrimary
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                },
                            ),
                        ) {
                            Icon(
                                imageVector = if (cadetAlreadyConfirmed) Icons.Default.Check else Icons.Default.PlayArrow,
                                contentDescription = if (cadetAlreadyConfirmed) "Подтверждено" else "Подтвердить",
                                Modifier.size(iconSizeBtn),
                            )
                        }
                    }
                }
                if (countdownStr != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            countdownStr,
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

@Composable
private fun CadetProfileCard(cadet: User, completedDrivingsCount: Int = 0, balanceBadgeScale: Float = 1f) {
    val shape = RoundedCornerShape(16.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape),
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
                .background(Color.White.copy(alpha = 0.35f)),
        )
        Box(modifier = Modifier.matchParentSize()) {
            TopLeftCornerShimmer(
                modifier = Modifier.align(Alignment.TopStart),
                cornerSizeDp = 140.dp,
                highlightAlpha = 0.24f,
                durationMillis = 2400,
            )
        }
        AnimatedGlossOverlay(
            modifier = Modifier.matchParentSize(),
            highlightAlpha = 0.12f,
            durationMillis = 2800,
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
                        "Профиль курсанта:",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                    )
                }
                ProfileRow(label = "ФИО:", value = cadet.fullName.ifBlank { "—" }, icon = Icons.Default.Person)
                ProfileRow(label = "Email:", value = cadet.email.ifBlank { "—" }, icon = Icons.Default.Email)
                ProfileRow(label = "Тел.:", value = cadet.phone.ifBlank { "—" }, icon = Icons.Default.Phone)
                ProfileRow(label = "Роль:", value = "Курсант", icon = Icons.Default.Badge)
                ProfileRow(
                    label = "Вождений:",
                    value = "${completedDrivingsCount} (${getCadetRoleName(completedDrivingsCount)})",
                    icon = Icons.Default.DirectionsCar,
                )
                ProfileRow(
                    label = "Баланс талонов:",
                    value = cadet.balance.toString(),
                    icon = Icons.Default.ConfirmationNumber,
                    valueFontSizeScale = 1.2f,
                    valueAsGreenBadge = true,
                    badgeScale = balanceBadgeScale,
                )
            }
        }
    }
}

private val CAD_ROLES_THRESHOLDS = listOf(8, 16, 24, 30) // Новичок 0-7, Любитель 8-15, Профи 16-23, Эксперт 24-30
private val CAD_ROLES_NAMES = listOf("Новичок", "Любитель", "Профи", "Эксперт")

private fun getCadetRoleName(completedDrivingsCount: Int): String {
    for (i in CAD_ROLES_NAMES.indices) {
        if (completedDrivingsCount < CAD_ROLES_THRESHOLDS[i]) return CAD_ROLES_NAMES[i]
    }
    return CAD_ROLES_NAMES.last()
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

@Composable
private fun InstructorCardInStyle(
    instructor: User,
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
        colors = CardDefaults.cardColors(containerColor = instructorCardLightBlue),
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
                "Карточка инструктора:",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
            ) {
                val avatarColor = colorForContactId(instructor.id)
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .then(
                            if (!instructor.chatAvatarUrl.isNullOrBlank()) Modifier
                            else Modifier.background(avatarColor)
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    if (!instructor.chatAvatarUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = instructor.chatAvatarUrl,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                        )
                    } else {
                        Text(
                            instructor.initials(),
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
                        instructor.fullName.ifBlank { "—" },
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
                            instructor.phone.ifBlank { "—" },
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Black,
                            maxLines = 2,
                        )
                    }
                }
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
                    "Учебное ТС: ",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    instructor.trainingVehicle?.takeIf { it.isNotBlank() } ?: "—",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Black,
                    maxLines = 2,
                )
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
