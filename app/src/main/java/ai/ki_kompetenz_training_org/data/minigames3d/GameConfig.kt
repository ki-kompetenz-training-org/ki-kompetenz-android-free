/*
 * Copyright 2026 Tobias Weiss
 * SPDX-License-Identifier: Apache-2.0
 * Touch-native game configuration - replaces old GameConfig.kt in T2-T4
 */
package ai.ki_kompetenz_training_org.data.minigames3d

/**
 * Touch-native game configuration object.
 * Centralized constants for scoring and behavior.
 */
object GameConfig {
    // Scoring constants
    const val CLASSIFY_POINTS = 25
    const val MAZE_GOAL_POINTS = 40
    const val MAZE_BONUS_POINTS = 15
    const val TRUTH_SNIPE_FAKE_POINTS = 20
    const val TRUTH_SNIPE_FACT_POINTS = 10
    const val CLASSIFY_STREAK_BONUS = 5

    // Arena constants
    const val DEFAULT_ARENA_RADIUS = 15.0

    fun getModeConfig(mode: GameMode): ModeConfig = when (mode) {
        GameMode.ORB_HUNT -> ModeConfig.orbHunt()
        GameMode.MAZE_RUN -> ModeConfig.mazeRun()
        GameMode.TRUTH_SNIPE -> ModeConfig.truthSnipe()
    }
}
