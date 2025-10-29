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
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.project.ui.UiState
import com.example.project.ui.viewmodels.GlucoseViewModel
import com.example.project.R

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
                Toast.makeText(context, context.getString(R.string.upload_success), Toast.LENGTH_SHORT).show()
                viewModel.resetUploadStatus() // Reset state after showing toast
            }
            is UiState.Error -> {
                Toast.makeText(context, context.getString(R.string.upload_failed, state.message), Toast.LENGTH_LONG).show()
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
                title = { Text(stringResource(id = R.string.upload_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.back_button_description),
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
                .padding(dimensionResource(id = R.dimen.padding_medium)),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (uploadStatus is UiState.Loading) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.spacing_large)))
                Text(stringResource(id = R.string.upload_uploading))
            } else {
                Button(onClick = { filePickerLauncher.launch("*/*") }) {
                    Icon(Icons.Filled.Add, contentDescription = stringResource(id = R.string.upload_button_description))
                    Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.spacing_small)))
                    Text(stringResource(id = R.string.upload_button_label))
                }
                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.spacing_large)))
                Text(
                    text = stringResource(id = R.string.upload_instructions),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
