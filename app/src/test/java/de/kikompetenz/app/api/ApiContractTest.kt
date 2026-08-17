package ai.ki_kompetenz_training_org.api

import ai.ki_kompetenz_training_org.data.api.ApiService
import ai.ki_kompetenz_training_org.data.api.LessonDetailDto
import ai.ki_kompetenz_training_org.data.api.LessonsResponseDto
import ai.ki_kompetenz_training_org.data.api.SubscriptionStatusDto
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory

/**
 * Contract Tests for API endpoints using MockWebServer.
 * Verifies that the API response format matches our kotlinx.serialization data classes.
 */
class ApiContractTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var apiService: ApiService

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    @Before
    fun setup() {
        mockWebServer = MockWebServer()

        val contentType = "application/json".toMediaType()
        val retrofit = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()

        apiService = retrofit.create(ApiService::class.java)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    // ── /api/content/lessons/{slug} Contract Tests ───────────────────────

    @Test
    fun `getLesson returns valid LessonDetailDto`() = runBlocking {
        val jsonResponse = """
            {
                "slug": "ki-einfuehrung",
                "title": "KI Einführung",
                "lesson": 1,
                "body": "# Was ist KI?\n\nKI steht für künstliche Intelligenz...",
                "description": "Grundlagen der künstlichen Intelligenz",
                "duration": "8 Min."
            }
        """.trimIndent()

        mockWebServer.enqueue(MockResponse()
            .setBody(jsonResponse)
            .setResponseCode(200)
            .setHeader("Content-Type", "application/json"))

        val lesson = apiService.getLesson("ki-einfuehrung")

        assertNotNull(lesson)
        assertEquals("ki-einfuehrung", lesson.slug)
        assertEquals("KI Einführung", lesson.title)
        assertEquals(1, lesson.lesson)
        assertNotNull(lesson.body)
        assertTrue(lesson.body!!.contains("Was ist KI?"))
    }

    @Test
    fun `getLesson handles missing optional fields`() = runBlocking {
        val jsonResponse = """
            {
                "slug": "ki-einfuehrung",
                "title": "KI Einführung"
            }
        """.trimIndent()

        mockWebServer.enqueue(MockResponse()
            .setBody(jsonResponse)
            .setResponseCode(200)
            .setHeader("Content-Type", "application/json"))

        val lesson = apiService.getLesson("ki-einfuehrung")

        assertEquals("ki-einfuehrung", lesson.slug)
        assertEquals("KI Einführung", lesson.title)
        assertEquals(null, lesson.lesson)
        // body defaults to "" with coerceInputValues = true
        assertEquals("", lesson.body)
    }

    // ── /api/content/lessons Contract Tests ─────────────────────────────

    @Test
    fun `getLessons returns list of lessons`() = runBlocking {
        val jsonResponse = """
            {
                "lessons": [
                    {"slug": "ki-einfuehrung", "title": "KI Einführung", "lesson": 1},
                    {"slug": "ki-typen", "title": "KI Typen", "lesson": 2},
                    {"slug": "ki-anwendung", "title": "KI Anwendung", "lesson": 3}
                ]
            }
        """.trimIndent()

        mockWebServer.enqueue(MockResponse()
            .setBody(jsonResponse)
            .setResponseCode(200)
            .setHeader("Content-Type", "application/json"))

        val response = apiService.getLessons()

        assertEquals(3, response.lessons.size)
        assertEquals("ki-einfuehrung", response.lessons[0].slug)
        assertEquals("ki-typen", response.lessons[1].slug)
    }

    @Test
    fun `getLessons handles empty list`() = runBlocking {
        val jsonResponse = """{"lessons": []}"""

        mockWebServer.enqueue(MockResponse()
            .setBody(jsonResponse)
            .setResponseCode(200)
            .setHeader("Content-Type", "application/json"))

        val response = apiService.getLessons()
        assertEquals(0, response.lessons.size)
    }

    // ── /api/store/subscription-status Contract Tests ──────────────────

    @Test
    fun `getSubscriptionStatus returns subscribed status`() = runBlocking {
        val jsonResponse = """
            {
                "subscribed": true,
                "plan": "premium",
                "expiresAt": "2025-12-31T23:59:59Z"
            }
        """.trimIndent()

        mockWebServer.enqueue(MockResponse()
            .setBody(jsonResponse)
            .setResponseCode(200)
            .setHeader("Content-Type", "application/json"))

        val status = apiService.getSubscriptionStatus()
        assertTrue(status.subscribed)
    }

    @Test
    fun `getSubscriptionStatus returns not subscribed`() = runBlocking {
        val jsonResponse = """{"subscribed": false}"""

        mockWebServer.enqueue(MockResponse()
            .setBody(jsonResponse)
            .setResponseCode(200)
            .setHeader("Content-Type", "application/json"))

        val status = apiService.getSubscriptionStatus()
        assertFalse(status.subscribed)
    }
}
