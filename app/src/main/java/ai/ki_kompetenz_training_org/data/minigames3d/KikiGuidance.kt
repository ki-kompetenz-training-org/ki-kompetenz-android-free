package ai.ki_kompetenz_training_org.data.minigames3d

import ai.ki_kompetenz_training_org.data.minigames.MiniGame
import ai.ki_kompetenz_training_org.data.minigames.MiniGameKind
import ai.ki_kompetenz_training_org.ui.gamification.RADAR_WEAK_THRESHOLD

/**
 * Pure Guidance-Logik (openspec add-kiki-guidance): leitet aus dem letzten
 * KIKI-Snapshot eine konkrete naechste Handlung ab.
 *
 * - PRACTICE: schwächste Domäne unter Schwellwert → passende Übung vorschlagen
 *   (Testing Effect / Desirable Difficulties: gezielter Abruf üben).
 * - DECAY: letzte Session >= 7 Tage her → Auffrischungs-Impuls
 *   (Spaced Practice; ~halbe EWMA-Half-life, m_eff um ~30 % verfallen).
 *
 * Keine Android-Abhängigkeit, kein Zufall — `now` wird injiziert.
 */
object KikiGuidance {

    /** Schwelle ab der eine Domäne als übungswürdig gilt (= Radar "schwach"). */
    const val GUIDANCE_THRESHOLD = RADAR_WEAK_THRESHOLD

    /** Tage seit letzter Session ab denen der Verfalls-Hinweis erscheint. */
    const val DECAY_HINT_DAYS = 7L

    const val MS_PER_DAY = 24L * 60L * 60L * 1000L

    enum class Type { PRACTICE, DECAY }

    /**
     * Eine abgeleitete Handlungsempfehlung.
     * PRACTICE: [domain] + [score] gesetzt, [daysSince] null.
     * DECAY: nur [daysSince] gesetzt.
     */
    data class Guidance(
        val type: Type,
        val domain: String? = null,
        val score: Int = 0,
        val daysSince: Int? = null,
    )

    /**
     * Leitet aus den per-Domain-Scores des letzten Snapshots die Empfehlung ab.
     *
     * @param scores Domäne → Score (0..100); leer wenn kein Snapshot existiert
     * @param now aktuelle Zeit in ms (injiziert, deterministisch testbar)
     * @param snapshotCreatedAt Erstellungszeit des letzten Snapshots in ms
     */
    fun guidanceFor(
        scores: Map<String, Int>,
        now: Long,
        snapshotCreatedAt: Long,
    ): Guidance? {
        val weakest = scores.minByOrNull { it.value }
        if (weakest != null && weakest.value < GUIDANCE_THRESHOLD) {
            return Guidance(
                type = Type.PRACTICE,
                domain = weakest.key,
                score = weakest.value,
            )
        }
        val days = ((now - snapshotCreatedAt) / MS_PER_DAY).toInt()
        if (scores.isNotEmpty() && days >= DECAY_HINT_DAYS) {
            return Guidance(type = Type.DECAY, daysSince = days)
        }
        return null
    }

    /**
     * Ordnet der Domäne ein Übungsspiel zu: erstes adaptives Spiel, dessen
     * [MiniGame.domainFilter] die Domäne enthält; Fallback ist das Filter-lose
     * adaptive Spiel (KI-Detektiv deckt alle Domänen ab). Null wenn es keine
     * adaptiven Spiele gibt.
     */
    fun matchingGame(domain: String, games: List<MiniGame>): MiniGame? {
        val adaptive = games.filter { it.kind == MiniGameKind.ADAPTIVE_QUIZ }
        return adaptive.firstOrNull { it.domainFilter?.contains(domain) == true }
            ?: adaptive.firstOrNull { it.domainFilter == null }
    }
}
