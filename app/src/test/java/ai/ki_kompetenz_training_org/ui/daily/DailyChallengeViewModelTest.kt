package ai.ki_kompetenz_training_org.ui.daily

import ai.ki_kompetenz_training_org.data.daily.DailyChallengeRepository
import ai.ki_kompetenz_training_org.data.minigames.MiniGames
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class DailyChallengeViewModelTest {

    private lateinit var repo: DailyChallengeRepository
    private lateinit var vm: DailyChallengeViewModel
    private val dispatcher = UnconfinedTestDispatcher()
    private val today = LocalDate.of(2026, 8, 25)

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
        repo = mockk(relaxed = true)
        every { repo.getTodayChallenge(today, MiniGames.ALL) } returns MiniGames.ALL[2]
        every { repo.isCompletedToday(today) } returns false
        every { repo.getStreak() } returns 0
        every { repo.calculateXpPreview(perfect = false) } returns 20
        every { repo.calculateXpPreview(perfect = true) } returns 35
        vm = DailyChallengeViewModel(repo, dispatcher, today)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initial_state_loads_todays_challenge() {
        assertNotNull(vm.state.value.challenge)
        assertEquals("high_risk_blitz", vm.state.value.challenge!!.id)
    }

    @Test
    fun initial_state_shows_not_completed() {
        assertFalse(vm.state.value.isCompleted)
    }

    @Test
    fun initial_state_shows_streak() {
        assertEquals(0, vm.state.value.streak)
    }

    @Test
    fun initial_state_shows_xp_preview() {
        assertEquals(20, vm.state.value.xpPreview)
    }

    @Test
    fun completeChallenge_awards_xp() = runTest {
        every { repo.completeChallenge(today, perfect = false) } returns 20
        vm.completeChallenge(perfect = false)
        assertEquals(20, vm.state.value.lastAwardedXp)
    }

    @Test
    fun completeChallenge_updates_completed_state() = runTest {
        every { repo.completeChallenge(today, perfect = false) } returns 20
        vm.completeChallenge(perfect = false)
        assertTrue(vm.state.value.isCompleted)
    }

    @Test
    fun completeChallenge_updates_streak() = runTest {
        every { repo.completeChallenge(today, perfect = true) } returns 35
        every { repo.getStreak() } returns 1
        vm.completeChallenge(perfect = true)
        assertEquals(1, vm.state.value.streak)
    }

    @Test
    fun completeChallenge_zero_xp_when_already_done() = runTest {
        every { repo.completeChallenge(today, perfect = false) } returns 0
        vm.completeChallenge(perfect = false)
        assertEquals(0, vm.state.value.lastAwardedXp)
    }

    @Test
    fun completeChallenge_calls_repo_once() = runTest {
        every { repo.completeChallenge(today, perfect = false) } returns 20
        vm.completeChallenge(perfect = false)
        coVerify(exactly = 1) { repo.completeChallenge(today, perfect = false) }
    }

    @Test
    fun refresh_reloads_state_from_repo() {
        every { repo.isCompletedToday(today) } returns true
        every { repo.getStreak() } returns 3
        every { repo.calculateXpPreview(perfect = false) } returns 30
        vm.refresh()
        assertTrue(vm.state.value.isCompleted)
        assertEquals(3, vm.state.value.streak)
        assertEquals(30, vm.state.value.xpPreview)
    }
}
