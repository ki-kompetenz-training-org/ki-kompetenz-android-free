package ai.ki_kompetenz_training_org.data.lessons

object Lesson15 {
    val lesson = InteractiveLesson(
        id = "lesson-15",
        lessonNumber = 15,
        titleDe = "Haftung und Compliance bei KI",
        titleEn = "Liability and Compliance with AI",
        descriptionDe = "Wer haftet wenn KI fehlschlaegt? Pflichten fuer Anbieter, Betreiber und Nutzer.",
        descriptionEn = "Who is liable when AI fails? Obligations for providers, deployers and users.",
        durationMinutes = 25,
        objectivesDe = listOf(
            "Haftungsverteilung bei KI-Fehlern verstehen",
            "Pflichten von Anbietern und Betreibern kennen",
            "Compliance-Massnahmen im Arbeitsalltag anwenden",
        ),
        objectivesEn = listOf(
            "Understand liability distribution for AI errors",
            "Know obligations of providers and deployers",
            "Apply compliance measures in daily work",
        ),
        sections = listOf(
            LessonSection(
                titleDe = "1. Wer haftet, wenn KI fehlschlaegt?",
                titleEn = "1. Who is liable when AI fails?",
                blocks = listOf(
                    ContentBlock.Text(
                        textDe = "Der EU AI Act (Art. 25) verteilt Verantwortung: Der Anbieter (Entwickler) haftet fuer Mängel des Systems, der Betreiber (Deployer) fuer sachgerechten Einsatz, Input und Kontrolle. Die neue Produkthaftungsrichtlinie (2024) erfasst KI-Software ausdruecklich: Auch immaterielle Schaeden wie Datenverlust werden ersatzfaehig. Vermutung der Fehlerhaftigkeit trifft den Anbieter, wenn das System offensichtlich fehlerhaft war.",
                        textEn = "The EU AI Act (Art. 25) distributes responsibility: the provider (developer) is liable for defects of the system, the deployer for appropriate use, input and oversight. The new Product Liability Directive (2024) explicitly covers AI software: immaterial damages like data loss become compensable. Presumption of defectiveness falls on the provider if the system was obviously faulty.",
                    ),
                    ContentBlock.Quiz(
                        questionDe = "Ein Unternehmen setzt ein zugekauftes KI-System ein und laesst Ergebnisse unkontrolliert in Kundenentscheidungen einfliessen. Wer haftet zunaechst fuer den Schaden?",
                        questionEn = "A company deploys a purchased AI system and lets results flow into customer decisions uncontrolled. Who is initially liable for the damage?",
                        options = listOf(
                            QuizOption("Ausschliesslich der Anbieter", "Solely the provider", isCorrect = false),
                            QuizOption("Der Betreiber, wegen fehlender menschlicher Aufsicht", "The deployer, for lacking human oversight", isCorrect = true),
                            QuizOption("Niemand — KI-Fehler sind Zufall", "Nobody — AI errors are chance", isCorrect = false),
                            QuizOption("Der Endkunde", "The end customer", isCorrect = false),
                        ),
                        explanationDe = "Richtig! Der Betreiber ist zu angemessener Aufsicht verpflichtet (Art. 26 AI Act); unkontrollierte Uebernahme macht ihn haftbar.",
                        explanationEn = "Correct! The deployer must ensure appropriate oversight (Art. 26 AI Act); uncontrolled adoption makes them liable.",
                    ),
                ),
            ),
            LessonSection(
                titleDe = "2. Compliance im Arbeitsalltag",
                titleEn = "2. Compliance in daily work",
                blocks = listOf(
                    ContentBlock.Text(
                        textDe = "Compliance-Mindeststandard: Risikoklasse klären, Zweck dokumentieren, menschliche Aufsicht benennen, KI-Nutzung intern regeln (Freigaben, Verbote), Logs fuehren, Schulungen nachweisen. Fuer Hochrisiko-Systeme kommen Bewertungspflichten, Datenqualitaets- und Dokumentationspflichten hinzu.",
                        textEn = "Compliance baseline: clarify risk class, document purpose, name a human supervisor, govern internal AI use (approvals, prohibitions), keep logs, evidence trainings. High-risk systems add assessment, data quality and documentation duties.",
                    ),
                    ContentBlock.Classification(
                        instructionDe = "Ordne die Pflicht der richtigen Rolle zu:",
                        instructionEn = "Assign the duty to the right role:",
                        categories = listOf(
                            ClassificationCategory(
                                nameDe = "Anbieter", nameEn = "Provider", emoji = "🏭",
                                items = listOf(
                                    ClassificationItem("Risikobewertung des Systems", "Risk assessment of the system"),
                                    ClassificationItem("Technische Dokumentation", "Technical documentation"),
                                    ClassificationItem("CE-Kennzeichnung", "CE marking"),
                                ),
                            ),
                            ClassificationCategory(
                                nameDe = "Betreiber", nameEn = "Deployer", emoji = "🏢",
                                items = listOf(
                                    ClassificationItem("Menschliche Aufsicht sicherstellen", "Ensure human oversight"),
                                    ClassificationItem("Daten inputseitig pruefen", "Check input data"),
                                    ClassificationItem("Nutzer schulen", "Train users"),
                                ),
                            ),
                        ),
                    ),
                    ContentBlock.Quiz(
                        questionDe = "Was gehoert NICHT zum Compliance-Mindeststandard fuer den KI-Einsatz?",
                        questionEn = "What does NOT belong to the compliance baseline for AI use?",
                        options = listOf(
                            QuizOption("Zweck der Nutzung dokumentieren", "Document the purpose of use", isCorrect = false),
                            QuizOption("Menschliche Aufsicht benennen", "Name a human supervisor", isCorrect = false),
                            QuizOption("Allen Mitarbeitenden freie Hand fuer alle Tools", "Free rein for all employees on all tools", isCorrect = true),
                            QuizOption("Interne Regelung mit Freigaben", "Internal policy with approvals", isCorrect = false),
                        ),
                        explanationDe = "Richtig! Gerade uneingeschraenkter Toolzugriff ohne Regeln ist der klassische Compliance-Fehler.",
                        explanationEn = "Correct! Unrestricted tool access without rules is the classic compliance failure.",
                    ),
                    ContentBlock.RiskThermometer(),
                    ContentBlock.TrueFalse(
                        statementDe = "Die neue Produkthaftungsrichtlinie (2024) erfasst KI-Software ausdruecklich.",
                        statementEn = "The new Product Liability Directive (2024) explicitly covers AI software.",
                        isTrue = true,
                        explanationDe = "Richtig — auch immaterielle Schaeden werden ersatzfaehig.",
                        explanationEn = "Correct — immaterial damages also become compensable.",
                    ),
                    ContentBlock.Quiz(
                        questionDe = "Wer haftet fuer fehlerhafte Eingabedaten beim Einsatz eines KI-Systems?",
                        questionEn = "Who is liable for faulty input data when deploying an AI system?",
                        options = listOf(
                            QuizOption("Der Betreiber (Deployer)", "The deployer", isCorrect = true), QuizOption("Der Anbieter", "The provider", isCorrect = false), QuizOption("Der Endkunde", "The end customer", isCorrect = false), QuizOption("Niemand", "Nobody", isCorrect = false),
                        ),
                        explanationDe = "Richtig — inputseitige Pruefung ist Betreiberpflicht (Art. 26).",
                        explanationEn = "Correct — checking input is a deployer duty (Art. 26).",
                    ),
                ),
            ),
        ),
        cognitiveLevel = CognitiveLevel.APPLICATION,
    )
}
