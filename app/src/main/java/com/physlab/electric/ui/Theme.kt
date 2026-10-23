package com.physlab.electric.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

object LabColors {
    val Background = Color(0xFF050608)
    val Surface = Color(0xFF0D1015)
    val SurfaceRaised = Color(0xFF141922)
    val SurfaceHigh = Color(0xFF1B2230)
    val Outline = Color(0xFF2A3342)
    val OutlineSoft = Color(0xFF1B222D)
    val Primary = Color(0xFF6FD5FF)
    val PrimaryDeep = Color(0xFF1B8DB8)
    val Amber = Color(0xFFFFC857)
    val Violet = Color(0xFFB9A2FF)
    val Green = Color(0xFF62E6A7)
    val Red = Color(0xFFFF6B72)
    val Ink = Color(0xFFF1F6FF)
    val InkMuted = Color(0xFF97A4B8)
    val InkFaint = Color(0xFF5A6678)
    val Board = Color(0xFF080A0E)
}

private val DarkColors = darkColorScheme(
    primary = LabColors.Primary,
    onPrimary = Color(0xFF002B3C),
    primaryContainer = Color(0xFF0F4A63),
    onPrimaryContainer = Color(0xFFC5EEFF),
    secondary = LabColors.Amber,
    onSecondary = Color(0xFF3A2800),
    secondaryContainer = Color(0xFF4A3608),
    onSecondaryContainer = Color(0xFFFFE7B0),
    tertiary = LabColors.Violet,
    onTertiary = Color(0xFF211447),
    background = LabColors.Background,
    onBackground = LabColors.Ink,
    surface = LabColors.Surface,
    onSurface = LabColors.Ink,
    surfaceVariant = LabColors.SurfaceRaised,
    onSurfaceVariant = LabColors.InkMuted,
    surfaceContainer = LabColors.SurfaceRaised,
    surfaceContainerHigh = LabColors.SurfaceHigh,
    surfaceContainerHighest = Color(0xFF222B3A),
    outline = LabColors.Outline,
    outlineVariant = LabColors.OutlineSoft,
    error = LabColors.Red,
    onError = Color(0xFF3A0004),
)

private val LabTypography = Typography(
    displaySmall = TextStyle(fontWeight = FontWeight.Bold, fontSize = 28.sp, letterSpacing = 0.2.sp),
    headlineMedium = TextStyle(fontWeight = FontWeight.Bold, fontSize = 23.sp, letterSpacing = 0.1.sp),
    titleLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 19.sp, letterSpacing = 0.1.sp),
    titleMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 15.sp, letterSpacing = 0.1.sp),
    titleSmall = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 13.sp, letterSpacing = 0.1.sp),
    bodyMedium = TextStyle(fontSize = 13.sp, lineHeight = 19.sp),
    bodySmall = TextStyle(fontSize = 11.5.sp, lineHeight = 17.sp),
    labelLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 13.sp, letterSpacing = 0.2.sp),
    labelMedium = TextStyle(fontWeight = FontWeight.Medium, fontSize = 11.sp, letterSpacing = 0.3.sp),
    labelSmall = TextStyle(fontWeight = FontWeight.Medium, fontSize = 9.5.sp, letterSpacing = 0.3.sp),
)

@Composable
fun ElectricLabTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColors,
        typography = LabTypography,
        content = content,
    )
}
