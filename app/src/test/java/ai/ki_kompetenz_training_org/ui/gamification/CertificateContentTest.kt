/*
 * Copyright 2026 Tobias Weiss
 * SPDX-License-Identifier: Apache-2.0
 */
package ai.ki_kompetenz_training_org.ui.gamification

import com.google.common.truth.Truth.assertThat
import org.junit.Assert.assertThrows
import org.junit.Test

/**
 * Unit-Tests der puren Zertifikat-Logik ([CertificateContent.buildLines],
 * [parsePerDomain]) — kein Robolectric (writePdf bleibt ungetestet).
 */
class CertificateContentTest {

    private val domains = List(9) { "Dom\u00e4ne $it" }
    private val perDomain = listOf(72, 60, 100, 0, 55, 48, 90, 33, 61)

    private fun lines(name: String = "Max Mustermann", locale: String = "de-DE") =
        CertificateContent.buildLines(
            kiki = 88,
            perDomain = perDomain,
            domains = domains,
            name = name,
            dateIso = "2026-03-14",
            localeTag = locale,
        )

    @Test
    fun `Titel, Name, Datum und KIKI-Zeile sind enthalten`() {
        val l = lines()
        assertThat(l).contains("KI-Kompetenz-Nachweis")
        assertThat(l).contains("Name: Max Mustermann")
        assertThat(l).contains("Datum: 2026-03-14")
        assertThat(l).contains("KI-Score (KIKI): 88/100")
    }

    @Test
    fun `9 Domaenezeilen mit Scores aus perDomain`() {
        val l = lines()
        assertThat(l).contains("Dom\u00e4ne 0: 72/100")
        assertThat(l).contains("Dom\u00e4ne 2: 100/100")
        assertThat(l).contains("Dom\u00e4ne 8: 61/100")
        val domainLines = l.filter { it.startsWith("Dom\u00e4ne ") }
        assertThat(domainLines).hasSize(9)
        assertThat(domainLines).containsExactly(
            *(domains.indices.map { "Dom\u00e4ne $it: ${perDomain[it]}/100" }).toTypedArray(),
        )
    }

    @Test
    fun `Fusszeile mit EU AI Act Art 4 und Link`() {
        val l = lines()
        assertThat(l).contains("Grundlage: EU AI Act Art. 4 - KI-Kompetenz")
        assertThat(l).contains("https://ki-kompetenz-training.org")
    }

    @Test
    fun `en-Locale nutzt englische Formulierungen`() {
        val l = lines(locale = "en-US")
        assertThat(l).contains("AI Literacy Certificate")
        assertThat(l).contains("Name: Max Mustermann")
        assertThat(l).contains("Date: 2026-03-14")
        assertThat(l).contains("AI Score (KIKI): 88/100")
        assertThat(l).contains("Based on EU AI Act Art. 4 - AI literacy")
    }

    @Test
    fun `leerer Name liefert Strich`() {
        assertThat(lines(name = "")).contains("Name: -")
    }

    @Test
    fun `Groessenungleichung wirft IllegalArgumentException`() {
        assertThrows(IllegalArgumentException::class.java) {
            CertificateContent.buildLines(50, listOf(1, 2, 3), domains, "X", "2026-01-01", "de")
        }
        assertThrows(IllegalArgumentException::class.java) {
            CertificateContent.buildLines(50, perDomain, domains.drop(1), "X", "2026-01-01", "de")
        }
    }

    @Test
    fun `parsePerDomain parst 9er JSON-Array`() {
        assertThat(parsePerDomain("[72, 60, 100, 0, 55, 48, 90, 33, 61]"))
            .isEqualTo(perDomain)
    }
}
