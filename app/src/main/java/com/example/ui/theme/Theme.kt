package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CinematicDarkColorScheme = darkColorScheme(
    primary = CinemaGold,
    onPrimary = Color(0xFF1A1400),
    primaryContainer = Color(0xFF382900),
    onPrimaryContainer = CinemaGoldLight,
    
    secondary = CinemaRed,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF450A0A),
    onSecondaryContainer = Color(0xFFFFDAD6),
    
    tertiary = CinemaGoldLight,
    onTertiary = Color.Black,
    
    background = DarkBackground,
    onBackground = TextPrimary,
    
    surface = DarkSurface,
    onSurface = TextPrimary,
    
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    
    outline = GlassBorder,
    outlineVariant = Color(0x33444455)
)

@Composable
fun CinematicTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CinematicDarkColorScheme,
        typography = PersianTypography,
        content = content
    )
}
