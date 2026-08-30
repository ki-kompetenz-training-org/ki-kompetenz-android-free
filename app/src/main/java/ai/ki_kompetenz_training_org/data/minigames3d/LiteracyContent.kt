package ai.ki_kompetenz_training_org.data.minigames3d

/**
 * AI-literacy statement carried by an in-game entity.
 * Drawn from the question bank's 9 domains, individualized by [MasteryTracker].
 */
data class LiteracyStatement(
    val textDe: String,
    val textEn: String,
    val domain: String,
    val isRisk: Boolean,
    val explanationDe: String = "",
    val explanationEn: String = "",
) {
    fun text(lang: String): String = if (lang == "de") textDe else textEn
    fun explanation(lang: String): String = if (lang == "de") explanationDe else explanationEn
}

/**
 * Per-domain mastery tracking. Persists to SharedPreferences.
 * Weights domain selection toward weak areas for individualized learning.
 *
 * A domain with 2/10 correct gets higher weight than 8/10.
 * Domains never seen get maximum weight.
 */
data class DomainMastery(
    val domain: String,
    val correct: Int,
    val total: Int,
)

class MasteryTracker(private val prefs: android.content.SharedPreferences) {
    companion object {
        private const val KEY_PREFIX = "mg3d_mastery_"
        private const val KEY_TOTAL_GAMES = "mg3d_total_games"
        private const val MIN_ATTEMPTS_FOR_ADAPTATION = 3
    }

    private val domains = listOf(
        "Grundlagen der KI",
        "Datenschutz & DSGVO",
        "EU AI Act & Risikoklassen",
        "Haftung & Compliance",
        "KI-Governance im Unternehmen",
        "KI-Tools im Arbeitsalltag",
        "Transparenzpflichten",
        "Erlaubte & verbotene Nutzung",
        "KI im erweiterten Kontext",
    )

    fun getMastery(domain: String): DomainMastery {
        val key = KEY_PREFIX + domain
        val data = prefs.getString(key, null) ?: return DomainMastery(domain, 0, 0)
        val parts = data.split("/")
        return if (parts.size == 2) DomainMastery(domain, parts[0].toIntOrNull() ?: 0, parts[1].toIntOrNull() ?: 0)
        else DomainMastery(domain, 0, 0)
    }

    fun recordResult(domain: String, correct: Boolean) {
        val current = getMastery(domain)
        val newTotal = current.total + 1
        val newCorrect = current.correct + if (correct) 1 else 0
        prefs.edit().putString(KEY_PREFIX + domain, "$newCorrect/$newTotal").apply()
    }

    /** Record multiple classifications from one game session. */
    fun recordClassifications(logs: List<ClassifyLog>) {
        for (log in logs) recordResult(log.domain, log.correct)
        val total = prefs.getInt(KEY_TOTAL_GAMES, 0) + 1
        prefs.edit().putInt(KEY_TOTAL_GAMES, total).apply()
    }

    /**
     * Select a domain weighted by inverse mastery.
     * Domains with fewer correct/total ratio get higher weight.
     * Domains with < MIN_ATTEMPTS get extra weight (never-seen = highest).
     */
    fun selectDomain(rng: () -> Double = { Math.random() }): String {
        val weights = domains.associateWith { d ->
            val m = getMastery(d)
            when {
                m.total == 0 -> 3.0
                m.total < MIN_ATTEMPTS_FOR_ADAPTATION -> 2.0
                else -> {
                    val ratio = m.correct.toDouble() / m.total.toDouble()
                    (2.0 - 1.7 * ratio).coerceIn(0.3, 2.0)
                }
            }
        }
        val totalWeight = weights.values.sum()
        var roll = rng() * totalWeight
        for ((domain, weight) in weights) {
            roll -= weight
            if (roll <= 0) return domain
        }
        return domains.last()
    }

    /** Domains where the learner scored below threshold. */
    fun weakDomains(threshold: Double = 0.6): List<DomainMastery> =
        domains.mapNotNull { d ->
            val m = getMastery(d)
            if (m.total >= MIN_ATTEMPTS_FOR_ADAPTATION && m.correct.toDouble() / m.total.toDouble() < threshold) m else null
        }

    /** Summary of all domains for post-game screen. */
    fun allMastery(): List<DomainMastery> = domains.map { getMastery(it) }

    fun totalGames(): Int = prefs.getInt(KEY_TOTAL_GAMES, 0)
}
