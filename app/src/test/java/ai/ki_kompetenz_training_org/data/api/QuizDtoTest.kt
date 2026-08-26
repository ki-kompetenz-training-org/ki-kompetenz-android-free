package ai.ki_kompetenz_training_org.data.api

import org.junit.Assert.*
import org.junit.Test

class QuizDtoTest {

    @Test
    fun quizQuestionDto_defaults_are_safe() {
        val dto = QuizQuestionDto(
            id = "q1",
            question = "What is AI?",
            options = listOf("A", "B", "C", "D"),
            correctAnswerIndex = 0,
        )
        assertEquals("q1", dto.id)
        assertEquals("What is AI?", dto.question)
        assertEquals(4, dto.options.size)
        assertEquals(0, dto.correctAnswerIndex)
        assertNull(dto.explanation)
        assertEquals(10, dto.points)
    }

    @Test
    fun quizQuestionDto_with_explanation_and_points() {
        val dto = QuizQuestionDto(
            id = "q2",
            question = "What is ML?",
            options = listOf("A", "B"),
            correctAnswerIndex = 1,
            explanation = "ML is a subset of AI",
            points = 20,
        )
        assertEquals("ML is a subset of AI", dto.explanation)
        assertEquals(20, dto.points)
    }

    @Test
    fun quizAnswerDto_basic() {
        val dto = QuizAnswerDto(
            questionId = "q1",
            selectedOptionIndex = 2,
            isCorrect = true,
        )
        assertEquals("q1", dto.questionId)
        assertEquals(2, dto.selectedOptionIndex)
        assertTrue(dto.isCorrect)
    }

    @Test
    fun quizAnswerDto_incorrect() {
        val dto = QuizAnswerDto(
            questionId = "q2",
            selectedOptionIndex = 0,
            isCorrect = false,
        )
        assertFalse(dto.isCorrect)
    }

    @Test
    fun lessonQuizResultDto_defaults() {
        val dto = LessonQuizResultDto(
            lessonSlug = "lesson-1",
            totalQuestions = 10,
            correctAnswers = 7,
            score = 70,
            passed = true,
        )
        assertEquals("lesson-1", dto.lessonSlug)
        assertEquals(10, dto.totalQuestions)
        assertEquals(7, dto.correctAnswers)
        assertEquals(70, dto.score)
        assertTrue(dto.passed)
        assertTrue(dto.answers.isEmpty())
    }

    @Test
    fun lessonQuizResultDto_with_answers() {
        val answers = listOf(
            QuizAnswerDto("q1", 0, true),
            QuizAnswerDto("q2", 1, false),
        )
        val dto = LessonQuizResultDto(
            lessonSlug = "lesson-2",
            totalQuestions = 2,
            correctAnswers = 1,
            score = 50,
            passed = false,
            answers = answers,
        )
        assertEquals(2, dto.answers.size)
        assertEquals("q1", dto.answers[0].questionId)
        assertFalse(dto.passed)
    }

    @Test
    fun lessonQuizResultDto_failed_quiz() {
        val dto = LessonQuizResultDto(
            lessonSlug = "lesson-3",
            totalQuestions = 5,
            correctAnswers = 1,
            score = 20,
            passed = false,
        )
        assertFalse(dto.passed)
        assertEquals(20, dto.score)
    }

    @Test
    fun lessonQuizResultDto_perfect_score() {
        val dto = LessonQuizResultDto(
            lessonSlug = "lesson-4",
            totalQuestions = 10,
            correctAnswers = 10,
            score = 100,
            passed = true,
        )
        assertEquals(100, dto.score)
        assertTrue(dto.passed)
    }
}
