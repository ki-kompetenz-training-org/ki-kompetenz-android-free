package ai.ki_kompetenz_training_org.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.google.common.truth.Truth.assertThat
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.plus
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.util.Locale

/**
 * Language preference resolution + persistence.
 *
 * [SettingsStore.effectiveLanguage] depends on [Locale.getDefault], so every
 * test pins a deterministic device locale: GERMANY is installed in [setUp] and
 * the original locale is restored in [tearDown]. Tests that need a different
 * locale override it locally.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SettingsLanguageTest {

    @get:Rule
    val tmp = TemporaryFolder()

    private lateinit var originalLocale: Locale

    @Before
    fun setUp() {
        originalLocale = Locale.getDefault()
        Locale.setDefault(Locale.GERMANY)
    }

    @After
    fun tearDown() {
        Locale.setDefault(originalLocale)
    }

    /** Same DataStore/JVM-store pattern as SettingsStoreLastLessonTest. */
    private fun TestScope.createStore(context: Context = mockk()): SettingsStore {
        val file = tmp.newFile("settings_${System.nanoTime()}.preferences_pb")
        val ds = PreferenceDataStoreFactory.create(
            scope = backgroundScope + UnconfinedTestDispatcher(),
        ) { file }
        return SettingsStore(context, dataStoreOverride = ds)
    }

    @Test
    fun `explicit de app language resolves to de`() = runTest {
        val store = createStore()
        assertThat(store.effectiveLanguage(SettingsStore.LANG_DE)).isEqualTo("de")
    }

    @Test
    fun `explicit en app language resolves to en`() = runTest {
        val store = createStore()
        assertThat(store.effectiveLanguage(SettingsStore.LANG_EN)).isEqualTo("en")
    }

    @Test
    fun `system app language resolves to de on german device locale`() = runTest {
        Locale.setDefault(Locale.GERMANY)
        val store = createStore()
        assertThat(store.effectiveLanguage(SettingsStore.LANG_SYSTEM)).isEqualTo("de")
    }

    @Test
    fun `system app language resolves to en on british device locale`() = runTest {
        Locale.setDefault(Locale.UK)
        val store = createStore()
        assertThat(store.effectiveLanguage(SettingsStore.LANG_SYSTEM)).isEqualTo("en")
    }

    @Test
    fun `null app language resolves to de on german device locale`() = runTest {
        Locale.setDefault(Locale.GERMANY)
        val store = createStore()
        assertThat(store.effectiveLanguage(null)).isEqualTo("de")
    }

    @Test
    fun `unsupported app language falls back to en`() = runTest {
        val store = createStore()
        assertThat(store.effectiveLanguage("fr")).isEqualTo("en")
    }

    @Test
    fun `setLanguage de round trips through the language flow`() = runTest {
        // setLanguage also writes SharedPreferences synchronously, so a relaxed
        // Context mock is needed for the getSharedPreferences(...).edit() chain.
        val store = createStore(context = mockk(relaxed = true))
        store.setLanguage(SettingsStore.LANG_DE)
        assertThat(store.language.first()).isEqualTo(SettingsStore.LANG_DE)
    }

    @Test
    fun `language defaults to system when never set`() = runTest {
        val store = createStore()
        assertThat(store.language.first()).isEqualTo(SettingsStore.LANG_SYSTEM)
    }
}
