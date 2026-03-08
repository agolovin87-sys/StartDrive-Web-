package com.example.startdrive.ui.auth

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.startdrive.data.model.User
import com.example.startdrive.data.repository.AuthRepository
import com.example.startdrive.data.repository.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AuthUiState(
    val user: User? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
)

private const val PREFS_STAY_IN_APP = "auth_prefs"
private const val KEY_STAY_IN_APP = "stay_in_app"

class AuthViewModel(
    private val authRepository: AuthRepository,
    private val application: Application,
) : ViewModel() {

    private val prefs = application.getSharedPreferences(PREFS_STAY_IN_APP, Application.MODE_PRIVATE)

    private fun saveStayInApp(value: Boolean) {
        prefs.edit().putBoolean(KEY_STAY_IN_APP, value).apply()
    }

    private fun getStayInApp(): Boolean = prefs.getBoolean(KEY_STAY_IN_APP, true)

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    val currentUserId: StateFlow<String?> = authRepository.authStateFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), authRepository.currentUserId)

    init {
        viewModelScope.launch {
            currentUserId.collect { uid ->
                if (uid != null) {
                    if (!getStayInApp()) {
                        authRepository.signOut()
                        _uiState.value = AuthUiState()
                        return@collect
                    }
                    val user = authRepository.getCurrentUser()
                    if (user == null) {
                        authRepository.signOut()
                        _uiState.value = AuthUiState(error = "Аккаунт удалён администратором.")
                    } else {
                        _uiState.value = _uiState.value.copy(user = user, error = null)
                    }
                } else {
                    val preservedError = _uiState.value.error
                    _uiState.value = AuthUiState(error = preservedError)
                }
            }
        }
    }

    fun signIn(email: String, password: String, stayInApp: Boolean) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            authRepository.signIn(email, password)
                .onSuccess {
                    saveStayInApp(stayInApp)
                    val user = authRepository.getCurrentUser()
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        user = user,
                        error = if (user == null) "Профиль не найден в базе. Обратитесь к администратору." else null,
                    )
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = it.message ?: "Ошибка входа",
                    )
                }
        }
    }

    fun register(fullName: String, email: String, phone: String, password: String, role: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            authRepository.register(fullName, email, phone, password, role)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        user = authRepository.getCurrentUser(),
                    )
                }
                .onFailure {
                    val message = when {
                        it.message?.contains("email address is already in use", ignoreCase = true) == true ->
                            "Этот email уже зарегистрирован. Войдите или восстановите пароль."
                        it.message?.contains("at least 6 characters", ignoreCase = true) == true ->
                            "Пароль должен быть не менее 6 символов."
                        it.message?.contains("invalid", ignoreCase = true) == true ->
                            "Неверный формат email или пароля."
                        it.message?.contains("PERMISSION_DENIED", ignoreCase = true) == true ->
                            "Нет доступа к базе. Проверьте правила Firestore."
                        else -> it.message ?: "Ошибка регистрации"
                    }
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = message,
                    )
                }
        }
    }

    fun signOut() {
        authRepository.currentUserId?.let { uid ->
            try { ChatRepository.setPresence(uid, false) } catch (_: Exception) { }
        }
        authRepository.signOut()
        _uiState.value = AuthUiState()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun refreshUser() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(user = authRepository.getCurrentUser())
        }
    }
}
