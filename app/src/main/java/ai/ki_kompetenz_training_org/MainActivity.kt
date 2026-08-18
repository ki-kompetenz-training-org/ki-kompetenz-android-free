package ai.ki_kompetenz_training_org

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import ai.ki_kompetenz_training_org.data.prefs.SettingsStore
import ai.ki_kompetenz_training_org.ui.navigation.BottomNavScreen
import ai.ki_kompetenz_training_org.ui.theme.KiKompetenzTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.util.Locale

/**
 * Tracks the currently applied locale so we only recreate when it actually changes.
 */
private var appliedLocaleTag: String? = null

class MainActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context) {
        // Apply persisted language before super so resources load in correct locale
        val prefs = newBase.getSharedPreferences("kikompetenz_settings", Context.MODE_PRIVATE)
        val lang = prefs.getString("language", SettingsStore.LANG_SYSTEM) ?: SettingsStore.LANG_SYSTEM
        val locale = when (lang) {
            SettingsStore.LANG_DE -> Locale("de")
            SettingsStore.LANG_EN -> Locale("en")
            else -> Locale.getDefault()
        }
        appliedLocaleTag = lang
        val config = Configuration(newBase.resources.configuration)
        config.setLocale(locale)
        super.attachBaseContext(newBase.createConfigurationContext(config))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KiKompetenzTheme {
                // Watch for DataStore language changes → recreate if locale actually changed
                val app = applicationContext as KiKompetenzApp
                val dataStoreLang by app.settingsStore.language.collectAsState(
                    initial = appliedLocaleTag ?: SettingsStore.LANG_SYSTEM
                )
                LaunchedEffect(dataStoreLang) {
                    if (dataStoreLang != null && dataStoreLang != appliedLocaleTag) {
                        // Persist to SharedPreferences so attachBaseContext picks it up
                        getSharedPreferences("kikompetenz_settings", MODE_PRIVATE)
                            .edit().putString("language", dataStoreLang).apply()
                        appliedLocaleTag = dataStoreLang
                        recreate()
                    }
                }
                BottomNavScreen()
            }
        }
    }
}
