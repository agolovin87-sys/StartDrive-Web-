package com.example.startdrive.ui.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch
import androidx.compose.ui.unit.dp
import com.example.startdrive.data.model.User
import com.example.startdrive.data.repository.UserRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminBalanceTab(
    adminId: String,
    snackbarHostState: SnackbarHostState = SnackbarHostState(),
    onNotification: (String) -> Unit = {},
) {
    val userRepo = remember { UserRepository() }
    val instructors by userRepo.usersByRole("instructor").collectAsState(initial = emptyList())
    val cadets by userRepo.usersByRole("cadet").collectAsState(initial = emptyList())
    var showInstructorSheet by remember { mutableStateOf(false) }
    var showCadetSheet by remember { mutableStateOf(false) }
    var selectedUser by remember { mutableStateOf<User?>(null) }
    var amount by remember { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()

    Column(Modifier.padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { showInstructorSheet = true }) { Text("Выбрать инструктора") }
            Button(onClick = { showCadetSheet = true }) { Text("Выбрать курсанта") }
        }
        selectedUser?.let { u ->
            Spacer(Modifier.height(16.dp))
            Text("Текущий баланс: ${u.balance} талонов", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = amount.toString(),
                onValueChange = { amount = it.toIntOrNull() ?: 0 },
                label = { Text("Количество талонов") },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = {
                    scope.launch {
                        runCatching { userRepo.updateBalance(u.id, "credit", amount, adminId) }
                            .onSuccess {
                                val msg = "Зачислено $amount талонов: ${u.fullName}"
                                snackbarHostState.showSnackbar(msg)
                                onNotification(msg)
                            }
                            .onFailure {
                                val msg = "Ошибка: ${it.message ?: "не удалось зачислить"}"
                                snackbarHostState.showSnackbar(msg)
                                onNotification(msg)
                            }
                    }
                }) { Text("Зачислить (+N)") }
                Button(onClick = {
                    scope.launch {
                        runCatching { userRepo.updateBalance(u.id, "debit", amount, adminId) }
                            .onSuccess {
                                val msg = "Списано $amount талонов: ${u.fullName}"
                                snackbarHostState.showSnackbar(msg)
                                onNotification(msg)
                            }
                            .onFailure {
                                val msg = "Ошибка: ${it.message ?: "не удалось списать"}"
                                snackbarHostState.showSnackbar(msg)
                                onNotification(msg)
                            }
                    }
                }) { Text("Списать (-N)") }
                Button(onClick = {
                    scope.launch {
                        runCatching { userRepo.updateBalance(u.id, "set", amount, adminId) }
                            .onSuccess {
                                val msg = "Баланс изменён на $amount талонов: ${u.fullName}"
                                snackbarHostState.showSnackbar(msg)
                                onNotification(msg)
                            }
                            .onFailure {
                                val msg = "Ошибка: ${it.message ?: "не удалось изменить"}"
                                snackbarHostState.showSnackbar(msg)
                                onNotification(msg)
                            }
                    }
                }) { Text("Изменить на (= N)") }
            }
        }

        if (showInstructorSheet) {
            ModalBottomSheet(
                onDismissRequest = { showInstructorSheet = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                dragHandle = { BottomSheetDefaults.DragHandle() },
            ) {
                LazyColumn(Modifier.padding(16.dp)) {
                    items(instructors) { u ->
                        Button(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                selectedUser = u
                                amount = 0
                                showInstructorSheet = false
                            },
                        ) { Text("${u.fullName} (${u.balance})") }
                    }
                }
            }
        }
        if (showCadetSheet) {
            ModalBottomSheet(
                onDismissRequest = { showCadetSheet = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                dragHandle = { BottomSheetDefaults.DragHandle() },
            ) {
                LazyColumn(Modifier.padding(16.dp)) {
                    items(cadets) { u ->
                        Button(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                selectedUser = u
                                amount = 0
                                showCadetSheet = false
                            },
                        ) { Text("${u.fullName} (${u.balance})") }
                    }
                }
            }
        }
    }
}
