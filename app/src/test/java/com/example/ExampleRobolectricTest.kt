package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.HardwareTelemetryEngine
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read app name from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("ApexBoost", appName)
  }

  @Test
  fun `telemetry engine collects cpu and ram snapshot`() = runTest {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val engine = HardwareTelemetryEngine(context)
    val telemetry = engine.collectTelemetry()
    assertTrue(telemetry.cpuCoresCount >= 1)
    assertTrue(telemetry.readinessScore in 20..100)
  }
}
