package com.example.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.data.repository.GlassThemeMode

/**
 * High-fidelity iOS Liquid Glass tokens for Dark and White theme modes.
 */
data class LiquidGlassTokens(
    val isDark: Boolean,
    val backgroundBase: Color,
    val ambientMesh1: Color,
    val ambientMesh2: Color,
    val ambientMesh3: Color,
    val glassCardBackground: Color,
    val glassCardElevated: Color,
    val glassCardBorder: Color,
    val glassCardBorderSubtle: Color,
    val glassSpecularTop: Color,
    val glassSpecularBottom: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val textGold: Color,
    val accentGold: Color,
    val accentGoldBright: Color,
    val accentGoldDark: Color,
    val accentGoldLight: Color,
    val navDockBackground: Color,
    val navDockBorder: Color,
    val navPillBackground: Color,
    val navPillContent: Color,
    val navUnselectedContent: Color,
    val chipBackground: Color,
    val chipSelectedBackground: Color,
    val searchBarBackground: Color,
    val searchBarBorder: Color,
    val cardShadowColor: Color
) {
    val goldGradient: Brush
        get() = Brush.horizontalGradient(listOf(accentGoldBright, accentGold, accentGoldDark))

    val specularBrush: Brush
        get() = Brush.verticalGradient(
            listOf(glassSpecularTop, Color.Transparent),
            startY = 0f,
            endY = 40f
        )

    val glassBorder: Color
        get() = glassCardBorder

    val glassCardInner: Color
        get() = glassCardElevated

    val buttonContent: Color
        get() = if (isDark) Color(0xFF07070B) else Color.White
}

// Dark Liquid Glass Palette (Obsidian + Frosted Smoked Glass + 24K Gold)
val DarkLiquidGlassTokens = LiquidGlassTokens(
    isDark = true,
    backgroundBase = Color(0xFF07070B),
    ambientMesh1 = Color(0xFF0F111E),
    ambientMesh2 = Color(0xFF1E170C),
    ambientMesh3 = Color(0xFF13101C),
    glassCardBackground = Color(0xCC11121C),
    glassCardElevated = Color(0xE0171826),
    glassCardBorder = Color(0x40D4AF37),
    glassCardBorderSubtle = Color(0x20FFFFFF),
    glassSpecularTop = Color(0x35FFFFFF),
    glassSpecularBottom = Color(0x06FFFFFF),
    textPrimary = Color(0xFFFFFFFF),
    textSecondary = Color(0xFFB4B4C8),
    textMuted = Color(0xFF71718A),
    textGold = Color(0xFFFFDF73),
    accentGold = Color(0xFFD4AF37),
    accentGoldBright = Color(0xFFFFDF73),
    accentGoldDark = Color(0xFFA67C1E),
    accentGoldLight = Color(0xFFFFF1C2),
    navDockBackground = Color(0xDD0D0E16),
    navDockBorder = Color(0x44D4AF37),
    navPillBackground = Color(0xFFD4AF37),
    navPillContent = Color(0xFF08080A),
    navUnselectedContent = Color(0xFF8E8EA6),
    chipBackground = Color(0xCC1A1A26),
    chipSelectedBackground = Color(0xFFD4AF37),
    searchBarBackground = Color(0xCC101018),
    searchBarBorder = Color(0x44D4AF37),
    cardShadowColor = Color(0x80000000)
)

// White Liquid Glass Palette (Crystal Frosted Milky Glass + Ice Reflection + Champagne Gold)
val WhiteLiquidGlassTokens = LiquidGlassTokens(
    isDark = false,
    backgroundBase = Color(0xFFF3F5FA),
    ambientMesh1 = Color(0xFFE6EEF8),
    ambientMesh2 = Color(0xFFFDF6EA),
    ambientMesh3 = Color(0xFFEBF1F7),
    glassCardBackground = Color(0xE6FFFFFF),
    glassCardElevated = Color(0xF8FFFFFF),
    glassCardBorder = Color(0x80D4AF37),
    glassCardBorderSubtle = Color(0x33000000),
    glassSpecularTop = Color(0xF0FFFFFF),
    glassSpecularBottom = Color(0x15000000),
    textPrimary = Color(0xFF0A0F1D), // Deep rich ink for maximum legibility outdoors
    textSecondary = Color(0xFF475569), // Slate 600
    textMuted = Color(0xFF8090A5),
    textGold = Color(0xFF9A6E04),
    accentGold = Color(0xFFB8860B),
    accentGoldBright = Color(0xFFD4AF37),
    accentGoldDark = Color(0xFF8F6704),
    accentGoldLight = Color(0xFFFAF0D7),
    navDockBackground = Color(0xF0FFFFFF),
    navDockBorder = Color(0x80D4AF37),
    navPillBackground = Color(0xFFD4AF37),
    navPillContent = Color(0xFF0A0F1D),
    navUnselectedContent = Color(0xFF64748B),
    chipBackground = Color(0xEEEDF2F7),
    chipSelectedBackground = Color(0xFFD4AF37),
    searchBarBackground = Color(0xF2FFFFFF),
    searchBarBorder = Color(0x66D4AF37),
    cardShadowColor = Color(0x18000000)
)

