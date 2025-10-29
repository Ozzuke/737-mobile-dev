package com.example.project.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.project.data.local.PreferencesRepository
import com.example.project.domain.repository.CgmApiRepository

/**
 * Factory for creating MainViewModel with dependencies
 */
class MainViewModelFactory(
    private val repository: CgmApiRepository,
    private val preferencesRepository: PreferencesRepository,
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repository, preferencesRepository, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
