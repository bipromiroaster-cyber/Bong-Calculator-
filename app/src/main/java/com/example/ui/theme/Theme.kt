package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Immutable
data class BongExtendedColors(
    val isDark: Boolean,
    val bgTop: Color,
    val bgMid: Color,
    val bgBottom: Color,
    val glowBlue: Color,
    val glowPurple: Color,
    val accentBlue: Color,
    val accentPurple: Color,
    val dangerAccent: Color,
    val displaySurface: Color,
    val keySurface: Color,
    val operatorSurface: Color,
    val scientificSurface: Color,
    val dangerSurface: Color,
    val glassBorderHighlight: Color,
    val glassBorderSubtle: Color,
    val sheetSurface: Color,
    val textPrimary: Color,
    val textSecondary: Color
)

private val DarkExtendedColors = BongExtendedColors(
    isDark = true,
    bgTop = DarkBgTop,
    bgMid = DarkBgMid,
    bgBottom = DarkBgBottom,
    glowBlue = DarkGlowBlue,
    glowPurple = DarkGlowPurple,
    accentBlue = NeonCyan,
    accentPurple = ElectricPurple,
    dangerAccent = CoralRed,
    displaySurface = DarkGlassDisplayBg,
    keySurface = DarkGlassSurface,
    operatorSurface = DarkGlassOperator,
    scientificSurface = DarkGlassScientific,
    dangerSurface = DarkGlassDanger,
    glassBorderHighlight = DarkGlassBorderLight,
    glassBorderSubtle = DarkGlassBorderDark,
    sheetSurface = DarkSheetSurface,
    textPrimary = DarkTextPrimary,
    textSecondary = DarkTextSecondary
)

private val LightExtendedColors = BongExtendedColors(
    isDark = false,
    bgTop = LightBgTop,
    bgMid = LightBgMid,
    bgBottom = LightBgBottom,
    glowBlue = LightGlowBlue,
    glowPurple = LightGlowPurple,
    accentBlue = CobaltBlue,
    accentPurple = RoyalPurple,
    dangerAccent = LightCoralRed,
    displaySurface = LightGlassDisplayBg,
    keySurface = LightGlassSurface,
    operatorSurface = LightGlassOperator,
    scientificSurface = LightGlassScientific,
    dangerSurface = LightGlassDanger,
    glassBorderHighlight = LightGlassBorderLight,
    glassBorderSubtle = LightGlassBorderDark,
    sheetSurface = LightSheetSurface,
    textPrimary = LightTextPrimary,
    textSecondary = LightTextSecondary
)

private val PremiumExtendedColors = BongExtendedColors(
    isDark = true,
    bgTop = PremiumBgTop,
    bgMid = PremiumBgMid,
    bgBottom = PremiumBgBottom,
    glowBlue = PremiumGlowGold,
    glowPurple = PremiumGlowTeal,
    accentBlue = PremiumGold,
    accentPurple = PremiumTeal,
    dangerAccent = PremiumRose,
    displaySurface = PremiumGlassDisplayBg,
    keySurface = PremiumGlassSurface,
    operatorSurface = PremiumGlassOperator,
    scientificSurface = PremiumGlassScientific,
    dangerSurface = PremiumGlassDanger,
    glassBorderHighlight = PremiumGlassBorderLight,
    glassBorderSubtle = PremiumGlassBorderDark,
    sheetSurface = PremiumSheetSurface,
    textPrimary = DarkTextPrimary,
    textSecondary = Color(0xFFCBD5E1)
)

val LocalBongColors = staticCompositionLocalOf { DarkExtendedColors }

private val DarkColorScheme = darkColorScheme(
    primary = NeonBlue,
    onPrimary = Color(0xFF041224),
    primaryContainer = Color(0xFF132A5E),
    onPrimaryContainer = DarkTextPrimary,
    secondary = ElectricPurple,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF2E1A5E),
    onSecondaryContainer = DarkTextPrimary,
    tertiary = NeonCyan,
    background = DarkBgTop,
    onBackground = DarkTextPrimary,
    surface = DarkSheetSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkGlassSurface,
    onSurfaceVariant = DarkTextSecondary,
    error = CoralRed,
    onError = Color.White,
    outline = DarkGlassBorderLight
)

private val LightColorScheme = lightColorScheme(
    primary = CobaltBlue,
    onPrimary = Color.White,
    primaryContainer = LightGlassOperator,
    onPrimaryContainer = LightTextPrimary,
    secondary = RoyalPurple,
    onSecondary = Color.White,
    secondaryContainer = LightGlassScientific,
    onSecondaryContainer = LightTextPrimary,
    tertiary = CobaltBlue,
    background = LightBgTop,
    onBackground = LightTextPrimary,
    surface = LightSheetSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightGlassSurface,
    onSurfaceVariant = LightTextSecondary,
    error = LightCoralRed,
    onError = Color.White,
    outline = LightGlassBorderDark
)

private val PremiumColorScheme = darkColorScheme(
    primary = PremiumGold,
    onPrimary = Color(0xFF1A1200),
    primaryContainer = Color(0xFF3F2B00),
    onPrimaryContainer = DarkTextPrimary,
    secondary = PremiumTeal,
    onSecondary = Color(0xFF021F1A),
    secondaryContainer = Color(0xFF0A3631),
    onSecondaryContainer = DarkTextPrimary,
    tertiary = PremiumGold,
    background = PremiumBgTop,
    onBackground = DarkTextPrimary,
    surface = PremiumSheetSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = PremiumGlassSurface,
    onSurfaceVariant = Color(0xFFCBD5E1),
    error = PremiumRose,
    onError = Color.White,
    outline = PremiumGlassBorderLight
)

private val BongShapes = Shapes(
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp)
)

@Composable
fun BongCalculatorTheme(
    darkTheme: Boolean = true,
    usePremiumTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        usePremiumTheme -> PremiumColorScheme
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val extendedColors = when {
        usePremiumTheme -> PremiumExtendedColors
        darkTheme -> DarkExtendedColors
        else -> LightExtendedColors
    }

    CompositionLocalProvider(LocalBongColors provides extendedColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = BongTypography,
            shapes = BongShapes,
            content = content
        )
    }
}
