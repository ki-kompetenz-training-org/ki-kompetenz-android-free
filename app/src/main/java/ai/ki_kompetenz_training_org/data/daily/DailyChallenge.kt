package ai.ki_kompetenz_training_org.data.daily

import ai.ki_kompetenz_training_org.data.minigames.MiniGame
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// -- Daily Challenge Selector (pure, deterministic) --

/**
 * Selects the daily challenge mini-game based on the date.
 * Pure function with no side effects. Same date + same games = same result.
 * Deterministic for support predictability. No server needed.
 */
object DailyChallengeSelector {

    /**
     * Selects a mini-game for the given date using deterministic modulo rotation.
     * @param date the date to select for
     * @param games the available mini-games
     * @return the selected MiniGame, or null if games list is empty
     */
    fun select(date: LocalDate, games: List<MiniGame>): MiniGame? {
        if (games.isEmpty()) return null
        val index = (date.toEpochDay() % games.size).let { if (it < 0) it + games.size else it }
        return games[index.toInt()]
    }
}

// -- Daily Challenge XP Rules (pure, unit-testable) --

/**
 * Pure XP calculation for daily challenge completion.
 * Separate from the mini-game's own XP -- this is a BONUS on top.
 *
 * Structure:
 * - Base: 20 XP for completing the challenge
 * - Perfect bonus: +15 XP if all rounds correct
 * - Streak bonus: +5 per consecutive day (capped at 30)
 * - Total max: 65 XP per day
 */
object DailyChallengeRules {
    const val baseXp = 20
    const val perfectBonus = 15
    const val streakBonusPerDay = 5
    const val maxStreakBonus = 30

    /**
     * Calculate streak bonus for a given streak day.
     * Day 1 = 0 bonus, Day 2 = 5, Day 3 = 10, ... capped at 30.
     */
    fun streakBonus(streakDay: Int): Int =
        minOf((streakDay - 1) * streakBonusPerDay, maxStreakBonus)

    /**
     * Calculate total daily challenge XP.
     * @param streakDay current streak day (1-indexed, 1 = first day)
     * @param perfect true if all rounds were answered correctly
     * @return total bonus XP to award
     */
    fun calculateXp(streakDay: Int, perfect: Boolean): Int =
        baseXp + (if (perfect) perfectBonus else 0) + streakBonus(streakDay)
}

// -- Daily Challenge State (pure, unit-testable) --

/**
 * Pure state logic for daily challenge completion and streak tracking.
 * No side effects -- all persistence is handled by the repository layer.
 */
object DailyChallengeState {
    private val ISO_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE

    /** Format a LocalDate to ISO date string (yyyy-MM-dd). */
    fun formatDate(date: LocalDate): String = date.format(ISO_FORMATTER)

    /** Parse an ISO date string to LocalDate. Returns null for invalid input. */
    fun parseDate(dateStr: String): LocalDate? =
        try { LocalDate.parse(dateStr, ISO_FORMATTER) } catch (_: Exception) { null }

    /**
     * Check if the daily challenge is already completed for a given date.
     * @param lastCompletedDate the ISO date string of the last completion, or null
     * @param today the date to check against
     * @return true if the challenge was already completed today
     */
    fun isCompleted(lastCompletedDate: String?, today: LocalDate): Boolean {
        if (lastCompletedDate == null) return false
        val lastDate = parseDate(lastCompletedDate) ?: return false
        return lastDate == today
    }

    /**
     * Calculate the new streak count after completing the daily challenge.
     * @param lastCompletedDate the ISO date string of the last completion, or null
     * @param today the current date
     * @return streak day count (0 = already completed today, 1 = first/after gap, 2+ = consecutive)
     */
    fun calculateStreak(lastCompletedDate: String?, today: LocalDate): Int {
        if (lastCompletedDate == null) return 1
        val lastDate = parseDate(lastCompletedDate) ?: return 1
        return when {
            lastDate == today -> 0 // already completed today
            lastDate == today.minusDays(1) -> 2 // consecutive day (yesterday + today)
            else -> 1 // gap detected, streak resets
        }
    }
}
