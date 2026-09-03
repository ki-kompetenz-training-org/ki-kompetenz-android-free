package ai.ki_kompetenz_training_org.api

import ai.ki_kompetenz_training_org.data.api.ApiJson
import ai.ki_kompetenz_training_org.data.api.ApiService
import ai.ki_kompetenz_training_org.data.api.SrsReviewRequestDto
import com.google.common.truth.Truth.assertThat
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit

/**
 * Contract tests for the remaining ApiService endpoints (ki-score, auth/me,
 * team, SRS) against MockWebServer, using the canonical [ApiJson] converter —
 * the same setup the production Retrofit client uses.
 */
class ApiEndpointsContractTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var apiService: ApiService

    @Before
    fun setup() {
        mockWebServer = MockWebServer()

        val contentType = "application/json".toMediaType()
        val retrofit = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addConverterFactory(ApiJson.asConverterFactory(contentType))
            .build()

        apiService = retrofit.create(ApiService::class.java)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    private fun ok(body: String): MockResponse = MockResponse()
        .setResponseCode(200)
        .setHeader("Content-Type", "application/json")
        .setBody(body)

    // ── GET api/content/ki-score ──────────────────────────────────────────

    @Test
    fun `getKiScoreData parses questions tiers and share`() = runBlocking {
        mockWebServer.enqueue(ok("""
            {
                "questions": [
                    {
                        "id": 1,
                        "text": "Was ist ein neuronales Netz?",
                        "options": ["Ein Computervirus", "Ein von Daten lernendes Modell", "Ein Browser-Plugin", "Ein Chatroom"],
                        "correct": 1,
                        "explanation": "Neuronale Netze lernen Muster aus Daten statt fester Regeln.",
                        "emoji": "🧠"
                    }
                ],
                "tiers": [
                    {"min": 0, "max": 2, "title": "KI-Neuling", "emoji": "🐣", "description": "Am Anfang der Reise."}
                ],
                "share": {
                    "prefix": "Mein KI-Score:",
                    "invite": "Mach auch den Test!",
                    "hashtags": "#KiScore #Weiterbildung"
                }
            }
        """.trimIndent()))

        val data = apiService.getKiScoreData()

        assertThat(data.questions).hasSize(1)
        val question = data.questions[0]
        assertThat(question.id).isEqualTo(1)
        assertThat(question.text).isEqualTo("Was ist ein neuronales Netz?")
        assertThat(question.options).hasSize(4)
        assertThat(question.options[1]).isEqualTo("Ein von Daten lernendes Modell")
        assertThat(question.correct).isEqualTo(1)
        assertThat(question.explanation).contains("Neuronale Netze")
        assertThat(question.emoji).isEqualTo("🧠")

        assertThat(data.tiers).hasSize(1)
        val tier = data.tiers[0]
        assertThat(tier.min).isEqualTo(0)
        assertThat(tier.max).isEqualTo(2)
        assertThat(tier.title).isEqualTo("KI-Neuling")
        assertThat(tier.description).isEqualTo("Am Anfang der Reise.")

        assertThat(data.share).isNotNull()
        assertThat(data.share!!.prefix).isEqualTo("Mein KI-Score:")
        assertThat(data.share!!.invite).isEqualTo("Mach auch den Test!")
        assertThat(data.share!!.hashtags).contains("KiScore")
    }

    @Test
    fun `getKiScoreData tolerates missing nullable share`() = runBlocking {
        mockWebServer.enqueue(ok("""
            {
                "questions": [],
                "tiers": [
                    {"min": 0, "max": 0, "title": "KI-Neuling", "emoji": "🐣"}
                ]
            }
        """.trimIndent()))

        val data = apiService.getKiScoreData()

        assertThat(data.questions).isEmpty()
        assertThat(data.tiers).hasSize(1)
        assertThat(data.share).isNull()
    }

    // ── GET api/auth/me ───────────────────────────────────────────────────

    @Test
    fun `getMe parses id and email with name omitted`() = runBlocking {
        // `name` is nullable — a minimal session payload may omit it.
        mockWebServer.enqueue(ok("""
            {"id": "usr_8f3a", "email": "tobias@example.org"}
        """.trimIndent()))

        val me = apiService.getMe()

        assertThat(me.id).isEqualTo("usr_8f3a")
        assertThat(me.email).isEqualTo("tobias@example.org")
        assertThat(me.name).isNull()
    }

    // ── GET api/team/me ───────────────────────────────────────────────────

    @Test
    fun `getMyTeam parses team membership and own member entry`() = runBlocking {
        // Member omits the nullable `completedAt`.
        mockWebServer.enqueue(ok("""
            {
                "team": {"id": "team-1", "name": "Alpha Team", "isAdmin": true},
                "member": {"name": "Tobias", "score": 11, "tier": "Gering"}
            }
        """.trimIndent()))

        val myTeam = apiService.getMyTeam()

        assertThat(myTeam.team).isNotNull()
        assertThat(myTeam.team!!.id).isEqualTo("team-1")
        assertThat(myTeam.team!!.name).isEqualTo("Alpha Team")
        assertThat(myTeam.team!!.isAdmin).isTrue()

        assertThat(myTeam.member).isNotNull()
        assertThat(myTeam.member!!.name).isEqualTo("Tobias")
        assertThat(myTeam.member!!.score).isEqualTo(11)
        assertThat(myTeam.member!!.tier).isEqualTo("Gering")
        assertThat(myTeam.member!!.completedAt).isNull()
    }

    // ── GET api/team/{id}/stats ───────────────────────────────────────────

    @Test
    fun `getTeamStats parses stats and requests the team path`() = runBlocking {
        mockWebServer.enqueue(ok("""
            {
                "team": {"id": "team-1", "name": "Alpha Team", "isAdmin": false},
                "members": 4,
                "withScores": 3,
                "avgScore": 9,
                "tierCount": {"Minimal": 2, "Gering": 1},
                "leaderboard": [
                    {"rank": 1, "name": "Tobias", "score": 12, "tier": "Gering", "isMe": true},
                    {"rank": 2, "name": "Ana", "score": 8, "tier": "Minimal"}
                ],
                "ownRank": 1,
                "ownScore": 12
            }
        """.trimIndent()))

        val stats = apiService.getTeamStats("team-1")

        val recorded = mockWebServer.takeRequest()
        assertThat(recorded.method).isEqualTo("GET")
        assertThat(recorded.path).contains("api/team/team-1/stats")

        assertThat(stats.team!!.name).isEqualTo("Alpha Team")
        assertThat(stats.members).isEqualTo(4)
        assertThat(stats.withScores).isEqualTo(3)
        assertThat(stats.avgScore).isEqualTo(9)
        assertThat(stats.tierCount).containsExactly("Minimal", 2, "Gering", 1)

        assertThat(stats.leaderboard).hasSize(2)
        assertThat(stats.leaderboard[0].rank).isEqualTo(1)
        assertThat(stats.leaderboard[0].isMe).isTrue()
        assertThat(stats.leaderboard[1].name).isEqualTo("Ana")
        assertThat(stats.leaderboard[1].isMe).isFalse()

        assertThat(stats.ownRank).isEqualTo(1)
        assertThat(stats.ownScore).isEqualTo(12)
    }

    // ── GET api/srs/due ───────────────────────────────────────────────────

    @Test
    fun `getDueCards parses cards with and without nextReview`() = runBlocking {
        mockWebServer.enqueue(ok("""
            {
                "cards": [
                    {
                        "id": "card-42",
                        "lessonId": "lesson-3",
                        "question": "Was bedeutet 'Machine Learning'?",
                        "answer": "Algorithmen lernen Muster aus Daten.",
                        "interval": 6,
                        "repetitions": 2,
                        "stability": 4.5,
                        "difficulty": 5.2,
                        "nextReview": "2026-03-01T09:00:00.000Z",
                        "reviewCount": 3
                    },
                    {
                        "id": "card-7",
                        "lessonId": "lesson-1",
                        "question": "Nenne ein Beispiel für KI im Alltag.",
                        "answer": "Sprachassistenten.",
                        "interval": 1,
                        "repetitions": 0,
                        "stability": 1.0,
                        "difficulty": 5.0
                    }
                ],
                "count": 2
            }
        """.trimIndent()))

        val due = apiService.getDueCards()

        assertThat(due.count).isEqualTo(2)
        assertThat(due.cards).hasSize(2)

        val first = due.cards[0]
        assertThat(first.id).isEqualTo("card-42")
        assertThat(first.lessonId).isEqualTo("lesson-3")
        assertThat(first.question).isEqualTo("Was bedeutet 'Machine Learning'?")
        assertThat(first.answer).contains("Daten")
        assertThat(first.interval).isEqualTo(6)
        assertThat(first.repetitions).isEqualTo(2)
        assertThat(first.stability).isEqualTo(4.5)
        assertThat(first.difficulty).isEqualTo(5.2)
        assertThat(first.nextReview).isEqualTo("2026-03-01T09:00:00.000Z")
        assertThat(first.reviewCount).isEqualTo(3)

        // Nullable `nextReview` omitted → null, everything else keeps defaults.
        val second = due.cards[1]
        assertThat(second.id).isEqualTo("card-7")
        assertThat(second.nextReview).isNull()
        assertThat(second.reviewCount).isEqualTo(0)
    }

    // ── POST api/srs/review ───────────────────────────────────────────────

    @Test
    fun `postReview posts body and parses scheduling result`() = runBlocking {
        mockWebServer.enqueue(ok("""
            {
                "success": true,
                "result": {
                    "cardId": "card-42",
                    "interval": 10,
                    "nextReview": "2026-03-11T09:00:00.000Z",
                    "stability": 6.1,
                    "difficulty": 4.8,
                    "repetitions": 3
                }
            }
        """.trimIndent()))

        val response = apiService.postReview(SrsReviewRequestDto(cardId = "card-42", quality = 4))

        val recorded = mockWebServer.takeRequest()
        assertThat(recorded.method).isEqualTo("POST")
        assertThat(recorded.path).isEqualTo("/api/srs/review")

        val sentBody = Json.parseToJsonElement(recorded.body.readUtf8()).jsonObject
        assertThat(sentBody["cardId"]?.jsonPrimitive?.contentOrNull).isEqualTo("card-42")
        assertThat(sentBody["quality"]?.jsonPrimitive?.int).isEqualTo(4)

        assertThat(response.success).isTrue()
        val result = response.result
        assertThat(result).isNotNull()
        assertThat(result!!.cardId).isEqualTo("card-42")
        assertThat(result.interval).isEqualTo(10)
        assertThat(result.nextReview).isEqualTo("2026-03-11T09:00:00.000Z")
        assertThat(result.stability).isEqualTo(6.1)
        assertThat(result.difficulty).isEqualTo(4.8)
        assertThat(result.repetitions).isEqualTo(3)
    }

    // ── HTTP 500 error contract ───────────────────────────────────────────

    @Test
    fun `getKiScoreData fails on HTTP 500`() = runBlocking {
        mockWebServer.enqueue(MockResponse()
            .setResponseCode(500)
            .setHeader("Content-Type", "application/json")
            .setBody("""{"error": "Internal Server Error"}"""))

        val result = runCatching { apiService.getKiScoreData() }

        assertThat(result.isFailure).isTrue()
    }
}
