package ai.ki_kompetenz_training_org.data.lessons

object Lesson11 {
    val lesson = InteractiveLesson(
        id = "lesson-11",
        lessonNumber = 11,
        titleDe = "Audit und Compliance Deep Dive",
        titleEn = "Audit and Compliance Deep Dive",
        descriptionDe = "KI-Audit, Compliance-Checkliste, Dokumentationspflichten, Meldewege.",
        descriptionEn = "AI audit, compliance checklist, documentation obligations, reporting channels.",
        durationMinutes = 25,
        objectivesDe = listOf(
            "KI-Audit-Prozess kennen",
            "EU AI Act Dokumentationspflichten verstehen",
            "Conformity Assessment durchfuehren koennen",
            "Meldepflichten bei Vorfellen kennen",
        ),
        objectivesEn = listOf(
            "Know AI audit process",
            "Understand EU AI Act documentation obligations",
            "Conduct conformity assessment",
            "Know reporting obligations for incidents",
        ),
        sections = listOf(
            LessonSection(
                titleDe = "1. Der KI-Audit-Prozess",
                titleEn = "1. The AI Audit Process",
                blocks = listOf(
                    ContentBlock.Text(
                        textDe = "KI-Audit in 6 Schritten: 1. Scope: Welche KI-Systeme werden auditiert? 2. Risk Classification: Welches Risikolevel? 3. Data Audit: Trainingsdaten pruefen. 4. Model Audit: Genauigkeit, Fairness, Robustheit testen. 5. Process Audit: Dokumentation, Aufsicht pruefen. 6. Report: Befunde, Empfehlungen, Fristen.",
                        textEn = "AI audit in 6 steps: 1. Scope. 2. Risk Classification. 3. Data Audit. 4. Model Audit. 5. Process Audit. 6. Report.",
                    ),
                    ContentBlock.TrueFalse(
                        statementDe = "Nur Hochrisiko-KI muss laut EU AI Act auditiert werden.",
                        statementEn = "Only high-risk AI must be audited under EU AI Act.",
                        isTrue = false,
                        explanationDe = "Falsch! ALLE KI-Systeme muessen konform sein. Hochrisiko-KI braucht zusaetzlich Conformity Assessment.",
                        explanationEn = "False! ALL AI systems must be compliant. High-risk AI additionally needs conformity assessment.",
                    ),
                ),
            ),
            LessonSection(
                titleDe = "2. Dokumentationspflichten",
                titleEn = "2. Documentation Obligations",
                blocks = listOf(
                    ContentBlock.Text(
                        textDe = "EU AI Act Art. 11: Hochrisiko-KI braucht: Technische Dokumentation, Logging-Mechanismen, Gebrauchsanweisung, Cybersecurity-Massnahmen, Qualitaetsmanagement. Logs mindestens 6 Monate aufbewahren (Art. 12).",
                        textEn = "EU AI Act Art. 11: High-risk AI needs: Technical documentation, logging mechanisms, user manual, cybersecurity measures, quality management. Logs at least 6 months (Art. 12).",
                    ),
                    ContentBlock.Quiz(
                        questionDe = "Wie lange muessen Logs von Hochrisiko-KI aufbewahrt werden?",
                        questionEn = "How long must high-risk AI logs be retained?",
                        options = listOf(
                            QuizOption("3 Monate", "3 months", isCorrect = false),
                            QuizOption("6 Monate", "6 months", isCorrect = true),
                            QuizOption("Keine Vorgabe", "No specification", isCorrect = false),
                            QuizOption("Unbegrenzt", "Indefinitely", isCorrect = false),
                        ),
                        explanationDe = "Richtig! Art. 12: Mindestens 6 Monate.",
                        explanationEn = "Correct! Art. 12: At least 6 months.",
                    ),
                    ContentBlock.Callout(
                        type = CalloutType.WARNING,
                        textDe = "Bussgelder EU AI Act: Bis zu 35 Millionen Euro oder 7% des weltweiten Jahresumsatzes (Art. 71). Hoeher als bei DSGVO!",
                        textEn = "Fines EU AI Act: Up to 35 million EUR or 7% of global annual turnover (Art. 71). Higher than GDPR!",
                    ),
                    ContentBlock.RiskThermometer(),
                    ContentBlock.FillBlank(
                        sentenceDe = "Nach Art. 12 AI Act muessen Logs von Hochrisiko-Systemen mindestens ___ Monate aufbewahrt werden.",
                        sentenceEn = "Under Art. 12 AI Act, logs of high-risk systems must be kept for at least ___ months.",
                        blankKey = "L11-logs",
                        choices = listOf("3", "6", "12", "24"),
                        correctIndex = 1,
                        explanationDe = "Richtig: mindestens 6 Monate.",
                        explanationEn = "Correct: at least 6 months.",
                    ),
                    ContentBlock.TrueFalse(
                        statementDe = "Art. 11 verlangt fuer Hochrisiko-KI technische Dokumentation und Logging-Mechanismen.",
                        statementEn = "Art. 11 requires technical documentation and logging mechanisms for high-risk AI.",
                        isTrue = true,
                        explanationDe = "Richtig — Kernpflichten fuer Anbieter.",
                        explanationEn = "Correct — core provider obligations.",
                    ),
                    ContentBlock.Quiz(
                        questionDe = "Wie hoch sind die Hoechstbussgelder nach EU AI Act (Art. 71)?",
                        questionEn = "How high are the maximum fines under the EU AI Act (Art. 71)?",
                        options = listOf(
                            QuizOption("35 Mio. EUR oder 7% des weltweiten Umsatzes", "EUR 35m or 7% of global turnover", isCorrect = true),
                            QuizOption("10 Mio. EUR", "EUR 10m", isCorrect = false),
                            QuizOption("2% des Umsatzes wie bei der DSGVO", "2% of turnover like GDPR", isCorrect = false),
                            QuizOption("100.000 EUR", "EUR 100,000", isCorrect = false),
                        ),
                        explanationDe = "Richtig — deutlich haerter als die DSGVO.",
                        explanationEn = "Correct — significantly harsher than GDPR.",
                    ),
                ),
            ),
        ),
        cognitiveLevel = CognitiveLevel.APPLICATION,
    )
}
