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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Гарнитура шрифтов
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
    appFontFamily: String = "System",
    content: @Composable () -> Unit
) {
    val currentFontFamily = try {
        when (appFontFamily) {
            "Serif" -> FontFamily.Serif
            "Monospace" -> FontFamily.Monospace
            "Default" -> FontFamily.Default
            else -> FontFamily.SansSerif
        }
    } catch (_: Exception) {
        FontFamily.SansSerif
    }

    val safeScale = fontSizeScale.coerceIn(0.6f, 1.4f)
    
    val baseTypography = Typography(
        bodyLarge = TextStyle(
            fontFamily = currentFontFamily,
            fontSize = 16.sp,
            lineHeight = 24.sp
        ),
        bodyMedium = TextStyle(
            fontFamily = currentFontFamily,
            fontSize = 14.sp,
            lineHeight = 20.sp
        ),
        bodySmall = TextStyle(
            fontFamily = currentFontFamily,
            fontSize = 12.sp,
            lineHeight = 16.sp
        ),
        titleLarge = TextStyle(
            fontFamily = currentFontFamily,
            fontSize = 22.sp,
            lineHeight = 28.sp,
            fontWeight = FontWeight.Bold
        ),
        titleMedium = TextStyle(
            fontFamily = currentFontFamily,
            fontSize = 18.sp,
            lineHeight = 24.sp,
            fontWeight = FontWeight.SemiBold
        ),
        titleSmall = TextStyle(
            fontFamily = currentFontFamily,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.Medium
        ),
        labelLarge = TextStyle(
            fontFamily = currentFontFamily,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.Medium
        ),
        labelMedium = TextStyle(
            fontFamily = currentFontFamily,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            fontWeight = FontWeight.Medium
        ),
        labelSmall = TextStyle(
            fontFamily = currentFontFamily,
            fontSize = 10.sp,
            lineHeight = 14.sp,
            fontWeight = FontWeight.Medium
        )
    )

    val currentDensity = LocalDensity.current
    val customDensity = Density(
        density = currentDensity.density,
        fontScale = currentDensity.fontScale * safeScale
    )

    CompositionLocalProvider(
        LocalDensity provides customDensity
    ) {
        MaterialTheme(
            colorScheme = DarkColorScheme,
            typography = baseTypography,
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