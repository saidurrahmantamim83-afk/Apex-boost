package com.example.data

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class InstalledGamesRepository(private val context: Context) {

    suspend fun loadLaunchableApps(
        pinnedPackages: Set<String>,
        perGame60FpsPackages: Set<String>
    ): List<InstalledAppInfo> = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolveInfos = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.queryIntentActivities(
                mainIntent,
                PackageManager.ResolveInfoFlags.of(0L)
            )
        } else {
            @Suppress("DEPRECATION")
            pm.queryIntentActivities(mainIntent, 0)
        }

        val selfPackage = context.packageName
        resolveInfos
            .asSequence()
            .mapNotNull { info ->
                val activityInfo = info.activityInfo ?: return@mapNotNull null
                val appInfo = activityInfo.applicationInfo ?: return@mapNotNull null
                val pkg = activityInfo.packageName
                if (pkg == selfPackage) return@mapNotNull null

                val isGame = isAppGame(appInfo)
                val label = runCatching { info.loadLabel(pm).toString() }.getOrDefault(pkg)
                val icon = runCatching { info.loadIcon(pm) }.getOrNull()

                InstalledAppInfo(
                    packageName = pkg,
                    label = label,
                    icon = icon,
                    isDetectedGame = isGame,
                    isPinnedToVault = pkg in pinnedPackages,
                    is60FpsLocked = pkg in perGame60FpsPackages || isGame
                )
            }
            .distinctBy { it.packageName }
            .sortedWith(
                compareByDescending<InstalledAppInfo> { it.isDetectedGame || it.isPinnedToVault }
                    .thenBy { it.label.lowercase() }
            )
            .toList()
    }

    fun launchPackage(packageName: String): Boolean {
        val intent = context.packageManager.getLaunchIntentForPackage(packageName) ?: return false
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return runCatching {
            context.startActivity(intent)
            true
        }.getOrDefault(false)
    }

    private fun isAppGame(appInfo: ApplicationInfo): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (appInfo.category == ApplicationInfo.CATEGORY_GAME) return true
        }
        @Suppress("DEPRECATION")
        return (appInfo.flags and ApplicationInfo.FLAG_IS_GAME) != 0
    }
}
