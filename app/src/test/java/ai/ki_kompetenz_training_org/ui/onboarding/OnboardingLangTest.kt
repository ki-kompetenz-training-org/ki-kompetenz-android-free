package ai.ki_kompetenz_training_org.ui.onboarding

import org.junit.Assert.assertEquals
import org.junit.Test

class OnboardingLangTest {

    @Test
    fun `default selection is always english regardless of device locale`() {
        assertEquals("en", OnboardingLang.defaultLanguage("de-DE"))
        assertEquals("en", OnboardingLang.defaultLanguage("de"))
        assertEquals("en", OnboardingLang.defaultLanguage("en-US"))
        assertEquals("en", OnboardingLang.defaultLanguage("fr"))
        assertEquals("en", OnboardingLang.defaultLanguage("zh-CN"))
    }

    @Test
    fun `unknown or missing locale defaults to english`() {
        assertEquals("en", OnboardingLang.defaultLanguage(null))
        assertEquals("en", OnboardingLang.defaultLanguage(""))
    }
}
