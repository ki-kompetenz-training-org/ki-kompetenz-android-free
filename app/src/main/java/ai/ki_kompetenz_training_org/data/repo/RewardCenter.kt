package ai.ki_kompetenz_training_org.data.repo

import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Transient reward events (level-ups, badge unlocks).
 *
 * Rules (ux-polish-pack / reward-moments):
 * - Events are one-shot and transient; they never persist UI state.
 * - Latest wins: a new event replaces a not-yet-shown one (no stacking).
 * - Screens show the dialog only at result moments / home - never during an
 *   active round.
 */
sealed interface RewardEvent {
    data class LevelUp(val newLevel: Int) : RewardEvent
    data class BadgeUnlocked(val badgeId: String) : RewardEvent
}

/** Central reward event bus with set-and-consume semantics (latest wins). */
class RewardCenter {
    private val _pending = MutableStateFlow<RewardEvent?>(null)

    /** The currently pending reward, or null. Consumed via [consume]. */
    val pending: StateFlow<RewardEvent?> = _pending

    /** Make an event the pending one (replaces any previous pending event). */
    fun emit(event: RewardEvent) {
        _pending.value = event
    }

    /** Mark the pending event as shown. */
    fun consume() {
        _pending.value = null
    }
}

/**
 * Diffs XP across observe() emissions and produces at most one
 * [RewardEvent.LevelUp] per level increase. The first emission only sets
 * the baseline (no celebration for the level the user already has).
 */
class LevelUpTracker(
    private val levelForXp: (Int) -> Int = GamificationRules::levelForXp,
) {
    private var lastLevel = 0

    fun onNext(xp: Int): RewardEvent.LevelUp? {
        val level = levelForXp(xp)
        val previous = lastLevel
        lastLevel = level
        return if (previous in 1 until level) RewardEvent.LevelUp(level) else null
    }
}

/**
 * Emits a badge celebration exactly once per badge. The set of celebrated
 * badges is persisted, so badges never celebrate again after an app restart.
 */
class BadgeCelebrationTracker(private val prefs: SharedPreferences) {

    fun onNext(badgeIds: List<String>): List<RewardEvent.BadgeUnlocked> {
        val celebrated = prefs.getStringSet(KEY, emptySet())?.toMutableSet() ?: mutableSetOf()
        val fresh = badgeIds.filter { it !in celebrated }
        if (fresh.isNotEmpty()) {
            celebrated.addAll(fresh)
            prefs.edit().putStringSet(KEY, celebrated).apply()
        }
        return fresh.map { RewardEvent.BadgeUnlocked(it) }
    }

    companion object {
        const val KEY = "celebrated_badges"
    }
}

/** Pure formatting helpers for reward UI. */
object RewardFormat {
    fun xpGain(amount: Int): String = "+$amount XP"

    /** Duration in ms for the check-in emphasis; 0 = instant (animations off). */
    fun checkInAnimationMs(animatorDurationScale: Float): Int = (320 * animatorDurationScale).toInt()
}
