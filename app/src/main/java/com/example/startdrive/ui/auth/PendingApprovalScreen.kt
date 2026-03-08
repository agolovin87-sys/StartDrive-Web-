package com.example.startdrive.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.startdrive.ui.theme.LocalScreenDimensions

@Composable
fun PendingApprovalScreen(
    onSignOut: () -> Unit,
    onCheckAgain: () -> Unit,
) {
    val dims = LocalScreenDimensions.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(dims.horizontalPadding),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Ожидайте подтверждения",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(dims.verticalPadding))
        Text(
            text = "Ваша регистрация отправлена администратору. После одобрения вы получите доступ в личный кабинет.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(dims.verticalPadding * 2))
        Button(
            onClick = onCheckAgain,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Проверить статус")
        }
        Spacer(modifier = Modifier.height(dims.mediumPadding))
        Button(
            onClick = onSignOut,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Выйти")
        }
    }
}
