package com.koflox.destinations.data.source.prefs

import android.content.SharedPreferences
import com.koflox.testing.coroutine.MainDispatcherRule
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class PreferencesDataSourceImplTest {

    companion object {
        private const val KEY_LOADED_FILES = "loaded_destination_files"
        private const val TEST_FILE_NAME = "destinations_tokyo_japan_35.6812_139.7671_tier1.json"
    }

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val prefs: SharedPreferences = mockk(relaxed = true)
    private val editor: SharedPreferences.Editor = mockk(relaxed = true)
    private lateinit var dataSource: PreferencesDataSourceImpl

    @Before
    fun setup() {
        every { prefs.edit() } returns editor
        every { editor.commit() } returns true
        dataSource = PreferencesDataSourceImpl(
            dispatcherIo = mainDispatcherRule.testDispatcher,
            prefs = prefs,
        )
    }

    @Test
    fun `getLoadedFiles returns empty set when no files loaded`() = runTest {
        every { prefs.getStringSet(KEY_LOADED_FILES, emptySet()) } returns emptySet()

        val result = dataSource.getLoadedFiles()

        assertTrue(result.isEmpty())
    }

    @Test
    fun `getLoadedFiles returns previously loaded files`() = runTest {
        val loadedFiles = setOf(TEST_FILE_NAME)
        every { prefs.getStringSet(KEY_LOADED_FILES, emptySet()) } returns loadedFiles

        val result = dataSource.getLoadedFiles()

        assertEquals(loadedFiles, result)
    }

    @Test
    fun `addLoadedFile adds file to existing set`() = runTest {
        val existingFiles = setOf("existing_file.json")
        every { prefs.getStringSet(KEY_LOADED_FILES, emptySet()) } returns existingFiles
        every { editor.putStringSet(any(), any()) } returns editor

        dataSource.addLoadedFile(TEST_FILE_NAME)

        verify { editor.putStringSet(KEY_LOADED_FILES, existingFiles + TEST_FILE_NAME) }
    }

    @Test
    fun `addLoadedFile commits changes`() = runTest {
        every { prefs.getStringSet(KEY_LOADED_FILES, emptySet()) } returns emptySet()
        every { editor.putStringSet(any(), any()) } returns editor

        dataSource.addLoadedFile(TEST_FILE_NAME)

        verify { editor.commit() }
    }
}
