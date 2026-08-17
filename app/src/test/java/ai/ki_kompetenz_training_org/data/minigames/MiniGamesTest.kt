package ai.ki_kompetenz_training_org.data.minigames

import org.junit.Assert.*
import org.junit.Test

class MiniGamesTest {

    // ── Data integrity tests ──

    // Free Edition: alle 8 Spiele sind frei verfügbar (Premium-Inhalte gibt es
    // ausschließlich in der Google-Play-Version ai.ki_kompetenz_training_org).

    @Test
    fun `all 8 free games are registered`() {
        assertEquals(8, MiniGames.ALL.size)
    }

    @Test
    fun `all games are free in the free edition`() {
        assertEquals(8, MiniGames.FREE.size)
        assertEquals(0, MiniGames.PREMIUM.size)
        MiniGames.ALL.forEach { game ->
            assertFalse("Game ${game.id} should be free", game.premium)
        }
    }

    @Test
    fun `no duplicate game IDs`() {
        val ids = MiniGames.ALL.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
    }

    @Test
    fun `every game has at least 3 rounds`() {
        MiniGames.ALL.forEach { game ->
            assertTrue("Game ${game.id} has only ${game.rounds.size} rounds (min 3)", game.rounds.size >= 3)
        }
    }

    @Test
    fun `every round has valid correctIndex within options range`() {
        MiniGames.ALL.forEach { game ->
            game.rounds.forEachIndexed { i, round ->
                assertTrue(
                    "Game ${game.id} round $i: correctIndex ${round.correctIndex} >= options size ${round.optionsDe.size}",
                    round.correctIndex >= 0 && round.correctIndex < round.optionsDe.size
                )
                assertEquals(
                    "Game ${game.id} round $i: de/en options count mismatch",
                    round.optionsDe.size, round.optionsEn.size
                )
            }
        }
    }

    @Test
    fun `all prompts and explanations are non-empty`() {
        MiniGames.ALL.forEach { game ->
            game.rounds.forEachIndexed { i, round ->
                assertTrue("Game ${game.id} round $i: empty promptDe", round.promptDe.isNotBlank())
                assertTrue("Game ${game.id} round $i: empty promptEn", round.promptEn.isNotBlank())
                assertTrue("Game ${game.id} round $i: empty explanationDe", round.explanationDe.isNotBlank())
                assertTrue("Game ${game.id} round $i: empty explanationEn", round.explanationEn.isNotBlank())
            }
        }
    }

    @Test
    fun `total rounds across all games is at least 70`() {
        val total = MiniGames.ALL.sumOf { it.rounds.size }
        assertTrue("Expected >= 60 total rounds, got $total", total >= 70)
    }

    // ── Language helper tests ──

    @Test
    fun `round prompt returns German for de`() {
        val game = MiniGames.ALL.first()
        val round = game.rounds.first()
        assertEquals(round.promptDe, round.prompt("de"))
    }

    @Test
    fun `round prompt returns English for en`() {
        val game = MiniGames.ALL.first()
        val round = game.rounds.first()
        assertEquals(round.promptEn, round.prompt("en"))
    }

    @Test
    fun `round prompt returns English for unknown lang`() {
        val game = MiniGames.ALL.first()
        val round = game.rounds.first()
        assertEquals(round.promptEn, round.prompt("fr"))
    }

    @Test
    fun `game title returns German for de`() {
        val game = MiniGames.ALL.first()
        assertEquals(game.titleDe, game.title("de"))
    }

    @Test
    fun `game description returns English for en`() {
        val game = MiniGames.ALL.first()
        assertEquals(game.descriptionEn, game.description("en"))
    }

    // ── Difficulty tests ──

    @Test
    fun `all games use a known difficulty level`() {
        val valid = setOf(Difficulty.BEGINNER, Difficulty.INTERMEDIATE, Difficulty.EXPERT)
        MiniGames.ALL.forEach { game ->
            assertTrue("Game ${game.id} has unknown difficulty ${game.difficulty}", game.difficulty in valid)
        }
        assertTrue("Expected at least one beginner game", MiniGames.ALL.any { it.difficulty == Difficulty.BEGINNER })
    }

    @Test
    fun `difficulty XP multipliers are ordered`() {
        assertTrue(Difficulty.BEGINNER.xpMultiplier < Difficulty.INTERMEDIATE.xpMultiplier)
        assertTrue(Difficulty.INTERMEDIATE.xpMultiplier < Difficulty.EXPERT.xpMultiplier)
    }

