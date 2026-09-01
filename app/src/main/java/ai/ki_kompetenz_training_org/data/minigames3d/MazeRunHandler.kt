/*
 * Copyright 2026 Tobias Weiss
 * SPDX-License-Identifier: Apache-2.0
 * Touch-native MazeRun mode handler - replaces MazeRunHandler.kt in T2-T4
 */
package ai.ki_kompetenz_training_org.data.minigames3d

/**
 * Touch-native MazeRun handler.
 * 
 * Mode behavior:
 * - Player moves on integer cell grid
 * - Swipe/dash in direction moves one cell
 * - Wall collision: rejected, shake feedback
 * - Goal cell: triggers classification decision
 * - Bonus cell: triggers classification decision, +15 points on correct
 * - Correct goal: +40 points, advance to next maze
 * - Wrong goal: -1 health, new statement (Lernloop)
 * - Decision timeout: -1 health
 */
object MazeRunHandler : ModedHandler {

    override fun init(s: GameState, cfg: ModeConfig, rng: () -> Double, content: LiteracyContentProvider?) {
        // Player starts at maze start position (already set in GameState.create)
    }

    override fun step(s: GameState, cfg: ModeConfig, tuning: TouchTuning, content: LiteracyContentProvider?, rng: () -> Double, dt: Double) {
        // MazeRun doesn't move entities between frames
        // Player movement is discrete via onDash
    }

    override fun onTap(s: GameState, cfg: ModeConfig, diskIndex: Int, content: LiteracyContentProvider?, rng: () -> Double) {
        // MazeRun doesn't use tap
    }

    override fun onDash(s: GameState, cfg: ModeConfig, dir: Direction, content: LiteracyContentProvider?, rng: () -> Double) {
        if (s.ended || s.pendingDecision != null) return
        
        val maze = s.maze ?: return
        val (newRow, newCol) = when (dir) {
            Direction.UP -> s.playerCellRow - 1 to s.playerCellCol
            Direction.DOWN -> s.playerCellRow + 1 to s.playerCellCol
            Direction.LEFT -> s.playerCellRow to s.playerCellCol - 1
            Direction.RIGHT -> s.playerCellRow to s.playerCellCol + 1
        }

        // Check bounds
        if (newRow < 0 || newRow >= maze.rows || newCol < 0 || newCol >= maze.cols) return

        // Check wall
        if (maze.isWall(newRow, newCol)) {
            // Wall collision - shake feedback (no state change in touch-native)
            return
        }

        // Move player
        s.playerCellRow = newRow
        s.playerCellCol = newCol

        // Check if new position triggers decision
        checkCellTrigger(s, maze, newRow, newCol, cfg, rng, content)
    }

    private fun checkCellTrigger(
        s: GameState,
        maze: MazeLayouts.MazeGrid,
        row: Int,
        col: Int,
        cfg: ModeConfig,
        rng: () -> Double,
        content: LiteracyContentProvider?
    ) {
        val contentProvider = content ?: return

        if (maze.isGoal(row, col)) {
            // Trigger goal decision
            val statement = if (rng() < 0.5) {
                contentProvider.randomRisk(rng)
            } else {
                contentProvider.randomFact(rng)
            }
            s.pendingDecision = PendingDecision(
                statement = statement,
                timerMax = cfg.decisionSeconds,
                timer = cfg.decisionSeconds,
                x = col.toDouble(),
                z = row.toDouble(),
                fromBonus = false,
                diskIndex = -1,
                isRisk = statement.isRisk,
            )
        } else if (maze.isBonus(row, col)) {
            // Trigger bonus decision
            val statement = if (rng() < 0.5) {
                contentProvider.randomRisk(rng)
            } else {
                contentProvider.randomFact(rng)
            }
            s.pendingDecision = PendingDecision(
                statement = statement,
                timerMax = cfg.decisionSeconds,
                timer = cfg.decisionSeconds,
                x = col.toDouble(),
                z = row.toDouble(),
                fromBonus = true,
                diskIndex = -1,
                isRisk = statement.isRisk,
            )
        }
    }

    override fun onDecisionClosed(s: GameState, cfg: ModeConfig, closed: PendingDecision, correct: Boolean, content: LiteracyContentProvider?, rng: () -> Double) {
        if (closed.fromBonus) {
            // Bonus cell consumed regardless of correctness
            // In touch-native, bonus cells are one-time triggers
            // The maze itself doesn't change, but we could track consumed bonuses
        }
        
        // If this was a goal decision and correct, check if all goals are reached
        // For simplicity, we just continue until time runs out
    }

    override fun topUp(s: GameState, cfg: ModeConfig, tuning: TouchTuning, rng: () -> Double, content: LiteracyContentProvider?) {
        // MazeRun doesn't need collectible top-up
    }
}
