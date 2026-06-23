package com.jewan.zipkr.ui.components

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.jewan.zipkr.R
import com.jewan.zipkr.data.Coordinate
import com.jewan.zipkr.ui.theme.ZipkrSpacing
import com.jewan.zipkr.util.kakaoMapDeepLink
import com.jewan.zipkr.util.kakaoMapWebFallback
import com.jewan.zipkr.util.naverMapDeepLink
import com.jewan.zipkr.util.naverMapWebFallback

// 카카오 공식 브랜드 컬러 — 카카오 로그인 SDK 가이드 기준이다.
private val KAKAO_YELLOW = Color(0xFFFEE500)
private val KAKAO_TEXT = Color(0xFF191919)

// 네이버 공식 브랜드 컬러 — 네이버 그린.
private val NAVER_GREEN = Color(0xFF03C75A)
private val NAVER_TEXT = Color.White

/**
 * 외부 지도 앱 진입 2 버튼이다.
 * - 카카오맵·네이버지도 deep link 시도 → 미설치 시 web fallback URL을 ACTION_VIEW로 연다.
 * - 좌표가 null이어도 주소 query 기반 fallback URL로 동작 (좌표 fetch 실패 시 graceful degradation).
 * - 각 버튼 배경은 해당 서비스의 공식 브랜드 컬러로 — "어디로 진입하는지"를 즉시 인지시킨다.
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
        BrandButton(
            label = stringResource(R.string.map_open_kakao),
            container = KAKAO_YELLOW,
            content = KAKAO_TEXT,
            onClick = {
                openOrFallback(
                    context = context,
                    deepLink = kakaoMapDeepLink(coord, address),
                    webFallback = kakaoMapWebFallback(address),
                )
            },
            modifier = Modifier.weight(1f),
        )
        BrandButton(
            label = stringResource(R.string.map_open_naver),
            container = NAVER_GREEN,
            content = NAVER_TEXT,
            onClick = {
                openOrFallback(
                    context = context,
                    deepLink = naverMapDeepLink(coord, address),
                    webFallback = naverMapWebFallback(address),
                )
            },
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun BrandButton(
    label: String,
    container: Color,
    content: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors =
            ButtonDefaults.buttonColors(
                containerColor = container,
                contentColor = content,
            ),
    ) {
        Text(label)
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
