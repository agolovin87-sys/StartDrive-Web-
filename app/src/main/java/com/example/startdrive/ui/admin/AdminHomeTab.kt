package com.example.startdrive.ui.admin

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Phone
import androidx.compose.foundation.clickable
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.startdrive.data.model.User
import com.example.startdrive.data.repository.UserRepository
import com.example.startdrive.ui.components.CollapsibleCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminHomeTab(
    adminId: String,
    snackbarHostState: SnackbarHostState = SnackbarHostState(),
    onNotification: (String) -> Unit = {},
) {
    val userRepo = remember { UserRepository() }
    val instructors by userRepo.usersByRole("instructor").collectAsState(initial = emptyList())
    val cadets by userRepo.usersByRole("cadet").collectAsState(initial = emptyList())
    val pendingUsers = remember(instructors, cadets) {
        (instructors.filter { !it.isActive } + cadets.filter { !it.isActive })
            .sortedBy { it.fullName }
    }
    var instructorForAssignCadet by remember { mutableStateOf<User?>(null) }
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
        if (pendingUsers.isNotEmpty()) {
            item {
                CollapsibleCard(title = "Заявки на регистрацию", count = pendingUsers.size) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        pendingUsers.forEach { u ->
                            PendingUserCard(
                                user = u,
                                onActivate = { scope.launch { userRepo.setActive(u.id, true) } },
                                onDelete = { scope.launch { userRepo.deleteUser(u.id) } },
                            )
                        }
                    }
                }
            }
        }
        item {
            CollapsibleCard(title = "Инструкторы", count = instructors.size) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    instructors.forEach { u ->
                        InstructorCard(
                            user = u,
                            onCall = {
                                val i = Intent(Intent.ACTION_DIAL).setData(Uri.parse("tel:${u.phone}"))
                                ctx.startActivity(i)
                            },
                            onMessage = { },
                            onAssignCadet = { instructorForAssignCadet = u },
                            onDelete = { scope.launch { userRepo.deleteUser(u.id) } },
                            onActiveChange = { scope.launch { userRepo.setActive(u.id, it) } },
                        )
                    }
                }
            }
        }
        item {
            CollapsibleCard(title = "Курсанты", count = cadets.size) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    cadets.forEach { u ->
                        val instructorName = instructors.find { it.id == u.assignedInstructorId }?.fullName ?: "—"
                        CadetCard(
                            user = u,
                            assignedInstructorName = instructorName,
                            onCall = {
                                val i = Intent(Intent.ACTION_DIAL).setData(Uri.parse("tel:${u.phone}"))
                                ctx.startActivity(i)
                            },
                            onMessage = { },
                            onDelete = { scope.launch { userRepo.deleteUser(u.id) } },
                            onActiveChange = { scope.launch { userRepo.setActive(u.id, it) } },
                        )
                    }
                }
            }
        }
        item {
            CollapsibleCard(title = "Расписание") {
                Text("ФИО Инструктора → Дата → Записи", style = MaterialTheme.typography.labelMedium)
                instructors.forEach { inst ->
                    Text("${inst.fullName}: записи в driving_sessions", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }

        instructorForAssignCadet?.let { instructor ->
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ModalBottomSheet(
                onDismissRequest = { instructorForAssignCadet = null },
                sheetState = sheetState,
                dragHandle = { BottomSheetDefaults.DragHandle() },
            ) {
                Column(Modifier.heightIn(max = 400.dp).padding(horizontal = 16.dp)) {
                    Text(
                        "Прикрепить курсанта к ${instructor.fullName}",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 12.dp),
                    )
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        items(cadets) { cadet ->
                            val alreadyAssigned = cadet.assignedInstructorId == instructor.id
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .then(
                                        if (alreadyAssigned) Modifier
                                        else Modifier.clickable {
                                            scope.launch {
                                                runCatching {
                                                    userRepo.assignCadetToInstructor(instructor.id, cadet.id)
                                                }.onSuccess {
                                                    val surname = cadet.fullName.trim().split(Regex("\\s+")).firstOrNull()?.takeIf { it.isNotBlank() } ?: "—"
                                                    val msg = "Курсант ($surname) прикреплён к ${instructor.fullName}"
                                                    snackbarHostState.showSnackbar(msg)
                                                    onNotification(msg)
                                                    instructorForAssignCadet = null
                                                }.onFailure {
                                                    val msg = "Ошибка: ${it.message ?: "не удалось прикрепить"}"
                                                    snackbarHostState.showSnackbar(msg)
                                                    onNotification(msg)
                                                }
                                            }
                                        }
                                    )
                                    .padding(vertical = 12.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(
                                    cadet.fullName,
                                    style = MaterialTheme.typography.bodyLarge,
                                )
                                if (alreadyAssigned) {
                                    Text(
                                        "прикреплён",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                }
                            }
                        }
                    }
                    if (cadets.isEmpty()) {
                        Text(
                            "Нет курсантов в системе",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(16.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InstructorCard(
    user: User,
    onCall: () -> Unit,
    onMessage: () -> Unit,
    onAssignCadet: () -> Unit,
    onDelete: () -> Unit,
    onActiveChange: (Boolean) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant),
        shape = MaterialTheme.shapes.small,
    ) {
        Column(Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(Modifier.weight(1f, fill = false)) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 2.dp)) {
                        Icon(Icons.Default.Person, contentDescription = null, Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                        Text(" ФИО: ", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(user.fullName, style = MaterialTheme.typography.bodyMedium, maxLines = 1)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 2.dp)) {
                        Icon(Icons.Default.Phone, contentDescription = null, Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                        Text(" Тел.: ", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(user.phone, style = MaterialTheme.typography.bodySmall)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AccountBalance, contentDescription = null, Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                        Text(" Баланс талонов: ", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${user.balance}", style = MaterialTheme.typography.bodySmall)
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    FilledTonalIconButton(onClick = onCall, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Call, contentDescription = "Позвонить", Modifier.size(18.dp))
                    }
                    FilledTonalIconButton(onClick = onMessage, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Email, contentDescription = "Сообщение", Modifier.size(18.dp))
                    }
                    FilledTonalIconButton(onClick = onAssignCadet, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.PersonAdd, contentDescription = "Прикрепить курсанта", Modifier.size(18.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Удалить", Modifier.size(18.dp))
                    }
                    Switch(checked = user.isActive, onCheckedChange = onActiveChange, modifier = Modifier.padding(start = 4.dp))
                }
            }
        }
    }
}

@Composable
private fun CadetCard(
    user: User,
    assignedInstructorName: String,
    onCall: () -> Unit,
    onMessage: () -> Unit,
    onDelete: () -> Unit,
    onActiveChange: (Boolean) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant),
        shape = MaterialTheme.shapes.small,
    ) {
        Column(Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(Modifier.weight(1f, fill = false)) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 2.dp)) {
                        Icon(Icons.Default.Person, contentDescription = null, Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                        Text(" ФИО: ", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(user.fullName, style = MaterialTheme.typography.bodyMedium, maxLines = 1)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 2.dp)) {
                        Icon(Icons.Default.Phone, contentDescription = null, Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                        Text(" Тел.: ", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(user.phone, style = MaterialTheme.typography.bodySmall)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, contentDescription = null, Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                        Text(" Инструктор: ", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(assignedInstructorName, style = MaterialTheme.typography.bodySmall)
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    FilledTonalIconButton(onClick = onCall, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Call, contentDescription = "Позвонить", Modifier.size(18.dp))
                    }
                    FilledTonalIconButton(onClick = onMessage, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Email, contentDescription = "Сообщение", Modifier.size(18.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Удалить", Modifier.size(18.dp))
                    }
                    Switch(checked = user.isActive, onCheckedChange = onActiveChange, modifier = Modifier.padding(start = 4.dp))
                }
            }
        }
    }
}

@Composable
private fun PendingUserCard(
    user: User,
    onActivate: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant),
        shape = MaterialTheme.shapes.small,
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(Modifier.weight(1f, fill = false)) {
                Text(
                    text = user.fullName.ifBlank { "Имя не указано" },
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                )
                Text(
                    text = user.email,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )
                Text(
                    text = user.phone,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = if (user.role == "instructor") "Инстр." else "Курсант",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(end = 4.dp),
                )
                FilledTonalIconButton(
                    onClick = onActivate,
                    modifier = Modifier.size(36.dp),
                ) {
                    Icon(Icons.Default.Check, contentDescription = "Активировать", Modifier.size(18.dp))
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp),
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Удалить", Modifier.size(18.dp))
                }
            }
        }
    }
}
