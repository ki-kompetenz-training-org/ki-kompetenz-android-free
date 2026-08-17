package ai.ki_kompetenz_training_org.data.api

/**
 * Data Transfer Object for quiz questions embedded in lessons.
 * Used for "test-to-unlock" functionality.
 */
data class QuizQuestionDto(
    val id: String,
    val question: String,
    val options: List<String>,  // e.g., ["Option A", "Option B", "Option C", "Option D"]
    val correctAnswerIndex: Int,  // 0-based index of the correct answer
    val explanation: String? = null,  // Optional explanation shown after answer
    val points: Int = 10,  // Points for this question
)

/**
 * Result of a user's answer to a quiz question.
 */
data class QuizAnswerDto(
    val questionId: String,
    val selectedOptionIndex: Int,
    val isCorrect: Boolean,
)

/**
 * Complete quiz result for a lesson.
 */
data class LessonQuizResultDto(
    val lessonSlug: String,
    val totalQuestions: Int,
    val correctAnswers: Int,
    val score: Int,  // Percentage
    val passed: Boolean,  // Whether the pass threshold was met
    val answers: List<QuizAnswerDto> = emptyList(),
)