val LocalLiquidGlassTokens = compositionLocalOf { DarkLiquidGlassTokens }

object LiquidGlassTheme {
    val tokens: LiquidGlassTokens
        @Composable
        @ReadOnlyComposable
        get() = LocalLiquidGlassTokens.current
}

/**
 * Animated dynamic Liquid Glass Theme provider.
 * Supports Dark Liquid Glass and White Liquid Glass with smooth color interpolations.
 */
@Composable
fun ProvideLiquidGlassTheme(
    mode: GlassThemeMode = GlassThemeMode.DARK,
    content: @Composable () -> Unit
) {
    val isSystemDark = isSystemInDarkTheme()
    val isDark = when (mode) {
        GlassThemeMode.DARK -> true
        GlassThemeMode.WHITE -> false
        GlassThemeMode.SYSTEM -> isSystemDark
    }

    val targetTokens = if (isDark) DarkLiquidGlassTokens else WhiteLiquidGlassTokens

    // Smoothly animate core theme colors for seamless switching
    val animBg = animateColorAsState(targetTokens.backgroundBase, tween(350), label = "bg")
    val animGlassBg = animateColorAsState(targetTokens.glassCardBackground, tween(350), label = "glassBg")
    val animBorder = animateColorAsState(targetTokens.glassCardBorder, tween(350), label = "border")
    val animTextPrimary = animateColorAsState(targetTokens.textPrimary, tween(350), label = "textPrimary")
    val animTextSecondary = animateColorAsState(targetTokens.textSecondary, tween(350), label = "textSec")
    val animAccentGold = animateColorAsState(targetTokens.accentGold, tween(350), label = "gold")
    val animNavBg = animateColorAsState(targetTokens.navDockBackground, tween(350), label = "navBg")

    val animatedTokens = targetTokens.copy(
        backgroundBase = animBg.value,
        glassCardBackground = animGlassBg.value,
        glassCardBorder = animBorder.value,
        textPrimary = animTextPrimary.value,
        textSecondary = animTextSecondary.value,
        accentGold = animAccentGold.value,
        navDockBackground = animNavBg.value
    )

    // Build Material 3 ColorScheme matching active Liquid Glass theme
    val m3ColorScheme = if (isDark) {
        darkColorScheme(
            primary = animatedTokens.accentGold,
            onPrimary = CharcoalBlack,
            primaryContainer = animatedTokens.accentGoldDark,
            onPrimaryContainer = animatedTokens.accentGoldLight,
            secondary = animatedTokens.accentGoldBright,
            onSecondary = CharcoalBlack,
            secondaryContainer = animatedTokens.glassCardElevated,
            onSecondaryContainer = animatedTokens.accentGoldLight,
            tertiary = AccentFour,
            onTertiary = Color.White,
            background = animatedTokens.backgroundBase,
            onBackground = animatedTokens.textPrimary,
            surface = animatedTokens.glassCardBackground,
            onSurface = animatedTokens.textPrimary,
            surfaceVariant = animatedTokens.glassCardElevated,
            onSurfaceVariant = animatedTokens.textSecondary,
            outline = animatedTokens.glassCardBorder,
            error = AccentWicket,
            onError = Color.White
        )
    } else {
        lightColorScheme(
            primary = animatedTokens.accentGold,
            onPrimary = Color.White,
            primaryContainer = animatedTokens.accentGoldLight,
            onPrimaryContainer = animatedTokens.textPrimary,
            secondary = animatedTokens.accentGoldBright,
            onSecondary = animatedTokens.textPrimary,
            secondaryContainer = animatedTokens.glassCardElevated,
            onSecondaryContainer = animatedTokens.textPrimary,
            tertiary = AccentFour,
            onTertiary = Color.White,
            background = animatedTokens.backgroundBase,
            onBackground = animatedTokens.textPrimary,
            surface = animatedTokens.glassCardBackground,
            onSurface = animatedTokens.textPrimary,
            surfaceVariant = animatedTokens.glassCardElevated,
            onSurfaceVariant = animatedTokens.textSecondary,
            outline = animatedTokens.glassCardBorder,
            error = AccentWicket,
            onError = Color.White
        )
    }

    CompositionLocalProvider(LocalLiquidGlassTokens provides animatedTokens) {
        MaterialTheme(
            colorScheme = m3ColorScheme,
            typography = Typography,
            content = content
        )
    }
}
