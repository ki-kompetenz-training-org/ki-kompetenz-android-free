package ai.ki_kompetenz_training_org.data.minigames3d

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Deterministische Tests fuer die pure Mathe-Bibliothek [CompetencyMath]
 * (KI-Kompetenz-Index, KIKI). Keine Android-Abhaengigkeit, kein org.json,
 * keine Uhr, kein Zufall — alle Eingaben sind fix.
 *
 * Vgl. openspec/changes/add-kiki-competency-index/specs/competency-index/spec.md.
 */
class CompetencyMathTest {

    private val msPerDay = CompetencyMath.MS_PER_DAY.toLong()

    // ---- decayFactor ----

    @Test
    fun decayFactor_zeroDelta_isOne() {
        assertThat(CompetencyMath.decayFactor(0L)).isWithin(1e-9).of(1.0)
    }

    @Test
    fun decayFactor_afterTwoHalfLives_isQuarter() {
        assertThat(CompetencyMath.decayFactor(28L * msPerDay)).isWithin(0.001).of(0.25)
    }

    // ---- alpha ----

    @Test
    fun alpha_sameDay_hitsFloor() {
        assertThat(CompetencyMath.alpha(0L)).isWithin(1e-9).of(0.25)
    }

    @Test
    fun alpha_afterHalfLife_isAbout0_293() {
        assertThat(CompetencyMath.alpha(7L * msPerDay)).isWithin(0.005).of(0.293)
    }

    @Test
    fun alpha_afterTwoHalfLives_is0_75() {
        assertThat(CompetencyMath.alpha(28L * msPerDay)).isWithin(0.001).of(0.75)
    }

    // ---- effectiveMastery ----

    @Test
    fun effectiveMastery_decaysStoredValueAtReadTime() {
        // 0.8 * 2^(-28/14) = 0.8 * 0.25 = 0.2
        assertThat(CompetencyMath.effectiveMastery(0.8, 28L * msPerDay)).isWithin(0.001).of(0.2)
    }

    // ---- updateEwma ----

    @Test
    fun updateEwma_correctAfter7Days_isAbout0_593() {
        assertThat(CompetencyMath.updateEwma(0.6, 1, 7L * msPerDay)).isWithin(0.005).of(0.593)
    }

    @Test
    fun updateEwma_wrongAfter28Days_is0_05() {
        assertThat(CompetencyMath.updateEwma(0.8, 0, 28L * msPerDay)).isWithin(0.005).of(0.05)
    }

    @Test
    fun updateEwma_sameDayWrong_hitsAlphaFloor() {
        assertThat(CompetencyMath.updateEwma(1.0, 0, 0L)).isWithin(1e-9).of(0.75)
    }

    @Test
    fun updateEwma_sameDayCorrectFromZero_isQuarter() {
        assertThat(CompetencyMath.updateEwma(0.0, 1, 0L)).isWithin(1e-9).of(0.25)
    }

    // ---- coverage ----

    @Test
    fun coverage_zeroEvents_isZero() {
        assertThat(CompetencyMath.coverage(0)).isWithin(1e-9).of(0.0)
    }

    @Test
    fun coverage_twoEvents_is0_2() {
        assertThat(CompetencyMath.coverage(2)).isWithin(1e-9).of(0.2)
    }

    @Test
    fun coverage_twentyEvents_isAbout0_7143() {
        assertThat(CompetencyMath.coverage(20)).isWithin(0.001).of(0.7143)
    }

    // ---- domainScore ----

    @Test
    fun domainScore_fullMasteryLowCoverage_is20() {
        assertThat(CompetencyMath.domainScore(1.0, 2)).isEqualTo(20)
    }

    @Test
    fun domainScore_highMasteryHighCoverage_is57() {
        assertThat(CompetencyMath.domainScore(0.8, 20)).isEqualTo(57)
    }

    @Test
    fun domainScore_zeroEvents_is0RegardlessOfMastery() {
        assertThat(CompetencyMath.domainScore(0.9, 0)).isEqualTo(0)
    }

    // ---- kiki ----

    @Test
    fun kiki_allNineDomainsAt71_is71() {
        assertThat(CompetencyMath.kiki(List(9) { 71 })).isEqualTo(71)
    }

    @Test
    fun kiki_threeOfNineMastered_is23() {
        assertThat(CompetencyMath.kiki(listOf(70, 70, 70, 0, 0, 0, 0, 0, 0))).isEqualTo(23)
    }

    @Test
    fun kiki_emptyList_is0() {
        assertThat(CompetencyMath.kiki(emptyList())).isEqualTo(0)
    }

    // ---- encodeV2 / decodeV2 ----

    @Test
    fun encodeV2_containsAllFields() {
        val raw = CompetencyMath.encodeV2(0.75, 2, 123L)
        assertThat(raw).contains("0.75")
        assertThat(raw).contains("2")
        assertThat(raw).contains("123")
    }

    @Test
    fun decodeV2_roundTripReturnsOriginalTriple() {
        assertThat(CompetencyMath.decodeV2(CompetencyMath.encodeV2(0.75, 2, 123L)))
            .isEqualTo(Triple(0.75, 2, 123L))
    }

    @Test
    fun decodeV2_garbage_isNull() {
        assertThat(CompetencyMath.decodeV2("muell")).isNull()
    }

    // ---- Legacy-Format ----

    @Test
    fun parseLegacy_correctSlashTotal_isPair() {
        assertThat(CompetencyMath.parseLegacy("3/5")).isEqualTo(Pair(3, 5))
    }

    @Test
    fun parseLegacy_garbage_isNull() {
        assertThat(CompetencyMath.parseLegacy("x")).isNull()
    }

    @Test
    fun isLegacy_slashFormat_isTrue() {
        assertThat(CompetencyMath.isLegacy("0/3")).isTrue()
    }

    @Test
    fun isLegacy_v2Json_isFalse() {
        assertThat(CompetencyMath.isLegacy(CompetencyMath.encodeV2(0.75, 2, 123L))).isFalse()
    }
}
