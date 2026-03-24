package com.example.startdrive.ui.cadet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.border
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import com.example.startdrive.data.model.DrivingSession
import com.example.startdrive.data.model.User
import com.example.startdrive.data.repository.DrivingRepository
import com.example.startdrive.data.repository.UserRepository
import com.example.startdrive.ui.components.CollapsibleCard
import com.example.startdrive.ui.components.GlossyButton
import com.example.startdrive.ui.components.GlossyCancelButton
import com.example.startdrive.ui.theme.CardOutlineWhite
import java.text.SimpleDateFormat
import java.util.Locale

private val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
private val dayFormat = SimpleDateFormat("EEEE", Locale("ru"))
private val cardLightBlue = Color(0xFFD0E3F5)
private val bookButtonGreen = Color(0xFF4CAF50)

private const val CADET_CONFIRM_ACTIVE_MINUTES_BEFORE = 15L
private const val AUTO_START_WAIT_MINUTES = 5L

/** Запрет отмены курсантом с 23:00 до 09:00 по местному времени. */
private fun isCadetCancelDisabledByTime(): Boolean {
    val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    return hour >= 23 || hour < 9
}

/** true, если до начала вождения осталось меньше 6 часов — отмена запрещена. */
private fun isWithin6HoursBeforeStart(startTimeMillis: Long?): Boolean {
    if (startTimeMillis == null) return false
    val now = System.currentTimeMillis()
    if (startTimeMillis <= now) return true
    return (startTimeMillis - now) <= 6 * 60 * 60 * 1000L
}

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
fun CadetRecordingTab(cadet: User) {
    val drivingRepo = remember { DrivingRepository() }
    val userRepo = remember { UserRepository() }
    val instructorId = cadet.assignedInstructorId ?: return
    val sessions by drivingRepo.sessionsForCadet(cadet.id).collectAsState(initial = emptyList())
    val requests = sessions.filter { it.status == "scheduled" }
    val openWindows by drivingRepo.openWindowsForCadet(instructorId).collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var sessionIdToConfirmCancel by remember { mutableStateOf<String?>(null) }
    var showNightCancelBlockedDialog by remember { mutableStateOf(false) }
    var showSixHoursCancelBlockedDialog by remember { mutableStateOf(false) }
    var showZeroBalanceMessage by remember { mutableStateOf(false) }
    var showMaxBookingsMessage by remember { mutableStateOf(false) }
    var instructorName by remember { mutableStateOf<String>("") }
    val isCancelDisabledByTime = isCadetCancelDisabledByTime()

    LaunchedEffect(instructorId) {
        instructorName = userRepo.getUser(instructorId)?.fullName?.takeIf { it.isNotBlank() }?.let(::formatSurnameWithInitials) ?: "—"
    }

    val currentBookingsCount = sessions.count { it.status == "scheduled" || it.status == "inProgress" }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            CollapsibleCard(title = "Записан на вождение", count = requests.size, icon = Icons.Default.Schedule, iconTint = MaterialTheme.colorScheme.primary, containerColor = Color(0xFFD0E3F5), titleColor = MaterialTheme.colorScheme.primary, titleFontWeight = FontWeight.Bold) {
                if (requests.isEmpty()) {
                    Text(
                        "Нет записей на вождение",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp),
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        requests.forEach { s ->
                            val startMillis = s.startTime?.toDate()?.time
                            val within6Hours = isWithin6HoursBeforeStart(startMillis)
                            val cancelBlocked = isCancelDisabledByTime || within6Hours
                            BookedSessionCard(
                                session = s,
                                instructorName = instructorName,
                                showConfirmBookingButton = !s.instructorConfirmed && s.openWindowId.isEmpty(),
                                onConfirmBooking = { scope.launch { drivingRepo.confirmBookingByCadet(s.id) } },
                                showConfirmStartButton = s.startRequestedByInstructor, // только после «Начать вождение» у инструктора
                                onConfirmStart = {
                                    scope.launch {
                                        drivingRepo.startSession(s.id)
                                        drivingRepo.confirmSessionByCadet(s.id)
                                    }
                                },
                                onAutoStart = { scope.launch { drivingRepo.startSession(s.id) } },
                                onCancel = {
                                    when {
                                        isCancelDisabledByTime -> showNightCancelBlockedDialog = true
                                        within6Hours -> showSixHoursCancelBlockedDialog = true
                                        else -> sessionIdToConfirmCancel = s.id
                                    }
                                },
                                cancelDisabledAppearance = cancelBlocked,
                            )
                        }
                    }
                }
            }
        }
        item {
            CollapsibleCard(title = "Свободные окна инструктора", count = openWindows.size, icon = Icons.Default.EventAvailable, iconTint = MaterialTheme.colorScheme.primary, containerColor = Color(0xFFD0E3F5), titleColor = MaterialTheme.colorScheme.primary, titleFontWeight = FontWeight.Bold) {
                if (openWindows.isEmpty()) {
                    Text(
                        "Нет свободных окон",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp),
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        openWindows.forEach { w ->
                            val dateStr = w.dateTime?.toDate()?.let { dateFormat.format(it) + " " + timeFormat.format(it) } ?: ""
                            OpenWindowCard(
                                instructorName = instructorName,
                                dateTimeStr = dateStr,
                                onBook = {
                                    when {
                                        cadet.balance <= 0 -> showZeroBalanceMessage = true
                                        currentBookingsCount >= cadet.balance -> showMaxBookingsMessage = true
                                        else -> scope.launch { drivingRepo.bookWindow(w.id, cadet.id) }
                                    }
                                },
                            )
                        }
                    }
                }
            }
        }
    }
    sessionIdToConfirmCancel?.let { sessionId ->
        AlertDialog(
            onDismissRequest = { sessionIdToConfirmCancel = null },
            title = { Text("Отмена вождения") },
            text = { Text("Вы уверены, что хотите отменить вождение?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            try {
                                drivingRepo.cancelByCadet(sessionId)
                                sessionIdToConfirmCancel = null
                            } catch (e: IllegalStateException) {
                                Toast.makeText(
                                    context,
                                    e.message ?: "Отмена невозможна",
                                    Toast.LENGTH_LONG,
                                ).show()
                                sessionIdToConfirmCancel = null
                            }
                        }
                    },
                ) { Text("Да") }
            },
            dismissButton = {
                TextButton(onClick = { sessionIdToConfirmCancel = null }) { Text("Нет") }
            },
        )
    }
    if (showNightCancelBlockedDialog) {
        AlertDialog(
            onDismissRequest = { showNightCancelBlockedDialog = false },
            title = { Text("Отмена недоступна") },
            text = {
                Text("В период с 23:00 до 9:00 вождение отменить нельзя, свяжитесь со своим инструктором или администратором.")
            },
            confirmButton = {
                TextButton(onClick = { showNightCancelBlockedDialog = false }) { Text("OK") }
            },
        )
    }
    if (showSixHoursCancelBlockedDialog) {
        AlertDialog(
            onDismissRequest = { showSixHoursCancelBlockedDialog = false },
            title = { Text("Внимание") },
            text = {
                Text("Нельзя отменить за 6 часов до вождения. Сообщите своему инструктору или администратору.")
            },
            confirmButton = {
                TextButton(onClick = { showSixHoursCancelBlockedDialog = false }) { Text("OK") }
            },
        )
    }
    if (showZeroBalanceMessage) {
        AlertDialog(
            onDismissRequest = { showZeroBalanceMessage = false },
            title = { Text("Запись невозможна") },
            text = {
                Text(
                    "На Вашем балансе: 0 талонов, запись невозможна! Для бронирования вождения необходимо приобрести талон на вождение, обратитесь к Администратору.",
                    color = MaterialTheme.colorScheme.error,
                )
            },
            confirmButton = {
                TextButton(onClick = { showZeroBalanceMessage = false }) { Text("OK") }
            },
        )
    }
    if (showMaxBookingsMessage) {
        AlertDialog(
            onDismissRequest = { showMaxBookingsMessage = false },
            title = { Text("Достигнут лимит записей") },
            text = {
                Text(
                    "По вашему балансу (${cadet.balance} талонов) уже запланировано максимальное количество вождений. Отмените одно из вождений, чтобы забронировать новое.",
                    color = MaterialTheme.colorScheme.error,
                )
            },
            confirmButton = {
                TextButton(onClick = { showMaxBookingsMessage = false }) { Text("OK") }
            },
        )
    }
}

