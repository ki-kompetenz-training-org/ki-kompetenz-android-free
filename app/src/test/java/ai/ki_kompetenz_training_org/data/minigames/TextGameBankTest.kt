package ai.ki_kompetenz_training_org.data.minigames

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TextGameBankTest {

    @Test
    fun `bank has at least 30 rounds with unique ids`() {
        val rounds = TextGameBank.ALL
        assertTrue("expected >= 30 rounds, got ${rounds.size}", rounds.size >= 30)
        assertEquals(rounds.size, rounds.map { it.id }.toSet().size)
    }

    @Test
    fun `each round has both languages and non-empty explanations`() {
        TextGameBank.ALL.forEach { r ->
            assertTrue("${r.id} de text empty", r.textDe.isNotBlank())
            assertTrue("${r.id} en text empty", r.textEn.isNotBlank())
            assertTrue("${r.id} de explanation empty", r.explanationDe.isNotBlank())
            assertTrue("${r.id} en explanation empty", r.explanationEn.isNotBlank())
        }
    }

    @Test
    fun `every round has a valid difficulty`() {
        TextGameBank.ALL.forEach { r ->
            assertTrue(
                "${r.id} has no difficulty",
                setOf(Difficulty.BEGINNER, Difficulty.INTERMEDIATE, Difficulty.EXPERT).contains(r.difficulty),
            )
        }
    }

    @Test
    fun `AI and human distribution deviates from balance by at most 30 percentage points`() {
        val total = TextGameBank.ALL.size
        val aiCount = TextGameBank.ALL.count { it.isAi }
        val humanCount = total - aiCount
        val aiShare = aiCount.toFloat() / total
        assertTrue("AI share $aiShare deviates too far (AI=$aiCount human=$humanCount)", aiShare >= 0.2f)
        assertTrue("AI share $aiShare deviates too far (AI=$aiCount human=$humanCount)", aiShare <= 0.8f)
        assertTrue("should contain humans", humanCount > 0)
    }

    @Test
    fun `rounds convert to binary-choice MiniGameRounds`() {
        TextGameBank.ALL.forEach { r ->
            val m = r.toMiniGameRound()
            assertEquals(2, m.optionsDe.size)
            assertEquals(2, m.optionsEn.size)
            assertEquals(m.correctIndex, m.correctIndex.coerceIn(0, 1))
            assertTrue(if (r.isAi) m.correctIndex == 0 else m.correctIndex == 1)
            assertTrue(m.explanationDe.isNotBlank())
            assertTrue(m.explanationEn.isNotBlank())
        }
    }

    @Test
    fun `fake-or-real game is registered and uses the bank`() {
        val game = MiniGames.ALL.firstOrNull { it.kind == MiniGameKind.FAKE_OR_REAL }
        assertTrue("FAKE_OR_REAL game missing", game != null)
        assertTrue("session source should hold all bank rounds", game!!.rounds.size == TextGameBank.ALL.size)
        assertTrue(game.isFakeOrReal)
    }
}
