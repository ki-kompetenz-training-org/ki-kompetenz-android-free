/*
 * Copyright 2026 Tobias Weiss
 * SPDX-License-Identifier: Apache-2.0
 */
package ai.ki_kompetenz_training_org.data.repo

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.time.LocalDate

/**
 * Unit-Tests für die pure Gamification-Regeln (XP-Kurve, Level,
 * Streak-Zählung, Check-in-XP).
 *
 * Die Freezes sind in [GamificationFreezeTest] abgedeckt (Repository-
 * Integration); hier geht es um die rechenlastigen Verträge der
 * [GamificationRules] selbst.
 */
class GamificationRulesTest {

    // ── XP-Kurve: 50 * n * (n-1) ─────────────────────────────────────────

    @Test
    fun `xpForLevel folgt der dokumentierten Kurve (L2=100, L3=300, L4=600, L5=1000)`() {
        assertThat(GamificationRules.xpForLevel(1)).isEqualTo(0)
        assertThat(GamificationRules.xpForLevel(2)).isEqualTo(100)
        assertThat(GamificationRules.xpForLevel(3)).isEqualTo(300)
        assertThat(GamificationRules.xpForLevel(4)).isEqualTo(600)
        assertThat(GamificationRules.xpForLevel(5)).isEqualTo(1000)
    }

    @Test
    fun `xpForLevel ist streng monoton steigend bis L20`() {
        var prev = GamificationRules.xpForLevel(1)
        for (level in 2..20) {
            val cur = GamificationRules.xpForLevel(level)
            assertThat(cur).isGreaterThan(prev)
            prev = cur
        }
    }

    // ── levelForXp ───────────────────────────────────────────────────────

    @Test
    fun `levelForXp - Grenzen genau an den Levelschwellen`() {
        assertThat(GamificationRules.levelForXp(0)).isEqualTo(1)
        assertThat(GamificationRules.levelForXp(99)).isEqualTo(1)
        assertThat(GamificationRules.levelForXp(100)).isEqualTo(2)
        assertThat(GamificationRules.levelForXp(299)).isEqualTo(2)
        assertThat(GamificationRules.levelForXp(300)).isEqualTo(3)
        assertThat(GamificationRules.levelForXp(1000)).isEqualTo(5)
    }

    @Test
    fun `levelForXp - ein XP unter der nächsten Schwelle bleibt im Level`() {
        // 50 * 8 * 7 = 2800 → L8 ab 2800; 2799 ist noch L7
        assertThat(GamificationRules.levelForXp(2799)).isEqualTo(7)
        assertThat(GamificationRules.levelForXp(2800)).isEqualTo(8)
    }

    // ── xpIntoLevel / xpNeededForNextLevel ───────────────────────────────

    @Test
    fun `xpIntoLevel - Fortschritt innerhalb des Levels`() {
        assertThat(GamificationRules.xpIntoLevel(0)).isEqualTo(0)
        assertThat(GamificationRules.xpIntoLevel(100)).isEqualTo(0)
        assertThat(GamificationRules.xpIntoLevel(250)).isEqualTo(150) // 250 - 100
    }

    @Test
    fun `xpIntoLevel plus Level-Schwelle ergibt Gesamt-XP (Invariante)`() {
        for (xp in listOf(0, 1, 99, 100, 250, 599, 600, 1500)) {
            val level = GamificationRules.levelForXp(xp)
            val reconstructed = GamificationRules.xpForLevel(level) +
                GamificationRules.xpIntoLevel(xp)
            assertThat(reconstructed).isEqualTo(xp)
        }
    }

    @Test
    fun `xpNeededForNextLevel - L1 braucht 100, L2 braucht 200, L4 braucht 400`() {
        assertThat(GamificationRules.xpNeededForNextLevel(0)).isEqualTo(100)
        assertThat(GamificationRules.xpNeededForNextLevel(100)).isEqualTo(200)
        assertThat(GamificationRules.xpNeededForNextLevel(600)).isEqualTo(400)
    }

    @Test
    fun `xpNeededForNextLevel ist konstant innerhalb eines Levels`() {
        // Beide XP-Werte liegen in L2 → jeweils 200 benötigt
        assertThat(GamificationRules.xpNeededForNextLevel(100))
            .isEqualTo(GamificationRules.xpNeededForNextLevel(299))
    }

    // ── nextStreak (Vertrag: yesterday→0 caller incrementiert, today→-1) ──

    private val today: LocalDate = LocalDate.of(2026, 9, 5)

    @Test
    fun `nextStreak - erster Check-in (null) startet bei 1`() {
        assertThat(GamificationRules.nextStreak(null, today)).isEqualTo(1)
    }

    @Test
    fun `nextStreak - gestern gecheckt liefert 0 (Caller incrementiert auf 1)`() {
        val yesterday = today.minusDays(1).toString()
        assertThat(GamificationRules.nextStreak(yesterday, today)).isEqualTo(0)
    }

    @Test
    fun `nextStreak - heute schon gecheckt liefert -1 (keine Aenderung)`() {
        assertThat(GamificationRules.nextStreak(today.toString(), today)).isEqualTo(-1)
    }

    @Test
    fun `nextStreak - Luecke ohne Freeze resettet auf 1`() {
        assertThat(GamificationRules.nextStreak("2020-01-01", today)).isEqualTo(1)
        assertThat(GamificationRules.nextStreak(today.minusDays(2).toString(), today)).isEqualTo(1)
    }

    // ── checkInXp: 5 × Tag, Cap 30 ───────────────────────────────────────

    @Test
    fun `checkInXp - 5 XP pro Tag bis zum Cap`() {
        assertThat(GamificationRules.checkInXp(1)).isEqualTo(5)
        assertThat(GamificationRules.checkInXp(5)).isEqualTo(25)
    }

    @Test
    fun `checkInXp - Cap bei 30 (Tag 6+)`() {
        assertThat(GamificationRules.checkInXp(6)).isEqualTo(30)
        assertThat(GamificationRules.checkInXp(10)).isEqualTo(30)
        assertThat(GamificationRules.checkInXp(100)).isEqualTo(30)
    }

    // ── Lesson-XP-Konstante ──────────────────────────────────────────────

    @Test
    fun `xpPerCompletedLesson ist positiv (Vertrag mit LessonDetail-UI)`() {
        assertThat(GamificationRules.xpPerCompletedLesson).isGreaterThan(0)
    }
}
