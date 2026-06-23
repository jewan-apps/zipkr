package com.jewan.zipkr.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jewan.zipkr.R
import com.jewan.zipkr.data.Sido
import com.jewan.zipkr.ui.theme.ZipkrSpacing

/**
 * 검색바와 한 줄에 묶이는 시·도 선택 앵커이다.
 * 검색바와 동일한 height·radius·outline 패턴을 공유해 "한 컴포넌트의 두 부분"으로 읽힌다.
 *
 * - 미선택: outline 사각 pill, 라벨 "전국"
 * - 선택: brand fill, 라벨 = 시도 짧은 이름 ("서울", "경기" 등)
 * - 클릭 시 onOpen() — 호스트가 ModalBottomSheet를 띄움
 *
 * 디자인 결정:
 * - location 아이콘 제거 — caret만으로도 "선택 가능" affordance 충분 (군더더기 회피)
 * - shape를 검색바(OutlinedTextField와 동일한 RoundedCornerShape)와 통일
 */
@Composable
fun SidoAnchor(
    selected: Sido?,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isActive = selected != null
    val container = if (isActive) MaterialTheme.colorScheme.primary else Color.Transparent
    val content = if (isActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
    val border =
        if (isActive) {
            null
        } else {
            BorderStroke(width = ANCHOR_BORDER_WIDTH, color = MaterialTheme.colorScheme.outline)
        }
    Surface(
        onClick = onOpen,
        shape = RoundedCornerShape(ANCHOR_RADIUS),
        color = container,
        contentColor = content,
        border = border,
        modifier = modifier.height(ANCHOR_HEIGHT),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = ANCHOR_HPAD),
        ) {
            Text(
                text = selected?.label() ?: stringResource(R.string.sido_anchor_default),
                fontSize = ANCHOR_LABEL_SIZE,
                fontWeight = FontWeight.SemiBold,
                color = content,
            )
            Spacer(Modifier.width(ZipkrSpacing.xs))
            Icon(
                imageVector = Icons.Outlined.ExpandMore,
                contentDescription = stringResource(R.string.sido_anchor_action),
                modifier = Modifier.size(ANCHOR_CARET_SIZE),
                tint = content,
            )
        }
    }
}

// 검색바와 통일하기 위한 공유 시각 토큰 — SearchScreen.SearchBar의 OutlinedTextField shape도 같은 값을 쓴다
private val ANCHOR_HEIGHT = 56.dp
private val ANCHOR_RADIUS = 14.dp
private val ANCHOR_HPAD = 14.dp
private val ANCHOR_BORDER_WIDTH = 1.dp
private val ANCHOR_CARET_SIZE = 16.dp
private val ANCHOR_LABEL_SIZE = 14.sp
