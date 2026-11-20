package com.example.data

import android.Manifest
import android.app.Activity
import android.app.ActivityManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.display.DisplayManager
import android.os.Build
import android.os.PerformanceHintManager
import android.os.PowerManager
import android.os.Process
import android.provider.Settings
import android.view.Display
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.R
import java.io.File
import kotlin.math.abs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class HardwareTelemetryEngine(private val context: Context) {

    private val activityManager =
        context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    private val powerManager =
        context.getSystemService(Context.POWER_SERVICE) as PowerManager
    private val displayManager =
        context.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private var hintSession: PerformanceHintManager.Session? = null

    suspend fun collectTelemetry(): SystemTelemetry = withContext(Dispatchers.IO) {
        val memInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memInfo)

        val totalMb = memInfo.totalMem / BYTES_PER_MB
        val availMb = memInfo.availMem / BYTES_PER_MB
        val usedMb = (totalMb - availMb).coerceAtLeast(0L)
        val thresholdMb = memInfo.threshold / BYTES_PER_MB
        val cachedMb = readCachedMemMb()

        val coreCount = Runtime.getRuntime().availableProcessors().coerceAtLeast(1)
        val cpuCores = readCpuCoreFrequencies(coreCount)

        val socLabel = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Build.SOC_MODEL.takeIf { it.isNotBlank() && it != Build.UNKNOWN } ?: Build.HARDWARE
        } else {
            Build.HARDWARE
        }
        val abiLabel = Build.SUPPORTED_ABIS.firstOrNull() ?: "arm64-v8a"

        val thermal = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            when (powerManager.currentThermalStatus) {
                PowerManager.THERMAL_STATUS_NONE -> ThermalHealth.OPTIMAL
                PowerManager.THERMAL_STATUS_LIGHT -> ThermalHealth.WARM
                PowerManager.THERMAL_STATUS_MODERATE,
                PowerManager.THERMAL_STATUS_SEVERE -> ThermalHealth.THROTTLED
                else -> ThermalHealth.CRITICAL
            }
        } else {
            ThermalHealth.OPTIMAL
        }

        val displayModes = collectDisplayModes()
        val activeDisplay = displayManager.getDisplay(Display.DEFAULT_DISPLAY)
        val currentHz = activeDisplay?.mode?.refreshRate ?: 60f
        val has60Hz = displayModes.any { it.is60HzTarget }

        val adpfSupported = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val phm = context.getSystemService(PerformanceHintManager::class.java)
            phm != null && phm.preferredUpdateRateNanos > 0L
        } else {
            false
        }

        SystemTelemetry(
            totalRamMb = totalMb,
            availableRamMb = availMb,
            usedRamMb = usedMb,
            ramThresholdMb = thresholdMb,
            cachedRamMb = cachedMb,
            isLowMemory = memInfo.lowMemory,
            cpuCoresCount = coreCount,
            cpuCores = cpuCores,
            socLabel = socLabel.uppercase(),
            abiLabel = abiLabel,
            cpuActiveTimeMs = Process.getElapsedCpuTime(),
            thermalHealth = thermal,
            isPowerSaveMode = powerManager.isPowerSaveMode,
            isSustainedPerformanceSupported = powerManager.isSustainedPerformanceModeSupported,
            isAdpfHintSupported = adpfSupported,
            currentRefreshRateHz = currentHz,
            supportedDisplayModes = displayModes,
            has60HzHardwareMode = has60Hz,
            isNotificationPolicyGranted = notificationManager.isNotificationPolicyAccessGranted
        )
    }

    suspend fun performRamOptimization(): RamSweepResult = withContext(Dispatchers.IO) {
        val beforeInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(beforeInfo)
        val beforeAvailMb = beforeInfo.availMem / BYTES_PER_MB

        val pm = context.packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val packages = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.queryIntentActivities(mainIntent, PackageManager.ResolveInfoFlags.of(0L))
        } else {
            @Suppress("DEPRECATION")
            pm.queryIntentActivities(mainIntent, 0)
        }

        val selfPkg = context.packageName
        var sweptCount = 0
        packages
            .asSequence()
            .mapNotNull { it.activityInfo?.packageName }
            .distinct()
            .filter { it != selfPkg }
            .forEach { pkg ->
                runCatching {
                    activityManager.killBackgroundProcesses(pkg)
                    sweptCount++
                }
            }

        runCatching {
            System.runFinalization()
            Runtime.getRuntime().gc()
        }

        val afterInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(afterInfo)
        val afterAvailMb = afterInfo.availMem / BYTES_PER_MB
        val reclaimedMb = (afterAvailMb - beforeAvailMb).toInt().coerceAtLeast(0)

        RamSweepResult(
            beforeAvailMb = beforeAvailMb,
            afterAvailMb = afterAvailMb,
            reclaimedMb = reclaimedMb,
            processesSwept = sweptCount
        )
    }

    fun applyWindowPerformanceLocks(
        activity: Activity,
        lock60Fps: Boolean,
        lockMaxPerformance: Boolean,
        boostCpuThreads: Boolean
    ) {
        val window = activity.window
        val params = window.attributes

        // 1. 60 FPS Display Mode & Refresh Rate Lock
        val display = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            activity.display
        } else {
            @Suppress("DEPRECATION")
            activity.windowManager.defaultDisplay
        }

        if (lock60Fps && display != null) {
            val activeMode = display.mode
            val best60HzMode = display.supportedModes
                .filter {
                    it.physicalWidth == activeMode.physicalWidth &&
                        it.physicalHeight == activeMode.physicalHeight
                }
                .minByOrNull { abs(it.refreshRate - TARGET_FPS) }

            if (best60HzMode != null && abs(best60HzMode.refreshRate - TARGET_FPS) <= 3f) {
                params.preferredDisplayModeId = best60HzMode.modeId
            }
            params.preferredRefreshRate = TARGET_FPS
        } else {
            params.preferredDisplayModeId = 0
            params.preferredRefreshRate = 0f
        }
        window.attributes = params

        // 2. Sustained Performance Mode & Keep Screen On
        if (powerManager.isSustainedPerformanceModeSupported) {
            window.setSustainedPerformanceMode(lockMaxPerformance)
        }
        if (lockMaxPerformance) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }

        // 3. Thread Priority & ADPF Performance Hint Session (API 31+)
        runCatching {
            val priority = if (lockMaxPerformance && boostCpuThreads) {
                Process.THREAD_PRIORITY_URGENT_DISPLAY
            } else {
                Process.THREAD_PRIORITY_DEFAULT
            }
            Process.setThreadPriority(priority)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            updateAdpfHintSession(enabled = lockMaxPerformance)
        }
    }

    fun reportAdpfFrameDuration(workDurationNanos: Long) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && workDurationNanos > 0L) {
            runCatching {
                hintSession?.reportActualWorkDuration(workDurationNanos)
            }
        }
    }

    fun applyDndGamingShield(enabled: Boolean): Boolean {
        if (!notificationManager.isNotificationPolicyAccessGranted) return false
        return runCatching {
            notificationManager.setInterruptionFilter(
                if (enabled) {
                    NotificationManager.INTERRUPTION_FILTER_PRIORITY
                } else {
                    NotificationManager.INTERRUPTION_FILTER_ALL
                }
            )
            true
        }.getOrDefault(false)
    }

    fun openNotificationPolicySettings() {
        val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching { context.startActivity(intent) }
    }

    fun updateBoostStatusNotification(
        enabled: Boolean,
        is60FpsLocked: Boolean,
        isMaxPerfLocked: Boolean,
        availRamMb: Long
    ) {
        if (!enabled || (!is60FpsLocked && !isMaxPerfLocked)) {
            notificationManager.cancel(BOOST_NOTIFICATION_ID)
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                BOOST_CHANNEL_ID,
                "ApexBoost Performance Status",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows active 60 FPS lock and performance optimization status"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val statusParts = buildList {
            if (isMaxPerfLocked) add("Max Performance Locked")
            if (is60FpsLocked) add("60 FPS Lock")
            add("${availRamMb} MB RAM Free")
        }

        val notification = NotificationCompat.Builder(context, BOOST_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("ApexBoost Engine Active")
            .setContentText(statusParts.joinToString(" • "))
            .setOngoing(true)
            .setSilent(true)
            .build()

        notificationManager.notify(BOOST_NOTIFICATION_ID, notification)
    }

    fun releaseSession() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            runCatching { hintSession?.close() }
            hintSession = null
        }
        notificationManager.cancel(BOOST_NOTIFICATION_ID)
    }

    private fun updateAdpfHintSession(enabled: Boolean) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return
        val phm = context.getSystemService(PerformanceHintManager::class.java) ?: return
        if (enabled) {
            if (hintSession == null) {
                runCatching {
                    hintSession = phm.createHintSession(
                        intArrayOf(Process.myTid()),
                        TARGET_FRAME_BUDGET_NS
                    )
                }
            }
        } else {
            runCatching { hintSession?.close() }
            hintSession = null
        }
    }

    private fun collectDisplayModes(): List<DisplayModeInfo> {
        val display = displayManager.getDisplay(Display.DEFAULT_DISPLAY) ?: return emptyList()
        val activeId = display.mode.modeId
        return display.supportedModes
            .map { mode ->
                DisplayModeInfo(
                    modeId = mode.modeId,
                    width = mode.physicalWidth,
                    height = mode.physicalHeight,
                    refreshRateHz = mode.refreshRate,
                    isActive = mode.modeId == activeId,
                    is60HzTarget = abs(mode.refreshRate - TARGET_FPS) <= 2.5f
                )
            }
            .sortedWith(
                compareByDescending<DisplayModeInfo> { it.isActive }
                    .thenBy { it.refreshRateHz }
            )
    }

    private fun readCpuCoreFrequencies(coreCount: Int): List<CpuCoreTelemetry> {
        return (0 until coreCount).map { index ->
            val base = "/sys/devices/system/cpu/cpu$index/cpufreq"
            val curKhz = readSysInt("$base/scaling_cur_freq")
            val maxKhz = readSysInt("$base/cpuinfo_max_freq")
            val minKhz = readSysInt("$base/cpuinfo_min_freq")
            val governor = readSysString("$base/scaling_governor")

            CpuCoreTelemetry(
                coreIndex = index,
                currentMhz = curKhz?.div(1000),
                maxMhz = maxKhz?.div(1000),
                minMhz = minKhz?.div(1000),
                governor = governor
            )
        }
    }

    private fun readCachedMemMb(): Long? {
        return runCatching {
            val file = File("/proc/meminfo")
            if (!file.canRead()) return@runCatching null
            file.useLines { lines ->
                val cachedLine = lines.firstOrNull { it.startsWith("Cached:") } ?: return@useLines null
                val kb = cachedLine
                    .substringAfter(":")
                    .trim()
                    .substringBefore(" ")
                    .toLongOrNull() ?: return@useLines null
                kb / 1024L
            }
        }.getOrNull()
    }

    private fun readSysInt(path: String): Int? = runCatching {
        val file = File(path)
        if (!file.canRead()) return@runCatching null
        file.readText().trim().toIntOrNull()
    }.getOrNull()

    private fun readSysString(path: String): String? = runCatching {
        val file = File(path)
        if (!file.canRead()) return@runCatching null
        file.readText().trim().takeIf { it.isNotEmpty() }
    }.getOrNull()

    companion object {
        private const val BYTES_PER_MB = 1024L * 1024L
        private const val TARGET_FPS = 60f
        private const val TARGET_FRAME_BUDGET_NS = 16_666_666L
        private const val BOOST_CHANNEL_ID = "apex_boost_status"
        private const val BOOST_NOTIFICATION_ID = 6001
    }
}
