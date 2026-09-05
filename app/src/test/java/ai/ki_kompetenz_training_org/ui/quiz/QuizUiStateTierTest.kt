/*
 * Copyright 2026 Tobias Weiss
 * SPDX-License-Identifier: Apache-2.0
 */
package ai.ki_kompetenz_training_org.ui.quiz

import ai.ki_kompetenz_training_org.data.api.KiScoreFallback
import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Unit-Tests für die Tier-Ableitung in [QuizUiState] gegen den gebundenen
 * Offline-Pool.
 *
 * Website-Parität (BUG-Fund 2026-09-05): Das Tier muss aus dem
 * ANTWORT-PROZENTSATZ (QuizScoring.scoreFor(answers)) berechnet werden —
 * nicht aus scorePoints. scorePoints enthält Combo-/Zeit-Bonuspunkte
 * ((100 + timeLeft*10) × Multiplikator) und übersteigt nach wenigen
 * richtigen Antworten die 100er-Tier-Skala → tierFor(scorePoints) liefert
 * null → Ergebnis-Screen ohne Tier, QuizResultEntity.tierTitle = "".
 */
class QuizUiStateTierTest {

    private val tiers = KiScoreFallback.data.tiers

    // ── Der Vertrag: Antworten-Prozentsatz, nicht Combo-Punkte ───────────

    @Test
    fun `0 von 10 richtig (0 Prozent) → unterstes Tier, egal wie hoch die Punkte sind`() {
        val s = QuizUiState(
            tiers = tiers,
            answers = List(10) { false },
            scorePoints = 0,
        )
        assertThat(s.tier).isSameInstanceAs(tiers[0])
    }

    @Test
    fun `5 von 10 richtig (50 Prozent) → mittleres Tier - AUCH bei Combo-Punkten über 100 (RED mit alter Logik)`() {
        val s = QuizUiState(
            tiers = tiers,
            answers = List(5) { true } + List(5) { false },
            scorePoints = 750, // Combo-/Zeit-Bonus: > 100er-Tier-Skala
        )
        assertThat(s.tier).isSameInstanceAs(tiers[2])
    }

    @Test
    fun `10 von 10 richtig (100 Prozent) → Top-Tier trotz gigantischer Combo-Punkte (RED mit alter Logik)`() {
        val s = QuizUiState(
            tiers = tiers,
            answers = List(10) { true },
            scorePoints = 5000,
        )
        assertThat(s.tier).isSameInstanceAs(tiers[4])
    }

    @Test
    fun `2 von 10 richtig (20 Prozent) → unterstes Tier (Grenze inklusive)`() {
        val s = QuizUiState(
            tiers = tiers,
            answers = List(2) { true } + List(8) { false },
            scorePoints = 300,
        )
        assertThat(s.tier).isSameInstanceAs(tiers[0])
    }

    @Test
    fun `3 von 10 richtig (30 Prozent) → zweites Tier (21-40)`() {
        val s = QuizUiState(
            tiers = tiers,
            answers = List(3) { true } + List(7) { false },
            scorePoints = 400,
        )
        assertThat(s.tier).isSameInstanceAs(tiers[1])
    }

    // ── Randfälle ────────────────────────────────────────────────────────

    @Test
    fun `leere Antworten (kein Spiel gespielt) → unterstes Tier (0 Prozent), kein Crash`() {
        val s = QuizUiState(tiers = tiers, answers = emptyList(), scorePoints = 0)
        assertThat(s.tier).isSameInstanceAs(tiers[0])
    }

    @Test
    fun `ohne Tiers (leere Liste) → tier null, kein Crash (Score wird trotzdem angezeigt)`() {
        val s = QuizUiState(tiers = emptyList(), answers = List(10) { true })
        assertThat(s.tier).isNull()
        assertThat(s.score).isEqualTo(s.scorePoints)
    }

    // ── Anzeige-Kompatibilität ───────────────────────────────────────────

    @Test
    fun `score bleibt scorePoints (Combo-Punkte) - nur die Tier-Ableitung aendert sich`() {
        val s = QuizUiState(
            tiers = tiers,
            answers = List(10) { true },
            scorePoints = 5000,
        )
        assertThat(s.score).isEqualTo(5000)
    }
}
