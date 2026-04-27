package com.jewan.zipkr.ui.components

import android.annotation.SuppressLint
import android.webkit.WebView
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.jewan.zipkr.data.Coordinate
import com.jewan.zipkr.util.buildKakaoMapHtml

private const val MAP_BASE_URL = "https://localhost"
private const val MAP_MIME_TYPE = "text/html"
private const val MAP_ENCODING = "UTF-8"
private val MAP_CORNER_RADIUS = 16.dp

/**
 * WebView로 카카오맵 web SDK를 임베드한다.
 * baseUrl을 https://localhost로 둬야 카카오 web 플랫폼 도메인 검사 통과.
 * setJavaScriptEnabled(true)는 카카오 SDK 로딩에 필요하다 — 우리 자체 JS는 주입 안 함.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun KakaoMapWebView(
    coord: Coordinate,
    jsKey: String,
    modifier: Modifier = Modifier,
    level: Int = 3,
) {
    val html = remember(coord, jsKey, level) { buildKakaoMapHtml(coord, jsKey, level) }
    AndroidView(
        modifier = modifier.clip(RoundedCornerShape(MAP_CORNER_RADIUS)),
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                loadDataWithBaseURL(MAP_BASE_URL, html, MAP_MIME_TYPE, MAP_ENCODING, null)
            }
        },
        update = { webView ->
            webView.loadDataWithBaseURL(MAP_BASE_URL, html, MAP_MIME_TYPE, MAP_ENCODING, null)
        },
    )
}
