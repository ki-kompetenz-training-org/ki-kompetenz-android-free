/*
 * Copyright 2026 Tobias Weiss
 * SPDX-License-Identifier: Apache-2.0
 */
package ai.ki_kompetenz_training_org.data.api

import ai.ki_kompetenz_training_org.data.repo.QuizScoring
import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Vertrag zwischen dem gebundenen Offline-Pool und dem Scoring:
 * [QuizScoring.tierFor] muss für JEDEN erreichbaren Score (0-100) ein
 * gültiges Tier aus [KiScoreFallback] liefern — sonst zeigt der Offline-KI-Score
 * bei einem Score ohne zugeordnetes Tier nichts an (schlimmster denkbaren Bug
 * genau im Sicherheitsnetz).
 */
class KiScoreFallbackScoringTest {

    private val tiers = KiScoreFallback.data.tiers

    // ── Totalität: kein Score ohne Tier ──────────────────────────────────

    @Test
    fun `tierFor liefert fuer JEDEN Score 0-100 ein Tier (Totalitaet)`() {
        for (score in 0..100) {
            val tier = QuizScoring.tierFor(score, tiers)
            assertThat(tier).isNotNull()
        }
    }

    @Test
    fun `tierFor liefert fuer negative und uebergrosse Scores trotzdem etwas oder null (kein Crash)`() {
        // -5 und 105 sind im Normalbetrieb unerreichbar; tierFor darf null
        // liefern, darf aber nicht abstuerzen.
        QuizScoring.tierFor(-5, tiers)
        QuizScoring.tierFor(105, tiers)
    }

    // ── Exakte Grenzzuordnung (0-20, 21-40, 41-60, 61-80, 81-100) ────────

    @Test
    fun `Scores an den Tiergrenzen landen im richtigen Tier`() {
        val t = { s: Int -> QuizScoring.tierFor(s, tiers) }

        assertThat(t(0)).isSameInstanceAs(tiers[0])
        assertThat(t(20)).isSameInstanceAs(tiers[0])
        assertThat(t(21)).isSameInstanceAs(tiers[1])
        assertThat(t(40)).isSameInstanceAs(tiers[1])
        assertThat(t(41)).isSameInstanceAs(tiers[2])
        assertThat(t(60)).isSameInstanceAs(tiers[2])
        assertThat(t(61)).isSameInstanceAs(tiers[3])
        assertThat(t(80)).isSameInstanceAs(tiers[3])
        assertThat(t(81)).isSameInstanceAs(tiers[4])
        assertThat(t(100)).isSameInstanceAs(tiers[4])
    }

    @Test
    fun `alle 5 Tiers werden von den Scores 0-100 erreicht (kein totes Tier)`() {
        val reached = (0..100).mapNotNull { QuizScoring.tierFor(it, tiers) }.toSet()
        assertThat(reached).hasSize(5)
    }

    // ── Brücke scoreFor → tierFor (Website-Paritaet) ─────────────────────

    @Test
    fun `scoreFor 10 von 10 = 100 Punkte → Top-Tier`() {
        val score = QuizScoring.scoreFor(List(10) { true })
        assertThat(score).isEqualTo(100)
        assertThat(QuizScoring.tierFor(score, tiers)).isSameInstanceAs(tiers[4])
    }

    @Test
    fun `scoreFor 7 von 10 = 70 Punkte → viertes Tier (61-80)`() {
        val answers = List(7) { true } + List(3) { false }
        val score = QuizScoring.scoreFor(answers)
        assertThat(score).isEqualTo(70)
        assertThat(QuizScoring.tierFor(score, tiers)).isSameInstanceAs(tiers[3])
    }

    @Test
    fun `scoreFor 5 von 10 = 50 Punkte → mittleres Tier (41-60)`() {
        val answers = List(5) { true } + List(5) { false }
        val score = QuizScoring.scoreFor(answers)
        assertThat(score).isEqualTo(50)
        assertThat(QuizScoring.tierFor(score, tiers)).isSameInstanceAs(tiers[2])
    }

    // ── Anzeige-Konsistenz der Tiers ─────────────────────────────────────

    @Test
    fun `Tier-Titel und Emojis sind eindeutig (Share-Text wird eindeutig)`() {
        assertThat(tiers.map { it.title }.distinct()).hasSize(5)
        assertThat(tiers.map { it.emoji }.distinct()).hasSize(5)
    }

    @Test
    fun `Tier-Bereiche sind aufsteigend sortiert (Fortschritt sichtbar)`() {
        tiers.zipWithNext { a, b ->
            assertThat(b.min).isGreaterThan(a.min)
            assertThat(b.max).isGreaterThan(a.max)
        }
    }
}
