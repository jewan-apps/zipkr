package com.jewan.zipkr.ui.components

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.jewan.zipkr.R
import com.jewan.zipkr.data.Coordinate
import com.jewan.zipkr.ui.theme.ZipkrSpacing
import com.jewan.zipkr.util.kakaoMapDeepLink
import com.jewan.zipkr.util.kakaoMapWebFallback
import com.jewan.zipkr.util.naverMapDeepLink
import com.jewan.zipkr.util.naverMapWebFallback

/**
 * 외부 지도 앱 진입 2 버튼이다.
 * - 카카오맵·네이버지도 deep link 시도 → 미설치 시 web fallback URL을 ACTION_VIEW로 연다.
 * - 좌표가 null이어도 주소 query 기반 fallback URL로 동작 (좌표 fetch 실패 시 graceful degradation).
 */
@Composable
fun MapDeepLinkButtons(
    coord: Coordinate?,
    address: String,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(ZipkrSpacing.sm),
    ) {
        FilledTonalButton(
            onClick = {
                openOrFallback(
                    context = context,
                    deepLink = kakaoMapDeepLink(coord, address),
                    webFallback = kakaoMapWebFallback(address),
                )
            },
            modifier = Modifier.weight(1f),
        ) {
            Text(stringResource(R.string.map_open_kakao))
        }
        FilledTonalButton(
            onClick = {
                openOrFallback(
                    context = context,
                    deepLink = naverMapDeepLink(coord, address),
                    webFallback = naverMapWebFallback(address),
                )
            },
            modifier = Modifier.weight(1f),
        ) {
            Text(stringResource(R.string.map_open_naver))
        }
    }
}

private fun openOrFallback(
    context: Context,
    deepLink: String,
    webFallback: String,
) {
    val deepIntent = Intent(Intent.ACTION_VIEW, Uri.parse(deepLink))
    try {
        context.startActivity(deepIntent)
    } catch (_: ActivityNotFoundException) {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(webFallback)))
    }
}
