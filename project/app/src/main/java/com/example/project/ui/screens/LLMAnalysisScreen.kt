package com.example.project.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.project.domain.model.*
import com.example.project.ui.components.EmptyView
import com.example.project.ui.components.ErrorView
import com.example.project.ui.components.LoadingView
import com.example.project.ui.UiState
import com.example.project.ui.viewmodels.CgmApiViewModel
import java.util.Locale

/**
 * Screen displaying LLM-generated analysis text for a specific dataset
 * Shows AI insights similar to the frontend's right panel
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LLMAnalysisScreen(
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
                title = { Text("AI Analysis") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
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
                    LoadingView("Generating AI insights...")
                }
                is UiState.Success -> {
                    LLMAnalysisContent(
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
                    EmptyView("No analysis data available")
                }
            }
        }
    }
}

@Composable
private fun LLMAnalysisContent(
    analysis: AnalysisResult,
    selectedPreset: String,
    onPresetChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Preset selector
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Time Range",
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

        // Overall rating card with color
        OverallRatingBanner(analysis.overallRating)

        // AI-generated text in a highlighted card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Header
                Column {
                    Text(
                        text = "Personalized Insights",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "AI-generated analysis and recommendations",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                // Summary section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "📊",
                            fontSize = 20.sp,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = "Overall Assessment",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Text(
                        text = analysis.summary,
                        style = MaterialTheme.typography.bodyLarge,
                        lineHeight = 24.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Interpretation section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "💡",
                            fontSize = 20.sp,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = "Detailed Insights",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                    Text(
                        text = analysis.interpretation,
                        style = MaterialTheme.typography.bodyLarge,
                        lineHeight = 24.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Warnings if any
                if (analysis.warnings.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "⚠️",
                                fontSize = 20.sp,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text(
                                text = "Important Notices",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        analysis.warnings.forEach { warning ->
                            Text(
                                text = "• $warning",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }
        }

        // Disclaimer
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "ℹ️",
                    fontSize = 20.sp
                )
                Column {
                    Text(
                        text = "Medical Disclaimer",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "This analysis is AI-generated and should not replace professional medical advice. Always consult with your healthcare provider before making changes to your diabetes management plan.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun OverallRatingBanner(rating: OverallRating) {
    val (backgroundColor, textColor, icon) = when (rating.category) {
        RatingCategory.GOOD -> Triple(
            Color(0xFF4CAF50),
            Color.White,
            "✓"
        )
        RatingCategory.ATTENTION -> Triple(
            Color(0xFFFFC107),
            Color(0xFF1F1F1F),
            "⚠"
        )
        RatingCategory.URGENT -> Triple(
            Color(0xFFF44336),
            Color.White,
            "!"
        )
        else -> Triple(
            Color(0xFF757575),
            Color.White,
            "?"
        )
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Icon
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(
                            color = Color.White.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(28.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = icon,
                        fontSize = 32.sp,
                        color = textColor
                    )
                }

                // Status text
                Column {
                    Text(
                        text = "Overall Status",
                        style = MaterialTheme.typography.labelMedium,
                        color = textColor.copy(alpha = 0.8f)
                    )
                    Text(
                        text = rating.category.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                }
            }

            // Score if available
            rating.score?.let { score ->
                Text(
                    text = String.format(Locale.US, "%.1f", score),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            }
        }
    }
}
