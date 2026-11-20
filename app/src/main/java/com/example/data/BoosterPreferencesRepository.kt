package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.boosterDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "apex_boost_preferences"
)

data class BoosterPreferences(
    val isMaxPerformanceLocked: Boolean = false,
    val is60FpsLocked: Boolean = true,
    val isCpuPriorityEnabled: Boolean = true,
    val isAutoRamSweepEnabled: Boolean = true,
    val isDndShieldEnabled: Boolean = false,
    val isBoostNotificationEnabled: Boolean = false,
    val pinnedGamePackages: Set<String> = emptySet(),
    val perGame60FpsPackages: Set<String> = emptySet(),
    val lastBoostTimestamp: Long = 0L,
    val lastRamReclaimedMb: Int = 0,
    val totalBoostSessions: Int = 0
)

class BoosterPreferencesRepository(private val context: Context) {

    private object Keys {
        val MAX_PERFORMANCE_LOCKED = booleanPreferencesKey("max_performance_locked")
        val FPS_60_LOCKED = booleanPreferencesKey("fps_60_locked")
        val CPU_PRIORITY_ENABLED = booleanPreferencesKey("cpu_priority_enabled")
        val AUTO_RAM_SWEEP_ENABLED = booleanPreferencesKey("auto_ram_sweep_enabled")
        val DND_SHIELD_ENABLED = booleanPreferencesKey("dnd_shield_enabled")
        val BOOST_NOTIFICATION_ENABLED = booleanPreferencesKey("boost_notification_enabled")
        val PINNED_GAME_PACKAGES = stringSetPreferencesKey("pinned_game_packages")
        val PER_GAME_60_FPS_PACKAGES = stringSetPreferencesKey("per_game_60_fps_packages")
        val LAST_BOOST_TIMESTAMP = longPreferencesKey("last_boost_timestamp")
        val LAST_RAM_RECLAIMED_MB = intPreferencesKey("last_ram_reclaimed_mb")
        val TOTAL_BOOST_SESSIONS = intPreferencesKey("total_boost_sessions")
    }

    val preferencesFlow: Flow<BoosterPreferences> = context.boosterDataStore.data.map { prefs ->
        BoosterPreferences(
            isMaxPerformanceLocked = prefs[Keys.MAX_PERFORMANCE_LOCKED] ?: false,
            is60FpsLocked = prefs[Keys.FPS_60_LOCKED] ?: true,
            isCpuPriorityEnabled = prefs[Keys.CPU_PRIORITY_ENABLED] ?: true,
            isAutoRamSweepEnabled = prefs[Keys.AUTO_RAM_SWEEP_ENABLED] ?: true,
            isDndShieldEnabled = prefs[Keys.DND_SHIELD_ENABLED] ?: false,
            isBoostNotificationEnabled = prefs[Keys.BOOST_NOTIFICATION_ENABLED] ?: false,
            pinnedGamePackages = prefs[Keys.PINNED_GAME_PACKAGES] ?: emptySet(),
            perGame60FpsPackages = prefs[Keys.PER_GAME_60_FPS_PACKAGES] ?: emptySet(),
            lastBoostTimestamp = prefs[Keys.LAST_BOOST_TIMESTAMP] ?: 0L,
            lastRamReclaimedMb = prefs[Keys.LAST_RAM_RECLAIMED_MB] ?: 0,
            totalBoostSessions = prefs[Keys.TOTAL_BOOST_SESSIONS] ?: 0
        )
    }

    suspend fun setMaxPerformanceLocked(enabled: Boolean) {
        context.boosterDataStore.edit { it[Keys.MAX_PERFORMANCE_LOCKED] = enabled }
    }

    suspend fun set60FpsLocked(enabled: Boolean) {
        context.boosterDataStore.edit { it[Keys.FPS_60_LOCKED] = enabled }
    }

    suspend fun setCpuPriorityEnabled(enabled: Boolean) {
        context.boosterDataStore.edit { it[Keys.CPU_PRIORITY_ENABLED] = enabled }
    }

    suspend fun setAutoRamSweepEnabled(enabled: Boolean) {
        context.boosterDataStore.edit { it[Keys.AUTO_RAM_SWEEP_ENABLED] = enabled }
    }

    suspend fun setDndShieldEnabled(enabled: Boolean) {
        context.boosterDataStore.edit { it[Keys.DND_SHIELD_ENABLED] = enabled }
    }

    suspend fun setBoostNotificationEnabled(enabled: Boolean) {
        context.boosterDataStore.edit { it[Keys.BOOST_NOTIFICATION_ENABLED] = enabled }
    }

    suspend fun togglePinnedGame(packageName: String) {
        context.boosterDataStore.edit { prefs ->
            val current = prefs[Keys.PINNED_GAME_PACKAGES] ?: emptySet()
            prefs[Keys.PINNED_GAME_PACKAGES] = if (packageName in current) {
                current - packageName
            } else {
                current + packageName
            }
        }
    }

    suspend fun togglePerGame60Fps(packageName: String) {
        context.boosterDataStore.edit { prefs ->
            val current = prefs[Keys.PER_GAME_60_FPS_PACKAGES] ?: emptySet()
            prefs[Keys.PER_GAME_60_FPS_PACKAGES] = if (packageName in current) {
                current - packageName
            } else {
                current + packageName
            }
        }
    }

    suspend fun recordBoostSession(reclaimedMb: Int, timestampMs: Long) {
        context.boosterDataStore.edit { prefs ->
            prefs[Keys.MAX_PERFORMANCE_LOCKED] = true
            prefs[Keys.FPS_60_LOCKED] = true
            prefs[Keys.LAST_BOOST_TIMESTAMP] = timestampMs
            prefs[Keys.LAST_RAM_RECLAIMED_MB] = reclaimedMb
            prefs[Keys.TOTAL_BOOST_SESSIONS] = (prefs[Keys.TOTAL_BOOST_SESSIONS] ?: 0) + 1
        }
    }

    suspend fun recordRamSweep(reclaimedMb: Int) {
        context.boosterDataStore.edit { prefs ->
            prefs[Keys.LAST_RAM_RECLAIMED_MB] = reclaimedMb
        }
    }
}
