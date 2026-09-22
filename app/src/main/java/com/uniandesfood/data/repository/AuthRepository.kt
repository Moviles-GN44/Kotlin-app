package com.uniandesfood.data.repository

import com.uniandesfood.data.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.regex.Pattern

class AuthRepository {

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val emailPattern = Pattern.compile(
        "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$"
    )

    fun validateEmail(email: String): String? {
        val trimmed = email.trim()
        if (trimmed.isEmpty()) return "Email address cannot be empty"
        if (trimmed.length > 50) return "Email must not exceed 50 characters"
        if (!emailPattern.matcher(trimmed).matches()) return "Please enter a valid email format"
        return null
    }

    fun validatePassword(password: String): String? {
        if (password.isEmpty()) return "Password cannot be empty"
        if (password.contains(" ")) return "Password cannot contain spaces"
        if (password.length < 6) return "Password must be at least 6 characters"
        if (password.length > 32) return "Password must not exceed 32 characters"
        return null
    }

    fun login(email: String, password: String): Result<User> {
        val emailError = validateEmail(email)
        if (emailError != null) return Result.failure(IllegalArgumentException(emailError))

        val passwordError = validatePassword(password)
        if (passwordError != null) return Result.failure(IllegalArgumentException(passwordError))

        // Simulated robust authentication
        val user = User(
            uid = "usr_${System.currentTimeMillis()}",
            email = email.trim().lowercase(),
            displayName = email.substringBefore("@").replace(".", " ").replaceFirstChar { it.uppercase() }
        )
        _currentUser.value = user
        return Result.success(user)
    }

    fun logout() {
        _currentUser.value = null
    }
}
