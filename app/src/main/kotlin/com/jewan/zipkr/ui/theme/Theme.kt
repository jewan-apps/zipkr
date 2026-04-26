package com.jewan.zipkr.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/**
 * 앱의 단일 진입 테마이다.
 * - Android 12+ Dynamic Color 자동 적용 (사용자 시스템 컬러 추종).
 * - 그 외 OS는 Color.kt의 자체 토큰 fallback.
 * - 시스템 다크 모드 자동 추종.
 */
@Composable
fun ZipkrTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val colors =
        when {
            dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            }
            darkTheme ->
                darkColorScheme(
                    primary = ZipkrColors.BrandAccentDark,
                    background = ZipkrColors.SurfaceDark,
                    surface = ZipkrColors.SurfaceDark,
                    onSurface = ZipkrColors.OnSurfaceDark,
                    error = ZipkrColors.ErrorDark,
                )
            else ->
                lightColorScheme(
                    primary = ZipkrColors.BrandAccent,
                    background = ZipkrColors.SurfaceLight,
                    surface = ZipkrColors.SurfaceLight,
                    onSurface = ZipkrColors.OnSurfaceLight,
                    error = ZipkrColors.ErrorLight,
                )
        }

    MaterialTheme(
        colorScheme = colors,
        typography = ZipkrTypography,
        shapes = ZipkrShapes,
        content = content,
    )
}
