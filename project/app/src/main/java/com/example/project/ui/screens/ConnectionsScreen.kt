package com.example.project.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.project.domain.model.ClinicianProfile
import com.example.project.domain.model.PatientProfile
import com.example.project.domain.model.User
import com.example.project.ui.UiState
import com.example.project.ui.components.EmptyView
import com.example.project.ui.components.ErrorView
import com.example.project.ui.components.LoadingView
import com.example.project.ui.viewmodels.AuthViewModel
import com.example.project.ui.viewmodels.ConnectionViewModel

/**
 * Connections screen - displays differently for patients vs clinicians
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectionsScreen(
    authViewModel: AuthViewModel,
    connectionViewModel: ConnectionViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()

    LaunchedEffect(currentUser) {
        when (currentUser) {
            is PatientProfile -> connectionViewModel.fetchConnectedClinicians()
            is ClinicianProfile -> connectionViewModel.fetchConnectedPatients()
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Connections") },
                navigationIcon = {
                    TextButton(onClick = onBackClick) {
                        Text("Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        when (currentUser) {
            is PatientProfile -> {
                PatientConnectionsView(
                    connectionViewModel = connectionViewModel,
                    modifier = Modifier.padding(paddingValues)
                )
            }
            is ClinicianProfile -> {
                ClinicianConnectionsView(
                    connectionViewModel = connectionViewModel,
                    modifier = Modifier.padding(paddingValues)
                )
            }
            else -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
private fun PatientConnectionsView(
    connectionViewModel: ConnectionViewModel,
    modifier: Modifier = Modifier
) {
    val connectionCodeState by connectionViewModel.connectionCodeState.collectAsStateWithLifecycle()
    val connectedCliniciansState by connectionViewModel.connectedCliniciansState.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Connection code section
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Share Code with Clinician",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                when (val state = connectionCodeState) {
                    is UiState.Success -> {
                        Text(
                            text = state.data.code,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .padding(vertical = 8.dp)
                                .fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Expires: ${state.data.expiresAt}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }
                    is UiState.Loading -> {
                        CircularProgressIndicator()
                    }
                    is UiState.Error -> {
                        Text(
                            text = state.message,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    else -> {
                        Text(
                            text = "Generate a code to allow a clinician to connect",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { connectionViewModel.generateConnectionCode() },
                    enabled = connectionCodeState !is UiState.Loading
                ) {
                    Text("Generate New Code")
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Connected clinicians list
        Text(
            text = "Connected Clinicians",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        when (val state = connectedCliniciansState) {
            is UiState.Loading -> LoadingView("Loading clinicians...")
            is UiState.Error -> ErrorView(state.message) {
                connectionViewModel.fetchConnectedClinicians()
            }
            is UiState.Empty -> EmptyView("No connected clinicians")
            is UiState.Success -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.data) { clinician ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = clinician.fullName,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                TextButton(
                                    onClick = {
                                        connectionViewModel.disconnectClinician(clinician.id)
                                    }
                                ) {
                                    Text("Disconnect", color = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
            else -> {}
        }
    }
}

@Composable
private fun ClinicianConnectionsView(
    connectionViewModel: ConnectionViewModel,
    modifier: Modifier = Modifier
) {
    val connectToPatientState by connectionViewModel.connectToPatientState.collectAsStateWithLifecycle()
    val connectedPatientsState by connectionViewModel.connectedPatientsState.collectAsStateWithLifecycle()
    var connectionCode by remember { mutableStateOf("") }

    LaunchedEffect(connectToPatientState) {
        if (connectToPatientState is UiState.Success) {
            connectionCode = ""
            connectionViewModel.fetchConnectedPatients()
            connectionViewModel.resetConnectState()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Connect to patient section
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Connect to Patient",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = connectionCode,
                    onValueChange = { connectionCode = it },
                    label = { Text("Patient Connection Code") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = connectToPatientState !is UiState.Loading
                )

                if (connectToPatientState is UiState.Error) {
                    Text(
                        text = (connectToPatientState as UiState.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        if (connectionCode.isNotBlank()) {
                            connectionViewModel.connectToPatient(connectionCode)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = connectToPatientState !is UiState.Loading && connectionCode.isNotBlank()
                ) {
                    if (connectToPatientState is UiState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Connect")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Connected patients list
        Text(
            text = "Connected Patients",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        when (val state = connectedPatientsState) {
            is UiState.Loading -> LoadingView("Loading patients...")
            is UiState.Error -> ErrorView(state.message) {
                connectionViewModel.fetchConnectedPatients()
            }
            is UiState.Empty -> EmptyView("No connected patients")
            is UiState.Success -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.data) { patient ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = patient.nickname,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                TextButton(
                                    onClick = {
                                        connectionViewModel.disconnectPatient(patient.id)
                                    }
                                ) {
                                    Text("Disconnect", color = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
            else -> {}
        }
    }
}
