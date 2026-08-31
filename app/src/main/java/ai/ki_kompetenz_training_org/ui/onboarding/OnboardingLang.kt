package ai.ki_kompetenz_training_org.ui.onboarding

import ai.ki_kompetenz_training_org.data.prefs.SettingsStore

/** Pure helpers for the onboarding language step. */
object OnboardingLang {
    /**
     * Default selection for the language step: always English; German is
     * explicitly selectable (product decision, 2026-08).
     */
    fun defaultLanguage(systemLanguage: String?): String = SettingsStore.LANG_EN
}
