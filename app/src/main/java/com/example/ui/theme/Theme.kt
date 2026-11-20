package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val ApexDarkColorScheme = darkColorScheme(
    primary = ElectricCyan,
    onPrimary = OnCyan,
    primaryContainer = CyanContainer,
    onPrimaryContainer = ElectricCyan,
    secondary = EmeraldVolt,
    onSecondary = OnEmerald,
    secondaryContainer = EmeraldContainer,
    onSecondaryContainer = EmeraldVolt,
    tertiary = AmberPulse,
    onTertiary = ObsidianBg,
    tertiaryContainer = AmberContainer,
    onTertiaryContainer = AmberPulse,
    background = ObsidianBg,
    onBackground = TextPrimary,
    surface = CarbonSurface,
    onSurface = TextPrimary,
    surfaceVariant = CarbonSurfaceElevated,
    onSurfaceVariant = TextSecondary,
    surfaceContainerLowest = ObsidianBg,
    surfaceContainerLow = CarbonSurface,
    surfaceContainer = CarbonSurfaceElevated,
    surfaceContainerHigh = CarbonSurfaceBright,
    surfaceContainerHighest = CarbonSurfaceBright,
    outline = BorderSlate,
    outlineVariant = BorderSlate.copy(alpha = 0.6f),
    error = CrimsonAlert,
    onError = ObsidianBg,
    errorContainer = CrimsonContainer,
    onErrorContainer = CrimsonAlert
)

val ApexShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun ApexBoostTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ApexDarkColorScheme,
        typography = ApexTypography,
        shapes = ApexShapes,
        content = content
    )
}
