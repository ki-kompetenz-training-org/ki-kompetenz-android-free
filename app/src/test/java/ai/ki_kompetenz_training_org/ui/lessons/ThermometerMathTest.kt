/*
 * Copyright 2026 Tobias Weiss
 * SPDX-License-Identifier: Apache-2.0
 */
package ai.ki_kompetenz_training_org.ui.lessons

import com.google.common.truth.Truth.assertThat
import org.junit.Assert.assertThrows
import org.junit.Test

/**
 * BUG-2 (Gerätetest Pixel 8 Pro, 2026-09-01): Der Glow-Indikator des
 * AI-Act-Thermometers erscheint an falscher Position und verschiebt das
 * Layout. Ursache: `(selectedLevel * (1f/levels) * 260).dp` als Flow-Child
 * der Level-Column — liegt relativ zur Column-Unterseite, nicht zu den
 * Segmentzentren, und nimmt 56dp Layout-Space ein.
 *
 * Fix (TDD-RED): Positions-Mathematik als reine Funktion extrahieren,
 * die den RELATIVEN Mittelpunkt des gewählten Segments berechnet
 * (0f = Oberkante, 1f = Unterkante). Das Composable platziert den Glow
 * dann über BoxWithConstraints/frac der REAL gemessenen Höhe.
 */
class ThermometerMathTest {

    @Test
    fun `Glow-Fraktion Level 0 von 4 liegt in Segmentmitte`() {
        val fraction = ThermometerMath.glowCenterFraction(selectedLevel = 0, totalLevels = 4)
        assertThat(fraction).isWithin(1e-6f).of(0.125f) // (0 + 0.5) / 4
    }

    @Test
    fun `Glow-Fraktion Level 3 von 4 liegt in Segmentmitte`() {
        val fraction = ThermometerMath.glowCenterFraction(selectedLevel = 3, totalLevels = 4)
        assertThat(fraction).isWithin(1e-6f).of(0.875f) // (3 + 0.5) / 4
    }

    @Test
    fun `Glow-Fraktion ist für alle Level monoton steigend`() {
        val fractions = (0 until 4).map { ThermometerMath.glowCenterFraction(it, 4) }
        assertThat(fractions).isInOrder()
    }

    @Test
    fun `Glow-Fraktion bleibt in Grenzen 0 bis 1`() {
        (0 until 4).forEach { level ->
            val f = ThermometerMath.glowCenterFraction(level, 4)
            assertThat(f).isAtLeast(0f)
            assertThat(f).isAtMost(1f)
        }
    }

    @Test
    fun `ungültiger Level-Index wird abgelehnt`() {
        assertThrows(IllegalArgumentException::class.java) {
            ThermometerMath.glowCenterFraction(selectedLevel = 4, totalLevels = 4)
        }
        assertThrows(IllegalArgumentException::class.java) {
            ThermometerMath.glowCenterFraction(selectedLevel = -1, totalLevels = 4)
        }
        assertThrows(IllegalArgumentException::class.java) {
            ThermometerMath.glowCenterFraction(selectedLevel = 0, totalLevels = 0)
        }
    }
}
