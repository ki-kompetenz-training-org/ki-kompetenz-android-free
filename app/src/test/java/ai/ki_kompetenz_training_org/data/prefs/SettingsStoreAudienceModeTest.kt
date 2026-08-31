package ai.ki_kompetenz_training_org.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.preferencesDataStoreFile
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
class SettingsStoreAudienceModeTest {

    @get:Rule
    val tmp = TemporaryFolder()

    private fun TestScope.createStore(): SettingsStore {
        val file = tmp.newFile("settings_audience_${System.nanoTime()}.preferences_pb")
        val ds = PreferenceDataStoreFactory.create(
            scope = backgroundScope + UnconfinedTestDispatcher(),
        ) { file }
        return SettingsStore(mockk(), dataStoreOverride = ds)
    }

    @Test
    fun `default audience mode is standard`() = runTest {
        val store = createStore()
        assertEquals("standard", store.audienceMode.first())
    }

    @Test
    fun `audience mode round trip`() = runTest {
        val store = createStore()
        store.setAudienceMode("seniors")
        assertEquals("seniors", store.audienceMode.first())
        store.setAudienceMode("kids")
        assertEquals("kids", store.audienceMode.first())
    }
}
