package ai.ki_kompetenz_training_org.ui.quiz

/**
 * Gamification constants for the KI-Score Quiz.
 * Mirrors the web version (components/KiScoreGame.tsx) for consistency.
 */
object QuizConstants {
    // Timer
    const val ROUND_SECONDS: Int = 20  // 20 seconds per question (same as web)
    const val TIMER_TICK_MS: Long = 1000  // 1 second

    // Lives (Herzen / ❤️)
    const val MAX_LIVES: Int = 3

    // Combo multipliers
    fun comboMultiplier(combo: Int): Double = when {
        combo >= 4 -> 2.0
        combo >= 2 -> 1.5
        else -> 1.0
    }

    // Points per correct answer: (100 + timeLeft * 10) * comboMultiplier
    fun pointsForCorrectAnswer(timeLeft: Int, combo: Int): Int {
        return ((100 + timeLeft * 10) * comboMultiplier(combo)).toInt()
    }

    // Total possible score (10 questions * max points)
    val MAX_SCORE: Int = pointsForCorrectAnswer(ROUND_SECONDS, 4) * 10
}
