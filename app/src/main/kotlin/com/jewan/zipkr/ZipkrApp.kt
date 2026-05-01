package com.jewan.zipkr

import android.app.Application
import com.google.android.gms.ads.MobileAds
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

/**
 * 앱 전역 진입점이다.
 * 디버그 빌드는 Timber DebugTree로 로컬 콘솔만 사용한다.
 * 릴리스 빌드는 CrashlyticsTree로 WARN 이상 로그·예외를 Firebase Crashlytics에 자동 전송한다.
 *
 * AdMob SDK는 화면 진입마다가 아닌 앱 시작 시 한 번만 init한다.
 */
@HiltAndroidApp
class ZipkrApp : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(CrashlyticsTree())
        }
        MobileAds.initialize(this) {}
    }
}

/**
 * Timber 로그를 Crashlytics 비치명 이벤트로 전송한다.
 * WARN 미만은 무시한다 (디버그성 INFO·DEBUG·VERBOSE는 운영에서 노이즈).
 */
private class CrashlyticsTree : Timber.Tree() {
    override fun log(
        priority: Int,
        tag: String?,
        message: String,
        t: Throwable?,
    ) {
        if (priority < android.util.Log.WARN) return
        FirebaseCrashlytics.getInstance().log("[$tag] $message")
        if (t != null) FirebaseCrashlytics.getInstance().recordException(t)
    }
}