@Composable
private fun BookedSessionCard(
    session: DrivingSession,
    instructorName: String,
    showConfirmBookingButton: Boolean = false,
    onConfirmBooking: () -> Unit = {},
    showConfirmStartButton: Boolean = false,
    onConfirmStart: () -> Unit = {},
    onAutoStart: () -> Unit = {},
    onCancel: () -> Unit,
    cancelDisabledAppearance: Boolean = false,
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
    val cadetAlreadyConfirmed = session.status == "inProgress" || session.session?.cadetConfirmed == true
    val inCountdownWindow = scheduledStartTimeMillis != null && now >= scheduledStartTimeMillis && now < autoStartAt && !cadetAlreadyConfirmed
    val remainingSeconds = if (inCountdownWindow) ((autoStartAt - now) / 1000).toInt().coerceAtLeast(0) else 0
    val countdownStr = if (remainingSeconds > 0) "До автоматического начала: %d:%02d".format(remainingSeconds / 60, remainingSeconds % 60) else null

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

    val instructorConfirmed = session.instructorConfirmed
    val startDate = session.startTime?.toDate()
    val dayStr = startDate?.let { dayFormat.format(it).replaceFirstChar { c -> c.lowercase() } } ?: "—"
    val dateStr = startDate?.let { dateFormat.format(it) } ?: "—"
    val timeStr = startDate?.let { timeFormat.format(it) } ?: "—"
    val shape = RoundedCornerShape(14.dp)
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val compact = maxWidth < 400.dp
        val iconSize = if (compact) 28.dp else 36.dp
        val smallIconSize = if (compact) 14.dp else 18.dp
        val padding = if (compact) 10.dp else 14.dp
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .border(3.dp, CardOutlineWhite, shape),
            colors = CardDefaults.cardColors(containerColor = cardLightBlue),
            shape = shape,
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(padding),
                verticalArrangement = Arrangement.spacedBy(if (compact) 8.dp else 10.dp),
                horizontalAlignment = Alignment.Start,
            ) {
                Text(
                    "Карточка вождения:",
                    style = if (compact) MaterialTheme.typography.labelMedium else MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                ) {
                    Icon(
                        Icons.Default.Schedule,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(iconSize),
                    )
                    Spacer(Modifier.size(if (compact) 8.dp else 12.dp))
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .widthIn(min = 0.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        horizontalAlignment = Alignment.Start,
                    ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(smallIconSize),
                        )
                        Spacer(Modifier.size(6.dp))
                        Text(
                            "Инструктор: ",
                            style = if (compact) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyMedium,
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            instructorName,
                            style = if (compact) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyMedium,
                            color = Color.Black,
                            maxLines = 2,
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(smallIconSize),
                        )
                        Spacer(Modifier.size(6.dp))
                        Text(
                            "День: ",
                            style = if (compact) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            dayStr,
                            style = if (compact) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(smallIconSize),
                        )
                        Spacer(Modifier.size(6.dp))
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
                        Spacer(Modifier.size(6.dp))
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
                    if (showConfirmStartButton && !cadetAlreadyConfirmed && scheduledStartTimeMillis != null && now < confirmActiveFrom) {
                        Text(
                            "Кнопка «Подтвердить» будет доступна за 15 мин до начала вождения",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                }
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                thickness = 1.dp,
            )
            if (compact) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    if (showConfirmBookingButton) {
                        GlossyButton(
                            onClick = onConfirmBooking,
                            text = "Подтвердить запись",
                            icon = Icons.Default.Check,
                            modifier = Modifier.wrapContentWidth(),
                            usePrimaryBlue = true,
                        )
                    }
                    if (showConfirmStartButton) {
                        GlossyButton(
                            onClick = onConfirmStart,
                            text = "Подтвердить",
                            icon = Icons.Default.Check,
                            modifier = Modifier.wrapContentWidth(),
                            usePrimaryBlue = true,
                            enabled = confirmButtonEnabled,
                        )
                    }
                    GlossyCancelButton(
                        onClick = onCancel,
                        text = "Отменить",
                        icon = Icons.Default.Delete,
                        modifier = Modifier.wrapContentWidth(),
                        disabledAppearance = cancelDisabledAppearance,
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
                ) {
                    if (showConfirmBookingButton) {
                        GlossyButton(
                            onClick = onConfirmBooking,
                            text = "Подтвердить запись",
                            icon = Icons.Default.Check,
                            modifier = Modifier.wrapContentWidth(),
                            usePrimaryBlue = true,
                        )
                    }
                    if (showConfirmStartButton) {
                        GlossyButton(
                            onClick = onConfirmStart,
                            text = "Подтвердить",
                            icon = Icons.Default.Check,
                            modifier = Modifier.wrapContentWidth(),
                            usePrimaryBlue = true,
                            enabled = confirmButtonEnabled,
                        )
                    }
                    GlossyCancelButton(
                        onClick = onCancel,
                        text = "Отменить",
                        icon = Icons.Default.Delete,
                        modifier = Modifier.wrapContentWidth(),
                        disabledAppearance = cancelDisabledAppearance,
                    )
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
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                thickness = 1.dp,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val statusValue = when {
                    session.startRequestedByInstructor -> "Инструктор ожидает подтверждения начала вождения"
                    !instructorConfirmed && session.openWindowId.isNotEmpty() -> "Ожидает подтверждения инструктором"
                    !instructorConfirmed -> "Ожидает вашего подтверждения записи"
                    else -> "Подтверждён"
                }
                val statusColor = if (instructorConfirmed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    tint = statusColor,
                    modifier = Modifier.size(smallIconSize),
                )
                Spacer(Modifier.size(6.dp))
                Text(
                    text = "Статус: ",
                    style = if (compact) MaterialTheme.typography.labelSmall else MaterialTheme.typography.labelMedium,
                    color = statusColor,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = statusValue,
                    style = if (compact) MaterialTheme.typography.labelSmall else MaterialTheme.typography.labelMedium,
                    color = statusColor,
                    maxLines = 3,
                )
            }
            }
        }
    }
}

