package ai.ki_kompetenz_training_org.ui.lessons

import ai.ki_kompetenz_training_org.data.db.LessonEntity
import ai.ki_kompetenz_training_org.data.db.LessonProgressEntity
import ai.ki_kompetenz_training_org.data.prefs.SettingsStore
import ai.ki_kompetenz_training_org.data.repo.ContentRepository
import ai.ki_kompetenz_training_org.data.repo.GamificationRepository
import ai.ki_kompetenz_training_org.data.repo.PremiumRepository
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LessonsViewModelTest {

    private val contentRepository: ContentRepository = mockk(relaxed = true)
    private val premiumRepository: PremiumRepository = mockk(relaxed = true)
    private val gamificationRepository: GamificationRepository = mockk(relaxed = true)
    private val settingsStore: SettingsStore = mockk(relaxed = true)

    private val lessonsFlow = MutableStateFlow<List<LessonEntity>>(emptyList())
    private val progressFlow = MutableStateFlow<List<LessonProgressEntity>>(emptyList())
    private val lastLessonFlow = MutableStateFlow<SettingsStore.LastLesson?>(null)

    private val dispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
        every { contentRepository.observeLessons() } returns lessonsFlow
        every { gamificationRepository.observeLessonProgress() } returns progressFlow
        every { settingsStore.lastLesson } returns lastLessonFlow
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun lesson(slug: String, n: Int) = LessonEntity(
        slug = slug,
        title = "Lesson $n",
        lessonNumber = n,
        duration = "10 min",
        description = "desc",
        objectivesJson = "[]",
        body = null,
    )

    private fun createViewModel() =
        LessonsViewModel(contentRepository, premiumRepository, gamificationRepository, settingsStore)

    @Test
    fun `completed slugs are exposed from lesson progress`() {
        lessonsFlow.value = listOf(lesson("lesson-1", 1), lesson("lesson-2", 2))
        progressFlow.value = listOf(LessonProgressEntity("lesson-1"))

        val state = createViewModel().state.value

        assertEquals(setOf("lesson-1"), state.completedSlugs)
    }

    @Test
    fun `last opened slug is exposed and excluded when completed`() {
        lessonsFlow.value = listOf(lesson("lesson-1", 1), lesson("lesson-2", 2))
        progressFlow.value = listOf(LessonProgressEntity("lesson-1"))
        lastLessonFlow.value = SettingsStore.LastLesson("lesson-1", 1)

        val state = createViewModel().state.value

        assertEquals(setOf("lesson-1"), state.completedSlugs)
        assertEquals(null, state.lastOpenedSlug)
    }

    @Test
    fun `last opened slug is shown when not completed`() {
        lastLessonFlow.value = SettingsStore.LastLesson("lesson-2", 2)

        val state = createViewModel().state.value

        assertEquals("lesson-2", state.lastOpenedSlug)
    }

    @Test
    fun `loading becomes false when lessons arrive`() {
        val state = createViewModel().state.value
        assertTrue(state.lessons.isEmpty())

        lessonsFlow.value = listOf(lesson("lesson-1", 1))

        val updated = createViewModel().state.value
        assertEquals(1, updated.lessons.size)
        assertEquals(false, updated.loading)
    }
}
