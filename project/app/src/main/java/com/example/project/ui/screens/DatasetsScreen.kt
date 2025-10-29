package com.example.project.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.project.domain.model.DatasetSummary
import com.example.project.ui.components.EmptyView
import com.example.project.ui.components.ErrorView
import com.example.project.ui.components.LoadingView
import com.example.project.ui.UiState
import com.example.project.ui.viewmodels.CgmApiViewModel
import com.example.project.R

/**
 * Screen displaying datasets from the CGM API
 * Shows loading state, error handling, and list of datasets
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatasetsScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onDatasetClick: (String) -> Unit = {},
    viewModel: CgmApiViewModel = viewModel()
) {
    val datasetsState by viewModel.datasetsState.collectAsState()
    val colorScheme = MaterialTheme.colorScheme

    // Fetch datasets when screen loads
    LaunchedEffect(Unit) {
        viewModel.fetchDatasets()
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
                    IconButton(onClick = { viewModel.retryFetchDatasets() }) {
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
            when (val state = datasetsState) {
                is UiState.Idle -> {
                    // Initial state - could show a welcome message
                }
                is UiState.Loading -> {
                    LoadingView(stringResource(id = R.string.loading_datasets))
                }
                is UiState.Success -> {
                    DatasetsList(
                        datasets = state.data,
                        onDatasetClick = onDatasetClick
                    )
                }
                is UiState.Error -> {
                    ErrorView(
                        message = state.message,
                        onRetry = { viewModel.retryFetchDatasets() }
                    )
                }
                is UiState.Empty -> {
                    EmptyView(message = stringResource(id = R.string.no_datasets_available))
                }
            }
        }
    }
}

@Composable
private fun DatasetsList(
    datasets: List<DatasetSummary>,
    onDatasetClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(datasets, key = { it.datasetId }) { dataset ->
            DatasetCard(
                dataset = dataset,
                onClick = { onDatasetClick(dataset.datasetId) }
            )
        }
    }
}

@Composable
private fun DatasetCard(
    dataset: DatasetSummary,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
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
                        text = stringResource(id = R.string.dataset_start_label, dataset.startDate.take(10)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = stringResource(id = R.string.dataset_end_label, dataset.endDate.take(10)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = stringResource(id = R.string.dataset_readings_count, dataset.rowCount),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = stringResource(id = R.string.dataset_interval_label, dataset.samplingIntervalMin),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