@Composable
private fun OpenWindowCard(
    instructorName: String,
    dateTimeStr: String,
    onBook: () -> Unit,
) {
    val shape = RoundedCornerShape(14.dp)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .border(3.dp, CardOutlineWhite, shape),
        colors = CardDefaults.cardColors(containerColor = cardLightBlue),
        shape = shape,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val isNarrow = maxWidth < 360.dp
            if (isNarrow) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalAlignment = Alignment.Start,
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            Icons.Default.EventAvailable,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(36.dp),
                        )
                        Spacer(Modifier.size(12.dp))
                        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.Start) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(18.dp),
                                )
                                Spacer(Modifier.size(6.dp))
                                Text(
                                    "Инструктор: ",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Black,
                                    fontWeight = FontWeight.Bold,
                                )
                                Text(
                                    instructorName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Black,
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(18.dp),
                                )
                                Spacer(Modifier.size(6.dp))
                                Text(
                                    dateTimeStr,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Medium,
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.EventAvailable,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp),
                                )
                                Spacer(Modifier.size(6.dp))
                                Text(
                                    "Статус: ",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                )
                                Text(
                                    "Свободное окно",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        GlossyButton(
                            onClick = onBook,
                            text = "Забронировать",
                            icon = Icons.Default.EventAvailable,
                            modifier = Modifier.wrapContentWidth(),
                            usePrimaryBlue = true,
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalAlignment = Alignment.Start,
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            Icons.Default.EventAvailable,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(36.dp),
                        )
                        Spacer(Modifier.size(12.dp))
                        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.Start) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(18.dp),
                                )
                                Spacer(Modifier.size(6.dp))
                                Text(
                                    "Инструктор: ",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Black,
                                    fontWeight = FontWeight.Bold,
                                )
                                Text(
                                    instructorName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Black,
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(18.dp),
                                )
                                Spacer(Modifier.size(6.dp))
                                Text(
                                    dateTimeStr,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Medium,
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.EventAvailable,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp),
                                )
                                Spacer(Modifier.size(6.dp))
                                Text(
                                    "Статус: ",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                )
                                Text(
                                    "Свободное окно",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        GlossyButton(
                            onClick = onBook,
                            text = "Забронировать",
                            icon = Icons.Default.EventAvailable,
                            modifier = Modifier.wrapContentWidth(),
                            usePrimaryBlue = true,
                        )
                    }
                }
            }
        }
    }
}
