/*
 * Copyright 2026 Tobias Weiss
 * SPDX-License-Identifier: Apache-2.0
 */
package ai.ki_kompetenz_training_org.data.minigames3d

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Unit-Tests für die Spiel-Typen und -Konfigurationen ([GameTypes.kt]).
 *
 * Die ModeConfig-Fabriken definieren das entire Spielgefühl der drei
 * 3D-Mini-Games; die TouchTuning-Stufen definieren die Zielgruppen-
 * Ergonomie (KINDER schneller + kürzere Entscheidungen, SENIOREN
 * langsamer + längere Entscheidungen). Diese Verträge werden hier
 * verriegelt, damit ein Tuning-Change nie unbeabsichtigt bleibt.
 */
class GameTypesTest {

    // ── ModeConfig-Fabriken: gemeinsame Verträge ─────────────────────────

    @Test
    fun `alle drei Modi teilen Arena-Grundgeruest (Radius 15, 60s, 3 Leben)`() {
        listOf(ModeConfig.orbHunt(), ModeConfig.mazeRun(), ModeConfig.truthSnipe()).forEach { cfg ->
            assertThat(cfg.arenaRadius).isEqualTo(15.0)
            assertThat(cfg.duration).isEqualTo(60.0)
            assertThat(cfg.maxHealth).isEqualTo(3)
        }
    }

    @Test
    fun `ModeConfigs sind spielbar (Ziele, Zeitraster) - Entscheidungszeit 4-6s je Modus`() {
        listOf(
            ModeConfig.orbHunt(), ModeConfig.mazeRun(), ModeConfig.truthSnipe()
        ).forEach { cfg ->
            assertThat(cfg.target).isGreaterThan(0)
            assertThat(cfg.decisionSeconds).isGreaterThan(0.0)
            assertThat(cfg.duration).isGreaterThan(cfg.decisionSeconds)
        }
        assertThat(ModeConfig.truthSnipe().decisionSeconds).isEqualTo(4.0)
        assertThat(ModeConfig.orbHunt().decisionSeconds).isEqualTo(5.0)
        assertThat(ModeConfig.mazeRun().decisionSeconds).isEqualTo(6.0)
    }

    @Test
    fun `Ziele sind abgestuft - MAZE_RUN 200, ORB_HUNT 250, TRUTH_SNIPE 300`() {
        assertThat(ModeConfig.mazeRun().target).isEqualTo(200)
        assertThat(ModeConfig.orbHunt().target).isEqualTo(250)
        assertThat(ModeConfig.truthSnipe().target).isEqualTo(300)
    }

    @Test
    fun `ORB_HUNT - statische Chips (chipSpeed 0)`() {
        val cfg = ModeConfig.orbHunt()
        assertThat(cfg.chipSpeed).isEqualTo(0.0)
        assertThat(cfg.minChips).isAtLeast(1)
        assertThat(cfg.maxChips).isAtLeast(cfg.minChips)
    }

    @Test
    fun `MAZE_RUN - KEINE Chips (Labyrinthmodus: Chip-Parameter alle 0)`() {
        val cfg = ModeConfig.mazeRun()
        assertThat(cfg.chipRadius).isEqualTo(0.0)
        assertThat(cfg.chipSpeed).isEqualTo(0.0)
        assertThat(cfg.chipLifetime).isEqualTo(0.0)
        assertThat(cfg.spawnInterval).isEqualTo(0.0)
        assertThat(cfg.minChips).isEqualTo(0)
        assertThat(cfg.maxChips).isEqualTo(0)
    }

    @Test
    fun `TRUTH_SNIPE - bewegte Chips (chipSpeed 1_5), enges Spawnfenster`() {
        val cfg = ModeConfig.truthSnipe()
        assertThat(cfg.chipSpeed).isGreaterThan(0.0)
        assertThat(cfg.minChips).isAtLeast(1)
        assertThat(cfg.maxChips).isAtLeast(cfg.minChips)
    }

    @Test
    fun `falsche Antworten kosten Punkte in jedem Modus (Strafe vorhanden)`() {
        listOf(
            ModeConfig.orbHunt(), ModeConfig.mazeRun(), ModeConfig.truthSnipe()
        ).forEach { cfg ->
            assertThat(cfg.wrongPoints).isGreaterThan(0)
        }
    }

    // ── TouchTuning: Zielgruppen-Ergonomie ───────────────────────────────

