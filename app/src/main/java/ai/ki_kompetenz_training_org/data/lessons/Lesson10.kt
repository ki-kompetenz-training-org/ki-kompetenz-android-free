package ai.ki_kompetenz_training_org.data.lessons

object Lesson10 {
    val lesson = InteractiveLesson(
        id = "lesson-10",
        lessonNumber = 10,
        titleDe = "Prompt Engineering und Advanced AI Tools",
        titleEn = "Prompt Engineering and Advanced AI Tools",
        descriptionDe = "Effektive Prompts, KI-Tools im Arbeitsalltag, Automatisierung.",
        descriptionEn = "Effective prompts, AI tools in daily work, automation.",
        durationMinutes = 30,
        objectivesDe = listOf(
            "Prompt-Engineering-Techniken anwenden",
            "KI-Tools in den Arbeitsalltag integrieren",
            "Automatisierungs-Potenziale erkennen",
        ),
        objectivesEn = listOf(
            "Apply prompt engineering techniques",
            "Integrate AI tools into daily work",
            "Recognize automation potential",
        ),
        sections = listOf(
            LessonSection(
                titleDe = "1. Prompt Engineering Grundlagen",
                titleEn = "1. Prompt Engineering Basics",
                blocks = listOf(
                    ContentBlock.Text(
                        textDe = "5 Techniken fuer bessere Prompts: 1. Rolle zuweisen. 2. Kontext geben. 3. Aufgabe spezifizieren. 4. Format vorgeben. 5. Beispiele zeigen (Few-Shot).",
                        textEn = "5 techniques for better prompts: 1. Assign role. 2. Give context. 3. Specify task. 4. Define format. 5. Show examples (Few-Shot).",
                    ),
                    ContentBlock.Classification(
                        instructionDe = "Ordne die Prompts:",
                        instructionEn = "Assign the prompts:",
                        categories = listOf(
                            ClassificationCategory(
                                nameDe = "Gut", nameEn = "Good", emoji = "2705",
                                items = listOf(
                                    ClassificationItem("Du bist Datenschutzbeauftragter. Erstelle eine Checkliste.", "You are a DPO. Create a checklist."),
                                    ClassificationItem("Format: Tabelle. Spalten: Risiko, Prioritaet.", "Format: Table. Columns: Risk, Priority."),
                                ),
                            ),
                            ClassificationCategory(
                                nameDe = "Schlecht", nameEn = "Bad", emoji = "274C",
                                items = listOf(
                                    ClassificationItem("Schreib etwas ueber DSGVO.", "Write something about GDPR."),
                                    ClassificationItem("KI ist gut oder schlecht?", "Is AI good or bad?"),
                                ),
                            ),
                        ),
                    ),
                ),
            ),
            LessonSection(
                titleDe = "2. KI-Tools und DSGVO",
                titleEn = "2. AI Tools and GDPR",
                blocks = listOf(
                    ContentBlock.Text(
                        textDe = "10 KI-Tools nach Einsatzgebiet: Text: ChatGPT, Claude, Gemini. Bilder: Midjourney, DALL-E. Code: GitHub Copilot, Cursor. Daten: Julius AI, Tableau AI. Praesentationen: Gamma. Research: Perplexity, Elicit.",
                        textEn = "10 AI tools by use case: Text: ChatGPT, Claude, Gemini. Images: Midjourney, DALL-E. Code: GitHub Copilot, Cursor. Data: Julius AI. Presentations: Gamma. Research: Perplexity, Elicit.",
                    ),
                    ContentBlock.Quiz(
                        questionDe = "Ein Mitarbeiter nutzt ChatGPT fuer Kunden-E-Mails mit personenbezogenen Daten. Problem?",
                        questionEn = "An employee uses ChatGPT for customer emails with personal data. Problem?",
                        options = listOf(
                            QuizOption("Kein Problem", "No problem", isCorrect = false),
                            QuizOption("DSGVO-Verstoss: personenbezogene Daten duerfen nicht an unbekannte Server", "GDPR violation: personal data must not be sent to unknown servers", isCorrect = true),
                            QuizOption("Nur internes Problem", "Only internal problem", isCorrect = false),
                            QuizOption("Erlaubt mit Zustimmung", "Allowed with consent", isCorrect = false),
                        ),
                        explanationDe = "Richtig! Art. 5 DSGVO: Datenverarbeitung muss auf rechtmassiger Grundlage erfolgen. Loesung: Anonymisierung oder Enterprise-Version.",
                        explanationEn = "Correct! Art. 5 GDPR: Processing must have a legal basis. Solution: Anonymize or use Enterprise version.",
                    ),
                    ContentBlock.RiskThermometer(),
                ),
            ),
        ),
    )
}
