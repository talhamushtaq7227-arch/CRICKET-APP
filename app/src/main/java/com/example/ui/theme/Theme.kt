package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val GlassEditionDarkColorScheme = darkColorScheme(
    primary = GoldPrimary,
    onPrimary = CharcoalBlack,
    primaryContainer = GoldDark,
    onPrimaryContainer = GoldLight,
    secondary = GoldBright,
    onSecondary = CharcoalBlack,
    secondaryContainer = CharcoalElevated,
    onSecondaryContainer = GoldLight,
    tertiary = AccentFour,
    onTertiary = Color.White,
    background = CharcoalBlack,
    onBackground = TextPrimary,
    surface = CharcoalDark,
    onSurface = TextPrimary,
    surfaceVariant = CharcoalCard,
    onSurfaceVariant = TextSecondary,
    outline = GlassBorder,
    outlineVariant = CharcoalBorder,
    error = AccentWicket,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep consistent luxury gold & obsidian brand identity
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = GlassEditionDarkColorScheme,
        typography = Typography,
        content = content
    )
}
