package com.jewan.zipkr

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.jewan.zipkr.ui.search.SearchScreen
import com.jewan.zipkr.ui.theme.ZipkrTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Splash Screen API는 super.onCreate 전에 install해야 system splash가 본 launch theme로 정확히 그려진다.
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (!BuildConfig.SHOW_ADS) {
            hideSystemBarsForStoreScreenshots()
        }
        setContent {
            ZipkrTheme {
                SearchScreen()
            }
        }
    }

    private fun hideSystemBarsForStoreScreenshots() {
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        controller.hide(WindowInsetsCompat.Type.systemBars())
    }
}
