package com.jewan.zipkr.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.jewan.zipkr.data.Address
import com.jewan.zipkr.ui.theme.ZipkrSpacing

private val ZIP_JUMBO_SIZE = 32.sp
private val LABEL_BODY_SIZE = 13.sp
private val SUBTLE_BODY_SIZE = 12.sp

/**
 * 상세 시트 상단의 점보 우편번호 + 풀 주소 영역이다.
 * 건물명은 빈 문자열일 때 숨긴다.
 */
@Composable
fun DetailHeader(
    address: Address,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(horizontal = ZipkrSpacing.md, vertical = ZipkrSpacing.sm),
        verticalArrangement = Arrangement.spacedBy(ZipkrSpacing.xs),
    ) {
        Text(
            text = address.zipCode,
            fontSize = ZIP_JUMBO_SIZE,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = address.roadAddress,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = address.jibunAddress,
            fontSize = LABEL_BODY_SIZE,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (address.buildingName.isNotEmpty()) {
            Text(
                text = address.buildingName,
                fontSize = LABEL_BODY_SIZE,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium,
            )
        }
        Text(
            text = address.englishAddress,
            fontSize = SUBTLE_BODY_SIZE,
            fontStyle = FontStyle.Italic,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
