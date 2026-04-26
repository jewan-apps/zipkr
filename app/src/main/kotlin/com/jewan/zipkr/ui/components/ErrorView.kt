package com.jewan.zipkr.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.jewan.zipkr.ui.theme.ZipkrSpacing
import com.jewan.zipkr.ui.theme.ZipkrTheme

/**
 * API 호출 실패·네트워크 오류 등 사용자에게 재시도를 권할 때 노출한다.
 * 사용자 친화 메시지만 표시하고, 내부 예외 원문은 노출하지 않는다 (헌법 §1.9).
 *
 * retryLabel은 호출부에서 stringResource로 주입하기 위해 파라미터화한다.
 * Phase 5 i18n에서 컴포넌트 본체 변경 없이 KO/EN 레이블이 교체된다.
 */
@Composable
fun ErrorView(
    message: String,
    onRetry: () -> Unit,
    retryLabel: String = "다시 시도",
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(ZipkrSpacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(ZipkrSpacing.md),
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center,
        )
        Button(onClick = onRetry) { Text(retryLabel) }
    }
}

@Preview(showBackground = true)
@Composable
private fun ErrorViewPreview() {
    ZipkrTheme {
        ErrorView(message = "잠시 후 다시 시도해주세요", onRetry = {})
    }
}
