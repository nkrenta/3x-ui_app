package com.example.xuimanager.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val LocalFontSizeScale = compositionLocalOf { 1.0f }

// Гарнитура шрифта из прототипа Google Stitch / Google Sans (FontFamily.SansSerif)
val StitchFontFamily = FontFamily.SansSerif
val GeistFontFamily = FontFamily.SansSerif
val GeistMonoFontFamily = FontFamily.Monospace

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

@Composable
fun CyberOpsButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
    shape: Shape = RoundedCornerShape(8.dp),
    content: @Composable RowScope.() -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = shape,
        colors = ButtonDefaults.buttonColors(
            containerColor = AccentBlue.copy(alpha = 0.28f),
            contentColor = AccentCyan,
            disabledContainerColor = DarkCardBg,
            disabledContentColor = TextSecondary
        ),
        border = BorderStroke(1.dp, if (enabled) AccentCyan.copy(alpha = 0.5f) else DarkCardBorder),
        contentPadding = contentPadding,
        content = content
    )
}