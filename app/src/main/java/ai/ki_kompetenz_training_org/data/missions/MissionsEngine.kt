package ai.ki_kompetenz_training_org.data.missions

/** Learning-event sources that can advance weekly missions. */
enum class MissionMetric {
    QUIZ_PLAYED,
    QUIZ_GOOD,
    MINIGAME_PLAYED,
    SRS_CARDS,
    LESSON_COMPLETED,
    DAILY_COMPLETED,
}

/** A weekly mission: [id] is stable across weeks, [metric] is the progress source. */
data class MissionTemplate(
    val id: String,
    val metric: MissionMetric,
    val target: Int,
)

/** Pure, deterministic weekly-mission logic — no Android dependencies. */
object MissionsEngine {

    const val rewardXp = 40
    const val allBonusXp = 50
    const val selectedPerWeek = 3

    val POOL: List<MissionTemplate> = listOf(
        MissionTemplate("quiz_play", MissionMetric.QUIZ_PLAYED, 3),
        MissionTemplate("quiz_good", MissionMetric.QUIZ_GOOD, 2),
        MissionTemplate("minigame_play", MissionMetric.MINIGAME_PLAYED, 4),
        MissionTemplate("srs_cards", MissionMetric.SRS_CARDS, 20),
        MissionTemplate("lessons", MissionMetric.LESSON_COMPLETED, 2),
        MissionTemplate("daily", MissionMetric.DAILY_COMPLETED, 3),
    )

    /**
     * Deterministically selects [selectedPerWeek] missions from [POOL] for an ISO week.
     * The same week always yields the same selection for all users (String.hashCode
     * is specified and stable across JVM processes).
     */
    fun selectForWeek(week: String): List<MissionTemplate> {
        var seed = week.hashCode() and Int.MAX_VALUE
        val remaining = POOL.toMutableList()
        val selected = mutableListOf<MissionTemplate>()
        while (selected.size < selectedPerWeek && remaining.isNotEmpty()) {
            seed = (seed * 1103515245L + 12345L).toInt() and Int.MAX_VALUE
            val index = seed % remaining.size
            selected += remaining.removeAt(index)
        }
        return selected
    }

    /** Advances [progress] by [amount], clamped to [target]. */
    fun progressAfter(progress: Int, target: Int, amount: Int): Int =
        (progress + amount).coerceAtMost(target)

    fun isCompleted(progress: Int, target: Int): Boolean = progress >= target

    /** Weekly bonus XP when all 3 selected missions are completed. */
    fun bonusForCompleted(totalCompleted: Int): Int =
        if (totalCompleted >= selectedPerWeek) allBonusXp else 0
}
