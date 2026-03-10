package com.koflox.settings.presentation

import android.app.Application
import android.content.pm.ApplicationInfo
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.koflox.di.DefaultDispatcher
import com.koflox.locale.domain.model.AppLanguage
import com.koflox.locale.domain.usecase.ObserveLocaleUseCase
import com.koflox.locale.domain.usecase.UpdateLocaleUseCase
import com.koflox.profile.domain.model.InvalidWeightException
import com.koflox.profile.domain.usecase.GetRiderWeightUseCase
import com.koflox.profile.domain.usecase.UpdateRiderWeightUseCase
import com.koflox.theme.domain.model.AppTheme
import com.koflox.theme.domain.usecase.ObserveThemeUseCase
import com.koflox.theme.domain.usecase.UpdateThemeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
internal class SettingsViewModel @Inject internal constructor(
    private val application: Application,
    private val observeThemeUseCase: ObserveThemeUseCase,
    private val updateThemeUseCase: UpdateThemeUseCase,
    private val observeLocaleUseCase: ObserveLocaleUseCase,
    private val updateLocaleUseCase: UpdateLocaleUseCase,
    private val getRiderWeightUseCase: GetRiderWeightUseCase,
    private val updateRiderWeightUseCase: UpdateRiderWeightUseCase,
    @param:DefaultDispatcher private val dispatcherDefault: CoroutineDispatcher,
) : ViewModel() {

    companion object {
        private val WEIGHT_INPUT_DEBOUNCE = 300.milliseconds
        private const val BUILD_INFO_SEPARATOR = "\u2022"
        private const val BUILD_TYPE_DEBUG = "debug"
        private const val BUILD_TYPE_RELEASE = "release"
    }

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private var weightUpdateJob: Job? = null

    init {
        initialize()
    }

    private fun initialize() {
        observeSettings()
        loadInitialData()
    }

    fun onEvent(event: SettingsUiEvent) {
        when (event) {
            is SettingsUiEvent.RiderWeightChanged -> scheduleWeightUpdate(event.input)
            else -> viewModelScope.launch(dispatcherDefault) {
                when (event) {
                    is SettingsUiEvent.ThemeSelected -> updateTheme(event.theme)
                    is SettingsUiEvent.LanguageSelected -> updateLanguage(event.language)
                    SettingsUiEvent.ThemeDropdownToggled -> toggleThemeDropdown()
                    SettingsUiEvent.LanguageDropdownToggled -> toggleLanguageDropdown()
                    SettingsUiEvent.DropdownsDismissed -> dismissDropdowns()
                    is SettingsUiEvent.RiderWeightChanged -> Unit
                }
            }
        }
    }

    private fun observeSettings() {
        viewModelScope.launch(dispatcherDefault) {
            combine(
                observeThemeUseCase.observeTheme(),
                observeLocaleUseCase.observeLanguage(),
            ) { theme, language ->
                Pair(theme, language)
            }.collect { (theme, language) ->
                _uiState.update {
                    it.copy(
                        selectedTheme = theme,
                        selectedLanguage = language,
                    )
                }
            }
        }
    }

    private fun loadInitialData() {
        viewModelScope.launch(dispatcherDefault) {
            val weightKg = getRiderWeightUseCase.getRiderWeightKg()
            val buildInfo = resolveBuildInfo()
            _uiState.update {
                it.copy(
                    riderWeightKg = formatWeight(weightKg),
                    buildInfoText = buildInfo,
                )
            }
        }
    }

    private fun resolveBuildInfo(): String {
        val packageInfo = application.packageManager.getPackageInfo(application.packageName, 0)
        val versionName = packageInfo.versionName.orEmpty()
        val isDebuggable = application.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
        val buildType = if (isDebuggable) BUILD_TYPE_DEBUG else BUILD_TYPE_RELEASE
        val arch = Build.SUPPORTED_ABIS?.firstOrNull().orEmpty()
        return "$versionName $BUILD_INFO_SEPARATOR $buildType $BUILD_INFO_SEPARATOR $arch"
    }

    private fun formatWeight(weightKg: Float?): String {
        if (weightKg == null) return ""
        return if (weightKg == weightKg.toLong().toFloat()) {
            weightKg.toLong().toString()
        } else {
            weightKg.toString()
        }
    }

    private suspend fun updateTheme(theme: AppTheme) {
        updateThemeUseCase.updateTheme(theme)
        _uiState.update { it.copy(isThemeDropdownExpanded = false) }
    }

    private suspend fun updateLanguage(language: AppLanguage) {
        updateLocaleUseCase.updateLanguage(language)
        _uiState.update { it.copy(isLanguageDropdownExpanded = false) }
    }

    private fun toggleThemeDropdown() {
        _uiState.update {
            it.copy(
                isThemeDropdownExpanded = !it.isThemeDropdownExpanded,
                isLanguageDropdownExpanded = false,
            )
        }
    }

    private fun toggleLanguageDropdown() {
        _uiState.update {
            it.copy(
                isLanguageDropdownExpanded = !it.isLanguageDropdownExpanded,
                isThemeDropdownExpanded = false,
            )
        }
    }

    private fun scheduleWeightUpdate(input: String) {
        _uiState.update { it.copy(riderWeightKg = input) }
        weightUpdateJob?.cancel()
        weightUpdateJob = viewModelScope.launch(dispatcherDefault) {
            delay(WEIGHT_INPUT_DEBOUNCE)
            updateRiderWeightUseCase.updateRiderWeightKg(input)
                .onSuccess {
                    _uiState.update { it.copy(riderWeightError = null) }
                }
                .onFailure { error ->
                    val weightError = (error as? InvalidWeightException)?.let {
                        RiderWeightError(
                            minWeightKg = it.minWeightKg.toInt(),
                            maxWeightKg = it.maxWeightKg.toInt(),
                        )
                    }
                    _uiState.update { it.copy(riderWeightError = weightError) }
                }
        }
    }

    private fun dismissDropdowns() {
        _uiState.update {
            it.copy(isThemeDropdownExpanded = false, isLanguageDropdownExpanded = false)
        }
    }
}
