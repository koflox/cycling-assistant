package com.koflox.cyclingassistant

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.koflox.cyclingassistant.navigation.AppNavHost
import com.koflox.cyclingassistant.ui.theme.CyclingAssistantTheme
import com.koflox.settings.api.ThemeProvider
import com.koflox.settings.domain.model.AppTheme
import org.koin.compose.koinInject

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeProvider: ThemeProvider = koinInject()
            val theme by themeProvider.observeTheme()
                .collectAsStateWithLifecycle(initialValue = AppTheme.SYSTEM)
            CyclingAssistantTheme(themePreference = theme) {
                AppNavHost()
            }
        }
    }
}
