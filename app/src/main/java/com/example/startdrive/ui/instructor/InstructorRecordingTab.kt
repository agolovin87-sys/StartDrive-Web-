package com.example.startdrive.ui.instructor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.border
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.startdrive.data.model.DrivingSession
import com.example.startdrive.data.model.InstructorOpenWindow
import com.example.startdrive.data.model.User
import com.example.startdrive.data.repository.DrivingRepository
import com.example.startdrive.data.repository.UserRepository
import com.example.startdrive.ui.components.GlossyButton
import com.example.startdrive.ui.components.CollapsibleCard
import com.example.startdrive.ui.components.CancelRedLight
import com.example.startdrive.ui.components.glossySheen
import com.example.startdrive.ui.theme.CardOutlineWhite
import com.example.startdrive.ui.theme.LocalScreenDimensions
import com.google.firebase.Timestamp
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
private val dayOfWeekFormat = SimpleDateFormat("EEEE", Locale("ru"))
private val dateOnlyFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
private val timeOnlyFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

/** Возвращает имя для отображения: убирает битые символы и подставляет fallback при пустом/некорректном имени. */
private fun safeDisplayName(fullName: String?, fallback: String): String {
    if (fullName.isNullOrBlank()) return fallback
    val trimmed = fullName.trim()
    if (trimmed.isEmpty()) return fallback
    val valid = trimmed.filter { c -> c.isLetterOrDigit() || c.isWhitespace() || c in ".-'" }.trim()
    return if (valid.length < 2) fallback else valid
}

/** Формат: Фамилия И.О. (первое слово — фамилия, остальные — инициалы). */
private fun formatSurnameWithInitials(fullName: String): String {
    if (fullName.isBlank()) return fullName
    val parts = fullName.trim().split(Regex("\\s+"))
    return when {
        parts.size >= 2 -> parts[0] + " " + parts.drop(1).map { it.firstOrNull()?.uppercaseChar()?.let { "$it." } ?: "" }.joinToString("")
        parts.size == 1 -> parts[0]
        else -> fullName
    }
}

private const val LESSON_DURATION_MINUTES = 90L
private const val LESSON_DURATION_MS = LESSON_DURATION_MINUTES * 60 * 1000

/** Сообщение для диалога «Время занято», когда слот уже есть в списке окон. */
private const val OCCUPIED_BY_WINDOW_MESSAGE = "\u0000WINDOW"