    @Test
    fun `byDifficulty returns only matching games`() {
        val beginnerGames = MiniGames.byDifficulty(Difficulty.BEGINNER)
        beginnerGames.forEach { game ->
            assertEquals(Difficulty.BEGINNER, game.difficulty)
        }
        assertTrue("Expected some beginner games", beginnerGames.isNotEmpty())
    }

    @Test
    fun `byDifficulty beginner + intermediate + expert = all`() {
        val byDiff = listOf(
            MiniGames.byDifficulty(Difficulty.BEGINNER),
            MiniGames.byDifficulty(Difficulty.INTERMEDIATE),
            MiniGames.byDifficulty(Difficulty.EXPERT),
        ).flatten().toSet()
        assertEquals(MiniGames.ALL.toSet(), byDiff)
    }

    @Test
    fun `difficulty displayNameDe and displayNameEn are non-empty`() {
        Difficulty.entries.forEach { d ->
            assertTrue("${d.name} displayNameDe is empty", d.displayNameDe.isNotBlank())
            assertTrue("${d.name} displayNameEn is empty", d.displayNameEn.isNotBlank())
        }
    }

    // ── Lookup tests ──

    @Test
    fun `byId returns correct game`() {
        val expected = MiniGames.ALL.first()
        val found = MiniGames.byId(expected.id)
        assertNotNull(found)
        assertEquals(expected.id, found!!.id)
    }

    @Test
    fun `byId returns null for unknown ID`() {
        assertNull(MiniGames.byId("nonexistent_game"))
    }

    // ── Random tests ──

    @Test
    fun `random freeOnly returns only free games`() {
        repeat(20) {
            val game = MiniGames.random(freeOnly = true)
            if (game != null) {
                assertFalse(game.premium)
            }
        }
    }

    @Test
    fun `random premiumOnly is always null in free edition`() {
        repeat(20) {
            assertNull(MiniGames.random(premiumOnly = true))
        }
    }

    // ── Content quality tests ──

    @Test
    fun `all game titles are unique`() {
        val titlesDe = MiniGames.ALL.map { it.titleDe }
        assertEquals(titlesDe.size, titlesDe.toSet().size)
    }

    @Test
    fun `all games have non-empty emoji`() {
        MiniGames.ALL.forEach { game ->
            assertTrue("Game ${game.id} has empty emoji", game.emoji.isNotBlank())
        }
    }

    @Test
    fun `all games have bilingual descriptions`() {
        MiniGames.ALL.forEach { game ->
            assertTrue("Game ${game.id}: empty descriptionDe", game.descriptionDe.isNotBlank())
            assertTrue("Game ${game.id}: empty descriptionEn", game.descriptionEn.isNotBlank())
        }
    }

    @Test
    fun `options have at least 2 choices per round`() {
        MiniGames.ALL.forEach { game ->
            game.rounds.forEachIndexed { i, round ->
                assertTrue(
                    "Game ${game.id} round $i: only ${round.optionsDe.size} options",
                    round.optionsDe.size >= 2
                )
            }
        }
    }

    @Test
    fun `specific known games exist`() {
        assertNotNull(MiniGames.byId("human_or_ai"))
        assertNotNull(MiniGames.byId("fact_or_hallucination"))
        assertNotNull(MiniGames.byId("high_risk_blitz"))
        assertNotNull(MiniGames.byId("agent_ampel"))
        assertNotNull(MiniGames.byId("shadow_ai_check"))
        assertNotNull(MiniGames.byId("prompt_profis"))
        assertNotNull(MiniGames.byId("bias_spotter"))
        assertNotNull(MiniGames.byId("dsgvo_check"))
    }

    @Test
    fun `premium games do not exist in the free edition`() {
        // Premium-Inhalte gibt es ausschließlich in der Google-Play-Version
        // (ai.ki_kompetenz_training_org).
        assertNull(MiniGames.byId("audit_trainer"))
        assertNull(MiniGames.byId("agent_simulator"))
        assertNull(MiniGames.byId("strategie_berater"))
        assertNull(MiniGames.byId("ki_schutzschild"))
        assertNull(MiniGames.byId("ki_sprachfuehrer"))
        assertNull(MiniGames.byId("ki_zielscheibe"))
        assertNull(MiniGames.byId("ki_vertrag"))
        assertNull(MiniGames.byId("change_manager"))
    }

    @Test
    fun `expert games are all premium`() {
        MiniGames.byDifficulty(Difficulty.EXPERT).forEach { game ->
            assertTrue("Expert game ${game.id} should be premium", game.premium)
        }
    }
}
