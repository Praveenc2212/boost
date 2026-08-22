package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val BoostDarkColorScheme = darkColorScheme(
    primary = NeonPurplePrimary,
    onPrimary = Color.White,
    primaryContainer = NeonPurpleDeep,
    onPrimaryContainer = NeonPurpleGlow,
    secondary = NeonCyan,
    onSecondary = SpaceBlack,
    secondaryContainer = SpaceNavyCard,
    onSecondaryContainer = NeonCyanLight,
    tertiary = CyberGold,
    onTertiary = SpaceBlack,
    tertiaryContainer = CyberGoldAmber,
    onTertiaryContainer = Color.White,
    background = SpaceBlack,
    onBackground = TextPrimary,
    surface = SpaceDeepNavy,
    onSurface = TextPrimary,
    surfaceVariant = SpaceNavySurface,
    onSurfaceVariant = TextSecondary,
    outline = SpaceGlassBorder,
    error = NeonCrimson,
    onError = Color.White
)

@Composable
fun BoostTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = BoostDarkColorScheme,
        typography = Typography,
        content = content
    )
}
