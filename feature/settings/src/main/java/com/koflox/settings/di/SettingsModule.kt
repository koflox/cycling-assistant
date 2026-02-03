package com.koflox.settings.di

import com.koflox.locale.di.localeModule
import com.koflox.profile.di.profileModule
import com.koflox.theme.di.themeModule
import org.koin.dsl.module

val settingsModule = module {
    includes(
        themeModule,
        localeModule,
        profileModule,
        presentationModule,
    )
}
