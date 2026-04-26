@file:OptIn(ExperimentalTextApi::class)

package com.jewan.zipkr.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.jewan.zipkr.R

/**
 * Pretendard 가변 폰트 기반 타이포그래피를 정의한다.
 * Material3의 Typography 카테고리에 매핑한다.
 *
 * 가변 폰트의 wght 축은 FontVariation.Settings로 명시적으로 지정해야 실제 굵기가 적용된다.
 * 누락 시 OS가 동일 마스터로 fallback하고 합성 굵게(faux-bold)를 적용해 시각 품질이 떨어진다.
 */
private val Pretendard = FontFamily(
    Font(
        R.font.pretendard_variable,
        weight = FontWeight.Normal,
        variationSettings = FontVariation.Settings(FontVariation.weight(400)),
    ),
    Font(
        R.font.pretendard_variable,
        weight = FontWeight.Medium,
        variationSettings = FontVariation.Settings(FontVariation.weight(500)),
    ),
    Font(
        R.font.pretendard_variable,
        weight = FontWeight.Bold,
        variationSettings = FontVariation.Settings(FontVariation.weight(700)),
    ),
)

internal val ZipkrTypography = Typography(
    displayLarge = TextStyle(fontFamily = Pretendard, fontWeight = FontWeight.Bold, fontSize = 48.sp, lineHeight = 56.sp),
    headlineLarge = TextStyle(fontFamily = Pretendard, fontWeight = FontWeight.Bold, fontSize = 32.sp, lineHeight = 40.sp),
    titleLarge = TextStyle(fontFamily = Pretendard, fontWeight = FontWeight.Medium, fontSize = 22.sp, lineHeight = 28.sp),
    titleMedium = TextStyle(fontFamily = Pretendard, fontWeight = FontWeight.Medium, fontSize = 16.sp, lineHeight = 24.sp),
    bodyLarge = TextStyle(fontFamily = Pretendard, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontFamily = Pretendard, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall = TextStyle(fontFamily = Pretendard, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 16.sp),
    labelLarge = TextStyle(fontFamily = Pretendard, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp),
)
