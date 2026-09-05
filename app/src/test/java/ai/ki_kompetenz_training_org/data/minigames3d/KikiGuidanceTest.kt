package ai.ki_kompetenz_training_org.data.minigames3d

import ai.ki_kompetenz_training_org.data.minigames.MiniGame
import ai.ki_kompetenz_training_org.data.minigames.MiniGameKind
import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Deterministische Tests fuer die pure Guidance-Logik [KikiGuidance]
 * (openspec/changes/add-kiki-guidance): Uebungsempfehlung aus dem
 * schwaechsten Domänen-Score + Verfalls-Hinweis bei Inaktivitaet.
 * Keine Android-Abhaengigkeit, keine Uhr, kein Zufall.
 */
class KikiGuidanceTest {

    private val day = KikiGuidance.MS_PER_DAY

    private fun scores(vararg pairs: Pair<String, Int>): Map<String, Int> = pairs.toMap()

    // ---- guidanceFor: Uebungsempfehlung ----

    @Test
    fun guidance_weakestDomainBelowThreshold_returnsRecommendation() {
        val g = KikiGuidance.guidanceFor(
            scores("Grundlagen der KI" to 80, "DSGVO" to 34, "Tools" to 55),
            now = 100L * day,
            snapshotCreatedAt = 99L * day,
        )
        assertThat(g).isNotNull()
        assertThat(g!!.type).isEqualTo(KikiGuidance.Type.PRACTICE)
        assertThat(g.domain).isEqualTo("DSGVO")
        assertThat(g.score).isEqualTo(34)
    }

    @Test
    fun guidance_allDomainsAtOrAboveThreshold_returnsNull() {
        val g = KikiGuidance.guidanceFor(
            scores("Grundlagen der KI" to 80, "DSGVO" to 60, "Tools" to 95),
            now = 100L * day,
            snapshotCreatedAt = 99L * day,
        )
        assertThat(g).isNull()
    }

    @Test
    fun guidance_emptyScores_returnsNull() {
        val g = KikiGuidance.guidanceFor(
            emptyMap(),
            now = 100L * day,
            snapshotCreatedAt = 99L * day,
        )
        assertThat(g).isNull()
    }

    @Test
    fun guidance_tieGoesToFirstListedDomain() {
        val g = KikiGuidance.guidanceFor(
            scores("A" to 40, "B" to 40),
            now = 100L * day,
            snapshotCreatedAt = 99L * day,
        )
        assertThat(g!!.domain).isEqualTo("A")
    }

    // ---- guidanceFor: Verfalls-Hinweis ----

    @Test
    fun guidance_oldSnapshotWithoutWeakDomain_returnsDecayHint() {
        val g = KikiGuidance.guidanceFor(
            scores("Grundlagen der KI" to 80, "Tools" to 75),
            now = 110L * day,
            snapshotCreatedAt = 100L * day, // 10 Tage alt
        )
        assertThat(g).isNotNull()
        assertThat(g!!.type).isEqualTo(KikiGuidance.Type.DECAY)
        assertThat(g.daysSince).isEqualTo(10)
        assertThat(g.domain).isNull()
    }

    @Test
    fun guidance_recentSnapshotWithoutWeakDomain_returnsNull() {
        val g = KikiGuidance.guidanceFor(
            scores("Grundlagen der KI" to 80),
            now = 103L * day,
            snapshotCreatedAt = 100L * day, // 3 Tage alt
        )
        assertThat(g).isNull()
    }

    @Test
    fun guidance_exactlySevenDays_returnsDecayHint() {
        val g = KikiGuidance.guidanceFor(
            scores("Grundlagen der KI" to 80),
            now = 107L * day,
            snapshotCreatedAt = 100L * day,
        )
        assertThat(g).isNotNull()
        assertThat(g!!.type).isEqualTo(KikiGuidance.Type.DECAY)
        assertThat(g.daysSince).isEqualTo(7)
    }

    @Test
    fun guidance_weakDomainWinsOverDecayHint() {
        val g = KikiGuidance.guidanceFor(
            scores("DSGVO" to 34),
            now = 120L * day,
            snapshotCreatedAt = 100L * day, // 20 Tage alt + schwache Domäne
        )
        assertThat(g!!.type).isEqualTo(KikiGuidance.Type.PRACTICE)
    }

    // ---- matchingGame ----

    private fun game(
        id: String,
        adaptive: Boolean,
        filter: List<String>? = null,
    ): MiniGame = MiniGame(
        id = id,
        emoji = "\uD83D\uDD75\uFE0F",
        titleDe = id, titleEn = id,
        descriptionDe = id, descriptionEn = id,
        rounds = emptyList(),
        kind = if (adaptive) MiniGameKind.ADAPTIVE_QUIZ else MiniGameKind.QUIZ,
        domainFilter = filter,
    )

    @Test
    fun matchingGame_domainInFilter_matchesFilteredAdaptiveGame() {
        val games = listOf(
            game("classic", adaptive = false),
            game("radar", adaptive = true, filter = listOf("EU AI Act", "DSGVO")),
            game("all", adaptive = true, filter = null),
        )
        assertThat(KikiGuidance.matchingGame("DSGVO", games)?.id).isEqualTo("radar")
    }

    @Test
    fun matchingGame_noFilterMatch_fallsBackToUnfilteredAdaptive() {
        val games = listOf(
            game("classic", adaptive = false),
            game("radar", adaptive = true, filter = listOf("EU AI Act")),
            game("all", adaptive = true, filter = null),
        )
        assertThat(KikiGuidance.matchingGame("Haftung", games)?.id).isEqualTo("all")
    }

    @Test
    fun matchingGame_firstFilteredMatchWins() {
        val games = listOf(
            game("radar", adaptive = true, filter = listOf("DSGVO")),
            game("fire", adaptive = true, filter = listOf("DSGVO", "Tools")),
        )
        assertThat(KikiGuidance.matchingGame("DSGVO", games)?.id).isEqualTo("radar")
    }

    @Test
    fun matchingGame_noAdaptiveGames_returnsNull() {
        val games = listOf(game("classic", adaptive = false))
        assertThat(KikiGuidance.matchingGame("DSGVO", games)).isNull()
    }
}
