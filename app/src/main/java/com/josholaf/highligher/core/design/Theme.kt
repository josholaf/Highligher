package com.josholaf.highligher.core.design

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.josholaf.highligher.R
import com.josholaf.highligher.core.datastore.ThemeMode
import com.josholaf.highligher.core.datastore.ThemePreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

val DeepForest = Color(0xFF0F1A15)
val HighlightGreen = Color(0xFF5FA876)
val Paper = Color(0xFFF7F4EC)
val WarmAccent = Color(0xFFC4A574)
val OnPaper = Color(0xFF1C1B19)
val Muted = Color(0xFF6B6B66)

private val Manrope = FontFamily(Font(R.font.manrope_family))

private val AppTypography = Typography(
    displayLarge = TextStyle(fontFamily = Manrope, fontWeight = FontWeight.Bold, fontSize = 57.sp, lineHeight = 64.sp, letterSpacing = (-0.25).sp),
    displayMedium = TextStyle(fontFamily = Manrope, fontWeight = FontWeight.Bold, fontSize = 45.sp, lineHeight = 52.sp, letterSpacing = 0.sp),
    displaySmall = TextStyle(fontFamily = Manrope, fontWeight = FontWeight.SemiBold, fontSize = 36.sp, lineHeight = 44.sp, letterSpacing = 0.sp),
    headlineLarge = TextStyle(fontFamily = Manrope, fontWeight = FontWeight.SemiBold, fontSize = 32.sp, lineHeight = 40.sp, letterSpacing = 0.sp),
    headlineMedium = TextStyle(fontFamily = Manrope, fontWeight = FontWeight.SemiBold, fontSize = 28.sp, lineHeight = 36.sp, letterSpacing = 0.sp),
    headlineSmall = TextStyle(fontFamily = Manrope, fontWeight = FontWeight.SemiBold, fontSize = 24.sp, lineHeight = 32.sp, letterSpacing = 0.sp),
    titleLarge = TextStyle(fontFamily = Manrope, fontWeight = FontWeight.SemiBold, fontSize = 22.sp, lineHeight = 28.sp, letterSpacing = 0.sp),
    titleMedium = TextStyle(fontFamily = Manrope, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.15.sp),
    titleSmall = TextStyle(fontFamily = Manrope, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp),
    bodyLarge = TextStyle(fontFamily = Manrope, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.15.sp),
    bodyMedium = TextStyle(fontFamily = Manrope, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.25.sp),
    bodySmall = TextStyle(fontFamily = Manrope, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.4.sp),
    labelLarge = TextStyle(fontFamily = Manrope, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp),
    labelMedium = TextStyle(fontFamily = Manrope, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.5.sp),
    labelSmall = TextStyle(fontFamily = Manrope, fontWeight = FontWeight.Medium, fontSize = 11.sp, lineHeight = 16.sp, letterSpacing = 0.5.sp)
)

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

private val LightColors = lightColorScheme(
    primary = Color(0xFF2F6A45), onPrimary = Color.White,
    primaryContainer = Color(0xFFB8E0C4), onPrimaryContainer = Color(0xFF0C2918),
    secondary = Color(0xFF8B6B3A), onSecondary = Color.White,
    secondaryContainer = Color(0xFFF5E4C8), onSecondaryContainer = Color(0xFF2B1E0A),
    tertiary = Color(0xFF4A6670), onTertiary = Color.White,
    tertiaryContainer = Color(0xFFD0E4EB), onTertiaryContainer = Color(0xFF0E2429),
    error = Color(0xFFBA1A1A), onError = Color.White,
    errorContainer = Color(0xFFFFDAD6), onErrorContainer = Color(0xFF410002),
    background = Paper, onBackground = OnPaper, surface = Paper, onSurface = OnPaper,
    surfaceVariant = Color(0xFFE8E4D8), onSurfaceVariant = Color(0xFF4A4740),
    surfaceContainerLowest = Color(0xFFFFFFFF), surfaceContainerLow = Color(0xFFF1EEE4),
    surfaceContainer = Color(0xFFEBE7DC), surfaceContainerHigh = Color(0xFFE5E1D6),
    surfaceContainerHighest = Color(0xFFDFDBD0),
    outline = Color(0xFF7A766C), outlineVariant = Color(0xFFCBC6BA),
    inverseSurface = Color(0xFF31302C), inverseOnSurface = Color(0xFFF4F0E7),
    inversePrimary = HighlightGreen, scrim = Color(0xFF000000)
)

private val DarkColors = darkColorScheme(
    primary = HighlightGreen, onPrimary = Color(0xFF00391F),
    primaryContainer = Color(0xFF1B4D32), onPrimaryContainer = Color(0xFFB8E0C4),
    secondary = Color(0xFFE0C292), onSecondary = Color(0xFF402D0A),
    secondaryContainer = Color(0xFF5A4320), onSecondaryContainer = Color(0xFFF5E4C8),
    tertiary = Color(0xFFB4CAD2), onTertiary = Color(0xFF1E353C),
    tertiaryContainer = Color(0xFF354C54), onTertiaryContainer = Color(0xFFD0E4EB),
    error = Color(0xFFFFB4AB), onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A), onErrorContainer = Color(0xFFFFDAD6),
    background = DeepForest, onBackground = Color(0xFFE4E2DC),
    surface = Color(0xFF131B17), onSurface = Color(0xFFE4E2DC),
    surfaceVariant = Color(0xFF3F4942), onSurfaceVariant = Color(0xFFBFC9C0),
    surfaceContainerLowest = Color(0xFF0B110E), surfaceContainerLow = Color(0xFF1A221D),
    surfaceContainer = Color(0xFF1E2722), surfaceContainerHigh = Color(0xFF28312C),
    surfaceContainerHighest = Color(0xFF333C37),
    outline = Color(0xFF899990), outlineVariant = Color(0xFF3F4942),
    inverseSurface = Color(0xFFE4E2DC), inverseOnSurface = Color(0xFF2E312E),
    inversePrimary = Color(0xFF2F6A45), scrim = Color(0xFF000000)
)

@HiltViewModel
class ThemeViewModel @Inject constructor(themePreferences: ThemePreferences) : ViewModel() {
    val themeMode: StateFlow<ThemeMode> = themePreferences.themeMode
        .stateIn(viewModelScope, SharingStarted.Eagerly, ThemeMode.SYSTEM)
}

@Composable
fun HighligherTheme(themeMode: ThemeMode? = null, content: @Composable () -> Unit) {
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
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
