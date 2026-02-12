package com.koflox.cyclingassistant.app

import android.app.Application
import android.content.Context
import androidx.lifecycle.SavedStateHandle
import org.junit.Test
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.test.verify.verify

@OptIn(KoinExperimentalAPI::class)
class DiVerificationTest {

    @Test
    fun `all Koin modules are correctly wired`() {
        appModule.verify(
            extraTypes = listOf(
                Context::class,
                Application::class,
                SavedStateHandle::class,
            ),
        )
    }
}
