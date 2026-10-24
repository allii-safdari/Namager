package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.domain.model.ThemeMode

/*

private val ColorOutline =Color(0xFF625A52)
private val NamagerDarkScheme = darkColorScheme(
    primary = NamagerOrange,
    onPrimary = NamagerBlack,
    primaryContainer = NamagerSurfaceVariant,
    onPrimaryContainer = NamagerIvory,
    secondary = NamagerAmber,
    onSecondary = NamagerBlack,
    tertiary = NamagerRed,
    onTertiary = NamagerIvory,
    background = NamagerBlack,
    onBackground = NamagerIvory,
    surface = NamagerSurface,
    onSurface = NamagerIvory,
    surfaceVariant = NamagerSurfaceVariant,
    onSurfaceVariant = NamagerMuted,
    outline = ColorOutline,
)

*/


private val NamagerColors = darkColorScheme(
    primary = Flame, onPrimary = OnFlame,
    secondary = FlameLight, onSecondary = OnFlame,
    tertiary = ImdbGold,
    background = DarkBackground, onBackground = TextPrimary,
    surface = DarkSurface, onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceHighlight, onSurfaceVariant = TextSecondary,
    outline = GlassBorder,
    error = Color(0xFFFF5A5F), onError = OnFlame
)

private val NamagerShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp)
)

object Spacing {
    val xs = 4.dp; val sm = 8.dp; val md = 12.dp
    val lg = 16.dp; val xl = 24.dp; val xxl = 32.dp
}

@Composable
fun NamagerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = NamagerColors,
        typography = NamagerTypography,
        shapes = NamagerShapes,
        content = content
    )
}
/*
@Composable
fun NamagerTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val colors = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && darkTheme -> dynamicDarkColorScheme(context)
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> dynamicLightColorScheme(context)
        else -> NamagerDarkScheme
    }
    MaterialTheme(colorScheme = colors, content = content)
}
*/

/*
@Composable
fun NamagerTheme(
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
*/
