package ai.ki_kompetenz_training_org.data.lessons

object Lesson9 {
    val lesson = InteractiveLesson(
        id = "lesson-9",
        lessonNumber = 9,
        titleDe = "KI-Strategie fuer Unternehmen",
        titleEn = "AI Strategy for Companies",
        descriptionDe = "KI-Roadmap, Use-Case-Priorisierung, ROI-Berechnung und Change Management.",
        descriptionEn = "AI roadmap, use-case prioritization, ROI calculation, and change management.",
        durationMinutes = 25,
        objectivesDe = listOf(
            "KI-Use-Cases priorisieren koennen",
            "ROI-Berechnung fuer KI-Projekte verstehen",
            "KI-Strategie-Phasen kennen",
            "Change Management bei KI-Einfuehrung planen",
        ),
        objectivesEn = listOf(
            "Prioritize AI use cases",
            "Understand ROI calculation for AI projects",
            "Know AI strategy phases",
            "Plan change management for AI introduction",
        ),
        sections = listOf(
            LessonSection(
                titleDe = "1. Die 5 Phasen der KI-Strategie",
                titleEn = "1. The 5 Phases of AI Strategy",
                blocks = listOf(
                    ContentBlock.Text(
                        textDe = "Phase 1: Assessment - Wo steht das Unternehmen? Datenqualitaet, Prozesse, Kompetenzen analysieren. Phase 2: Vision - Ziele und KPIs definieren. Phase 3: Use-Case-Auswahl - Welche KI-Anwendungen bringen den groessten Wert? Phase 4: Roadmap - Schritt-fuer-Schritt-Plan mit Meilensteinen. Phase 5: Scale - Erfolgreiche PoCs ausrollen.",
                        textEn = "Phase 1: Assessment - Analyze data quality, processes, competencies. Phase 2: Vision - Define goals and KPIs. Phase 3: Use-Case Selection - Which AI applications bring the most value? Phase 4: Roadmap - Step-by-step plan with milestones. Phase 5: Scale - Roll out successful PoCs.",
                    ),
                    ContentBlock.Classification(
                        instructionDe = "Ordne die Aktivitaeten den Strategiephasen zu:",
                        instructionEn = "Assign the activities to strategy phases:",
                        categories = listOf(
                            ClassificationCategory(
                                nameDe = "Assessment", nameEn = "Assessment", emoji = "📋",
                                items = listOf(
                                    ClassificationItem("Datenqualitaets-Audit", "Data quality audit"),
                                    ClassificationItem("Mitarbeiter-Umfrage zu KI-Kompetenzen", "Employee survey on AI skills"),
                                    ClassificationItem("Wettbewerbsanalyse", "Competitive analysis"),
                                ),
                            ),
                            ClassificationCategory(
                                nameDe = "Vision und Roadmap", nameEn = "Vision and Roadmap", emoji = "🎯",
                                items = listOf(
                                    ClassificationItem("KPIs definieren", "Define KPIs"),
                                    ClassificationItem("3-Jahres-Plan erstellen", "Create 3-year plan"),
                                    ClassificationItem("Budget freigeben", "Approve budget"),
                                ),
                            ),
                            ClassificationCategory(
                                nameDe = "Scale", nameEn = "Scale", emoji = "📈",
                                items = listOf(
                                    ClassificationItem("Erfolgreichen PoC ausrollen", "Roll out successful PoC"),
                                    ClassificationItem("Trainings fuer alle Abteilungen", "Training for all departments"),
                                    ClassificationItem("Zentrale KI-Plattform aufbauen", "Build central AI platform"),
                                ),
                            ),
                        ),
                    ),
                    ContentBlock.Quiz(
                        questionDe = "Ein Use-Case hat hohen Kundennutzen aber extreme Komplexitaet. Was tun?",
                        questionEn = "A use case has high customer value but extreme complexity. What to do?",
                        options = listOf(
                            QuizOption("Direkt starten", "Start immediately", isCorrect = false),
                            QuizOption("Zuerst als PoC testen und Komplexitaet reduzieren", "Test as PoC first and reduce complexity", isCorrect = true),
                            QuizOption("Verwerfen", "Discard", isCorrect = false),
                            QuizOption("Externe Beratung beauftragen", "Hire external consultants", isCorrect = false),
                        ),
                        explanationDe = "Richtig! Ein PoC reduziert das Risiko.",
                        explanationEn = "Correct! A PoC reduces risk.",
                    ),
                ),
            ),
            LessonSection(
                titleDe = "2. Change Management",
                titleEn = "2. Change Management",
                blocks = listOf(
                    ContentBlock.Text(
                        textDe = "Top 5 Challenges: 1. Widerstand der Mitarbeiter. 2. Fehlende Datenkultur. 3. Unrealistische Erwartungen. 4. Fachkraeftemangel. 5. Ethik und Compliance. Loesung: Kommunikation frueh beginnen, Quick Wins zeigen, Schulungen anbieten.",
                        textEn = "Top 5 challenges: 1. Employee resistance. 2. Missing data culture. 3. Unrealistic expectations. 4. Talent shortage. 5. Ethics and compliance. Solution: Communicate early, show quick wins, offer training.",
                    ),
                    ContentBlock.TrueFalse(
                        statementDe = "Die beste KI-Strategie startet mit dem KI-Tool.",
                        statementEn = "The best AI strategy starts with the AI tool.",
                        isTrue = false,
                        explanationDe = "Falsch! Strategie-First: Business-Problem zuerst definieren.",
                        explanationEn = "False! Strategy-First: Define the business problem first.",
                    ),
                    ContentBlock.RiskThermometer(),
                ),
            ),
        ),
    )
}
