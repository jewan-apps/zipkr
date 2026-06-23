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
 * - 기본은 [ActivePalette] 기반 brand 컬러 우선 (Dynamic Color OFF).
 *   양산 앱의 brand 정체성을 wallpaper 추출 색에 양보하지 않기 위함이다.
 * - Dynamic Color를 원하면 호출부에서 dynamicColor = true 명시 (Material You 옵트인).
 * - 시스템 다크 모드 자동 추종.
 */
@Composable
fun ZipkrTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
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
                    onPrimary = ZipkrColors.OnBrand,
                    background = ZipkrColors.SurfaceDark,
                    surface = ZipkrColors.SurfaceDark,
                    onSurface = ZipkrColors.OnSurfaceDark,
                    surfaceVariant = ZipkrColors.SubtleDark,
                    onSurfaceVariant = ZipkrColors.OnSubtleDark,
                    error = ZipkrColors.ErrorDark,
                )
            else ->
                lightColorScheme(
                    primary = ZipkrColors.BrandAccent,
                    onPrimary = ZipkrColors.OnBrand,
                    background = ZipkrColors.SurfaceLight,
                    surface = ZipkrColors.SurfaceLight,
                    onSurface = ZipkrColors.OnSurfaceLight,
                    surfaceVariant = ZipkrColors.SubtleLight,
                    onSurfaceVariant = ZipkrColors.OnSubtleLight,
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
