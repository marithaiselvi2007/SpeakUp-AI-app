package com.example.data.model

import androidx.compose.ui.graphics.Color

enum class ThemeColorOption(val displayName: String, val primary: Color, val secondary: Color, val accent: Color) {
    BLUE("Blue", Color(0xFF2563EB), Color(0xFF3B82F6), Color(0xFF06B6D4)),
    PURPLE("Purple", Color(0xFF7C3AED), Color(0xFF8B5CF6), Color(0xFFEC4899)),
    GREEN("Green", Color(0xFF059669), Color(0xFF10B981), Color(0xFF84CC16)),
    ORANGE("Orange", Color(0xFFEA580C), Color(0xFFF97316), Color(0xFFFBBF24)),
    PINK("Pink", Color(0xFFDB2777), Color(0xFFEC4899), Color(0xFFA855F7)),
    TEAL("Teal", Color(0xFF0D9488), Color(0xFF14B8A6), Color(0xFF3B82F6))
}

enum class ThemeModeOption(val displayName: String) {
    LIGHT("Light Mode"),
    DARK("Dark Mode"),
    SYSTEM("System Default")
}

enum class EnglishLevel(val displayName: String, val description: String) {
    BEGINNER("Beginner", "Basic vocabulary, simple sentences, foundational grammar"),
    INTERMEDIATE("Intermediate", "Conversational English, routine topics, developing fluency"),
    ADVANCED("Advanced", "Complex ideas, nuanced vocabulary, professional fluency")
}
