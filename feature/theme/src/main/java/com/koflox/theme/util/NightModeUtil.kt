package com.koflox.theme.util

import android.content.Context
import android.content.res.Configuration

fun isNightMode(context: Context): Boolean {
    val nightMode = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
    return nightMode == Configuration.UI_MODE_NIGHT_YES
}
