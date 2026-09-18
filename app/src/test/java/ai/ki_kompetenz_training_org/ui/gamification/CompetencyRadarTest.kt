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

    // ── radarDisplayScores ─────────────────────────────────────────────────

    @Test
    fun `Display-Floor hebt Null-Scores an`() {
        assertThat(radarDisplayScores(listOf(0, 50, 100)))
            .isEqualTo(listOf(RADAR_MIN_DISPLAY, 50, 100))
    }

    @Test
    fun `Werte ueber dem Floor bleiben unveraendert`() {
        assertThat(radarDisplayScores(listOf(6, 7, 99)))
            .isEqualTo(listOf(6, 7, 99))
    }

    @Test
    fun `leere Liste bleibt leer`() {
        assertThat(radarDisplayScores(emptyList())).isEmpty()
    }

    // ── kikiTier ────────────────────────────────────────────────────────

    @Test
    fun `Tier-Grenzen spiegeln KiScoreFallback-Tiers`() {
        assertThat(kikiTier(0).second).isEqualTo("KI-Laie")
        assertThat(kikiTier(20).second).isEqualTo("KI-Laie")
        assertThat(kikiTier(21).second).isEqualTo("KI-Entdecker")
        assertThat(kikiTier(40).second).isEqualTo("KI-Entdecker")
        assertThat(kikiTier(41).second).isEqualTo("KI-Praktiker")
        assertThat(kikiTier(60).second).isEqualTo("KI-Praktiker")
        assertThat(kikiTier(61).second).isEqualTo("KI-Profi")
        assertThat(kikiTier(80).second).isEqualTo("KI-Profi")
        assertThat(kikiTier(81).second).isEqualTo("KI-Visionär")
        assertThat(kikiTier(100).second).isEqualTo("KI-Visionär")
    }

    @Test
    fun `Tier ausserhalb 0-100 wird geklemmt`() {
        assertThat(kikiTier(-5).second).isEqualTo("KI-Laie")
        assertThat(kikiTier(150).second).isEqualTo("KI-Visionär")
    }

    @Test
    fun `Jedes Tier hat Emoji und Titel`() {
        for (kiki in listOf(0, 30, 50, 70, 95)) {
            val (emoji, title) = kikiTier(kiki)
            assertThat(emoji).isNotEmpty()
            assertThat(title).isNotEmpty()
        }
    }

    // ── radarSmoothSegments ────────────────────────────────────────────────

    @Test
    fun `weniger als 3 Scheitelpunkte liefern keine Segmente`() {
        assertThat(radarSmoothSegments(emptyList())).isEmpty()
        assertThat(radarSmoothSegments(listOf(center, Offset(200f, 100f)))).isEmpty()
    }

    @Test
    fun `Dreieck - Segmente laufen ueber Kanten-Mittelpunkte`() {
        val a = Offset(100f, 50f)
        val b = Offset(200f, 150f)
        val c = Offset(0f, 150f)
        val segs = radarSmoothSegments(listOf(a, b, c))
        assertThat(segs).hasSize(3)
        // Segment für a: Start = Mitte(c,a), Kontrolle = a, Ende = Mitte(a,b)
        val (start, control, end) = segs[0]
        assertThat(start).isEqualTo(Offset(50f, 100f))
        assertThat(control).isEqualTo(a)
        assertThat(end).isEqualTo(Offset(150f, 100f))
        // Segment für b: Start = Ende des Vorgaengers
        assertThat(segs[1].first).isEqualTo(Offset(150f, 100f))
        assertThat(segs[1].second).isEqualTo(b)
        assertThat(segs[1].third).isEqualTo(Offset(100f, 150f))
        // Segment für c schliesst den Ring zum Start des ersten Segments
        assertThat(segs[2].third).isEqualTo(Offset(50f, 100f))
    }
}
