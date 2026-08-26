package ai.ki_kompetenz_training_org.data.daily

import ai.ki_kompetenz_training_org.data.minigames.MiniGame
import android.content.SharedPreferences
import java.time.LocalDate

/**
 * Repository for daily challenge state persistence.
 * Backed by SharedPreferences (kikompetenz_gamification prefs).
 * DSGVO-compliant: all data stored locally, no server transmission.
 */
class DailyChallengeRepository(
    private val prefs: SharedPreferences
) {
    companion object {
        private const val KEY_LAST_DATE = "daily_challenge_last_date"
        private const val KEY_STREAK = "daily_challenge_streak"
    }

    /**
     * Get today's challenge mini-game.
     * Uses deterministic selection based on the date.
     */
    fun getTodayChallenge(today: LocalDate, games: List<MiniGame>): MiniGame? =
        DailyChallengeSelector.select(today, games)

    /**
     * Check if the daily challenge has already been completed today.
     */
    fun isCompletedToday(today: LocalDate): Boolean {
        val lastDate = prefs.getString(KEY_LAST_DATE, null)
        return DailyChallengeState.isCompleted(lastDate, today)
    }

    /**
     * Get the current daily challenge streak count.
     */
    fun getStreak(): Int = prefs.getInt(KEY_STREAK, 0)

    /**
     * Complete the daily challenge.
     * Awards XP, updates streak, and persists completion date.
     * @return XP awarded (0 if already completed today)
     */
    fun completeChallenge(today: LocalDate, perfect: Boolean): Int {
        val lastDate = prefs.getString(KEY_LAST_DATE, null)

        // Already completed today — no XP, no persistence
        if (DailyChallengeState.isCompleted(lastDate, today)) return 0

        // Calculate new streak
        val currentStreak = prefs.getInt(KEY_STREAK, 0)
        val newStreak = calculateNewStreak(lastDate, today, currentStreak)

        // Calculate XP
        val xp = DailyChallengeRules.calculateXp(streakDay = newStreak, perfect = perfect)

        // Persist
        prefs.edit()
            .putString(KEY_LAST_DATE, DailyChallengeState.formatDate(today))
            .putInt(KEY_STREAK, newStreak)
            .apply()

        return xp
    }

    /**
     * Preview the potential XP for completing today's challenge.
     * Uses the current stored streak (or 0 if no streak).
     */
    fun calculateXpPreview(perfect: Boolean): Int {
        val streak = prefs.getInt(KEY_STREAK, 0)
        val streakDay = maxOf(1, streak)
        return DailyChallengeRules.calculateXp(streakDay = streakDay, perfect = perfect)
    }

    /**
     * Calculate the new streak after completing today's challenge.
     * - If last completion was yesterday: streak + 1
     * - If last completion was today: 0 (shouldn't happen, handled by caller)
     * - If gap or first time: 1
     */
    private fun calculateNewStreak(lastDate: String?, today: LocalDate, currentStreak: Int): Int {
        if (lastDate == null) return 1
        val last = DailyChallengeState.parseDate(lastDate) ?: return 1
        return when {
            last == today -> 0 // already completed (shouldn't reach here)
            last == today.minusDays(1) -> currentStreak + 1 // consecutive day
            else -> 1 // gap, reset
        }
    }
}
