package ai.ki_kompetenz_training_org.ui.quiz

import ai.ki_kompetenz_training_org.data.api.KiScoreDataDto
import ai.ki_kompetenz_training_org.data.db.AppDatabase
import ai.ki_kompetenz_training_org.data.repo.ContentRepository
import ai.ki_kompetenz_training_org.data.repo.GamificationRepository
import ai.ki_kompetenz_training_org.ui.common.UiError
import io.mockk.coEvery
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
class QuizViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()

    private val contentRepository: ContentRepository = mockk()
    private val gamificationRepository: GamificationRepository = mockk(relaxed = true)
    private val db: AppDatabase = mockk(relaxed = true)

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = QuizViewModel(contentRepository, db, gamificationRepository)

    @Test
    fun `load failure emits typed UiError`() = runTest {
        coEvery { contentRepository.fetchKiScoreData() } returns Result.failure(Exception("offline"))
        val vm = viewModel()
        val s = vm.state.value
        assertEquals(QuizPhase.ERROR, s.phase)
        assertEquals(UiError.QUIZ_LOAD, s.error)
        assertNull(s.questions.firstOrNull())
    }

    @Test
    fun `load success leaves error null`() = runTest {
        coEvery { contentRepository.fetchKiScoreData() } returns Result.success(
            KiScoreDataDto(questions = emptyList(), tiers = emptyList(), share = null),
        )
        val vm = viewModel()
        assertEquals(QuizPhase.INTRO, vm.state.value.phase)
        assertNull(vm.state.value.error)
    }

    @Test
    fun `retry after failure resets error on success`() = runTest {
        coEvery { contentRepository.fetchKiScoreData() } returns
            Result.failure(Exception("offline")) andThen
            Result.success(KiScoreDataDto(questions = emptyList(), tiers = emptyList(), share = null))
        val vm = viewModel()
        assertEquals(UiError.QUIZ_LOAD, vm.state.value.error)
        vm.load()
        assertEquals(QuizPhase.INTRO, vm.state.value.phase)
        assertNull(vm.state.value.error)
    }
}
