package com.example.project.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class UserProfile(
    val name: String,
    val email: String,
    val memberSince: String
)

class ProfileViewModel : ViewModel() {

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile

    init {
        fetchUserProfile()
    }

    private fun fetchUserProfile() {
        viewModelScope.launch {
            // In a real app, you would fetch this from a data source.
            // For now, we'll use mock data.
            _userProfile.value = UserProfile(
                name = "John Doe",
                email = "john.doe@example.com",
                memberSince = "Jan 1, 2023"
            )
        }
    }
}
