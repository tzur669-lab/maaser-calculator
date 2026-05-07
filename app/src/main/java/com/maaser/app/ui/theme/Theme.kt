package com.maaser.app.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable

private val AppColorScheme = lightColorScheme(
    primary          = Primary,
    onPrimary        = OnBackground,
    primaryContainer = PrimaryLight,
    background       = Background,
    surface          = Surface,
    onBackground     = OnBackground,
    onSurface        = TextPrimary,
)

@Composable
fun MaaserTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = AppColorScheme, typography = AppTypography, content = content)
}
