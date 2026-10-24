package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.EnglishLevel
import com.example.data.model.ThemeColorOption
import com.example.data.model.ThemeModeOption
import com.example.data.model.UserProfile
import com.example.data.remote.AuthStateResult
import com.example.data.remote.FirebaseAuthService
import com.example.data.repository.SpeakUpRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

data class UserSessionInfo(
    val uid: String,
    val email: String,
    val displayName: String,
    val photoUrl: String?
)

sealed class AuthUiState {
    data object Idle : AuthUiState()
    data object Loading : AuthUiState()
    data class Authenticated(val user: UserSessionInfo, val isProfileCompleted: Boolean) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val authService = FirebaseAuthService(application)
    private val repository = SpeakUpRepository(application)

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _currentUserProfile = MutableStateFlow<UserProfile?>(null)
    val currentUserProfile: StateFlow<UserProfile?> = _currentUserProfile.asStateFlow()

    private var currentActiveUid: String = ""

    init {
        checkInitialAuthState()
    }

    private fun checkInitialAuthState() {
        try {
            val user = authService.currentUser
            if (user != null) {
                currentActiveUid = user.uid
                viewModelScope.launch {
                    val profile = repository.getUserProfile(user.uid)
                    _currentUserProfile.value = profile
                    val savedName = profile?.name?.trim()?.takeIf { it.isNotBlank() && !it.equals("Learner", ignoreCase = true) && !it.equals("Guest", ignoreCase = true) }
                    val effectiveName = savedName ?: user.displayName?.takeIf { it.isNotBlank() } ?: "Learner"
                    val session = UserSessionInfo(
                        uid = user.uid,
                        email = user.email ?: "",
                        displayName = effectiveName,
                        photoUrl = profile?.photoUrl?.takeIf { it.isNotBlank() } ?: user.photoUrl?.toString()
                    )
                    val isCompleted = profile?.isProfileCompleted == true
                    _uiState.value = AuthUiState.Authenticated(session, isCompleted)
                }
            } else {
                _uiState.value = AuthUiState.Idle
            }
        } catch (e: Throwable) {
            Log.w("AuthViewModel", "Initial auth state check bypass: ${e.message}")
            _uiState.value = AuthUiState.Idle
        }
    }

