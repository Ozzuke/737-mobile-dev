package com.example.project.ui.viewmodels

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.project.domain.model.GlucoseReading
import com.example.project.domain.repository.CgmApiRepository
import com.example.project.domain.repository.GlucoseRepository
import com.example.project.domain.repository.GlucoseCsvRepository
import com.example.project.ui.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class GlucoseViewModel(
    application: Application,
    private val glucoseRepository: GlucoseRepository,
    private val csvRepository: GlucoseCsvRepository,
    private val cgmApiRepository: CgmApiRepository
) : AndroidViewModel(application) {

    private val _readings = MutableStateFlow<List<GlucoseReading>>(emptyList())
    val readings: StateFlow<List<GlucoseReading>> = _readings

    private val _latestReading = MutableStateFlow<GlucoseReading?>(null)
    val latestReading: StateFlow<GlucoseReading?> = _latestReading

    private val _uploadStatus = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val uploadStatus: StateFlow<UiState<Unit>> = _uploadStatus

    init {
        loadLatestReading()
        loadAllReadings()
    }

    private fun loadLatestReading() {
        viewModelScope.launch {
            glucoseRepository.latestReading.collect { reading ->
                _latestReading.value = reading
            }
        }
    }

    private fun loadAllReadings() {
        viewModelScope.launch {
            glucoseRepository.allReadings.collect { readingsList ->
                _readings.value = readingsList
            }
        }
    }

    fun uploadCsv(uri: Uri) {
        viewModelScope.launch {
            _uploadStatus.value = UiState.Loading
            try {
                val readings = csvRepository.parseGlucoseData(getApplication(), uri)
                glucoseRepository.insertReadings(readings)
                loadLatestReading() // Refresh local data
                _uploadStatus.value = UiState.Success(Unit)
            } catch (error: Exception) {
                _uploadStatus.value = UiState.Error(error.message ?: "Failed to read CSV", error)
            }
        }
    }

    fun uploadToRemote(uri: Uri) {
        viewModelScope.launch {
            _uploadStatus.value = UiState.Loading
            // We can enhance this later to get nickname and unit from the user
            val remoteResult = cgmApiRepository.uploadDataset(uri, null, null)
            remoteResult.onSuccess {
                // The local uploadCsv function already shows a success state.
                // We could add a different state for remote success if needed.
                _uploadStatus.value = UiState.Success(Unit)
            }.onFailure { error ->
                _uploadStatus.value = UiState.Error(error.message ?: "Failed to upload to remote", error)
            }
        }
    }

    fun resetUploadStatus() {
        _uploadStatus.value = UiState.Idle
    }
}
