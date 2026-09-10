package ai.ki_kompetenz_training_org.data.prefs

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.plus
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import io.mockk.mockk
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.lang.System

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsStoreReminderTest {

    @get:Rule
    val tmp = TemporaryFolder()

    private fun TestScope.createStore(): SettingsStore {
        val file = tmp.newFile("settings_reminder_${System.nanoTime()}.preferences_pb")
        val ds = PreferenceDataStoreFactory.create(
            scope = backgroundScope + UnconfinedTestDispatcher(),
        ) { file }
        return SettingsStore(mockk(), dataStoreOverride = ds)
    }

    @Test
    fun `reminder enabled by default`() = runTest {
        val store = createStore()
        assertEquals(true, store.reminderEnabled.first())
    }

    @Test
    fun `default reminder time is 19 00`() = runTest {
        val store = createStore()
        assertEquals(Pair(19, 0), store.reminderTime.first())
    }

    @Test
    fun `reminder toggle round trip`() = runTest {
        val store = createStore()
        store.setReminderEnabled(false)
        assertEquals(false, store.reminderEnabled.first())
        store.setReminderEnabled(true)
        assertEquals(true, store.reminderEnabled.first())
    }

    @Test
    fun `reminder time round trip`() = runTest {
        val store = createStore()
        store.setReminderTime(9, 0)
        assertEquals(Pair(9, 0), store.reminderTime.first())
        store.setReminderTime(13, 0)
        assertEquals(Pair(13, 0), store.reminderTime.first())
    }
}
