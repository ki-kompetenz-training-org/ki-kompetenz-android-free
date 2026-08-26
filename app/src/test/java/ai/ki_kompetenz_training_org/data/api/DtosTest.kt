package ai.ki_kompetenz_training_org.data.api

import kotlinx.serialization.json.Json
import org.junit.Assert.*
import org.junit.Test

class DtosTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun lessonSummaryDto_defaults_are_safe() {
        val dto = LessonSummaryDto()
        assertEquals("", dto.slug)
        assertEquals("", dto.title)
        assertNull(dto.lesson)
        assertNull(dto.duration)
        assertEquals("", dto.description)
        assertTrue(dto.objectives.isEmpty())
    }

    @Test
    fun lessonSummaryDto_deserializes_from_json() {
        val raw = """{"slug":"lesson-1","title":"Intro","lesson":1,"duration":"15min","description":"KI Basics","objectives":["Goal 1"]}"""
        val dto = json.decodeFromString<LessonSummaryDto>(raw)
        assertEquals("lesson-1", dto.slug)
        assertEquals("Intro", dto.title)
        assertEquals(1, dto.lesson)
        assertEquals("15min", dto.duration)
        assertEquals("KI Basics", dto.description)
        assertEquals(1, dto.objectives.size)
    }

    @Test
    fun lessonsResponseDto_default_is_empty_list() {
        val dto = LessonsResponseDto()
        assertTrue(dto.lessons.isEmpty())
    }

    @Test
    fun lessonsResponseDto_deserializes_lessons_array() {
        val raw = """{"lessons":[{"slug":"a","title":"T"}]}"""
        val dto = json.decodeFromString<LessonsResponseDto>(raw)
        assertEquals(1, dto.lessons.size)
        assertEquals("a", dto.lessons[0].slug)
    }

    @Test
    fun lessonDetailDto_default_includes_body() {
        val dto = LessonDetailDto()
        assertEquals("", dto.body)
    }

    @Test
    fun kiScoreQuestionDto_defaults_are_safe() {
        val dto = KiScoreQuestionDto()
        assertEquals(0, dto.id)
        assertEquals("", dto.text)
        assertTrue(dto.options.isEmpty())
        assertEquals(0, dto.correct)
        assertEquals("", dto.explanation)
        assertEquals("", dto.emoji)
    }

    @Test
    fun kiScoreQuestionDto_deserializes_full_question() {
        val raw = """{"id":42,"text":"What is AI?","options":["A","B","C","D"],"correct":1,"explanation":"B is correct","emoji":"question"}"""
        val dto = json.decodeFromString<KiScoreQuestionDto>(raw)
        assertEquals(42, dto.id)
        assertEquals("What is AI?", dto.text)
        assertEquals(4, dto.options.size)
        assertEquals(1, dto.correct)
    }

    @Test
    fun kiScoreTierDto_defaults() {
        val dto = KiScoreTierDto()
        assertEquals(0, dto.min)
        assertEquals(0, dto.max)
        assertEquals("", dto.title)
        assertEquals("", dto.emoji)
        assertEquals("", dto.description)
    }

    @Test
    fun kiScoreShareDto_defaults() {
        val dto = KiScoreShareDto()
        assertEquals("", dto.prefix)
        assertEquals("", dto.invite)
        assertEquals("", dto.hashtags)
    }

    @Test
    fun kiScoreDataDto_defaults() {
        val dto = KiScoreDataDto()
        assertTrue(dto.questions.isEmpty())
        assertTrue(dto.tiers.isEmpty())
        assertNull(dto.share)
    }

    @Test
    fun teamDto_defaults() {
        val dto = TeamDto()
        assertEquals("", dto.id)
        assertEquals("", dto.name)
        assertFalse(dto.isAdmin)
    }

    @Test
    fun memberDto_defaults_are_all_null() {
        val dto = MemberDto()
        assertNull(dto.name)
        assertNull(dto.score)
        assertNull(dto.tier)
        assertNull(dto.completedAt)
    }

    @Test
    fun myTeamResponseDto_defaults_are_null() {
        val dto = MyTeamResponseDto()
        assertNull(dto.team)
        assertNull(dto.member)
    }

    @Test
    fun leaderboardEntryDto_defaults() {
        val dto = LeaderboardEntryDto()
        assertEquals(0, dto.rank)
        assertNull(dto.name)
        assertNull(dto.score)
        assertFalse(dto.isMe)
    }

    @Test
    fun leaderboardEntryDto_deserializes_with_isMe_flag() {
        val raw = """{"rank":1,"name":"Alice","score":850,"tier":"KI-Experte","isMe":true}"""
        val dto = json.decodeFromString<LeaderboardEntryDto>(raw)
        assertEquals(1, dto.rank)
        assertEquals("Alice", dto.name)
        assertEquals(850, dto.score!!)
        assertTrue(dto.isMe)
    }

    @Test
    fun teamStatsResponseDto_defaults() {
        val dto = TeamStatsResponseDto()
        assertNull(dto.team)
        assertEquals(0, dto.members)
        assertEquals(0, dto.withScores)
        assertEquals(0, dto.avgScore)
        assertTrue(dto.tierCount.isEmpty())
        assertTrue(dto.leaderboard.isEmpty())
        assertNull(dto.ownRank)
        assertNull(dto.ownScore)
    }

    @Test
    fun errorResponseDto_defaults() {
        val dto = ErrorResponseDto()
        assertEquals("", dto.error)
    }

    @Test
    fun errorResponseDto_deserializes_error_message() {
        val dto = json.decodeFromString<ErrorResponseDto>("""{"error":"Not found"}""")
        assertEquals("Not found", dto.error)
    }
}
