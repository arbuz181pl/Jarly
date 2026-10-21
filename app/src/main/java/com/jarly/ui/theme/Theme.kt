package com.jarly.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

enum class ThemeMode { SYSTEM, LIGHT, DARK }

private val LightScheme = lightColorScheme(
    primary = Color(0xFF8A5A00),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDF9B),
    onPrimaryContainer = Color(0xFF2A1D00),
    secondary = Color(0xFF6F5B40),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF9E0BC),
    onSecondaryContainer = Color(0xFF271904),
    tertiary = Color(0xFF934A21),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFDAC8),
    onTertiaryContainer = Color(0xFF341000),
    background = Color(0xFFFFF8F0),
    onBackground = Color(0xFF201C13),
    surface = Color(0xFFFFF8F0),
    onSurface = Color(0xFF201C13),
    surfaceVariant = Color(0xFFEDE3D1),
    onSurfaceVariant = Color(0xFF4D4639),
    outline = Color(0xFF7E766A),
)

private val DarkScheme = darkColorScheme(
    primary = Color(0xFFF2C14E),
    onPrimary = Color(0xFF3F2D00),
    primaryContainer = Color(0xFF5B4300),
    onPrimaryContainer = Color(0xFFFFDF9B),
    secondary = Color(0xFFDCC5A1),
    onSecondary = Color(0xFF3A2F16),
    secondaryContainer = Color(0xFF52432A),
    onSecondaryContainer = Color(0xFFF9E0BC),
    tertiary = Color(0xFFFFB694),
    onTertiary = Color(0xFF4F1D00),
    tertiaryContainer = Color(0xFF692F00),
    onTertiaryContainer = Color(0xFFFFDAC8),
    background = Color(0xFF181109),
    onBackground = Color(0xFFEDE1CE),
    surface = Color(0xFF181109),
    onSurface = Color(0xFFEDE1CE),
    surfaceVariant = Color(0xFF4D4639),
    onSurfaceVariant = Color(0xFFD0C5B3),
    outline = Color(0xFF998F7F),
)

@Composable
fun JarlyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme: ColorScheme = if (darkTheme) DarkScheme else LightScheme

    MaterialExpressiveTheme(
        colorScheme = colorScheme,
        motionScheme = MotionScheme.expressive(),
        shapes = Shapes(),
        typography = Typography(),
        content = content,
    )
}
