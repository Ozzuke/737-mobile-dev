package com.example.project.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.project.ui.components.GlucoseWidget
import com.example.project.ui.components.WidgetTile
import com.example.project.ui.theme.ProjectTheme
import com.example.project.ui.viewmodels.GlucoseViewModel
import com.example.project.R

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

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.uploadCsv(it) }
    }

    Scaffold(
        topBar = {
            val c = MaterialTheme.colorScheme
            CenterAlignedTopAppBar(
                title = { Text(stringResource(id = R.string.dashboard_title)) },
                navigationIcon = {
                    IconButton(onClick = onAddClick) {
                        Icon(Icons.Outlined.Add, contentDescription = stringResource(id = R.string.add_button_description), tint = c.onPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Outlined.Settings, contentDescription = stringResource(id = R.string.settings_button_description), tint = c.onPrimary)
                    }
                    IconButton(onClick = onInfoClick) {
                        Icon(Icons.Outlined.Info, contentDescription = stringResource(id = R.string.info_button_description), tint = c.onPrimary)
                    }
                    IconButton(onClick = onProfileClick) {
                        Icon(Icons.Outlined.AccountCircle, contentDescription = stringResource(id = R.string.profile_button_description), tint = c.onPrimary)
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
                GlucoseWidget(
                    modifier = Modifier.weight(1f),
                    height = 140.dp,
                    latestReading = latestReading,
                    onClick = { filePickerLauncher.launch("text/*") }
                )

                WidgetTile(modifier = Modifier.weight(1f), height = 140.dp, label = stringResource(id = R.string.example_insulin_label))
            }
            WidgetTile(modifier = Modifier.fillMaxWidth(), height = 200.dp, label = stringResource(id = R.string.example_wide_label))
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
