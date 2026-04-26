package com.jewan.zipkr.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * 카드·버튼·다이얼로그의 라운딩 토큰을 정의한다.
 * 기본 카드 라운딩은 16dp — 모던 톤의 핵심.
 */
internal val ZipkrShapes =
    Shapes(
        extraSmall = RoundedCornerShape(4.dp),
        small = RoundedCornerShape(8.dp),
        medium = RoundedCornerShape(16.dp),
        large = RoundedCornerShape(24.dp),
        extraLarge = RoundedCornerShape(32.dp),
    )
