/*
 * Copyright 2026 Tobias Weiss
 * SPDX-License-Identifier: Apache-2.0
 */
package ai.ki_kompetenz_training_org.ui.home

import ai.ki_kompetenz_training_org.data.db.CompetencySnapshotEntity
import ai.ki_kompetenz_training_org.data.minigames3d.KikiGuidance
import androidx.compose.ui.geometry.Offset
import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Unit-Tests der puren Sparkline-Mathematik ([kikiDelta], [sparklinePoints])
 * — Offset ist pure Kotlin, kein Robolectric noetig.
 */
class KikiSparklineTest {

    private fun snapshot(week: String, kiki: Int) =
        CompetencySnapshotEntity(weekKey = week, kiki = kiki, perDomainJson = "[]", createdAt = 0L)

    // ── kikiDelta ───────────────────────────────────────────────────────────

    @Test
    fun `leere Liste liefert null`() {
        assertThat(kikiDelta(emptyList())).isNull()
    }

    @Test
    fun `ein Snapshot liefert null`() {
        assertThat(kikiDelta(listOf(snapshot("2026-W35", 54)))).isNull()
    }

    @Test
    fun `zwei Snapshots liefern die Differenz`() {
        val snapshots = listOf(snapshot("2026-W35", 54), snapshot("2026-W36", 61))
        assertThat(kikiDelta(snapshots)).isEqualTo(7)
    }

    @Test
    fun `negative Differenz wird geliefert`() {
        val snapshots = listOf(snapshot("2026-W35", 61), snapshot("2026-W36", 54))
        assertThat(kikiDelta(snapshots)).isEqualTo(-7)
    }

    @Test
    fun `Eingabe wird intern chronologisch sortiert`() {
        // Absichtlich unsortiert uebergeben (Repository liefert DESC):
        val snapshots = listOf(snapshot("2026-W36", 61), snapshot("2026-W35", 54))
        assertThat(kikiDelta(snapshots)).isEqualTo(7)
    }

    @Test
    fun `mehrere Snapshots - Delta nur aus den letzten beiden`() {
        val snapshots = listOf(
            snapshot("2026-W33", 10),
            snapshot("2026-W34", 50),
            snapshot("2026-W35", 54),
            snapshot("2026-W36", 61),
        )
        assertThat(kikiDelta(snapshots)).isEqualTo(7)
    }

    @Test
    fun `gleiche Werte liefern 0`() {
        val snapshots = listOf(snapshot("2026-W35", 55), snapshot("2026-W36", 55))
        assertThat(kikiDelta(snapshots)).isEqualTo(0)
    }

    // ── sparklinePoints ─────────────────────────────────────────────────────

    @Test
    fun `leere Werte liefern keine Punkte`() {
        assertThat(sparklinePoints(emptyList(), 200f, 100f, 10f)).isEmpty()
    }

    @Test
    fun `einzelner Wert wird horizontal zentriert`() {
        val points = sparklinePoints(listOf(50), width = 200f, height = 100f, padding = 10f)
        assertThat(points).hasSize(1)
        assertThat(points[0].x).isWithin(1e-3f).of(100f)
        // y: 100 - 10 - 0.5 * 80 = 50 (Mitte der Canvas)
        assertThat(points[0].y).isWithin(1e-3f).of(50f)
    }

    @Test
    fun `zwei Werte spannen die volle Breite auf`() {
        val points = sparklinePoints(listOf(0, 100), width = 220f, height = 100f, padding = 10f)
        // erster Punkt: links unten (0)
        assertThat(points[0].x).isWithin(1e-3f).of(10f)
        assertThat(points[0].y).isWithin(1e-3f).of(90f)
        // zweiter Punkt: rechts oben (100)
        assertThat(points[1].x).isWithin(1e-3f).of(210f)
        assertThat(points[1].y).isWithin(1e-3f).of(10f)
    }

    @Test
    fun `drei Werte werden gleichmaessig verteilt`() {
        val points = sparklinePoints(listOf(0, 50, 100), width = 220f, height = 120f, padding = 10f)
        assertThat(points).hasSize(3)
        val xs = points.map { it.x }
        val step = (220f - 20f) / 2
        assertThat(xs[1] - xs[0]).isWithin(1e-3f).of(step)
        assertThat(xs[2] - xs[1]).isWithin(1e-3f).of(step)
        // hoeherer Wert = kleineres y (nach oben gezeichnet)
        assertThat(points[2].y).isLessThan(points[1].y)
        assertThat(points[1].y).isLessThan(points[0].y)
    }

    @Test
    fun `Werte ausserhalb 0-100 werden geklemmt`() {
        val points = sparklinePoints(listOf(-20, 150), width = 220f, height = 100f, padding = 10f)
        assertThat(points[0].y).isWithin(1e-3f).of(90f) // wie 0
        assertThat(points[1].y).isWithin(1e-3f).of(10f) // wie 100
    }

    // ── guidanceFooterRes (openspec add-kiki-guidance) ───────────────────

    private val practice = KikiGuidance.Guidance(
        type = KikiGuidance.Type.PRACTICE,
        domain = "DSGVO",
        score = 34,
    )

    private val decay = KikiGuidance.Guidance(
        type = KikiGuidance.Type.DECAY,
        daysSince = 10,
    )

    @Test
    fun `footer practice mit Spiel zeigt Empfehlungs-Zeile`() {
        assertThat(guidanceFooterRes(practice, hasPracticeGame = true))
            .isEqualTo(ai.ki_kompetenz_training_org.R.string.kiki_practice_now)
    }

    @Test
    fun `footer practice ohne Spiel zeigt keinen Footer`() {
        assertThat(guidanceFooterRes(practice, hasPracticeGame = false)).isNull()
    }

    @Test
    fun `footer decay zeigt Verfalls-Hinweis auch ohne Spiel`() {
        assertThat(guidanceFooterRes(decay, hasPracticeGame = false))
            .isEqualTo(ai.ki_kompetenz_training_org.R.string.kiki_decay_hint)
    }

    @Test
    fun `footer null-Guidance zeigt keinen Footer`() {
        assertThat(guidanceFooterRes(null, hasPracticeGame = true)).isNull()
    }
}
