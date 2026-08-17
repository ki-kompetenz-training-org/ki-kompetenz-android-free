package ai.ki_kompetenz_training_org.data.lessons

object Lesson12 {
    val lesson = InteractiveLesson(
        id = "lesson-12",
        lessonNumber = 12,
        titleDe = "Change Management und AI Culture",
        titleEn = "Change Management and AI Culture",
        descriptionDe = "KI-Kultur aufbauen, Widerstand ueberwinden, Weiterbildung.",
        descriptionEn = "Building AI culture, overcoming resistance, continuing education.",
        durationMinutes = 25,
        objectivesDe = listOf(
            "KI-Kultur im Unternehmen aufbauen",
            "Widerstand gegen KI adressieren",
            "Weiterbildungsstrategien entwickeln",
        ),
        objectivesEn = listOf(
            "Build AI culture in the company",
            "Address resistance to AI",
            "Develop continuing education strategies",
        ),
        sections = listOf(
            LessonSection(
                titleDe = "1. Was ist KI-Kultur?",
                titleEn = "1. What is AI Culture?",
                blocks = listOf(
                    ContentBlock.Text(
                        textDe = "KI-Kultur: Datenbasierte Entscheidungen sind Standard. Experimentierfreude wird gefoerdert. Fehler sind Lernchancen. Transparenz wird gelebt. Ethik wird bei jeder Entscheidung beruecksichtigt. Weiterbildung ist Pflicht.",
                        textEn = "AI culture: Data-driven decisions are standard. Experimentation is encouraged. Errors are learning opportunities. Transparency is practiced. Ethics is always considered. Continuing education is mandatory.",
                    ),
                    ContentBlock.Classification(
                        instructionDe = "Welche Reaktionen auf KI-Einfuehrung?",
                        instructionEn = "Which reactions to AI adoption?",
                        categories = listOf(
                            ClassificationCategory(
                                nameDe = "Positiv", nameEn = "Positive", emoji = "1F4AA",
                                items = listOf(
                                    ClassificationItem("Freude ueber Zeitersparnis", "Excitement about time savings"),
                                    ClassificationItem("Neugier auf neue Tools", "Curiosity about new tools"),
                                ),
                            ),
                            ClassificationCategory(
                                nameDe = "Negativ", nameEn = "Negative", emoji = "1F614",
                                items = listOf(
                                    ClassificationItem("Angst vor Jobverlust", "Fear of job loss"),
                                    ClassificationItem("Skepsis gegenueber KI-Qualitaet", "Skepticism about AI quality"),
                                    ClassificationItem("Unwille zum Umlernen", "Unwillingness to relearn"),
                                ),
                            ),
                        ),
                    ),
                ),
            ),
            LessonSection(
                titleDe = "2. Widerstand ueberwinden",
                titleEn = "2. Overcoming Resistance",
                blocks = listOf(
                    ContentBlock.Text(
                        textDe = "5 Strategien: 1. Frueh einbinden. 2. Transparenz. 3. Schulung. 4. Quick Wins sichtbar machen. 5. Vorbilder: Fuehrungskraefte nutzen KI aktiv.",
                        textEn = "5 strategies: 1. Involve early. 2. Transparency. 3. Training. 4. Show quick wins. 5. Role models.",
                    ),
                    ContentBlock.Quiz(
                        questionDe = "Welcher Ansatz ist am besten, um Skepsis gegenueber KI abzubauen?",
                        questionEn = "Which approach best reduces skepticism about AI?",
                        options = listOf(
                            QuizOption("KI einfuehren und dann schulen", "Introduce AI then train", isCorrect = false),
                            QuizOption("Schulen, Quick Wins zeigen, dann skaliert einfuehren", "Train, show quick wins, then scale", isCorrect = true),
                            QuizOption("Nur Fuehrungskraefte schulen", "Only train executives", isCorrect = false),
                        ),
                        explanationDe = "Richtig! Schulung vor der Einfuehrung baut Vertrauen auf.",
                        explanationEn = "Correct! Training before implementation builds trust.",
                    ),
                    ContentBlock.TrueFalse(
                        statementDe = "KI-Weiterbildung ist nur fuer IT-Mitarbeiter relevant.",
                        statementEn = "AI continuing education is only relevant for IT employees.",
                        isTrue = false,
                        explanationDe = "Falsch! EU AI Act fordert, dass ALLE Nutzer von Hochrisiko-KI qualifiziert sind (Art. 9(2)).",
                        explanationEn = "False! EU AI Act requires ALL users of high-risk AI to be qualified (Art. 9(2)).",
                    ),
                    ContentBlock.RiskThermometer(),
                ),
            ),
        ),
    )
}
