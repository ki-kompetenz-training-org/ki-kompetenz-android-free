package ai.ki_kompetenz_training_org.data.minigames3d

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Expansion gate: at least 12 statements per domain (6+ facts, 6+ risks),
 * 120+ total, complete bilingual content, correct flags, no duplicates.
 */
class LiteracyBankExpansionTest {

    @Test
    fun every_domain_has_at_least_12_statements() {
        for (d in LiteracyBank.DOMAINS) {
            val total = LiteracyBank.facts(d).size + LiteracyBank.risks(d).size
            assertTrue("domain '$d' has only $total statements (need >= 12)", total >= 12)
        }
    }

    @Test
    fun every_domain_has_at_least_6_facts() {
        for (d in LiteracyBank.DOMAINS) {
            assertTrue("domain '$d' has only ${LiteracyBank.facts(d).size} facts (need >= 6)", LiteracyBank.facts(d).size >= 6)
        }
    }

    @Test
    fun every_domain_has_at_least_6_risks() {
        for (d in LiteracyBank.DOMAINS) {
            assertTrue("domain '$d' has only ${LiteracyBank.risks(d).size} risks (need >= 6)", LiteracyBank.risks(d).size >= 6)
        }
    }

    @Test
    fun total_bank_exceeds_120_statements() {
        assertTrue("total ${LiteracyBank.totalFacts() + LiteracyBank.totalRisks()} (need >= 120)", LiteracyBank.totalFacts() + LiteracyBank.totalRisks() >= 120)
    }

    @Test
    fun all_statements_have_complete_bilingual_content() {
        for (s in all()) {
            assertTrue("blank textDe: '${s.textEn}'", s.textDe.isNotBlank())
            assertTrue("blank textEn: '${s.textDe}'", s.textEn.isNotBlank())
            assertTrue("blank explanationDe for '${s.textDe}'", s.explanationDe.isNotBlank())
            assertTrue("blank explanationEn for '${s.textEn}'", s.explanationEn.isNotBlank())
        }
    }

    @Test
    fun all_statements_belong_to_known_domain() {
        for (s in all()) {
            assertTrue("unknown domain '${s.domain}'", s.domain in LiteracyBank.DOMAINS)
        }
    }

    @Test
    fun isRisk_flags_match_source_map() {
        for (d in LiteracyBank.DOMAINS) {
            for (s in LiteracyBank.facts(d)) {
                assertTrue("fact flagged isRisk=true: '${s.textDe}'", !s.isRisk)
            }
            for (s in LiteracyBank.risks(d)) {
                assertTrue("risk flagged isRisk=false: '${s.textDe}'", s.isRisk)
            }
        }
    }

    @Test
    fun no_duplicate_statements() {
        val all = all()
        assertEquals("duplicate textDe present", all.size, all.map { it.textDe }.distinct().size)
        assertEquals("duplicate textEn present", all.size, all.map { it.textEn }.distinct().size)
    }

    private fun all(): List<LiteracyStatement> =
        LiteracyBank.DOMAINS.flatMap { LiteracyBank.facts(it) + LiteracyBank.risks(it) }
}
