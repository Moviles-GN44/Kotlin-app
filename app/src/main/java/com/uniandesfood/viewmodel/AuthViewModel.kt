package com.uniandesfood.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uniandesfood.data.model.User
import com.uniandesfood.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
    val user: User? = null,
    val generalError: String? = null
)

class AuthViewModel(
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun onEmailChange(newEmail: String) {
        val trimmed = if (newEmail.length > 50) newEmail.take(50) else newEmail
        _uiState.value = _uiState.value.copy(
            email = trimmed,
            emailError = authRepository.validateEmail(trimmed),
            generalError = null
        )
    }

    fun onPasswordChange(newPassword: String) {
        val sanitized = if (newPassword.length > 32) newPassword.take(32) else newPassword
        _uiState.value = _uiState.value.copy(
            password = sanitized,
            passwordError = authRepository.validatePassword(sanitized),
            generalError = null
        )
    }

    fun login() {
        val current = _uiState.value
        val emailErr = authRepository.validateEmail(current.email)
        val passErr = authRepository.validatePassword(current.password)

        if (emailErr != null || passErr != null) {
            _uiState.value = current.copy(emailError = emailErr, passwordError = passErr)
            return
        }

        _uiState.value = current.copy(isLoading = true, generalError = null)
        viewModelScope.launch {
            val result = authRepository.login(current.email, current.password)
            result.onSuccess { user ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isAuthenticated = true,
                    user = user,
                    generalError = null
                )
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    generalError = error.message ?: "Authentication failed"
                )
            }
        }
    }

    fun logout() {
        authRepository.logout()
        _uiState.value = AuthUiState()
    }
}
