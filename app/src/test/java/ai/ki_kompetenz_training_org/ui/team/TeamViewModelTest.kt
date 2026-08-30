package ai.ki_kompetenz_training_org.ui.team

import ai.ki_kompetenz_training_org.data.repo.AuthRepository
import ai.ki_kompetenz_training_org.data.repo.GamificationRepository
import ai.ki_kompetenz_training_org.data.repo.TeamRepository
import ai.ki_kompetenz_training_org.ui.common.UiError
import io.mockk.coEvery
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
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TeamViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()

    private val authRepository: AuthRepository = mockk()
    private val teamRepository: TeamRepository = mockk()
    private val gamificationRepository: GamificationRepository = mockk(relaxed = true)

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        every { authRepository.isLoggedIn() } returns true
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = TeamViewModel(authRepository, teamRepository, gamificationRepository)

    @Test
    fun `load failure emits typed UiError and stops loading`() = runTest {
        coEvery { teamRepository.getMyTeam() } returns Result.failure(Exception("offline"))
        val vm = viewModel()
        assertEquals(UiError.TEAM_LOAD, vm.state.value.error)
        assertFalse(vm.state.value.loading)
    }

    @Test
    fun `load success without team clears error`() = runTest {
        coEvery { teamRepository.getMyTeam() } returns Result.success(
            ai.ki_kompetenz_training_org.data.api.MyTeamResponseDto(team = null),
        )
        val vm = viewModel()
        assertNull(vm.state.value.error)
        assertFalse(vm.state.value.loading)
        assertNull(vm.state.value.team?.team)
    }

    @Test
    fun `retry after failure resets error on success`() = runTest {
        coEvery { teamRepository.getMyTeam() } returns
            Result.failure(Exception("offline")) andThen
            Result.success(ai.ki_kompetenz_training_org.data.api.MyTeamResponseDto(team = null))
        val vm = viewModel()
        assertEquals(UiError.TEAM_LOAD, vm.state.value.error)
        vm.load()
        assertNull(vm.state.value.error)
    }

    @Test
    fun `logged out clears error`() = runTest {
        coEvery { teamRepository.getMyTeam() } returns Result.failure(Exception("offline"))
        val vm = viewModel()
        assertEquals(UiError.TEAM_LOAD, vm.state.value.error)
        every { authRepository.isLoggedIn() } returns false
        vm.load()
        assertNull(vm.state.value.error)
        assertFalse(vm.state.value.loggedIn)
    }
}
