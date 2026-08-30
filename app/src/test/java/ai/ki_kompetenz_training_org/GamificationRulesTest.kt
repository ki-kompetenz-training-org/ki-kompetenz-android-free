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
            assertEquals("Badge-Anzahl für $locale", 11, badges.size)
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

    // ── Streak freeze ───────────────────────────────────────────────────────
    @Test
    fun `iso week key is stable and locale independent`() {
        assertEquals("2026-W35", GamificationRules.isoWeekKey(LocalDate.of(2026, 8, 30)))
        assertEquals("2026-W35", GamificationRules.isoWeekKey(LocalDate.of(2026, 8, 24)))
        // Year boundary: 2027-01-01 (Friday) still belongs to ISO week 53 of 2026.
        assertEquals("2026-W53", GamificationRules.isoWeekKey(LocalDate.of(2027, 1, 1)))
        // 2027-01-04 (Monday) starts ISO week 1 of 2027.
        assertEquals("2027-W01", GamificationRules.isoWeekKey(LocalDate.of(2027, 1, 4)))
    }

    @Test
    fun `weekly freeze granted once per week`() {
        val monday = LocalDate.of(2026, 8, 24)
        val sunday = LocalDate.of(2026, 8, 30)
        assertTrue(GamificationRules.shouldGrantWeeklyFreeze(null, monday))
        assertTrue(GamificationRules.shouldGrantWeeklyFreeze("2026-W34", monday))
        assertFalse(GamificationRules.shouldGrantWeeklyFreeze("2026-W35", monday))
        assertFalse(GamificationRules.shouldGrantWeeklyFreeze("2026-W35", sunday))
        assertTrue(GamificationRules.shouldGrantWeeklyFreeze("2026-W35", LocalDate.of(2026, 8, 31)))
    }

    @Test
    fun `streak outcome bridges exactly one missed day with freeze`() {
        // yesterday → continue (freeze kept)
        assertEquals(GamificationRules.StreakOutcome.CONTINUE, GamificationRules.streakOutcome(1, 0))
        assertEquals(GamificationRules.StreakOutcome.CONTINUE, GamificationRules.streakOutcome(1, 2))
        // exactly one missed day + freeze available → consume freeze, continue
        assertEquals(GamificationRules.StreakOutcome.CONSUME_FREEZE, GamificationRules.streakOutcome(2, 1))
        assertEquals(GamificationRules.StreakOutcome.CONSUME_FREEZE, GamificationRules.streakOutcome(2, 2))
        // exactly one missed day + no freeze → reset
        assertEquals(GamificationRules.StreakOutcome.RESET, GamificationRules.streakOutcome(2, 0))
        // two or more missed days → reset even with freezes
        assertEquals(GamificationRules.StreakOutcome.RESET, GamificationRules.streakOutcome(3, 2))
        assertEquals(GamificationRules.StreakOutcome.RESET, GamificationRules.streakOutcome(5, 1))
        // same day / first check-in handled by caller
        assertEquals(GamificationRules.StreakOutcome.CONTINUE, GamificationRules.streakOutcome(0, 0))
    }

    @Test
    fun `freeze purchase gated by xp and cap`() {
        assertEquals(100, GamificationRules.freezePriceXp)
        assertEquals(2, GamificationRules.maxFreezes)
        assertTrue(GamificationRules.canPurchaseFreeze(0, 100))
        assertTrue(GamificationRules.canPurchaseFreeze(1, 100))
        assertFalse(GamificationRules.canPurchaseFreeze(2, 500))
        assertFalse(GamificationRules.canPurchaseFreeze(0, 99))
        assertFalse(GamificationRules.canPurchaseFreeze(3, 1000))
    }
}