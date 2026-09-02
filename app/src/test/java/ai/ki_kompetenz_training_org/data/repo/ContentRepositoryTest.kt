package ai.ki_kompetenz_training_org.data.repo

import ai.ki_kompetenz_training_org.data.api.ApiJson
import ai.ki_kompetenz_training_org.data.api.ApiService
import ai.ki_kompetenz_training_org.data.db.AppDatabase
import ai.ki_kompetenz_training_org.data.db.ContentDao
import com.google.common.truth.Truth.assertThat
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit

/**
 * Integration-Tests für [ContentRepository] gegen einen MockWebServer.
 *
 * Retrofit wird wie in NetworkModule aufgebaut — nur mit ApiJson als
 * kanonischer Json-Konfiguration (isLenient=true, ignoreUnknownKeys=true,
 * coerceInputValues=true) und einem minimalen OkHttpClient (kein Retry-
 * Interceptor, keine echten Netzwerk-Calls). Die Room-DB ist Android-gebunden
 * und wird mit mockk (relaxed) ersetzt, damit der Cache-Pfad (upsertLesson)
 * beobachtbar bleibt.
 *
 * Kein Android-Kontext nötig: der komplette Fetch-Pfad läuft über ApiService +
 * Cache-DAO, beides hier per Retrofit/MockWebServer bzw. mockk vorhanden.
 */
class ContentRepositoryTest {

    private lateinit var server: MockWebServer
    private lateinit var contentDao: ContentDao
    private lateinit var repository: ContentRepository

    /** Echter Geräte-Payload (2026-09-01, GET /api/content/lessons):
     *  `lesson` als STRING "0"/"1" — must parse to Int thanks to ApiJson. */
    private val realDeviceLessonsPayload = """
        {
          "lessons": [
            {
              "slug": "lesson-1",
              "title": "Was ist Künstliche Intelligenz?",
              "lesson": "0",
              "duration": "15 min",
              "description": "Grundlagen: KI definieren.",
              "objectives": ["KI unterscheiden", "KI-Arten benennen"]
            },
            {
              "slug": "lesson-2",
              "title": "Maschinelles Lernen",
              "lesson": "1",
              "duration": "20 min",
              "description": "Wie KI lernt.",
              "objectives": []
            }
          ]
        }
    """.trimIndent()

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()

        val contentType = "application/json".toMediaType()
        val api: ApiService = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .client(OkHttpClient())
            .addConverterFactory(ApiJson.asConverterFactory(contentType))
            .build()
            .create(ApiService::class.java)

        contentDao = mockk(relaxed = true)
        val db = mockk<AppDatabase>(relaxed = true)
        every { db.contentDao() } returns contentDao

        repository = ContentRepository(api, db)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    // ── fetchLessons ─────────────────────────────────────────────────────────

    @Test
    fun `fetchLessons - Erfolg - echter Geräte-Payload (lesson als String) liefert success mit 2 Lektionen und lesson==0`() =
        runBlocking {
            server.enqueue(
                MockResponse().setResponseCode(200).setBody(realDeviceLessonsPayload)
            )

            val result = repository.fetchLessons()

            assertThat(result.isSuccess).isTrue()
            val lessons = result.getOrThrow()
            assertThat(lessons).hasSize(2)
            assertThat(lessons[0].slug).isEqualTo("lesson-1")
            assertThat(lessons[0].lesson).isEqualTo(0)   // "0" (String) → 0 (Int)
            assertThat(lessons[1].lesson).isEqualTo(1)
            assertThat(lessons[0].objectives)
                .containsExactly("KI unterscheiden", "KI-Arten benennen")

            // Dokumentiert den Netzwerk-Vertrag: GET /api/content/lessons
            val request = server.takeRequest()
            assertThat(request.path).isEqualTo("/api/content/lessons")
        }

    @Test
    fun `fetchLessons - HTTP 500 liefert Result_failure und schreibt keinen Cache`() =
        runBlocking {
            server.enqueue(
                MockResponse().setResponseCode(500).setBody("Internal Server Error")
            )

            val result = repository.fetchLessons()

            assertThat(result.isFailure).isTrue()
            coVerify(exactly = 0) { contentDao.upsertLesson(any()) }
        }

