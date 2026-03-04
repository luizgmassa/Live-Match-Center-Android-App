package com.massa.livecenter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.massa.livecenter.presentation.ui.screen.LiveMatchCenterScreen
import com.massa.livecenter.ui.theme.LiveCenterTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LiveCenterTheme {
                LiveMatchCenterScreen()
            }
        }
    }
}
