package com.jewan.zipkr.ui.components

import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.ViewGroup
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
 *
 * 디자인 결정:
 * - layoutParams MATCH_PARENT 명시: AndroidView가 자식 wrap_content로 측정하면 html viewport가 0이 되어
 *   100% 높이가 0px로 그려진다.
 * - update에서 html 식별자(tag) 비교 후에만 reload: 매 recompose 무조건 reload하면 SDK 로딩 도중 페이지가 끊긴다.
 * - onTouch에서 requestDisallowInterceptTouchEvent(true): ModalBottomSheet의 swipe 제스처가 WebView 내부
 *   드래그(지도 이동)를 가로채는 걸 막는다.
 */
@SuppressLint("SetJavaScriptEnabled", "ClickableViewAccessibility")
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
                layoutParams =
                    ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                    )
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                tag = html
                setOnTouchListener { view, event ->
                    if (event.action == MotionEvent.ACTION_DOWN) {
                        view.parent.requestDisallowInterceptTouchEvent(true)
                    }
                    false
                }
                loadDataWithBaseURL(MAP_BASE_URL, html, MAP_MIME_TYPE, MAP_ENCODING, null)
            }
        },
        update = { webView ->
            if (webView.tag != html) {
                webView.tag = html
                webView.loadDataWithBaseURL(MAP_BASE_URL, html, MAP_MIME_TYPE, MAP_ENCODING, null)
            }
        },
    )
}
