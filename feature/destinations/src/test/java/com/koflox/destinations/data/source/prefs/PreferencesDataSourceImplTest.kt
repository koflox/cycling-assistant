package com.koflox.destinations.data.source.prefs

import android.content.SharedPreferences
import com.koflox.testing.coroutine.MainDispatcherRule
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class PreferencesDataSourceImplTest {

    companion object {
        private const val KEY_DB_INITIALIZED = "database_initialized"
    }

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val prefs: SharedPreferences = mockk(relaxed = true)
    private val editor: SharedPreferences.Editor = mockk(relaxed = true)
    private lateinit var dataSource: PreferencesDataSourceImpl

    @Before
    fun setup() {
        every { prefs.edit() } returns editor
        every { editor.putBoolean(any(), any()) } returns editor
        every { editor.commit() } returns true
        dataSource = PreferencesDataSourceImpl(
            dispatcherIo = mainDispatcherRule.testDispatcher,
            prefs = prefs,
        )
    }

    @Test
    fun `isDatabaseInitialized returns false when not initialized`() = runTest {
        every { prefs.getBoolean(KEY_DB_INITIALIZED, false) } returns false

        val result = dataSource.isDatabaseInitialized()

        assertFalse(result)
    }

    @Test
    fun `isDatabaseInitialized returns true when initialized`() = runTest {
        every { prefs.getBoolean(KEY_DB_INITIALIZED, false) } returns true

        val result = dataSource.isDatabaseInitialized()

        assertTrue(result)
    }

    @Test
    fun `isDatabaseInitialized reads correct key from preferences`() = runTest {
        every { prefs.getBoolean(any(), any()) } returns false

        dataSource.isDatabaseInitialized()

        verify { prefs.getBoolean(KEY_DB_INITIALIZED, false) }
    }

    @Test
    fun `setDatabaseInitialized writes true to preferences`() = runTest {
        dataSource.setDatabaseInitialized(true)

        verify { editor.putBoolean(KEY_DB_INITIALIZED, true) }
    }

    @Test
    fun `setDatabaseInitialized writes false to preferences`() = runTest {
        dataSource.setDatabaseInitialized(false)

        verify { editor.putBoolean(KEY_DB_INITIALIZED, false) }
    }

    @Test
    fun `setDatabaseInitialized commits changes`() = runTest {
        dataSource.setDatabaseInitialized(true)

        verify { editor.commit() }
    }
}
