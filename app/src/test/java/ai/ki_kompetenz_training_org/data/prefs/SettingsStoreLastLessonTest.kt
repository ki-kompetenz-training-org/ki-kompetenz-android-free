package ai.ki_kompetenz_training_org.data.prefs

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.plus
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsStoreLastLessonTest {

    @get:Rule
    val tmp = TemporaryFolder()

    private fun TestScope.createStore(): SettingsStore {
        val file = tmp.newFile("settings_${System.nanoTime()}.preferences_pb")
        val ds = PreferenceDataStoreFactory.create(
            scope = backgroundScope + UnconfinedTestDispatcher(),
        ) { file }
        return SettingsStore(mockk(), dataStoreOverride = ds)
    }

    @Test
    fun `last lesson is null by default`() = runTest {
        val store = createStore()
        assertNull(store.lastLesson.first())
    }

    @Test
    fun `set and read back last lesson`() = runTest {
        val store = createStore()
        store.setLastLesson("lesson-2", 2)
        assertEquals(SettingsStore.LastLesson("lesson-2", 2), store.lastLesson.first())
    }

    @Test
    fun `overwriting replaces the last lesson`() = runTest {
        val store = createStore()
        store.setLastLesson("lesson-1", 1)
        store.setLastLesson("lesson-3", 3)
        assertEquals(SettingsStore.LastLesson("lesson-3", 3), store.lastLesson.first())
    }
}
