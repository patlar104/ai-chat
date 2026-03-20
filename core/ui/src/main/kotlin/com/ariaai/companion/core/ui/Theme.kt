package com.ariaai.companion.core.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val DarkColorScheme = darkColorScheme(
    background = AppColors.Background,
    surface = AppColors.Surface,
    surfaceContainer = AppColors.SurfaceContainer,
    surfaceVariant = AppColors.SurfaceVariant,
    primary = AppColors.Primary,
    onPrimary = AppColors.OnPrimary,
    onSurface = AppColors.OnSurface,
    onSurfaceVariant = AppColors.OnSurfaceVariant,
    error = AppColors.Error,
    onError = AppColors.OnError,
)

private val AppTypography = Typography(
    bodyLarge = TextStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 24.sp, // 16 * 1.5
    ),
    labelMedium = TextStyle(
        fontSize = 14.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 19.6.sp, // 14 * 1.4
    ),
    headlineSmall = TextStyle(
        fontSize = 20.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 24.sp, // 20 * 1.2
    ),
    displaySmall = TextStyle(
        fontSize = 28.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 30.8.sp, // 28 * 1.1
    ),
)

@Composable
fun AICompanionTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = AppTypography,
        content = content,
    )
}
