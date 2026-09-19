package ai.ki_kompetenz_training_org.data.lessons

object Lesson16 {
    val lesson = InteractiveLesson(
        id = "lesson-16",
        lessonNumber = 16,
        titleDe = "KI im erweiterten Kontext",
        titleEn = "AI in the Extended Context",
        descriptionDe = "KI in Bildung, Gesundheit, Medien und Gesellschaft — Chancen und Grenzen.",
        descriptionEn = "AI in education, health, media and society — opportunities and limits.",
        durationMinutes = 20,
        objectivesDe = listOf(
            "Anwendungsfelder von KI jenseits des Buerroalltags einordnen",
            "Chancen und Risiken je Kontext abwaegen",
            "Gesellschaftliche Entwicklungen bewerten",
        ),
        objectivesEn = listOf(
            "Assess AI application fields beyond office work",
            "Weigh opportunities and risks per context",
            "Evaluate societal developments",
        ),
        sections = listOf(
            LessonSection(
                titleDe = "1. KI in Bildung und Gesundheit",
                titleEn = "1. AI in Education and Health",
                blocks = listOf(
                    ContentBlock.Text(
                        textDe = "Bildung: KI-Tutoren personalisieren Lernpfade, automatisches Feedback entlastet Lehrende — aber Bias in Bewertungsalgorithmen kann Benachteiligung verstaerken. Gesundheit: Diagnostik-Unterstützung erreicht teils Expertenniveau (Radiologie, Dermatologie), Drug-Discovery verkuerzt Forschungszyklen — aber Fehldiagnosen ohne aerztliche Kontrolle bleiben Hochrisiko und sind im AI Act entsprechend reguliert.",
                        textEn = "Education: AI tutors personalize learning paths, automated feedback relieves teachers — but bias in grading algorithms can reinforce disadvantage. Health: diagnostic support sometimes reaches expert level (radiology, dermatology), drug discovery shortens research cycles — but misdiagnoses without medical control remain high-risk and are regulated accordingly in the AI Act.",
                    ),
                    ContentBlock.Quiz(
                        questionDe = "Warum gilt KI-Diagnostik im AI Act als Hochrisiko?",
                        questionEn = "Why does AI diagnostics count as high-risk under the AI Act?",
                        options = listOf(
                            QuizOption("Weil die Technik neu ist", "Because the technology is new", isCorrect = false),
                            QuizOption("Weil Gesundheit und Sicherheit der Menschen betroffen sind", "Because people's health and safety are affected", isCorrect = true),
                            QuizOption("Weil sie teuer ist", "Because it is expensive", isCorrect = false),
                            QuizOption("Weil nur Aerzte sie bedienen duerfen", "Because only doctors may use it", isCorrect = false),
                        ),
                        explanationDe = "Richtig! Entscheidend ist das Schaedenspotenzial fuer Gesundheit und Sicherheit, nicht Neuheit oder Kosten.",
                        explanationEn = "Correct! What matters is the damage potential for health and safety, not novelty or cost.",
                    ),
                ),
            ),
            LessonSection(
                titleDe = "2. Gesellschaftliche Perspektiven",
                titleEn = "2. Societal Perspectives",
                blocks = listOf(
                    ContentBlock.Text(
                        textDe = "Arbeitsmarkt: KI ersetzt eher Taetigkeiten als Berufe — Neuverteilung erfordert Weiterbildung. Medien: Deepfakes und KI-generierte Inhalte bedrohen Vertrauen; Kennzeichnungspflichten (AI Act Art. 50) und Wasserzeichen sind Gegenmassnahmen. Umwelt: Training und Betrieb verbrauchen erhebliche Energie (siehe Lektion 14). Faehigkeit, KI realistisch einzuordnen, wird zur Grundkompetenz — genau das, was dieses Training aufbaut.",
                        textEn = "Labor market: AI replaces tasks rather than professions — redistribution requires retraining. Media: deepfakes and AI-generated content threaten trust; labeling duties (AI Act Art. 50) and watermarks are countermeasures. Environment: training and operation consume significant energy (see lesson 14). The ability to assess AI realistically becomes a foundational skill — exactly what this training builds.",
                    ),
                    ContentBlock.Classification(
                        instructionDe = "Einordnung: Chance oder Risiko?",
                        instructionEn = "Classify: opportunity or risk?",
                        categories = listOf(
                            ClassificationCategory(
                                nameDe = "Chance", nameEn = "Opportunity", emoji = "🌱",
                                items = listOf(
                                    ClassificationItem("Personalisierte Lernpfade", "Personalized learning paths"),
                                    ClassificationItem("Schnellere Wirkstoffforschung", "Faster drug research"),
                                    ClassificationItem("Entlastung bei Routinearbeit", "Relief from routine work"),
                                ),
                            ),
                            ClassificationCategory(
                                nameDe = "Risiko", nameEn = "Risk", emoji = "⚠️",
                                items = listOf(
                                    ClassificationItem("Deepfakes untergraben Vertrauen", "Deepfakes undermine trust"),
                                    ClassificationItem("Bewertungs-Bias benachteiligt", "Grading bias discriminates"),
                                    ClassificationItem("Energieverbrauch des Betriebs", "Operating energy consumption"),
                                ),
                            ),
                        ),
                    ),
                    ContentBlock.RiskThermometer(),
                ),
            ),
        ),
        cognitiveLevel = CognitiveLevel.APPLICATION,
    )
}
