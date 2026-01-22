package com.koflox.designsystem.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * CompositionLocal to provide the current dark theme state throughout the app.
 * This reflects the app's theme preference (not just the system theme).
 */
val LocalDarkTheme = compositionLocalOf { false }

val CyclingLightColorScheme = lightColorScheme(
    primary = CyclingGreen,
    onPrimary = Color.White,
    primaryContainer = CyclingGreenLight,
    onPrimaryContainer = Color(0xFF002106),
    secondary = CyclingBlue,
    onSecondary = Color.White,
    secondaryContainer = CyclingBlueLight,
    onSecondaryContainer = Color(0xFF001A41),
    tertiary = CyclingOrange,
    onTertiary = Color.White,
    tertiaryContainer = CyclingOrangeLight,
    onTertiaryContainer = Color(0xFF2D1600),
    error = ErrorLight,
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = BackgroundLight,
    onBackground = OnSurfaceLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceContainerLight,
    onSurfaceVariant = Color(0xFF49454F),
    surfaceContainerHigh = Color(0xFFECE6F0), // Elevated surface for cards
    outline = OutlineLight,
    outlineVariant = Color(0xFFCAC4D0),
    scrim = Color.Black,
    inverseSurface = Color(0xFF313033),
    inverseOnSurface = Color(0xFFF4EFF4),
    inversePrimary = CyclingGreenDark,
)

val CyclingDarkColorScheme = darkColorScheme(
    primary = CyclingGreenDark,
    onPrimary = Color(0xFF003910),
    primaryContainer = CyclingGreen,
    onPrimaryContainer = Color(0xFFA0F5A2),
    secondary = CyclingBlueDark,
    onSecondary = Color(0xFF002D6A),
    secondaryContainer = CyclingBlue,
    onSecondaryContainer = Color(0xFFD6E3FF),
    tertiary = CyclingOrangeDark,
    onTertiary = Color(0xFF4A2800),
    tertiaryContainer = CyclingOrange,
    onTertiaryContainer = Color(0xFFFFDCC7),
    error = ErrorDark,
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = ErrorDark,
    background = BackgroundDark,
    onBackground = OnSurfaceDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceContainerDark,
    onSurfaceVariant = Color(0xFFB0B0B0), // Neutral gray
    surfaceContainerHigh = SurfaceContainerHighDark,
    outline = OutlineDark,
    outlineVariant = Color(0xFF444444), // Neutral dark gray
    scrim = Color.Black,
    inverseSurface = Color(0xFFE0E0E0),
    inverseOnSurface = Color(0xFF303030),
    inversePrimary = CyclingGreen,
)
