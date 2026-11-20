package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.outlined.DeveloperBoard
import androidx.compose.material.icons.outlined.SdStorage
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.BoostSequenceState
import com.example.data.ThermalHealth
import com.example.ui.components.LockControlTile
import com.example.ui.components.MetricPill
import com.example.ui.components.TurboReadinessGauge
import com.example.ui.theme.AmberPulse
import com.example.ui.theme.BorderSlate
import com.example.ui.theme.CarbonSurface
import com.example.ui.theme.CarbonSurfaceBright
import com.example.ui.theme.CarbonSurfaceElevated
import com.example.ui.theme.CrimsonAlert
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldVolt
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.BoosterUiState
import kotlin.math.roundToInt

@Composable
fun CockpitScreen(
    uiState: BoosterUiState,
    onBoostNow: () -> Unit,
    onReleaseLock: () -> Unit,
    onToggle60Fps: (Boolean) -> Unit,
    onToggleMaxPerf: (Boolean) -> Unit,
    onToggleCpuPriority: (Boolean) -> Unit,
    onToggleAutoRam: (Boolean) -> Unit,
    onSweepRamNow: () -> Unit,
    onOpenFpsLab: () -> Unit,
    modifier: Modifier = Modifier
) {
    val prefs = uiState.preferences
    val telemetry = uiState.telemetry
    val thermalColor = when (telemetry.thermalHealth) {
        ThermalHealth.OPTIMAL -> EmeraldVolt
        ThermalHealth.WARM -> AmberPulse
        ThermalHealth.THROTTLED, ThermalHealth.CRITICAL -> CrimsonAlert
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("cockpit_screen"),
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
                // Header Status Strip
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.app_name).uppercase(),
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "${telemetry.socLabel} • ${telemetry.cpuCoresCount} CORES",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        MetricPill(
                            label = "TEMP",
                            value = telemetry.thermalHealth.label,
                            accentColor = thermalColor
                        )
                        MetricPill(
                            label = "HZ",
                            value = if (prefs.is60FpsLocked) {
                                "60 LOCK"
                            } else {
                                "${telemetry.currentRefreshRateHz.roundToInt()} Hz"
                            },
                            accentColor = if (prefs.is60FpsLocked) ElectricCyan else TextSecondary
                        )
                    }
                }

                // Hero Turbo Cockpit Card
                HeroCockpitCard(
                    uiState = uiState,
                    onBoostNow = onBoostNow,
                    onReleaseLock = onReleaseLock,
                    onOpenFpsLab = onOpenFpsLab
                )

                // 2x2 Hardware Lock Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    LockControlTile(
                        title = stringResource(R.string.fps_lock_title),
                        subtitle = if (prefs.is60FpsLocked) "60.0 FPS • 16.67ms" else "NATIVE ${telemetry.currentRefreshRateHz.roundToInt()}Hz",
                        icon = Icons.Filled.Speed,
                        checked = prefs.is60FpsLocked,
                        onCheckedChange = onToggle60Fps,
                        accentColor = ElectricCyan,
                        testTag = "tile_60fps_lock",
                        modifier = Modifier.weight(1f)
                    )
                    LockControlTile(
                        title = stringResource(R.string.max_perf_title),
                        subtitle = if (prefs.isMaxPerformanceLocked) "SUSTAINED + ADPF" else "BALANCED MODE",
                        icon = Icons.Filled.Bolt,
                        checked = prefs.isMaxPerformanceLocked,
                        onCheckedChange = onToggleMaxPerf,
                        accentColor = EmeraldVolt,
                        testTag = "tile_max_perf",
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    LockControlTile(
                        title = stringResource(R.string.cpu_optimizer_title),
                        subtitle = if (prefs.isCpuPriorityEnabled) "URGENT DISPLAY" else "DEFAULT SCHED",
                        icon = Icons.Outlined.DeveloperBoard,
                        checked = prefs.isCpuPriorityEnabled,
                        onCheckedChange = onToggleCpuPriority,
                        accentColor = ElectricCyan,
                        testTag = "tile_cpu_priority",
                        modifier = Modifier.weight(1f)
                    )
                    LockControlTile(
                        title = stringResource(R.string.ram_cleaner_title),
                        subtitle = if (prefs.isAutoRamSweepEnabled) "AUTO ON LAUNCH" else "MANUAL SWEEP",
                        icon = Icons.Filled.CleaningServices,
                        checked = prefs.isAutoRamSweepEnabled,
                        onCheckedChange = onToggleAutoRam,
                        accentColor = EmeraldVolt,
                        testTag = "tile_auto_ram",
                        modifier = Modifier.weight(1f)
                    )
                }

                // Live RAM Telemetry & Immediate Sweep Card
                RamTelemetryCard(
                    uiState = uiState,
                    onSweepRamNow = onSweepRamNow
                )

                // Live CPU Cores & Thermal Telemetry Card
                CpuTelemetryCard(
                    uiState = uiState,
                    thermalColor = thermalColor
                )
            }
        }
    }
}

