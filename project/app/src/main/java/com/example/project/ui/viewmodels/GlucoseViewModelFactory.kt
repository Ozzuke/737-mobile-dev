package com.example.project.ui.viewmodels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.project.domain.repository.CgmApiRepository
import com.example.project.domain.repository.GlucoseRepository
import com.example.project.domain.repository.GlucoseCsvRepository

/**
 * Factory for creating GlucoseViewModel with repository dependency injection
 */
class GlucoseViewModelFactory(
    private val application: Application,
    private val glucoseRepository: GlucoseRepository,
    private val csvRepository: GlucoseCsvRepository,
    private val cgmApiRepository: CgmApiRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GlucoseViewModel::class.java)) {
            return GlucoseViewModel(application, glucoseRepository, csvRepository, cgmApiRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
