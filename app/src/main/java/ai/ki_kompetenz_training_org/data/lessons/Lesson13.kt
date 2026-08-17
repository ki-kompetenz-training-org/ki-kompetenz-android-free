package ai.ki_kompetenz_training_org.data.lessons

object Lesson13 {
    val lesson = InteractiveLesson(
        id = "lesson-13",
        lessonNumber = 13,
        titleDe = "LLMs als Gehirn, Workflows als Muskeln",
        titleEn = "LLMs as Brains, Workflows as Muscles",
        descriptionDe = "LLMs im Unternehmen, RAG, Agenten-Workflows, Automation.",
        descriptionEn = "Using LLMs in companies, RAG, agent workflows, automation.",
        durationMinutes = 25,
        objectivesDe = listOf(
            "LLM-Einsatzszenarien kennen",
            "RAG-Architektur verstehen",
            "KI-Agenten und Workflow-Automatisierung verstehen",
        ),
        objectivesEn = listOf(
            "Know LLM use cases",
            "Understand RAG architecture",
            "Understand AI agents and workflow automation",
        ),
        sections = listOf(
            LessonSection(
                titleDe = "1. LLMs im Unternehmen",
                titleEn = "1. LLMs in Companies",
                blocks = listOf(
                    ContentBlock.Text(
                        textDe = "LLM Einsatzszenarien: Kundenservice Chatbot, Wissensmanagement, Dokumentenanalyse, Code-Generierung, Content-Creation, Datenanalyse (NL to SQL).",
                        textEn = "LLM use cases: Customer service chatbot, knowledge management, document analysis, code generation, content creation, data analysis (NL to SQL).",
                    ),
                    ContentBlock.TrueFalse(
                        statementDe = "Ein LLM kann ohne weitere Architektur interne Dokumente beantworten.",
                        statementEn = "An LLM can answer internal document questions without additional architecture.",
                        isTrue = false,
                        explanationDe = "Falsch! LLMs kennen nur ihre Trainingsdaten. Interne Dokumente brauchen RAG.",
                        explanationEn = "False! LLMs only know training data. Internal documents need RAG.",
                    ),
                ),
            ),
            LessonSection(
                titleDe = "2. RAG und KI-Agenten",
                titleEn = "2. RAG and AI Agents",
                blocks = listOf(
                    ContentBlock.Text(
                        textDe = "RAG in 4 Schritten: 1. Indexing - Dokumente in Vektordatenbank. 2. Retrieval - Aehnliche Chunks finden. 3. Augmentation - Kontext hinzufuegen. 4. Generation - LLM antwortet. KI-Agent: LLM mit Werkzeugen (Tools), kann Aktionen ausfuehren.",
                        textEn = "RAG in 4 steps: 1. Indexing. 2. Retrieval. 3. Augmentation. 4. Generation. AI Agent: LLM with tools, can take actions.",
                    ),
                    ContentBlock.Quiz(
                        questionDe = "Was ist der groesste Vorteil von RAG gegenueber Fine-Tuning?",
                        questionEn = "What is the biggest advantage of RAG over fine-tuning?",
                        options = listOf(
                            QuizOption("Hoehere Genauigkeit", "Higher accuracy", isCorrect = false),
                            QuizOption("Aktualisierbar ohne Retraining", "Updatable without retraining", isCorrect = true),
                            QuizOption("Weniger Speicher", "Less storage", isCorrect = false),
                            QuizOption("Schneller", "Faster", isCorrect = false),
                        ),
                        explanationDe = "Richtig! Neue Dokumente einfach in Vektordatenbank ablegen.",
                        explanationEn = "Correct! Simply add new documents to vector DB.",
                    ),
                    ContentBlock.RiskThermometer(),
                ),
            ),
        ),
    )
}
