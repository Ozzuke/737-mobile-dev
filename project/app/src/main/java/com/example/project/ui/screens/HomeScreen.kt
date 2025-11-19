package com.example.project.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.project.R
import com.example.project.domain.model.ClinicianProfile
import com.example.project.domain.model.RatingCategory
import com.example.project.ui.components.GlucoseGraphCard
import com.example.project.ui.UiState
import com.example.project.ui.viewmodels.AuthViewModel
import com.example.project.ui.viewmodels.ConnectionViewModel
import com.example.project.ui.viewmodels.MainViewModel
import java.util.Locale

/**
 * Home screen showing latest glucose data from API
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onAddClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onInfoClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onConnectionsClick: () -> Unit = {},
    onStatusClick: (String) -> Unit = {}, // Navigate to analysis with dataset ID
    onGraphClick: (String, String) -> Unit = { _, _ -> }, // Navigate to metrics with dataset ID and preset
    onOfflineClick: () -> Unit = {}, // Show disclaimer when offline
    mainViewModel: MainViewModel,
    authViewModel: AuthViewModel,
    connectionViewModel: ConnectionViewModel
) {
    val homeState by mainViewModel.homeState.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()
    val connectedPatientsState by connectionViewModel.connectedPatientsState.collectAsState()
    var selectedPreset by remember { mutableStateOf("24h") }

    // Check if user is a clinician
    val isClinician = currentUser is ClinicianProfile
    LaunchedEffect(isClinician) {
        mainViewModel.setClinicianMode(isClinician)
    }

    // Fetch connected patients for clinicians
    LaunchedEffect(isClinician) {
        if (isClinician) {
            connectionViewModel.fetchConnectedPatients()
        }
    }

    // Trigger initial data fetch only when not clinician or patient selected
    LaunchedEffect(isClinician, homeState.selectedPatientId) {
        if (!isClinician || homeState.selectedPatientId != null) {
            mainViewModel.fetchLatestData()
        }
    }

    Scaffold(
        topBar = {
            val c = MaterialTheme.colorScheme
            CenterAlignedTopAppBar(
                title = { Text("Dashboard") },
                navigationIcon = {
                    Row {
                        // Offline indicator
                        if (!homeState.isOnline) {
                            IconButton(onClick = onOfflineClick) {
                                Icon(
                                    Icons.Filled.Warning,
                                    contentDescription = "Offline",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                        IconButton(onClick = onAddClick) {
                            Icon(Icons.Outlined.Add, contentDescription = "Add", tint = c.onPrimary)
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onConnectionsClick) {
                        Icon(Icons.Outlined.Share, contentDescription = "Connections", tint = c.onPrimary)
                    }
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Outlined.Settings, contentDescription = "Settings", tint = c.onPrimary)
                    }
                    IconButton(onClick = onInfoClick) {
                        Icon(Icons.Outlined.Info, contentDescription = "Info", tint = c.onPrimary)
                    }
                    IconButton(onClick = onProfileClick) {
                        Icon(Icons.Outlined.AccountCircle, contentDescription = "Profile", tint = c.onPrimary)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = c.primary,
                    titleContentColor = c.onPrimary,
                    navigationIconContentColor = c.onPrimary,
                    actionIconContentColor = c.onPrimary
                )
            )
        }
    ) { inner ->
        Box(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize()
        ) {
            when {
                homeState.isLoading -> {
                    // Loading state
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Loading latest data...", style = MaterialTheme.typography.bodyLarge)
                    }
                }
                homeState.error != null && homeState.latestDataset == null -> {
                    // Error state with no data
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Error",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = homeState.error ?: "Unknown error",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { mainViewModel.retry() }) {
                            Icon(Icons.Outlined.Refresh, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Retry")
                        }
                    }
                }
                else -> {
                    // Success state - show data
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Patient selector for clinicians
                        if (isClinician && connectedPatientsState is UiState.Success) {
                            val patients = (connectedPatientsState as UiState.Success).data
                            PatientSelectorCard(
                                patients = patients,
                                selectedPatientId = homeState.selectedPatientId,
                                onPatientSelected = { patientId ->
                                    mainViewModel.setSelectedPatientId(patientId)
                                }
                            )
                            if (homeState.selectedPatientId == null) {
                                ClinicianEmptyState()
                                return@Column
                            }
                        }
                        // Current glucose + status indicator (side by side)
                        // Compute a shared status color to use for both cards so they match
                        val statusColor = when (homeState.status) {
                            RatingCategory.GOOD -> Color(0xFF4CAF50) // Green
                            RatingCategory.ATTENTION -> Color(0xFFFFC107) // Yellow/Orange
                            RatingCategory.URGENT -> Color(0xFFF44336) // Red
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                        // Choose a readable content color: white on dark status colors, dark on light surfaces
                        val statusContentColor = if (statusColor.luminance() < 0.5f) {
                            Color.White
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Left: Current glucose value
                            GlucoseValueCard(
                                glucose = homeState.latestGlucose,
                                unit = (homeState.preferredUnit ?: homeState.datasetData?.unit ?: ""),
                                sourceUnit = homeState.datasetData?.unit,
                                onClick = {
                                    homeState.latestDataset?.let { dataset ->
                                        onGraphClick(dataset.datasetId, selectedPreset)
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            )

                            // Right: Status indicator
                            StatusCard(
                                status = homeState.status,
                                onClick = {
                                    homeState.latestDataset?.let { dataset ->
                                        onStatusClick(dataset.datasetId)
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                containerColor = statusColor,
                                contentColor = statusContentColor
                            )
                        }

                        // Timeframe selector
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("24h", "7d", "14d").forEach { preset ->
                                FilterChip(
                                    selected = selectedPreset == preset,
                                    onClick = {
                                        selectedPreset = preset
                                        mainViewModel.changePreset(preset)
                                    },
                                    label = { Text(preset) },
                                    modifier = Modifier.weight(1f),
                                    colors = FilterChipDefaults.filterChipColors(
                                        labelColor = MaterialTheme.colorScheme.onSurface,
                                        selectedLabelColor = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                            }
                        }

                        // Glucose graph
                        GlucoseGraphCard(
                            datasetData = homeState.datasetData,
                            preset = selectedPreset,
                            onClick = {
                                homeState.latestDataset?.let { dataset ->
                                    onGraphClick(dataset.datasetId, selectedPreset)
                                }
                            },
                            sourceUnit = homeState.datasetData?.unit,
                            displayUnit = (homeState.preferredUnit ?: homeState.datasetData?.unit)
                        )

                        // Error message if any (but still showing data)
                        if (homeState.error != null) {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer
                                )
                            ) {
                                Text(
                                    text = homeState.error ?: "",
                                    modifier = Modifier.padding(16.dp),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Card showing current glucose value (left card)
 */
