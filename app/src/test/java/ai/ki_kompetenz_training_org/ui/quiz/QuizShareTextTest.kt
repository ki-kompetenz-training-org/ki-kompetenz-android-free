/*
 * Copyright 2026 Tobias Weiss
 * SPDX-License-Identifier: Apache-2.0
 */
package ai.ki_kompetenz_training_org.ui.quiz

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Unit-Tests für [QuizShareText] — der reine Textteiler des KI-Score-Ergebnisses.
 *
 * Reine JVM-Tests (keine Android-Klassen): die Formatierung muss exakt die
 * bestehende Ausgabe reproduzieren (Prefix mit gefüllten Platzhaltern,
 * Leerzeile, Link).
 */
class QuizShareTextTest {

    private val link = "https://ki-kompetenz-training.org/ki-score"

    @Test
    fun `score interpoliert den Platzhalter {score}`() {
        assertThat(QuizShareText.build(87, null, null, "Mein Score: {score}", link))
            .isEqualTo("Mein Score: 87\n\n$link")
    }

    @Test
    fun `emoji und tier ersetzen ihre Platzhalter`() {
        assertThat(QuizShareText.build(87, "🏆", "KI-Experte", "{emoji} {tier} - {score}", link))
            .isEqualTo("🏆 KI-Experte - 87\n\n$link")
    }

    @Test
    fun `leerer Prefix ergibt nur den Link ohne fuehrenden Separator`() {
        assertThat(QuizShareText.build(87, "🏆", "KI-Experte", "", link))
            .isEqualTo(link)
    }

    @Test
    fun `Link ist immer die letzte Zeile`() {
        assertThat(QuizShareText.build(90, null, null, "Text", link))
            .isEqualTo("Text\n\n$link")
    }

    @Test
    fun `null emoji und null tier werden leer ersetzt`() {
        assertThat(QuizShareText.build(87, null, null, "S:{score}|E:{emoji}|T:{tier}", link))
            .isEqualTo("S:87|E:|T:\n\n$link")
    }
}
