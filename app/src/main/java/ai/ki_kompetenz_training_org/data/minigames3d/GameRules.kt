package ai.ki_kompetenz_training_org.data.minigames3d

/** Shared game rules: win/lose computation and game-end mutation. */
object GameRules {

    fun computeResult(
        score: Int,
        target: Int,
        health: Int,
        reason: EndReason,
    ): GameResult {
        val won = if (reason == EndReason.TIME) score >= target else health > 0
        return GameResult(won, reason, score, target)
    }

    /** Mark the game as ended with the given reason and compute the win flag. */
    fun endGame(s: GameState, reason: EndReason) {
        if (s.ended) return
        s.ended = true
        s.endReason = reason
        val res = computeResult(s.score, s.target, s.health, reason)
        s.won = res.won
    }
}

data class GameResult(
    val won: Boolean,
    val reason: EndReason,
    val score: Int,
    val target: Int,
)
