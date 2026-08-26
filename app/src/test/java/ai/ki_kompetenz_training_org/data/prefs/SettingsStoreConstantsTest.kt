package ai.ki_kompetenz_training_org.data.prefs

import org.junit.Assert.*
import org.junit.Test

class SettingsStoreConstantsTest {

    @Test
    fun lang_system_constant_is_system() {
        assertEquals("system", SettingsStore.LANG_SYSTEM)
    }

    @Test
    fun lang_de_constant_is_de() {
        assertEquals("de", SettingsStore.LANG_DE)
    }

    @Test
    fun lang_en_constant_is_en() {
        assertEquals("en", SettingsStore.LANG_EN)
    }

    @Test
    fun lang_constants_are_distinct() {
        val langs = setOf(SettingsStore.LANG_SYSTEM, SettingsStore.LANG_DE, SettingsStore.LANG_EN)
        assertEquals(3, langs.size)
    }

    @Test
    fun lang_system_is_not_a_real_locale() {
        // "system" means follow device default, it should not be "de" or "en"
        assertNotEquals(SettingsStore.LANG_DE, SettingsStore.LANG_SYSTEM)
        assertNotEquals(SettingsStore.LANG_EN, SettingsStore.LANG_SYSTEM)
    }
}
