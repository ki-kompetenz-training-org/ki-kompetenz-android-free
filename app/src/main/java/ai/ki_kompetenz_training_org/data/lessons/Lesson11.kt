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
                ),
            ),
        ),
    )
}
