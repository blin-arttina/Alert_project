package com.assetsalert.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val AccentGreen = Color(0xFF00E28A)
val AccentRed = Color(0xFFFF5C5C)
val DarkBg = Color(0xFF121212)
val DarkSurface = Color(0xFF1E1E1E)
val GrayText = Color(0xFFB0B0B0)

private val AssetsAlertDarkScheme = darkColorScheme(
    primary = AccentGreen,
    secondary = GrayText,
    background = DarkBg,
    surface = DarkSurface,
    onPrimary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White,
    error = AccentRed
)

private val AssetsAlertLightScheme = lightColorScheme(
    primary = Color(0xFF00A868),
    secondary = Color(0xFF6B6B6B),
    error = Color(0xFFB3261E)
)

@Composable
fun AssetsAlertTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colors = if (darkTheme) AssetsAlertDarkScheme else AssetsAlertLightScheme
    MaterialTheme(colorScheme = colors, content = content)
}
