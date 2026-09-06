/*
 * Copyright 2026 Tobias Weiss
 * SPDX-License-Identifier: Apache-2.0
 */
package ai.ki_kompetenz_training_org.data.minigames3d

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Verriegelt den zentralen Punkt- und Konfigurationsvertrag des Touch-Arenas:
 * [GameConfig]. Die Konstanten hier sind die Quelle aller Punktvergaben im
 * [GameEngine] (Punktevergabe in GameEngineTest/ModedHandlersTest verhaltens-
 * getestet — dieser Test verriegelt die WERTE und die [GameConfig.getModeConfig]
 * Dispatch-Exhaustivität).
 *
 * Stand (Audit 2026-09-05): CLASSIFY_POINTS, MAZE_BONUS_POINTS,
 * TRUTH_SNIPE_FACT_POINTS, CLASSIFY_STREAK_BONUS und DEFAULT_ARENA_RADIUS
 * sind im Scoring-Pfad aktiv; MAZE_GOAL_POINTS und TRUTH_SNIPE_FAKE_POINTS
 * sind in MAIN nur deklariert (aktuell nicht im Pfad — hier trotzdem als
 * deklarierter Vertrag festgehalten, damit eine spätere Aktivierung ohne
 * stilles Driften testbar ist).
 */
class GameConfigTest {

    // ── Punkt-Konstanten ─────────────────────────────────────────────────

    @Test
    fun `KLASSIFIKATION vergibt 25 Punkte (CLASSIFY_POINTS)`() {
        assertThat(GameConfig.CLASSIFY_POINTS).isEqualTo(25)
    }

    @Test
    fun `MAZE Ziel erreicht 40 Punkte (MAZE_GOAL_POINTS)`() {
        assertThat(GameConfig.MAZE_GOAL_POINTS).isEqualTo(40)
    }

    @Test
    fun `MAZE Bonus-Spawn vergibt 15 Punkte (MAZE_BONUS_POINTS)`() {
        assertThat(GameConfig.MAZE_BONUS_POINTS).isEqualTo(15)
    }

    @Test
    fun `TruthSnipe Fake erkannt 20 Punkte (TRUTH_SNIPE_FAKE_POINTS)`() {
        assertThat(GameConfig.TRUTH_SNIPE_FAKE_POINTS).isEqualTo(20)
    }

    @Test
    fun `TruthSnipe Fakt archiviert halbe Punkte (TRUTH_SNIPE_FACT_POINTS = 10)`() {
        assertThat(GameConfig.TRUTH_SNIPE_FACT_POINTS).isEqualTo(10)
        // Vertrag: Fakt = halber Fake-Wert (20/2)
        assertThat(GameConfig.TRUTH_SNIPE_FACT_POINTS * 2)
            .isEqualTo(GameConfig.TRUTH_SNIPE_FAKE_POINTS)
    }

    @Test
    fun `Classify-Streak-Bonus ist 5 und deckelt sich bei 50 (10 Grade)`() {
        assertThat(GameConfig.CLASSIFY_STREAK_BONUS).isEqualTo(5)
        // Streak x 5 wird in GameEngine mit min(_, 50) gedeckelt
        assertThat(GameConfig.CLASSIFY_STREAK_BONUS * 10).isEqualTo(50)
    }

    @Test
    fun `Standard-Arenaradius ist 15 (DEFAULT_ARENA_RADIUS)`() {
        assertThat(GameConfig.DEFAULT_ARENA_RADIUS).isEqualTo(15.0)
    }

    // ── Dispatch-Exhaustivität ───────────────────────────────────────────

    @Test
    fun `getModeConfig - ORB_HUNT liefert die orbHunt-Konfiguration`() {
        assertThat(GameConfig.getModeConfig(GameMode.ORB_HUNT))
            .isEqualTo(ModeConfig.orbHunt())
    }

    @Test
    fun `getModeConfig - MAZE_RUN liefert die mazeRun-Konfiguration`() {
        assertThat(GameConfig.getModeConfig(GameMode.MAZE_RUN))
            .isEqualTo(ModeConfig.mazeRun())
    }

    @Test
    fun `getModeConfig - TRUTH_SNIPE liefert die truthSnipe-Konfiguration`() {
        assertThat(GameConfig.getModeConfig(GameMode.TRUTH_SNIPE))
            .isEqualTo(ModeConfig.truthSnipe())
    }

    // ── Kreuz-Verweise (Zusammenspiel mit den ModeConfig-Factories) ──────

    @Test
    fun `getModeConfig liefert die shared Arena-Konfiguration gleich DEFAULT_ARENA_RADIUS`() {
        assertThat(GameConfig.getModeConfig(GameMode.ORB_HUNT).arenaRadius)
            .isEqualTo(GameConfig.DEFAULT_ARENA_RADIUS)
        assertThat(GameConfig.getModeConfig(GameMode.MAZE_RUN).arenaRadius)
            .isEqualTo(GameConfig.DEFAULT_ARENA_RADIUS)
        assertThat(GameConfig.getModeConfig(GameMode.TRUTH_SNIPE).arenaRadius)
            .isEqualTo(GameConfig.DEFAULT_ARENA_RADIUS)
    }
}
