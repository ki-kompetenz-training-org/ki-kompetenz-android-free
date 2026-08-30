package ai.ki_kompetenz_training_org.data.minigames3d

import org.junit.Assert.*
import org.junit.Test

class GameRulesTest {

    @Test
    fun computeResult_time_and_enough_score_wins() {
        val r = GameRules.computeResult(250, 250, 3, EndReason.TIME)
        assertTrue(r.won)
        assertEquals(EndReason.TIME, r.reason)
    }

    @Test
    fun computeResult_time_and_low_score_loses() {
        val r = GameRules.computeResult(100, 250, 3, EndReason.TIME)
        assertFalse(r.won)
    }

    @Test
    fun computeResult_health_death_loses() {
        val r = GameRules.computeResult(500, 250, 0, EndReason.HEALTH)
        assertFalse(r.won)
    }

    @Test
    fun computeResult_health_survived_wins() {
        val r = GameRules.computeResult(500, 250, 1, EndReason.HEALTH)
        assertTrue(r.won)
    }

    @Test
    fun endGame_sets_flags() {
        val s = GameEngine.createState(GameMode.ORB_HUNT, rng = { 0.5 })
        s.score = 250
        GameRules.endGame(s, EndReason.TIME)
        assertTrue(s.ended)
        assertEquals(EndReason.TIME, s.endReason)
        assertTrue(s.won)
    }

    @Test
    fun endGame_is_idempotent() {
        val s = GameEngine.createState(GameMode.ORB_HUNT, rng = { 0.5 })
        GameRules.endGame(s, EndReason.HEALTH)
        s.health = 3
        GameRules.endGame(s, EndReason.TIME)
        assertEquals(EndReason.HEALTH, s.endReason)
    }
}
