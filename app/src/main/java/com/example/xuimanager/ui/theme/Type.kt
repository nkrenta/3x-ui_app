package com.example.xuimanager.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val PlusJakartaSansFontFamily = FontFamily.SansSerif
val JetBrainsMonoFontFamily = FontFamily.Monospace

val Typography = Typography(
    titleMedium = TextStyle(
        fontFamily = PlusJakartaSansFontFamily,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold
    ),
    bodySmall = TextStyle(
        fontFamily = JetBrainsMonoFontFamily,
        fontSize = 12.sp,
        color = Color(0xFF9CA3AF)
    )
)