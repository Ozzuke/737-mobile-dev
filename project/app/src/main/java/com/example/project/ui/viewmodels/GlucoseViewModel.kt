package com.example.project.ui.viewmodels

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.project.domain.model.GlucoseReading
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch

import com.example.project.domain.repository.GlucoseRepository

class GlucoseViewModel(
    application: Application,
    private val repository: GlucoseRepository,
    private val csvRepository: com.example.project.domain.repository.GlucoseCsvRepository
) : AndroidViewModel(application) {

    val latestReading: StateFlow<GlucoseReading?>
    val allReadings: StateFlow<List<GlucoseReading>>

    init {

        // Convert Flow to StateFlow for UI observation
        latestReading = repository.latestReading.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

        allReadings = repository.allReadings.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    fun uploadCsv(uri: Uri) {
        viewModelScope.launch {
            val readings = csvRepository.parseGlucoseData(getApplication(), uri)
            repository.insertReadings(readings)
        }
    }

    fun updateReadings(readings: List<GlucoseReading>) {
        viewModelScope.launch {
            repository.insertReadings(readings)
        }
    }

    fun deleteAllReadings() {
        viewModelScope.launch {
            repository.deleteAllReadings()
        }
    }
}
