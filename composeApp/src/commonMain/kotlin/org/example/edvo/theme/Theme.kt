package org.example.edvo.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = EdvoColor.Primary,
    onPrimary = EdvoColor.OnPrimary,
    background = EdvoColor.Background,
    onBackground = EdvoColor.OnBackground,
    surface = EdvoColor.Surface,
    onSurface = EdvoColor.OnSurface,
    error = EdvoColor.ErrorRed,
    onError = Color.White,
    // Secondary elements
    secondary = EdvoColor.LightGray,
    onSecondary = Color.Black,
    surfaceContainer = EdvoColor.DarkSurface,
    outline = EdvoColor.DarkBorder
)

@Composable
fun EdvoTheme(
    content: @Composable () -> Unit
) {
    // Force Dark Mode always
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
