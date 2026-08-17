package ai.ki_kompetenz_training_org.data.api

import kotlinx.serialization.Serializable

/** Core data transfer objects for the ki-kompetenz-training.org REST API.
 *
 * All DTOs use Kotlin Serialization for JSON parsing.
 * Default empty values ensure safe deserialization when fields are missing.
 */

// ── Content API ─────────────────────────────────────────────────────────────

/** Summary of a lesson as returned by the lessons list endpoint. */
@Serializable
data class LessonSummaryDto(
    val slug: String = "",
    val title: String = "",
    val lesson: Int? = null,
    val duration: String? = null,
    val description: String = "",
    val objectives: List<String> = emptyList(),
)

@Serializable
data class LessonsResponseDto(
    val lessons: List<LessonSummaryDto> = emptyList(),
)

/** Detailed lesson content including markdown body and quiz questions. */
@Serializable
data class LessonDetailDto(
    val slug: String = "",
    val title: String = "",
    val lesson: Int? = null,
    val duration: String? = null,
    val description: String = "",
    val objectives: List<String> = emptyList(),
    val body: String = "",
)

// ── KI-Score quiz data (mirrors messages/de.json KiScore) ───────────────────

/** A single quiz question with 4 answer options, correct index, and explanation. */
@Serializable
data class KiScoreQuestionDto(
    val id: Int = 0,
    val text: String = "",
    val options: List<String> = emptyList(),
    val correct: Int = 0,
    val explanation: String = "",
    val emoji: String = "",
)

@Serializable
data class KiScoreTierDto(
    val min: Int = 0,
    val max: Int = 0,
    val title: String = "",
    val emoji: String = "",
    val description: String = "",
)

@Serializable
data class KiScoreShareDto(
    val prefix: String = "",
    val invite: String = "",
    val hashtags: String = "",
)

@Serializable
data class KiScoreDataDto(
    val questions: List<KiScoreQuestionDto> = emptyList(),
    val tiers: List<KiScoreTierDto> = emptyList(),
    val share: KiScoreShareDto? = null,
)

// ── Team API (DSGVO: only names/scores, never emails) ───────────────────────

/** Team membership and leaderboard information. */
@Serializable
data class TeamDto(
    val id: String = "",
    val name: String = "",
    val isAdmin: Boolean = false,
)

@Serializable
data class MemberDto(
    val name: String? = null,
    val score: Int? = null,
    val tier: String? = null,
    val completedAt: String? = null,
)

@Serializable
data class MyTeamResponseDto(
    val team: TeamDto? = null,
    val member: MemberDto? = null,
)

@Serializable
data class LeaderboardEntryDto(
    val rank: Int = 0,
    val name: String? = null,
    val score: Int? = null,
    val tier: String? = null,
    val completedAt: String? = null,
    val isMe: Boolean = false,
)

@Serializable
data class TeamStatsResponseDto(
    val team: TeamDto? = null,
    val members: Int = 0,
    val withScores: Int = 0,
    val avgScore: Int = 0,
    val tierCount: Map<String, Int> = emptyMap(),
    val leaderboard: List<LeaderboardEntryDto> = emptyList(),
    val ownRank: Int? = null,
    val ownScore: Int? = null,
)

@Serializable
data class ErrorResponseDto(val error: String = "")