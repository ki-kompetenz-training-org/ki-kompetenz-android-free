package ai.ki_kompetenz_training_org.data.minigames3d

/**
 * Provides AI-literacy statements for in-game entities.
 * Implementations may weight domains by the learner's mastery profile
 * (individualization): weak domains surface more often.
 */
interface LiteracyContentProvider {
    /** A statement the learner should identify as a TRUE AI fact. */
    fun randomFact(rng: () -> Double = { Math.random() }): LiteracyStatement
    /** A statement the learner should identify as an AI RISK / false claim. */
    fun randomRisk(rng: () -> Double = { Math.random() }): LiteracyStatement
}
