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
                    ContentBlock.Quiz(
                        questionDe = "Was ist Schritt 1 einer RAG-Pipeline?",
                        questionEn = "What is step 1 of a RAG pipeline?",
                        options = listOf(
                            QuizOption("Indexing: Dokumente in Vektordatenbank", "Indexing: documents into a vector database", isCorrect = true), QuizOption("Generation: LLM antwortet", "Generation: LLM answers", isCorrect = false), QuizOption("Retrieval: Chunks finden", "Retrieval: find chunks", isCorrect = false), QuizOption("Augmentation: Kontext anfuegen", "Augmentation: add context", isCorrect = false),
                        ),
                        explanationDe = "Richtig — ohne Indexierung kein Retrieval.",
                        explanationEn = "Correct — no retrieval without indexing.",
                    ),
                    ContentBlock.TrueFalse(
                        statementDe = "Ein KI-Agent kann mit Tools Aktionen ausfuehren, nicht nur Text erzeugen.",
                        statementEn = "An AI agent can perform actions with tools, not just generate text.",
                        isTrue = true,
                        explanationDe = "Richtig — Agent = LLM plus Werkzeuge.",
                        explanationEn = "Correct — agent = LLM plus tools.",
                    ),
                    ContentBlock.FillBlank(
                        sentenceDe = "Beim NL-to-SQL-Anwendungsfall wird natuerliche Sprache in ___ uebersetzt.",
                        sentenceEn = "In the NL-to-SQL use case, natural language is translated into ___.",
                        blankKey = "L13-nl2sql",
                        choices = listOf("SQL-Abfragen", "Excel-Formeln", "PDF-Dateien", "Screenshots"),
                        correctIndex = 0,
                        explanationDe = "Richtig — so analysieren Nicht-Techniker Datenbanken.",
                        explanationEn = "Correct — this lets non-technicians query databases.",
                    ),
                ),
            ),
        ),
        cognitiveLevel = CognitiveLevel.APPLICATION,
    )
}
