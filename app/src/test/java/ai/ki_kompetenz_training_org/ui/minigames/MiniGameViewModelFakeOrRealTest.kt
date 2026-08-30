package ai.ki_kompetenz_training_org.ui.minigames

import ai.ki_kompetenz_training_org.data.minigames.Difficulty
import ai.ki_kompetenz_training_org.data.minigames.MiniGame
import ai.ki_kompetenz_training_org.data.minigames.MiniGameKind
import ai.ki_kompetenz_training_org.data.minigames.MiniGames
import ai.ki_kompetenz_training_org.data.minigames.TextGameBank
import ai.ki_kompetenz_training_org.data.repo.GamificationRepository
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.random.Random

@OptIn(ExperimentalCoroutinesApi::class)
class MiniGameViewModelFakeOrRealTest {

    private val gamification = mockk<GamificationRepository>(relaxed = true)

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun fakeOrRealGame() = MiniGames.ALL.first { it.isFakeOrReal }

    @Test
    fun `session draws exactly 10 rounds from the bank`() = runTest {
        val vm = MiniGameViewModel(fakeOrRealGame(), gamification, rng = Random(1))
        assertEquals(TextGameBank.SESSION_SIZE, vm.sessionRounds.size)
        val bankIds = TextGameBank.ALL.map { it.id }.toSet()
        vm.sessionRounds.forEach { r -> assertTrue(r.promptDe in TextGameBank.ALL.map { it.textDe }) }
        assertEquals(10, vm.sessionRounds.distinct().size)
    }

    @Test
    fun `session rounds differ across seeds (random shuffle)`() {
        val game = fakeOrRealGame()
        val ids1 = MiniGameViewModel(game, gamification, rng = Random(1)).sessionRounds.map { it.promptDe }
        val ids2 = MiniGameViewModel(game, gamification, rng = Random(2)).sessionRounds.map { it.promptDe }
        val ids3 = MiniGameViewModel(game, gamification, rng = Random(3)).sessionRounds.map { it.promptDe }
        // At least two distinct orderings (10 of 30 → order variation is not guaranteed but overwhelmingly likely)
        val distinct = setOf(ids1, ids2, ids3)
        assertNotEquals(ids1, ids2)
        assertTrue("expected varied shuffles", distinct.size >= 2)
    }

    @Test
    fun `binary choice and result after 10 rounds`() = runTest {
        val vm = MiniGameViewModel(fakeOrRealGame(), gamification, rng = Random(5))
        repeat(10) {
            val s = vm.state.value
            vm.selectOption(1)
            vm.next()
        }
        assertEquals(GamePhase.RESULT, vm.state.value.phase)
        assertEquals(10, vm.state.value.answers.size)
    }

    @Test
    fun `perfect 10 of 10 awards XP and unlocks the dedicated badge path`() = runTest {
        val rounds = fakeOrRealGame().rounds.shuffled(Random(7)).take(TextGameBank.SESSION_SIZE)
        val stub = MiniGame(
            id = "fake_or_real", emoji = "🤖", titleDe = "t", titleEn = "t",
            descriptionDe = "d", descriptionEn = "d",
            rounds = rounds, kind = MiniGameKind.FAKE_OR_REAL,
            difficulty = Difficulty.INTERMEDIATE,
        )
        val vm = MiniGameViewModel(stub, gamification, rng = Random(9))
        repeat(10) { i ->
            vm.selectOption(vm.sessionRounds[i].correctIndex)
            vm.next()
        }
        assertEquals(GamePhase.RESULT, vm.state.value.phase)
        assertEquals(10, vm.state.value.answers.count { it })
        coVerify { gamification.onMiniGameFinished(10, 10, "fake_or_real") }
    }
}
