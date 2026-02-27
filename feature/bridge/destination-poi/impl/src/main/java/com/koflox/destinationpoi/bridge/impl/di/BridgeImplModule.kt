package com.koflox.destinationpoi.bridge.impl.di

import com.koflox.destinationpoi.bridge.impl.navigator.PoiUiNavigatorImpl
import com.koflox.destinationpoi.bridge.navigator.PoiUiNavigator
import org.koin.dsl.module

val destinationPoiBridgeImplModule = module {
    factory<PoiUiNavigator> {
        PoiUiNavigatorImpl()
    }
}
