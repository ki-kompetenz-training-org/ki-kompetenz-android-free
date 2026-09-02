/*
 * Copyright 2026 Tobias Weiss
 * SPDX-License-Identifier: Apache-2.0
 * Tests for the touch-native mode handler dispatcher and the per-mode
 * GameConfig defaults. Pure Config/Dispatch — no gamification coupling.
 */
package ai.ki_kompetenz_training_org.data.minigames3d

import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import org.junit.Test

/**
 * Verifiziert den Handler-Dispatch über [ModedHandlers.get] und die
 * ModeConfig-Invarianten über [GameConfig.getModeConfig].
 *
 * Dispatch ist stabil: [ModedHandlers.get] liest aus einer konstanten Map
 * von [GameMode] auf die jeweiligen Singleton-`object`-Handler, daher liefern
 * wiederholte Aufrufe garantiert dieselbe Instanz.
 */
class ModedHandlersTest {

    // ========== 1) Dispatch: get() liefert den passenden Handler ==========

    @Test
    fun `get liefert fuer ORB_HUNT einen OrbHuntHandler`() {
        val handler = ModedHandlers.get(GameMode.ORB_HUNT)
        assertThat(handler).isInstanceOf(OrbHuntHandler::class.java)
    }

    @Test
    fun `get liefert fuer TRUTH_SNIPE einen TruthSnipeHandler`() {
        val handler = ModedHandlers.get(GameMode.TRUTH_SNIPE)
        assertThat(handler).isInstanceOf(TruthSnipeHandler::class.java)
    }

    @Test
    fun `get liefert fuer MAZE_RUN einen MazeRunHandler`() {
        val handler = ModedHandlers.get(GameMode.MAZE_RUN)
        assertThat(handler).isInstanceOf(MazeRunHandler::class.java)
    }

    // ========== 2) Dispatch-Stabilität ==========

    @Test
    fun `get ist stabil und liefert fuer jeden Modus dieselbe Instanz`() {
        // Implementierungspruefung: die Handler-Map zeigt auf Kotlin-`object`
        // (OrbHuntHandler, TruthSnipeHandler, MazeRunHandler), d.h. get() ist
        // ein Singleton-Dispatch — wiederholte Aufrufe liefern dieselbe Instanz.
        GameMode.values().forEach { mode ->
            val first = ModedHandlers.get(mode)
            val second = ModedHandlers.get(mode)
            assertWithMessage("get($mode) muss stabil dieselbe Instanz liefern")
                .that(second)
                .isSameInstanceAs(first)
        }
    }

    @Test
    fun `get leitet jeden bestehenden Modus weiter ohne zu werfen`() {
        GameMode.values().forEach { mode ->
            assertThat(ModedHandlers.get(mode)).isNotNull()
        }
    }

    // ========== 3) GameConfig liefert fuer ALLE Modi eine Konfiguration ==========

    @Test
    fun `getModeConfig liefert fuer jeden GameMode eine nicht-null Konfiguration`() {
        GameMode.values().forEach { mode ->
            assertWithMessage("getModeConfig($mode) darf nicht null sein")
                .that(GameConfig.getModeConfig(mode))
                .isNotNull()
        }
    }

    // ========== 4) Config-Invarianten pro Modus ==========

    @Test
    fun `jeder Modus erfuellt die Konfigurationsinvarianten`() {
        GameMode.values().forEach { mode ->
            val cfg = GameConfig.getModeConfig(mode)
            assertWithMessage("$mode: target muss > 0 sein")
                .that(cfg.target)
                .isGreaterThan(0)
            assertWithMessage("$mode: maxHealth muss >= 1 sein")
                .that(cfg.maxHealth)
                .isAtLeast(1)
            // Zeitbudget in Sekunden (ModeConfig.duration → GameState.timeLeft)
            assertWithMessage("$mode: duration (Zeitbudget) muss > 0 sein")
                .that(cfg.duration)
                .isGreaterThan(0.0)
            assertWithMessage("$mode: decisionSeconds muss > 0 sein")
                .that(cfg.decisionSeconds)
                .isGreaterThan(0.0)
            assertWithMessage("$mode: minChips darf nie negativ sein")
                .that(cfg.minChips)
                .isAtLeast(0)
        }
    }

    // ========== 5) Arena-Modi brauchen Chipdichte ==========

    @Test
    fun `ORB_HUNT und TRUTH_SNIPE haben minChips von mindestens 4`() {
        // Die Arena braucht genug Spawn-Dichte: Mit null-Content natuerlich 0,
        // aber die Basis-Konfiguration muss fuer beide Arena-Modi >= 4 sein.
        val orbHuntChips = GameConfig.getModeConfig(GameMode.ORB_HUNT).minChips
        val truthSnipeChips = GameConfig.getModeConfig(GameMode.TRUTH_SNIPE).minChips
        assertThat(orbHuntChips).isAtLeast(4)
        assertThat(truthSnipeChips).isAtLeast(4)
    }

    @Test
    fun `MAZE_RUN darf minChips 0 haben da keine freien Chips gespawnt werden`() {
        // Quelle: ModeConfig.mazeRun() setzt minChips = 0 / maxChips = 0 —
        // MAZE_RUN bewegt den Spieler ueber ein Raster und spawnt keine
        // frei schwebenden Chips, daher ist 0 hier zulaessig.
        val cfg = GameConfig.getModeConfig(GameMode.MAZE_RUN)
        assertThat(cfg.minChips).isEqualTo(0)
        assertThat(cfg.maxChips).isEqualTo(0)
    }
}
