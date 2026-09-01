/*
 * Copyright 2026 Tobias Weiss
 * SPDX-License-Identifier: Apache-2.0
 * Touch-native mode handlers dispatcher - replaces ModeHandler.kt in T2-T4
 */
package ai.ki_kompetenz_training_org.data.minigames3d

/**
 * Touch-native mode handler interface.
 * Each game mode provides its own implementation.
 */
interface ModedHandler {
    /** Initialize game state for this mode */
    fun init(s: GameState, cfg: ModeConfig, rng: () -> Double, content: LiteracyContentProvider?)

    /** Per-frame step for this mode (called when pendingDecision == null) */
    fun step(s: GameState, cfg: ModeConfig, tuning: TouchTuning, content: LiteracyContentProvider?, rng: () -> Double, dt: Double)

    /** Handle tap on entity at diskIndex */
    fun onTap(s: GameState, cfg: ModeConfig, diskIndex: Int, content: LiteracyContentProvider?, rng: () -> Double)

    /** Handle dash in direction */
    fun onDash(s: GameState, cfg: ModeConfig, dir: Direction, content: LiteracyContentProvider?, rng: () -> Double)

    /** Called after a decision is resolved */
    fun onDecisionClosed(s: GameState, cfg: ModeConfig, closed: PendingDecision, correct: Boolean, content: LiteracyContentProvider?, rng: () -> Double)

    /** Top-up collectibles for this mode */
    fun topUp(s: GameState, cfg: ModeConfig, tuning: TouchTuning, rng: () -> Double, content: LiteracyContentProvider?)
}

/**
 * Dispatcher for mode-specific handlers.
 */
object ModedHandlers {
    private val handlers: Map<GameMode, ModedHandler> = mapOf(
        GameMode.ORB_HUNT to OrbHuntHandler,
        GameMode.MAZE_RUN to MazeRunHandler,
        GameMode.TRUTH_SNIPE to TruthSnipeHandler,
    )

    fun get(mode: GameMode): ModedHandler = handlers[mode] ?: error("No handler for mode $mode")
}
