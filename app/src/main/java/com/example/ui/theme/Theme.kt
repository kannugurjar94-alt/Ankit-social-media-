package com.example.ui.theme

import android.app.Activity
import android.os.Build
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
    primary = NeonViolet,
    onPrimary = TextPrimary,
    secondary = ElectricCyan,
    onSecondary = CosmicDark,
    tertiary = NeonPink,
    background = CosmicDark,
    onBackground = TextPrimary,
    surface = CosmicDarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = CosmicCardBg,
    onSurfaceVariant = TextSecondary,
    outline = CosmicGlassBorder
)

private val LightColorScheme = lightColorScheme(
    primary = RoyalBlue,
    onPrimary = TextPrimary,
    secondary = NeonViolet,
    onSecondary = TextPrimary,
    tertiary = NeonPink,
    background = CosmicDark, // AnkitVerse is dedicated dark aesthetic
    onBackground = TextPrimary,
    surface = CosmicDarkSurface,
    onSurface = TextPrimary
)

@Composable
fun AnkitVerseTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = CosmicDark.toArgb()
            window.navigationBarColor = CosmicDark.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    AnkitVerseTheme(darkTheme = darkTheme, content = content)
}
