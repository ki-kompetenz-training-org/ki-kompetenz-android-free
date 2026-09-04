package ai.ki_kompetenz_training_org.data.minigames3d

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import kotlin.math.abs

class MasteryTrackerTest {

    private lateinit var prefs: InMemoryPrefs

    @Before
    fun setUp() {
        prefs = InMemoryPrefs()
    }

    private fun masteryKey(domain: String) = "mg3d_mastery_$domain"

    /** Schreibt einen Rohwert direkt in die Prefs (Legacy oder v2), ohne MasteryTracker. */
    private fun storeRaw(domain: String, raw: String) {
        prefs.edit().putString(masteryKey(domain), raw).commit()
    }

    private fun readRaw(domain: String): String? = prefs.getString(masteryKey(domain), null)

    @Test
    fun empty_mastery_is_zero() {
        val t = MasteryTracker(prefs)
        val m = t.getMastery("Grundlagen der KI")
        assertEquals(0, m.correct)
        assertEquals(0, m.total)
    }

    @Test
    fun recordResult_increments() {
        val t = MasteryTracker(prefs)
        t.recordResult("Grundlagen der KI", true)
        t.recordResult("Grundlagen der KI", false)
        val m = t.getMastery("Grundlagen der KI")
        assertEquals(2, m.total)
        // Zwei Events am selben Tag: Alpha-Floor 0.25 -> m = 1.0 + 0.25 * (0 - 1.0) = 0.75
        assertTrue(abs(m.m - 0.75) < 0.01)
    }

    @Test
    fun selectDomain_never_seen_returns_some_domain() {
        val t = MasteryTracker(prefs)
        val d = t.selectDomain(rng = { 0.1 })
        assertTrue(LiteracyBank.DOMAINS.contains(d))
    }

    @Test
    fun weak_domain_is_selected_more_often() {
        val t = MasteryTracker(prefs)
        // Make Datenschutz weak (1/10), all other domains strong (9/10)
        for (d in LiteracyBank.DOMAINS) {
            for (i in 0 until 10) {
                val weak = d == "Datenschutz & DSGVO"
                t.recordResult(d, if (weak) i == 0 else i < 9)
            }
        }
        // Statistical check over many draws
        var weakCount = 0
        var strongCount = 0
        for (i in 0 until 2000) {
            val d = t.selectDomain()
            if (d == "Datenschutz & DSGVO") weakCount++
            if (d == "Grundlagen der KI") strongCount++
        }
        assertTrue("weak=$weakCount strong=$strongCount", weakCount > strongCount)
    }

    @Test
    fun weakDomains_returns_below_threshold() {
        val t = MasteryTracker(prefs)
        t.recordResult("Haftung & Compliance", false)
        t.recordResult("Haftung & Compliance", false)
        t.recordResult("Haftung & Compliance", false)
        val weak = t.weakDomains(threshold = 0.6)
        assertTrue(weak.any { it.domain == "Haftung & Compliance" })
    }

    @Test
    fun strong_domain_not_weak() {
        val t = MasteryTracker(prefs)
        for (i in 0 until 10) t.recordResult("Grundlagen der KI", true)
        val weak = t.weakDomains(threshold = 0.6)
        assertFalse(weak.any { it.domain == "Grundlagen der KI" })
    }

    @Test
    fun recordClassifications_bulk_and_total_games() {
        val t = MasteryTracker(prefs)
        val logs = listOf(
            ClassifyLog("Grundlagen der KI", true, LiteracyStatement("a", "a", "Grundlagen der KI", false)),
            ClassifyLog("Datenschutz & DSGVO", false, LiteracyStatement("b", "b", "Datenschutz & DSGVO", true)),
        )
        t.recordClassifications(logs)
        assertEquals(1, t.totalGames())
        assertEquals(1, t.getMastery("Grundlagen der KI").correct)
        assertEquals(0, t.getMastery("Datenschutz & DSGVO").correct)
    }

    @Test
    fun allMastery_returns_all_domains() {
        val t = MasteryTracker(prefs)
        val all = t.allMastery()
        assertEquals(LiteracyBank.DOMAINS.size, all.size)
    }

    @Test
    fun mastery_usage_applied_when_played() {
        val t = MasteryTracker(prefs)
        for (i in 0 until 12) t.recordResult("EU AI Act & Risikoklassen", i % 2 == 0)
        assertEquals(12, t.getMastery("EU AI Act & Risikoklassen").total)
    }

    // ---- v2: EWMA-Mastery mit Decay-at-Read, Legacy-Backfill, injizierbare Uhr ----

    @Test
    fun v2_json_written() {
        val t = MasteryTracker(prefs)
        t.recordResult("Grundlagen der KI", true)
        assertTrue(readRaw("Grundlagen der KI")!!.startsWith("{"))
    }

    @Test
    fun legacy_backfill_read() {
        storeRaw("Grundlagen der KI", "3/5")
        val t = MasteryTracker(prefs)
        val m = t.getMastery("Grundlagen der KI")
        assertTrue(abs(m.m - 0.6) < 0.001)
        assertEquals(5, m.total)
        // Abgeleitetes correct = round(m * total) — haelt alte Aufrufer (AdaptiveQuizViewModel) kompatibel.
        assertEquals(3, m.correct)
    }

    @Test
    fun legacy_backfill_persists() {
        storeRaw("Grundlagen der KI", "3/5")
        val t = MasteryTracker(prefs)
        t.getMastery("Grundlagen der KI")
        assertTrue(readRaw("Grundlagen der KI")!!.startsWith("{"))
    }

    @Test
    fun corrupt_defaults_zero() {
        storeRaw("Grundlagen der KI", "murks")
        val t = MasteryTracker(prefs)
        val m = t.getMastery("Grundlagen der KI")
        assertEquals(0.0, m.m, 1e-12)
        assertEquals(0, m.total)
    }

    @Test
    fun decay_at_read() {
        var now = 1_000_000L
        val t = MasteryTracker(prefs) { now }
        t.recordResult("Grundlagen der KI", correct = true)
        assertEquals(1.0, t.getMastery("Grundlagen der KI").m, 1e-9)
        now += 28L * 24 * 3600 * 1000 // 2 Halbwertszeiten
        val decayed = t.getMastery("Grundlagen der KI")
        assertTrue(abs(decayed.m - 0.25) < 0.01)
        // Erneutes Lesen ist stabil: Lesen ueberschreibt den Stored-Wert nicht.
        val again = t.getMastery("Grundlagen der KI")
        assertTrue(abs(again.m - 0.25) < 0.01)
    }

    @Test
    fun decay_then_blend_after_7d() {
        var now = 1_000_000L
        val t = MasteryTracker(prefs) { now }
        storeRaw("Datenschutz & DSGVO", "6/10")
        // Backfill: m = 6/10 = 0.6, n = 10, t = t0
        val backfilled = t.getMastery("Datenschutz & DSGVO")
        assertTrue(abs(backfilled.m - 0.6) < 0.001)
        assertEquals(10, backfilled.total)
        now += 7L * 24 * 3600 * 1000
        t.recordResult("Datenschutz & DSGVO", correct = true)
        val m = t.getMastery("Datenschutz & DSGVO")
        // m = 0.6*2^(-0.5) + (1 - 2^(-0.5))*(1 - 0.6*2^(-0.5)) ≈ 0.593
        assertTrue(abs(m.m - 0.593) < 0.01)
        // Legacy n=10 + 1 neues Event
        assertEquals(11, m.total)
    }
}
