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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.project.R
import com.example.project.domain.model.DatasetData
import com.example.project.domain.model.GlucosePoint
import java.util.Locale
import kotlin.math.ceil
import kotlin.math.floor

/**
 * Glucose graph component showing glucose levels over time
 */
@Composable
fun GlucoseGraphCard(
    datasetData: DatasetData?,
    preset: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    sourceUnit: String? = null,
    displayUnit: String? = null,
    graphHeight: Dp = 200.dp
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
                text = stringResource(id = R.string.glucose_levels_title, preset),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (datasetData != null && datasetData.overlayDays.isNotEmpty()) {
                GlucoseGraph(
                    datasetData = datasetData,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(graphHeight),
                    fromUnit = sourceUnit ?: datasetData.unit,
                    toUnit = displayUnit ?: datasetData.unit
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(graphHeight)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant,
                            RoundedCornerShape(8.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(id = R.string.graph_no_data),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(id = R.string.graph_tap_metrics),
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
    modifier: Modifier = Modifier,
    fromUnit: String?,
    toUnit: String?
) {
    // Flatten and convert points
    val allPoints = mutableListOf<GlucosePoint>()
    datasetData.overlayDays.forEach { day ->
        day.points.forEach { point ->
            val converted = convertGlucose(point.glucose, fromUnit, toUnit)
            allPoints.add(GlucosePoint(minute = point.minute, glucose = converted))
        }
    }

    if (allPoints.isEmpty()) return

    // Determine ranges in display unit
    val minGlucose = allPoints.minOf { it.glucose }
    val maxGlucose = allPoints.maxOf { it.glucose }
    val glucoseRange = (maxGlucose - minGlucose).coerceAtLeast(1e-6)

    // Thresholds in mmol/L, convert to display unit if needed
    val displayIsMgdl = (toUnit ?: "").lowercase(Locale.US).contains("mg/dl")
    fun thr(x: Double): Double = if (displayIsMgdl) x * 18.0 else x
    val normalRange = thr(4.0)..thr(10.0)
    val dayColor = Color(0xFF00695C)

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val leftPadding = 60f
        val rightPadding = 20f
        val topPadding = 20f
        val bottomPadding = 20f
        val graphWidth = width - leftPadding - rightPadding
        val graphHeight = height - topPadding - bottomPadding

        // Y ticks based on data range
        val tickInterval = when {
            glucoseRange <= 5 -> 1.0
            glucoseRange <= 10 -> 2.0
            glucoseRange <= 20 -> 5.0
            else -> 10.0
        }
        val minTick = floor(minGlucose / tickInterval) * tickInterval
        val maxTick = ceil(maxGlucose / tickInterval) * tickInterval
        val ticks = mutableListOf<Double>()
        var currentTick = minTick
        while (currentTick <= maxTick + 1e-6) {
            ticks.add(currentTick)
            currentTick += tickInterval
        }

        val textPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.DKGRAY
            textSize = 28f
            textAlign = android.graphics.Paint.Align.RIGHT
        }
        val labelPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.DKGRAY
            textSize = 30f
            textAlign = android.graphics.Paint.Align.LEFT
        }

        // Background
        drawRect(
            color = Color(0xFFFFF0F0),
            topLeft = Offset(leftPadding, topPadding),
            size = androidx.compose.ui.geometry.Size(graphWidth, graphHeight)
        )

        ticks.forEach { tickValue ->
            val y = height - bottomPadding - (((tickValue - minGlucose) / glucoseRange) * graphHeight).toFloat()
            drawLine(color = Color.LightGray.copy(alpha = 0.4f), start = Offset(leftPadding, y), end = Offset(leftPadding + graphWidth, y), strokeWidth = 1f)
            drawContext.canvas.nativeCanvas.drawText(
                String.format(Locale.US, "%.1f", tickValue),
                leftPadding - 15f,
                y + 10f,
                textPaint
            )
        }

        // Time-in-range band
        val normalTop = height - bottomPadding - (((normalRange.endInclusive - minGlucose) / glucoseRange) * graphHeight).toFloat()
        val normalBottom = height - bottomPadding - (((normalRange.start - minGlucose) / glucoseRange) * graphHeight).toFloat()
        val tirTop = normalTop.coerceIn(topPadding, height - bottomPadding)
        val tirBottom = normalBottom.coerceIn(topPadding, height - bottomPadding)
        drawRect(
            color = Color(0xFFC7F5D9).copy(alpha = 0.7f),
            topLeft = Offset(leftPadding, tirTop),
            size = androidx.compose.ui.geometry.Size(graphWidth, (tirBottom - tirTop).coerceAtLeast(0f))
        )
        drawLine(
            color = Color(0xFF4CAF50),
            start = Offset(leftPadding, tirTop),
            end = Offset(leftPadding + graphWidth, tirTop),
            strokeWidth = 2f
        )
        drawLine(
            color = Color(0xFF4CAF50),
            start = Offset(leftPadding, tirBottom),
            end = Offset(leftPadding + graphWidth, tirBottom),
            strokeWidth = 2f
        )
        drawContext.canvas.nativeCanvas.drawText(
            "TIR max",
            leftPadding + 8f,
            tirTop - 8f,
            labelPaint
        )
        drawContext.canvas.nativeCanvas.drawText(
            "TIR min",
            leftPadding + 8f,
            tirBottom + 28f,
            labelPaint
        )

        // Time axis labels every 6h
        val maxMinute = allPoints.maxOf { it.minute }.coerceAtLeast(1)
        val xLabelPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.DKGRAY
            textSize = 32f
            textAlign = android.graphics.Paint.Align.CENTER
        }
        for (hour in 0..24 step 3) {
            val minute = hour * 60
            val x = leftPadding + (minute / maxMinute.toFloat()) * graphWidth
            drawLine(color = Color.LightGray.copy(alpha = 0.4f), start = Offset(x, height - bottomPadding), end = Offset(x, height - bottomPadding + 8f), strokeWidth = 1f)
            drawContext.canvas.nativeCanvas.drawText(
                String.format(Locale.US, "%02d:00", hour),
                x,
                height - 4f,
                xLabelPaint
            )
        }

        // Draw each day path with low-alpha teal
        datasetData.overlayDays.forEach { day ->
            if (day.points.size < 2) return@forEach
            val convertedPoints = day.points.map { pt ->
                pt.copy(glucose = convertGlucose(pt.glucose, fromUnit, toUnit))
            }
            val path = Path()
            convertedPoints.forEachIndexed { index, point ->
                val x = leftPadding + (point.minute / maxMinute.toFloat()) * graphWidth
                val y = height - bottomPadding - (((point.glucose - minGlucose) / glucoseRange) * graphHeight).toFloat()
                if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            drawPath(
                path = path,
                color = dayColor.copy(alpha = 0.3f),
                style = Stroke(width = 4.5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        }
    }
}

private fun convertGlucose(value: Double, fromUnit: String?, toUnit: String?): Double {
    val from = (fromUnit ?: "").lowercase(Locale.US)
    val to = (toUnit ?: "").lowercase(Locale.US)
    if (from.isEmpty() || to.isEmpty() || from == to) return value
    return if (from.contains("mg/dl") && to.contains("mmol")) {
        value / 18.0
    } else if (from.contains("mmol") && to.contains("mg/dl")) {
        value * 18.0
    } else value
}
