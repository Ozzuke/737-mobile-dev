package com.example.project.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.project.domain.model.DatasetData
import com.example.project.domain.model.GlucosePoint
import kotlin.math.max

/**
 * Glucose graph component showing glucose levels over time
 */
@Composable
fun GlucoseGraphCard(
    datasetData: DatasetData?,
    preset: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Glucose Levels ($preset)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (datasetData != null && datasetData.overlayDays.isNotEmpty()) {
                GlucoseGraph(
                    datasetData = datasetData,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant,
                            RoundedCornerShape(8.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No data available",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Tap to view detailed metrics",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Simple line chart showing glucose values over time
 */
@Composable
private fun GlucoseGraph(
    datasetData: DatasetData,
    modifier: Modifier = Modifier
) {
    // Flatten all points from all days into a single list
    val allPoints = mutableListOf<GlucosePoint>()
    datasetData.overlayDays.forEach { day ->
        day.points.forEach { point ->
            allPoints.add(GlucosePoint(minute = point.minute, glucose = point.glucose))
        }
    }

    if (allPoints.isEmpty()) {
        return
    }

    // Find min/max glucose values for scaling
    val minGlucose = allPoints.minOf { it.glucose }
    val maxGlucose = allPoints.maxOf { it.glucose }
    val glucoseRange = maxGlucose - minGlucose

    // Define glucose zones (example values for mmol/L)
    val normalRange = 4.0f..10.0f
    val warningLow = 3.0f..4.0f
    val warningHigh = 10.0f..15.0f

    val primaryColor = MaterialTheme.colorScheme.primary
    val normalColor = Color(0xFF4CAF50) // Green
    val warningColor = Color(0xFFFFC107) // Yellow
    val criticalColor = Color(0xFFF44336) // Red

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val padding = 40f

        // Draw background zones
        val normalTop = height - ((normalRange.endInclusive - minGlucose) / glucoseRange * (height - padding)).toFloat()
        val normalBottom = height - ((normalRange.start - minGlucose) / glucoseRange * (height - padding)).toFloat()

        drawRect(
            color = normalColor.copy(alpha = 0.1f),
            topLeft = Offset(padding, max(normalTop, padding)),
            size = androidx.compose.ui.geometry.Size(width - padding * 2, (normalBottom - normalTop).coerceAtLeast(0f))
        )

        // Draw glucose line
        if (allPoints.size > 1) {
            val path = Path()
            val stepX = (width - padding * 2) / (allPoints.size - 1)

            allPoints.forEachIndexed { index, point ->
                val x = padding + index * stepX
                val y = height - ((point.glucose - minGlucose) / glucoseRange * (height - padding)).toFloat()

                if (index == 0) {
                    path.moveTo(x, y)
                } else {
                    path.lineTo(x, y)
                }

                // Draw point
                drawCircle(
                    color = when {
                        point.glucose < warningLow.start -> criticalColor
                        point.glucose in warningLow -> warningColor
                        point.glucose in normalRange -> normalColor
                        point.glucose in warningHigh -> warningColor
                        else -> criticalColor
                    },
                    radius = 4f,
                    center = Offset(x, y)
                )
            }

            drawPath(
                path = path,
                color = primaryColor,
                style = Stroke(width = 3f)
            )
        }

        // Draw axis labels (simplified)
        // TODO: Add proper axis labels with time and glucose values
    }
}
