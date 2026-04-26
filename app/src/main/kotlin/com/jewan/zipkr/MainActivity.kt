package com.jewan.zipkr

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.jewan.zipkr.ui.showcase.DesignShowcaseScreen
import com.jewan.zipkr.ui.theme.ZipkrTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ZipkrTheme {
                DesignShowcaseScreen()
            }
        }
    }
}
