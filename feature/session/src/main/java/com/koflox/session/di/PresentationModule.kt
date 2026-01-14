package com.koflox.session.di

import com.koflox.session.presentation.session.SessionViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

internal val presentationModule = module {
    viewModelOf(::SessionViewModel)
}
