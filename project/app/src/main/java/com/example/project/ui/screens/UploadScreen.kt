package com.example.project.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.project.data.repository.GlucoseCsvRepository
import com.example.project.ui.viewmodels.GlucoseViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadScreen(
    onBackClick: () -> Unit = {},
    viewModel: GlucoseViewModel = viewModel()
) {
    val c = MaterialTheme.colorScheme
    val context = LocalContext.current
    val repository = remember { GlucoseCsvRepository() }
    val coroutineScope = rememberCoroutineScope()

    var uploadStatus by remember { mutableStateOf<String?>(null) }
    var isUploading by remember { mutableStateOf(false) }

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            isUploading = true
            coroutineScope.launch {
                try {
                    val readings = repository.parseGlucoseData(context, it)
                    if (readings.isNotEmpty()) {
                        viewModel.updateReadings(readings)
                        uploadStatus = "Successfully uploaded ${readings.size} glucose readings"
                    } else {
                        uploadStatus = "No glucose readings found in file"
                    }
                } catch (e: Exception) {
                    uploadStatus = "Error parsing file: ${e.message}"
                } finally {
                    isUploading = false
                }
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
        Box(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = { filePicker.launch("text/*") },
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(64.dp),
                    contentPadding = PaddingValues(vertical = 18.dp, horizontal = 24.dp),
                    enabled = !isUploading
                ) {
                    if (isUploading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text(
                            "Upload CSV file",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }

                uploadStatus?.let { status ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .padding(top = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (status.startsWith("Error"))
                                MaterialTheme.colorScheme.errorContainer
                            else
                                MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Text(
                            text = status,
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}
