package com.example.project.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class UserProfile(
    val name: String,
    val email: String,
    val memberSince: String,
    val dateOfBirth: String,
    val phone: String,
    val cgmSensor: String,
    val notificationsEnabled: Boolean
)

class ProfileViewModel : ViewModel() {

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile

    init {
        fetchUserProfile()
    }

    private fun fetchUserProfile() {
        viewModelScope.launch {
            // In a real app, you would fetch this from a data source (e.g., DataStore, Room, or API).
            // For now, we'll use mock data to demonstrate data binding.
            _userProfile.value = UserProfile(
                name = "John Doe",
                email = "john.doe@example.com",
                memberSince = "Jan 1, 2023",
                dateOfBirth = "2000-01-01",
                phone = "+372 5555 5555",
                cgmSensor = "Dexcom G7",
                notificationsEnabled = true
            )
        }
    }
}
