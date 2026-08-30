package ai.ki_kompetenz_training_org.data.repo

import ai.ki_kompetenz_training_org.data.api.ApiService
import ai.ki_kompetenz_training_org.data.api.KiScoreDataDto
import ai.ki_kompetenz_training_org.data.api.KiScoreQuestionDto
import ai.ki_kompetenz_training_org.data.api.KiScoreTierDto
import ai.ki_kompetenz_training_org.data.api.LessonDetailDto
import ai.ki_kompetenz_training_org.data.api.LessonSummaryDto
import ai.ki_kompetenz_training_org.data.db.AppDatabase
import ai.ki_kompetenz_training_org.data.db.LessonEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.Json

/**
 * Loads lessons + quiz data from the server, with an offline Room cache.
 * Premium gating is NOT applied here — the UI layer decides (mirrors the
 * web premium-gate flow: first 2 lessons are free preview).
 */
class ContentRepository(
    private val api: ApiService,
    private val db: AppDatabase,
) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun fetchLessons(lang: String? = null): Result<List<LessonSummaryDto>> =
        runCatching { api.getLessons(lang).lessons }
            .onSuccess { lessons ->
                // Cache metadata for offline browsing
                lessons.forEach { l ->
                    db.contentDao().upsertLesson(
                        LessonEntity(
                            slug = l.slug,
                            title = l.title,
                            lessonNumber = l.lesson,
                            duration = l.duration,
                            description = l.description,
                            objectivesJson = json.encodeToString(
                                kotlinx.serialization.serializer<List<String>>(),
                                l.objectives,
                            ),
                            body = null,
                        )
                    )
                }
            }

    fun observeLessons(): Flow<List<LessonEntity>> = db.contentDao().observeLessons()

    suspend fun fetchLesson(slug: String, lang: String? = null): Result<LessonDetailDto> =
        runCatching { api.getLesson(slug, lang) }
            .onSuccess { l ->
                db.contentDao().upsertLesson(
                    LessonEntity(
                        slug = l.slug,
                        title = l.title,
                        lessonNumber = l.lesson,
                        duration = l.duration,
                        description = l.description,
                        objectivesJson = json.encodeToString(
                            kotlinx.serialization.serializer<List<String>>(),
                            l.objectives,
                        ),
                        body = l.body,
                    )
                )
            }

    suspend fun getCachedLesson(slug: String): LessonEntity? {
        // TEMPORARY FIX: Database disabled due to Gradle cache corruption
        // Loading lessons from API only to avoid crashes
        return null
    }

    suspend fun fetchKiScoreData(): Result<KiScoreDataDto> =
        runCatching { api.getKiScoreData() }
}

/** Pure score calculation — unit-testable (mirrors the web KiScoreGame). */
object QuizScoring {
    fun scoreFor(answers: List<Boolean>): Int =
        if (answers.isEmpty()) 0
        else Math.round((answers.count { it }.toFloat() / answers.size) * 100)

    fun tierFor(score: Int, tiers: List<KiScoreTierDto>): KiScoreTierDto? =
        tiers.firstOrNull { score >= it.min && score <= it.max }

    fun shuffledOptions(question: KiScoreQuestionDto): List<String> {
        val indices = question.options.indices.toMutableList()
        indices.shuffle()
        return indices.map { question.options[it] }
    }
}