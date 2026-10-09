package com.example.akijeet.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AppColorScheme = lightColorScheme(
    primary = BrownDark,
    onPrimary = Color.White,
    secondary = OrangePrimary,
    onSecondary = Color.White,
    background = OrangeLight,
    onBackground = BrownDark,
    surface = Color.White,
    onSurface = BrownDark
)

@Composable
fun AkijeetTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AppColorScheme,
        typography = Typography,
        content = content
    )
}
