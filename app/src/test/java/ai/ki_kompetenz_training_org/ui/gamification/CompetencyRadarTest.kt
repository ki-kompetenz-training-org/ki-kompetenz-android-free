/*
 * Copyright 2026 Tobias Weiss
 * SPDX-License-Identifier: Apache-2.0
 */
package ai.ki_kompetenz_training_org.ui.gamification

import androidx.compose.ui.geometry.Offset
import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Unit-Tests der puren Radar-Mathematik ([parseDomainScores], [radarVertex])
 * — kein Robolectric, kein Compose-Test-Framework noetig: Offset ist pure Kotlin.
 */
class CompetencyRadarTest {

    // ── parseDomainScores ───────────────────────────────────────────────────

    @Test
    fun `null liefert Null-Scores in Domaenenlaenge`() {
        assertThat(parseDomainScores(null, 9)).isEqualTo(List(9) { 0 })
    }

    @Test
    fun `leerer String liefert Null-Scores`() {
        assertThat(parseDomainScores("", 9)).isEqualTo(List(9) { 0 })
        assertThat(parseDomainScores("   ", 9)).isEqualTo(List(9) { 0 })
    }

    @Test
    fun `nur Klammern liefert Null-Scores`() {
        assertThat(parseDomainScores("[]", 9)).isEqualTo(List(9) { 0 })
    }

    @Test
    fun `gueltiges JSON wird geparst und auf 9 Eintraege aufgefuellt`() {
        assertThat(parseDomainScores("[76, 12, 40]", 9))
            .isEqualTo(listOf(76, 12, 40, 0, 0, 0, 0, 0, 0))
    }

    @Test
    fun `korrupte Eintraege werden zu 0`() {
        assertThat(parseDomainScores("[a, b, 55]", 4))
            .isEqualTo(listOf(0, 0, 55, 0))
    }

    @Test
    fun `zu viele Eintraege werden abgeschnitten`() {
        assertThat(parseDomainScores("[1,2,3,4,5,6,7,8,9,10,11]", 9))
            .isEqualTo(listOf(1, 2, 3, 4, 5, 6, 7, 8, 9))
    }

    @Test
    fun `kein JSON-Format liefert Null-Scores`() {
        assertThat(parseDomainScores("murks", 9)).isEqualTo(List(9) { 0 })
        assertThat(parseDomainScores("{\"m\":1}", 3)).isEqualTo(List(3) { 0 })
    }

    // ── radarVertex ─────────────────────────────────────────────────────────

    private val center = Offset(100f, 100f)

    @Test
    fun `Score 0 liegt im Zentrum`() {
        val v = radarVertex(0, index = 0, axisCount = 9, center = center, radius = 50f)
        assertThat(v).isEqualTo(center)
    }

    @Test
    fun `Achse 0 mit Score 100 zeigt nach oben`() {
        val v = radarVertex(100, index = 0, axisCount = 9, center = center, radius = 50f)
        assertThat(v.x).isWithin(1e-3f).of(100f)
        assertThat(v.y).isWithin(1e-3f).of(50f)
    }

    @Test
    fun `4 Achsen - Index 1 zeigt nach rechts`() {
        val v = radarVertex(100, index = 1, axisCount = 4, center = center, radius = 40f)
        assertThat(v.x).isWithin(1e-3f).of(140f)
        assertThat(v.y).isWithin(1e-3f).of(100f)
    }

    @Test
    fun `Scores ausserhalb 0-100 werden geklemmt`() {
        val over = radarVertex(150, index = 0, axisCount = 9, center = center, radius = 50f)
        val at = radarVertex(100, index = 0, axisCount = 9, center = center, radius = 50f)
        assertThat(over).isEqualTo(at)
        val under = radarVertex(-5, index = 0, axisCount = 9, center = center, radius = 50f)
        assertThat(under).isEqualTo(center)
    }

    @Test
    fun `mittlerer Score liegt proportional zwischen Zentrum und Rand`() {
        val v = radarVertex(50, index = 0, axisCount = 9, center = center, radius = 50f)
        assertThat(v.y).isWithin(1e-3f).of(75f)
    }

    @Test
    fun `Schwellwert-Konstante ist 60`() {
        assertThat(RADAR_WEAK_THRESHOLD).isEqualTo(60)
    }
}
