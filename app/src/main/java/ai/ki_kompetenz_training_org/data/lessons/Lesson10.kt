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
                                nameDe = "Gut", nameEn = "Good", emoji = "✅",
                                items = listOf(
                                    ClassificationItem("Du bist Datenschutzbeauftragter. Erstelle eine Checkliste.", "You are a DPO. Create a checklist."),
                                    ClassificationItem("Format: Tabelle. Spalten: Risiko, Prioritaet.", "Format: Table. Columns: Risk, Priority."),
                                ),
                            ),
                            ClassificationCategory(
                                nameDe = "Schlecht", nameEn = "Bad", emoji = "❌",
                                items = listOf(
                                    ClassificationItem("Schreib etwas ueber DSGVO.", "Write something about GDPR."),
                                    ClassificationItem("KI ist gut oder schlecht?", "Is AI good or bad?"),
                                ),
                            ),
                        ),
                    ),
                    ContentBlock.PromptExercise(
                        promptDe = "Schreibe einen Prompt, der eine KI bittet, einen professionellen Email-Betreff fuer eine Bewerbung zu generieren.",
                        promptEn = "Write a prompt that asks an AI to generate a professional email subject line for a job application.",
                        modelAnswerDe = "Du bist ein Experte fuer berufliche Kommunikation. Generiere drei professionelle Betreffzeilen fuer eine Bewerbung als [Position] bei [Unternehmen]. Die Betreffzeilen sollen praegnant, hoeflich und aufmerksamkeitsstark sein.",
                        modelAnswerEn = "You are an expert in professional communication. Generate three professional subject lines for a job application as [Position] at [Company]. The subject lines should be concise, polite, and attention-grabbing.",
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
                    ContentBlock.TrueFalse(
                        statementDe = "Few-Shot-Prompting bedeutet, dem Modell Beispiele im Prompt zu zeigen.",
                        statementEn = "Few-shot prompting means showing the model examples in the prompt.",
                        isTrue = true,
                        explanationDe = "Richtig — Beispiele zeigen dem Modell das gewuenschte Muster.",
                        explanationEn = "Correct — examples show the model the desired pattern.",
                    ),
                    ContentBlock.FillBlank(
                        sentenceDe = "Einer der staerksten Prompt-Tricks: dem KI-Assistenten eine ___ zuweisen (z. B. 'Du bist Jurist').",
                        sentenceEn = "One of the strongest prompt tricks: assign the AI assistant a ___ (e.g. 'You are a lawyer').",
                        blankKey = "L10-rolle",
                        choices = listOf("Rolle", "Farbe", "Postleitzahl", "Telefonnummer"),
                        correctIndex = 0,
                        explanationDe = "Rollen steuern Perspektive und Tonfall.",
                        explanationEn = "Roles steer perspective and tone.",
                    ),
                    ContentBlock.Quiz(
                        questionDe = "Welches Tool gehoert zum Einsatzgebiet Code?",
                        questionEn = "Which tool belongs to the code category?",
                        options = listOf(
                            QuizOption("GitHub Copilot", "GitHub Copilot", isCorrect = true), QuizOption("Midjourney", "Midjourney", isCorrect = false), QuizOption("Gamma", "Gamma", isCorrect = false), QuizOption("Elicit", "Elicit", isCorrect = false),
                        ),
                        explanationDe = "Richtig — Copilot (und Cursor) sind Code-Assistenten.",
                        explanationEn = "Correct — Copilot (and Cursor) are code assistants.",
                    ),
                ),
            ),
        ),
        cognitiveLevel = CognitiveLevel.APPLICATION,
    )
}
