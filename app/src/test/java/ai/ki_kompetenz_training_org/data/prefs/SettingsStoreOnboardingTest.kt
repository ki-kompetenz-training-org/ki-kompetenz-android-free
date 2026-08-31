package ai.ki_kompetenz_training_org.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.plus
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsStoreOnboardingTest {

    @get:Rule
    val tmp = TemporaryFolder()

    private fun TestScope.createStore(): SettingsStore {
        val file = tmp.newFile("settings_onboarding_${System.nanoTime()}.preferences_pb")
        val ds = PreferenceDataStoreFactory.create(
            scope = backgroundScope + UnconfinedTestDispatcher(),
        ) { file }
        return SettingsStore(mockk(relaxed = true), dataStoreOverride = ds)
    }

    @Test
    fun `onboarding not completed by default`() = runTest {
        val store = createStore()
        assertFalse(store.onboardingCompleted.first())
    }

    @Test
    fun `markOnboardingCompleted flips the flag`() = runTest {
        val store = createStore()
        store.markOnboardingCompleted()
        assertTrue(store.onboardingCompleted.first())
    }

    @Test
    fun `onboarding language selection persists immediately`() = runTest {
        val store = createStore()
        store.setLanguage(SettingsStore.LANG_EN)
        assertEquals("en", store.language.first())
        store.setLanguage(SettingsStore.LANG_DE)
        assertEquals("de", store.language.first())
    }
}
