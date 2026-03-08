package com.example.startdrive.ui.panels

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.startdrive.R
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.TextButton
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.background
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.startdrive.data.model.User
import com.example.startdrive.data.repository.ChatRepository
import com.example.startdrive.data.repository.AppSettings
import com.example.startdrive.data.repository.InAppNotificationBaseline
import kotlinx.coroutines.delay
import com.example.startdrive.data.repository.NotificationStorage
import com.example.startdrive.ui.components.NotificationItem
import com.example.startdrive.ui.components.NotificationsTabContent
import com.example.startdrive.ui.components.TopSnackbarHost
import com.example.startdrive.data.repository.DrivingRepository
import com.example.startdrive.data.repository.UserRepository
import com.example.startdrive.ui.admin.AdminBalanceTab
import com.example.startdrive.ui.admin.AdminChatTab
import com.example.startdrive.ui.admin.AdminHistoryTab
import com.example.startdrive.ui.admin.AdminHomeTab
import com.example.startdrive.ui.theme.LocalScreenDimensions
import com.example.startdrive.ui.components.AnimatedGlossOverlay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanel(
    currentUser: User,
    onSignOut: () -> Unit,
) {
    var tabIndex by rememberSaveable { mutableIntStateOf(0) }
    var selectedChatContactId by rememberSaveable { mutableStateOf<String?>(null) }
    var showNotifications by rememberSaveable { mutableStateOf(false) }
    var showLogoutConfirm by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val tabs = listOf("Главная", "Баланс", "Чат", "История")

    val userRepo = remember { UserRepository() }
    val contacts by userRepo.allNonAdminUsers().collectAsState(initial = emptyList())
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

    val drivingRepo = remember { DrivingRepository() }
    val balanceHistory by drivingRepo.allBalanceHistory().collectAsState(initial = emptyList())
    val totalHistoryCount = balanceHistory.size
    val loadedBaseline = remember(currentUser.id) { InAppNotificationBaseline.loadAdmin(context, currentUser.id) }
    var prevHistoryTotalCount by remember(currentUser.id) {
        mutableStateOf(loadedBaseline?.historyTotalCount ?: -1)
    }
    var unseenHistoryCount by remember(currentUser.id) { mutableStateOf(0) }
    var badgesResetOnRestart by remember(currentUser.id) { mutableStateOf(false) }
    LaunchedEffect(tabIndex, totalHistoryCount) {
        if (prevHistoryTotalCount < 0) {
            prevHistoryTotalCount = totalHistoryCount
            return@LaunchedEffect
        }
        if (tabIndex == 3) {
            unseenHistoryCount = 0
            prevHistoryTotalCount = totalHistoryCount
        } else if (totalHistoryCount > prevHistoryTotalCount) {
            unseenHistoryCount += (totalHistoryCount - prevHistoryTotalCount)
            prevHistoryTotalCount = totalHistoryCount
        }
    }
    val themeKey = AppSettings.getTheme(context)
    LaunchedEffect(themeKey, totalHistoryCount, notificationsList.size) {
        if (badgesResetOnRestart) return@LaunchedEffect
        delay(2000)
        if (badgesResetOnRestart) return@LaunchedEffect
        prevHistoryTotalCount = totalHistoryCount
        unseenHistoryCount = 0
        lastSeenNotificationCount = notificationsList.size
        badgesResetOnRestart = true
    }

    Scaffold(
        snackbarHost = { TopSnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Личный кабинет Администратора",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontSize = (18 * dims.titleFontScale).sp,
                        ),
                    )
                },
                actions = {
                    IconButton(onClick = { showNotifications = !showNotifications }) {
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
                    IconButton(onClick = { showLogoutConfirm = true }) {
                        Icon(Icons.Default.Logout, contentDescription = "Выйти")
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
                    val isChatTab = index == 2
                    val isHistoryTab = index == 3
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
                                            painter = painterResource(R.drawable.ic_history_instructor),
                                            contentDescription = title,
                                            modifier = Modifier.size(dims.navIconSize),
                                        )
                                        else -> Icon(Icons.Default.History, contentDescription = title, modifier = Modifier.size(dims.navIconSize))
                                    }
                                    if (tabIndex == index) {
                                        AnimatedGlossOverlay(
                                            modifier = Modifier.matchParentSize(),
                                            highlightAlpha = 0.20f,
                                            durationMillis = 2200,
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
                        selected = !showNotifications && tabIndex == index,
                        onClick = {
                            showNotifications = false
                            tabIndex = index
                        },
                    )
                }
            }
        },
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            if (showNotifications) {
                NotificationsTabContent(
                    notifications = notificationsList.toList(),
                    onClear = {
                        notificationsList.clear()
                        NotificationStorage.clear(context, currentUser.id)
                    },
                )
            } else when (tabIndex) {
                0 -> AdminHomeTab(
                    adminId = currentUser.id,
                    snackbarHostState = snackbarHostState,
                    onNotification = { addNotification(it) },
                )
                1 -> AdminBalanceTab(
                    adminId = currentUser.id,
                    snackbarHostState = snackbarHostState,
                    onNotification = { addNotification(it) },
                )
                2 -> AdminChatTab(
                    currentUserId = currentUser.id,
                    selectedContactId = selectedChatContactId,
                    onSelectedContactIdChange = { selectedChatContactId = it },
                    snackbarHostState = snackbarHostState,
                    onNotification = { addNotification(it) },
                )
                3 -> AdminHistoryTab(adminId = currentUser.id)
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
                    InAppNotificationBaseline.saveAdmin(context, currentUser.id, totalUnread, totalHistoryCount)
                    onSignOut()
                }) { Text("Да") }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirm = false }) { Text("Нет") }
            },
        )
    }
}

