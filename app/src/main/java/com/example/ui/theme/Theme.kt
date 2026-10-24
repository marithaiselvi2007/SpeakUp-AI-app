package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.data.model.ThemeColorOption
import com.example.data.model.ThemeModeOption

@Composable
fun SpeakUpTheme(
    themeColor: ThemeColorOption = ThemeColorOption.BLUE,
    themeMode: ThemeModeOption = ThemeModeOption.SYSTEM,
    content: @Composable () -> Unit
) {
    val isDark = when (themeMode) {
        ThemeModeOption.LIGHT -> false
        ThemeModeOption.DARK -> true
        ThemeModeOption.SYSTEM -> isSystemInDarkTheme()
    }

    val primary = getThemePrimary(themeColor, isDark)
    val secondary = getThemeSecondary(themeColor, isDark)

    val colorScheme = if (isDark) {
        darkColorScheme(
            primary = primary,
            onPrimary = Color.Black,
            primaryContainer = primary.copy(alpha = 0.25f),
            onPrimaryContainer = primary,
            secondary = secondary,
            onSecondary = Color.Black,
            secondaryContainer = secondary.copy(alpha = 0.2f),
            onSecondaryContainer = secondary,
            background = DarkNavyBackground,
            onBackground = Color(0xFFF1F5F9),
            surface = DarkNavySurface,
            onSurface = Color(0xFFF8FAFC),
            surfaceVariant = DarkNavySurfaceVariant,
            onSurfaceVariant = Color(0xFFCBD5E1),
            outline = Color(0xFF334155),
            outlineVariant = Color(0xFF1E293B),
            error = ErrorRed,
            onError = Color.White
        )
    } else {
        lightColorScheme(
            primary = primary,
            onPrimary = Color.White,
            primaryContainer = primary.copy(alpha = 0.12f),
            onPrimaryContainer = primary,
            secondary = secondary,
            onSecondary = Color.White,
            secondaryContainer = secondary.copy(alpha = 0.1f),
            onSecondaryContainer = secondary,
            background = LightBackground,
            onBackground = Color(0xFF0F172A),
            surface = LightSurface,
            onSurface = Color(0xFF0F172A),
            surfaceVariant = LightSurfaceVariant,
            onSurfaceVariant = Color(0xFF475569),
            outline = Color(0xFFCBD5E1),
            outlineVariant = Color(0xFFE2E8F0),
            error = ErrorRed,
            onError = Color.White
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
