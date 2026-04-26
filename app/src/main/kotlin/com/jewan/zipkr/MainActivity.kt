package com.jewan.zipkr

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.jewan.zipkr.ui.search.SearchScreen
import com.jewan.zipkr.ui.theme.ZipkrTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ZipkrTheme {
                SearchScreen(
                    // Phase 4에서 상세 화면 네비게이션으로 교체된다.
                    onCardClick = { /* TODO: Phase 4 detail navigation */ },
                )
            }
        }
    }
}
