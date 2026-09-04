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
 * Per-domain mastery tracking (v2: EWMA with decay-at-read). Persists to SharedPreferences.
 * Weights domain selection toward weak areas for individualized learning.
 *
 * [m] is the STORED EWMA mastery (updated only on answer events); callers reading
 * via [MasteryTracker.getMastery] receive the EFFECTIVE mastery, decayed at read
 * time (14-day half-life, see [CompetencyMath]).
 * [correct] is derived as round(m * total) to keep ratio-based callers compatible.
 */
data class DomainMastery(
    val domain: String,
    val m: Double,
    val total: Int,
    val lastEventMs: Long = 0L,
) {
    /** Derived correct count — keeps e.g. AdaptiveQuizViewModel's ratio logic compiling. */
    val correct: Int get() = Math.round(m * total).toInt().coerceIn(0, total)
}

class MasteryTracker(
    private val prefs: android.content.SharedPreferences,
    private val nowMs: () -> Long = System::currentTimeMillis,
) {
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

    /**
     * Reads the STORED (m, n, t) triple for a domain.
     * - v2 JSON is parsed directly.
     * - Legacy "correct/total" is backfilled (m = correct/total, n = total, t = now)
     *   and immediately persisted as v2.
     * - Corrupt or missing values default to (0.0, 0, 0L); corrupt values are
     *   overwritten on the next write.
     */
    private fun readStored(domain: String): Triple<Double, Int, Long> {
        val raw = prefs.getString(KEY_PREFIX + domain, null) ?: return Triple(0.0, 0, 0L)
        CompetencyMath.decodeV2(raw)?.let { return it }
        if (CompetencyMath.isLegacy(raw)) {
            val legacy = CompetencyMath.parseLegacy(raw) ?: return Triple(0.0, 0, 0L)
            val (correct, total) = legacy
            val t = nowMs()
            val m = if (total > 0) correct / total.toDouble() else 0.0
            // Backfill: sofort als v2 persistieren.
            prefs.edit().putString(KEY_PREFIX + domain, CompetencyMath.encodeV2(m, total, t)).apply()
            return Triple(m, total, t)
        }
        return Triple(0.0, 0, 0L)
    }

    /** Effective mastery with decay-at-read; does NOT write (except legacy backfill). */
    fun getMastery(domain: String): DomainMastery {
        val (m, n, t) = readStored(domain)
        val deltaMs = (nowMs() - t).coerceAtLeast(0L)
        return DomainMastery(domain, CompetencyMath.effectiveMastery(m, deltaMs), n, t)
    }

    /** Records one answer event: first decay the stored EWMA, then blend the answer in. */
    fun recordResult(domain: String, correct: Boolean) {
        val (m, n, t) = readStored(domain)
        val x = if (correct) 1 else 0
        val now = nowMs()
        val mNeu = if (n == 0) x.toDouble() else CompetencyMath.updateEwma(m, x, now - t)
        prefs.edit().putString(KEY_PREFIX + domain, CompetencyMath.encodeV2(mNeu, n + 1, now)).apply()
    }

    /** Record multiple classifications from one game session. */
    fun recordClassifications(logs: List<ClassifyLog>) {
        for (log in logs) recordResult(log.domain, log.correct)
        val total = prefs.getInt(KEY_TOTAL_GAMES, 0) + 1
        prefs.edit().putInt(KEY_TOTAL_GAMES, total).apply()
    }

    /**
     * Select a domain weighted by inverse (effective) mastery.
     * Domains with low effective EWMA mastery get higher weight.
     * Domains with < MIN_ATTEMPTS get extra weight (never-seen = highest).
     */
    fun selectDomain(rng: () -> Double = { Math.random() }): String {
        val weights = domains.associateWith { d ->
            val m = getMastery(d)
            when {
                m.total == 0 -> 3.0
                m.total < MIN_ATTEMPTS_FOR_ADAPTATION -> 2.0
                else -> (2.0 - 1.7 * m.m).coerceIn(0.3, 2.0)
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

    /** Domains where the learner's effective mastery is below threshold. */
    fun weakDomains(threshold: Double = 0.6): List<DomainMastery> =
        domains.mapNotNull { d ->
            val m = getMastery(d)
            if (m.total >= MIN_ATTEMPTS_FOR_ADAPTATION && m.m < threshold) m else null
        }

    /** Summary of all domains for post-game screen. */
    fun allMastery(): List<DomainMastery> = domains.map { getMastery(it) }

    fun totalGames(): Int = prefs.getInt(KEY_TOTAL_GAMES, 0)
}
