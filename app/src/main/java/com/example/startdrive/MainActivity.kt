package com.example.startdrive

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.startdrive.data.repository.AppSettings
import com.example.startdrive.data.repository.AuthRepository
import com.example.startdrive.data.repository.ChatRepository
import com.example.startdrive.data.repository.UserRepository
import com.example.startdrive.ui.navigation.StartDriveNavGraph
import com.example.startdrive.ui.theme.StartDriveTheme
import coil.Coil
import coil.ImageLoader
import coil.decode.SvgDecoder
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Coil.setImageLoader(
            ImageLoader.Builder(this).components { add(SvgDecoder.Factory()) }.build()
        )
        enableEdgeToEdge()
        val authRepository = AuthRepository()
        val userRepository = UserRepository()
        val chatRepository = ChatRepository
        setContent {
            val context = LocalContext.current
            val notificationLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission(),
            ) { isGranted ->
                // Можно обновить UI или повторить запрос при следующем входе
            }
            LaunchedEffect(Unit) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                        notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
            }
            val darkTheme = AppSettings.isDarkTheme(context, isSystemInDarkTheme())
            StartDriveTheme(darkTheme = darkTheme, dynamicColor = false) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    val authViewModel: com.example.startdrive.ui.auth.AuthViewModel = viewModel(
                        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                                @Suppress("UNCHECKED_CAST")
                                return com.example.startdrive.ui.auth.AuthViewModel(authRepository, application) as T
                            }
                        }
                    )
                    val user by authViewModel.uiState.collectAsState()
                    LaunchedEffect(user.user?.id) {
                        user.user?.id?.let { uid ->
                            try {
                                val token = FirebaseMessaging.getInstance().token.await()
                                userRepository.updateFcmToken(uid, token)
                            } catch (_: Exception) { }
                            try {
                                chatRepository.setPresence(uid, true)
                            } catch (_: Exception) { }
                        }
                    }
                    StartDriveNavGraph(authViewModel = authViewModel)
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        AuthRepository().currentUserId?.let { uid ->
            ChatRepository.setPresence(uid, false)
        }
    }

    override fun onResume() {
        super.onResume()
        AuthRepository().currentUserId?.let { uid ->
            ChatRepository.setPresence(uid, true)
        }
    }
}