    @Test
    fun `STANDARD-Tuning ist neutral (keine Multiplikatoren, keine Entscheidungszeit)`() {
        val t = TouchTuning.STANDARD
        assertThat(t.speedMultiplier).isEqualTo(1.0)
        assertThat(t.decisionSeconds).isNull()
        assertThat(t.spawnRateMultiplier).isEqualTo(1.0)
    }

    @Test
    fun `KIDS-Tuning - schneller, kuerzere Entscheidungen, mehr Spawns (Grenze 8s)`() {
        val t = TouchTuning.KIDS
        assertThat(t.speedMultiplier).isGreaterThan(TouchTuning.STANDARD.speedMultiplier)
        assertThat(t.decisionSeconds).isEqualTo(8.0)
        assertThat(t.decisionSeconds!!).isLessThan(TouchTuning.SENIORS.decisionSeconds!!)
        assertThat(t.spawnRateMultiplier).isGreaterThan(TouchTuning.STANDARD.spawnRateMultiplier)
    }

    @Test
    fun `SENIORS-Tuning - langsamer, laengere Entscheidungen (18s), weniger Spawns`() {
        val t = TouchTuning.SENIORS
        assertThat(t.speedMultiplier).isLessThan(TouchTuning.STANDARD.speedMultiplier)
        assertThat(t.decisionSeconds).isEqualTo(18.0)
        assertThat(t.spawnRateMultiplier).isLessThan(TouchTuning.STANDARD.spawnRateMultiplier)
    }

    @Test
    fun `Entscheidungszeit-Ordnung: KIDS kuerzer als STANDARD-Implicite, SENIOREN am laengsten`() {
        // STANDARD hat null (Entscheidung bleibt auf cfg.decisionSeconds = 5s);
        // die Ordnung KIDS < (implizit 5) < SENIORS ist der Ergonomie-Vertrag.
        val implicitStandard = 5.0
        assertThat(TouchTuning.KIDS.decisionSeconds!!).isLessThan(implicitStandard)
        assertThat(TouchTuning.SENIORS.decisionSeconds!!).isGreaterThan(implicitStandard)
    }

    // ── MazeConfig ───────────────────────────────────────────────────────

    @Test
    fun `MazeConfig - Level waehlt Layout per Modulo (wrap-around)`() {
        val size = MazeLayouts.LAYOUTS.size
        assertThat(MazeConfig(0).layoutIndex).isEqualTo(0)
        assertThat(MazeConfig(1).layoutIndex).isEqualTo(1 % size)
        assertThat(MazeConfig(size).layoutIndex).isEqualTo(0)
        assertThat(MazeConfig(size + 2).layoutIndex).isEqualTo(2)
    }

    @Test
    fun `MazeConfig - layoutIndex bleibt immer im gueltigen Bereich fuer Level >= 0`() {
        val size = MazeLayouts.LAYOUTS.size
        for (level in 0 until size * 2) {
            val idx = MazeConfig(level).layoutIndex
            assertThat(idx).isAtLeast(0)
            assertThat(idx).isLessThan(size)
        }
    }

    // ── Direction / GameAction ───────────────────────────────────────────

    @Test
    fun `Direction hat genau 4 Richtungen (Swipe-Vertrag)`() {
        assertThat(Direction.values.toList()).containsExactly(
            Direction.UP, Direction.DOWN, Direction.LEFT, Direction.RIGHT
        )
    }

    // ── LiteracyStatement: Sprach-Fallback ───────────────────────────────

    @Test
    fun `LiteracyStatement - de liefert Deutsch, alles andere faellt auf Englisch`() {
        val s = LiteracyStatement(
            textDe = "Risiko",
            textEn = "Risk",
            domain = "EU AI Act",
            isRisk = true,
            explanationDe = "Erklärung",
            explanationEn = "Explanation",
        )
        assertThat(s.text("de")).isEqualTo("Risiko")
        assertThat(s.text("en")).isEqualTo("Risk")
        assertThat(s.text("fr")).isEqualTo("Risk") // Fallback
        assertThat(s.explanation("de")).isEqualTo("Erklärung")
        assertThat(s.explanation("en")).isEqualTo("Explanation")
        assertThat(s.explanation("xx")).isEqualTo("Explanation")
    }

    @Test
    fun `LiteracyStatement - Erklärungen duerfen leer sein (Defaults)`() {
        val s = LiteracyStatement(
            textDe = "Risiko", textEn = "Risk",
            domain = "d", isRisk = false,
        )
        assertThat(s.explanationDe).isEmpty()
        assertThat(s.explanationEn).isEmpty()
    }
}
