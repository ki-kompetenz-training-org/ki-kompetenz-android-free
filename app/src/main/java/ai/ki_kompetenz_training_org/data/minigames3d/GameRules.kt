/*
 * Copyright 2026 Tobias Weiss
 * SPDX-License-Identifier: Apache-2.0
 * Touch-native game rules - replaces GameRules.kt in T2-T4
 */
package ai.ki_kompetenz_training_org.data.minigames3d

/**
 * Touch-native game rules utility.
 * Centralized game end logic.
 */
object GameRules {

    /**
     * End the game with the given reason.
     * Sets ended=true, endReason, and won based on whether target was reached.
     *
     * Idempotent (FIX 2026-09-01): Der erste End-Grund gewinnt. Vorher hat ein
     * zweiter endGame-Aufruf (z. B. Zeitablauf nach Health-Tod) den Grund UND
     * die won-Bewertung überschrieben.
     */
    fun endGame(s: GameState, reason: EndReason) {
        if (s.ended) return
        s.ended = true
        s.endReason = reason
        s.won = s.score >= s.target
    }
}
