package com.example.ui.theme

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
 * Solid Midnight Navy & Bright Cyan Sports Design System tokens.
 * Replaces translucent liquid glass with clean, solid modern UI panels, crisp borders, and 3D depth.
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
        get() = Brush.horizontalGradient(listOf(accentGoldLight, accentGold, accentGoldDark))

    val specularBrush: Brush
        get() = Brush.verticalGradient(
            listOf(glassSpecularTop, Color.Transparent),
            startY = 0f,
            endY = 24f
        )

    val glassBorder: Color
        get() = glassCardBorder

    val glassCardInner: Color
        get() = glassCardElevated

    val buttonContent: Color
        get() = if (isDark) Color(0xFF041022) else Color.White
}

// Solid Midnight Navy & Bright Cyan Sports Palette (matches reference image)
val DarkLiquidGlassTokens = LiquidGlassTokens(
    isDark = true,
    backgroundBase = Color(0xFF050C1A),
    ambientMesh1 = Color(0xFF0A1D3B),
    ambientMesh2 = Color(0xFF082B49),
    ambientMesh3 = Color(0xFF07152B),
    glassCardBackground = Color(0xFF09172C),
    glassCardElevated = Color(0xFF0D213F),
    glassCardBorder = Color(0xFF173F70),
    glassCardBorderSubtle = Color(0xFF112E54),
    glassSpecularTop = Color(0x143CE2FF),
    glassSpecularBottom = Color(0x00000000),
    textPrimary = Color(0xFFF6FAFF),
    textSecondary = Color(0xFF9AB8D9),
    textMuted = Color(0xFF5E80A6),
    textGold = Color(0xFF3CE2FF),
    accentGold = Color(0xFF29E2FF),
    accentGoldBright = Color(0xFF4AE8FF),
    accentGoldDark = Color(0xFF00B8D4),
    accentGoldLight = Color(0xFFA4F0FF),
    navDockBackground = Color(0xFF08172E),
    navDockBorder = Color(0xFF173F70),
    navPillBackground = Color(0xFF3CE2FF),
    navPillContent = Color(0xFF041022),
    navUnselectedContent = Color(0xFF8AAAD0),
    chipBackground = Color(0xFF0B1C36),
    chipSelectedBackground = Color(0xFF3CE2FF),
    searchBarBackground = Color(0xFF09172C),
    searchBarBorder = Color(0xFF173F70),
    cardShadowColor = Color(0xCC02060E)
)

// Clean Ice-Blue & Deep Navy Daylight Sports Palette
val WhiteLiquidGlassTokens = LiquidGlassTokens(
    isDark = false,
    backgroundBase = Color(0xFFF0F6FC),
    ambientMesh1 = Color(0xFFDCECFB),
    ambientMesh2 = Color(0xFFD0F0F8),
    ambientMesh3 = Color(0xFFE4F0FA),
    glassCardBackground = Color(0xFFFFFFFF),
    glassCardElevated = Color(0xFFF5FAFF),
    glassCardBorder = Color(0xFFB8D8F0),
    glassCardBorderSubtle = Color(0xFFD6E8F8),
    glassSpecularTop = Color(0x00FFFFFF),
    glassSpecularBottom = Color(0x00FFFFFF),
    textPrimary = Color(0xFF051329),
    textSecondary = Color(0xFF355478),
    textMuted = Color(0xFF6987A8),
    textGold = Color(0xFF0088A9),
    accentGold = Color(0xFF0091EA),
    accentGoldBright = Color(0xFF00B8D4),
    accentGoldDark = Color(0xFF006064),
    accentGoldLight = Color(0xFFE0F7FA),
    navDockBackground = Color(0xFFFFFFFF),
    navDockBorder = Color(0xFFB8D8F0),
    navPillBackground = Color(0xFF00B8D4),
    navPillContent = Color(0xFF041022),
    navUnselectedContent = Color(0xFF476788),
    chipBackground = Color(0xFFE6F2FC),
    chipSelectedBackground = Color(0xFF00B8D4),
    searchBarBackground = Color(0xFFFFFFFF),
    searchBarBorder = Color(0xFFB8D8F0),
    cardShadowColor = Color(0x22031A38)
)

val LocalLiquidGlassTokens = compositionLocalOf { DarkLiquidGlassTokens }

object LiquidGlassTheme {
    val tokens: LiquidGlassTokens
        @Composable
        @ReadOnlyComposable
        get() = LocalLiquidGlassTokens.current
}

/**
 * Solid Midnight Navy & Bright Cyan Theme Provider.
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

    val activeTokens = if (isDark) DarkLiquidGlassTokens else WhiteLiquidGlassTokens

    val m3ColorScheme = if (isDark) {
        darkColorScheme(
            primary = activeTokens.accentGold,
            onPrimary = CharcoalBlack,
            primaryContainer = activeTokens.accentGoldDark,
            onPrimaryContainer = activeTokens.accentGoldLight,
            secondary = activeTokens.accentGoldBright,
            onSecondary = CharcoalBlack,
            secondaryContainer = activeTokens.glassCardElevated,
            onSecondaryContainer = activeTokens.accentGoldLight,
            tertiary = AccentFour,
            onTertiary = Color.White,
            background = activeTokens.backgroundBase,
            onBackground = activeTokens.textPrimary,
            surface = activeTokens.glassCardBackground,
            onSurface = activeTokens.textPrimary,
            surfaceVariant = activeTokens.glassCardElevated,
            onSurfaceVariant = activeTokens.textSecondary,
            error = AccentWicket,
            onError = Color.White,
            outline = activeTokens.glassCardBorder
        )
    } else {
        lightColorScheme(
            primary = activeTokens.accentGold,
            onPrimary = Color.White,
            primaryContainer = activeTokens.accentGoldLight,
            onPrimaryContainer = activeTokens.accentGoldDark,
            secondary = activeTokens.accentGoldBright,
            onSecondary = Color.White,
            secondaryContainer = activeTokens.chipBackground,
            onSecondaryContainer = activeTokens.textPrimary,
            tertiary = AccentFour,
            onTertiary = Color.White,
            background = activeTokens.backgroundBase,
            onBackground = activeTokens.textPrimary,
            surface = activeTokens.glassCardBackground,
            onSurface = activeTokens.textPrimary,
            surfaceVariant = activeTokens.glassCardElevated,
            onSurfaceVariant = activeTokens.textSecondary,
            error = AccentWicket,
            onError = Color.White,
            outline = activeTokens.glassCardBorder
        )
    }

    CompositionLocalProvider(LocalLiquidGlassTokens provides activeTokens) {
        MaterialTheme(
            colorScheme = m3ColorScheme,
            typography = Typography,
            content = content
        )
    }
}
