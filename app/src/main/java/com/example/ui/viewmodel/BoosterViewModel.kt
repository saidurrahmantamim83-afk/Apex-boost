package com.example.ui.viewmodel

import android.app.Activity
import android.app.Application
import android.view.Choreographer
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.BoostSequenceState
import com.example.data.BoosterPreferences
import com.example.data.BoosterPreferencesRepository
import com.example.data.HardwareTelemetryEngine
import com.example.data.InstalledAppInfo
import com.example.data.InstalledGamesRepository
import com.example.data.LiveFrameMetrics
import com.example.data.SystemTelemetry
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class BenchmarkLoadLevel(val label: String, val particleCount: Int) {
    LIGHT("Light", 28),
    BALANCED("Balanced", 76),
    HEAVY("Heavy Stress", 180)
}

data class BoosterUiState(
    val preferences: BoosterPreferences = BoosterPreferences(),
    val telemetry: SystemTelemetry = SystemTelemetry(),
    val frameMetrics: LiveFrameMetrics = LiveFrameMetrics(),
    val installedApps: List<InstalledAppInfo> = emptyList(),
    val boostSequence: BoostSequenceState = BoostSequenceState.Idle,
    val benchmarkLoad: BenchmarkLoadLevel = BenchmarkLoadLevel.BALANCED,
    val isSweepingRam: Boolean = false
) {
    val vaultGames: List<InstalledAppInfo>
        get() = installedApps.filter { it.isDetectedGame || it.isPinnedToVault }
}

sealed interface BoosterUiEvent {
    data class ShowMessage(val message: String) : BoosterUiEvent
    data object RequestNotificationPermission : BoosterUiEvent
}

class BoosterViewModel(application: Application) : AndroidViewModel(application) {

    private val prefsRepo = BoosterPreferencesRepository(application)
    private val telemetryEngine = HardwareTelemetryEngine(application)
    private val gamesRepo = InstalledGamesRepository(application)

    private val _telemetry = MutableStateFlow(SystemTelemetry())
    private val _frameMetrics = MutableStateFlow(LiveFrameMetrics())
    private val _installedApps = MutableStateFlow<List<InstalledAppInfo>>(emptyList())
    private val _boostSequence = MutableStateFlow<BoostSequenceState>(BoostSequenceState.Idle)
    private val _benchmarkLoad = MutableStateFlow(BenchmarkLoadLevel.BALANCED)
    private val _isSweepingRam = MutableStateFlow(false)

    private val _events = MutableSharedFlow<BoosterUiEvent>(extraBufferCapacity = 8)
    val events: SharedFlow<BoosterUiEvent> = _events.asSharedFlow()

