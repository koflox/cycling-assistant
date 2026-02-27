package com.koflox.map.di

import com.koflox.map.intent.GoogleMapsIntentHelper
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

val mapModule = module {
    single { GoogleMapsIntentHelper(application = androidApplication()) }
}
