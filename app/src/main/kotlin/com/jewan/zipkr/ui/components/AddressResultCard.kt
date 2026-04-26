package com.jewan.zipkr.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
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
 * - 우측 큰 IconButton 탭 → 우편번호 즉시 복사 (즉시성 동선 핵심).
 * - 카드 footer chip 2개 → 한글주소·영문주소 부가 복사.
 * - 도로명·지번·우편번호 점보·영문주소 4 항목 모두 노출 (spec §3 결과 리스트 정의).
 *
 * onCopyAddress(label, text)는 단일 콜백으로 통합한다 — 호출부에서 어떤 항목인지 모르고,
 * label은 클립보드 라벨/토스트 표시에 활용된다.
 */
@Composable
fun AddressResultCard(
    address: Address,
    onCardClick: () -> Unit,
    onCopyAddress: (label: String, text: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val zipLabel = stringResource(R.string.copy_zip_label)
    val roadLabel = stringResource(R.string.copy_road_label)
    val englishLabel = stringResource(R.string.copy_english_label)

    Card(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable(onClick = onCardClick),
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(
            modifier = Modifier.padding(ZipkrSpacing.md),
            verticalArrangement = Arrangement.spacedBy(ZipkrSpacing.sm),
        ) {
            CardHeader(
                address = address,
                onCopyZip = { onCopyAddress(zipLabel, address.zipCode) },
            )
            CopyChips(
                onCopyRoad = { onCopyAddress(roadLabel, address.roadAddress) },
                onCopyEnglish = { onCopyAddress(englishLabel, address.englishAddress) },
            )
        }
    }
}

@Composable
private fun CardHeader(
    address: Address,
    onCopyZip: () -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
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

@Composable
private fun CopyChips(
    onCopyRoad: () -> Unit,
    onCopyEnglish: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(ZipkrSpacing.sm),
    ) {
        CopyChip(label = stringResource(R.string.copy_road_chip), onClick = onCopyRoad)
        CopyChip(label = stringResource(R.string.copy_english_chip), onClick = onCopyEnglish)
    }
}

@Composable
private fun CopyChip(
    label: String,
    onClick: () -> Unit,
) {
    AssistChip(
        onClick = onClick,
        label = { Text(label, style = MaterialTheme.typography.bodySmall) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Outlined.ContentCopy,
                contentDescription = null,
                modifier = Modifier.size(AssistChipDefaults.IconSize),
            )
        },
    )
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
            onCopyAddress = { _, _ -> },
        )
    }
}
