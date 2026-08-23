package ai.ki_kompetenz_training_org.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
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
class SettingsStore(private val context: Context) {
    companion object {
        private val LANGUAGE_KEY = stringPreferencesKey("language")
        private val KIBOT_HELLO_SHOWN = booleanPreferencesKey("kibot_hello_shown")
        private val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        const val LANG_SYSTEM = "system"
        const val LANG_DE = "de"
        const val LANG_EN = "en"
    }

    /** Current language preference: "system", "de", or "en". */
    val language: Flow<String> = context.settingsDataStore.data.map { prefs ->
        prefs[LANGUAGE_KEY] ?: LANG_SYSTEM
    }

    /** Whether the KiBot hello dialog has been shown. */
    val kibotHelloShown: Flow<Boolean> = context.settingsDataStore.data.map { prefs ->
        prefs[KIBOT_HELLO_SHOWN] ?: false
    }

    /** Whether onboarding has been completed. */
    val onboardingCompleted: Flow<Boolean> = context.settingsDataStore.data.map { prefs ->
        prefs[ONBOARDING_COMPLETED] ?: false
    }

    /** Mark KiBot hello dialog as shown. */
    suspend fun markKibotHelloShown() {
        context.settingsDataStore.edit { it[KIBOT_HELLO_SHOWN] = true }
    }

    /** Mark onboarding as completed. */
    suspend fun markOnboardingCompleted() {
        context.settingsDataStore.edit { it[ONBOARDING_COMPLETED] = true }
    }

    /** Set language preference. Writes to both DataStore (async flow) and SharedPreferences (sync for attachBaseContext). */
    suspend fun setLanguage(lang: String) {
        context.settingsDataStore.edit { it[LANGUAGE_KEY] = lang }
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
