package com.jewan.zipkr.ui.showcase

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.PreviewFontScale
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import com.jewan.zipkr.ui.components.EmptyState
import com.jewan.zipkr.ui.components.ErrorView
import com.jewan.zipkr.ui.components.LoadingSkeleton
import com.jewan.zipkr.ui.theme.ZipkrSpacing
import com.jewan.zipkr.ui.theme.ZipkrTheme

/**
 * 디자인 시스템(컬러·타이포·라운딩·공통 컴포넌트)을 한 화면에 모아 노출한다.
 * - 멀티 프리뷰 어노테이션으로 디바이스/테마/폰트 크기 매트릭스를 IDE에서 검증한다.
 * - MVP 출시본은 SearchScreen으로 교체되며, 본 화면은 후속 앱들의 디자인 회귀 검증 도구로 자산화한다.
 */
@Composable
fun DesignShowcaseScreen(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(ZipkrSpacing.md),
            verticalArrangement = Arrangement.spacedBy(ZipkrSpacing.lg),
        ) {
            SectionHeader("우편번호 — Design Showcase")
            ColorSection()
            TypographySection()
            ShapeSection()
            ComponentSection()
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(text = text, style = MaterialTheme.typography.headlineLarge)
}

@Composable
private fun SubHeader(text: String) {
    Text(text = text, style = MaterialTheme.typography.titleMedium)
}

@Composable
private fun ColorSection() {
    Column(verticalArrangement = Arrangement.spacedBy(ZipkrSpacing.sm)) {
        SubHeader("Colors")
        Row(horizontalArrangement = Arrangement.spacedBy(ZipkrSpacing.sm)) {
            ColorSwatch("Primary", MaterialTheme.colorScheme.primary)
            ColorSwatch("Surface", MaterialTheme.colorScheme.surface)
            ColorSwatch("Variant", MaterialTheme.colorScheme.surfaceVariant)
            ColorSwatch("Error", MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
private fun ColorSwatch(
    name: String,
    color: Color,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier =
                Modifier
                    .size(56.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .background(color),
        )
        Spacer(Modifier.height(ZipkrSpacing.xs))
        Text(text = name, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun TypographySection() {
    Column(verticalArrangement = Arrangement.spacedBy(ZipkrSpacing.sm)) {
        SubHeader("Typography")
        Text("displayLarge 가나다 ABC 06234", style = MaterialTheme.typography.displayLarge)
        Text("headlineLarge 가나다 ABC 06234", style = MaterialTheme.typography.headlineLarge)
        Text("titleLarge 가나다 ABC 06234", style = MaterialTheme.typography.titleLarge)
        Text("bodyLarge 가나다 ABC 06234", style = MaterialTheme.typography.bodyLarge)
        Text("bodySmall 가나다 ABC 06234", style = MaterialTheme.typography.bodySmall)
        Text("labelLarge 가나다 ABC 06234", style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
private fun ShapeSection() {
    Column(verticalArrangement = Arrangement.spacedBy(ZipkrSpacing.sm)) {
        SubHeader("Shapes (extraSmall / small / medium / large / extraLarge)")
        Row(horizontalArrangement = Arrangement.spacedBy(ZipkrSpacing.sm)) {
            listOf(
                MaterialTheme.shapes.extraSmall,
                MaterialTheme.shapes.small,
                MaterialTheme.shapes.medium,
                MaterialTheme.shapes.large,
                MaterialTheme.shapes.extraLarge,
            ).forEach { shape ->
                Box(
                    modifier =
                        Modifier
                            .size(48.dp)
                            .clip(shape)
                            .background(MaterialTheme.colorScheme.primary),
                )
            }
        }
    }
}

@Composable
private fun ComponentSection() {
    Column(verticalArrangement = Arrangement.spacedBy(ZipkrSpacing.md)) {
        SubHeader("Components")
        EmptyState(
            title = "어떤 주소든 입력해보세요",
            description = "도로명·지번·건물명 모두 검색됩니다.",
        )
        ErrorView(message = "잠시 후 다시 시도해주세요", onRetry = {})
        LoadingSkeleton(cardCount = 2)
    }
}

@PreviewScreenSizes
@PreviewLightDark
@PreviewFontScale
@Composable
private fun DesignShowcaseScreenPreview() {
    ZipkrTheme(dynamicColor = false) {
        DesignShowcaseScreen()
    }
}
