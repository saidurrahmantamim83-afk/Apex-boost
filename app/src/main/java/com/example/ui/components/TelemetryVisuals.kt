package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AmberPulse
import com.example.ui.theme.BorderSlate
import com.example.ui.theme.CarbonSurface
import com.example.ui.theme.CarbonSurfaceBright
import com.example.ui.theme.CarbonSurfaceElevated
import com.example.ui.theme.CrimsonAlert
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldVolt
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun TurboReadinessGauge(
    score: Int,
    liveFps: Float,
    isMaxPerfLocked: Boolean,
    is60FpsLocked: Boolean,
    modifier: Modifier = Modifier
) {
    val animatedSweep by animateFloatAsState(
        targetValue = (score.coerceIn(0, 100) / 100f) * 240f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "gaugeSweep"
    )
    val accentColor by animateColorAsState(
        targetValue = when {
            isMaxPerfLocked -> EmeraldVolt
            is60FpsLocked -> ElectricCyan
            else -> AmberPulse
        },
        animationSpec = tween(300),
        label = "gaugeColor"
    )

    val infinitePulse = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infinitePulse.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.65f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Box(
        modifier = modifier.size(188.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokePx = 14.dp.toPx()
            val inset = strokePx / 2f + 12.dp.toPx()
            val arcSize = Size(size.width - inset * 2, size.height - inset * 2)
            val topLeft = Offset(inset, inset)
            val startAngle = 150f

            // Subtle glow halo when locked
            if (isMaxPerfLocked || is60FpsLocked) {
                drawArc(
                    color = accentColor.copy(alpha = pulseAlpha * 0.22f),
                    startAngle = startAngle,
                    sweepAngle = 240f,
                    useCenter = false,
                    topLeft = Offset(inset - 6.dp.toPx(), inset - 6.dp.toPx()),
                    size = Size(arcSize.width + 12.dp.toPx(), arcSize.height + 12.dp.toPx()),
                    style = Stroke(width = strokePx + 10.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            // Track arc
            drawArc(
                color = CarbonSurfaceBright,
                startAngle = startAngle,
                sweepAngle = 240f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )

            // Active performance sweep
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(ElectricCyan, accentColor, ElectricCyan)
                ),
                startAngle = startAngle,
                sweepAngle = animatedSweep,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )

            // Precision tachometer ticks along inner radius
            val center = Offset(size.width / 2f, size.height / 2f)
            val innerR = (arcSize.width / 2f) - 16.dp.toPx()
            for (i in 0..16) {
                val angleDeg = startAngle + (i * (240f / 16f))
                val angleRad = (angleDeg * PI / 180.0).toFloat()
                val tickLen = if (i % 4 == 0) 9.dp.toPx() else 4.dp.toPx()
                val p1 = Offset(
                    center.x + cos(angleRad) * innerR,
                    center.y + sin(angleRad) * innerR
                )
                val p2 = Offset(
                    center.x + cos(angleRad) * (innerR - tickLen),
                    center.y + sin(angleRad) * (innerR - tickLen)
                )
                val isLit = (i / 16f) * 240f <= animatedSweep
                drawLine(
                    color = if (isLit) accentColor else TextMuted.copy(alpha = 0.4f),
                    start = p1,
                    end = p2,
                    strokeWidth = if (i % 4 == 0) 2.5.dp.toPx() else 1.5.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text(
                text = if (isMaxPerfLocked) "LOCKED" else "SCORE",
                style = MaterialTheme.typography.labelSmall,
                color = accentColor
            )
            Text(
                text = "$score%",
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Surface(
                color = accentColor.copy(alpha = 0.14f),
                shape = RoundedCornerShape(50),
                border = BorderStroke(1.dp, accentColor.copy(alpha = 0.45f))
            ) {
                Text(
                    text = "${liveFps.roundToInt()} FPS",
                    style = MaterialTheme.typography.labelMedium,
                    color = accentColor,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                )
            }
        }
    }
}

@Composable
fun FrameTimeWaveformGraph(
    frameTimesMs: List<Float>,
    targetBudgetMs: Float = 16.67f,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = CarbonSurface,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, BorderSlate)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "FRAME PACING WAVEFORM",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )
                Text(
                    text = "BUDGET: 16.67 ms (60 FPS)",
                    style = MaterialTheme.typography.labelSmall,
                    color = ElectricCyan
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(104.dp)
            ) {
                val maxChartMs = 34f
                val budgetY = size.height * (1f - (targetBudgetMs / maxChartMs).coerceIn(0f, 1f))

                // 16.67 ms dashed budget line
                drawLine(
                    color = ElectricCyan.copy(alpha = 0.7f),
                    start = Offset(0f, budgetY),
                    end = Offset(size.width, budgetY),
                    strokeWidth = 1.5.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)
                )

                if (frameTimesMs.isEmpty()) return@Canvas

                val count = 40
                val barSpacing = 3.dp.toPx()
                val totalBarsWidth = size.width
                val barWidth = ((totalBarsWidth - barSpacing * (count - 1)) / count).coerceAtLeast(2f)
                val padded = if (frameTimesMs.size < count) {
                    List(count - frameTimesMs.size) { 0f } + frameTimesMs
                } else {
                    frameTimesMs.takeLast(count)
                }

                padded.forEachIndexed { idx, ms ->
                    if (ms <= 0f) return@forEachIndexed
                    val normalized = (ms / maxChartMs).coerceIn(0.06f, 1f)
                    val barHeight = size.height * normalized
                    val x = idx * (barWidth + barSpacing)
                    val barColor = when {
                        ms <= 17.6f -> EmeraldVolt
                        ms <= 22.0f -> AmberPulse
                        else -> CrimsonAlert
                    }
                    drawLine(
                        color = barColor,
                        start = Offset(x + barWidth / 2f, size.height),
                        end = Offset(x + barWidth / 2f, size.height - barHeight),
                        strokeWidth = barWidth,
                        cap = StrokeCap.Round
                    )
                }
            }
        }
    }
}

