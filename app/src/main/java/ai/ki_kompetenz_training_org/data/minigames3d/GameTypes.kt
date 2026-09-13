package ai.ki_kompetenz_training_org.data.minigames3d

/**
 * Classification log entry for post-game mastery update.
 * Shared between adaptive quiz and (former) 3D arena modes.
 */
data class ClassifyLog(
    val domain: String,
    val correct: Boolean,
    val statement: LiteracyStatement,
)
