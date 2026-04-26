package com.jewan.zipkr.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * 앱의 컬러 토큰을 정의한다.
 * - Light/Dark 시스템 자동 추종 (Theme.kt에서 결정).
 * - 브랜드 액센트는 단일 색(Indigo 600 계열) — 절제된 모던 톤.
 */
internal object ZipkrColors {
    val BrandAccent = Color(0xFF4F46E5)      // Indigo 600
    val BrandAccentDark = Color(0xFF818CF8)  // Indigo 400 (다크 모드용)

    val SurfaceLight = Color(0xFFFFFFFF)
    val SurfaceDark = Color(0xFF111827)
    val OnSurfaceLight = Color(0xFF111827)
    val OnSurfaceDark = Color(0xFFF9FAFB)

    val SubtleLight = Color(0xFFF3F4F6)
    val SubtleDark = Color(0xFF1F2937)
    val OnSubtleLight = Color(0xFF6B7280)
    val OnSubtleDark = Color(0xFF9CA3AF)

    val ErrorLight = Color(0xFFDC2626)
    val ErrorDark = Color(0xFFF87171)
}
