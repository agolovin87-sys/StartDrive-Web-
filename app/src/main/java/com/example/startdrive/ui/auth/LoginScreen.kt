package com.example.startdrive.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.startdrive.R
import com.example.startdrive.ui.theme.LocalScreenDimensions

@Composable
fun LoginScreen(
    uiState: AuthUiState,
    onSignIn: (email: String, password: String, stayInApp: Boolean) -> Unit,
    onNavigateToRegister: () -> Unit,
    onClearError: () -> Unit,
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var stayInApp by rememberSaveable { mutableStateOf(false) }
    val dims = LocalScreenDimensions.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(dims.horizontalPadding)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(dims.mediumPadding))
                .background(Color.White, RoundedCornerShape(dims.mediumPadding))
                .padding(dims.mediumPadding),
        ) {
            Image(
                painter = painterResource(R.drawable.ic_logo_startdrive),
                contentDescription = "StartDrive",
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 180.dp),
                contentScale = ContentScale.Fit,
            )
        }
        Spacer(modifier = Modifier.height(dims.smallPadding))
        Text(
            text = "Вход в систему",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(dims.verticalPadding * 2))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        )
        Spacer(modifier = Modifier.height(dims.verticalPadding))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Пароль") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        )

        Spacer(modifier = Modifier.height(dims.mediumPadding))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "Оставаться в приложении",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Switch(
                checked = stayInApp,
                onCheckedChange = { stayInApp = it },
            )
        }

        uiState.error?.let { err ->
            Spacer(modifier = Modifier.height(dims.smallPadding))
            Text(text = err, color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(dims.horizontalPadding))

        if (uiState.isLoading) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = { onSignIn(email, password, stayInApp) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Войти")
            }
        }

        Spacer(modifier = Modifier.height(dims.verticalPadding))
        TextButton(onClick = onNavigateToRegister) {
            Text("Нет аккаунта? Зарегистрироваться")
        }
    }
}