    @Test
    fun `fetchLessons - HTML-Fehlerseite mit 200 liefert Result_failure statt Crash`() =
        runBlocking {
            server.enqueue(
                MockResponse().setResponseCode(200)
                    .setBody("<!DOCTYPE html><html><body>Not Found</body></html>")
            )

            val result = repository.fetchLessons()

            assertThat(result.isFailure).isTrue()
        }

    @Test
    fun `fetchLessons - leeres lessons-Array liefert success mit leerer Liste`() =
        runBlocking {
            server.enqueue(
                MockResponse().setResponseCode(200).setBody("""{"lessons":[]}""")
            )

            val result = repository.fetchLessons()

            assertThat(result.isSuccess).isTrue()
            assertThat(result.getOrThrow()).isEmpty()
            coVerify(exactly = 0) { contentDao.upsertLesson(any()) }
        }

    @Test
    fun `fetchLessons - Erfolg speichert jede Lektion im Room-Cache (lessonNumber + objectivesJson)`() =
        runBlocking {
            server.enqueue(
                MockResponse().setResponseCode(200).setBody(realDeviceLessonsPayload)
            )

            repository.fetchLessons()

            coVerify {
                contentDao.upsertLesson(
                    match {
                        it.slug == "lesson-1" &&
                            it.lessonNumber == 0 &&   // "0" als String koertiert
                            it.objectivesJson.contains("KI unterscheiden")
                    }
                )
            }
        }

    @Test
    fun `fetchLessons - übermittelt lang-Query-Parameter wie der Produktionsclient`() =
        runBlocking {
            server.enqueue(
                MockResponse().setResponseCode(200).setBody(realDeviceLessonsPayload)
            )

            val result = repository.fetchLessons("de")

            assertThat(result.isSuccess).isTrue()
            val request = server.takeRequest()
            assertThat(request.path).isEqualTo("/api/content/lessons?lang=de")
        }

    // ── fetchLesson ──────────────────────────────────────────────────────────

    @Test
    fun `fetchLesson - Erfolg parst lesson und body`() =
        runBlocking {
            server.enqueue(
                MockResponse().setResponseCode(200).setBody(
                    """
                    {
                      "slug": "lesson-3",
                      "title": "Neuronale Netze",
                      "lesson": "2",
                      "duration": "30 min",
                      "description": "Wie KI lernt.",
                      "objectives": ["Gewichte", "Aktivierung"],
                      "body": "# Markdown-Inhalt\n\nEin künstliches Neuron..."
                    }
                    """.trimIndent()
                )
            )

            val result = repository.fetchLesson("lesson-3")

            assertThat(result.isSuccess).isTrue()
            val lesson = result.getOrThrow()
            assertThat(lesson.slug).isEqualTo("lesson-3")
            assertThat(lesson.lesson).isEqualTo(2)       // "2" (String) → 2
            assertThat(lesson.body).contains("Markdown-Inhalt")

            val request = server.takeRequest()
            assertThat(request.path).isEqualTo("/api/content/lessons/lesson-3")
        }

    @Test
    fun `fetchLesson - HTTP 404 liefert Result_failure und schreibt keinen Cache`() =
        runBlocking {
            server.enqueue(
                MockResponse().setResponseCode(404).setBody("""{"error":"Not Found"}""")
            )

            val result = repository.fetchLesson("gibt-es-nicht")

            assertThat(result.isFailure).isTrue()
            coVerify(exactly = 0) { contentDao.upsertLesson(any()) }
        }

    @Test
    fun `fetchLesson - Erfolg speichert body im Room-Cache`() =
        runBlocking {
            server.enqueue(
                MockResponse().setResponseCode(200).setBody(
                    """
                    {
                      "slug": "lesson-3",
                      "title": "Neuronale Netze",
                      "lesson": "2",
                      "body": "# Markdown-Inhalt"
                    }
                    """.trimIndent()
                )
            )

            val result = repository.fetchLesson("lesson-3")

            assertThat(result.isSuccess).isTrue()
            coVerify {
                contentDao.upsertLesson(match { it.slug == "lesson-3" && it.body!!.contains("Markdown-Inhalt") })
            }
        }
}
