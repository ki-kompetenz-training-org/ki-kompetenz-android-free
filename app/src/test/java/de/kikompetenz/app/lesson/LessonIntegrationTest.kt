package ai.ki_kompetenz_training_org.lesson

import ai.ki_kompetenz_training_org.data.api.ApiService
import ai.ki_kompetenz_training_org.data.api.LessonDetailDto
import ai.ki_kompetenz_training_org.data.db.AppDatabase
import ai.ki_kompetenz_training_org.data.db.ContentDao
import ai.ki_kompetenz_training_org.data.repo.ContentRepository
import ai.ki_kompetenz_training_org.data.repo.GamificationRepository
import ai.ki_kompetenz_training_org.data.repo.PremiumRepository
import ai.ki_kompetenz_training_org.ui.lessons.LessonDetailViewModel
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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LessonIntegrationTest {

    private lateinit var apiService: ApiService
    private lateinit var contentDao: ContentDao
    private lateinit var database: AppDatabase
    private lateinit var contentRepository: ContentRepository
    private lateinit var premiumRepository: PremiumRepository
    private lateinit var gamificationRepository: GamificationRepository
    private lateinit var viewModel: LessonDetailViewModel
    private val dispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
        apiService = mockk()
        contentDao = mockk()
        database = mockk()
        every { database.contentDao() } returns contentDao
        contentRepository = ContentRepository(apiService, database)
        premiumRepository = PremiumRepository(apiService)
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
    fun `lesson loads from API`() = runTest {
        val lessonDto = LessonDetailDto(slug = "ki-einfuehrung", title = "KI Einführung", lesson = 1, body = "# Was ist KI?")
        coEvery { apiService.getLesson("ki-einfuehrung") } returns lessonDto
        coEvery { contentDao.upsertLesson(any()) } returns Unit
        coEvery { contentDao.getLesson("ki-einfuehrung") } returns null

        createViewModel("ki-einfuehrung")

        val state = viewModel.state.value
        assertNotNull(state.lesson)
        assertEquals("ki-einfuehrung", state.lesson?.slug)
        assertFalse(state.loading)
    }

    @Test
    fun `premium lesson is correctly identified`() = runTest {
        coEvery { apiService.getLesson("ki-vertiefung") } returns
            LessonDetailDto(slug = "ki-vertiefung", title = "KI Vertiefung", lesson = 9, body = "Advanced")
        coEvery { contentDao.upsertLesson(any()) } returns Unit
        coEvery { contentDao.getLesson("ki-vertiefung") } returns null

        createViewModel("ki-vertiefung")
        assertTrue(premiumRepository.isPremiumLesson(9))
    }

    @Test
    fun `error during lesson loading is handled gracefully`() = runTest {
        coEvery { apiService.getLesson("ki-einfuehrung") } throws Exception("Network timeout")
        coEvery { contentDao.getLesson("ki-einfuehrung") } returns null

        createViewModel("ki-einfuehrung")

        val state = viewModel.state.value
        assertFalse(state.loading)
        assertNotNull(state.error)
    }

    @Test
    fun `null lesson number is handled`() = runTest {
        coEvery { apiService.getLesson("ki-einfuehrung") } returns
            LessonDetailDto(slug = "ki-einfuehrung", title = "KI Einführung", lesson = null, body = "Test")
        coEvery { contentDao.upsertLesson(any()) } returns Unit
        coEvery { contentDao.getLesson("ki-einfuehrung") } returns null

        createViewModel("ki-einfuehrung")

        val state = viewModel.state.value
        assertNotNull(state.lesson)
        assertEquals("ki-einfuehrung", state.lesson?.slug)
        assertFalse(premiumRepository.isPremiumLesson(null))
    }
}
