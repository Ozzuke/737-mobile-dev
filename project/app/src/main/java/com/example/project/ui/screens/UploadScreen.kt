package com.example.project.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.project.ui.UiState
import com.example.project.ui.viewmodels.GlucoseViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadScreen(
    onBackClick: () -> Unit = {},
    viewModel: GlucoseViewModel = viewModel()
) {
    val c = MaterialTheme.colorScheme
    val context = LocalContext.current
    val uploadStatus by viewModel.uploadStatus.collectAsState()

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.uploadToRemote(it)
        }
    }

    // Show feedback toast on upload result
    LaunchedEffect(uploadStatus) {
        when (val state = uploadStatus) {
            is UiState.Success -> {
                Toast.makeText(context, "Upload successful!", Toast.LENGTH_SHORT).show()
                viewModel.resetUploadStatus() // Reset state after showing toast
            }
            is UiState.Error -> {
                Toast.makeText(context, "Upload failed: ${state.message}", Toast.LENGTH_LONG).show()
                viewModel.resetUploadStatus() // Reset state
            }
            else -> {
                // Idle or Loading states
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Upload") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = c.onPrimary
                        )
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
            modifier = Modifier
                .padding(inner)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (uploadStatus is UiState.Loading) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text("Uploading, please wait...")
            } else {
                Button(onClick = { filePickerLauncher.launch("*/*") }) {
                    Icon(Icons.Filled.Add, contentDescription = "Upload")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Upload from CSV")
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Upload a CSV file with glucose readings. The file will be saved locally and sent to the remote server for analysis.",
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
