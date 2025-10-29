package com.example.project.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.project.R
import com.example.project.domain.model.*
import com.example.project.ui.components.EmptyView
import com.example.project.ui.components.ErrorView
import com.example.project.ui.components.LoadingView
import com.example.project.ui.UiState
import com.example.project.ui.viewmodels.CgmApiViewModel
import java.util.Locale

/**
 * Screen displaying analysis results for a specific dataset
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalysisScreen(
    datasetId: String,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    viewModel: CgmApiViewModel = viewModel()
) {
    val analysisState by viewModel.analysisState.collectAsState()
    val colorScheme = MaterialTheme.colorScheme
    var selectedPreset by remember { mutableStateOf("24h") }

    // Fetch analysis when screen loads or preset changes
    LaunchedEffect(datasetId, selectedPreset) {
        viewModel.analyzeDataset(datasetId, selectedPreset)
    }

    // Clear analysis state when leaving screen
    DisposableEffect(Unit) {
        onDispose {
            viewModel.clearAnalysis()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(id = R.string.analysis_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
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
        Box(
            modifier = modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            when (val state = analysisState) {
                is UiState.Idle -> {
                    // Initial state
                }
                is UiState.Loading -> {
                    LoadingView(stringResource(id = R.string.analyzing_dataset))
                }
                is UiState.Success -> {
                    AnalysisContent(
                        analysis = state.data,
                        selectedPreset = selectedPreset,
                        onPresetChange = { selectedPreset = it }
                    )
                }
                is UiState.Error -> {
                    ErrorView(
                        message = state.message,
                        onRetry = { viewModel.analyzeDataset(datasetId, selectedPreset) }
                    )
                }
                is UiState.Empty -> {
                    EmptyView(stringResource(id = R.string.no_analysis_data))
                }
            }
        }
    }
}

@Composable
private fun AnalysisContent(
    analysis: AnalysisResult,
    selectedPreset: String,
    onPresetChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Preset selector
        PresetSelector(
            selectedPreset = selectedPreset,
            onPresetChange = onPresetChange
        )

        // Overall rating
        OverallRatingCard(analysis.overallRating)

        // Warnings (if any)
        if (analysis.warnings.isNotEmpty()) {
            WarningsCard(analysis.warnings)
        }

        // Summary and interpretation
        TextAnalysisCard(
            summary = analysis.summary,
            interpretation = analysis.interpretation
        )

        // Patterns
        if (analysis.patterns.isNotEmpty()) {
            PatternsCard(analysis.patterns)
        }

        // Trends
        if (analysis.trends.isNotEmpty()) {
            TrendsCard(analysis.trends)
        }

        // Extrema
        if (analysis.extrema.isNotEmpty()) {
            ExtremaCard(analysis.extrema)
        }
    }
}

@Composable
private fun PresetSelector(
    selectedPreset: String,
    onPresetChange: (String) -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(id = R.string.time_range_label),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("24h", "7d", "14d").forEach { preset ->
                    FilterChip(
                        selected = selectedPreset == preset,
                        onClick = { onPresetChange(preset) },
                        label = { Text(preset) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun OverallRatingCard(rating: OverallRating) {
    val cardColor = when (rating.category) {
        RatingCategory.GOOD -> MaterialTheme.colorScheme.primaryContainer
        RatingCategory.ATTENTION -> MaterialTheme.colorScheme.secondaryContainer
        RatingCategory.URGENT -> MaterialTheme.colorScheme.errorContainer
        RatingCategory.UNKNOWN -> MaterialTheme.colorScheme.surfaceVariant
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Overall Rating",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                rating.score?.let { score ->
                    Text(
                        text = "Score: ${String.format(Locale.US, "%.1f", score)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Text(
                text = rating.category.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            rating.reasons.forEach { reason ->
                Text(
                    text = "• $reason",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun WarningsCard(warnings: List<String>) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Warnings",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            warnings.forEach { warning ->
                Text(
                    text = "⚠️ $warning",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

@Composable
private fun TextAnalysisCard(summary: String, interpretation: String) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column {
                Text(
                    text = "Summary",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = summary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            HorizontalDivider()
            Column {
                Text(
                    text = "Interpretation",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = interpretation,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun PatternsCard(patterns: List<Pattern>) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Detected Patterns",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            patterns.forEach { pattern ->
                PatternItem(pattern)
                if (pattern != patterns.last()) {
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun PatternItem(pattern: Pattern) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = pattern.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = pattern.severity.name,
                style = MaterialTheme.typography.bodySmall,
                color = when (pattern.severity) {
                    PatternSeverity.HIGH -> MaterialTheme.colorScheme.error
                    PatternSeverity.MODERATE -> MaterialTheme.colorScheme.tertiary
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
        Text(
            text = pattern.summary,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = "${pattern.instances.size} instance(s) detected",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun TrendsCard(trends: List<TrendAnnotation>) {
    Card(
        shape = RoundedCornerShape(12.dp),
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
                text = "Trends",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            trends.forEach { trend ->
                Text(
                    text = "${trend.direction.name}: ${trend.exampleSpan} (${String.format(Locale.US, "%.2f", trend.slopeMmolLPerHour)} mmol/L per hour)",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun ExtremaCard(extrema: List<ExtremaAnnotation>) {
    Card(
        shape = RoundedCornerShape(12.dp),
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
                text = "Extrema",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            extrema.forEach { extremum ->
                val hours = extremum.minute / 60
                val minutes = extremum.minute % 60
                Text(
                    text = "${extremum.kind.name}: ${String.format(Locale.US, "%.1f", extremum.value)} mmol/L at ${String.format(Locale.US, "%02d:%02d", hours, minutes)}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
