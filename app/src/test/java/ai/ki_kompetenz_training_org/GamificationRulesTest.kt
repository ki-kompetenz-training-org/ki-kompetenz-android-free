package ai.ki_kompetenz_training_org

import ai.ki_kompetenz_training_org.data.repo.Badges
import ai.ki_kompetenz_training_org.data.repo.GamificationRules
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class GamificationRulesTest {

    // ── Level curve: 50 * n * (n-1) ─────────────────────────────────────────
    @Test
    fun `xp needed to reach level`() {
        assertEquals(0, GamificationRules.xpForLevel(1))
        assertEquals(100, GamificationRules.xpForLevel(2))
        assertEquals(300, GamificationRules.xpForLevel(3))
        assertEquals(600, GamificationRules.xpForLevel(4))
        assertEquals(1000, GamificationRules.xpForLevel(5))
    }

    @Test
    fun `level derived from xp`() {
        assertEquals(1, GamificationRules.levelForXp(0))
        assertEquals(1, GamificationRules.levelForXp(99))
        assertEquals(2, GamificationRules.levelForXp(100))
        assertEquals(2, GamificationRules.levelForXp(299))
        assertEquals(3, GamificationRules.levelForXp(300))
        assertEquals(5, GamificationRules.levelForXp(1000))
        assertEquals(6, GamificationRules.levelForXp(1500))
    }

    @Test
    fun `xp into level and needed`() {
        // 250 XP → Level 3 (braucht 300), 250-300 = -50?? → Level 2 (100-299): 150/200
        assertEquals(2, GamificationRules.levelForXp(250))
        assertEquals(150, GamificationRules.xpIntoLevel(250))
        assertEquals(200, GamificationRules.xpNeededForNextLevel(250))
        // exact boundary: 300 XP → Level 3, 0 into level, 300 needed
        assertEquals(3, GamificationRules.levelForXp(300))
        assertEquals(0, GamificationRules.xpIntoLevel(300))
        assertEquals(300, GamificationRules.xpNeededForNextLevel(300))
    }

    // ── Streak logic ────────────────────────────────────────────────────────
    @Test
    fun `streak starts at 1 for first check-in`() {
        assertEquals(1, GamificationRules.nextStreak(null))
    }

    @Test
    fun `streak continues when yesterday`() {
        val yesterday = LocalDate.now().minusDays(1).format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE)
        // returns 0 → caller increments (checked in today)
        assertEquals(0, GamificationRules.nextStreak(yesterday))
    }

    @Test
    fun `streak resets when gap`() {
        val threeDaysAgo = LocalDate.now().minusDays(3).format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE)
        assertEquals(1, GamificationRules.nextStreak(threeDaysAgo))
    }

    @Test
    fun `already checked in today returns -1`() {
        val today = LocalDate.now().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE)
        assertEquals(-1, GamificationRules.nextStreak(today))
    }

    @Test
    fun `check-in xp grows and caps at 30`() {
        assertEquals(5, GamificationRules.checkInXp(1))
        assertEquals(10, GamificationRules.checkInXp(2))
        assertEquals(25, GamificationRules.checkInXp(5))
        assertEquals(30, GamificationRules.checkInXp(6))
        assertEquals(30, GamificationRules.checkInXp(20))
    }

    // ── Quiz XP ─────────────────────────────────────────────────────────────
    @Test
    fun `quiz xp per correct answer plus perfect bonus`() {
        assertEquals(0, GamificationRules.quizXp(0, 10))
        assertEquals(70, GamificationRules.quizXp(7, 10))
        // perfect: 10*10 + 50 = 150
        assertEquals(150, GamificationRules.quizXp(10, 10))
    }

    // ── Badge catalog ───────────────────────────────────────────────────────
    @Test
    fun `badge catalog has all expected badges`() {
        val ids = Badges.all().map { it.id }
        assertTrue(ids.containsAll(
            listOf("first_score", "perfect_score", "streak_3", "streak_7", "lesson_first",
                "lesson_all", "team_player", "mini_game", "mini_game_all", "visionary")
        ))
        // unique ids
        assertEquals(ids.size, ids.distinct().size)

        // Alle 4 Sprachen haben vollständige Übersetzungen (kein Text fehlt)
        for (locale in listOf("de", "en", "fr", "zh")) {
            val badges = Badges.all(locale)
            assertEquals("Badge-Anzahl für $locale", 10, badges.size)
            badges.forEach { b ->
                assertFalse("${b.id} ($locale): title leer", b.title.isBlank())
                assertFalse("${b.id} ($locale): description leer", b.description.isBlank())
                // Kein deutscher Resttext in en/fr/zh (einfacher Plausibilitätscheck)
                if (locale != "de") {
                    assertFalse("${b.id} ($locale): deutscher Text '$'", b.title.contains("schritt", ignoreCase = true))
                }
            }
        }
        // every badge has emoji + title
        Badges.all().forEach { b ->
            assertFalse("${b.id} emoji", b.emoji.isBlank())
            assertFalse("${b.id} title", b.title.isBlank())
            assertFalse("${b.id} desc", b.description.isBlank())
        }
    }
}