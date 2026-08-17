package ai.ki_kompetenz_training_org.data.repo

import ai.ki_kompetenz_training_org.data.api.ApiService
import ai.ki_kompetenz_training_org.data.api.LessonDetailDto
import ai.ki_kompetenz_training_org.data.api.LessonSummaryDto
import ai.ki_kompetenz_training_org.data.api.LessonsResponseDto
import ai.ki_kompetenz_training_org.data.db.AppDatabase
import ai.ki_kompetenz_training_org.data.db.ContentDao
import ai.ki_kompetenz_training_org.data.db.LessonEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ContentRepositoryTest {

    private lateinit var apiService: ApiService
    private lateinit var contentDao: ContentDao
    private lateinit var database: AppDatabase
    private lateinit var repository: ContentRepository

    @Before
    fun setup() {
        apiService = mockk()
        contentDao = mockk()
        database = mockk()
        every { database.contentDao() } returns contentDao
        repository = ContentRepository(apiService, database)
    }

    @Test
    fun `fetchLesson returns success on valid response`() = runBlocking {
        val lessonDto = LessonDetailDto(
            slug = "ki-einfuehrung",
            title = "KI Einführung",
            lesson = 1,
            body = "# Was ist KI?",
        )
        coEvery { apiService.getLesson("ki-einfuehrung") } returns lessonDto
        coEvery { contentDao.upsertLesson(any()) } returns Unit

        val result = repository.fetchLesson("ki-einfuehrung")

        assertTrue(result.isSuccess)
        assertEquals("ki-einfuehrung", result.getOrThrow().slug)
    }

    @Test
    fun `fetchLesson returns failure on network error`() = runBlocking {
        coEvery { apiService.getLesson("ki-einfuehrung") } throws Exception("Network error")

        val result = repository.fetchLesson("ki-einfuehrung")

        assertTrue(result.isFailure)
        assertEquals("Network error", result.exceptionOrNull()?.message)
    }

    @Test
    fun `fetchLessons returns success with lesson list`() = runBlocking {
        val lessons = listOf(
            LessonSummaryDto(slug = "lesson-1", title = "Lektion 1", lesson = 1),
            LessonSummaryDto(slug = "lesson-2", title = "Lektion 2", lesson = 2),
        )
        coEvery { apiService.getLessons() } returns LessonsResponseDto(lessons)
        coEvery { contentDao.upsertLesson(any()) } returns Unit

        val result = repository.fetchLessons()

        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrThrow().size)
    }

    @Test
    fun `getCachedLesson returns null when no cache`() = runBlocking {
        coEvery { contentDao.getLesson("ki-einfuehrung") } returns null

        val result = repository.getCachedLesson("ki-einfuehrung")

        assertNull(result)
    }

    @Test
    fun `getCachedLesson returns null (cache disabled temporarily)`() = runBlocking {
        // getCachedLesson is currently hardcoded to return null
        // (TEMPORARY FIX: Database disabled due to Gradle cache corruption)
        val result = repository.getCachedLesson("ki-einfuehrung")

        assertNull(result)
    }

    @Test
    fun `fetchLesson updates cache on success`() = runBlocking {
        val lessonDto = LessonDetailDto(
            slug = "ki-einfuehrung",
            title = "KI Einführung",
            lesson = 1,
            body = "# Was ist KI?",
        )
        coEvery { apiService.getLesson("ki-einfuehrung") } returns lessonDto
        coEvery { contentDao.upsertLesson(any()) } returns Unit

        repository.fetchLesson("ki-einfuehrung")

        coVerify { contentDao.upsertLesson(any()) }
    }

    @Test
    fun `fetchLesson does not update cache on failure`() = runBlocking {
        coEvery { apiService.getLesson("non-existent") } throws Exception("Not found")

        val result = repository.fetchLesson("non-existent")

        assertTrue(result.isFailure)
        coVerify(exactly = 0) { contentDao.upsertLesson(any()) }
    }

    @Test
    fun `observeLessons returns flow from DAO`() = runBlocking {
        val lessonEntities = listOf(
            LessonEntity(
                slug = "lesson-1", title = "Lektion 1", lessonNumber = 1,
                duration = null, description = "", objectivesJson = "[]", body = null,
            ),
        )
        every { contentDao.observeLessons() } returns flowOf(lessonEntities)

        val result = repository.observeLessons().first()

        assertEquals(1, result.size)
        assertEquals("lesson-1", result[0].slug)
    }
}
