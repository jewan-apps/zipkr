package com.jewan.zipkr.ads

import android.content.Context
import android.util.DisplayMetrics
import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.jewan.zipkr.BuildConfig

// 적응형 배너의 일반 높이 ~50-100dp. 광고 응답 전 reserved 영역으로 60dp 확보해 layout jump 회피.
private val MIN_BANNER_HEIGHT = 60.dp

/**
 * AdMob 적응형 배너 광고이다.
 * - debug 빌드: BuildConfig.ADMOB_BANNER_UNIT_ID = Google 공식 테스트 ID ("Test Ad" 라벨 표시)
 * - release 빌드: local.properties로 주입된 실 광고 단위 ID (Phase 7에서 주입)
 *
 * MobileAds.initialize는 ZipkrApp.onCreate에서 한 번만 호출한다 — 화면 진입마다 init하지 않는다.
 * AdView의 layoutParams MATCH_PARENT는 명시 필요 — Compose AndroidView 안에서 wrap_content로 측정되면
 * Width 0이 되어 광고 요청이 안 나간다.
 */
@Composable
fun AdBanner(modifier: Modifier = Modifier) {
    AndroidView(
        modifier = modifier.fillMaxWidth().height(MIN_BANNER_HEIGHT),
        factory = { ctx ->
            AdView(ctx).apply {
                layoutParams =
                    ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                    )
                setAdSize(adaptiveBannerSize(ctx))
                adUnitId = BuildConfig.ADMOB_BANNER_UNIT_ID
                loadAd(AdRequest.Builder().build())
            }
        },
    )
}

private fun adaptiveBannerSize(context: Context): AdSize {
    val metrics: DisplayMetrics = context.resources.displayMetrics
    val widthDp = (metrics.widthPixels / metrics.density).toInt()
    return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, widthDp)
}