    fun signInWithGoogle(activityContext: Context? = null) {
        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            try {
                when (val result = authService.signInWithGoogle(activityContext)) {
                    is AuthStateResult.Success -> {
                        currentActiveUid = result.uid
                        var profile = repository.getUserProfile(result.uid)
                        if (profile == null) {
                            val initialName = result.displayName.trim().ifBlank { "Learner" }
                            profile = UserProfile(
                                uid = result.uid,
                                name = initialName,
                                email = result.email,
                                photoUrl = result.photoUrl ?: "",
                                isProfileCompleted = false
                            )
                            repository.saveUserProfile(profile)
                        }
                        _currentUserProfile.value = profile
                        val savedName = profile.name.trim().takeIf { it.isNotBlank() && !it.equals("Learner", ignoreCase = true) && !it.equals("Guest", ignoreCase = true) }
                        val effectiveName = savedName ?: result.displayName.trim().ifBlank { "Learner" }
                        val session = UserSessionInfo(
                            uid = result.uid,
                            email = result.email,
                            displayName = effectiveName,
                            photoUrl = result.photoUrl ?: profile.photoUrl
                        )
                        _uiState.value = AuthUiState.Authenticated(session, profile.isProfileCompleted)
                    }
                    is AuthStateResult.Cancelled -> {
                        _uiState.value = AuthUiState.Idle
                    }
                    is AuthStateResult.Error -> {
                        _uiState.value = AuthUiState.Error(result.message)
                    }
                }
            } catch (e: Throwable) {
                _uiState.value = AuthUiState.Error(e.localizedMessage ?: "Google Sign-In encountered an issue.")
            }
        }
    }

    fun signInWithEmail(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            _uiState.value = AuthUiState.Error("Please enter both email and password")
            return
        }
        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            try {
                when (val result = authService.signInWithEmail(email, pass)) {
                    is AuthStateResult.Success -> {
                        currentActiveUid = result.uid
                        val session = UserSessionInfo(
                            uid = result.uid,
                            email = result.email,
                            displayName = result.displayName,
                            photoUrl = result.photoUrl
                        )
                        var profile = repository.getUserProfile(result.uid)
                        if (profile == null) {
                            profile = UserProfile(
                                uid = result.uid,
                                name = result.displayName.ifBlank { email.substringBefore("@") },
                                email = email,
                                isProfileCompleted = false
                            )
                            repository.saveUserProfile(profile)
                        }
                        _currentUserProfile.value = profile
                        _uiState.value = AuthUiState.Authenticated(session, profile.isProfileCompleted)
                    }
                    is AuthStateResult.Cancelled -> {
                        _uiState.value = AuthUiState.Idle
                    }
                    is AuthStateResult.Error -> {
                        _uiState.value = AuthUiState.Error(result.message)
                    }
                }
            } catch (e: Throwable) {
                _uiState.value = AuthUiState.Error(e.localizedMessage ?: "Email sign-in failed")
            }
        }
    }

    fun signUpWithEmail(name: String, email: String, pass: String) {
        if (email.isBlank() || pass.isBlank() || name.isBlank()) {
            _uiState.value = AuthUiState.Error("Please fill in all fields")
            return
        }
        if (pass.length < 6) {
            _uiState.value = AuthUiState.Error("Password must be at least 6 characters")
            return
        }
        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            try {
                when (val result = authService.signUpWithEmail(email, pass, name)) {
                    is AuthStateResult.Success -> {
                        currentActiveUid = result.uid
                        val session = UserSessionInfo(
                            uid = result.uid,
                            email = result.email,
                            displayName = result.displayName,
                            photoUrl = result.photoUrl
                        )
                        val profile = UserProfile(
                            uid = result.uid,
                            name = name.trim().ifBlank { "Learner" },
                            email = email.trim(),
                            isProfileCompleted = false
                        )
                        repository.saveUserProfile(profile)
                        _currentUserProfile.value = profile
                        _uiState.value = AuthUiState.Authenticated(session, false)
                    }
                    is AuthStateResult.Cancelled -> {
                        _uiState.value = AuthUiState.Idle
                    }
                    is AuthStateResult.Error -> {
                        _uiState.value = AuthUiState.Error(result.message)
                    }
                }
            } catch (e: Throwable) {
                _uiState.value = AuthUiState.Error(e.localizedMessage ?: "Account creation failed")
            }
        }
    }

    fun continueAsGuest() {
        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            val guestUid = "guest_user"
            currentActiveUid = guestUid
            val session = UserSessionInfo(
                uid = guestUid,
                email = "guest@speakup.ai",
                displayName = "Guest Learner",
                photoUrl = null
            )
            var profile = repository.getUserProfile(guestUid)
            if (profile == null) {
                profile = UserProfile(
                    uid = guestUid,
                    name = "Guest Learner",
                    email = "guest@speakup.ai",
                    englishLevel = EnglishLevel.BEGINNER,
                    dailyGoalMinutes = 15,
                    isProfileCompleted = true
                )
                repository.saveUserProfile(profile)
            }
            _currentUserProfile.value = profile
            _uiState.value = AuthUiState.Authenticated(session, profile.isProfileCompleted)
        }
    }

    fun sendPasswordReset(email: String, onComplete: (Boolean, String) -> Unit) {
        if (email.isBlank()) {
            onComplete(false, "Please enter your email address")
            return
        }
        viewModelScope.launch {
            val result = authService.sendPasswordReset(email)
            if (result.isSuccess) {
                onComplete(true, "Password reset email sent to $email")
            } else {
                onComplete(false, result.exceptionOrNull()?.localizedMessage ?: "Password reset is unavailable offline.")
            }
        }
    }

    fun completeProfile(
        name: String,
        englishLevel: EnglishLevel,
        dailyGoalMinutes: Int,
        practiceDays: List<String>,
        preferredPracticeTime: String,
        sessionDurationMinutes: Int,
        preferredTopics: List<String>,
        photoUrl: String
    ) {
        val uid = currentActiveUid.ifBlank {
            authService.currentUser?.uid ?: "guest_user"
        }
        viewModelScope.launch {
            val existing = repository.getUserProfile(uid)
            val updated = UserProfile(
                uid = uid,
                name = name.ifBlank { existing?.name ?: "Learner" },
                email = existing?.email ?: (authService.currentUser?.email ?: ""),
                photoUrl = photoUrl.ifBlank { existing?.photoUrl ?: "" },
                englishLevel = englishLevel,
                practiceDays = practiceDays,
                preferredPracticeTime = preferredPracticeTime,
                sessionDurationMinutes = sessionDurationMinutes,
                dailyGoalMinutes = dailyGoalMinutes,
                preferredTopics = preferredTopics,
                isProfileCompleted = true,
                updatedAt = System.currentTimeMillis()
            )
            repository.saveUserProfile(updated)
            _currentUserProfile.value = updated
            val session = UserSessionInfo(
                uid = uid,
                email = updated.email,
                displayName = updated.name,
                photoUrl = updated.photoUrl
            )
            _uiState.value = AuthUiState.Authenticated(session, true)
        }
    }

    fun signOut() {
        try {
            authService.signOut()
        } catch (e: Throwable) {
            Log.w("AuthViewModel", "Sign out bypass: ${e.message}")
        }
        _currentUserProfile.value = null
        currentActiveUid = ""
        _uiState.value = AuthUiState.Idle
    }

    fun clearError() {
        if (_uiState.value is AuthUiState.Error) {
            _uiState.value = AuthUiState.Idle
        }
    }
}
