package ai.ki_kompetenz_training_org.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.Locale

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "kikompetenz_settings")

/**
 * Stores user preferences (language, onboarding) in DataStore.
 * No encryption needed - language and onboarding flags are not sensitive data.
 */
class SettingsStore(
    private val context: Context,
    private val dataStoreOverride: DataStore<Preferences>? = null,
) {
    companion object {
        private val LANGUAGE_KEY = stringPreferencesKey("language")
        private val KIBOT_HELLO_SHOWN = booleanPreferencesKey("kibot_hello_shown")
        private val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        private val AUDIENCE_MODE_KEY = stringPreferencesKey("audience_mode")
        private val LAST_LESSON_SLUG = stringPreferencesKey("last_lesson_slug")
        private val LAST_LESSON_INDEX = intPreferencesKey("last_lesson_index")
        const val LANG_SYSTEM = "system"
        const val LANG_DE = "de"
        const val LANG_EN = "en"
    }

    /** UI density mode: "standard" | "kids" | "seniors" (default standard). */
    val audienceMode: Flow<String> = dataStore.data.map { prefs ->
        prefs[AUDIENCE_MODE_KEY] ?: "standard"
    }

    suspend fun setAudienceMode(key: String) {
        dataStore.edit { prefs ->
            prefs[AUDIENCE_MODE_KEY] = key
        }
    }

    /** The lesson the user opened last - powers the continue-learning card on home. */
    data class LastLesson(val slug: String, val index: Int)

    private val dataStore: DataStore<Preferences>
        get() = dataStoreOverride ?: context.settingsDataStore

    /** Current language preference: "system", "de", or "en". */
    val language: Flow<String> = dataStore.data.map { prefs ->
        prefs[LANGUAGE_KEY] ?: LANG_SYSTEM
    }

    /** Raw language preference; null when the user never chose a language. */
    val languageRaw: Flow<String?> = dataStore.data.map { prefs ->
        prefs[LANGUAGE_KEY]
    }

    /** Whether the KiBot hello dialog has been shown. */
    val kibotHelloShown: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[KIBOT_HELLO_SHOWN] ?: false
    }

    /** Whether onboarding has been completed. */
    val onboardingCompleted: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[ONBOARDING_COMPLETED] ?: false
    }

    /** Mark KiBot hello dialog as shown. */
    suspend fun markKibotHelloShown() {
        dataStore.edit { it[KIBOT_HELLO_SHOWN] = true }
    }

    /** Mark onboarding as completed. */
    suspend fun markOnboardingCompleted() {
        dataStore.edit { it[ONBOARDING_COMPLETED] = true }
    }

    /** The lesson the user opened last, for the continue-learning card. */
    val lastLesson: Flow<LastLesson?> = dataStore.data.map { prefs ->
        prefs[LAST_LESSON_SLUG]?.let { slug ->
            LastLesson(slug, prefs[LAST_LESSON_INDEX] ?: 0)
        }
    }

    /** Remember the last opened lesson (slug + lesson number). */
    suspend fun setLastLesson(slug: String, index: Int) {
        dataStore.edit {
            it[LAST_LESSON_SLUG] = slug
            it[LAST_LESSON_INDEX] = index
        }
    }

    /** Set language preference. Writes to both DataStore (async flow) and SharedPreferences (sync for attachBaseContext). */
    suspend fun setLanguage(lang: String) {
        dataStore.edit { it[LANGUAGE_KEY] = lang }
        context.getSharedPreferences("kikompetenz_settings", Context.MODE_PRIVATE)
            .edit().putString("language", lang).apply()
    }

    /** Resolve effective language code from preference. */
    fun effectiveLanguage(appLang: String?): String {
        val raw = if (appLang == LANG_SYSTEM || appLang == null) {
            Locale.getDefault().language
        } else {
            appLang
        }
        return if (raw == "de") "de" else "en"
    }
}
