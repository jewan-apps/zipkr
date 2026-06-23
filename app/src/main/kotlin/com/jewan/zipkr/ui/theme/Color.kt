package com.jewan.zipkr.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * 앱의 컬러 토큰이다.
 * - 브랜드 컬러(Brand·Subtle·Error 등)는 [ActivePalette]에서 derive된다 (Palettes.kt에서 한 줄 교체).
 * - 다크 모드의 Surface/OnSurface 등 universal 톤은 본 파일에서 직접 정의한다 (팔레트와 무관).
 * - Light/Dark 분기는 Theme.kt가 담당한다.
 */
internal object ZipkrColors {
    // Brand — ActivePalette에서 derive
    val BrandAccent: Color = ActivePalette.primary
    val BrandAccentDark: Color = ActivePalette.primaryDark
    val OnBrand: Color = ActivePalette.onPrimary

    // Surface — light는 팔레트에서, dark는 universal 어두운 톤
    val SurfaceLight: Color = ActivePalette.surface
    val SurfaceDark: Color = Color(0xFF111827)
    val OnSurfaceLight: Color = Color(0xFF111827)
    val OnSurfaceDark: Color = Color(0xFFF9FAFB)

    // Subtle — light는 팔레트에서, dark는 universal
    val SubtleLight: Color = ActivePalette.subtle
    val SubtleDark: Color = Color(0xFF1F2937)
    val OnSubtleLight: Color = Color(0xFF6B7280)
    val OnSubtleDark: Color = Color(0xFF9CA3AF)

    // Error — 팔레트에서, dark variant는 universal 핑크-레드
    val ErrorLight: Color = ActivePalette.error
    val ErrorDark: Color = Color(0xFFF87171)
}
