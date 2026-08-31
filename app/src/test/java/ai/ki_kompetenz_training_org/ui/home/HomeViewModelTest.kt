package ai.ki_kompetenz_training_org.ui.home

import ai.ki_kompetenz_training_org.data.db.LessonEntity
import ai.ki_kompetenz_training_org.data.db.LessonProgressEntity
import ai.ki_kompetenz_training_org.data.prefs.SettingsStore
import ai.ki_kompetenz_training_org.data.repo.AuthRepository
import ai.ki_kompetenz_training_org.data.repo.ContentRepository
import ai.ki_kompetenz_training_org.data.repo.GamificationRepository
import ai.ki_kompetenz_training_org.data.repo.PremiumRepository
import ai.ki_kompetenz_training_org.data.repo.TeamRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val authRepository: AuthRepository = mockk(relaxed = true)
    private val premiumRepository: PremiumRepository = mockk(relaxed = true)
    private val teamRepository: TeamRepository = mockk(relaxed = true)
    private val contentRepository: ContentRepository = mockk(relaxed = true)
    private val gamificationRepository: GamificationRepository = mockk(relaxed = true)
    private val settingsStore: SettingsStore = mockk(relaxed = true)

    private val lessonsFlow = MutableStateFlow<List<LessonEntity>>(emptyList())
    private val gamificationFlow = MutableStateFlow<ai.ki_kompetenz_training_org.data.db.GamificationEntity?>(null)
    private val progressFlow = MutableStateFlow<List<LessonProgressEntity>>(emptyList())
    private val lastLessonFlow = MutableStateFlow<SettingsStore.LastLesson?>(null)

    private val dispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
        every { authRepository.isLoggedIn() } returns false
        every { contentRepository.observeLessons() } returns lessonsFlow
        every { gamificationRepository.observe() } returns gamificationFlow
        every { gamificationRepository.observeLessonProgress() } returns progressFlow
        every { settingsStore.lastLesson } returns lastLessonFlow
        coEvery { contentRepository.fetchLessons(any()) } returns Result.success(emptyList())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun entity(slug: String, title: String, n: Int) = LessonEntity(
        slug = slug,
        title = title,
        lessonNumber = n,
        duration = "10 min",
        description = "d",
        objectivesJson = "[]",
        body = null,
    )

    private fun createViewModel() = HomeViewModel(
        authRepository,
        premiumRepository,
        teamRepository,
        contentRepository,
        gamificationRepository,
        settingsStore,
    )

    @Test
    fun `last lesson is resolved to title and index`() {
        lessonsFlow.value = listOf(entity("lesson-1", "Was ist KI?", 1), entity("lesson-2", "Arten von KI", 2))
        lastLessonFlow.value = SettingsStore.LastLesson("lesson-2", 2)

        val state = createViewModel().state.value

        assertEquals(LastLessonUi("lesson-2", "Arten von KI", 2), state.lastLesson)
    }

    @Test
    fun `stale last lesson slug resolves to null`() {
        lessonsFlow.value = listOf(entity("lesson-1", "Was ist KI?", 1))
        lastLessonFlow.value = SettingsStore.LastLesson("lesson-99", 99)

        val state = createViewModel().state.value

        assertNull(state.lastLesson)
    }

    @Test
    fun `total lessons follows the lesson list size`() {
        lessonsFlow.value = listOf(entity("lesson-1", "A", 1), entity("lesson-2", "B", 2))

        val state = createViewModel().state.value

        assertEquals(2, state.totalLessons)
    }

    @Test
    fun `total lessons falls back to 14 while list is empty`() {
        val state = createViewModel().state.value

        assertEquals(14, state.totalLessons)
    }

    @Test
    fun `lesson progress comes from completed lessons`() {
        progressFlow.value = listOf(LessonProgressEntity("lesson-1"), LessonProgressEntity("lesson-2"))

        val state = createViewModel().state.value

        assertEquals(2, state.lessonProgress)
    }
}
