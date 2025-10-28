package com.example.project.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.project.domain.repository.CgmApiRepository

/**
 * Factory for creating CgmApiViewModel with repository dependency injection
 */
class CgmApiViewModelFactory(
    private val repository: CgmApiRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CgmApiViewModel::class.java)) {
            return CgmApiViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
