package com.example.project.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.project.domain.model.User
import com.example.project.domain.repository.AuthRepository
import com.example.project.ui.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for authentication operations
 */
class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    // Authentication state
    private val _authState = MutableStateFlow<UiState<User>>(UiState.Idle)
    val authState: StateFlow<UiState<User>> = _authState.asStateFlow()

    // Current user
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    // Is authenticated
    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    init {
        checkAuthentication()
    }

    /**
     * Check if user is authenticated on init
     */
    private fun checkAuthentication() {
        _isAuthenticated.value = authRepository.isAuthenticated()
        _currentUser.value = authRepository.getCurrentUser()

        // If authenticated but no cached user, fetch profile
        if (_isAuthenticated.value && _currentUser.value == null) {
            fetchProfile()
        }
    }

    /**
     * Register a new patient
     */
    fun registerPatient(
        username: String,
        password: String,
        nickname: String,
        birthYear: Int,
        diabetesDiagnosisMonth: Int,
        diabetesDiagnosisYear: Int
    ) {
        viewModelScope.launch {
            _authState.value = UiState.Loading

            val result = authRepository.registerPatient(
                username,
                password,
                nickname,
                birthYear,
                diabetesDiagnosisMonth,
                diabetesDiagnosisYear
            )

            _authState.value = when {
                result.isSuccess -> {
                    UiState.Success(result.getOrNull()!!)
                }
                else -> {
                    val exception = result.exceptionOrNull() ?: Exception("Registration failed")
                    UiState.Error(exception.message ?: "Registration failed", exception)
                }
            }
        }
    }

    /**
     * Register a new clinician
     */
    fun registerClinician(
        username: String,
        password: String,
        fullName: String
    ) {
        viewModelScope.launch {
            _authState.value = UiState.Loading

            val result = authRepository.registerClinician(username, password, fullName)

            _authState.value = when {
                result.isSuccess -> {
                    UiState.Success(result.getOrNull()!!)
                }
                else -> {
                    val exception = result.exceptionOrNull() ?: Exception("Registration failed")
                    UiState.Error(exception.message ?: "Registration failed", exception)
                }
            }
        }
    }

    /**
     * Login with username and password
     */
    fun login(username: String, password: String) {
        viewModelScope.launch {
            _authState.value = UiState.Loading

            val result = authRepository.login(username, password)

            if (result.isSuccess) {
                val (user, _) = result.getOrNull()!!
                _currentUser.value = user
                _isAuthenticated.value = true
                _authState.value = UiState.Success(user)
            } else {
                val exception = result.exceptionOrNull() ?: Exception("Login failed")
                _authState.value = UiState.Error(
                    exception.message ?: "Login failed",
                    exception
                )
            }
        }
    }

    /**
     * Logout
     */
    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _currentUser.value = null
            _isAuthenticated.value = false
            _authState.value = UiState.Idle
        }
    }

    /**
     * Fetch user profile
     */
    fun fetchProfile() {
        viewModelScope.launch {
            val result = authRepository.getProfile()

            if (result.isSuccess) {
                _currentUser.value = result.getOrNull()
            }
        }
    }

    /**
     * Update profile
     */
    fun updateProfile(nickname: String?, fullName: String?) {
        viewModelScope.launch {
            _authState.value = UiState.Loading

            val result = authRepository.updateProfile(nickname, fullName)

            if (result.isSuccess) {
                _currentUser.value = result.getOrNull()
                _authState.value = UiState.Success(result.getOrNull()!!)
            } else {
                val exception = result.exceptionOrNull() ?: Exception("Update failed")
                _authState.value = UiState.Error(
                    exception.message ?: "Update failed",
                    exception
                )
            }
        }
    }

    /**
     * Update password
     */
    fun updatePassword(currentPassword: String, newPassword: String) {
        viewModelScope.launch {
            _authState.value = UiState.Loading

            val result = authRepository.updatePassword(currentPassword, newPassword)

            _authState.value = if (result.isSuccess) {
                UiState.Success(_currentUser.value!!)
            } else {
                val exception = result.exceptionOrNull() ?: Exception("Password update failed")
                UiState.Error(exception.message ?: "Password update failed", exception)
            }
        }
    }

    /**
     * Delete account
     */
    fun deleteAccount() {
        viewModelScope.launch {
            _authState.value = UiState.Loading

            val result = authRepository.deleteAccount()

            if (result.isSuccess) {
                _currentUser.value = null
                _isAuthenticated.value = false
                _authState.value = UiState.Idle
            } else {
                val exception = result.exceptionOrNull() ?: Exception("Delete failed")
                _authState.value = UiState.Error(
                    exception.message ?: "Delete failed",
                    exception
                )
            }
        }
    }

    /**
     * Reset auth state to idle
     */
    fun resetAuthState() {
        _authState.value = UiState.Idle
    }
}
