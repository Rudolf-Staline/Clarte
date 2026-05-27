package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = ClartePrimary,
    secondary = ClarteSecondary,
    tertiary = ClarteTertiary,
    background = ClarteBackground,
    surface = ClarteSurface,
    onPrimary = Color(0xFF131316),
    onSecondary = Color(0xFF111827),
    onTertiary = Color(0xFF111827),
    onBackground = ClarteTextPrimary,
    onSurface = ClarteTextPrimary,
    surfaceVariant = ClarteSurfaceSelected,
    onSurfaceVariant = ClarteTextPrimary,
    outline = ClarteBorder
)

private val LightColorScheme = lightColorScheme(
    primary = ClarteLightPrimary,
    secondary = ClarteLightSecondary,
    tertiary = ClarteLightSecondary,
    background = ClarteLightBackground,
    surface = ClarteLightSurface,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = ClarteLightTextPrimary,
    onSurface = ClarteLightTextPrimary,
    surfaceVariant = Color(0xFFE5E7EB),
    onSurfaceVariant = ClarteLightTextPrimary,
    outline = Color(0xFFE5E7EB)
)

@Composable
fun MyApplicationTheme(
    selectedTheme: String = "système",
    content: @Composable () -> Unit
) {
    val darkTheme = when (selectedTheme.lowercase().trim()) {
        "sombre" -> true
        "clair" -> false
        else -> isSystemInDarkTheme() // "système"
    }

    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
