package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Deep Obsidian / Charcoal Backgrounds
val CharcoalBlack = Color(0xFF08080A)
val CharcoalDark = Color(0xFF101014)
val CharcoalCard = Color(0xFF16161D)
val CharcoalElevated = Color(0xFF1E1E28)
val CharcoalBorder = Color(0xFF2A2A38)

// Metallic Gold Accents
val GoldPrimary = Color(0xFFD4AF37)
val GoldBright = Color(0xFFFFDF73)
val GoldDark = Color(0xFFA67C1E)
val GoldLight = Color(0xFFFFF1C2)
val GoldMuted = Color(0xFF8A7338)

// Accent Colors
val AccentWicket = Color(0xFFE53935)
val AccentFour = Color(0xFF10B981)
val AccentSix = Color(0xFF8B5CF6)
val AccentExtra = Color(0xFFF59E0B)

// Frosted Glass Tokens
val GlassBackground = Color(0xCC13131A)
val GlassSurface = Color(0x99181822)
val GlassBorder = Color(0x40D4AF37)
val GlassBorderSubtle = Color(0x20FFFFFF)

// Text
val TextPrimary = Color(0xFFFFFFFF)
val TextSecondary = Color(0xFFB0B0C0)
val TextMuted = Color(0xFF6C6C7D)
val TextGold = Color(0xFFFFE082)

// Gradients
val GoldGradient = Brush.linearGradient(
    colors = listOf(GoldBright, GoldPrimary, GoldDark)
)

val DarkGlassGradient = Brush.verticalGradient(
    colors = listOf(Color(0xE61A1A24), Color(0xFA0E0E14))
)

val ScorecardGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xF00A0A0E), Color(0xDE14141C), Color(0xF00A0A0E))
)
