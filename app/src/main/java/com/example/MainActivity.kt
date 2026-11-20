package com.example

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.ApexBoostApp
import com.example.ui.theme.ApexBoostTheme
import com.example.ui.viewmodel.BoosterViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: BoosterViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        )

        setContent {
            val prefs by viewModel.preferencesFlow.collectAsStateWithLifecycle()

            LaunchedEffect(
                prefs.is60FpsLocked,
                prefs.isMaxPerformanceLocked,
                prefs.isCpuPriorityEnabled
            ) {
                viewModel.syncWindowLocks(this@MainActivity)
            }

            ApexBoostTheme {
                ApexBoostApp(viewModel = viewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.startFrameMonitoring()
        viewModel.syncWindowLocks(this)
        viewModel.refreshTelemetryOnce()
    }

    override fun onPause() {
        viewModel.stopFrameMonitoring()
        super.onPause()
    }
}
