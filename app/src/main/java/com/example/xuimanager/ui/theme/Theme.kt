package com.example.xuimanager.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val LocalFontSizeScale = compositionLocalOf { 1.0f }

// Гарнитура шрифта из прототипа Google Stitch / Google Sans (FontFamily.SansSerif)
private val StitchFontFamily = FontFamily.SansSerif

private val DarkColorScheme = darkColorScheme(
    background = DarkBackground,
    surface = DarkCardBg,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    primary = AccentBlue,
    outline = DarkCardBorder
)

@Composable
fun ThreeXUITheme(
    fontSizeScale: Float = 1.0f,
    content: @Composable () -> Unit
) {
    val scaledTypography = Typography(
        bodyLarge = TextStyle(
            fontFamily = StitchFontFamily,
            fontSize = (16 * fontSizeScale).sp,
            lineHeight = (24 * fontSizeScale).sp
        ),
        bodyMedium = TextStyle(
            fontFamily = StitchFontFamily,
            fontSize = (14 * fontSizeScale).sp,
            lineHeight = (20 * fontSizeScale).sp
        ),
        bodySmall = TextStyle(
            fontFamily = StitchFontFamily,
            fontSize = (12 * fontSizeScale).sp,
            lineHeight = (16 * fontSizeScale).sp
        ),
        titleLarge = TextStyle(
            fontFamily = StitchFontFamily,
            fontSize = (22 * fontSizeScale).sp,
            lineHeight = (28 * fontSizeScale).sp,
            fontWeight = FontWeight.Bold
        ),
        titleMedium = TextStyle(
            fontFamily = StitchFontFamily,
            fontSize = (18 * fontSizeScale).sp,
            lineHeight = (24 * fontSizeScale).sp,
            fontWeight = FontWeight.SemiBold
        ),
        titleSmall = TextStyle(
            fontFamily = StitchFontFamily,
            fontSize = (14 * fontSizeScale).sp,
            lineHeight = (20 * fontSizeScale).sp,
            fontWeight = FontWeight.Medium
        ),
        labelLarge = TextStyle(
            fontFamily = StitchFontFamily,
            fontSize = (14 * fontSizeScale).sp,
            lineHeight = (20 * fontSizeScale).sp,
            fontWeight = FontWeight.Medium
        ),
        labelMedium = TextStyle(
            fontFamily = StitchFontFamily,
            fontSize = (12 * fontSizeScale).sp,
            lineHeight = (16 * fontSizeScale).sp,
            fontWeight = FontWeight.Medium
        ),
        labelSmall = TextStyle(
            fontFamily = StitchFontFamily,
            fontSize = (10 * fontSizeScale).sp,
            lineHeight = (14 * fontSizeScale).sp,
            fontWeight = FontWeight.Medium
        )
    )

    CompositionLocalProvider(LocalFontSizeScale provides fontSizeScale) {
        MaterialTheme(
            colorScheme = DarkColorScheme,
            typography = scaledTypography,
            content = content
        )
    }
}