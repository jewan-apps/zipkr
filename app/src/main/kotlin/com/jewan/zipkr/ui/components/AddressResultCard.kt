package com.jewan.zipkr.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.jewan.zipkr.R
import com.jewan.zipkr.data.Address
import com.jewan.zipkr.ui.theme.ZipkrSpacing
import com.jewan.zipkr.ui.theme.ZipkrTheme

/**
 * 검색 결과 단일 카드이다.
 * - 카드 자체 탭 → 상세 화면 진입.
 * - 우측 복사 아이콘 탭 → 우편번호만 즉시 클립보드 복사 (즉시성 동선 핵심).
 * - 도로명·지번·우편번호 점보·영문주소 4 항목 모두 노출 (spec §3 결과 리스트 정의).
 */
@Composable
fun AddressResultCard(
    address: Address,
    onCardClick: () -> Unit,
    onCopyZip: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable(onClick = onCardClick),
        shape = MaterialTheme.shapes.medium,
    ) {
        Row(
            modifier = Modifier.padding(ZipkrSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(ZipkrSpacing.xs),
            ) {
                Text(text = address.roadAddress, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = address.jibunAddress,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "📮 ${address.zipCode}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = address.englishAddress,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = onCopyZip) {
                Icon(
                    imageVector = Icons.Outlined.ContentCopy,
                    contentDescription = stringResource(R.string.copy_zip_action),
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AddressResultCardPreview() {
    ZipkrTheme {
        AddressResultCard(
            address =
                Address(
                    zipCode = "06234",
                    roadAddress = "서울특별시 강남구 테헤란로 123",
                    jibunAddress = "역삼동 736-1",
                    englishAddress = "123, Teheran-ro, Gangnam-gu, Seoul",
                ),
            onCardClick = {},
            onCopyZip = {},
        )
    }
}
