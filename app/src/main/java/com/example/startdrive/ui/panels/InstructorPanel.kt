package com.example.startdrive.ui.panels

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.TextButton
import androidx.compose.foundation.Image
import com.example.startdrive.R
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import android.media.AudioAttributes
import android.media.MediaPlayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.draw.scale
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import com.example.startdrive.data.model.DrivingSession
import com.example.startdrive.data.model.User
import com.example.startdrive.data.repository.AppSettings
import com.example.startdrive.data.repository.InAppNotificationBaseline
import com.example.startdrive.data.repository.NotificationStorage
import com.example.startdrive.data.repository.ChatRepository
import com.example.startdrive.data.repository.DrivingRepository
import com.example.startdrive.data.repository.UserRepository
import com.example.startdrive.ui.instructor.InstructorChatTab
import com.example.startdrive.ui.instructor.InstructorHistoryTab
import com.example.startdrive.ui.instructor.InstructorHomeTab
import com.example.startdrive.ui.instructor.InstructorProfileTab
import com.example.startdrive.ui.instructor.InstructorRecordingTab
import com.example.startdrive.ui.theme.LocalScreenDimensions
import com.example.startdrive.ui.components.NotificationItem
import com.example.startdrive.ui.components.NotificationsTabContent
import com.example.startdrive.ui.components.TopSnackbarHost
import com.example.startdrive.ui.settings.SettingsTab
import com.example.startdrive.ui.pdd.PddMarkupScreen
import com.example.startdrive.ui.pdd.PddPenaltiesScreen
import com.example.startdrive.ui.pdd.PddSignsScreen
import com.example.startdrive.ui.pdd.PddTicketScreen
import com.example.startdrive.ui.pdd.PddTicketsScreen
import com.example.startdrive.ui.pdd.PddTopicsScreen
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.delay
import com.example.startdrive.ui.components.AnimatedGlossOverlay

