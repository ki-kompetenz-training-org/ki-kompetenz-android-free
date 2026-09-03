/*
 * Copyright 2026 Tobias Weiss
 * SPDX-License-Identifier: Apache-2.0
 */
package ai.ki_kompetenz_training_org.ui.lessons

import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import org.junit.Test

/**
 * Deep-Verifikation der EuAiActRiskLevels-Metadaten. Ergänzt
 * EuAiActRiskLevelsTest (Listen-Level) ohne es zu duplizieren — hier geht es
 * um die Qualität der einzelnen Einträge:
 *
 * - Beispiel-Qualität: jeder einzelne Beispiel-String (DE & EN) ist gefüllt,
 *   jede Stufe liefert mindestens 2 Beispiele, keine Duplikate pro Stufe.
 * - Farb-Qualität: Thermometer-Segmente müssen voll deckend sein
 *   (Color.alpha == 1.0f) — halbtransparente Segmentfarben brechen die Optik.
 * - Bilinguale Konsistenz: DE- und EN-Beispiellisten sind parallel lang.
 * - Inhaltliche Tiefe: Beschreibungen sind nicht-trivial, und die High-Risk-
 *   Pflichten nennen menschliche Aufsicht (Kernpflicht des EU AI Act, Art. 14).
 */
class EuAiActRiskLevelsDeepTest {

    @Test
    fun `jeder einzelne DE-Beispiel-String jeder Stufe ist nicht leer`() {
        EuAiActRiskLevels.levels.forEachIndexed { levelIndex, level ->
            assertThat(level.examplesDe).isNotEmpty()
            level.examplesDe.forEachIndexed { exampleIndex, example ->
                assertWithMessage(
                    "DE-Beispiel Nr. ${exampleIndex + 1} von Stufe $levelIndex (${level.labelDe})",
                ).that(example).isNotEmpty()
            }
        }
    }

    @Test
    fun `jeder einzelne EN-Beispiel-String jeder Stufe ist nicht leer`() {
        EuAiActRiskLevels.levels.forEachIndexed { levelIndex, level ->
            assertThat(level.examplesEn).isNotEmpty()
            level.examplesEn.forEachIndexed { exampleIndex, example ->
                assertWithMessage(
                    "EN-Beispiel Nr. ${exampleIndex + 1} von Stufe $levelIndex (${level.labelEn})",
                ).that(example).isNotEmpty()
            }
        }
    }

    @Test
    fun `jede Stufe hat mindestens 2 Beispiele in DE und EN`() {
        EuAiActRiskLevels.levels.forEach { level ->
            assertWithMessage("DE-Beispielliste von ${level.labelDe}")
                .that(level.examplesDe.size).isAtLeast(2)
            assertWithMessage("EN-Beispielliste von ${level.labelEn}")
                .that(level.examplesEn.size).isAtLeast(2)
        }
    }

    @Test
    fun `alle Stufenfarben sind voll deckend mit Alpha 1`() {
        EuAiActRiskLevels.levels.forEach { level ->
            assertWithMessage("Farbe von ${level.labelDe} (${level.color}) muss voll deckend sein")
                .that(level.color.alpha).isEqualTo(1.0f)
        }
    }

    @Test
    fun `keine doppelten DE-Beispiele innerhalb einer Stufe`() {
        EuAiActRiskLevels.levels.forEach { level ->
            assertWithMessage("DE-Beispiele von ${level.labelDe} dürfen keine Duplikate enthalten")
                .that(level.examplesDe).containsNoDuplicates()
        }
    }

    @Test
    fun `DE- und EN-Beispiellisten sind pro Stufe gleich lang`() {
        EuAiActRiskLevels.levels.forEach { level ->
            assertWithMessage("Bilinguale Konsistenz von ${level.labelDe}: gleich viele DE- und EN-Beispiele")
                .that(level.examplesDe).hasSize(level.examplesEn.size)
        }
    }

    @Test
    fun `Beschreibungen sind in beiden Sprachen nicht-trivial mit mindestens 20 Zeichen`() {
        EuAiActRiskLevels.levels.forEach { level ->
            assertWithMessage("descriptionDe von ${level.labelDe} ist zu knapp für eine sinnvolle Erklärung")
                .that(level.descriptionDe.length).isAtLeast(20)
            assertWithMessage("descriptionEn von ${level.labelEn} ist zu knapp für eine sinnvolle Erklärung")
                .that(level.descriptionEn.length).isAtLeast(20)
        }
    }

    @Test
    fun `High-Risk-Pflichten nennen menschliche Aufsicht in DE und EN`() {
        val high = EuAiActRiskLevels.levels[2]
        // Precondition: Index 2 ist tatsächlich die High-Risk-Stufe (Stamm ohne Endung).
        assertWithMessage("Index 2 muss die High-Risk-Stufe sein")
            .that(high.labelDe).contains("Hoh")

        val mentionsOversightDe = "Mensch" in high.obligationsDe || "menschliche" in high.obligationsDe
        assertWithMessage("DE-Pflichten der High-Risk-Stufe müssen menschliche Aufsicht nennen")
            .that(mentionsOversightDe).isTrue()

        val mentionsOversightEn = "human" in high.obligationsEn || "oversight" in high.obligationsEn
        assertWithMessage("EN-Pflichten der High-Risk-Stufe müssen menschliche Aufsicht nennen")
            .that(mentionsOversightEn).isTrue()
    }
}
