package ai.ki_kompetenz_training_org.ui.kibot

import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * KiBot growth stages tied to user level.
 * As the user learns, KiBot evolves visually.
 */
enum class GrowthStage {
    NEONATE,   // Level 1–2: Small, simple, one antenna
    LEARNER,   // Level 3–5: Bigger, two antennae, arms
    THINKER,   // Level 6–9: Brain dome, brighter eyes
    EXPERT,    // Level 10+: Full body, boosters, orbital ring
    ;

    companion object {
        fun forLevel(level: Int): GrowthStage = when (level) {
            in 1..2 -> NEONATE
            in 3..5 -> LEARNER
            in 6..9 -> THINKER
            else -> EXPERT
        }
    }
}

/**
 * KiBot emotional states driven by user behavior.
 */
enum class EmotionalState {
    IDLE,        // Default: gentle bobbing
    HAPPY,       // Check-in, correct answer
    CELEBRATING, // Streak milestone, perfect game
    CONFUSED,    // Wrong answer, API error
    SLEEPY,      // Not checked in 2+ days
    THRILLED,    // Premium unlocked, big milestone
    ;

    companion object {
        fun baseline(streak: Int, daysSinceCheckIn: Int, checkedInToday: Boolean): EmotionalState =
            when {
                daysSinceCheckIn >= 2 && !checkedInToday -> SLEEPY
                else -> IDLE
            }
    }
}

/**
 * Full KiBot state derived from gamification data.
 * No new DB tables — state is computed from existing GamificationRepository.
 */
data class KiBotState(
    val growthStage: GrowthStage,
    val emotionalBaseline: EmotionalState,
    val xp: Int,
    val xpIntoLevel: Int,
    val xpNeeded: Int,
    val level: Int,
    val streak: Int,
    val checkedInToday: Boolean,
) {
    companion object {
        /**
         * Derive KiBot state from gamification data.
         * @param daysSinceCheckIn Days since last check-in (0 = today).
         */
        fun from(
            level: Int,
            xp: Int,
            xpIntoLevel: Int,
            xpNeeded: Int,
            streak: Int,
            daysSinceCheckIn: Int,
            checkedInToday: Boolean,
        ): KiBotState = KiBotState(
            growthStage = GrowthStage.forLevel(level),
            emotionalBaseline = EmotionalState.baseline(streak, daysSinceCheckIn, checkedInToday),
            xp = xp,
            xpIntoLevel = xpIntoLevel,
            xpNeeded = xpNeeded,
            level = level,
            streak = streak,
            checkedInToday = checkedInToday,
        )
    }
}

/**
 * Compute days since last check-in from the stored day string.
 */
fun daysSinceLastCheckIn(lastCheckInDay: String?): Int {
    if (lastCheckInDay == null) return Int.MAX_VALUE
    return try {
        val last = LocalDate.parse(lastCheckInDay, DateTimeFormatter.ISO_LOCAL_DATE)
        val today = LocalDate.now()
        java.time.Duration.between(last.atStartOfDay(), today.atStartOfDay()).toInt().coerceAtLeast(0)
    } catch (_: Exception) {
        Int.MAX_VALUE
    }
}
