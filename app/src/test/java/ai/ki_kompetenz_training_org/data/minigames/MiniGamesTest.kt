package ai.ki_kompetenz_training_org.data.minigames

import org.junit.Assert.*
import org.junit.Test

class MiniGamesTest {

    // ── Data integrity tests ──

    @Test
    fun `all 12 games are registered with 3 arena games`() {
        assertEquals(12, MiniGames.ALL.size)
        assertEquals(3, MiniGames.ARENA3D.size)
    }

    @Test
    fun `all games are free`() {
        assertEquals(12, MiniGames.FREE.size)
        assertEquals(0, MiniGames.PREMIUM.size)
    }

    @Test
    fun `free games are not premium and premium games are premium`() {
        MiniGames.FREE.forEach { game ->
            assertFalse("Free game ${game.id} should not be premium", game.premium)
        }
        MiniGames.PREMIUM.forEach { game ->
            assertTrue("Premium game ${game.id} should be premium", game.premium)
        }
    }

    @Test
    fun `no duplicate game IDs`() {
        val ids = MiniGames.ALL.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
    }

    @Test
    fun `every quiz game has at least 3 rounds`() {
        MiniGames.ALL.filter { it.kind == MiniGameKind.QUIZ }.forEach { game ->
            assertTrue("Game ${game.id} has only ${game.rounds.size} rounds (min 3)", game.rounds.size >= 3)
        }
    }

    @Test
    fun `arena games have no rounds but valid mode`() {
        MiniGames.ARENA3D.forEach { game ->
            assertEquals(0, game.rounds.size)
            assertEquals(MiniGameKind.ARENA_3D, game.kind)
            assertNotNull("Game ${game.id} missing threeMode", game.threeMode)
        }
    }

    @Test
    fun `every quiz round has valid correctIndex within options range`() {
        MiniGames.ALL.filter { it.kind == MiniGameKind.QUIZ }.forEach { game ->
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
    fun `all prompts and explanations are non-empty for quiz rounds`() {
        MiniGames.ALL.filter { it.kind == MiniGameKind.QUIZ }.forEach { game ->
            game.rounds.forEachIndexed { i, round ->
                assertTrue("Game ${game.id} round $i: empty promptDe", round.promptDe.isNotBlank())
                assertTrue("Game ${game.id} round $i: empty promptEn", round.promptEn.isNotBlank())
                assertTrue("Game ${game.id} round $i: empty explanationDe", round.explanationDe.isNotBlank())
                assertTrue("Game ${game.id} round $i: empty explanationEn", round.explanationEn.isNotBlank())
            }
        }
    }

    @Test
    fun `total rounds across quiz games is at least 24`() {
        val total = MiniGames.ALL.filter { it.kind == MiniGameKind.QUIZ }.sumOf { it.rounds.size }
        assertTrue("Expected >= 24 total rounds, got $total", total >= 24)
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
    fun `beginner and intermediate difficulty levels are represented`() {
        val difficulties = MiniGames.ALL.map { it.difficulty }.toSet()
        assertTrue("Missing BEGINNER", difficulties.contains(Difficulty.BEGINNER))
        assertTrue("Missing INTERMEDIATE", difficulties.contains(Difficulty.INTERMEDIATE))
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
    fun `byDifficulty beginner + intermediate = all`() {
        val byDiff = listOf(
            MiniGames.byDifficulty(Difficulty.BEGINNER),
            MiniGames.byDifficulty(Difficulty.INTERMEDIATE),
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
    fun `random premiumOnly returns null when no premium games exist`() {
        // Currently all games are free; premium catalog may expand later
        assertNull(MiniGames.random(premiumOnly = true))
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
        assertNotNull(MiniGames.byId("orb_hunt"))
        assertNotNull(MiniGames.byId("maze_run"))
        assertNotNull(MiniGames.byId("truth_snipe"))
    }
}
