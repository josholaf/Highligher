package com.josholaf.highligher.core.design

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.josholaf.highligher.core.datastore.ThemeMode
import com.josholaf.highligher.core.datastore.ThemePreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

val DeepForest = Color(0xFF17231D)
val HighlightGreen = Color(0xFF78B68A)
val Paper = Color(0xFFF3F1E9)
val WarmAccent = Color(0xFFC6A66A)
val OnPaper = Color(0xFF1A1A1A)
val Muted = Color(0xFF6B6B6B)

private val LightColors = lightColorScheme(
    primary = HighlightGreen,
    onPrimary = Color.White,
    secondary = WarmAccent,
    onSecondary = DeepForest,
    background = Paper,
    onBackground = OnPaper,
    surface = Color.White,
    onSurface = OnPaper,
    surfaceVariant = Color(0xFFE8E4D9),
    onSurfaceVariant = Muted
)

private val DarkColors = darkColorScheme(
    primary = HighlightGreen,
    onPrimary = DeepForest,
    secondary = WarmAccent,
    onSecondary = DeepForest,
    background = DeepForest,
    onBackground = Paper,
    surface = Color(0xFF1F2A24),
    onSurface = Paper,
    surfaceVariant = Color(0xFF2A3830),
    onSurfaceVariant = Color(0xFFA8B5AD)
)

@HiltViewModel
class ThemeViewModel @Inject constructor(
    themePreferences: ThemePreferences
) : ViewModel() {
    val themeMode: StateFlow<ThemeMode> = themePreferences.themeMode
        .stateIn(viewModelScope, SharingStarted.Eagerly, ThemeMode.SYSTEM)
}

@Composable
fun HighligherTheme(
    themeMode: ThemeMode? = null,
    content: @Composable () -> Unit
) {
    val resolvedMode = themeMode ?: run {
        val vm: ThemeViewModel = hiltViewModel()
        val mode by vm.themeMode.collectAsState()
        mode
    }
    val darkTheme = when (resolvedMode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content
    )
}
