package com.example.project.ui.viewmodels

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.project.data.local.GlucoseDatabase
import com.example.project.data.repository.GlucoseDatabaseRepository
import com.example.project.domain.model.GlucoseReading
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch

import com.example.project.data.repository.GlucoseCsvRepository
import com.example.project.domain.repository.GlucoseRepository

class GlucoseViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: GlucoseRepository
    private val csvRepository: com.example.project.domain.repository.GlucoseCsvRepository

    val latestReading: StateFlow<GlucoseReading?>
    val allReadings: StateFlow<List<GlucoseReading>>

    init {
        val database = GlucoseDatabase.getInstance(application)
        repository = GlucoseDatabaseRepository(database.glucoseDao())
        csvRepository = GlucoseCsvRepository()

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
