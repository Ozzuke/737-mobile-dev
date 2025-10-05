package com.example.project.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.example.project.domain.model.GlucoseReading
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class GlucoseViewModel : ViewModel() {
    private val _latestReading = MutableStateFlow<GlucoseReading?>(null)
    val latestReading: StateFlow<GlucoseReading?> = _latestReading.asStateFlow()

    private val _allReadings = MutableStateFlow<List<GlucoseReading>>(emptyList())
    val allReadings: StateFlow<List<GlucoseReading>> = _allReadings.asStateFlow()

    fun updateReadings(readings: List<GlucoseReading>) {
        _allReadings.value = readings
        _latestReading.value = readings.lastOrNull()
    }
}
