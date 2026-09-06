/*
 * Copyright 2026 Tobias Weiss
 * SPDX-License-Identifier: Apache-2.0
 */
package ai.ki_kompetenz_training_org.ui.quiz

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Unit-Tests für [QuizConstants] — die Punkte-Formel, die laut Doku die
 * Website (components/KiScoreGame.tsx) spiegelt.
 *
 * Diese Werte bestimmen, was der Nutzer im KI-Score sieht — eine Formel-
 * Änderung ohne Bewusstsein verschiebt alle Scores und Tiers.
 */
class QuizConstantsTest {

    // ── Konstanten-Vertrag ───────────────────────────────────────────────

    @Test
    fun `Runde dauert 20 Sekunden (Website-Paritaet)`() {
        assertThat(QuizConstants.ROUND_SECONDS).isEqualTo(20)
    }

    @Test
    fun `3 Leben (Herzen)`() {
        assertThat(QuizConstants.MAX_LIVES).isEqualTo(3)
    }

    @Test
    fun `Timer tickt sekündlich`() {
        assertThat(QuizConstants.TIMER_TICK_MS).isEqualTo(1000L)
    }

    // ── comboMultiplier-Stufen ───────────────────────────────────────────

    @Test
    fun `comboMultiplier - keine Streak oder 1 richtig: 1x`() {
        assertThat(QuizConstants.comboMultiplier(0)).isEqualTo(1.0)
        assertThat(QuizConstants.comboMultiplier(1)).isEqualTo(1.0)
    }

    @Test
    fun `comboMultiplier - 2-3 richtige: 1_5x (Grenzen)`() {
        assertThat(QuizConstants.comboMultiplier(2)).isEqualTo(1.5)
        assertThat(QuizConstants.comboMultiplier(3)).isEqualTo(1.5)
    }

    @Test
    fun `comboMultiplier - ab 4 richtigen: 2x (Obergrenze)`() {
        assertThat(QuizConstants.comboMultiplier(4)).isEqualTo(2.0)
        assertThat(QuizConstants.comboMultiplier(9)).isEqualTo(2.0)
    }

    // ── pointsForCorrectAnswer: (100 + timeLeft * 10) * multiplier ───────

    @Test
    fun `Punkte bei voller Zeit ohne Combo: 300`() {
        assertThat(QuizConstants.pointsForCorrectAnswer(20, 0)).isEqualTo(300)
    }

    @Test
    fun `Punkte bei abgelaufener Zeit: Basis 100`() {
        assertThat(QuizConstants.pointsForCorrectAnswer(0, 0)).isEqualTo(100)
    }

    @Test
    fun `Punkte steigen mit verbleibender Zeit (Geschwindigkeit belohnt)`() {
        val fast = QuizConstants.pointsForCorrectAnswer(20, 0)
        val slow = QuizConstants.pointsForCorrectAnswer(5, 0)
        assertThat(fast).isGreaterThan(slow)
    }

    @Test
    fun `Punkte steigen mit Combo (Streak belohnt) - gleiche Zeit`() {
        val noCombo = QuizConstants.pointsForCorrectAnswer(20, 1)
        val midCombo = QuizConstants.pointsForCorrectAnswer(20, 3)
        val maxCombo = QuizConstants.pointsForCorrectAnswer(20, 5)
        assertThat(midCombo).isGreaterThan(noCombo)
        assertThat(maxCombo).isGreaterThan(midCombo)
    }

    @Test
    fun `Punkte sind immer positiv (auch bei timeLeft 0 und ohne Combo)`() {
        assertThat(QuizConstants.pointsForCorrectAnswer(0, 0)).isGreaterThan(0)
    }

    // ── MAX_SCORE ────────────────────────────────────────────────────────

    @Test
    fun `MAX_SCORE = 10 perfekte Antworten mit Max-Combo und Max-Zeit (6000)`() {
        // (100 + 20*10) * 2.0 = 600 pro Frage, × 10 Fragen
        assertThat(QuizConstants.MAX_SCORE).isEqualTo(6000)
        assertThat(QuizConstants.MAX_SCORE).isEqualTo(
            QuizConstants.pointsForCorrectAnswer(QuizConstants.ROUND_SECONDS, 4) * 10
        )
    }
}
