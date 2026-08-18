package ai.ki_kompetenz_training_org.data.repo

import ai.ki_kompetenz_training_org.data.api.ApiService
import ai.ki_kompetenz_training_org.data.api.SubscriptionStatusDto
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [PremiumRepository] using MockK.
 */
class PremiumRepositoryTest {

    private lateinit var apiService: ApiService
    private lateinit var repository: PremiumRepository

    @Before
    fun setup() {
        apiService = mockk()
        repository = PremiumRepository(apiService)
    }

    // ── isPremiumLesson() Tests ────────────────────────────────────────────

    @Test
    fun `lesson 1 is free`() {
        assertFalse(repository.isPremiumLesson(1))
    }

    @Test
    fun `lesson 2 is free`() {
        assertFalse(repository.isPremiumLesson(2))
    }

    @Test
    fun `lesson 3 is free`() {
        assertFalse(repository.isPremiumLesson(3))
    }

    @Test
    fun `lesson 4 is free (even number)`() {
        assertFalse(repository.isPremiumLesson(4))
    }

    @Test
    fun `lesson 5 is free (below threshold)`() {
        assertFalse(repository.isPremiumLesson(5))
    }

    @Test
    fun `lesson 6 is free (even number)`() {
        assertFalse(repository.isPremiumLesson(6))
    }

    @Test
    fun `lesson 7 is free (below threshold)`() {
        assertFalse(repository.isPremiumLesson(7))
    }

    @Test
    fun `lesson 10 is premium (above threshold)`() {
        assertTrue(repository.isPremiumLesson(10))
    }

    @Test
    fun `null lesson number is free`() {
        assertFalse(repository.isPremiumLesson(null))
    }

    @Test
    fun `negative lesson number is free`() {
        assertFalse(repository.isPremiumLesson(-1))
    }

    @Test
    fun `zero lesson number is free`() {
        assertFalse(repository.isPremiumLesson(0))
    }

    // ── isPremium() Tests ─────────────────────────────────────────────────

    @Test
    fun `isPremium returns true when subscribed`() = runBlocking {
        coEvery { apiService.getSubscriptionStatus() } returns SubscriptionStatusDto(subscribed = true)

        val result = repository.isPremium().getOrThrow()

        assertTrue(result)
        coVerify { apiService.getSubscriptionStatus() }
    }

    @Test
    fun `isPremium returns false when not subscribed`() = runBlocking {
        coEvery { apiService.getSubscriptionStatus() } returns SubscriptionStatusDto(subscribed = false)

        val result = repository.isPremium().getOrThrow()

        assertFalse(result)
    }

    @Test
    fun `isPremium handles API error`() = runBlocking {
        coEvery { apiService.getSubscriptionStatus() } throws Exception("Network error")

        val result = repository.isPremium()

        assertTrue(result.isFailure)
        assertEquals("Network error", result.exceptionOrNull()?.message)
    }

    // ── Pattern Verification ────────────────────────────────────────────

    fun `all lessons 1-8 are free`() {
        (1..8).forEach { lessonNumber ->
            assertFalse("Lektion $lessonNumber should be free", repository.isPremiumLesson(lessonNumber))
        }
    }

    @Test
    fun `lessons 9+ are premium`() {
        for (lessonNumber in 9..20) {
            assertTrue("Lektion $lessonNumber should be premium", repository.isPremiumLesson(lessonNumber))
        }
    }

    @Test
    fun `at least 50 percent of first 10 lessons are free`() {
        val totalLessons = 10
        val freeLessons = (1..totalLessons).count { !repository.isPremiumLesson(it) }
        assertTrue("At least 50% of lessons should be free", freeLessons >= totalLessons / 2)
    }
}
