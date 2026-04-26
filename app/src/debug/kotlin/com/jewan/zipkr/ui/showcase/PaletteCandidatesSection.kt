package com.jewan.zipkr.ui.showcase

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.jewan.zipkr.ui.theme.ActivePalette
import com.jewan.zipkr.ui.theme.Palette
import com.jewan.zipkr.ui.theme.Palettes
import com.jewan.zipkr.ui.theme.ZipkrSpacing

/**
 * 팔레트 카탈로그를 한 화면에 시각화한다.
 * - 데이터는 [Palettes.all] 단일 출처에서 가져온다 — 새 팔레트 추가 시 자동 반영.
 * - [ActivePalette]로 선택된 후보는 카드 헤더에 ✅ 표시한다.
 * - 본 섹션은 디자인 시스템 회귀 검증 도구로 자산화하며, 출시본은 SearchScreen으로 교체된다.
 */
@Composable
fun PaletteCandidatesSection() {
    Column(verticalArrangement = Arrangement.spacedBy(ZipkrSpacing.md)) {
        Text(text = "Palette 카탈로그", style = MaterialTheme.typography.headlineLarge)
        Text(
            text = "활성 팔레트는 Palettes.kt의 ActivePalette 한 줄로 교체한다.",
            style = MaterialTheme.typography.bodySmall,
        )
        Palettes.all.forEach { PaletteCard(it) }
    }
}

@Composable
private fun PaletteCard(palette: Palette) {
    val isActive = palette.code == ActivePalette.code
    val activeMark = if (isActive) " ✅ Active" else ""
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.medium)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(ZipkrSpacing.md),
        verticalArrangement = Arrangement.spacedBy(ZipkrSpacing.sm),
    ) {
        Text(
            text = "${palette.code}. ${palette.name}$activeMark",
            style = MaterialTheme.typography.titleMedium,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(ZipkrSpacing.sm)) {
            Swatch("Primary", palette.primary)
            Swatch("PrimaryDark", palette.primaryDark)
            Swatch("Surface", palette.surface)
            Swatch("Subtle", palette.subtle)
            Swatch("Error", palette.error)
        }
        ZipNumberPreview(primary = palette.primary, onPrimary = palette.onPrimary)
        Text(
            text = "Pretendard 가나다 ABC 06234",
            style = MaterialTheme.typography.bodyMedium,
            color = palette.primary,
        )
    }
}

@Composable
private fun Swatch(
    name: String,
    color: Color,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier =
                Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(color),
        )
        Text(text = name, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun ZipNumberPreview(
    primary: Color,
    onPrimary: Color,
) {
    Box(
        modifier =
            Modifier
                .clip(MaterialTheme.shapes.medium)
                .background(primary)
                .padding(horizontal = ZipkrSpacing.md, vertical = ZipkrSpacing.sm),
    ) {
        Text(
            text = "📮 06234",
            style = MaterialTheme.typography.titleLarge,
            color = onPrimary,
        )
    }
}
