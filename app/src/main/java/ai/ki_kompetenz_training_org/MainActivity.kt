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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import ai.ki_kompetenz_training_org.data.prefs.SettingsStore
import ai.ki_kompetenz_training_org.ui.navigation.BottomNavScreen
import ai.ki_kompetenz_training_org.ui.onboarding.OnboardingScreen
import ai.ki_kompetenz_training_org.ui.theme.AudienceMode
import ai.ki_kompetenz_training_org.ui.theme.KiKompetenzTheme
import ai.ki_kompetenz_training_org.ui.theme.LocalAudienceMode
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
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
        // Keep the JVM default in sync so Locale.getDefault()-based content
        // selection (lessons, mini games) follows the in-app language.
        Locale.setDefault(locale)
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
                val app = applicationContext as KiKompetenzApp

                // Audience mode: seniors gets 1.15x font scale via LocalDensity.
                val audienceModeRaw by app.settingsStore.audienceMode.collectAsState(
                    initial = "standard"
                )
                val mode = AudienceMode.fromKey(audienceModeRaw)
                val baseDensity = LocalDensity.current
                val scaledDensity = Density(
                    density = baseDensity.density,
                    fontScale = baseDensity.fontScale * mode.fontScaleFactor,
                )
                CompositionLocalProvider(
                    LocalAudienceMode provides mode,
                    LocalDensity provides scaledDensity,
                ) {

                // Watch for DataStore language changes → recreate if locale actually changed.
                // Uses languageRaw: null (= never set) must NOT push "system" over a
                // previously persisted value in SharedPreferences.
                // While onboarding runs, the language is applied via localized
                // composition (no recreate); onboarding completion recreates once.
                val onboardingDone by app.settingsStore.onboardingCompleted.collectAsState(initial = false)
                val dataStoreLang by app.settingsStore.languageRaw.collectAsState(
                    initial = appliedLocaleTag
                )
                LaunchedEffect(dataStoreLang) {
                    if (onboardingDone && dataStoreLang != null && dataStoreLang != appliedLocaleTag) {
                        // Persist to SharedPreferences so attachBaseContext picks it up
                        getSharedPreferences("kikompetenz_settings", MODE_PRIVATE)
                            .edit().putString("language", dataStoreLang).apply()
                        appliedLocaleTag = dataStoreLang
                        recreate()
                    }
                }

                // Show onboarding on first launch
                val onboardingScope = rememberCoroutineScope()
                val pendingLessonStart = rememberSaveable { mutableStateOf(false) }
                var onboardingLangChanged by rememberSaveable { mutableStateOf(false) }
                if (!onboardingDone) {
                    OnboardingScreen(
                        onCompleted = { startLesson1 ->
                            // Persist synchron: recreate() below can destroy the
                            // composition before a launched coroutine would run.
                            kotlinx.coroutines.runBlocking {
                                app.settingsStore.markOnboardingCompleted()
                            }
                            if (startLesson1) pendingLessonStart.value = true
                            if (onboardingLangChanged) {
                                // Fresh activity so resources boot in the new locale
                                recreate()
                            }
                        },
                        settingsStore = app.settingsStore,
                        onLanguageSelected = { onboardingLangChanged = true },
                    )
                } else {
                    BottomNavScreen(
                        openSrs = intent.getBooleanExtra("openSrs", false),
                        startLessonSlug = if (pendingLessonStart.value) "lesson-1" else null,
                        onDeepStartConsumed = { pendingLessonStart.value = false },
                    )
                }
                }
            }
        }
    }
}
