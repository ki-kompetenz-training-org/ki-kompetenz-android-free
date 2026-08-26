package ai.ki_kompetenz_training_org.data.repo

import org.junit.Assert.*
import org.junit.Test

class BadgesTest {

    @Test
    fun `all badges returns 10 badges`() {
        assertEquals(10, Badges.all().size)
    }

    @Test
    fun `all badges have unique IDs`() {
        val ids = Badges.all().map { it.id }
        assertEquals(ids.size, ids.toSet().size)
    }

    @Test
    fun `all badges have non-empty emoji`() {
        Badges.all().forEach { badge ->
            assertTrue("Badge ${badge.id} has empty emoji", badge.emoji.isNotBlank())
        }
    }

    @Test
    fun `all badges have non-empty title`() {
        Badges.all().forEach { badge ->
            assertTrue("Badge ${badge.id} has empty title", badge.title.isNotBlank())
        }
    }

    @Test
    fun `all badges have non-empty description`() {
        Badges.all().forEach { badge ->
            assertTrue("Badge ${badge.id} has empty description", badge.description.isNotBlank())
        }
    }

    @Test
    fun `badges in German have German titles`() {
        val badges = Badges.all("de")
        assertEquals("Erster Schritt", badges[0].title)
        assertEquals("Perfektion", badges[1].title)
    }

    @Test
    fun `badges in English have English titles`() {
        val badges = Badges.all("en")
        assertEquals("First Steps", badges[0].title)
        assertEquals("Perfection", badges[1].title)
    }

    @Test
    fun `badges in French have French titles`() {
        val badges = Badges.all("fr")
        assertEquals("Premier pas", badges[0].title)
    }

    @Test
    fun `badges in Chinese have Chinese titles`() {
        val badges = Badges.all("zh")
        assertEquals("第一步", badges[0].title)
    }

    @Test
    fun `badges fallback to English for unknown locale`() {
        val badges = Badges.all("xx")
        assertEquals("First Steps", badges[0].title)
    }

    @Test
    fun `badge IDs match expected set`() {
        val ids = Badges.all().map { it.id }
        assertTrue("first_score" in ids)
        assertTrue("perfect_score" in ids)
        assertTrue("streak_3" in ids)
        assertTrue("streak_7" in ids)
        assertTrue("lesson_first" in ids)
        assertTrue("lesson_all" in ids)
        assertTrue("team_player" in ids)
        assertTrue("mini_game" in ids)
        assertTrue("mini_game_all" in ids)
        assertTrue("visionary" in ids)
    }

    @Test
    fun `badge emojis are all non-empty and unique`() {
        val emojis = Badges.all().map { it.emoji }
        assertEquals("All 10 emojis should be unique", emojis.size, emojis.toSet().size)
        emojis.forEach { e ->
            assertTrue("Emoji should be non-empty", e.isNotBlank())
        }
    }

    @Test
    fun `badge descriptions are non-empty in all locales`() {
        listOf("de", "en", "fr", "zh").forEach { locale ->
            Badges.all(locale).forEach { badge ->
                assertTrue("Badge ${badge.id} desc empty in $locale", badge.description.isNotBlank())
            }
        }
    }

    @Test
    fun `streak_3 badge has correct German description`() {
        val badges = Badges.all("de")
        val streak3 = badges.find { it.id == "streak_3" }
        assertNotNull(streak3)
        assertTrue(streak3!!.description.contains("3 Tage"))
    }

    @Test
    fun `streak_7 badge has correct English description`() {
        val badges = Badges.all("en")
        val streak7 = badges.find { it.id == "streak_7" }
        assertNotNull(streak7)
        assertTrue(streak7!!.description.contains("7 days"))
    }

    @Test
    fun `visionary badge has correct German description`() {
        val badges = Badges.all("de")
        val visionary = badges.find { it.id == "visionary" }
        assertNotNull(visionary)
        assertTrue(visionary!!.description.contains("81"))
    }
}
