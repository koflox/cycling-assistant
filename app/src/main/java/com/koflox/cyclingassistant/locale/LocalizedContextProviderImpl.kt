package com.koflox.cyclingassistant.locale

import android.content.Context
import android.content.res.Configuration
import com.koflox.designsystem.context.LocalizedContextProvider
import com.koflox.locale.domain.model.AppLanguage
import com.koflox.locale.domain.usecase.ObserveLocaleUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import java.util.Locale

internal class LocalizedContextProviderImpl(
    private val applicationContext: Context,
    observeLocaleUseCase: ObserveLocaleUseCase,
) : LocalizedContextProvider {

    private val currentLanguage: StateFlow<AppLanguage> = observeLocaleUseCase.observeLanguage()
        .stateIn(
            scope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
            started = SharingStarted.Eagerly,
            initialValue = AppLanguage.DEFAULT,
        )

    @Volatile
    private var cachedEntry: Pair<AppLanguage, Context>? = null

    override fun getLocalizedContext(): Context {
        val language = currentLanguage.value
        cachedEntry?.let { (cachedLang, cachedCtx) ->
            if (cachedLang == language) return cachedCtx
        }
        val locale = Locale.forLanguageTag(language.code)
        val config = Configuration(applicationContext.resources.configuration).apply {
            setLocale(locale)
        }
        val ctx = applicationContext.createConfigurationContext(config)
        cachedEntry = language to ctx
        return ctx
    }
}
