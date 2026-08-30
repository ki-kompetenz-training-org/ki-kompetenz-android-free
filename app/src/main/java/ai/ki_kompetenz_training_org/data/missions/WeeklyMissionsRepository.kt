package ai.ki_kompetenz_training_org.data.missions

import ai.ki_kompetenz_training_org.data.repo.GamificationRepository
import ai.ki_kompetenz_training_org.data.repo.GamificationRules
import android.content.SharedPreferences
import java.time.LocalDate

/** Read model for a single ISO week's missions. */
data class WeeklyMissionsState(
    val week: String,
    val progress: Map<String, Int>,
    val completed: Set<String>,
    val bonusAwarded: Boolean,
)

/**
 * Weekly missions backed by SharedPreferences ("kikompetenz_missions").
 * Deterministic 3-of-6 selection per ISO week; progress advanced by learning
 * events; 40 XP per mission and 50 XP bonus once per week. Defensive: record()
 * never throws and can never block the learning flow.
 */
class WeeklyMissionsRepository(
    private val prefs: SharedPreferences?,
    private val gamification: GamificationRepository? = null,
) {
    companion object {
        const val KEY_WEEK = "missions_week"
        const val KEY_BONUS = "missions_bonus"
        fun progressKey(id: String) = "mission_progress_$id"
        fun completedKey(id: String) = "mission_completed_$id"
    }

    fun currentWeek(today: LocalDate = LocalDate.now()): String = GamificationRules.isoWeekKey(today)

    fun selectedFor(week: String): List<MissionTemplate> = MissionsEngine.selectForWeek(week)

    /** State for the current ISO week; progress resets automatically on week rollover. */
    fun current(today: LocalDate = LocalDate.now()): WeeklyMissionsState {
        val week = currentWeek(today)
        val stored = prefs?.getString(KEY_WEEK, null)
        if (stored != week) return WeeklyMissionsState(week, emptyMap(), emptySet(), false)
        val progress = MissionsEngine.POOL.associate { t ->
            t.id to (prefs?.getInt(progressKey(t.id), 0) ?: 0)
        }
        val completed = MissionsEngine.POOL.mapNotNull { t ->
            t.id.takeIf { (prefs?.getBoolean(completedKey(t.id), false) == true) }
        }.toSet()
        return WeeklyMissionsState(
            week = week,
            progress = progress,
            completed = completed,
            bonusAwarded = prefs?.getBoolean(KEY_BONUS, false) == true,
        )
    }

    /**
     * Records a learning event, advancing every selected mission of that metric.
     * Awards mission XP (+ weekly bonus) exactly once per mission/week.
     */
    suspend fun record(metric: MissionMetric, amount: Int = 1, today: LocalDate = LocalDate.now()) {
        val p = prefs ?: return
        runCatching {
            val state = current(today)
            val selected = selectedFor(state.week)
            val applicable = selected.filter { it.metric == metric }
            if (applicable.isEmpty()) return
            val editor = p.edit()
            val completedNow = state.completed.toMutableSet()
            for (template in applicable) {
                val key = progressKey(template.id)
                if (state.completed.contains(template.id)) {
                    editor.putInt(key, template.target)
                    continue
                }
                val progress = MissionsEngine.progressAfter(
                    state.progress[template.id] ?: 0,
                    template.target,
                    amount,
                )
                editor.putInt(key, progress)
                if (MissionsEngine.isCompleted(progress, template.target)) {
                    editor.putBoolean(completedKey(template.id), true)
                    completedNow += template.id
                    gamification?.addXp(MissionsEngine.rewardXp)
                }
            }
            if (completedNow.size >= MissionsEngine.selectedPerWeek && !state.bonusAwarded) {
                editor.putBoolean(KEY_BONUS, true)
                gamification?.addXp(MissionsEngine.allBonusXp)
            }
            editor.putString(KEY_WEEK, state.week)
            editor.apply()
        }
    }
}
