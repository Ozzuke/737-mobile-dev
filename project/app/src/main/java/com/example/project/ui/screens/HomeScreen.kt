package com.example.project.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.project.ui.components.WidgetTile
import com.example.project.ui.theme.ProjectTheme
import com.example.project.ui.viewmodels.GlucoseViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onAddClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onInfoClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    viewModel: GlucoseViewModel = viewModel()
) {
    val latestReading by viewModel.latestReading.collectAsState()

    Scaffold(
        topBar = {
            val c = MaterialTheme.colorScheme
            CenterAlignedTopAppBar(
                title = { Text("Dashboard") },
                navigationIcon = {
                    IconButton(onClick = onAddClick) {
                        Icon(Icons.Outlined.Add, contentDescription = "Add", tint = c.onPrimary)
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
        Column(
            Modifier
                .padding(inner)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                // Glucose Reading Widget
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    tonalElevation = 2.dp
                ) {
                    Column(
                        modifier = Modifier
                            .height(140.dp)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Latest Glucose",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        latestReading?.let { reading ->
                            Text(
                                text = "${reading.glucoseValue}",
                                style = MaterialTheme.typography.displayMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "mmol/L",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = reading.timestamp.substringAfter("T").substringBefore("+"),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        } ?: run {
                            Text(
                                text = "No Data",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Upload CSV",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                WidgetTile(modifier = Modifier.weight(1f), height = 140.dp, label = "Example Insulin")
            }
            WidgetTile(modifier = Modifier.fillMaxWidth(), height = 200.dp, label = "Example (wide)")
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewHome() {
    ProjectTheme {
        HomeScreen()
    }
}
