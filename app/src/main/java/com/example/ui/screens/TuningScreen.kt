package com.example.ui.screens

import android.os.Build
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DoNotDisturbOn
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.outlined.DisplaySettings
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
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
import com.example.ui.components.MetricPill
import com.example.ui.theme.AmberPulse
import com.example.ui.theme.BorderSlate
import com.example.ui.theme.CarbonSurface
import com.example.ui.theme.CarbonSurfaceBright
import com.example.ui.theme.CarbonSurfaceElevated
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldVolt
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.BoosterUiState
import kotlin.math.roundToInt

@Composable
fun TuningScreen(
    uiState: BoosterUiState,
    onToggleDndShield: (Boolean) -> Unit,
    onToggleBoostNotification: (Boolean) -> Unit,
    onOpenDndSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val prefs = uiState.preferences
    val telemetry = uiState.telemetry

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("tuning_screen"),
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
                Column {
                    Text(
                        text = "HARDWARE & TUNING",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Display modes, gaming shield & system APIs",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }

                // Gaming Shield & Status Notification
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    color = CarbonSurface,
                    border = BorderStroke(1.dp, BorderSlate)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                color = ElectricCyan.copy(alpha = 0.14f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.DoNotDisturbOn,
                                    contentDescription = "Do Not Disturb Gaming Shield",
                                    tint = ElectricCyan,
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .size(20.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Do Not Disturb Gaming Shield",
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Text(
                                    text = if (telemetry.isNotificationPolicyGranted) {
                                        "Silence alerts automatically when Boost Lock is active"
                                    } else {
                                        "Requires Notification Policy access in Android Settings"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary
                                )
                            }
                            if (telemetry.isNotificationPolicyGranted) {
                                Switch(
                                    checked = prefs.isDndShieldEnabled,
                                    onCheckedChange = onToggleDndShield,
                                    modifier = Modifier
                                        .minimumInteractiveComponentSize()
                                        .testTag("dnd_shield_switch"),
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = CarbonSurface,
                                        checkedTrackColor = ElectricCyan,
                                        uncheckedThumbColor = TextSecondary,
                                        uncheckedTrackColor = CarbonSurfaceBright
                                    )
                                )
                            } else {
                                OutlinedButton(
                                    onClick = onOpenDndSettings,
                                    modifier = Modifier
                                        .minimumInteractiveComponentSize()
                                        .testTag("grant_dnd_button"),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, ElectricCyan.copy(alpha = 0.5f)),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ElectricCyan)
                                ) {
                                    Text("GRANT", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }

                        HorizontalDivider(color = BorderSlate.copy(alpha = 0.6f))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                color = EmeraldVolt.copy(alpha = 0.14f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.NotificationsActive,
                                    contentDescription = "Boost Status Notification",
                                    tint = EmeraldVolt,
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .size(20.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Active Performance Status Alert",
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Text(
                                    text = "Shows live 60 FPS lock & free RAM while gaming",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary
                                )
                            }
                            Switch(
                                checked = prefs.isBoostNotificationEnabled,
                                onCheckedChange = onToggleBoostNotification,
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("boost_notification_switch"),
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = CarbonSurface,
                                    checkedTrackColor = EmeraldVolt,
                                    uncheckedThumbColor = TextSecondary,
                                    uncheckedTrackColor = CarbonSurfaceBright
                                )
                            )
                        }
                    }
                }

                // Supported Display Refresh Modes
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    color = CarbonSurface,
                    border = BorderStroke(1.dp, BorderSlate)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.DisplaySettings,
                                contentDescription = "Display modes",
                                tint = ElectricCyan,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Display Refresh Modes",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }

                        if (telemetry.supportedDisplayModes.isEmpty()) {
                            Text(
                                text = "Default display mode active (${telemetry.currentRefreshRateHz.roundToInt()} Hz)",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        } else {
                            telemetry.supportedDisplayModes.forEach { mode ->
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (mode.isActive) CarbonSurfaceElevated else CarbonSurfaceBright.copy(alpha = 0.4f),
                                    border = BorderStroke(
                                        1.dp,
                                        if (mode.isActive) ElectricCyan.copy(alpha = 0.6f) else BorderSlate.copy(alpha = 0.5f)
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = "${mode.width} x ${mode.height}",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "Mode #${mode.modeId}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = TextMuted
                                            )
                                        }
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            if (mode.is60HzTarget) {
                                                MetricPill(
                                                    label = "TARGET",
                                                    value = "60 FPS",
                                                    accentColor = ElectricCyan
                                                )
                                            }
                                            Text(
                                                text = "${mode.refreshRateHz.roundToInt()} Hz",
                                                style = MaterialTheme.typography.titleSmall,
                                                color = if (mode.isActive) EmeraldVolt else TextSecondary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Hardware Capabilities & Session Stats
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    color = CarbonSurface,
                    border = BorderStroke(1.dp, BorderSlate)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Info,
                                contentDescription = "Device API diagnostics",
                                tint = EmeraldVolt,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Device Engine Capabilities",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }

                        CapabilityRow(
                            label = "Android API Level",
                            statusText = "API ${Build.VERSION.SDK_INT} (${Build.VERSION.RELEASE})",
                            isSupported = true
                        )
                        CapabilityRow(
                            label = "Sustained Performance Mode",
                            statusText = if (telemetry.isSustainedPerformanceSupported) "HARDWARE SUPPORTED" else "SCHEDULER FALLBACK",
                            isSupported = telemetry.isSustainedPerformanceSupported
                        )
                        CapabilityRow(
                            label = "ADPF PerformanceHintManager",
                            statusText = if (telemetry.isAdpfHintSupported) "ACTIVE (16.67ms)" else "THREAD PRIORITY",
                            isSupported = telemetry.isAdpfHintSupported
                        )
                        CapabilityRow(
                            label = "60 Hz Hardware Display Mode",
                            statusText = if (telemetry.has60HzHardwareMode) "AVAILABLE" else "WINDOW RATE HINT",
                            isSupported = telemetry.has60HzHardwareMode
                        )
                        CapabilityRow(
                            label = "Total Boost Sessions",
                            statusText = "${prefs.totalBoostSessions} SESSIONS • LAST +${prefs.lastRamReclaimedMb} MB",
                            isSupported = true
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun CapabilityRow(
    label: String,
    statusText: String,
    isSupported: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = if (isSupported) EmeraldVolt else AmberPulse,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = statusText,
                style = MaterialTheme.typography.labelSmall,
                color = if (isSupported) EmeraldVolt else AmberPulse
            )
        }
    }
}
