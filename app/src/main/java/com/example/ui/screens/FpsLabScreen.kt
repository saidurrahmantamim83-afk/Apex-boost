package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.components.FpsLockSimulationCanvas
import com.example.ui.components.FrameTimeWaveformGraph
import com.example.ui.components.MetricPill
import com.example.ui.theme.AmberPulse
import com.example.ui.theme.BorderSlate
import com.example.ui.theme.CarbonSurface
import com.example.ui.theme.CarbonSurfaceBright
import com.example.ui.theme.CarbonSurfaceElevated
import com.example.ui.theme.CrimsonAlert
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldVolt
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.BenchmarkLoadLevel
import com.example.ui.viewmodel.BoosterUiState
import kotlin.math.roundToInt

@Composable
fun FpsLabScreen(
    uiState: BoosterUiState,
    onToggle60Fps: (Boolean) -> Unit,
    onToggleMaxPerf: (Boolean) -> Unit,
    onSelectLoad: (BenchmarkLoadLevel) -> Unit,
    onResetCounters: () -> Unit,
    modifier: Modifier = Modifier
) {
    val prefs = uiState.preferences
    val metrics = uiState.frameMetrics
    val fpsColor = when {
        metrics.currentFps >= 57f -> EmeraldVolt
        metrics.currentFps >= 45f -> AmberPulse
        else -> CrimsonAlert
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("fps_lab_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 840.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "60 FPS LOCK LAB",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Live Choreographer VSync & frame-budget verifier",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                    OutlinedButton(
                        onClick = onResetCounters,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("reset_fps_counters_button"),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, BorderSlate),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "Reset frame counters",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("RESET", style = MaterialTheme.typography.labelSmall)
                    }
                }

                // Live Orbital Stress Canvas + Live FPS HUD
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    color = CarbonSurfaceElevated,
                    border = BorderStroke(
                        1.dp,
                        if (prefs.is60FpsLocked) ElectricCyan.copy(alpha = 0.6f) else BorderSlate
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                    ) {
                        FpsLockSimulationCanvas(
                            particleCount = uiState.benchmarkLoad.particleCount,
                            is60FpsLocked = prefs.is60FpsLocked,
                            isMaxPerfLocked = prefs.isMaxPerformanceLocked,
                            modifier = Modifier.fillMaxSize()
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp)
                                .align(Alignment.TopCenter),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            MetricPill(
                                label = "LOCK",
                                value = if (prefs.is60FpsLocked) "60 FPS" else "UNLOCKED",
                                accentColor = if (prefs.is60FpsLocked) ElectricCyan else AmberPulse
                            )
                            MetricPill(
                                label = "DISPLAY",
                                value = "${uiState.telemetry.currentRefreshRateHz.roundToInt()} Hz",
                                accentColor = EmeraldVolt
                            )
                        }

                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(14.dp)
                        ) {
                            Text(
                                text = String.format("%.1f FPS", metrics.currentFps),
                                style = MaterialTheme.typography.displayMedium,
                                color = fpsColor
                            )
                            Text(
                                text = String.format(
                                    "AVG %.2f ms  •  1%% LOW %.0f FPS",
                                    metrics.averageFrameTimeMs,
                                    metrics.onePercentLowFps
                                ),
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                        }

                        Surface(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(14.dp),
                            color = CarbonSurface.copy(alpha = 0.85f),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, BorderSlate)
                        ) {
                            Text(
                                text = "JANK: ${metrics.jankFrameCount}",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (metrics.jankFrameCount == 0) EmeraldVolt else AmberPulse,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                // Benchmark Load Selector
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = CarbonSurface,
                    border = BorderStroke(1.dp, BorderSlate)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "GPU / CANVAS BENCHMARK LOAD",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextSecondary
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            BenchmarkLoadLevel.entries.forEach { level ->
                                FilterChip(
                                    selected = uiState.benchmarkLoad == level,
                                    onClick = { onSelectLoad(level) },
                                    label = {
                                        Text(
                                            text = level.label,
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("bench_load_${level.name.lowercase()}"),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = ElectricCyan.copy(alpha = 0.16f),
                                        selectedLabelColor = ElectricCyan
                                    )
                                )
                            }
                        }
                    }
                }

                // Real-time Frame Time Waveform Graph
                FrameTimeWaveformGraph(frameTimesMs = metrics.recentFrameTimesMs)

                // Direct Lab Lock Toggles
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    color = CarbonSurface,
                    border = BorderStroke(1.dp, BorderSlate)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        LabSwitchRow(
                            title = "Enforce 60 FPS Window Lock",
                            subtitle = "Locks preferredDisplayModeId & preferredRefreshRate to 60 Hz",
                            icon = Icons.Filled.Speed,
                            checked = prefs.is60FpsLocked,
                            onCheckedChange = onToggle60Fps,
                            accentColor = ElectricCyan,
                            testTag = "lab_60fps_switch"
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        LabSwitchRow(
                            title = "Sustained Max Performance",
                            subtitle = "Keeps screen awake + engages ADPF PerformanceHintManager",
                            icon = Icons.Filled.Bolt,
                            checked = prefs.isMaxPerformanceLocked,
                            onCheckedChange = onToggleMaxPerf,
                            accentColor = EmeraldVolt,
                            testTag = "lab_max_perf_switch"
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LabSwitchRow(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    accentColor: androidx.compose.ui.graphics.Color,
    testTag: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            color = (if (checked) accentColor else TextMuted).copy(alpha = 0.15f),
            shape = RoundedCornerShape(10.dp)
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
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier
                .minimumInteractiveComponentSize()
                .testTag(testTag),
            colors = SwitchDefaults.colors(
                checkedThumbColor = CarbonSurface,
                checkedTrackColor = accentColor,
                uncheckedThumbColor = TextSecondary,
                uncheckedTrackColor = CarbonSurfaceBright,
                uncheckedBorderColor = BorderSlate
            )
        )
    }
}
