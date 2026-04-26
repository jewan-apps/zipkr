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
private fun pretendardFont(
    weight: FontWeight,
    axisValue: Int,
) = Font(
    R.font.pretendard_variable,
    weight = weight,
    variationSettings = FontVariation.Settings(FontVariation.weight(axisValue)),
)

private val Pretendard =
    FontFamily(
        pretendardFont(FontWeight.Normal, 400),
        pretendardFont(FontWeight.Medium, 500),
        pretendardFont(FontWeight.Bold, 700),
    )

private fun pretendardStyle(
    weight: FontWeight,
    sizeSp: Int,
    lineHeightSp: Int,
) = TextStyle(
    fontFamily = Pretendard,
    fontWeight = weight,
    fontSize = sizeSp.sp,
    lineHeight = lineHeightSp.sp,
)

internal val ZipkrTypography =
    Typography(
        displayLarge = pretendardStyle(FontWeight.Bold, sizeSp = 48, lineHeightSp = 56),
        headlineLarge = pretendardStyle(FontWeight.Bold, sizeSp = 32, lineHeightSp = 40),
        titleLarge = pretendardStyle(FontWeight.Medium, sizeSp = 22, lineHeightSp = 28),
        titleMedium = pretendardStyle(FontWeight.Medium, sizeSp = 16, lineHeightSp = 24),
        bodyLarge = pretendardStyle(FontWeight.Normal, sizeSp = 16, lineHeightSp = 24),
        bodyMedium = pretendardStyle(FontWeight.Normal, sizeSp = 14, lineHeightSp = 20),
        bodySmall = pretendardStyle(FontWeight.Normal, sizeSp = 12, lineHeightSp = 16),
        labelLarge = pretendardStyle(FontWeight.Medium, sizeSp = 14, lineHeightSp = 20),
    )
