package ai.ki_kompetenz_training_org.data.lessons

object Lesson14 {
    val lesson = InteractiveLesson(
        id = "lesson-14",
        lessonNumber = 14,
        titleDe = "Green, Efficient and Effective AI",
        titleEn = "Green, Efficient and Effective AI",
        descriptionDe = "Nachhaltige KI, Energieeffizienz, wirtschaftliche KI-Nutzung.",
        descriptionEn = "Sustainable AI, energy efficiency, economical AI use.",
        durationMinutes = 25,
        objectivesDe = listOf(
            "KI-Energieverbrauch verstehen",
            "Massnahmen fuer energieeffiziente KI kennen",
            "Green AI Principles anwenden",
            "KI-Kosten optimieren",
        ),
        objectivesEn = listOf(
            "Understand AI energy consumption",
            "Know measures for energy-efficient AI",
            "Apply Green AI Principles",
            "Optimize AI costs",
        ),
        sections = listOf(
            LessonSection(
                titleDe = "1. KI und Energieverbrauch",
                titleEn = "1. AI and Energy Consumption",
                blocks = listOf(
                    ContentBlock.Text(
                        textDe = "GPT-4 Training: ca. 50 GWh. Eine ChatGPT-Anfrage: 3x mehr als Google-Suche. KI-Rechenzentren: 1-2% des weltweiten Stroms. Prognose 2030: 4%.",
                        textEn = "GPT-4 training: approx. 50 GWh. One ChatGPT query: 3x more than Google search. AI data centers: 1-2% of global electricity. Forecast 2030: 4%.",
                    ),
                    ContentBlock.Quiz(
                        questionDe = "Wie viel Strom verbraucht eine ChatGPT-Anfrage gegenueber Google-Suche?",
                        questionEn = "How much power does a ChatGPT query use vs Google search?",
                        options = listOf(
                            QuizOption("Etwa gleich viel", "About the same", isCorrect = false),
                            QuizOption("Ca. 3x mehr", "About 3x more", isCorrect = true),
                            QuizOption("10x weniger", "10x less", isCorrect = false),
                            QuizOption("100x mehr", "100x more", isCorrect = false),
                        ),
                        explanationDe = "Richtig! Ca. 0.003 kWh vs. 0.001 kWh.",
                        explanationEn = "Correct! About 0.003 kWh vs. 0.001 kWh.",
                    ),
                ),
            ),
            LessonSection(
                titleDe = "2. Green AI und KI-Kosten",
                titleEn = "2. Green AI and AI Costs",
                blocks = listOf(
                    ContentBlock.Text(
                        textDe = "Green AI: Kleinere Modelle, Fine-Tuning statt Training, Oekostrom-Rechenzentren, Caching, effiziente Architekturen (MoE). KI-Kosten: API-Calls, Training, Infrastruktur, Personal. Optimierung: Pay-per-Use, Open-Source (Llama, Mistral), Auto-Scaling.",
                        textEn = "Green AI: Smaller models, fine-tuning instead of training, renewable energy, caching, MoE architectures. AI costs: API calls, training, infrastructure, personnel. Optimization: Pay-per-use, open-source, auto-scaling.",
                    ),
                    ContentBlock.Classification(
                        instructionDe = "Ordne die Massnahmen:",
                        instructionEn = "Assign the measures:",
                        categories = listOf(
                            ClassificationCategory(
                                nameDe = "Energie sparen", nameEn = "Save Energy", emoji = "🔋",
                                items = listOf(
                                    ClassificationItem("Kleineres Modell nutzen", "Use smaller model"),
                                    ClassificationItem("Caching implementieren", "Implement caching"),
                                    ClassificationItem("Inferenz optimieren", "Optimize inference"),
                                ),
                            ),
                            ClassificationCategory(
                                nameDe = "Gruene Energie", nameEn = "Green Energy", emoji = "🌿",
                                items = listOf(
                                    ClassificationItem("Oekostrom-Rechenzentrum", "Renewable energy data center"),
                                    ClassificationItem("Standort: Kuehle Regionen", "Location: cool regions"),
                                    ClassificationItem("Carbon Footprint messen", "Measure carbon footprint"),
                                ),
                            ),
                        ),
                    ),
                    ContentBlock.Quiz(
                        questionDe = "Ein Unternehmen verarbeitet taeglich 10000 vertrauliche Dokumente mit KI. Welche Option ist DSGVO-konform?",
                        questionEn = "A company processes 10000 confidential documents daily with AI. Which option is GDPR-compliant?",
                        options = listOf(
                            QuizOption("ChatGPT Enterprise API", "ChatGPT Enterprise API", isCorrect = false),
                            QuizOption("Self-Hosted Open-Source-Modell (Llama 3)", "Self-hosted open-source model (Llama 3)", isCorrect = true),
                            QuizOption("Claude API", "Claude API", isCorrect = false),
                            QuizOption("Google Gemini API", "Google Gemini API", isCorrect = false),
                        ),
                        explanationDe = "Richtig! Self-Hosting: Keine Daten verlassen das Unternehmen, bei grossem Volumen kosteneffizient.",
                        explanationEn = "Correct! Self-hosting: No data leaves the company, cost-effective at high volume.",
                    ),
                    ContentBlock.RiskThermometer(),
                ),
            ),
        ),
    )
}
