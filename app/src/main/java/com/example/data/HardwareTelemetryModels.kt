package com.example.data

import android.graphics.drawable.Drawable

enum class ThermalHealth(val label: String) {
    OPTIMAL("COOL"),
    WARM("WARM"),
    THROTTLED("THROTTLED"),
    CRITICAL("CRITICAL")
}

data class CpuCoreTelemetry(
    val coreIndex: Int,
    val currentMhz: Int?,
    val maxMhz: Int?,
    val minMhz: Int?,
    val governor: String?
) {
    val loadFraction: Float
        get() {
            val cur = currentMhz ?: return 0f
            val max = maxMhz ?: return 0f
            if (max <= 0) return 0f
            return (cur.toFloat() / max.toFloat()).coerceIn(0.05f, 1f)
        }
}

data class DisplayModeInfo(
    val modeId: Int,
    val width: Int,
    val height: Int,
    val refreshRateHz: Float,
    val isActive: Boolean,
    val is60HzTarget: Boolean
)

data class SystemTelemetry(
    val totalRamMb: Long = 0L,
    val availableRamMb: Long = 0L,
    val usedRamMb: Long = 0L,
    val ramThresholdMb: Long = 0L,
    val cachedRamMb: Long? = null,
    val isLowMemory: Boolean = false,
    val cpuCoresCount: Int = 1,
    val cpuCores: List<CpuCoreTelemetry> = emptyList(),
    val socLabel: String = "ARM64",
    val abiLabel: String = "arm64-v8a",
    val cpuActiveTimeMs: Long = 0L,
    val thermalHealth: ThermalHealth = ThermalHealth.OPTIMAL,
    val isPowerSaveMode: Boolean = false,
    val isSustainedPerformanceSupported: Boolean = false,
    val isAdpfHintSupported: Boolean = false,
    val currentRefreshRateHz: Float = 60f,
    val supportedDisplayModes: List<DisplayModeInfo> = emptyList(),
    val has60HzHardwareMode: Boolean = false,
    val isNotificationPolicyGranted: Boolean = false
) {
    val ramUsageFraction: Float
        get() = if (totalRamMb > 0L) {
            (usedRamMb.toFloat() / totalRamMb.toFloat()).coerceIn(0f, 1f)
        } else {
            0f
        }

    val ramFreePercent: Int
        get() = if (totalRamMb > 0L) {
            ((availableRamMb * 100L) / totalRamMb).toInt().coerceIn(0, 100)
        } else {
            0
        }

    val readinessScore: Int
        get() {
            val ramScore = (ramFreePercent * 0.55f)
            val thermalPenalty = when (thermalHealth) {
                ThermalHealth.OPTIMAL -> 0f
                ThermalHealth.WARM -> 8f
                ThermalHealth.THROTTLED -> 22f
                ThermalHealth.CRITICAL -> 40f
            }
            val powerSavePenalty = if (isPowerSaveMode) 18f else 0f
            val base = 52f + ramScore - thermalPenalty - powerSavePenalty
            return base.toInt().coerceIn(20, 100)
        }
}

data class LiveFrameMetrics(
    val currentFps: Float = 60f,
    val averageFrameTimeMs: Float = 16.67f,
    val onePercentLowFps: Float = 60f,
    val jankFrameCount: Int = 0,
    val recentFrameTimesMs: List<Float> = emptyList(),
    val targetFps: Int = 60
)

data class RamSweepResult(
    val beforeAvailMb: Long,
    val afterAvailMb: Long,
    val reclaimedMb: Int,
    val processesSwept: Int
)

sealed interface BoostSequenceState {
    data object Idle : BoostSequenceState
    data class Optimizing(val stepLabel: String, val progress: Float) : BoostSequenceState
    data class Completed(
        val reclaimedMb: Int,
        val processesSwept: Int,
        val fpsLockedTo60: Boolean,
        val maxPerfActive: Boolean
    ) : BoostSequenceState
}

data class InstalledAppInfo(
    val packageName: String,
    val label: String,
    val icon: Drawable?,
    val isDetectedGame: Boolean,
    val isPinnedToVault: Boolean,
    val is60FpsLocked: Boolean
)