/** Проверяет, попадает ли выбранное время в уже занятый слот (забронированное/записанное вождение). Возвращает Фамилия И.О. курсанта при конфликте или null. */
private fun findOccupiedCadetName(
    selectedTimeMs: Long,
    sessions: List<DrivingSession>,
    cadetMap: Map<String, User>,
): String? {
    val selectedCal = Calendar.getInstance().apply { timeInMillis = selectedTimeMs }
    for (s in sessions) {
        if (s.status != "scheduled" && s.status != "inProgress") continue
        val startMs = s.startTime?.toDate()?.time ?: continue
        val startCal = Calendar.getInstance().apply { timeInMillis = startMs }
        if (selectedCal.get(Calendar.YEAR) != startCal.get(Calendar.YEAR) ||
            selectedCal.get(Calendar.DAY_OF_YEAR) != startCal.get(Calendar.DAY_OF_YEAR)
        ) continue
        val endMs = startMs + LESSON_DURATION_MS
        if (selectedTimeMs >= startMs && selectedTimeMs < endMs) {
            val fullName = cadetMap[s.cadetId]?.fullName?.takeIf { it.isNotBlank() }
            return fullName?.let { formatSurnameWithInitials(safeDisplayName(it, it)) } ?: "Курсант (${s.cadetId.take(8)})"
        }
    }
    return null
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstructorRecordingTab(instructor: User) {
    val dims = LocalScreenDimensions.current
    val userRepo = remember { UserRepository() }
    val drivingRepo = remember { DrivingRepository() }
    val sessions by drivingRepo.sessionsForInstructor(instructor.id).collectAsState(initial = emptyList())
    val openWindows by drivingRepo.openWindowsForInstructor(instructor.id).collectAsState(initial = emptyList())
    // ФИО курсантов — только из Firestore коллекции users (не из driving_sessions)
    var cadetMap by remember { mutableStateOf<Map<String, User>>(emptyMap()) }
    LaunchedEffect(sessions) {
        val cadetIds = sessions.map { it.cadetId }.distinct()
        cadetMap = cadetIds.associateWith { id -> userRepo.getUser(id) }.filterValues { it != null }.mapValues { it.value!! }
    }
    var selectedCadetId by remember { mutableStateOf<String?>(null) }
    var pickDate by remember { mutableStateOf<Date?>(null) }
    var pickTime by remember { mutableStateOf<Date?>(null) }
    var cadets by remember { mutableStateOf<List<User>>(emptyList()) }
    LaunchedEffect(instructor.id) {
        val list = mutableListOf<User>()
        for (id in instructor.assignedCadets) {
            userRepo.getUser(id)?.let { list.add(it) }
        }
        cadets = list
    }
    val scope = rememberCoroutineScope()
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showDatePickerAddWindow by remember { mutableStateOf(false) }
    var showTimePickerAddWindow by remember { mutableStateOf(false) }
    var sessionIdToConfirmCancel by remember { mutableStateOf<String?>(null) }
    var showCadetZeroBalanceMessage by remember { mutableStateOf(false) }
    var showCadetMaxBookingsMessage by remember { mutableStateOf(false) }
    var showTimeOccupiedDialog by remember { mutableStateOf<String?>(null) }

    val scheduledSessions = remember(sessions) { sessions.filter { it.status == "scheduled" } }
    var pendingOpenWindows by remember { mutableStateOf<List<InstructorOpenWindow>>(emptyList()) }
    LaunchedEffect(openWindows) {
        pendingOpenWindows = pendingOpenWindows.filter { p ->
            val pTime = p.dateTime?.toDate()?.time ?: 0L
            !openWindows.any { o -> o.instructorId == p.instructorId && (o.dateTime?.toDate()?.time ?: 0L) == pTime }
        }
    }
    // Только свободные окна: после отмены курсантом слот снова попадает сюда
    val displayOpenWindows = remember(openWindows, pendingOpenWindows) {
        val freeFromServer = openWindows.filter { it.status == "free" }
        val pendingFiltered = pendingOpenWindows.filter { p ->
            val pTime = p.dateTime?.toDate()?.time ?: 0L
            !openWindows.any { o -> o.instructorId == p.instructorId && (o.dateTime?.toDate()?.time ?: 0L) == pTime }
        }
        (freeFromServer + pendingFiltered).sortedBy { it.dateTime?.toDate()?.time ?: 0L }
    }
    val onCancelSession = remember(scope, drivingRepo) { { id: String ->
        scope.launch { drivingRepo.cancelByInstructor(id) }
        Unit
    } }
    val onDeleteWindow = remember(scope, drivingRepo) { { id: String ->
        scope.launch { drivingRepo.deleteOpenWindow(id) }
        Unit
    } }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(dims.verticalPadding),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item(key = "h-assigned") {
            CollapsibleCard(title = "Назначенное вождение", count = scheduledSessions.size, icon = Icons.Default.Schedule, iconTint = MaterialTheme.colorScheme.primary, containerColor = Color(0xFFD0E3F5), titleColor = MaterialTheme.colorScheme.primary, titleFontWeight = FontWeight.Bold) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    for (s in scheduledSessions) {
                        key(s.id) {
                            AssignedWindowCard(
                                session = s,
                                cadetName = cadetMap[s.cadetId]?.fullName?.takeIf { it.isNotBlank() }?.let { formatSurnameWithInitials(safeDisplayName(it, it)) } ?: safeDisplayName(cadetMap[s.cadetId]?.fullName, "Курсант (${s.cadetId.take(8)})"),
                                onConfirmClick = { scope.launch { drivingRepo.confirmBookingByInstructor(s.id) } },
                                onCancelClick = { sessionIdToConfirmCancel = s.id },
                            )
                        }
                    }
                }
            }
        }
        item(key = "h-free-windows") {
            val onDeleteWindowOrPending: (String) -> Unit = { id ->
                if (id.startsWith("pending_")) {
                    pendingOpenWindows = pendingOpenWindows.filter { it.id != id }
                } else {
                    onDeleteWindow(id)
                }
            }
            CollapsibleCard(title = "Свободные окна", count = displayOpenWindows.size, icon = Icons.Default.EventAvailable, iconTint = MaterialTheme.colorScheme.primary, containerColor = Color(0xFFD0E3F5), titleColor = MaterialTheme.colorScheme.primary, titleFontWeight = FontWeight.Bold) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    for (w in displayOpenWindows) {
                        key(w.id) {
                            OpenWindowCard(
                                windowId = w.id,
                                dateTimeStr = w.dateTime?.toDate()?.let { dateFormat.format(it) } ?: "",
                                onDelete = onDeleteWindowOrPending,
                            )
                        }
                    }
                }
            }
        }
        item {
            CollapsibleCard(title = "Записать на вождение", icon = Icons.Default.PersonAdd, iconTint = MaterialTheme.colorScheme.primary, containerColor = Color(0xFFDDD5E8), titleColor = MaterialTheme.colorScheme.primary, titleFontWeight = FontWeight.Bold, modifier = Modifier.border(3.dp, CardOutlineWhite, RoundedCornerShape(14.dp))) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    var cadetDropdownExpanded by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.fillMaxWidth()) {
                        GlossyButton(
                            onClick = { cadetDropdownExpanded = true },
                            text = selectedCadetId?.let { id ->
                                val fn = cadets.find { it.id == id }?.fullName ?: return@let "Курсант"
                                formatSurnameWithInitials(safeDisplayName(fn, fn))
                            } ?: "Выберите курсанта",
                            icon = Icons.Default.Person,
                            usePrimaryBlue = true,
                            trailingIcon = Icons.Default.ExpandMore,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        DropdownMenu(
                            expanded = cadetDropdownExpanded,
                            onDismissRequest = { cadetDropdownExpanded = false },
                        ) {
                            for (c in cadets) {
                                DropdownMenuItem(
                                    text = { Text(formatSurnameWithInitials(safeDisplayName(c.fullName, "Курсант"))) },
                                    onClick = {
                                        selectedCadetId = c.id
                                        cadetDropdownExpanded = false
                                    },
                                )
                            }
                        }
                    }
                    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                        val isNarrow = maxWidth < 360.dp
                        if (isNarrow) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                GlossyButton(
                                    onClick = { showDatePicker = true },
                                    text = pickDate?.let { SimpleDateFormat("dd.MM.yy", Locale.getDefault()).format(it) } ?: "Дата",
                                    icon = Icons.Default.CalendarMonth,
                                    usePrimaryBlue = true,
                                    modifier = Modifier.fillMaxWidth(),
                                )
                                GlossyButton(
                                    onClick = { showTimePicker = true },
                                    text = pickTime?.let { SimpleDateFormat("HH:mm", Locale.getDefault()).format(it) } ?: "Время",
                                    icon = Icons.Default.Schedule,
                                    usePrimaryBlue = true,
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                GlossyButton(
                                    onClick = { showDatePicker = true },
                                    text = pickDate?.let { SimpleDateFormat("dd.MM.yy", Locale.getDefault()).format(it) } ?: "Дата",
                                    icon = Icons.Default.CalendarMonth,
                                    usePrimaryBlue = true,
                                    modifier = Modifier.weight(1f),
                                )
                                GlossyButton(
                                    onClick = { showTimePicker = true },
                                    text = pickTime?.let { SimpleDateFormat("HH:mm", Locale.getDefault()).format(it) } ?: "Время",
                                    icon = Icons.Default.Schedule,
                                    usePrimaryBlue = true,
                                    modifier = Modifier.weight(1f),
                                )
                            }
                        }
                    }
                    val dateTime = if (pickDate != null && pickTime != null) {
                        val c = Calendar.getInstance().apply { time = pickTime!! }
                        val d = Calendar.getInstance().apply { time = pickDate!! }
                        d.set(Calendar.HOUR_OF_DAY, c.get(Calendar.HOUR_OF_DAY))
                        d.set(Calendar.MINUTE, c.get(Calendar.MINUTE))
                        Timestamp(d.time)
                    } else null
                    GlossyButton(
                        onClick = {
                            val cadetId = selectedCadetId ?: return@GlossyButton
                            val selectedCadet = cadets.find { it.id == cadetId }
                            if (selectedCadet == null) return@GlossyButton
                            val cadetBookingsCount = sessions.count { it.cadetId == cadetId && (it.status == "scheduled" || it.status == "inProgress") }
                            when {
                                selectedCadet.balance <= 0 -> showCadetZeroBalanceMessage = true
                                cadetBookingsCount >= selectedCadet.balance -> showCadetMaxBookingsMessage = true
                                else -> {
                                    val dt = dateTime ?: Timestamp.now()
                                    val occupiedCadet = findOccupiedCadetName(dt.toDate().time, sessions, cadetMap)
                                    if (occupiedCadet != null) {
                                        showTimeOccupiedDialog = occupiedCadet
                                    } else {
                                        scope.launch { drivingRepo.createSession(instructor.id, cadetId, dt) }
                                    }
                                }
                            }
                        },
                        text = "Записать",
                        icon = Icons.Default.Check,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = selectedCadetId != null && dateTime != null,
                        usePrimaryBlue = true,
                    )
                }
            }
        }
        item {
            CollapsibleCard(title = "Добавить окно", icon = Icons.Default.Add, iconTint = MaterialTheme.colorScheme.primary, containerColor = Color(0xFFDDD5E8), titleColor = MaterialTheme.colorScheme.primary, titleFontWeight = FontWeight.Bold, modifier = Modifier.border(3.dp, CardOutlineWhite, RoundedCornerShape(14.dp))) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                        val isNarrow = maxWidth < 360.dp
                        if (isNarrow) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                GlossyButton(
                                    onClick = { showDatePickerAddWindow = true },
                                    text = pickDate?.let { SimpleDateFormat("dd.MM.yy", Locale.getDefault()).format(it) } ?: "Дата",
                                    icon = Icons.Default.CalendarMonth,
                                    modifier = Modifier.fillMaxWidth(),
                                    usePrimaryBlue = true,
                                )
                                GlossyButton(
                                    onClick = { showTimePickerAddWindow = true },
                                    text = pickTime?.let { SimpleDateFormat("HH:mm", Locale.getDefault()).format(it) } ?: "Время",
                                    icon = Icons.Default.Schedule,
                                    modifier = Modifier.fillMaxWidth(),
                                    usePrimaryBlue = true,
                                )
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                GlossyButton(
                                    onClick = { showDatePickerAddWindow = true },
                                    text = pickDate?.let { SimpleDateFormat("dd.MM.yy", Locale.getDefault()).format(it) } ?: "Дата",
                                    icon = Icons.Default.CalendarMonth,
                                    modifier = Modifier.weight(1f),
                                    usePrimaryBlue = true,
                                )
                                GlossyButton(
                                    onClick = { showTimePickerAddWindow = true },
                                    text = pickTime?.let { SimpleDateFormat("HH:mm", Locale.getDefault()).format(it) } ?: "Время",
                                    icon = Icons.Default.Schedule,
                                    modifier = Modifier.weight(1f),
                                    usePrimaryBlue = true,
                                )
                            }
                        }
                    }
                    val dateTimeAdd = if (pickDate != null && pickTime != null) {
                        val c = Calendar.getInstance().apply { time = pickTime!! }
                        val d = Calendar.getInstance().apply { time = pickDate!! }
                        d.set(Calendar.HOUR_OF_DAY, c.get(Calendar.HOUR_OF_DAY))
                        d.set(Calendar.MINUTE, c.get(Calendar.MINUTE))
                        Timestamp(d.time)
                    } else null
                    GlossyButton(
                        onClick = {
                            val dt = dateTimeAdd ?: Timestamp.now()
                            val occupiedCadet = findOccupiedCadetName(dt.toDate().time, sessions, cadetMap)
                            if (occupiedCadet != null) {
                                showTimeOccupiedDialog = occupiedCadet
                            } else {
                                val pending = InstructorOpenWindow(
                                    id = "pending_${System.currentTimeMillis()}",
                                    instructorId = instructor.id,
                                    dateTime = dt,
                                    status = "free",
                                )
                                pendingOpenWindows = pendingOpenWindows + pending
                                scope.launch {
                                    drivingRepo.addOpenWindow(instructor.id, dt)
                                }
                            }
                        },
                        text = "Подтвердить",
                        icon = Icons.Default.Check,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = dateTimeAdd != null,
                        usePrimaryBlue = true,
                    )
                }
            }
        }
    }

    if (showDatePicker) {
        DatePickerDialogFutureOnly(
            initialDate = pickDate ?: Date(),
            onDismiss = { showDatePicker = false },
            onConfirm = { date ->
                pickDate = date
                showDatePicker = false
            },
        )
    }
    if (showTimePicker) {
        TimePickerDialogSimple(
            initialTime = pickTime ?: Date(),
            onDismiss = { showTimePicker = false },
            onConfirm = { time ->
                val dateForValidation = pickDate ?: Date()
                val cal = Calendar.getInstance().apply { this.time = dateForValidation }
                val timeCal = Calendar.getInstance().apply { this.time = time }
                cal.set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY))
                cal.set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE))
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val fullMs = cal.timeInMillis
                val occupiedCadet = findOccupiedCadetName(fullMs, sessions, cadetMap)
                if (occupiedCadet != null) {
                    showTimeOccupiedDialog = occupiedCadet
                } else {
                    pickTime = time
                }
                showTimePicker = false
            },
        )
    }
    if (showDatePickerAddWindow) {
        DatePickerDialogFutureOnly(
            initialDate = pickDate ?: Date(),
            onDismiss = { showDatePickerAddWindow = false },
            onConfirm = { date ->
                pickDate = date
                showDatePickerAddWindow = false
            },
        )
    }
    if (showTimePickerAddWindow) {
        TimePickerDialogSimple(
            initialTime = pickTime ?: Date(),
            onDismiss = { showTimePickerAddWindow = false },
            onConfirm = { time ->
                val dateForValidation = pickDate ?: Date()
                val cal = Calendar.getInstance().apply { this.time = dateForValidation }
                val timeCal = Calendar.getInstance().apply { this.time = time }
                cal.set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY))
                cal.set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE))
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val fullMs = cal.timeInMillis
                val occupiedCadet = findOccupiedCadetName(fullMs, sessions, cadetMap)
                val windowAtSameTime = (openWindows + pendingOpenWindows).any { w ->
                    val wMs = w.dateTime?.toDate()?.time ?: return@any false
                    val wCal = Calendar.getInstance().apply { timeInMillis = wMs }
                    cal.get(Calendar.YEAR) == wCal.get(Calendar.YEAR) &&
                        cal.get(Calendar.DAY_OF_YEAR) == wCal.get(Calendar.DAY_OF_YEAR) &&
                        cal.get(Calendar.HOUR_OF_DAY) == wCal.get(Calendar.HOUR_OF_DAY) &&
                        cal.get(Calendar.MINUTE) == wCal.get(Calendar.MINUTE)
                }
                when {
                    occupiedCadet != null -> showTimeOccupiedDialog = occupiedCadet
                    windowAtSameTime -> showTimeOccupiedDialog = OCCUPIED_BY_WINDOW_MESSAGE
                    else -> pickTime = time
                }
                showTimePickerAddWindow = false
            },
        )
    }
    sessionIdToConfirmCancel?.let { sessionId ->
        AlertDialog(
            onDismissRequest = { sessionIdToConfirmCancel = null },
            title = { Text("Отмена вождения") },
            text = { Text("Вы уверены, что хотите отменить вождение?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onCancelSession(sessionId)
                        sessionIdToConfirmCancel = null
                    },
                ) { Text("Да") }
            },
            dismissButton = {
                TextButton(onClick = { sessionIdToConfirmCancel = null }) { Text("Нет") }
            },
        )
    }
    if (showCadetZeroBalanceMessage) {
        val selectedCadet = selectedCadetId?.let { id -> cadets.find { it.id == id } }
        val cadetShortName = selectedCadet?.fullName?.takeIf { it.isNotBlank() }?.let { formatSurnameWithInitials(safeDisplayName(it, it)) } ?: "Курсант"
        AlertDialog(
            onDismissRequest = { showCadetZeroBalanceMessage = false },
            title = { Text("Запись невозможна") },
            text = {
                Text(
                    "На балансе у курсанта: $cadetShortName — 0 талонов, запись невозможна!",
                    color = MaterialTheme.colorScheme.error,
                )
            },
            confirmButton = {
                TextButton(onClick = { showCadetZeroBalanceMessage = false }) { Text("OK") }
            },
        )
    }
    if (showCadetMaxBookingsMessage) {
        val selectedCadet = selectedCadetId?.let { id -> cadets.find { it.id == id } }
        if (selectedCadet != null) {
            AlertDialog(
                onDismissRequest = { showCadetMaxBookingsMessage = false },
                title = { Text("Достигнут лимит записей курсанта") },
                text = {
                    Text(
                        "По балансу курсанта (${selectedCadet.balance} талонов) уже запланировано максимальное количество вождений. Запись невозможна.",
                        color = MaterialTheme.colorScheme.error,
                    )
                },
                confirmButton = {
                    TextButton(onClick = { showCadetMaxBookingsMessage = false }) { Text("OK") }
                },
            )
        }
    }
    showTimeOccupiedDialog?.let { message ->
        val isWindowMessage = message == OCCUPIED_BY_WINDOW_MESSAGE
        AlertDialog(
            onDismissRequest = { showTimeOccupiedDialog = null },
            title = { Text("Время занято") },
            text = {
                Text(
                    if (isWindowMessage) "Данное время уже есть в списке окон. Выберите другое время."
                    else "Данное время занято курсантом: $message. Выберите свободное время.",
                    color = MaterialTheme.colorScheme.error,
                )
            },
            confirmButton = {
                TextButton(onClick = { showTimeOccupiedDialog = null }) { Text("OK") }
            },
        )
    }
}

