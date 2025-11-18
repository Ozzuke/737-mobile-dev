package com.example.project.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.project.R
import com.example.project.domain.model.DatasetSummary
import com.example.project.ui.components.EmptyView
import com.example.project.ui.components.ErrorView
import com.example.project.ui.components.LoadingView
import com.example.project.ui.UiState
import com.example.project.ui.viewmodels.CgmApiViewModel

/**
 * Screen displaying datasets from the CGM API
 * Shows loading state, error handling, and list of datasets
 * - Click dataset to view analysis
 * - Long press to set as active dataset for home screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatasetsScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onDatasetClick: (String) -> Unit = {},
    onSetActiveDataset: (String) -> Unit = {},
    onDeleteDataset: (String) -> Unit = {},
    viewModel: CgmApiViewModel = viewModel(),
    patientId: String? = null,
    isClinician: Boolean = false
) {
    val datasetsState by viewModel.datasetsState.collectAsState()
    val colorScheme = MaterialTheme.colorScheme

    // Fetch datasets when screen loads
    LaunchedEffect(patientId) {
        if (isClinician && patientId == null) {
            viewModel.clearDatasetsState()
        } else {
            viewModel.fetchDatasets(patientId)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(id = R.string.datasets_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.back_button_description),
                            tint = colorScheme.onPrimary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.retryFetchDatasets(patientId) }) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = stringResource(id = R.string.refresh_button_description),
                            tint = colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = colorScheme.primary,
                    titleContentColor = colorScheme.onPrimary,
                    navigationIconContentColor = colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Box(
            modifier = modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            if (isClinician && patientId == null) {
                ClinicianDatasetsEmptyState()
            } else {
                when (val state = datasetsState) {
                    is UiState.Idle -> { }
                    is UiState.Loading -> { LoadingView(stringResource(id = R.string.loading_datasets)) }
                    is UiState.Success -> {
                        DatasetsList(
                            datasets = state.data,
                            onDatasetClick = onDatasetClick,
                            onSetActiveDataset = onSetActiveDataset,
                            onDeleteDataset = onDeleteDataset
                        )
                    }
                    is UiState.Error -> {
                        ErrorView(
                            message = state.message,
                            onRetry = { viewModel.retryFetchDatasets(patientId) }
                        )
                    }
                    is UiState.Empty -> { EmptyView(message = stringResource(id = R.string.no_datasets_available)) }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
@Suppress("ASSIGNED_VALUE_IS_NEVER_READ")
private fun DatasetsList(
    datasets: List<DatasetSummary>,
    onDatasetClick: (String) -> Unit,
    onSetActiveDataset: (String) -> Unit,
    onDeleteDataset: (String) -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(datasets, key = { it.datasetId }) { dataset ->
            DatasetCard(
                dataset = dataset,
                onClick = { onDatasetClick(dataset.datasetId) },
                onLongClick = { onSetActiveDataset(dataset.datasetId) },
                onDelete = { showDeleteDialog = dataset.datasetId }
            )
        }
    }

    // Delete confirmation dialog
    showDeleteDialog?.let { datasetId ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Delete Dataset?") },
            text = { Text("Are you sure you want to delete this dataset from the server? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteDataset(datasetId)
                        showDeleteDialog = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DatasetCard(
    dataset: DatasetSummary,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
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
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = dataset.nickname,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Start: ${dataset.startDate.take(10)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "End: ${dataset.endDate.take(10)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${dataset.rowCount} readings",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "${dataset.samplingIntervalMin} min interval",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete dataset",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun ClinicianDatasetsEmptyState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Select a patient to view their datasets",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
    }
}