@Composable
private fun HeroCockpitCard(
    uiState: BoosterUiState,
    onBoostNow: () -> Unit,
    onReleaseLock: () -> Unit,
    onOpenFpsLab: () -> Unit
) {
    val prefs = uiState.preferences
    val isLocked = prefs.isMaxPerformanceLocked
    val seq = uiState.boostSequence

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = CarbonSurfaceElevated,
        border = BorderStroke(
            width = 1.dp,
            color = if (isLocked) EmeraldVolt.copy(alpha = 0.65f) else BorderSlate
        )
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Image(
                painter = painterResource(id = R.drawable.img_boost_cockpit_1795147753921),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .matchParentSize()
                    .alpha(0.32f)
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                ObsidianBg.copy(alpha = 0.35f),
                                CarbonSurface.copy(alpha = 0.90f)
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                TurboReadinessGauge(
                    score = uiState.telemetry.readinessScore,
                    liveFps = uiState.frameMetrics.currentFps,
                    isMaxPerfLocked = prefs.isMaxPerformanceLocked,
                    is60FpsLocked = prefs.is60FpsLocked
                )

                AnimatedVisibility(visible = seq is BoostSequenceState.Optimizing) {
                    if (seq is BoostSequenceState.Optimizing) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = seq.stepLabel,
                                style = MaterialTheme.typography.labelMedium,
                                color = ElectricCyan
                            )
                            LinearProgressIndicator(
                                progress = { seq.progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(50)),
                                color = ElectricCyan,
                                trackColor = CarbonSurfaceBright
                            )
                        }
                    }
                }

                AnimatedVisibility(visible = seq is BoostSequenceState.Completed && isLocked) {
                    if (seq is BoostSequenceState.Completed) {
                        Surface(
                            color = EmeraldVolt.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, EmeraldVolt.copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = "Swept ${seq.processesSwept} apps • +${seq.reclaimedMb} MB RAM freed • 60 FPS Lock Active",
                                style = MaterialTheme.typography.labelSmall,
                                color = EmeraldVolt,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = if (isLocked) onOpenFpsLab else onBoostNow,
                        enabled = seq !is BoostSequenceState.Optimizing,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .testTag("boost_lock_button"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isLocked) EmeraldVolt else ElectricCyan,
                            contentColor = ObsidianBg
                        )
                    ) {
                        Icon(
                            imageVector = if (isLocked) Icons.Filled.Lock else Icons.Filled.Bolt,
                            contentDescription = if (isLocked) "Performance locked" else "Optimize and lock",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isLocked) "VERIFY IN 60 FPS LAB" else stringResource(R.string.boost_now),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }

                    if (isLocked) {
                        OutlinedButton(
                            onClick = onReleaseLock,
                            modifier = Modifier
                                .height(52.dp)
                                .minimumInteractiveComponentSize()
                                .testTag("release_lock_button"),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, BorderSlate),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.LockOpen,
                                contentDescription = "Release performance lock",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RamTelemetryCard(
    uiState: BoosterUiState,
    onSweepRamNow: () -> Unit
) {
    val telemetry = uiState.telemetry
    val animatedUsage by animateFloatAsState(
        targetValue = telemetry.ramUsageFraction,
        animationSpec = tween(400),
        label = "ramUsage"
    )
    val usageColor = when {
        animatedUsage > 0.85f -> CrimsonAlert
        animatedUsage > 0.70f -> AmberPulse
        else -> ElectricCyan
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = CarbonSurface,
        border = BorderStroke(1.dp, BorderSlate)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        color = usageColor.copy(alpha = 0.14f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Memory,
                            contentDescription = "RAM Memory",
                            tint = usageColor,
                            modifier = Modifier
                                .padding(8.dp)
                                .size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "System Memory (RAM)",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${telemetry.usedRamMb} / ${telemetry.totalRamMb} MB USED",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                }

                OutlinedButton(
                    onClick = onSweepRamNow,
                    enabled = !uiState.isSweepingRam,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("sweep_ram_button"),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, ElectricCyan.copy(alpha = 0.55f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ElectricCyan)
                ) {
                    if (uiState.isSweepingRam) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = ElectricCyan
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Filled.CleaningServices,
                            contentDescription = "Sweep RAM",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("SWEEP", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }

            LinearProgressIndicator(
                progress = { animatedUsage },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(50)),
                color = usageColor,
                trackColor = CarbonSurfaceBright
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "AVAILABLE: ${telemetry.availableRamMb} MB (${telemetry.ramFreePercent}%)",
                    style = MaterialTheme.typography.labelSmall,
                    color = EmeraldVolt
                )
                val extraLabel = telemetry.cachedRamMb?.let { "CACHED: $it MB" }
                    ?: "LOW MEM THRESH: ${telemetry.ramThresholdMb} MB"
                Text(
                    text = extraLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
            }
        }
    }
}

@Composable
private fun CpuTelemetryCard(
    uiState: BoosterUiState,
    thermalColor: Color
) {
    val telemetry = uiState.telemetry
    val hasCoreFreqs = telemetry.cpuCores.any { it.currentMhz != null }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = CarbonSurface,
        border = BorderStroke(1.dp, BorderSlate)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        color = ElectricCyan.copy(alpha = 0.14f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.SdStorage,
                            contentDescription = "CPU Cores",
                            tint = ElectricCyan,
                            modifier = Modifier
                                .padding(8.dp)
                                .size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "CPU & Thermal Engine",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${telemetry.cpuCoresCount} Cores • ${telemetry.abiLabel}",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Thermostat,
                        contentDescription = "Thermal status",
                        tint = thermalColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = telemetry.thermalHealth.label,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = thermalColor
                    )
                }
            }

            if (hasCoreFreqs) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    telemetry.cpuCores.chunked(4).forEach { rowCores ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowCores.forEach { core ->
                                Surface(
                                    modifier = Modifier.weight(1f),
                                    color = CarbonSurfaceElevated,
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, BorderSlate)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = "CPU${core.coreIndex}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TextMuted
                                        )
                                        Text(
                                            text = core.currentMhz?.let { "${it} MHz" } ?: "ACTIVE",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = ElectricCyan
                                        )
                                        LinearProgressIndicator(
                                            progress = { core.loadFraction },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(4.dp)
                                                .clip(RoundedCornerShape(50)),
                                            color = ElectricCyan,
                                            trackColor = CarbonSurfaceBright
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        modifier = Modifier.weight(1f),
                        color = CarbonSurfaceElevated,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, BorderSlate)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "CPU PROCESS TIME",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${telemetry.cpuActiveTimeMs} ms",
                                style = MaterialTheme.typography.titleSmall,
                                color = ElectricCyan
                            )
                        }
                    }
                    Surface(
                        modifier = Modifier.weight(1f),
                        color = CarbonSurfaceElevated,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, BorderSlate)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "SUSTAINED PERF API",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (telemetry.isSustainedPerformanceSupported) {
                                    if (uiState.preferences.isMaxPerformanceLocked) "ENGAGED" else "READY"
                                } else {
                                    "THREAD BOOST"
                                },
                                style = MaterialTheme.typography.titleSmall,
                                color = EmeraldVolt
                            )
                        }
                    }
                }
            }
        }
    }
}