@Composable
private fun AssignedWindowCard(
    session: DrivingSession,
    cadetName: String,
    onConfirmClick: () -> Unit,
    onCancelClick: () -> Unit,
) {
    val dims = LocalScreenDimensions.current
    val startDate = session.startTime?.toDate()
    val dayStr = startDate?.let { dayOfWeekFormat.format(it).replaceFirstChar { c -> c.uppercase() } } ?: ""
    val dateStr = startDate?.let { dateOnlyFormat.format(it) } ?: ""
    val timeStr = startDate?.let { timeOnlyFormat.format(it) } ?: ""
    val instructorConfirmed = session.instructorConfirmed
    val bookedByCadet = session.openWindowId.isNotEmpty()
    val showConfirmButton = bookedByCadet && !instructorConfirmed
    val statusText = when {
        showConfirmButton -> "Курсант забронировал — ожидает вашего подтверждения"
        !instructorConfirmed -> "Ожидает подтверждения записи курсантом"
        else -> "Подтверждён"
    }
    val cardShape = RoundedCornerShape(14.dp)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(cardShape)
            .border(3.dp, CardOutlineWhite, cardShape),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFD0E3F5),
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
        shape = cardShape,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dims.horizontalPadding),
            verticalArrangement = Arrangement.spacedBy(dims.mediumPadding),
            horizontalAlignment = Alignment.Start,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "Карточка вождения: ",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    cadetName,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(dims.listIconSize),
                )
                Spacer(Modifier.width(dims.mediumPadding))
                Column(horizontalAlignment = Alignment.Start) {
                    Text(
                        dayStr,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "Дата: ",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            dateStr,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Schedule,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "Время: ",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            timeStr,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                thickness = 1.dp,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                val statusColor = if (instructorConfirmed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    tint = statusColor,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "Статус: ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = statusColor,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = statusColor,
                )
            }
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                thickness = 1.dp,
            )
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val isNarrow = maxWidth < 360.dp
                if (isNarrow) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(dims.smallPadding),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        if (showConfirmButton) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                            ) {
                                GlossyButton(
                                    onClick = onConfirmClick,
                                    text = "Подтвердить",
                                    icon = Icons.Default.Check,
                                    usePrimaryBlue = true,
                                    modifier = Modifier.wrapContentWidth(),
                                )
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            GlossyButton(
                                onClick = onCancelClick,
                                text = "Отменить",
                                icon = Icons.Default.Delete,
                                useRedCancel = true,
                                modifier = Modifier.wrapContentWidth(),
                            )
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (showConfirmButton) {
                            GlossyButton(
                                onClick = onConfirmClick,
                                text = "Подтвердить",
                                icon = Icons.Default.Check,
                                usePrimaryBlue = true,
                                modifier = Modifier.wrapContentWidth(),
                            )
                            Spacer(Modifier.width(dims.smallPadding))
                        }
                        GlossyButton(
                            onClick = onCancelClick,
                            text = "Отменить",
                            icon = Icons.Default.Delete,
                            useRedCancel = true,
                            modifier = Modifier.wrapContentWidth(),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OpenWindowCard(
    windowId: String,
    dateTimeStr: String,
    onDelete: (String) -> Unit,
) {
    val dims = LocalScreenDimensions.current
    val deleteClick = remember(windowId, onDelete) { { onDelete(windowId) } }
    val cardShape = RoundedCornerShape(14.dp)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(cardShape)
            .border(3.dp, CardOutlineWhite, cardShape)
            .defaultMinSize(minHeight = 72.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFD0E3F5),
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
        shape = cardShape,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val isNarrow = maxWidth < 360.dp
            if (isNarrow) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(dims.horizontalPadding),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(dims.listIconSize),
                        )
                        Spacer(Modifier.width(dims.mediumPadding))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Свободное окно",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                dateTimeStr,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Medium,
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "Статус: ",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                )
                                Text(
                                    "свободно",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.error,
                                )
                            }
                        }
                    }
                    GlossyButton(
                        onClick = deleteClick,
                        text = "Удалить",
                        icon = Icons.Default.Delete,
                        useRedCancel = true,
                        modifier = Modifier.wrapContentWidth(),
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(dims.horizontalPadding),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(dims.listIconSize),
                        )
                        Spacer(Modifier.width(dims.mediumPadding))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Свободное окно",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                dateTimeStr,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Medium,
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "Статус: ",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                )
                                Text(
                                    "свободно",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.error,
                                )
                            }
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        GlossyButton(
                            onClick = deleteClick,
                            text = "Удалить",
                            icon = Icons.Default.Delete,
                            useRedCancel = true,
                            modifier = Modifier.wrapContentWidth(),
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerDialogFutureOnly(
    initialDate: Date,
    onDismiss: () -> Unit,
    onConfirm: (Date) -> Unit,
) {
    val calendar = remember { Calendar.getInstance() }
    val todayStart = remember {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
    val selectableDates = object : SelectableDates {
        override fun isSelectableDate(utcTimeMillis: Long): Boolean {
            val cal = Calendar.getInstance().apply { timeInMillis = utcTimeMillis }
            val dayStart = cal.apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
            return dayStart >= todayStart
        }
        override fun isSelectableYear(year: Int): Boolean = year >= Calendar.getInstance().get(Calendar.YEAR)
    }
    val state = androidx.compose.material3.rememberDatePickerState(
        initialSelectedDateMillis = initialDate.time,
        selectableDates = selectableDates,
    )
    androidx.compose.material3.DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    state.selectedDateMillis?.let { ms -> onConfirm(Date(ms)) }
                    onDismiss()
                },
            ) { Text("OK") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        },
    ) {
        androidx.compose.material3.DatePicker(state = state)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialogSimple(
    initialTime: Date,
    onDismiss: () -> Unit,
    onConfirm: (Date) -> Unit,
) {
    val cal = remember(initialTime) { Calendar.getInstance().apply { time = initialTime } }
    val state = androidx.compose.material3.rememberTimePickerState(
        initialHour = cal.get(Calendar.HOUR_OF_DAY),
        initialMinute = cal.get(Calendar.MINUTE),
        is24Hour = true,
    )
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val c = Calendar.getInstance()
                    c.set(Calendar.HOUR_OF_DAY, state.hour)
                    c.set(Calendar.MINUTE, state.minute)
                    c.set(Calendar.SECOND, 0)
                    c.set(Calendar.MILLISECOND, 0)
                    onConfirm(c.time)
                    onDismiss()
                },
            ) { Text("OK") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } },
        text = {
            androidx.compose.material3.TimePicker(state = state)
        },
    )
}
