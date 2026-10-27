package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Deep Midnight Navy / Stadium Night Backgrounds
val CharcoalBlack = Color(0xFF040A18)
val CharcoalDark = Color(0xFF071226)
val CharcoalCard = Color(0xFF0B1B36)
val CharcoalElevated = Color(0xFF102447)
val CharcoalBorder = Color(0xFF1C3B6E)

// Glowing Cyan & Aqua Highlights (Mapped to existing token names for full compatibility)
val GoldPrimary = Color(0xFF00E5FF)
val GoldBright = Color(0xFF40C4FF)
val GoldDark = Color(0xFF00B8D4)
val GoldLight = Color(0xFF84FFFF)
val GoldMuted = Color(0xFF00838F)

// Explicit Cyan / Navy tokens
val CyanPrimary = Color(0xFF00E5FF)
val CyanBright = Color(0xFF84FFFF)
val CyanGlow = Color(0xFF00B8D4)
val NavyDeep = Color(0xFF040A18)
val NavySurface = Color(0xFF0A1931)
val NavyElevated = Color(0xFF11264A)

// Cricket Event Accent Colors
val AccentWicket = Color(0xFFFF3B5C)
val AccentFour = Color(0xFF00E676)
val AccentSix = Color(0xFF00E5FF)
val AccentExtra = Color(0xFFFFB300)

// Frosted Liquid Glass Tokens
val GlassBackground = Color(0xD90A1931)
val GlassSurface = Color(0xB30E2242)
val GlassBorder = Color(0x5500E5FF)
val GlassBorderSubtle = Color(0x2884FFFF)

// Text Hierarchy
val TextPrimary = Color(0xFFF4FAFF)
val TextSecondary = Color(0xFF9AB8D9)
val TextMuted = Color(0xFF5D7FA3)
val TextGold = Color(0xFF00E5FF)

// Gradients
val GoldGradient = Brush.linearGradient(
    colors = listOf(GoldLight, GoldPrimary, GoldDark)
)

val DarkGlassGradient = Brush.verticalGradient(
    colors = listOf(Color(0xEB0E2244), Color(0xFA050D1E))
)

val ScorecardGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xF2050E21), Color(0xE00C2147), Color(0xF2050E21))
)
