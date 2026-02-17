package com.koflox.cyclingassistant

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.ContextThemeWrapper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.koflox.cyclingassistant.navigation.AppNavHost
import com.koflox.cyclingassistant.ui.theme.CyclingAssistantTheme
import com.koflox.locale.domain.model.AppLanguage
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.Locale

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleIntent(intent)
        enableEdgeToEdge()
        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val navController = rememberNavController()
            when (val state = uiState) {
                MainUiState.Loading -> LoadingScreen()
                is MainUiState.Ready -> {
                    LocalizedContent(language = state.language) {
                        CyclingAssistantTheme(themePreference = state.theme) {
                            AppNavHost(navController = navController)
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        viewModel.handleIntent(intent?.action)
        intent?.action = null
    }
}

@Composable
private fun LoadingScreen() {
    CyclingAssistantTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator()
        }
    }
}

@Composable
private fun LocalizedContent(
    language: AppLanguage,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val locale = Locale.forLanguageTag(language.code)
    val configuration = LocalConfiguration.current
    val localizedConfiguration = remember(language) {
        Configuration(configuration).apply { setLocale(locale) }
    }
    val localizedContext = remember(language) {
        ContextThemeWrapper(context, R.style.Theme_CyclingAssistant).apply {
            applyOverrideConfiguration(localizedConfiguration)
        }
    }
    CompositionLocalProvider(
        LocalContext provides localizedContext,
        LocalConfiguration provides localizedConfiguration,
    ) {
        content()
    }
}
