package com.jewan.zipkr.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jewan.zipkr.ui.theme.ZipkrSpacing
import com.jewan.zipkr.ui.theme.ZipkrTheme

/**
 * 검색 로딩 중 표시하는 스켈레톤 카드 N장이다.
 * 사용자에게 "곧 결과가 온다"는 즉시성 신호를 준다.
 */
@Composable
fun LoadingSkeleton(
    cardCount: Int = 3,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(ZipkrSpacing.md),
        verticalArrangement = Arrangement.spacedBy(ZipkrSpacing.md),
    ) {
        repeat(cardCount) { SkeletonCard() }
    }
}

@Composable
private fun SkeletonCard() {
    val transition = rememberInfiniteTransition(label = "skeleton")
    val alpha by transition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 800), RepeatMode.Reverse),
        label = "skeletonAlpha",
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(ZipkrSpacing.md)
            .alpha(alpha),
        verticalArrangement = Arrangement.spacedBy(ZipkrSpacing.sm),
    ) {
        Spacer(Modifier.fillMaxWidth().height(16.dp).clip(MaterialTheme.shapes.extraSmall).background(MaterialTheme.colorScheme.outline))
        Spacer(Modifier.fillMaxWidth(0.7f).height(12.dp).clip(MaterialTheme.shapes.extraSmall).background(MaterialTheme.colorScheme.outline))
        Spacer(Modifier.fillMaxWidth(0.5f).height(12.dp).clip(MaterialTheme.shapes.extraSmall).background(MaterialTheme.colorScheme.outline))
    }
}

@Preview(showBackground = true)
@Composable
private fun LoadingSkeletonPreview() {
    ZipkrTheme { LoadingSkeleton() }
}
