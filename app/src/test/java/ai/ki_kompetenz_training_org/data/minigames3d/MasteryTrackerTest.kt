package ai.ki_kompetenz_training_org.data.minigames3d

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class MasteryTrackerTest {

    private lateinit var prefs: InMemoryPrefs

    @Before
    fun setUp() {
        prefs = InMemoryPrefs()
    }

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
        assertEquals(1, m.correct)
        assertEquals(2, m.total)
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
}
