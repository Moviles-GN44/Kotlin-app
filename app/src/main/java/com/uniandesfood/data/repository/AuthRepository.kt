package com.uniandesfood.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.uniandesfood.data.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import java.util.regex.Pattern

class AuthRepository(
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private val _currentUser = MutableStateFlow<User?>(mapFirebaseUser(firebaseAuth.currentUser))
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val emailPattern = Pattern.compile(
        "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$"
    )

    init {
        firebaseAuth.addAuthStateListener { auth ->
            _currentUser.value = mapFirebaseUser(auth.currentUser)
        }
    }

    private fun mapFirebaseUser(fbUser: FirebaseUser?): User? {
        if (fbUser == null) return null
        val defaultName = fbUser.email?.substringBefore("@")
            ?.replace(".", " ")
            ?.split(" ")
            ?.joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
            ?: "Student"
        return User(
            uid = fbUser.uid,
            email = fbUser.email ?: "",
            displayName = if (!fbUser.displayName.isNullOrBlank()) fbUser.displayName!! else defaultName
        )
    }

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

    suspend fun login(email: String, password: String): Result<User> {
        val emailError = validateEmail(email)
        if (emailError != null) return Result.failure(IllegalArgumentException(emailError))

        val passwordError = validatePassword(password)
        if (passwordError != null) return Result.failure(IllegalArgumentException(passwordError))

        return try {
            val authResult = firebaseAuth.signInWithEmailAndPassword(email.trim().lowercase(), password).await()
            val fbUser = authResult.user ?: throw IllegalStateException("User not found after sign-in")
            val user = mapFirebaseUser(fbUser) ?: User(uid = fbUser.uid, email = email.trim().lowercase())
            _currentUser.value = user
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(email: String, password: String, displayName: String = ""): Result<User> {
        val emailError = validateEmail(email)
        if (emailError != null) return Result.failure(IllegalArgumentException(emailError))

        val passwordError = validatePassword(password)
        if (passwordError != null) return Result.failure(IllegalArgumentException(passwordError))

        return try {
            val authResult = firebaseAuth.createUserWithEmailAndPassword(email.trim().lowercase(), password).await()
            val fbUser = authResult.user ?: throw IllegalStateException("User creation failed")
            val name = if (displayName.isNotBlank()) displayName else {
                email.substringBefore("@")
                    .replace(".", " ")
                    .split(" ")
                    .joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
            }
            val user = User(
                uid = fbUser.uid,
                email = fbUser.email ?: email.trim().lowercase(),
                displayName = name,
                preferredBuilding = "ML"
            )
            try {
                firestore.collection("users").document(user.uid).set(user.toMap()).await()
            } catch (_: Exception) {
                // Non-fatal if offline or rules delay
            }
            _currentUser.value = user
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() {
        firebaseAuth.signOut()
        _currentUser.value = null
    }
}
