/*
 * Copyright 2026 Tobias Weiss
 * SPDX-License-Identifier: Apache-2.0
 */
package ai.ki_kompetenz_training_org.data.minigames3d

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Unit-Tests für die GameState-Fabrik ([GameState.create]).
 *
 * Der Startzustand definiert, was der Spieler sieht, wenn ein 3D-Mini-Game
 * beginnt: volles Leben, 60 Sekunden, Score 0, keine Endflags. Für MAZE_RUN
 * wird zusätzlich das Labyrinth geladen und die Startposition gesetzt.
 */
class GameStateFactoryTest {

    @Test
    fun `create für ORB_HUNT - frischer Startzustand ohne Labyrinth`() {
        val s = GameState.create(GameMode.ORB_HUNT, ModeConfig.orbHunt())

        assertThat(s.mode).isEqualTo(GameMode.ORB_HUNT)
        assertThat(s.time).isEqualTo(0.0)
        assertThat(s.timeLeft).isEqualTo(60.0)
        assertThat(s.score).isEqualTo(0)
        assertThat(s.health).isEqualTo(3)
        assertThat(s.maxHealth).isEqualTo(3)
        assertThat(s.target).isEqualTo(250)
        assertThat(s.collectibles).isEmpty()
        assertThat(s.maze).isNull()
        assertThat(s.ended).isFalse()
        assertThat(s.endReason).isNull()
        assertThat(s.won).isFalse()
        assertThat(s.justScored).isFalse()
        assertThat(s.justHit).isFalse()
        assertThat(s.pendingDecision).isNull()
        assertThat(s.classifications).isEmpty()
    }

    @Test
    fun `create für MAZE_RUN - Labyrinth geladen, Spieler an Startposition`() {
        val s = GameState.create(GameMode.MAZE_RUN, ModeConfig.mazeRun())

        assertThat(s.mode).isEqualTo(GameMode.MAZE_RUN)
        assertThat(s.maze).isNotNull()
        val start = s.maze!!.startPos()
        assertThat(s.playerCellRow).isEqualTo(start.first)
        assertThat(s.playerCellCol).isEqualTo(start.second)
    }

    @Test
    fun `create für TRUTH_SNIPE - kein Labyrinth, Ziel 300`() {
        val s = GameState.create(GameMode.TRUTH_SNIPE, ModeConfig.truthSnipe())

        assertThat(s.maze).isNull()
        assertThat(s.target).isEqualTo(300)
        assertThat(s.health).isEqualTo(s.maxHealth)
    }

    @Test
    fun `create übernimmt Dauer und Ziel aus der ModeConfig (keine Hardcodes)`() {
        listOf(
            GameMode.ORB_HUNT to ModeConfig.orbHunt(),
            GameMode.MAZE_RUN to ModeConfig.mazeRun(),
            GameMode.TRUTH_SNIPE to ModeConfig.truthSnipe(),
        ).forEach { (mode, cfg) ->
            val s = GameState.create(mode, cfg)
            assertThat(s.timeLeft).isEqualTo(cfg.duration)
            assertThat(s.target).isEqualTo(cfg.target)
            assertThat(s.maxHealth).isEqualTo(cfg.maxHealth)
            assertThat(s.health).isEqualTo(cfg.maxHealth)
        }
    }

    @Test
    fun `create liefert veränderbare Spiel-Listen (Engine schreibt hinein)`() {
        val s = GameState.create(GameMode.ORB_HUNT, ModeConfig.orbHunt())

        // Die Engine sammelt Chips/Logs in diesen Listen — Mutationen müssen möglich sein
        s.collectibles.add(
            Disk(x = 1.0, z = 1.0, r = 0.5, vx = 0.0, vz = 0.0)
        )
        assertThat(s.collectibles).hasSize(1)
    }
}
