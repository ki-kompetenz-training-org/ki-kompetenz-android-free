package ai.ki_kompetenz_training_org

import ai.ki_kompetenz_training_org.data.prefs.SettingsStore
import android.app.Application
import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.AndroidJUnitRunner
import kotlinx.coroutines.runBlocking

/**
 * Setzt den App-Zustand analog zum echten "Deutsch + Skip"-Onboarding-Pfad,
 * BEVOR die ersten Tests laufen: Sprache=DE (DataStore + SharedPreferences,
 * damit MainActivity die Ressourcen deutsch lädt) und onboarding-completed.
 *
 * Wichtig: AndroidJUnitRunner.callApplicationOnCreate() wird von der
 * Instrumentation aufgerufen, entsprechenden dem DAS App-onCreate,
 * also der perfekte Zeitpunkt für den Preseed.
 */
class OnboardingResettingRunner : AndroidJUnitRunner() {

    override fun callApplicationOnCreate(app: Application) {
        super.callApplicationOnCreate(app)
        preSeed()
    }

    private fun preSeed() {
        val instr = InstrumentationRegistry.getInstrumentation()
        val ctx = instr.targetContext
        val kiApp = ctx.applicationContext as? KiKompetenzApp
        if (kiApp != null) {
            runBlocking {
                kiApp.settingsStore.markOnboardingCompleted()
                kiApp.settingsStore.setLanguage(SettingsStore.LANG_DE)
            }
            ctx.getSharedPreferences(
                "kikompetenz_settings", Context.MODE_PRIVATE,
            ).edit().putString("language", SettingsStore.LANG_DE).apply()
        }
    }
}
