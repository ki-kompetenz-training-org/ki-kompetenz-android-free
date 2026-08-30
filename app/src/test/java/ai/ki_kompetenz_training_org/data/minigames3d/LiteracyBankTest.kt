package ai.ki_kompetenz_training_org.data.minigames3d

import org.junit.Assert.*
import org.junit.Test

class LiteracyBankTest {

    @Test
    fun every_domain_has_facts() {
        for (d in LiteracyBank.DOMAINS) {
            assertTrue(LiteracyBank.facts(d).isNotEmpty())
        }
    }

    @Test
    fun every_domain_has_risks() {
        for (d in LiteracyBank.DOMAINS) {
            assertTrue(LiteracyBank.risks(d).isNotEmpty())
        }
    }

    @Test
    fun facts_are_flagged_not_risk() {
        for (d in LiteracyBank.DOMAINS) {
            for (s in LiteracyBank.facts(d)) {
                assertFalse(s.isRisk)
            }
        }
    }

    @Test
    fun risks_are_flagged_risk() {
        for (d in LiteracyBank.DOMAINS) {
            for (s in LiteracyBank.risks(d)) {
                assertTrue(s.isRisk)
            }
        }
    }

    @Test
    fun statements_are_bilingual() {
        for (d in LiteracyBank.DOMAINS) {
            for (s in LiteracyBank.facts(d) + LiteracyBank.risks(d)) {
                assertTrue(s.textDe.isNotBlank())
                assertTrue(s.textEn.isNotBlank())
            }
        }
    }

    @Test
    fun total_content_is_substantial() {
        assertTrue(LiteracyBank.totalFacts() >= 9)
        assertTrue(LiteracyBank.totalRisks() >= 9)
    }

    @Test
    fun master_bank_content_provides_facts() {
        val provider = MasteryBankContent(null)
        val f = provider.randomFact(rng = { 0.7 })
        assertFalse(f.isRisk)
        assertTrue(f.domain in LiteracyBank.DOMAINS)
    }

    @Test
    fun master_bank_content_provides_risks() {
        val provider = MasteryBankContent(null)
        val r = provider.randomRisk(rng = { 0.3 })
        assertTrue(r.isRisk)
        assertTrue(r.domain in LiteracyBank.DOMAINS)
    }

    @Test
    fun determinism_with_same_seed() {
        val provider = MasteryBankContent(null)
        // Same rng sequence -> same picks (for a fixed pool)
        val r1 = provider.randomRisk(rng = { 0.99 })
        val r2 = provider.randomRisk(rng = { 0.99 })
        assertEquals(r1, r2)
    }
}
