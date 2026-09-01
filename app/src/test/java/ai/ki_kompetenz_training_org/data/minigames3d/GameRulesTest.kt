package ai.ki_kompetenz_training_org.data.minigames3d

import org.junit.Assert.*
import org.junit.Test

/**
 * GameRules-Tests (touch-native API).
 *
 * Die ehemaligen computeResult-Tests (score/target/health/reason → result)
 * sind mit der T2-T4-Migration obsolet: Diese Logik liegt jetzt in
 * GameEngine.endGame + GameRules.endGame(s, reason) und wird in
 * GameEngineTest abgedeckt.
 */
class GameRulesTest {

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
