package ai.ki_kompetenz_training_org.data.lessons

import kotlinx.serialization.Serializable

// ── Interactive content blocks ──────────────────────────────────────────────

/** A single interactive block within a lesson section. */
@Serializable
sealed class ContentBlock {
    /** Informative paragraph. */
    @Serializable
    data class Text(
        val textDe: String,
        val textEn: String,
    ) : ContentBlock()

    /** Highlighted callout box (tip, warning, example). */
    @Serializable
    data class Callout(
        val type: CalloutType,
        val textDe: String,
        val textEn: String,
    ) : ContentBlock()

    /** Inline knowledge check — tap to reveal answer. */
    @Serializable
    data class KnowledgeCheck(
        val questionDe: String,
        val questionEn: String,
        val answerDe: String,
        val answerEn: String,
    ) : ContentBlock()

    /** Classification exercise: drag items into correct categories. */
    @Serializable
    data class Classification(
        val instructionDe: String,
        val instructionEn: String,
        val categories: List<ClassificationCategory>,
    ) : ContentBlock()

    /** Multiple-choice quiz question. */
    @Serializable
    data class Quiz(
        val questionDe: String,
        val questionEn: String,
        val options: List<QuizOption>,
        val explanationDe: String,
        val explanationEn: String,
    ) : ContentBlock()

    /** Fill-in-the-blank: tap the correct word from choices. */
    @Serializable
    data class FillBlank(
        val sentenceDe: String,
        val sentenceEn: String,
        val blankKey: String,
        val choices: List<String>,
        val correctIndex: Int,
        val explanationDe: String,
        val explanationEn: String,
    ) : ContentBlock()

    /** True/False statement with explanation. */
    @Serializable
    data class TrueFalse(
        val statementDe: String,
        val statementEn: String,
        val isTrue: Boolean,
        val explanationDe: String,
        val explanationEn: String,
    ) : ContentBlock()

    /** Interactive EU AI Act Risk Thermometer (3D-style visualization). */
    @Serializable
    data class RiskThermometer(
        val titleDe: String = "EU AI Act Risiko-Thermometer",
        val titleEn: String = "EU AI Act Risk Thermometer",
    ) : ContentBlock()
}

@Serializable
enum class CalloutType {
    TIP,
    WARNING,
    EXAMPLE,
    DEFINITION,
    LAW,
}

@Serializable
data class ClassificationCategory(
    val nameDe: String,
    val nameEn: String,
    val emoji: String,
    val items: List<ClassificationItem>,
)

@Serializable
data class ClassificationItem(
    val textDe: String,
    val textEn: String,
)

@Serializable
data class QuizOption(
    val textDe: String,
    val textEn: String,
    val isCorrect: Boolean,
)

// ── Lesson section (grouped content blocks) ─────────────────────────────────

@Serializable
data class LessonSection(
    val titleDe: String,
    val titleEn: String,
    val blocks: List<ContentBlock>,
)

// ── Full interactive lesson ─────────────────────────────────────────────────

@Serializable
data class InteractiveLesson(
    val id: String,
    val lessonNumber: Int,
    val titleDe: String,
    val titleEn: String,
    val descriptionDe: String,
    val descriptionEn: String,
    val durationMinutes: Int,
    val objectivesDe: List<String>,
    val objectivesEn: List<String>,
    val sections: List<LessonSection>,
    val isPremium: Boolean = false,
)
