package com.koflox.designsystem.testutil

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.LocaleList
import com.koflox.designsystem.context.LocalizedContextProvider
import io.mockk.every
import io.mockk.mockk
import java.util.Locale

fun mockLocalizedContextProvider(locale: Locale = Locale.US): LocalizedContextProvider {
    val localeList: LocaleList = mockk { every { get(0) } returns locale }
    val configuration: Configuration = mockk { every { locales } returns localeList }
    val resources: Resources = mockk { every { this@mockk.configuration } returns configuration }
    val context: Context = mockk { every { this@mockk.resources } returns resources }
    return mockk { every { getLocalizedContext() } returns context }
}
