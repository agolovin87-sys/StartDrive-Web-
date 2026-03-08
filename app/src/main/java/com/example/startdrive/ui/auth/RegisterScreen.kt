package com.example.startdrive.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import com.example.startdrive.R
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.startdrive.ui.theme.LocalScreenDimensions

@Composable
fun RegisterScreen(
    uiState: AuthUiState,
    onRegister: (fullName: String, email: String, phone: String, password: String, role: String) -> Unit,
    onNavigateBack: () -> Unit,
    onClearError: () -> Unit,
) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("cadet") }
    val dims = LocalScreenDimensions.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(dims.horizontalPadding)
            .verticalScroll(rememberScrollState()),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(dims.mediumPadding))
                .background(Color.White)
                .padding(dims.mediumPadding),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_logo_startdrive),
                    contentDescription = "StartDrive",
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 120.dp),
                    contentScale = ContentScale.Fit,
                )
            }
        }
        Spacer(modifier = Modifier.height(dims.smallPadding))
        Text(
            text = "Регистрация",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(modifier = Modifier.height(dims.horizontalPadding))

        OutlinedTextField(
            value = fullName,
            onValueChange = { fullName = it },
            label = { Text("ФИО") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(dims.mediumPadding))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        )
        Spacer(modifier = Modifier.height(dims.mediumPadding))
        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Номер телефона") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
        )
        Spacer(modifier = Modifier.height(dims.mediumPadding))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Пароль") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        )

        Spacer(modifier = Modifier.height(dims.verticalPadding))
        Text("Роль:", style = MaterialTheme.typography.labelLarge)
        Column(verticalArrangement = Arrangement.spacedBy(dims.smallPadding)) {
            androidx.compose.foundation.layout.Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = role == "cadet", onClick = { role = "cadet" })
                Text("Курсант", modifier = Modifier.padding(start = dims.smallPadding))
            }
            androidx.compose.foundation.layout.Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = role == "instructor", onClick = { role = "instructor" })
                Text("Инструктор", modifier = Modifier.padding(start = dims.smallPadding))
            }
        }

        uiState.error?.let { err ->
            Spacer(modifier = Modifier.height(dims.smallPadding))
            Text(text = err, color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(dims.horizontalPadding))

        if (uiState.isLoading) {
            CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally))
        } else {
            Button(
                onClick = { onRegister(fullName, email, phone, password, role) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Зарегистрироваться")
            }
        }

        Spacer(modifier = Modifier.height(dims.mediumPadding))
        TextButton(onClick = onNavigateBack) {
            Text("Назад к входу")
        }
    }
}
