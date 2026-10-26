package com.example.ui.theme

import androidx.compose.runtime.Composable
import com.example.data.repository.GlassThemeMode

@Composable
fun MyApplicationTheme(
    themeMode: GlassThemeMode = GlassThemeMode.DARK,
    content: @Composable () -> Unit
) {
    ProvideLiquidGlassTheme(
        mode = themeMode,
        content = content
    )
}
