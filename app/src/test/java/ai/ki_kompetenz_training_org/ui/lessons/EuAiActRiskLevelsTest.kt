/*
 * Copyright 2026 Tobias Weiss
 * SPDX-License-Identifier: Apache-2.0
 */
package ai.ki_kompetenz_training_org.ui.lessons

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Reproduzierte Geräte-Bugs (Pixel 8 Pro, 2026-09-01) als Unit-Tests:
 *
 * BUG-1 (Thermometer-Daten): Der AI-Act-Thermometer zeigt Inhalte ohne
 *           Locale-Unterscheidung — englische Nutzer sehen deutsche Strings
 *           ("Beispiele:" statt "Examples:").
 * BUG-2 (Thermometer-Glow): Der Selection-Glow-Indikator verwendet falsche
 *           Offset-Mathematik `(level * 1/levels * 260).dp` relativ zur
 *           Column-Unterseite — der Glow erscheint an falscher Position und
 *           verschiebt das Layout (der 56dp-Box nimmt Flow-Space ein).
 *
 * Diese Tests fixieren das KORREKTE Verhalten (TDD-RED vor dem Fix).
 */
class EuAiActRiskLevelsTest {

    @Test
    fun `hat genau 4 Risikostufen`() {
        assertThat(EuAiActRiskLevels.levels).hasSize(4)
    }

    @Test
    fun `Reihenfolge minimal bis unannehmbar`() {
        val labels = EuAiActRiskLevels.levels.map { it.labelDe }
        assertThat(labels[0]).contains("Minimal")
        assertThat(labels[1]).contains("Gering")
        assertThat(labels[2]).contains("Hoh")
        assertThat(labels[3]).contains("Unannehmbar")
    }

    @Test
    fun `Farben sind eindeutig`() {
        val colors = EuAiActRiskLevels.levels.map { it.color }
        assertThat(colors.distinct()).hasSize(4)
    }

    @Test
    fun `Emojis sind eindeutig und nicht leer`() {
        val emojis = EuAiActRiskLevels.levels.map { it.emoji }
        assertThat(emojis.distinct()).hasSize(4)
        emojis.forEach { assertThat(it).isNotEmpty() }
    }

    @Test
    fun `jede Stufe hat DE- und EN-Label und sie unterscheiden sich`() {
        EuAiActRiskLevels.levels.forEach { level ->
            assertThat(level.labelDe).isNotEmpty()
            assertThat(level.labelEn).isNotEmpty()
            // EN-Label darf nicht das DE-Label sein (locale switch muss sichtbar wirken)
            assertThat(level.labelEn).isNotEqualTo(level.labelDe)
        }
    }

    @Test
    fun `jede Stufe hat Beschreibungen in beiden Sprachen`() {
        EuAiActRiskLevels.levels.forEach { level ->
            assertThat(level.descriptionDe).isNotEmpty()
            assertThat(level.descriptionEn).isNotEmpty()
        }
    }

    @Test
    fun `jede Stufe hat Beispiele in beiden Sprachen`() {
        EuAiActRiskLevels.levels.forEach { level ->
            assertThat(level.examplesDe).isNotEmpty()
            assertThat(level.examplesEn).isNotEmpty()
        }
    }

    @Test
    fun `jede Stufe hat Pflichten in beiden Sprachen`() {
        EuAiActRiskLevels.levels.forEach { level ->
            assertThat(level.obligationsDe).isNotEmpty()
            assertThat(level.obligationsEn).isNotEmpty()
        }
    }

    @Test
    fun `verbotene Stufe nennt Bußgeldhöhe`() {
        val banned = EuAiActRiskLevels.levels[3]
        assertThat(banned.obligationsDe).contains("35")
        assertThat(banned.obligationsEn).contains("35")
    }
}