    val preferencesFlow: StateFlow<BoosterPreferences> = prefsRepo.preferencesFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = BoosterPreferences()
    )

    val uiState: StateFlow<BoosterUiState> = combine(
        preferencesFlow,
        _telemetry,
        _frameMetrics,
        _installedApps,
        _boostSequence,
        _benchmarkLoad,
        _isSweepingRam
    ) { values ->
        val prefs = values[0] as BoosterPreferences
        val telemetry = values[1] as SystemTelemetry
        val frameMetrics = values[2] as LiveFrameMetrics
        @Suppress("UNCHECKED_CAST")
        val apps = values[3] as List<InstalledAppInfo>
        val seq = values[4] as BoostSequenceState
        val load = values[5] as BenchmarkLoadLevel
        val sweeping = values[6] as Boolean

        BoosterUiState(
            preferences = prefs,
            telemetry = telemetry,
            frameMetrics = frameMetrics,
            installedApps = apps,
            boostSequence = seq,
            benchmarkLoad = load,
            isSweepingRam = sweeping
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = BoosterUiState()
    )

    private var frameMonitorActive = false
    private val frameDeltasMs = ArrayDeque<Float>(64)
    private var lastFrameNanos = 0L
    private var totalJankCount = 0
    private var framesSincePublish = 0

    private val frameCallback = object : Choreographer.FrameCallback {
        override fun doFrame(frameTimeNanos: Long) {
            if (!frameMonitorActive) return
            if (lastFrameNanos > 0L) {
                val deltaNs = (frameTimeNanos - lastFrameNanos).coerceAtLeast(1_000_000L)
                val deltaMs = deltaNs / 1_000_000f
                if (deltaMs < 250f) {
                    if (frameDeltasMs.size >= 60) {
                        frameDeltasMs.removeFirst()
                    }
                    frameDeltasMs.addLast(deltaMs)
                    if (deltaMs > 18.8f) {
                        totalJankCount++
                    }
                    if (preferencesFlow.value.isMaxPerformanceLocked) {
                        telemetryEngine.reportAdpfFrameDuration(deltaNs)
                    }
                    framesSincePublish++
                    if (framesSincePublish >= 8 && frameDeltasMs.isNotEmpty()) {
                        framesSincePublish = 0
                        publishFrameMetrics()
                    }
                }
            }
            lastFrameNanos = frameTimeNanos
            Choreographer.getInstance().postFrameCallback(this)
        }
    }

    init {
        startTelemetryPolling()
        observeInstalledApps()
    }

    private fun startTelemetryPolling() {
        viewModelScope.launch {
            while (isActive) {
                val snapshot = telemetryEngine.collectTelemetry()
                _telemetry.value = snapshot
                val prefs = preferencesFlow.value
                telemetryEngine.updateBoostStatusNotification(
                    enabled = prefs.isBoostNotificationEnabled,
                    is60FpsLocked = prefs.is60FpsLocked,
                    isMaxPerfLocked = prefs.isMaxPerformanceLocked,
                    availRamMb = snapshot.availableRamMb
                )
                delay(1_200L)
            }
        }
    }

    private fun observeInstalledApps() {
        viewModelScope.launch {
            preferencesFlow.collect { prefs ->
                _installedApps.value = gamesRepo.loadLaunchableApps(
                    pinnedPackages = prefs.pinnedGamePackages,
                    perGame60FpsPackages = prefs.perGame60FpsPackages
                )
            }
        }
    }

    fun startFrameMonitoring() {
        if (frameMonitorActive) return
        frameMonitorActive = true
        lastFrameNanos = 0L
        Choreographer.getInstance().postFrameCallback(frameCallback)
    }

    fun stopFrameMonitoring() {
        frameMonitorActive = false
        Choreographer.getInstance().removeFrameCallback(frameCallback)
    }

    private fun publishFrameMetrics() {
        val samples = frameDeltasMs.toList()
        if (samples.isEmpty()) return
        val avgMs = samples.average().toFloat().coerceAtLeast(1f)
        val liveFps = (1000f / avgMs).coerceIn(1f, 240f)
        val sortedDesc = samples.sortedDescending()
        val worstSlice = sortedDesc.take((samples.size / 10).coerceAtLeast(1))
        val worstAvgMs = worstSlice.average().toFloat().coerceAtLeast(1f)
        val onePercentLow = (1000f / worstAvgMs).coerceIn(1f, liveFps)

        val prefs = preferencesFlow.value
        _frameMetrics.value = LiveFrameMetrics(
            currentFps = liveFps,
            averageFrameTimeMs = avgMs,
            onePercentLowFps = onePercentLow,
            jankFrameCount = totalJankCount,
            recentFrameTimesMs = samples.takeLast(40),
            targetFps = if (prefs.is60FpsLocked) 60 else _telemetry.value.currentRefreshRateHz.toInt()
        )
    }

    fun resetFrameMetricsCounters() {
        frameDeltasMs.clear()
        totalJankCount = 0
        _frameMetrics.update {
            it.copy(jankFrameCount = 0, recentFrameTimesMs = emptyList())
        }
    }

    fun syncWindowLocks(activity: Activity) {
        val prefs = preferencesFlow.value
        telemetryEngine.applyWindowPerformanceLocks(
            activity = activity,
            lock60Fps = prefs.is60FpsLocked,
            lockMaxPerformance = prefs.isMaxPerformanceLocked,
            boostCpuThreads = prefs.isCpuPriorityEnabled
        )
    }

    fun toggleMaxPerformance(enabled: Boolean) {
        viewModelScope.launch {
            prefsRepo.setMaxPerformanceLocked(enabled)
            if (!enabled) {
                _boostSequence.value = BoostSequenceState.Idle
            }
            refreshTelemetryOnce()
        }
    }

    fun toggle60FpsLock(enabled: Boolean) {
        viewModelScope.launch {
            prefsRepo.set60FpsLocked(enabled)
            refreshTelemetryOnce()
            _events.tryEmit(
                BoosterUiEvent.ShowMessage(
                    if (enabled) "60 FPS frame rate lock engaged" else "60 FPS lock released"
                )
            )
        }
    }

    fun toggleCpuPriority(enabled: Boolean) {
        viewModelScope.launch {
            prefsRepo.setCpuPriorityEnabled(enabled)
        }
    }

    fun toggleAutoRamSweep(enabled: Boolean) {
        viewModelScope.launch {
            prefsRepo.setAutoRamSweepEnabled(enabled)
        }
    }

    fun toggleDndShield(enabled: Boolean) {
        viewModelScope.launch {
            if (enabled && !_telemetry.value.isNotificationPolicyGranted) {
                telemetryEngine.openNotificationPolicySettings()
                _events.tryEmit(
                    BoosterUiEvent.ShowMessage("Grant Do Not Disturb access to enable Gaming Shield")
                )
                return@launch
            }
            val applied = telemetryEngine.applyDndGamingShield(enabled)
            prefsRepo.setDndShieldEnabled(if (enabled) applied else false)
        }
    }

    fun toggleBoostNotification(enabled: Boolean) {
        viewModelScope.launch {
            if (enabled) {
                _events.tryEmit(BoosterUiEvent.RequestNotificationPermission)
            }
            prefsRepo.setBoostNotificationEnabled(enabled)
            refreshTelemetryOnce()
        }
    }

    fun onNotificationPermissionResult(granted: Boolean) {
        viewModelScope.launch {
            if (!granted) {
                prefsRepo.setBoostNotificationEnabled(false)
                _events.tryEmit(
                    BoosterUiEvent.ShowMessage("Notification permission denied; status HUD disabled")
                )
            } else {
                refreshTelemetryOnce()
            }
        }
    }

    fun setBenchmarkLoad(level: BenchmarkLoadLevel) {
        _benchmarkLoad.value = level
    }

    fun runRamSweepOnly() {
        if (_isSweepingRam.value) return
        viewModelScope.launch {
            _isSweepingRam.value = true
            val result = telemetryEngine.performRamOptimization()
            prefsRepo.recordRamSweep(result.reclaimedMb)
            refreshTelemetryOnce()
            _isSweepingRam.value = false
            val msg = if (result.reclaimedMb > 0) {
                "Swept ${result.processesSwept} background apps • Freed ${result.reclaimedMb} MB RAM"
            } else {
                "Swept ${result.processesSwept} background processes • ${result.afterAvailMb} MB RAM ready"
            }
            _events.tryEmit(BoosterUiEvent.ShowMessage(msg))
        }
    }

    fun runFullBoostSequence() {
        if (_boostSequence.value is BoostSequenceState.Optimizing) return
        viewModelScope.launch {
            _boostSequence.value = BoostSequenceState.Optimizing("Sweeping background RAM...", 0.25f)
            val ramResult = telemetryEngine.performRamOptimization()
            delay(260L)

            _boostSequence.value = BoostSequenceState.Optimizing("Elevating CPU thread priority...", 0.58f)
            prefsRepo.setCpuPriorityEnabled(true)
            delay(240L)

            _boostSequence.value = BoostSequenceState.Optimizing("Locking 60 FPS & Sustained Perf...", 0.88f)
            prefsRepo.recordBoostSession(
                reclaimedMb = ramResult.reclaimedMb,
                timestampMs = System.currentTimeMillis()
            )
            if (preferencesFlow.value.isDndShieldEnabled) {
                telemetryEngine.applyDndGamingShield(true)
            }
            refreshTelemetryOnce()
            delay(220L)

            _boostSequence.value = BoostSequenceState.Completed(
                reclaimedMb = ramResult.reclaimedMb,
                processesSwept = ramResult.processesSwept,
                fpsLockedTo60 = true,
                maxPerfActive = true
            )
            _events.tryEmit(
                BoosterUiEvent.ShowMessage(
                    "Max Performance & 60 FPS Locked • ${ramResult.afterAvailMb} MB RAM available"
                )
            )
        }
    }

    fun releasePerformanceLock() {
        viewModelScope.launch {
            prefsRepo.setMaxPerformanceLocked(false)
            if (preferencesFlow.value.isDndShieldEnabled) {
                telemetryEngine.applyDndGamingShield(false)
            }
            _boostSequence.value = BoostSequenceState.Idle
            refreshTelemetryOnce()
            _events.tryEmit(BoosterUiEvent.ShowMessage("Performance lock disengaged"))
        }
    }

    fun launchGameWithBoost(app: InstalledAppInfo) {
        viewModelScope.launch {
            _boostSequence.value = BoostSequenceState.Optimizing("Boosting ${app.label}...", 0.35f)
            val ramResult = if (preferencesFlow.value.isAutoRamSweepEnabled) {
                telemetryEngine.performRamOptimization()
            } else {
                null
            }
            prefsRepo.set60FpsLocked(app.is60FpsLocked)
            prefsRepo.recordBoostSession(
                reclaimedMb = ramResult?.reclaimedMb ?: 0,
                timestampMs = System.currentTimeMillis()
            )
            refreshTelemetryOnce()
            _boostSequence.value = BoostSequenceState.Completed(
                reclaimedMb = ramResult?.reclaimedMb ?: 0,
                processesSwept = ramResult?.processesSwept ?: 0,
                fpsLockedTo60 = app.is60FpsLocked,
                maxPerfActive = true
            )
            val launched = gamesRepo.launchPackage(app.packageName)
            if (!launched) {
                _events.tryEmit(BoosterUiEvent.ShowMessage("Unable to launch ${app.label}"))
            }
        }
    }

    fun togglePinAppToVault(packageName: String) {
        viewModelScope.launch {
            prefsRepo.togglePinnedGame(packageName)
        }
    }

    fun togglePerGame60Fps(packageName: String) {
        viewModelScope.launch {
            prefsRepo.togglePerGame60Fps(packageName)
        }
    }

    fun openDndSettings() {
        telemetryEngine.openNotificationPolicySettings()
    }

    fun refreshTelemetryOnce() {
        viewModelScope.launch {
            _telemetry.value = telemetryEngine.collectTelemetry()
        }
    }

    override fun onCleared() {
        stopFrameMonitoring()
        telemetryEngine.releaseSession()
        super.onCleared()
    }
}
