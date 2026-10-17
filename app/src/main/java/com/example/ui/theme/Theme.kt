package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import com.example.data.model.ThemeMode

@Composable
fun CinematicTheme(
    themeMode: ThemeMode = ThemeMode.DARK,
    accentColor: Color = CinemaGold,
    content: @Composable () -> Unit
) {
    val isSystemInDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDark
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
    }

    val colorScheme = if (isDark) {
        darkColorScheme(
            primary = accentColor,
            onPrimary = if (accentColor.luminance() > 0.5f) Color.Black else Color.White,
            primaryContainer = accentColor.copy(alpha = 0.25f),
            onPrimaryContainer = Color.White,
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
    } else {
        lightColorScheme(
            primary = accentColor,
            onPrimary = if (accentColor.luminance() > 0.5f) Color.Black else Color.White,
            primaryContainer = accentColor.copy(alpha = 0.15f),
            onPrimaryContainer = Color(0xFF1E1E1E),
            secondary = CinemaRed,
            onSecondary = Color.White,
            secondaryContainer = Color(0xFFFFEAEA),
            onSecondaryContainer = Color(0xFF8C0009),
            tertiary = Color(0xFFD49E0F),
            onTertiary = Color.White,
            background = Color(0xFFF6F7F9),
            onBackground = Color(0xFF18191D),
            surface = Color.White,
            onSurface = Color(0xFF18191D),
            surfaceVariant = Color(0xFFE8E9ED),
            onSurfaceVariant = Color(0xFF565863),
            outline = Color(0x22000000),
            outlineVariant = Color(0x11000000)
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = PersianTypography,
        content = content
    )
}
