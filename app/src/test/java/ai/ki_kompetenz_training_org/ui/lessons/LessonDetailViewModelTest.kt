package ai.ki_kompetenz_training_org.ui.lessons

import ai.ki_kompetenz_training_org.data.api.LessonDetailDto
import ai.ki_kompetenz_training_org.data.prefs.SettingsStore
import ai.ki_kompetenz_training_org.data.repo.ContentRepository
import ai.ki_kompetenz_training_org.data.repo.GamificationRepository
import ai.ki_kompetenz_training_org.data.repo.PremiumRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LessonDetailViewModelTest {

    private lateinit var contentRepository: ContentRepository
    private lateinit var premiumRepository: PremiumRepository
    private lateinit var gamificationRepository: GamificationRepository
    private lateinit var viewModel: LessonDetailViewModel
    private val dispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
        contentRepository = mockk()
        premiumRepository = mockk()
        gamificationRepository = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(slug: String) {
        viewModel = LessonDetailViewModel(slug, contentRepository, premiumRepository, gamificationRepository, dispatcher)
    }

    @Test
    fun `loads lesson successfully from API`() = runTest {
        val lesson = LessonDetailDto(slug = "lesson-1", title = "Lesson 1", lesson = 1, body = "# Was ist KI?")
        coEvery { contentRepository.fetchLesson("lesson-1", any()) } returns Result.success(lesson)
        coEvery { contentRepository.getCachedLesson(any()) } returns null

        createViewModel("lesson-1")

        val state = viewModel.state.value
        assertEquals("lesson-1", state.lesson?.slug)
        assertEquals("Lesson 1", state.lesson?.title)
        assertFalse(state.loading)
        assertNull(state.error)
        coVerify { contentRepository.fetchLesson("lesson-1", any()) }
    }

    @Test
    fun `handles lesson loading error`() = runTest {
        coEvery { contentRepository.fetchLesson("lesson-1", any()) } returns Result.failure(Exception("Network error"))
        coEvery { contentRepository.getCachedLesson(any()) } returns null

        createViewModel("lesson-1")

        val state = viewModel.state.value
        assertFalse(state.loading)
        assertNotNull(state.error)
    }

    @Test
    fun `lesson ki-einfuehrung has 3 quiz questions`() = runTest {
        coEvery { contentRepository.fetchLesson("lesson-1", any()) } returns Result.success(
            LessonDetailDto(slug = "lesson-1", title = "Lesson 1", lesson = 1))
        coEvery { contentRepository.getCachedLesson(any()) } returns null

        createViewModel("lesson-1")
        assertEquals(3, viewModel.state.value.quizQuestions.size)
    }

    @Test
    fun `startQuiz sets showQuiz to true`() = runTest {
        coEvery { contentRepository.fetchLesson("lesson-1", any()) } returns Result.success(
            LessonDetailDto(slug = "lesson-1", title = "Lesson 1", lesson = 1))
        coEvery { contentRepository.getCachedLesson(any()) } returns null

        createViewModel("lesson-1")
        viewModel.startQuiz()

        assertTrue(viewModel.state.value.showQuiz)
        assertEquals(0, viewModel.state.value.currentScore)
        assertFalse(viewModel.state.value.isTestPassed)
    }

    @Test
    fun `submitAnswer updates score and checks pass threshold`() = runTest {
        coEvery { contentRepository.fetchLesson("lesson-1", any()) } returns Result.success(
            LessonDetailDto(slug = "lesson-1", title = "Lesson 1", lesson = 1))
        coEvery { contentRepository.getCachedLesson(any()) } returns null

        createViewModel("lesson-1")
        viewModel.startQuiz()
        viewModel.submitAnswer("q1", 0)

        assertEquals(25, viewModel.state.value.currentScore)
        assertFalse(viewModel.state.value.isTestPassed)
    }

    @Test
    fun `submitAllCorrectAnswers passes test`() = runTest {
        coEvery { contentRepository.fetchLesson("lesson-1", any()) } returns Result.success(
            LessonDetailDto(slug = "lesson-1", title = "Lesson 1", lesson = 1))
        coEvery { contentRepository.getCachedLesson(any()) } returns null

        createViewModel("lesson-1")
        viewModel.startQuiz()
        viewModel.submitAnswer("q1", 0)
        viewModel.submitAnswer("q2", 3)
        viewModel.submitAnswer("q3", 1)

        assertEquals(100, viewModel.state.value.currentScore)
        assertTrue(viewModel.state.value.isTestPassed)
    }

    @Test
    fun `submitWrongAnswer does not add points`() = runTest {
        coEvery { contentRepository.fetchLesson("lesson-1", any()) } returns Result.success(
            LessonDetailDto(slug = "lesson-1", title = "Lesson 1", lesson = 1))
        coEvery { contentRepository.getCachedLesson(any()) } returns null

        createViewModel("lesson-1")
        viewModel.startQuiz()
        viewModel.submitAnswer("q1", 1)

        assertEquals(0, viewModel.state.value.currentScore)
        assertFalse(viewModel.state.value.isTestPassed)
    }

    @Test
    fun `partialCorrectAnswers can still pass`() = runTest {
        coEvery { contentRepository.fetchLesson("lesson-1", any()) } returns Result.success(
            LessonDetailDto(slug = "lesson-1", title = "Lesson 1", lesson = 1))
        coEvery { contentRepository.getCachedLesson(any()) } returns null

        createViewModel("lesson-1")
        viewModel.startQuiz()
        viewModel.submitAnswer("q1", 0)
        viewModel.submitAnswer("q3", 1)

        assertEquals(75, viewModel.state.value.currentScore)
        assertTrue(viewModel.state.value.isTestPassed)
    }

    @Test
    fun `markCompleted does nothing when test not passed`() = runTest {
        coEvery { contentRepository.fetchLesson("lesson-1", any()) } returns Result.success(
            LessonDetailDto(slug = "lesson-1", title = "Lesson 1", lesson = 1))
        coEvery { contentRepository.getCachedLesson(any()) } returns null

        createViewModel("lesson-1")
        viewModel.startQuiz()
        viewModel.markCompleted()

        coVerify(exactly = 0) { gamificationRepository.markLessonCompleted(any()) }
    }

    @Test
    fun `markCompleted calls repository when test passed`() = runTest {
        coEvery { contentRepository.fetchLesson("lesson-1", any()) } returns Result.success(
            LessonDetailDto(slug = "lesson-1", title = "Lesson 1", lesson = 1))
        coEvery { contentRepository.getCachedLesson(any()) } returns null

        createViewModel("lesson-1")
        viewModel.startQuiz()
        viewModel.submitAnswer("q1", 0)
        viewModel.submitAnswer("q2", 3)
        viewModel.submitAnswer("q3", 1)
        viewModel.markCompleted()

        coVerify { gamificationRepository.markLessonCompleted("lesson-1") }
    }

    @Test
    fun `canCompleteLesson returns false when test not passed`() = runTest {
        coEvery { contentRepository.fetchLesson("lesson-1", any()) } returns Result.success(
            LessonDetailDto(slug = "lesson-1", title = "Lesson 1", lesson = 1))
        coEvery { contentRepository.getCachedLesson(any()) } returns null

        createViewModel("lesson-1")
        assertFalse(viewModel.canCompleteLesson())
    }

    @Test
    fun `canCompleteLesson returns true when test passed`() = runTest {
        coEvery { contentRepository.fetchLesson("lesson-1", any()) } returns Result.success(
            LessonDetailDto(slug = "lesson-1", title = "Lesson 1", lesson = 1))
        coEvery { contentRepository.getCachedLesson(any()) } returns null

        createViewModel("lesson-1")
        viewModel.startQuiz()
        viewModel.submitAnswer("q1", 0)
        viewModel.submitAnswer("q2", 3)
        viewModel.submitAnswer("q3", 1)

        assertTrue(viewModel.canCompleteLesson())
    }

    @Test
    fun `lesson with no quiz questions has empty list`() = runTest {
        coEvery { contentRepository.fetchLesson("lesson-no-quiz", any()) } returns Result.success(
            LessonDetailDto(slug = "lesson-no-quiz", title = "No Quiz", lesson = 10))
        coEvery { contentRepository.getCachedLesson(any()) } returns null

        createViewModel("lesson-no-quiz")
        assertEquals(0, viewModel.state.value.quizQuestions.size)
    }

    // ── Completion summary + last-lesson persistence (ux-polish-pack) ──

    private fun createViewModelWith(slug: String, settings: SettingsStore?, total: Int = 14) {
        viewModel = LessonDetailViewModel(
            slug, contentRepository, premiumRepository, gamificationRepository,
            dispatcher, settings, total,
        )
    }

    @Test
    fun `markCompleted sets completion summary with next lesson and persists state`() = runTest {
        val settings: SettingsStore = mockk(relaxed = true)
        coEvery { contentRepository.fetchLesson("lesson-1", any()) } returns Result.success(
            LessonDetailDto(slug = "lesson-1", title = "Lesson 1", lesson = 1))
        coEvery { contentRepository.getCachedLesson(any()) } returns null

        createViewModelWith("lesson-1", settings)
        viewModel.startQuiz()
        viewModel.submitAnswer("q1", 0)
        viewModel.submitAnswer("q2", 3)
        viewModel.submitAnswer("q3", 1)

        viewModel.markCompleted()

        val completed = viewModel.state.value.completed
        assertNotNull(completed)
        assertEquals(100, completed!!.scorePct)
        assertEquals(ai.ki_kompetenz_training_org.data.repo.GamificationRules.xpPerCompletedLesson, completed.xpGained)
        assertEquals("lesson-2", completed.nextSlug)
        coVerify { gamificationRepository.markLessonCompleted("lesson-1") }
        coVerify { settings.setLastLesson("lesson-1", 1) }
    }

    @Test
    fun `markCompleted on last lesson has no next slug`() = runTest {
        coEvery { contentRepository.fetchLesson("lesson-14", any()) } returns Result.success(
            LessonDetailDto(slug = "lesson-14", title = "Lesson 14", lesson = 14))
        coEvery { contentRepository.getCachedLesson(any()) } returns null

        createViewModelWith("lesson-14", null, total = 14)
        viewModel.startQuiz()
        viewModel.submitAnswer("q1", 0)
        viewModel.submitAnswer("q2", 3)
        viewModel.submitAnswer("q3", 1)

        viewModel.markCompleted()

        assertEquals(null, viewModel.state.value.completed?.nextSlug)
    }

    @Test
    fun `markCompleted without passed test does nothing`() = runTest {
        coEvery { contentRepository.fetchLesson("lesson-1", any()) } returns Result.success(
            LessonDetailDto(slug = "lesson-1", title = "Lesson 1", lesson = 1))
        coEvery { contentRepository.getCachedLesson(any()) } returns null

        createViewModelWith("lesson-1", null)
        viewModel.markCompleted()

        assertNull(viewModel.state.value.completed)
        coVerify(exactly = 0) { gamificationRepository.markLessonCompleted(any()) }
    }

    @Test
    fun `opening a lesson persists it as last lesson`() = runTest {
        val settings: SettingsStore = mockk(relaxed = true)
        coEvery { contentRepository.fetchLesson("lesson-3", any()) } returns Result.success(
            LessonDetailDto(slug = "lesson-3", title = "Lesson 3", lesson = 3))
        coEvery { contentRepository.getCachedLesson(any()) } returns null

        createViewModelWith("lesson-3", settings)

        coVerify { settings.setLastLesson("lesson-3", 3) }
    }
}
