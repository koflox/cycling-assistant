package com.koflox.cyclingassistant

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.koflox.cyclingassistant.navigation.AppNavHost
import com.koflox.cyclingassistant.ui.theme.CyclingAssistantTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CyclingAssistantTheme {
                AppNavHost()
            }
        }
    }
}
