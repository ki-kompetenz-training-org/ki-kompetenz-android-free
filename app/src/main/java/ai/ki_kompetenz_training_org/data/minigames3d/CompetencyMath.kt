package ai.ki_kompetenz_training_org.data.minigames3d

import kotlin.math.max
import kotlin.math.pow

/**
 * Pure Mathe-Bibliothek fuer den KI-Kompetenz-Index (KIKI).
 *
 * Keine Android-Abhaengigkeit, kein org.json — reine JVM-Logik, damit alles
 * in Plain-Unit-Tests (testDebugUnitTest) testbar ist.
 *
 * Modell: EWMA-Domain-Mastery mit 14 Tagen Halbwertszeit.
 * Vgl. openspec/changes/add-kiki-competency-index/specs/competency-index/spec.md.
 */
object CompetencyMath {

    /** Halbwertszeit der Vergessenskurve in Tagen. */
    const val HALF_LIFE_DAYS = 14.0

    /** Untergrenze fuer alpha, damit Same-Day-Sessions das Mastery bewegen. */
    const val ALPHA_FLOOR = 0.25

    /** Sattigungsfaktor (Coverage) K = 8 Events. */
    const val COVERAGE_K = 8.0

    /** Millisekunden pro Tag. */
    const val MS_PER_DAY = 86_400_000.0

    /** Vergessensfaktor 2^(-delta_days / HALF_LIFE_DAYS). */
    fun decayFactor(deltaMs: Long): Double =
        2.0.pow(-deltaMs / (MS_PER_DAY * HALF_LIFE_DAYS))

    /** Mischfaktor alpha = max(1 - decay, ALPHA_FLOOR). */
    fun alpha(deltaMs: Long): Double =
        max(1.0 - decayFactor(deltaMs), ALPHA_FLOOR)

    /** Effektives Mastery zum Lesezeitpunkt (Stored-Wert wird NICHT ueberschrieben). */
    fun effectiveMastery(mStored: Double, deltaMs: Long): Double =
        mStored * decayFactor(deltaMs)

    /** EWMA-Update: erst dekayen, dann Antwort einmischen (x in {0, 1}). */
    fun updateEwma(mStored: Double, x: Int, deltaMs: Long): Double {
        val mDecayed = mStored * decayFactor(deltaMs)
        return mDecayed + alpha(deltaMs) * (x - mDecayed)
    }

    /** Coverage-Faktor n / (n + K). */
    fun coverage(n: Int): Double = n.toDouble() / (n + COVERAGE_K)

    /** Domain-Score 0..100 = round(100 * m * coverage(n)). */
    fun domainScore(m: Double, n: Int): Int =
        Math.round(100.0 * m * coverage(n)).toInt()

    /** KIKI = arithmetisches Mittel aller Domain-Scores. */
    fun kiki(scores: List<Int>): Int =
        if (scores.isEmpty()) 0 else Math.round(scores.average()).toInt()

    // ---- v2-Persistenzformat (hand-gerollt, KEIN org.json) ----

    /** Codiert Mastery als {"m":<m>,"n":<n>,"t":<t>}. */
    fun encodeV2(m: Double, n: Int, t: Long): String =
        "{\"m\":$m,\"n\":$n,\"t\":$t}"

    /** Dekodiert v2-JSON; null bei Parsefehlern oder wenn raw nicht mit '{' beginnt. */
    fun decodeV2(raw: String): Triple<Double, Int, Long>? {
        val s = raw.trim()
        if (!s.startsWith("{") || !s.endsWith("}")) return null
        val body = s.removePrefix("{").removeSuffix("}")
        if (body.isEmpty()) return null
        var m: Double? = null
        var n: Int? = null
        var t: Long? = null
        for (part in body.split(",")) {
            val kv = part.split(":")
            if (kv.size != 2) return null
            val key = kv[0].trim()
            val value = kv[1].trim()
            try {
                when (key) {
                    "\"m\"" -> m = value.toDouble()
                    "\"n\"" -> n = value.toInt()
                    "\"t\"" -> t = value.toLong()
                    else -> return null
                }
            } catch (_: NumberFormatException) {
                return null
            }
        }
        val mm = m ?: return null
        val nn = n ?: return null
        val tt = t ?: return null
        return Triple(mm, nn, tt)
    }

    /** true, wenn raw ein Legacy-"correct/total"-String (und kein v2-JSON) ist. */
    fun isLegacy(raw: String): Boolean =
        raw.contains('/') && !raw.trim().startsWith("{")

    /** Parst Legacy-"correct/total"; null, wenn nicht exakt 2 parsebare Ints. */
    fun parseLegacy(raw: String): Pair<Int, Int>? {
        val parts = raw.split('/')
        if (parts.size != 2) return null
        val correct = parts[0].trim().toIntOrNull() ?: return null
        val total = parts[1].trim().toIntOrNull() ?: return null
        return Pair(correct, total)
    }
}