@Composable
private fun GlucoseValueCard(
    glucose: Double?,
    unit: String,
    sourceUnit: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    val displayGlucose = glucose?.let { convertGlucose(it, sourceUnit, unit) }

    Card(
        modifier = modifier
            .clickable(onClick = onClick)
            .height(140.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Current",
                style = MaterialTheme.typography.labelMedium,
                color = contentColor
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (displayGlucose != null) {
                Text(
                    text = "%.1f".format(displayGlucose),
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                )
                Text(
                    text = unit,
                    style = MaterialTheme.typography.bodyLarge,
                    color = contentColor
                )
            } else {
                Text(
                    text = "-- $unit",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                )
            }
        }
    }
}

/**
 * Card showing status indicator (right card)
 */
@Composable
private fun StatusCard(
    status: RatingCategory?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color? = null,
    contentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    val (color, text, icon) = when (status) {
        RatingCategory.GOOD -> Triple(
            Color(0xFF4CAF50), // Green
            "GOOD",
            "✓"
        )
        RatingCategory.ATTENTION -> Triple(
            Color(0xFFFFC107), // Yellow/Orange
            "ATTENTION",
            "!"
        )
        RatingCategory.URGENT -> Triple(
            Color(0xFFF44336), // Red
            "URGENT",
            "X"
        )
        else -> Triple(
            MaterialTheme.colorScheme.surfaceVariant,
            stringResource(id = R.string.ai_analysis_label),
            "?"
        )
    }

    val finalContainer = containerColor ?: color

    Card(
        modifier = modifier
            .clickable(onClick = onClick)
            .height(140.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = finalContainer)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = icon,
                style = MaterialTheme.typography.displayLarge,
                fontSize = 48.sp,
                color = contentColor,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = contentColor,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}


/**
 * Patient selector dropdown for clinicians
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PatientSelectorCard(
    patients: List<com.example.project.domain.model.ConnectedPatient>,
    selectedPatientId: String?,
    onPatientSelected: (String?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val displayValue = when {
        selectedPatientId == null -> "Select Patient"
        else -> patients.find { it.id == selectedPatientId }?.nickname ?: "Select Patient"
    }
    val options = listOf<Pair<String?, String>>(null to "Select patient") + patients.map { it.id to it.nickname }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Viewing Patient Data",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = displayValue,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Patient") },
                    trailingIcon = {
                        Icon(
                            Icons.Filled.ArrowDropDown,
                            contentDescription = "Select patient"
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    options.forEach { (id, label) ->
                        DropdownMenuItem(
                            text = { Text(label) },
                            onClick = {
                                onPatientSelected(id)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ClinicianEmptyState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Select a patient to view data",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .height(180.dp)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text("No data to display", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

// Helper conversion
private fun convertGlucose(value: Double, fromUnit: String?, toUnit: String?): Double {
    val from = (fromUnit ?: "").lowercase(Locale.US)
    val to = (toUnit ?: "").lowercase(Locale.US)
    if (from.isEmpty() || to.isEmpty() || from == to) return value
    return if (from.contains("mg/dl") && to.contains("mmol")) {
        value / 18.0
    } else if (from.contains("mmol") && to.contains("mg/dl")) {
        value * 18.0
    } else value
}