/** Извлекает фамилию (первое слово) из ФИО для уведомлений: «Курсант (Иванов) ...». */
private fun surnameFromFullName(fullName: String?): String =
    fullName?.trim()?.split(Regex("\\s+"))?.firstOrNull()?.takeIf { it.isNotBlank() } ?: "—"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstructorPanel(
    currentUser: User,
    onSignOut: () -> Unit,
    onRefreshUser: () -> Unit = {},
) {
    var tabIndex by rememberSaveable { mutableIntStateOf(0) }
    var selectedChatContactId by rememberSaveable { mutableStateOf<String?>(null) }
    var showProfile by rememberSaveable { mutableStateOf(false) }
    var showNotifications by rememberSaveable { mutableStateOf(false) }
    var showSettings by rememberSaveable { mutableStateOf(false) }
    var selectedPddCategory by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedPddTicket by rememberSaveable { mutableStateOf<String?>(null) }
    var showLogoutConfirm by remember { mutableStateOf(false) }
    var showDrivingCompletedSession by remember { mutableStateOf<DrivingSession?>(null) }
    var isClosingDrivingDialog by remember { mutableStateOf(false) }
    var triggerBalancePulse by remember { mutableStateOf(false) }
    val drivingDialogScale = remember { Animatable(1f) }
    val drivingDialogOffsetY = remember { Animatable(0f) }
    val tabs = listOf("Главная", "Запись", "Чат", "Билеты", "История")

    val userRepo = remember { UserRepository() }
    val adminUsers by userRepo.usersByRole("admin").collectAsState(initial = emptyList())
    var cadets by remember { mutableStateOf<List<User>>(emptyList()) }
    LaunchedEffect(currentUser.id) {
        val inst = userRepo.getUser(currentUser.id)
        cadets = inst?.assignedCadets?.mapNotNull { userRepo.getUser(it) } ?: emptyList()
    }
    val contacts = listOfNotNull(adminUsers.firstOrNull()) + cadets
    LaunchedEffect(contacts.map { it.id }, currentUser.id) {
        contacts.forEach { c ->
            ChatRepository.messages(ChatRepository.chatRoomId(currentUser.id, c.id))
        }
    }
    val totalUnread by ChatRepository.totalUnreadCount(currentUser.id, contacts.map { it.id }).collectAsState(0)
    val dims = LocalScreenDimensions.current
    val context = LocalContext.current

    val notificationsList = remember(currentUser.id) {
        mutableStateListOf<NotificationItem>().apply {
            addAll(
                NotificationStorage.load(context, currentUser.id).map { n ->
                    NotificationItem(n.id, n.timestamp, n.message)
                }
            )
        }
    }
    var lastSeenNotificationCount by remember(currentUser.id) { mutableStateOf(0) }
    LaunchedEffect(showNotifications) {
        if (showNotifications) lastSeenNotificationCount = notificationsList.size
    }
    val unreadNotificationCount = (notificationsList.size - lastSeenNotificationCount).coerceAtLeast(0)

    fun addNotification(message: String) {
        val item = NotificationItem(System.currentTimeMillis(), System.currentTimeMillis(), message)
        notificationsList.add(0, item)
        NotificationStorage.save(context, currentUser.id, notificationsList.map { NotificationStorage.StoredNotification(it.id, it.timestamp, it.message) })
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val drivingRepo = remember { DrivingRepository() }
    val sessions by drivingRepo.sessionsForInstructor(currentUser.id).collectAsState(initial = emptyList())
    val balanceHistory by drivingRepo.balanceHistory(currentUser.id).collectAsState(initial = emptyList())

    val loadedBaseline = remember(currentUser.id) { InAppNotificationBaseline.loadInstructor(context, currentUser.id) }
    var prevSessionStates by remember(currentUser.id) {
        mutableStateOf<Map<String, Triple<String, Boolean, Boolean>>>(loadedBaseline?.sessionStates ?: emptyMap())
    }
    var prevBalanceCount by remember(currentUser.id) {
        mutableStateOf(loadedBaseline?.balanceCount ?: -1)
    }
    var prevTotalUnread by remember(currentUser.id) {
        mutableStateOf(loadedBaseline?.unread ?: -1)
    }
    var inAppNotificationInitialized by remember(currentUser.id) {
        mutableStateOf(loadedBaseline != null)
    }
    var totalUnreadJustSynced by remember(currentUser.id) { mutableStateOf(false) }
    var badgesResetOnRestart by remember(currentUser.id) { mutableStateOf(false) }
    var balanceJustSynced by remember(currentUser.id) { mutableStateOf(false) }
    var sessionLoopFirstRunDone by remember(currentUser.id) { mutableStateOf(false) }
    var cadetRatingShownFor by remember { mutableStateOf<Set<String>>(emptySet()) }

    LaunchedEffect(sessions) {
        if (!inAppNotificationInitialized) {
            if (sessions.isEmpty()) return@LaunchedEffect
            prevSessionStates = sessions.associate { it.id to Triple(it.status, it.instructorConfirmed, it.session?.cadetConfirmed == true) }
            inAppNotificationInitialized = true
            return@LaunchedEffect
        }
        if (sessions.isEmpty()) return@LaunchedEffect
        if (!sessionLoopFirstRunDone) {
            prevSessionStates = sessions.associate { it.id to Triple(it.status, it.instructorConfirmed, it.session?.cadetConfirmed == true) }
            sessionLoopFirstRunDone = true
            return@LaunchedEffect
        }
        for (s in sessions) {
            val prev = prevSessionStates[s.id]
            val prevInstructorConfirmed = prev?.second == true
            val prevCadetConfirmed = prev?.third == true
            if (prev == null && s.status == "scheduled" && s.openWindowId.isNotEmpty()) {
                val surname = surnameFromFullName(cadets.find { it.id == s.cadetId }?.fullName)
                val msg = "Курсант ($surname) забронировал вождение — подтвердите бронь"
                if (AppSettings.getTextNotificationsEnabled(context)) snackbarHostState.showSnackbar(msg)
                addNotification(msg)
                if (AppSettings.getSoundNotificationsEnabled(context)) {
                    MediaPlayer.create(context, R.raw.kursan_zabroniroval)?.apply {
                        start()
                        setOnCompletionListener { release() }
                    }
                }
            }
            if (prev != null && !prevInstructorConfirmed && s.instructorConfirmed) {
                val surname = surnameFromFullName(cadets.find { it.id == s.cadetId }?.fullName)
                val msg = "Курсант ($surname) подтвердил запись на вождение"
                if (AppSettings.getTextNotificationsEnabled(context)) snackbarHostState.showSnackbar(msg)
                addNotification(msg)
            }
            if (prev != null && !prevCadetConfirmed && s.session?.cadetConfirmed == true) {
                val surname = surnameFromFullName(cadets.find { it.id == s.cadetId }?.fullName)
                val msg = "Курсант ($surname) подтвердил начало вождения"
                if (AppSettings.getTextNotificationsEnabled(context)) snackbarHostState.showSnackbar(msg)
                addNotification(msg)
            }
            if (prev != null && prev.first != "cancelledByCadet" && s.status == "cancelledByCadet") {
                val surname = surnameFromFullName(cadets.find { it.id == s.cadetId }?.fullName)
                val msg = "Курсант ($surname) отменил вождение"
                if (AppSettings.getTextNotificationsEnabled(context)) snackbarHostState.showSnackbar(msg)
                addNotification(msg)
            }
            if (prev != null && prev.first != "completed" && s.status == "completed") {
                showDrivingCompletedSession = s
            }
            if (prev != null && s.status == "completed" && s.cadetRating > 0 && s.id !in cadetRatingShownFor) {
                val msg = "Вождение завершено!"
                if (AppSettings.getTextNotificationsEnabled(context)) snackbarHostState.showSnackbar(msg)
                addNotification(msg)
                cadetRatingShownFor = cadetRatingShownFor + s.id
            }
        }
        prevSessionStates = sessions.associate { it.id to Triple(it.status, it.instructorConfirmed, it.session?.cadetConfirmed == true) }
    }

    LaunchedEffect(balanceHistory, inAppNotificationInitialized) {
        if (!inAppNotificationInitialized) return@LaunchedEffect
        if (prevBalanceCount < 0) {
            if (balanceHistory.isEmpty()) return@LaunchedEffect
            prevBalanceCount = balanceHistory.size
            balanceJustSynced = true
            return@LaunchedEffect
        }
        if (balanceJustSynced) {
            balanceJustSynced = false
            if (balanceHistory.isNotEmpty()) prevBalanceCount = balanceHistory.size
            return@LaunchedEffect
        }
        if (balanceHistory.size > prevBalanceCount && balanceHistory.isNotEmpty()) {
            val newest = balanceHistory.first()
            val msg = when (newest.type) {
                "credit" -> "Поступление на баланс: +${newest.amount}"
                "debit" -> "Списание с баланса: −${newest.amount}"
                else -> null
            }
            msg?.let { m ->
                if (AppSettings.getTextNotificationsEnabled(context)) snackbarHostState.showSnackbar(m)
                addNotification(m)
            }
            if (newest.type == "debit" && AppSettings.getSoundNotificationsEnabled(context)) {
                MediaPlayer.create(context, R.raw.spisanie_s_balansa)?.apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    start()
                    setOnCompletionListener { release() }
                }
            }
        }
        if (balanceHistory.isNotEmpty()) prevBalanceCount = balanceHistory.size // не затирать baseline при пустой загрузке
    }

    LaunchedEffect(totalUnread, inAppNotificationInitialized) {
        if (!inAppNotificationInitialized) return@LaunchedEffect
        if (prevTotalUnread < 0) {
            prevTotalUnread = totalUnread
            totalUnreadJustSynced = true
            return@LaunchedEffect
        }
        if (totalUnreadJustSynced) {
            totalUnreadJustSynced = false
            prevTotalUnread = totalUnread
            return@LaunchedEffect
        }
        if (totalUnread > prevTotalUnread) {
            val msg = "Новое сообщение в чате"
            if (AppSettings.getTextNotificationsEnabled(context)) snackbarHostState.showSnackbar(msg)
            addNotification(msg)
        }
        if (totalUnread >= prevTotalUnread) prevTotalUnread = totalUnread
    }

    val historySessionCount = sessions.count { it.status in listOf("completed", "cancelledByInstructor", "cancelledByCadet") }
    val totalHistoryCount = historySessionCount + balanceHistory.size
    var prevHistoryTotalCount by remember(currentUser.id) {
        mutableStateOf(loadedBaseline?.historyTotalCount ?: -1)
    }
    var unseenHistoryCount by remember(currentUser.id) { mutableStateOf(0) }
    val homeBadgeCount = sessions.count { it.status == "scheduled" && (!it.instructorConfirmed || it.startRequestedByInstructor) }
    val recordingBadgeCount = sessions.count { it.status == "scheduled" && (!it.instructorConfirmed || it.startRequestedByInstructor) }
    LaunchedEffect(tabIndex, totalHistoryCount) {
        if (prevHistoryTotalCount < 0) {
            prevHistoryTotalCount = totalHistoryCount
            return@LaunchedEffect
        }
        if (tabIndex == 4) {
            unseenHistoryCount = 0
            prevHistoryTotalCount = totalHistoryCount
        } else if (totalHistoryCount > prevHistoryTotalCount) {
            unseenHistoryCount += (totalHistoryCount - prevHistoryTotalCount)
            prevHistoryTotalCount = totalHistoryCount
        }
    }
    val themeKey = AppSettings.getTheme(context)
    LaunchedEffect(inAppNotificationInitialized, themeKey, totalHistoryCount, notificationsList.size) {
        if (!inAppNotificationInitialized || badgesResetOnRestart) return@LaunchedEffect
        delay(2000)
        if (badgesResetOnRestart) return@LaunchedEffect
        prevHistoryTotalCount = totalHistoryCount
        unseenHistoryCount = 0
        lastSeenNotificationCount = notificationsList.size
        badgesResetOnRestart = true
    }

    Scaffold(
        snackbarHost = { },
        topBar = {
            TopAppBar(
                title = {
                Text(
                    "Кабинет инструктора:",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = (18 * dims.titleFontScale).sp,
                    ),
                )
            },
                actions = {
                    IconButton(onClick = {
                        showProfile = !showProfile
                        if (showProfile) {
                            showNotifications = false
                            showSettings = false
                        }
                    }) {
                        Icon(Icons.Default.Person, contentDescription = if (showProfile) "Назад" else "Профиль")
                    }
                    IconButton(onClick = {
                        showNotifications = !showNotifications
                        if (showNotifications) {
                            showProfile = false
                            showSettings = false
                        }
                    }) {
                        Box(contentAlignment = Alignment.TopEnd) {
                            Icon(Icons.Default.Notifications, contentDescription = if (showNotifications) "Закрыть уведомления" else "Уведомления")
                            if (unreadNotificationCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .offset(x = dims.smallPadding, y = (-dims.smallPadding))
                                        .size(dims.navBadgeSize)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.error),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = if (unreadNotificationCount > 99) "99+" else "$unreadNotificationCount",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onError,
                                    )
                                }
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            )
        },
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { index, title ->
                    val isHomeTab = index == 0
                    val isRecordingTab = index == 1
                    val isChatTab = index == 2
                    val isTicketsTab = index == 3
                    val isHistoryTab = index == 4
                    NavigationBarItem(
                        icon = {
                            Box(
                                modifier = Modifier.size(dims.navIconSize),
                                contentAlignment = Alignment.Center,
                            ) {
                                Box(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .clip(RoundedCornerShape(18.dp)),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    when (index) {
                                        0 -> Image(
                                            painter = painterResource(R.drawable.ic_home_instructor),
                                            contentDescription = title,
                                            modifier = Modifier.size(dims.navIconSize),
                                        )
                                        1 -> Image(
                                            painter = painterResource(R.drawable.ic_record_instructor),
                                            contentDescription = title,
                                            modifier = Modifier.size(dims.navIconSize),
                                        )
                                        2 -> Image(
                                            painter = painterResource(R.drawable.ic_chat_instructor),
                                            contentDescription = title,
                                            modifier = Modifier.size(dims.navIconSize),
                                        )
                                        3 -> Image(
                                            painter = painterResource(R.drawable.ic_tickets_instructor),
                                            contentDescription = title,
                                            modifier = Modifier.size(dims.navIconSize),
                                        )
                                        4 -> Image(
                                            painter = painterResource(R.drawable.ic_history_instructor),
                                            contentDescription = title,
                                            modifier = Modifier.size(dims.navIconSize),
                                        )
                                        else -> Image(
                                            painter = painterResource(R.drawable.ic_history_instructor),
                                            contentDescription = title,
                                            modifier = Modifier.size(dims.navIconSize),
                                        )
                                    }
                                    if (!showProfile && !showNotifications && !showSettings && tabIndex == index) {
                                        AnimatedGlossOverlay(
                                            modifier = Modifier.matchParentSize(),
                                            highlightAlpha = 0.20f,
                                            durationMillis = 2200,
                                        )
                                    }
                                }
                                if (isHomeTab && homeBadgeCount > 0) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(start = dims.smallPadding, bottom = dims.smallPadding)
                                            .size(dims.navBadgeSize)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.error),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text(
                                            text = if (homeBadgeCount > 99) "99+" else "$homeBadgeCount",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onError,
                                        )
                                    }
                                }
                                if (isRecordingTab && recordingBadgeCount > 0) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(start = dims.smallPadding, bottom = dims.smallPadding)
                                            .size(dims.navBadgeSize)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.error),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text(
                                            text = if (recordingBadgeCount > 99) "99+" else "$recordingBadgeCount",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onError,
                                        )
                                    }
                                }
                                if (isChatTab && totalUnread > 0) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(start = dims.smallPadding, bottom = dims.smallPadding)
                                            .size(dims.navBadgeSize)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.error),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text(
                                            text = if (totalUnread > 99) "99+" else "$totalUnread",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onError,
                                        )
                                    }
                                }
                                if (isHistoryTab && unseenHistoryCount > 0) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(start = dims.smallPadding, bottom = dims.smallPadding)
                                            .size(dims.navBadgeSize)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.error),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text(
                                            text = if (unseenHistoryCount > 99) "99+" else "$unseenHistoryCount",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onError,
                                        )
                                    }
                                }
                            }
                        },
                        label = { Text(title) },
                        selected = !showProfile && !showNotifications && !showSettings && tabIndex == index,
                        onClick = {
                            showProfile = false
                            showNotifications = false
                            showSettings = false
                            tabIndex = index
                        },
                    )
                }
            }
        },
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            if (showSettings) {
                SettingsTab(onBack = { showSettings = false; showProfile = true }, currentUserId = currentUser.id)
            } else if (showProfile) {
                InstructorProfileTab(
                    instructor = currentUser,
                    balanceBadgeScale = 1f,
                    onSettingsClick = { showSettings = true; showProfile = false },
                    onLogoutClick = { showLogoutConfirm = true },
                )
            } else if (showNotifications) {
                NotificationsTabContent(
                    notifications = notificationsList.toList(),
                    onClear = {
                        notificationsList.clear()
                        NotificationStorage.clear(context, currentUser.id)
                    },
                )
            } else when (tabIndex) {
                0 -> InstructorHomeTab(
                    instructor = currentUser,
                    onOpenChatWith = { contactId ->
                        selectedChatContactId = contactId
                        tabIndex = 2
                    },
                    triggerBalancePulse = triggerBalancePulse,
                    onBalancePulseDone = { triggerBalancePulse = false },
                )
                1 -> InstructorRecordingTab(instructor = currentUser)
                2 -> InstructorChatTab(
                    currentUserId = currentUser.id,
                    selectedContactId = selectedChatContactId,
                    onSelectedContactIdChange = { selectedChatContactId = it },
                )
                3 -> when {
                    selectedPddCategory == "by_topic" && selectedPddTicket == null -> PddTopicsScreen(
                        onBack = { selectedPddCategory = null },
                    )
                    selectedPddCategory == "signs" && selectedPddTicket == null -> PddSignsScreen(
                        onBack = { selectedPddCategory = null },
                    )
                    selectedPddCategory == "markup" && selectedPddTicket == null -> PddMarkupScreen(
                        onBack = { selectedPddCategory = null },
                    )
                    selectedPddCategory == "penalties" && selectedPddTicket == null -> PddPenaltiesScreen(
                        onBack = { selectedPddCategory = null },
                    )
                    selectedPddTicket != null -> PddTicketScreen(
                        categoryId = selectedPddCategory ?: "A_B",
                        ticketName = selectedPddTicket!!,
                        onBack = { selectedPddTicket = null },
                    )
                    else -> PddTicketsScreen(
                        selectedCategoryId = selectedPddCategory,
                        onBack = { if (selectedPddCategory != null) selectedPddCategory = null },
                        onCategoryClick = { selectedPddCategory = it },
                        onTicketClick = { selectedPddTicket = it },
                    )
                }
                4 -> InstructorHistoryTab(instructorId = currentUser.id)
            }
            TopSnackbarHost(snackbarHostState, Modifier.align(Alignment.BottomCenter))
        }
    }
    if (showLogoutConfirm) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirm = false },
            title = { Text("Выход") },
            text = {
                Text("Вы уверены что хотите выйти? Можно просто свернуть приложение ))")
            },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutConfirm = false
                    InAppNotificationBaseline.saveInstructor(
                        context,
                        currentUser.id,
                        totalUnread,
                        balanceHistory.size,
                        historySessionCount + balanceHistory.size,
                        sessions.associate { it.id to Triple(it.status, it.instructorConfirmed, it.session?.cadetConfirmed == true) },
                    )
                    onSignOut()
                }) { Text("Да") }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirm = false }) { Text("Нет") }
            },
        )
    }
    LaunchedEffect(showDrivingCompletedSession) {
        if (showDrivingCompletedSession != null) {
            drivingDialogScale.snapTo(1f)
            drivingDialogOffsetY.snapTo(0f)
            isClosingDrivingDialog = false
        }
    }
    LaunchedEffect(isClosingDrivingDialog) {
        if (!isClosingDrivingDialog) return@LaunchedEffect
        drivingDialogScale.animateTo(0.2f, tween(350))
        drivingDialogOffsetY.animateTo(-400f, tween(350))
        showDrivingCompletedSession = null
        isClosingDrivingDialog = false
        triggerBalancePulse = true
        onRefreshUser()
    }
    showDrivingCompletedSession?.let { session ->
        val currentSession = sessions.find { it.id == session.id } ?: session
        val dateTimeFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
        val balance = currentUser.balance
        val startMillis = currentSession.session?.startTime ?: currentSession.startTime?.toDate()?.time ?: 0L
        val startStr = if (startMillis > 0) dateTimeFormat.format(Date(startMillis)) else "—"
        val completedStr = currentSession.completedAt?.toDate()?.let { dateTimeFormat.format(it) } ?: "—"
        val drivingCompletedGreen = Color(0xFF2E7D32)
        Dialog(
            onDismissRequest = { showDrivingCompletedSession = null },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .scale(drivingDialogScale.value)
                        .offset(y = drivingDialogOffsetY.value.dp)
                        .fillMaxWidth(0.78f)
                        .clip(RoundedCornerShape(20.dp)),
                ) {
                    Image(
                        painter = painterResource(R.drawable.fon_driving_completed),
                        contentDescription = null,
                        modifier = Modifier.matchParentSize(),
                        contentScale = ContentScale.Crop,
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .wrapContentSize()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.72f))
                            .padding(20.dp),
                    ) {
                        Column(
                            modifier = Modifier.wrapContentWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                        val titleStyle = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        val labelStyle = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                        val valueStyle = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Normal)
                        val valueColor = Color.Blue
                        Text(
                            "Вождение завершено!",
                            style = titleStyle,
                            color = drivingCompletedGreen,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(Modifier.size(2.dp))
                        Row(horizontalArrangement = Arrangement.Center) {
                            Text("Баланс талонов: ", style = labelStyle, color = Color.Black)
                            Text("$balance", style = valueStyle, color = valueColor)
                        }
                        Row(horizontalArrangement = Arrangement.Center) {
                            Text("Зачислено: ", style = labelStyle, color = Color.Black)
                            Text("+1 талон", style = valueStyle, color = valueColor)
                        }
                        Spacer(Modifier.size(2.dp))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Начато: ", style = labelStyle, color = Color.Black)
                            Text(startStr, style = valueStyle, color = valueColor, textAlign = TextAlign.Center)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Завершено: ", style = labelStyle, color = Color.Black)
                            Text(completedStr, style = valueStyle, color = valueColor, textAlign = TextAlign.Center)
                        }
                        Spacer(Modifier.size(8.dp))
                        Button(
                            onClick = { isClosingDrivingDialog = true },
                        ) {
                            Text("Закрыть")
                        }
                    }
                    }
                }
            }
        }
    }
}
