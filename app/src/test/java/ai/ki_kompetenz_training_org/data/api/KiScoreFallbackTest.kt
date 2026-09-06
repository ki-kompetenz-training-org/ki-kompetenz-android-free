/*
 * Copyright 2026 Tobias Weiss
 * SPDX-License-Identifier: Apache-2.0
 */
package ai.ki_kompetenz_training_org.data.api

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Daten-Integrität des gebundenen KI-Score-Offline-Pools.
 *
 * Der Pool ist das Sicherheitsnetz für den BUG-Report 2026-09-05
 * ("Quiz konnte nicht geladen werden"): Wenn die API nicht erreichbar ist,
 * spielt der Nutzer gegen [KiScoreFallback]. Ein einziger defekter Datensatz
 * (falscher correct-Index, leere Erklärung) wäre direkt beim Nutzer sichtbar —
 * daher diese strukturellen Verträge.
 */
class KiScoreFallbackTest {

    private val data = KiScoreFallback.data

    // ── Umfang ───────────────────────────────────────────────────────────

    @Test
    fun `Pool reicht fuer mind 2 Runden ohne Wiederholung (ab 20 Fragen)`() {
        // Pro Runde werden 10 Fragen gezogen — der Pool muss deutlich
        // größer sein, damit aufsteigende Schwierigkeit (ids) erhalten bleibt.
        assertThat(data.questions.size).isAtLeast(20)
    }

    @Test
    fun `Pool hat die vollen 50 Fragen des API-Mirrors (Stand 2026-09-05)`() {
        assertThat(data.questions.size).isEqualTo(50)
    }

    @Test
    fun `Fragen-IDs sind eindeutig`() {
        assertThat(data.questions.map { it.id }.distinct()).hasSize(data.questions.size)
    }

    @Test
    fun `Fragen-Texte sind eindeutig (keine Duplikate im Pool)`() {
        assertThat(data.questions.map { it.text }.distinct()).hasSize(data.questions.size)
    }

    // ── Frage-Struktur ───────────────────────────────────────────────────

    @Test
    fun `jede Frage hat genau 4 Optionen`() {
        data.questions.forEach { q ->
            assertThat(q.options).hasSize(4)
        }
    }

    @Test
    fun `correct-Index liegt immer im Optionenbereich`() {
        data.questions.forEach { q ->
            assertThat(q.correct).isAtLeast(0)
            assertThat(q.correct).isLessThan(q.options.size)
        }
    }

    @Test
    fun `keine leeren Fragen, Optionen oder Erklaerungen`() {
        data.questions.forEach { q ->
            assertThat(q.text).isNotEmpty()
            q.options.forEach { opt -> assertThat(opt).isNotEmpty() }
            assertThat(q.explanation).isNotEmpty()
        }
    }

    @Test
    fun `Optionen sind pro Frage eindeutig`() {
        data.questions.forEach { q ->
            assertThat(q.options.distinct()).hasSize(q.options.size)
        }
    }

    @Test
    fun `correct-Antworten sind verteilt - nicht alle auf Index 0 (sonst erratbar)`() {
        val distribution = data.questions.groupBy { it.correct }.keys
        assertThat(distribution.size).isAtLeast(3)
    }

    // ── Tiers ────────────────────────────────────────────────────────────

    @Test
    fun `5 Tiers, sortiert und lueckenlos ueber 0-100`() {
        val tiers = data.tiers
        assertThat(tiers).hasSize(5)
        assertThat(tiers.first().min).isEqualTo(0)
        assertThat(tiers.last().max).isEqualTo(100)
        tiers.sortedBy { it.min }.zipWithNext { a, b ->
            assertThat(b.min).isEqualTo(a.max + 1) // keine Lücken, keine Überlappung
        }
    }

    @Test
    fun `jedes Tier hat Titel, Emoji und Beschreibung`() {
        data.tiers.forEach { t ->
            assertThat(t.title).isNotEmpty()
            assertThat(t.emoji).isNotEmpty()
            assertThat(t.description).isNotEmpty()
        }
    }

    // ── Share ────────────────────────────────────────────────────────────

    @Test
    fun `Share-Prefix enthaelt die Platzhalter score, tier und emoji`() {
        val prefix = data.share?.prefix.orEmpty()
        assertThat(prefix).contains("{score}")
        assertThat(prefix).contains("{tier}")
        assertThat(prefix).contains("{emoji}")
    }

    @Test
    fun `Share-Invite und Hashtags vorhanden`() {
        assertThat(data.share?.invite).isNotEmpty()
        assertThat(data.share?.hashtags).isNotEmpty()
    }
}
