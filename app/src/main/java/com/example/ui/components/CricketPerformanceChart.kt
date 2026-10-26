package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LiquidGlassTheme
import java.util.Locale

@Composable
fun CricketBarChart(
    title: String,
    data: List<Pair<String, Int>>,
    unit: String = "",
    modifier: Modifier = Modifier
) {
    val tokens = LiquidGlassTheme.tokens
    GlassCard(
        modifier = modifier.fillMaxWidth(),
        backgroundColor = tokens.glassCardBackground,
        borderColor = tokens.glassBorder
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.BarChart,
                        contentDescription = null,
                        tint = tokens.accentGold,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = title,
                        color = tokens.textPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                if (data.isNotEmpty()) {
                    val maxVal = data.maxOfOrNull { it.second } ?: 0
                    Text(
                        text = "Peak: $maxVal $unit",
                        color = tokens.accentGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (data.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .background(tokens.glassCardInner, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No recorded matches yet",
                            color = tokens.textPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Play and save matches to generate performance charts",
                            color = tokens.textSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
            } else {
                val maxVal = maxOf(1, data.maxOf { it.second })
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val canvasWidth = size.width
                        val canvasHeight = size.height
                        val bottomPadding = 30f
                        val topPadding = 24f
                        val chartHeight = canvasHeight - bottomPadding - topPadding

                        // Draw horizontal subtle grid lines
                        val gridLines = 3
                        for (i in 0..gridLines) {
                            val y = topPadding + (chartHeight / gridLines) * i
                            drawLine(
                                color = tokens.glassBorder,
                                start = Offset(0f, y),
                                end = Offset(canvasWidth, y),
                                strokeWidth = 1f
                            )
                        }

                        val barCount = data.size
                        val totalSpacing = (barCount + 1) * 16f
                        val availableWidth = canvasWidth - totalSpacing
                        val barWidth = maxOf(20f, minOf(50f, availableWidth / barCount))
                        val step = canvasWidth / barCount

                        val barBrush = Brush.verticalGradient(
                            colors = listOf(tokens.accentGold, tokens.accentGold.copy(alpha = 0.6f))
                        )

                        data.forEachIndexed { i, pair ->
                            val xCenter = step * i + (step / 2f)
                            val valHeight = (pair.second.toFloat() / maxVal) * chartHeight
                            val barTop = topPadding + (chartHeight - valHeight)

                            // Draw Bar
                            drawRoundRect(
                                brush = barBrush,
                                topLeft = Offset(xCenter - barWidth / 2f, barTop),
                                size = Size(barWidth, valHeight.coerceAtLeast(4f)),
                                cornerRadius = CornerRadius(6f, 6f)
                            )
                        }
                    }

                    // Value labels
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 4.dp)
                            .align(Alignment.BottomCenter),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        data.forEach { (label, value) ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "$value",
                                    color = tokens.accentGold,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (label.length > 5) label.take(4) + "…" else label,
                                    color = tokens.textSecondary,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CricketTrendLineChart(
    title: String,
    data: List<Pair<String, Double>>,
    unit: String = "",
    modifier: Modifier = Modifier
) {
    val tokens = LiquidGlassTheme.tokens
    GlassCard(
        modifier = modifier.fillMaxWidth(),
        backgroundColor = tokens.glassCardBackground,
        borderColor = tokens.glassBorder
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ShowChart,
                        contentDescription = null,
                        tint = tokens.accentGold,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = title,
                        color = tokens.textPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                if (data.isNotEmpty()) {
                    val latest = data.last().second
                    Text(
                        text = "Latest: ${String.format(Locale.US, "%.1f", latest)} $unit",
                        color = tokens.accentGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (data.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .background(tokens.glassCardInner, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Insufficient trend data",
                            color = tokens.textPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Metrics will appear here as matches are scored",
                            color = tokens.textSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
            } else {
                val maxVal = maxOf(1.0, data.maxOf { it.second })
                val minVal = 0.0

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val canvasWidth = size.width
                        val canvasHeight = size.height
                        val bottomPadding = 30f
                        val topPadding = 24f
                        val chartHeight = canvasHeight - bottomPadding - topPadding

                        // Grid lines
                        for (i in 0..2) {
                            val y = topPadding + (chartHeight / 2) * i
                            drawLine(
                                color = tokens.glassBorder,
                                start = Offset(0f, y),
                                end = Offset(canvasWidth, y),
                                strokeWidth = 1f
                            )
                        }

                        if (data.size == 1) {
                            val cx = canvasWidth / 2f
                            val cy = topPadding + chartHeight / 2f
                            drawCircle(color = tokens.accentGold, radius = 8f, center = Offset(cx, cy))
                            drawCircle(color = tokens.glassCardBackground, radius = 4f, center = Offset(cx, cy))
                        } else {
                            val step = canvasWidth / (data.size - 1)
                            val path = Path()

                            val points = data.mapIndexed { i, (_, value) ->
                                val x = step * i
                                val normalized = ((value - minVal) / (maxVal - minVal)).coerceIn(0.0, 1.0)
                                val y = (topPadding + (chartHeight * (1.0 - normalized))).toFloat()
                                Offset(x, y)
                            }

                            path.moveTo(points.first().x, points.first().y)
                            for (p in points.drop(1)) {
                                path.lineTo(p.x, p.y)
                            }

                            // Draw smooth trend line
                            drawPath(
                                path = path,
                                color = tokens.accentGold,
                                style = Stroke(width = 4f, cap = StrokeCap.Round)
                            )

                            // Draw glowing points
                            points.forEach { p ->
                                drawCircle(color = tokens.accentGold, radius = 6f, center = p)
                                drawCircle(color = tokens.glassCardBackground, radius = 3f, center = p)
                            }
                        }
                    }

                    // Value labels
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 4.dp)
                            .align(Alignment.BottomCenter),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        data.forEach { (label, value) ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = String.format(Locale.US, "%.1f", value),
                                    color = tokens.accentGold,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (label.length > 5) label.take(4) + "…" else label,
                                    color = tokens.textSecondary,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
