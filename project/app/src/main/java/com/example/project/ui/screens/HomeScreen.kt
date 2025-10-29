package com.example.project.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.project.domain.model.RatingCategory
import com.example.project.ui.components.GlucoseGraphCard
import com.example.project.ui.viewmodels.MainViewModel

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
    onStatusClick: (String) -> Unit = {}, // Navigate to analysis with dataset ID
    onGraphClick: (String, String) -> Unit = { _, _ -> }, // Navigate to metrics with dataset ID and preset
    onOfflineClick: () -> Unit = {}, // Show disclaimer when offline
    mainViewModel: MainViewModel
) {
    val homeState by mainViewModel.homeState.collectAsState()
    var selectedPreset by remember { mutableStateOf("24h") }

    // Trigger initial data fetch
    LaunchedEffect(Unit) {
        mainViewModel.fetchLatestData()
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
                        // Current glucose + status indicator (side by side)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Left: Current glucose value
                            GlucoseValueCard(
                                glucose = homeState.latestGlucose,
                                unit = homeState.datasetData?.unit ?: "mmol/L",
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
                                modifier = Modifier.weight(1f)
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
                                    modifier = Modifier.weight(1f)
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
                            }
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
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clickable(onClick = onClick)
            .height(140.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
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
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (glucose != null) {
                Text(
                    text = "%.1f".format(glucose),
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = unit,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text(
                    text = "-- $unit",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
    modifier: Modifier = Modifier
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
            "⚠"
        )
        RatingCategory.URGENT -> Triple(
            Color(0xFFF44336), // Red
            "URGENT",
            "!"
        )
        else -> Triple(
            MaterialTheme.colorScheme.surfaceVariant,
            "UNKNOWN",
            "?"
        )
    }

    Card(
        modifier = modifier
            .clickable(onClick = onClick)
            .height(140.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = color)
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
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

/**
 * Status indicator badge showing GOOD/ATTENTION/URGENT
 */
@Composable
private fun StatusIndicator(status: RatingCategory?) {
    val (color, text) = when (status) {
        RatingCategory.GOOD -> Pair(
            MaterialTheme.colorScheme.primaryContainer,
            "GOOD"
        )
        RatingCategory.ATTENTION -> Pair(
            MaterialTheme.colorScheme.secondaryContainer,
            "ATTENTION"
        )
        RatingCategory.URGENT -> Pair(
            MaterialTheme.colorScheme.errorContainer,
            "URGENT"
        )
        else -> Pair(
            MaterialTheme.colorScheme.surfaceVariant,
            "UNKNOWN"
        )
    }

    Box(
        modifier = Modifier
            .background(color, RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
    }
}
