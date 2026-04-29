package com.koflox.strava.impl.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.koflox.di.DefaultDispatcher
import com.koflox.strava.api.model.StravaAuthState
import com.koflox.strava.impl.domain.usecase.StravaAuthUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class StravaSettingsViewModel @Inject constructor(
    private val authUseCase: StravaAuthUseCase,
    @param:DefaultDispatcher private val dispatcherDefault: CoroutineDispatcher,
) : ViewModel() {

    private val _authState = MutableStateFlow<StravaAuthState>(StravaAuthState.LoggedOut)
    val authState: StateFlow<StravaAuthState> = _authState.asStateFlow()

    init {
        viewModelScope.launch(dispatcherDefault) {
            authUseCase.observeAuthState().collect { _authState.value = it }
        }
    }
}
