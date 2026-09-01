/*
 * Copyright 2026 Tobias Weiss
 * SPDX-License-Identifier: Apache-2.0
 * Touch-native game state - replaces GameState in T2-T4
 */
package ai.ki_kompetenz_training_org.data.minigames3d

/**
 * Touch-native game state.
 * All fields are var for in-place mutation during stepGame.
 *
 * Freeze invariant: when pendingDecision != null, entities do not move.
 * Only the decision timer counts down.
 */
data class GameState(
    val mode: GameMode,
    var time: Double,
    var timeLeft: Double,
    var score: Int,
    var health: Int,
    val maxHealth: Int,
    val target: Int,
    val collectibles: MutableList<Disk>,
    val maze: MazeLayouts.MazeGrid?,
    var playerCellRow: Int,
    var playerCellCol: Int,
    var ended: Boolean,
    var endReason: EndReason?,
    var won: Boolean,
    var justScored: Boolean,
    var justHit: Boolean,
    var scoreX: Double,
    var scoreZ: Double,
    var scoreKind: Int,
    var hitX: Double,
    var hitZ: Double,
    var hitKind: Int,
    var classifyStreak: Int,
    var pendingDecision: PendingDecision?,
    val classifications: MutableList<ClassifyLog>,
) {
    companion object {
        fun create(mode: GameMode, cfg: ModeConfig): GameState {
            val maze = if (mode == GameMode.MAZE_RUN) {
                MazeLayouts.layout(cfg.mazeLevel)
            } else {
                null
            }

            val startPos = maze?.startPos() ?: (0 to 0)

            return GameState(
                mode = mode,
                time = 0.0,
                timeLeft = cfg.duration,
                score = 0,
                health = cfg.maxHealth,
                maxHealth = cfg.maxHealth,
                target = cfg.target,
                collectibles = mutableListOf(),
                maze = maze,
                playerCellRow = startPos.first,
                playerCellCol = startPos.second,
                ended = false,
                endReason = null,
                won = false,
                justScored = false,
                justHit = false,
                scoreX = 0.0,
                scoreZ = 0.0,
                scoreKind = 0,
                hitX = 0.0,
                hitZ = 0.0,
                hitKind = 0,
                classifyStreak = 0,
                pendingDecision = null,
                classifications = mutableListOf(),
            )
        }
    }
}

/**
 * Touch-native pending decision.
 * Triggered when player taps an entity (ORB_HUNT, TRUTH_SNIPE) or reaches goal (MAZE_RUN).
 * 
 * @param statement The AI literacy statement to classify
 * @param timerMax Initial timer value (counts down)
 * @param timer Current timer value
 * @param x Position x for visualization
 * @param z Position z for visualization
 * @param fromBonus True if this decision is from a maze bonus cell
 * @param diskIndex Index in collectibles list (or -1 for maze statements)
 * @param isRisk Whether the statement is a risk (for display purposes)
 */
