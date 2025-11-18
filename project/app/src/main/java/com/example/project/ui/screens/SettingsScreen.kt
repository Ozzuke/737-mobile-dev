package com.example.project.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.Flow
import com.example.project.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    getPreferredUnit: () -> Flow<String?>,
    setPreferredUnit: (String) -> Unit,
    darkModeEnabled: Boolean,
    onDarkModeChanged: (Boolean) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val currentPref by getPreferredUnit().collectAsState(initial = null)

    var selectedUnit by remember(currentPref) { mutableStateOf(currentPref ?: "mmol/L") }
    var darkModeToggle by remember { mutableStateOf(darkModeEnabled) }
    LaunchedEffect(darkModeEnabled) { darkModeToggle = darkModeEnabled }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(id = R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.back_button_description),
                            tint = colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = colorScheme.primary,
                    titleContentColor = colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(text = stringResource(id = R.string.settings_units_header), style = MaterialTheme.typography.titleMedium)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = stringResource(id = R.string.settings_units_label), modifier = Modifier.weight(1f))
                // Simple unit selector
                SegmentedButton(
                    options = listOf("mmol/L", "mg/dL"),
                    selected = selectedUnit,
                    onSelected = { new ->
                        selectedUnit = new
                        setPreferredUnit(new)
                    }
                )
            }
            HorizontalDivider()
            Text(text = stringResource(id = R.string.settings_theme_header), style = MaterialTheme.typography.titleMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = stringResource(id = R.string.settings_dark_mode_label), style = MaterialTheme.typography.bodyLarge)
                    Text(
                        text = stringResource(id = R.string.settings_dark_mode_description),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = darkModeToggle,
                    onCheckedChange = { enabled ->
                        darkModeToggle = enabled
                        onDarkModeChanged(enabled)
                    }
                )
            }
        }
    }
}

@Composable
private fun SegmentedButton(
    options: List<String>,
    selected: String,
    onSelected: (String) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { option ->
            FilterChip(
                selected = option == selected,
                onClick = { onSelected(option) },
                label = { Text(option) }
            )
        }
    }
}
