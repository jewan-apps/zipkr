package com.jewan.zipkr

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

/**
 * 앱 전역 진입점이다.
 * 디버그 빌드에서만 Timber DebugTree를 심어 로컬 콘솔로 로그를 흘린다.
 * 릴리스 빌드는 Phase 7에서 Crashlytics Tree로 교체한다.
 */
@HiltAndroidApp
class ZipkrApp : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}
