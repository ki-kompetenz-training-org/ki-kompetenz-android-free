package ai.ki_kompetenz_training_org.ui.srs

import ai.ki_kompetenz_training_org.data.api.SrsCardDto
import ai.ki_kompetenz_training_org.data.repo.AuthRepository
import ai.ki_kompetenz_training_org.data.repo.GamificationRepository
import ai.ki_kompetenz_training_org.data.repo.SrsRepository
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
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SrsViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()

    private val authRepository: AuthRepository = mockk()
    private val srsRepository: SrsRepository = mockk()
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

    private fun viewModel() = SrsViewModel(authRepository, srsRepository, gamificationRepository)

    @Test
    fun `not logged in skips load`() = runTest {
        every { authRepository.isLoggedIn() } returns false
        val vm = viewModel()
        assertEquals(SrsPhase.NOT_LOGGED_IN, vm.state.value.phase)
        assertNull(vm.state.value.error)
    }

    @Test
    fun `load failure emits typed UiError`() = runTest {
        coEvery { srsRepository.getDueCards() } returns Result.failure(Exception("offline"))
        val vm = viewModel()
        assertEquals(SrsPhase.ERROR, vm.state.value.phase)
        assertEquals(UiError.SRS_LOAD, vm.state.value.error)
    }

    @Test
    fun `empty card list yields NO_CARDS`() = runTest {
        coEvery { srsRepository.getDueCards() } returns Result.success(emptyList())
        val vm = viewModel()
        assertEquals(SrsPhase.NO_CARDS, vm.state.value.phase)
        assertNull(vm.state.value.error)
    }

    @Test
    fun `rate failure keeps review state and emits SRS_SAVE`() = runTest {
        val cards = listOf(SrsCardDto(id = "c1", question = "q", answer = "a"))
        coEvery { srsRepository.getDueCards() } returns Result.success(cards)
        coEvery { srsRepository.postReview(any(), any()) } returns Result.failure(Exception("timeout"))
        val vm = viewModel()
        vm.revealAnswer()
        vm.rate(3)
        assertEquals(UiError.SRS_SAVE, vm.state.value.error)
        // Review session stays intact for retry.
        assertEquals(SrsPhase.REVIEW, vm.state.value.phase)
        assertEquals("c1", vm.state.value.currentCard?.id)
    }

    @Test
    fun `successful rate clears previous error`() = runTest {
        val cards = listOf(SrsCardDto(id = "c1", question = "q", answer = "a"))
        coEvery { srsRepository.getDueCards() } returns Result.success(cards)
        coEvery { srsRepository.postReview(any(), any()) } returns
            Result.failure(Exception("timeout")) andThen Result.success(Unit)
        val vm = viewModel()
        vm.revealAnswer()
        vm.rate(3)
        assertEquals(UiError.SRS_SAVE, vm.state.value.error)
        vm.rate(3)
        assertNull(vm.state.value.error)
    }
}
