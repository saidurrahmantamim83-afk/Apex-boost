package com.example.ui

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.R
import com.example.ui.navigation.NavRoutes
import com.example.ui.screens.CockpitScreen
import com.example.ui.screens.FpsLabScreen
import com.example.ui.screens.GameVaultScreen
import com.example.ui.screens.TuningScreen
import com.example.ui.theme.CarbonSurface
import com.example.ui.theme.CyanContainer
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.BoosterUiEvent
import com.example.ui.viewmodel.BoosterViewModel

private data class TopLevelDestination(
    val route: String,
    val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

private val topLevelDestinations = listOf(
    TopLevelDestination(
        route = NavRoutes.ROUTE_COCKPIT,
        labelRes = R.string.nav_cockpit,
        selectedIcon = Icons.Filled.Bolt,
        unselectedIcon = Icons.Outlined.Bolt,
        testTag = "nav_cockpit"
    ),
    TopLevelDestination(
        route = NavRoutes.ROUTE_VAULT,
        labelRes = R.string.nav_games,
        selectedIcon = Icons.Filled.SportsEsports,
        unselectedIcon = Icons.Outlined.SportsEsports,
        testTag = "nav_vault"
    ),
    TopLevelDestination(
        route = NavRoutes.ROUTE_FPS_LAB,
        labelRes = R.string.nav_fps_lab,
        selectedIcon = Icons.Filled.Speed,
        unselectedIcon = Icons.Outlined.Speed,
        testTag = "nav_fps_lab"
    ),
    TopLevelDestination(
        route = NavRoutes.ROUTE_TUNING,
        labelRes = R.string.nav_settings,
        selectedIcon = Icons.Filled.Tune,
        unselectedIcon = Icons.Outlined.Tune,
        testTag = "nav_tuning"
    )
)

@Composable
fun ApexBoostApp(
    viewModel: BoosterViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val snackbarHostState = remember { SnackbarHostState() }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        viewModel.onNotificationPermissionResult(granted)
    }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is BoosterUiEvent.ShowMessage -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                BoosterUiEvent.RequestNotificationPermission -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        viewModel.onNotificationPermissionResult(true)
                    }
                }
            }
        }
    }

    fun navigateTopLevel(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBg)
    ) {
        val useNavRail = maxWidth >= 600.dp

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = ObsidianBg,
            contentWindowInsets = WindowInsets.safeDrawing,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            bottomBar = {
                if (!useNavRail) {
                    NavigationBar(
                        containerColor = CarbonSurface,
                        tonalElevation = 0.dp
                    ) {
                        topLevelDestinations.forEach { dest ->
                            val selected = currentDestination?.hierarchy?.any {
                                it.route == dest.route
                            } == true
                            val label = stringResource(dest.labelRes)
                            NavigationBarItem(
                                selected = selected,
                                onClick = { navigateTopLevel(dest.route) },
                                modifier = Modifier.testTag(dest.testTag),
                                icon = {
                                    Icon(
                                        imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                        contentDescription = label
                                    )
                                },
                                label = {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = ElectricCyan,
                                    selectedTextColor = ElectricCyan,
                                    indicatorColor = CyanContainer,
                                    unselectedIconColor = TextSecondary,
                                    unselectedTextColor = TextSecondary
                                )
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (useNavRail) {
                    NavigationRail(
                        modifier = Modifier.fillMaxHeight(),
                        containerColor = CarbonSurface
                    ) {
                        topLevelDestinations.forEach { dest ->
                            val selected = currentDestination?.hierarchy?.any {
                                it.route == dest.route
                            } == true
                            val label = stringResource(dest.labelRes)
                            NavigationRailItem(
                                selected = selected,
                                onClick = { navigateTopLevel(dest.route) },
                                modifier = Modifier.testTag(dest.testTag),
                                icon = {
                                    Icon(
                                        imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                        contentDescription = label
                                    )
                                },
                                label = {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                },
                                colors = NavigationRailItemDefaults.colors(
                                    selectedIconColor = ElectricCyan,
                                    selectedTextColor = ElectricCyan,
                                    indicatorColor = CyanContainer,
                                    unselectedIconColor = TextSecondary,
                                    unselectedTextColor = TextSecondary
                                )
                            )
                        }
                    }
                }

                NavHost(
                    navController = navController,
                    startDestination = NavRoutes.ROUTE_COCKPIT,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    composable(NavRoutes.ROUTE_COCKPIT) {
                        CockpitScreen(
                            uiState = uiState,
                            onBoostNow = viewModel::runFullBoostSequence,
                            onReleaseLock = viewModel::releasePerformanceLock,
                            onToggle60Fps = viewModel::toggle60FpsLock,
                            onToggleMaxPerf = viewModel::toggleMaxPerformance,
                            onToggleCpuPriority = viewModel::toggleCpuPriority,
                            onToggleAutoRam = viewModel::toggleAutoRamSweep,
                            onSweepRamNow = viewModel::runRamSweepOnly,
                            onOpenFpsLab = { navigateTopLevel(NavRoutes.ROUTE_FPS_LAB) }
                        )
                    }
                    composable(NavRoutes.ROUTE_VAULT) {
                        GameVaultScreen(
                            uiState = uiState,
                            onBoostAndLaunch = viewModel::launchGameWithBoost,
                            onTogglePinApp = viewModel::togglePinAppToVault,
                            onTogglePerGame60Fps = viewModel::togglePerGame60Fps,
                            onOpenFpsLab = { navigateTopLevel(NavRoutes.ROUTE_FPS_LAB) }
                        )
                    }
                    composable(NavRoutes.ROUTE_FPS_LAB) {
                        FpsLabScreen(
                            uiState = uiState,
                            onToggle60Fps = viewModel::toggle60FpsLock,
                            onToggleMaxPerf = viewModel::toggleMaxPerformance,
                            onSelectLoad = viewModel::setBenchmarkLoad,
                            onResetCounters = viewModel::resetFrameMetricsCounters
                        )
                    }
                    composable(NavRoutes.ROUTE_TUNING) {
                        TuningScreen(
                            uiState = uiState,
                            onToggleDndShield = viewModel::toggleDndShield,
                            onToggleBoostNotification = viewModel::toggleBoostNotification,
                            onOpenDndSettings = viewModel::openDndSettings
                        )
                    }
                }
            }
        }
    }
}
