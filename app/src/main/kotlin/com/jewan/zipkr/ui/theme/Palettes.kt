package com.jewan.zipkr.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * 브랜드 컬러 후보(팔레트) 카탈로그이다.
 *
 * 후속 99개 양산 앱이 모두 같은 패턴으로 brand color를 결정하도록 카탈로그·교체 패턴을 자산화한다.
 * - 새 팔레트 추가는 본 파일에 Palette 한 개 추가하면 끝 — 쇼케이스에도 자동 노출된다.
 * - 활성 팔레트 교체는 [ActivePalette] 한 줄만 바꾸면 끝.
 * - Color.kt의 [ZipkrColors] 토큰은 [ActivePalette]에서 자동 derive된다.
 */
internal data class Palette(
    val code: String,
    val name: String,
    val primary: Color,
    val primaryDark: Color,
    val onPrimary: Color,
    val surface: Color,
    val subtle: Color,
    val error: Color,
)

internal object Palettes {
    val KoreaPostRed =
        Palette(
            code = "A",
            name = "Korea Post Red (정통)",
            primary = Color(0xFFE60012),
            primaryDark = Color(0xFFFCA5A5),
            onPrimary = Color.White,
            surface = Color(0xFFFFFFFF),
            subtle = Color(0xFFFEE2E2),
            error = Color(0xFFF59E0B),
        )

    val BrickRed =
        Palette(
            code = "B",
            name = "Brick Red (모던 우체국)",
            primary = Color(0xFFB91C1C),
            primaryDark = Color(0xFFFCA5A5),
            onPrimary = Color.White,
            surface = Color(0xFFFFFFFF),
            subtle = Color(0xFFFEF2F2),
            error = Color(0xFFEA580C),
        )

    val PostalNavy =
        Palette(
            code = "C",
            name = "Postal Navy + Red Accent",
            primary = Color(0xFF1E3A8A),
            primaryDark = Color(0xFF93C5FD),
            onPrimary = Color.White,
            surface = Color(0xFFFFFFFF),
            subtle = Color(0xFFEFF6FF),
            error = Color(0xFFDC2626),
        )

    val PostalYellow =
        Palette(
            code = "D",
            name = "Deutsche Post Yellow",
            primary = Color(0xFFFACC15),
            primaryDark = Color(0xFFFDE047),
            onPrimary = Color.Black,
            surface = Color(0xFFFFFEF7),
            subtle = Color(0xFFFEF9C3),
            error = Color(0xFFDC2626),
        )

    val CrimsonCream =
        Palette(
            code = "E",
            name = "Crimson + Cream (도장+한지)",
            primary = Color(0xFF9B1C1C),
            primaryDark = Color(0xFFFCA5A5),
            onPrimary = Color(0xFFFFF8F0),
            surface = Color(0xFFFFF8F0),
            subtle = Color(0xFFFFEDD5),
            error = Color(0xFFDC2626),
        )

    val ForestRed =
        Palette(
            code = "F",
            name = "Forest + Red Accent (친환경)",
            primary = Color(0xFF15803D),
            primaryDark = Color(0xFF86EFAC),
            onPrimary = Color.White,
            surface = Color(0xFFFFFFFF),
            subtle = Color(0xFFF0FDF4),
            error = Color(0xFFDC2626),
        )

    val CobaltVermillion =
        Palette(
            code = "G",
            name = "Cobalt + Vermillion (모던 한국)",
            primary = Color(0xFF1D4ED8),
            primaryDark = Color(0xFF93C5FD),
            onPrimary = Color.White,
            surface = Color(0xFFFFFFFF),
            subtle = Color(0xFFEFF6FF),
            error = Color(0xFFEF4444),
        )

    val Indigo =
        Palette(
            code = "H",
            name = "Indigo (Material 기본)",
            primary = Color(0xFF4F46E5),
            primaryDark = Color(0xFF818CF8),
            onPrimary = Color.White,
            subtle = Color(0xFFEEF2FF),
            surface = Color(0xFFFFFFFF),
            error = Color(0xFFDC2626),
        )

    /** 쇼케이스/카탈로그 노출 순서 — A~H 정렬. */
    val all =
        listOf(
            KoreaPostRed,
            BrickRed,
            PostalNavy,
            PostalYellow,
            CrimsonCream,
            ForestRed,
            CobaltVermillion,
            Indigo,
        )
}

/**
 * 현재 활성 팔레트.
 * 브랜드 컬러 교체 시 본 한 줄만 바꾸면 [ZipkrColors]·`Theme.kt`·쇼케이스가 모두 따라 바뀐다.
 */
internal val ActivePalette: Palette = Palettes.BrickRed