@Composable
fun FpsLockSimulationCanvas(
    particleCount: Int,
    is60FpsLocked: Boolean,
    isMaxPerfLocked: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "fpsBench")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2.0 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbitPhase"
    )

    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val maxRadius = (size.minDimension / 2f) * 0.86f
        val primary = if (isMaxPerfLocked) EmeraldVolt else ElectricCyan

        // Concentric radar rings
        listOf(0.35f, 0.65f, 0.95f).forEach { factor ->
            drawCircle(
                color = BorderSlate.copy(alpha = 0.55f),
                radius = maxRadius * factor,
                center = center,
                style = Stroke(width = 1.2.dp.toPx())
            )
        }

        // Crosshairs
        drawLine(
            color = BorderSlate.copy(alpha = 0.4f),
            start = Offset(center.x - maxRadius, center.y),
            end = Offset(center.x + maxRadius, center.y),
            strokeWidth = 1.dp.toPx()
        )
        drawLine(
            color = BorderSlate.copy(alpha = 0.4f),
            start = Offset(center.x, center.y - maxRadius),
            end = Offset(center.x, center.y + maxRadius),
            strokeWidth = 1.dp.toPx()
        )

        // Orbiting telemetry particles
        for (i in 0 until particleCount) {
            val layer = (i % 4) + 1
            val ringRadius = maxRadius * (0.22f + layer * 0.18f)
            val speed = if (layer % 2 == 0) 1f else -1.35f
            val angle = phase * speed + (i.toFloat() / particleCount) * (2f * PI.toFloat()) * layer
            val x = center.x + cos(angle) * ringRadius
            val y = center.y + sin(angle) * ringRadius
            val nodeColor = when {
                i % 5 == 0 -> primary
                i % 3 == 0 -> ElectricCyan
                else -> Color.White.copy(alpha = 0.78f)
            }
            drawCircle(
                color = nodeColor.copy(alpha = 0.25f),
                radius = 5.dp.toPx(),
                center = Offset(x, y)
            )
            drawCircle(
                color = nodeColor,
                radius = 2.4.dp.toPx(),
                center = Offset(x, y)
            )
        }

        // Core lock emblem
        drawCircle(
            color = primary.copy(alpha = if (is60FpsLocked) 0.22f else 0.1f),
            radius = 24.dp.toPx(),
            center = center
        )
        drawCircle(
            color = primary,
            radius = 12.dp.toPx(),
            center = center,
            style = Stroke(width = 2.5.dp.toPx())
        )
    }
}

@Composable
fun LockControlTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    accentColor: Color,
    testTag: String,
    modifier: Modifier = Modifier
) {
    val borderColor = if (checked) accentColor.copy(alpha = 0.65f) else BorderSlate
    val bgColor = if (checked) CarbonSurfaceElevated else CarbonSurface

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .clickable { onCheckedChange(!checked) }
            .testTag(testTag),
        color = bgColor,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = (if (checked) accentColor else TextMuted).copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = if (checked) accentColor else TextSecondary,
                        modifier = Modifier
                            .padding(8.dp)
                            .size(20.dp)
                    )
                }
                Switch(
                    checked = checked,
                    onCheckedChange = onCheckedChange,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("${testTag}_switch"),
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = CarbonSurface,
                        checkedTrackColor = accentColor,
                        uncheckedThumbColor = TextSecondary,
                        uncheckedTrackColor = CarbonSurfaceBright,
                        uncheckedBorderColor = BorderSlate
                    )
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = JetBrainsMonoFontFamily,
                        fontWeight = FontWeight.Medium
                    ),
                    color = if (checked) accentColor else TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun MetricPill(
    label: String,
    value: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = accentColor.copy(alpha = 0.12f),
        shape = RoundedCornerShape(50),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelMedium,
                color = accentColor
            )
        }
    }
}
