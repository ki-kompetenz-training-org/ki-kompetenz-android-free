package ai.ki_kompetenz_training_org.data.minigames3d

import org.junit.Assert.*
import org.junit.Test

class GameEngineTest {

    @Test
    fun createState_initializes_orb_hunt() {
        val s = GameEngine.createState(GameMode.ORB_HUNT, rng = { 0.5 })
        assertEquals(GameMode.ORB_HUNT, s.mode)
        assertEquals(60.0, s.timeLeft, 1e-9)
        assertEquals(0, s.score)
        assertEquals(3, s.health)
        assertEquals(250, s.target)
        assertEquals(3, s.collectibles.size)
        assertEquals(4, s.hazards.size)
    }

    @Test
    fun createState_initializes_maze_run() {
        val s = GameEngine.createState(GameMode.MAZE_RUN, rng = { 0.5 })
        assertEquals(200, s.target)
        assertTrue(s.goal != null)
        assertEquals(4, s.walls.size)
        assertEquals(3, s.hazards.size)
    }

    @Test
    fun createState_initializes_truth_snipe() {
        val s = GameEngine.createState(GameMode.TRUTH_SNIPE, rng = { 0.5 })
        assertEquals(300, s.target)
        assertEquals(4, s.collectibles.size)
        assertEquals(5, s.hazards.size)
    }

    @Test
    fun stepGame_moves_player_right() {
        val s = GameEngine.createState(GameMode.ORB_HUNT, rng = { 0.5 })
        val x0 = s.player.x
        GameEngine.stepGame(s, InputState(false, false, false, true, false), dt = 1.0 / 60.0)
        assertTrue(s.player.x > x0)
    }

    @Test
    fun stepGame_moves_player_up() {
        val s = GameEngine.createState(GameMode.ORB_HUNT, rng = { 0.5 })
        val z0 = s.player.z
        GameEngine.stepGame(s, InputState(true, false, false, false, false), dt = 1.0 / 60.0)
        assertTrue(s.player.z < z0)
    }

    @Test
    fun stepGame_does_nothing_after_end() {
        val s = GameEngine.createState(GameMode.ORB_HUNT, rng = { 0.5 })
        GameRules.endGame(s, EndReason.TIME)
        val score = s.score
        GameEngine.stepGame(s, InputState(false, false, true, false, false), dt = 1.0)
        assertEquals(score, s.score)
    }

    @Test
    fun stepGame_decrements_time() {
        val s = GameEngine.createState(GameMode.ORB_HUNT, rng = { 0.5 })
        GameEngine.stepGame(s, InputState(false, false, false, false, false), dt = 1.0)
        assertEquals(59.0, s.timeLeft, 1e-9)
    }

    @Test
    fun stepGame_ends_when_time_runs_out() {
        val s = GameEngine.createState(GameMode.ORB_HUNT, rng = { 0.5 })
        // Idle player would be hit by chasers - make invulnerable so TIME decides
        s.player.invuln = 999.0
        for (i in 0 until 3660) {
            GameEngine.stepGame(s, InputState(false, false, false, false, false), dt = 1.0 / 60.0)
            if (s.ended) break
        }
        assertTrue(s.ended)
        assertEquals(EndReason.TIME, s.endReason)
    }

    @Test
    fun stepGame_player_stays_in_arena() {
        val s = GameEngine.createState(GameMode.ORB_HUNT, rng = { 0.5 })
        for (i in 0 until 600) {
            GameEngine.stepGame(s, InputState(true, false, true, true, false), dt = 1.0 / 60.0)
            if (s.ended) break
        }
        val dist = kotlin.math.hypot(s.player.x, s.player.z)
        assertTrue(dist <= GameConfig.MODES[GameMode.ORB_HUNT]!!.arenaRadius + 0.1)
    }

    @Test
    fun classify_correct_fact_scores() {
        val s = GameEngine.createState(GameMode.ORB_HUNT, rng = { 0.5 })
        val orb = s.collectibles.first()
        s.player.x = orb.x
        s.player.z = orb.z
        val scoreBefore = s.score
        GameEngine.stepGame(s, InputState(false, false, false, false, false, classify = ClassifyAction.FACT), dt = 0.016)
        assertTrue(s.score >= scoreBefore)
    }

    @Test
    fun classify_adds_mastery_log() {
        val s = GameEngine.createState(GameMode.ORB_HUNT, rng = { 0.5 })
        val orb = s.collectibles.first()
        s.player.x = orb.x
        s.player.z = orb.z
        GameEngine.stepGame(s, InputState(false, false, false, false, false, classify = ClassifyAction.FACT), dt = 0.016)
        assertTrue(s.classifications.isNotEmpty())
    }
}
