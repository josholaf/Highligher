package com.josholaf.highligher.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val DeepForest = Color(0xFF17231D)
val HighlightGreen = Color(0xFF78B68A)
val Paper = Color(0xFFF3F1E9)
val WarmAccent = Color(0xFFC6A66A)

private val DarkColors = darkColorScheme(
    primary = HighlightGreen,
    secondary = WarmAccent,
    background = DeepForest,
    surface = DeepForest,
    onPrimary = DeepForest,
    onBackground = Paper,
    onSurface = Paper
)

private val LightColors = lightColorScheme(
    primary = HighlightGreen,
    secondary = WarmAccent,
    background = Paper,
    surface = Paper,
    onPrimary = DeepForest,
    onBackground = DeepForest,
    onSurface = DeepForest
)

@Composable
fun HighligherTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content
    )
}
