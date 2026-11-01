package com.spendsight.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = ElectricTeal,
    onPrimary = DarkBackground,
    primaryContainer = ElectricTealDark,
    onPrimaryContainer = DarkTextPrimary,
    secondary = CyberViolet,
    onSecondary = DarkTextPrimary,
    secondaryContainer = CyberVioletDark,
    onSecondaryContainer = DarkTextPrimary,
    tertiary = NeonGreen,
    onTertiary = DarkBackground,
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkBorder,
    outlineVariant = DarkBorderHighlight,
    error = NeonCoral,
    onError = DarkTextPrimary
)

private val LightColorScheme = lightColorScheme(
    primary = ElectricTealDark,
    onPrimary = LightSurface,
    primaryContainer = ElectricTealGlow,
    onPrimaryContainer = LightTextPrimary,
    secondary = CyberViolet,
    onSecondary = LightSurface,
    secondaryContainer = CyberVioletGlow,
    onSecondaryContainer = LightTextPrimary,
    tertiary = NeonGreen,
    onTertiary = LightSurface,
    background = LightBackground,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceElevated,
    onSurfaceVariant = LightTextSecondary,
    outline = LightBorder,
    outlineVariant = LightBorderHighlight,
    error = NeonCoral,
    onError = LightSurface
)

@Composable
fun SpendSightTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.background.toArgb()
                window.navigationBarColor = colorScheme.background.toArgb()
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = !darkTheme
                insetsController.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    ProvideReduceMotion {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = SpendSightTypography,
            shapes = SpendSightShapes,
            content = content
        )
    }
}

