package com.koflox.designsystem.context

import android.content.Context

interface LocalizedContextProvider {
    fun getLocalizedContext(): Context
}
