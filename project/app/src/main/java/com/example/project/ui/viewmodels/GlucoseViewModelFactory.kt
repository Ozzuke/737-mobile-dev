package com.example.project.ui.viewmodels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.project.domain.repository.GlucoseRepository

/**
 * Factory for creating GlucoseViewModel with repository dependency injection
 */
class GlucoseViewModelFactory(
    private val application: Application,
    private val glucoseRepository: GlucoseRepository,
    private val csvRepository: com.example.project.domain.repository.GlucoseCsvRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GlucoseViewModel::class.java)) {
            return GlucoseViewModel(application, glucoseRepository, csvRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
