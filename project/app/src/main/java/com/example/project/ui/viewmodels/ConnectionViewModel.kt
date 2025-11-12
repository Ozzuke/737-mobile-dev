package com.example.project.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.project.domain.model.ConnectionCode
import com.example.project.domain.model.ConnectedClinician
import com.example.project.domain.model.ConnectedPatient
import com.example.project.domain.repository.AuthRepository
import com.example.project.ui.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for connection management between patients and clinicians
 */
class ConnectionViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    // Connection code state (for patients)
    private val _connectionCodeState = MutableStateFlow<UiState<ConnectionCode>>(UiState.Idle)
    val connectionCodeState: StateFlow<UiState<ConnectionCode>> = _connectionCodeState.asStateFlow()

    // Connected patients state (for clinicians)
    private val _connectedPatientsState = MutableStateFlow<UiState<List<ConnectedPatient>>>(UiState.Idle)
    val connectedPatientsState: StateFlow<UiState<List<ConnectedPatient>>> = _connectedPatientsState.asStateFlow()

    // Connected clinicians state (for patients)
    private val _connectedCliniciansState = MutableStateFlow<UiState<List<ConnectedClinician>>>(UiState.Idle)
    val connectedCliniciansState: StateFlow<UiState<List<ConnectedClinician>>> = _connectedCliniciansState.asStateFlow()

    // Connect to patient state (for clinicians)
    private val _connectToPatientState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val connectToPatientState: StateFlow<UiState<Unit>> = _connectToPatientState.asStateFlow()

    /**
     * Generate a new connection code (Patient only)
     */
    fun generateConnectionCode() {
        viewModelScope.launch {
            _connectionCodeState.value = UiState.Loading

            val result = authRepository.generateConnectionCode()

            _connectionCodeState.value = if (result.isSuccess) {
                UiState.Success(result.getOrNull()!!)
            } else {
                val exception = result.exceptionOrNull() ?: Exception("Failed to generate code")
                UiState.Error(exception.message ?: "Failed to generate code", exception)
            }
        }
    }

    /**
     * Get connected clinicians (Patient only)
     */
    fun fetchConnectedClinicians() {
        viewModelScope.launch {
            _connectedCliniciansState.value = UiState.Loading

            val result = authRepository.getConnectedClinicians()

            _connectedCliniciansState.value = if (result.isSuccess) {
                val clinicians = result.getOrNull()!!
                if (clinicians.isEmpty()) {
                    UiState.Empty
                } else {
                    UiState.Success(clinicians)
                }
            } else {
                val exception = result.exceptionOrNull() ?: Exception("Failed to fetch clinicians")
                UiState.Error(exception.message ?: "Failed to fetch clinicians", exception)
            }
        }
    }

    /**
     * Disconnect a clinician (Patient only)
     */
    fun disconnectClinician(clinicianId: String) {
        viewModelScope.launch {
            val result = authRepository.disconnectClinician(clinicianId)

            if (result.isSuccess) {
                // Refresh the list
                fetchConnectedClinicians()
            }
        }
    }

    /**
     * Connect to a patient using code (Clinician only)
     */
    fun connectToPatient(connectionCode: String) {
        viewModelScope.launch {
            _connectToPatientState.value = UiState.Loading

            val result = authRepository.connectToPatient(connectionCode)

            _connectToPatientState.value = if (result.isSuccess) {
                UiState.Success(Unit)
            } else {
                val exception = result.exceptionOrNull() ?: Exception("Failed to connect")
                UiState.Error(exception.message ?: "Failed to connect", exception)
            }
        }
    }

    /**
     * Get connected patients (Clinician only)
     */
    fun fetchConnectedPatients() {
        viewModelScope.launch {
            _connectedPatientsState.value = UiState.Loading

            val result = authRepository.getConnectedPatients()

            _connectedPatientsState.value = if (result.isSuccess) {
                val patients = result.getOrNull()!!
                if (patients.isEmpty()) {
                    UiState.Empty
                } else {
                    UiState.Success(patients)
                }
            } else {
                val exception = result.exceptionOrNull() ?: Exception("Failed to fetch patients")
                UiState.Error(exception.message ?: "Failed to fetch patients", exception)
            }
        }
    }

    /**
     * Disconnect a patient (Clinician only)
     */
    fun disconnectPatient(patientId: String) {
        viewModelScope.launch {
            val result = authRepository.disconnectPatient(patientId)

            if (result.isSuccess) {
                // Refresh the list
                fetchConnectedPatients()
            }
        }
    }

    /**
     * Reset connect to patient state
     */
    fun resetConnectState() {
        _connectToPatientState.value = UiState.Idle
    }
}
