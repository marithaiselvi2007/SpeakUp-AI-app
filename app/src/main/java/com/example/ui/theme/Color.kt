package com.example.ui.theme

import androidx.compose.ui.graphics.Color
import com.example.data.model.ThemeColorOption

// Base neutrals
val DarkNavyBackground = Color(0xFF0B0F19)
val DarkNavySurface = Color(0xFF131B2E)
val DarkNavySurfaceVariant = Color(0xFF1E293B)

val LightBackground = Color(0xFFF8FAFC)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFF1F5F9)

// Metric Colors
val GrammarGreen = Color(0xFF10B981)
val VocabPurple = Color(0xFF8B5CF6)
val FluencyBlue = Color(0xFF3B82F6)
val PronunciationOrange = Color(0xFFF97316)
val ConfidencePink = Color(0xFFEC4899)
val OverallGold = Color(0xFFF59E0B)

val ErrorRed = Color(0xFFEF4444)
val SuccessGreen = Color(0xFF22C55E)
val TanglishCyan = Color(0xFF06B6D4)

// Color scheme palettes for the 6 themes
fun getThemePrimary(themeOption: ThemeColorOption, dark: Boolean): Color {
    return when (themeOption) {
        ThemeColorOption.BLUE -> if (dark) Color(0xFF60A5FA) else Color(0xFF2563EB)
        ThemeColorOption.PURPLE -> if (dark) Color(0xFFA78BFA) else Color(0xFF7C3AED)
        ThemeColorOption.GREEN -> if (dark) Color(0xFF34D399) else Color(0xFF059669)
        ThemeColorOption.ORANGE -> if (dark) Color(0xFFFB923C) else Color(0xFFEA580C)
        ThemeColorOption.PINK -> if (dark) Color(0xFFF472B6) else Color(0xFFDB2777)
        ThemeColorOption.TEAL -> if (dark) Color(0xFF2DD4BF) else Color(0xFF0D9488)
    }
}

fun getThemeSecondary(themeOption: ThemeColorOption, dark: Boolean): Color {
    return when (themeOption) {
        ThemeColorOption.BLUE -> if (dark) Color(0xFF93C5FD) else Color(0xFF1D4ED8)
        ThemeColorOption.PURPLE -> if (dark) Color(0xFFC4B5FD) else Color(0xFF6D28D9)
        ThemeColorOption.GREEN -> if (dark) Color(0xFF6EE7B7) else Color(0xFF047857)
        ThemeColorOption.ORANGE -> if (dark) Color(0xFFFDBA74) else Color(0xFFC2410C)
        ThemeColorOption.PINK -> if (dark) Color(0xFFF9A8D4) else Color(0xFFBE185D)
        ThemeColorOption.TEAL -> if (dark) Color(0xFF5EEAD4) else Color(0xFF0F766E)
    }
}
