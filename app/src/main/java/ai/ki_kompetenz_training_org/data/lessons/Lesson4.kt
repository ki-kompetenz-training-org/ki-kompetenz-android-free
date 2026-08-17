package ai.ki_kompetenz_training_org.data.lessons

object Lesson4 {

    val lesson = InteractiveLesson(
        id = "lesson-4",
        lessonNumber = 4,
        titleDe = "Datenschutz und KI: Wer weiß was über dich?",
        titleEn = "Privacy and AI: Who knows what about you?",
        descriptionDe = "KI und Datenschutz, DSGVO-Grundlagen, Datenminimierung und die Rolle der Einwilligung.",
        descriptionEn = "AI and data protection, GDPR basics, data minimization, and the role of consent.",
        durationMinutes = 20,
        objectivesDe = listOf(
            "DSGVO-Grundrechte in Bezug auf KI erklären",
            "Datenminimierung und Zweckbindung unterscheiden",
            "Einwilligung vs. legitimes Interesse kennen",
            "KI-Datenschutzrisiken identifizieren",
        ),
        objectivesEn = listOf(
            "Explain GDPR fundamental rights regarding AI",
            "Distinguish data minimization and purpose limitation",
            "Know consent vs. legitimate interest",
            "Identify AI data protection risks",
        ),
        sections = listOf(
            LessonSection(
                titleDe = "1. Deine Daten — die neue Ölraffinerie",
                titleEn = "1. Your data — the new oil refinery",
                blocks = listOf(
                    ContentBlock.Callout(
                        type = CalloutType.DEFINITION,
                        textDe = "KI braucht Daten wie ein Motor Benzin braucht. Je mehr Daten, desto besser die KI — und desto mehr Fragen zum Datenschutz.",
                        textEn = "AI needs data like an engine needs fuel. More data = better AI — and more privacy questions.",
                    ),
                    ContentBlock.KnowledgeCheck(
                        questionDe = "Welche Daten sammeln die Apps auf deinem Handy?",
                        questionEn = "What data do the apps on your phone collect?",
                        answerDe = "Standort, Kontakte, Fotos, Mikrofon-Audio, Tastatur-Muster, Surfverhalten, Kaufhistorie, Gesundheitsdaten... Wahrscheinlich mehr als du denkst! Check mal: Settings > Privacy auf deinem Handy.",
                        answerEn = "Location, contacts, photos, mic audio, keyboard patterns, browsing behavior, purchase history, health data... Probably more than you think! Check: Settings > Privacy on your phone.",
                    ),
                    ContentBlock.TrueFalse(
                        statementDe = "Wenn eine App kostenlos ist, bist du nicht der Kunde — du bist das Produkt.",
                        statementEn = "If an app is free, you are not the customer — you are the product.",
                        isTrue = true,
                        explanationDe = "Korrekt! Kostenlose Apps finanzieren sich meist durch Datenverkauf oder personalisierte Werbung. Deine Daten sind das Geschäftsmodell.",
                        explanationEn = "Correct! Free apps usually monetize through data selling or personalized ads. Your data is the business model.",
                    ),
                ),
            ),

            LessonSection(
                titleDe = "2. DSGVO-Grundprinzipien",
                titleEn = "2. GDPR Core Principles",
                blocks = listOf(
                    ContentBlock.Text(
                        textDe = "Die 7 DSGVO-Grundprinzipien:\n\n" +
                            "1. Rechtmäßigkeit — Braucht eine Rechtsgrundlage\n" +
                            "2. Zweckbindung — Nur für den angegebenen Zweck\n" +
                            "3. Datenminimierung — Nur was nötig ist\n" +
                            "4. Richtigkeit — Muss korrekt sein\n" +
                            "5. Speicherbegrenzung — Nicht ewig aufbewahren\n" +
                            "6. Integrität & Vertraulichkeit — Schutz vor Verlust\n" +
                            "7. Rechenschaftspflicht — Nachweisbar machen",
                        textEn = "The 7 GDPR principles:\n\n" +
                            "1. Lawfulness — Needs a legal basis\n" +
                            "2. Purpose limitation — Only for stated purpose\n" +
                            "3. Data minimization — Only what is necessary\n" +
                            "4. Accuracy — Must be correct\n" +
                            "5. Storage limitation — Not forever\n" +
                            "6. Integrity & Confidentiality — Protection against loss\n" +
                            "7. Accountability — Demonstrate compliance",
                    ),
                    ContentBlock.Classification(
                        instructionDe = "Ordne die Szenarien dem verletzten DSGVO-Prinzip zu:",
                        instructionEn = "Assign each scenario to the violated GDPR principle:",
                        categories = listOf(
                            ClassificationCategory(
                                nameDe = "Zweckbindung",
                                nameEn = "Purpose Limitation",
                                emoji = "🎯",
                                items = listOf(
                                    ClassificationItem("Gesundheitsapp verkauft Daten an Werbenetzwerk", "Health app sells data to ad network"),
                                    ClassificationItem("Navigation-App trackt Shop-Besuche", "Navigation app tracks store visits"),
                                ),
                            ),
                            ClassificationCategory(
                                nameDe = "Datenminimierung",
                                nameEn = "Data Minimization",
                                emoji = "✂️",
                                items = listOf(
                                    ClassificationItem("Parksche App fragt nach Blutgruppe", "Parking app asks for blood type"),
                                    ClassificationItem("Taschenrechner will Zugriff auf Kontakte", "Calculator wants contacts access"),
                                ),
                            ),
                            ClassificationCategory(
                                nameDe = "Speicherbegrenzung",
                                nameEn = "Storage Limitation",
                                emoji = "⏰",
                                items = listOf(
                                    ClassificationItem("E-Commerce speichert 10 Jahre alte Bestelldaten", "E-commerce stores 10-year-old order data"),
                                    ClassificationItem("Lösch-Antrag wird ignoriert", "Deletion request is ignored"),
                                ),
                            ),
                        ),
                    ),
                ),
            ),

            LessonSection(
                titleDe = "3. KI-spezifische Datenschutzrisiken",
                titleEn = "3. AI-specific data protection risks",
                blocks = listOf(
                    ContentBlock.Text(
                        textDe = "KI bringt neue Datenschutzrisiken:\n\n" +
                            "Profilierung: KI erstellt Persönlichkeitsprofile aus Verhaltensdaten\n" +
                            "Inferenz: KI schließt von Daten auf sensible Eigenschaften\n" +
                            "Tracking: Facial Recognition, Browser-Fingerprinting, Device ID\n" +
                            "Scoring: Social Scoring, Kreditscoring, Risikobewertung\n" +
                            "Bias: Diskriminierung durch voreingenommene Trainingsdaten",
                        textEn = "AI brings new privacy risks:\n\n" +
                            "Profiling: AI creates personality profiles from behavioral data\n" +
                            "Inference: AI infers sensitive attributes from data\n" +
                            "Tracking: Facial Recognition, Browser Fingerprinting, Device ID\n" +
                            "Scoring: Social scoring, credit scoring, risk assessment\n" +
                            "Bias: Discrimination through biased training data",
                    ),
                    ContentBlock.FillBlank(
                        sentenceDe = "___ bedeutet, dass eine KI aus deinem Surfverhalten auf deine politische Meinung schließt.",
                        sentenceEn = "___ means an AI infers your political opinion from your browsing behavior.",
                        blankKey = "Inferenz",
                        choices = listOf("Profilierung", "Inferenz", "Tracking", "Bias"),
                        correctIndex = 1,
                        explanationDe = "Richtig! Inferenz = Rückschlüsse ziehen. Die KI hat deine politische Meinung nie direkt — sie schließt aus indirectly. Das ist besonders heimtückisch.",
                        explanationEn = "Correct! Inference = drawing conclusions. The AI never had your political opinion directly — it inferred it indirectly. This is particularly insidious.",
                    ),
                    ContentBlock.TrueFalse(
                        statementDe = "Eine KI kann deinen Gesundheitszustand aus deinem Surfverhalten vorhersagen.",
                        statementEn = "An AI can predict your health condition from your browsing behavior.",
                        isTrue = true,
                        explanationDe = "Richtig! Forscher haben gezeigt, dass ML-Modelle Depressionen, Diabetes und sogar Schwangerschaft aus Suchanfragen vorhersagen können. Ein Grund, warum DSGVO Art. 9 besondere Kategorien schützt!",
                        explanationEn = "Correct! Researchers showed ML models can predict depression, diabetes, and even pregnancy from search queries. One reason why GDPR Art. 9 protects special categories!",
                    ),
                ),
            ),

            LessonSection(
                titleDe = "4. Einwilligung vs. Legitimes Interesse",
                titleEn = "4. Consent vs. Legitimate Interest",
                blocks = listOf(
                    ContentBlock.Quiz(
                        questionDe = "Ein Online-Shop nutzt KI, um personalisierte Produktempfehlungen zu zeigen. Welche Rechtsgrundlage ist typisch?",
                        questionEn = "An online shop uses AI for personalized product recommendations. What is the typical legal basis?",
                        options = listOf(
                            QuizOption("Einwilligung", "Consent", isCorrect = false),
                            QuizOption("Vertragserfüllung", "Contract performance", isCorrect = false),
                            QuizOption("Legitimes Interesse", "Legitimate interest", isCorrect = true),
                            QuizOption("Rechtspflicht", "Legal obligation", isCorrect = false),
                        ),
                        explanationDe = "Richtig! Personalisierte Empfehlungen fallen meist unter legitimes Interesse (Art. 6 Abs. 1 lit. f DSGVO). Aber: Der Shop muss eine Interessenabwägung durchführen und Widerspruchsrecht einräumen!",
                        explanationEn = "Correct! Personalized recommendations usually fall under legitimate interest (Art. 6(1)(f) GDPR). But: The shop must do an interest balancing test and allow opt-out!",
                    ),
                    ContentBlock.Quiz(
                        questionDe = "Eine Gesundheits-KI analysiert medizinische Bilder. Welche Rechtsgrundlage?",
                        questionEn = "A health AI analyzes medical images. What legal basis?",
                        options = listOf(
                            QuizOption("Legitimes Interesse", "Legitimate interest", isCorrect = false),
                            QuizOption("Einwilligung (explizit, informiert)", "Explicit informed consent", isCorrect = true),
                            QuizOption("Vertragserfüllung", "Contract performance", isCorrect = false),
                            QuizOption("Öffentliches Interesse", "Public interest", isCorrect = false),
                        ),
                        explanationDe = "Richtig! Gesundheitsdaten = Art. 9 DSGVO (besondere Kategorie). Nur EINWILLIGUNG oder gesetzliche Pflicht sind möglich — legitimes Interesse reicht NICHT!",
                        explanationEn = "Correct! Health data = Art. 9 GDPR (special category). Only CONSENT or legal obligation are possible — legitimate interest is NOT enough!",
                    ),
                    ContentBlock.Callout(
                        type = CalloutType.WARNING,
                        textDe = "Achtung: Vorab-Einwilligung (Cookie-Banner) reicht NICHT für KI-Profiling!\nDSGVO Art. 7 verlangt: Freiwillig, spezifisch, informiert und unmissverständlich. Ein \"Alle akzeptieren\"-Button auf Cookie-Bannern erfüllt diese Kriterien NICHT.",
                        textEn = "Warning: Pre-ticked consent (cookie banners) is NOT enough for AI profiling!\nGDPR Art. 7 requires: Freely given, specific, informed, and unambiguous. An \"Accept all\" button on cookie banners does NOT meet these criteria.",
                    ),
                ),
            ),

            LessonSection(
                titleDe = "5. DSGVO Art. 17: Recht auf Vergessenwerden",
                titleEn = "5. GDPR Art. 17: Right to erasure",
                blocks = listOf(
                    ContentBlock.Text(
                        textDe = "Jeder EU-Bürger hat das Recht, dass seine Daten gelöscht werden:\n\n" +
                            "- Direkte Löschung bei dem Unternehmen\n" +
                            "- Löschung bei allen Empfängern\n" +
                            "- Löschung von Backup-Kopien\n" +
                            "- Keine unzumutbare Verzögerung (meist 1 Monat)\n\n" +
                            "KI-Kompetenz bietet diese Funktion über das Dashboard an.",
                        textEn = "Every EU citizen has the right to have their data deleted:\n\n" +
                            "- Direct deletion at the company\n" +
                            "- Deletion at all recipients\n" +
                            "- Deletion from backup copies\n" +
                            "- No undue delay (usually 1 month)\n\n" +
                            "KI-Kompetenz provides this feature via the dashboard.",
                    ),
                    ContentBlock.TrueFalse(
                        statementDe = "Ein Unternehmen darf die Löschung verweigern, weil die KI noch trainiert werden muss.",
                        statementEn = "A company can refuse deletion because the AI still needs to be trained.",
                        isTrue = false,
                        explanationDe = "Falsch! Die KI-Trainingspflicht ist kein Grund, Daten länger zu speichern. Das Unternehmen muss die Daten löschen und die KI ggf. neu trainieren — ohne deine Daten!",
                        explanationEn = "False! AI training need is not a reason to keep data longer. The company must delete the data and retrain the AI if needed — without your data!",
                    ),
                ),
            ),

            LessonSection(
                titleDe = "6. Wissens-Quiz",
                titleEn = "6. Knowledge Quiz",
                blocks = listOf(
                    ContentBlock.Quiz(
                        questionDe = "Welches DSGVO-Prinzip verletzt ein Social-Media-Ki, das sensible Eigenschaften aus Likes vorhersagt?",
                        questionEn = "Which GDPR principle does a social media AI violate by predicting sensitive attributes from likes?",
                        options = listOf(
                            QuizOption("Speicherbegrenzung", "Storage limitation", isCorrect = false),
                            QuizOption("Zweckbindung", "Purpose limitation", isCorrect = false),
                            QuizOption("Datenminimierung + Zweckbindung", "Data minimization + Purpose limitation", isCorrect = true),
                            QuizOption("Richtigkeit", "Accuracy", isCorrect = false),
                        ),
                        explanationDe = "Richtig! Die KI sammelt MORE data als nötig (Datenminimierung) und nutzt es für einen ZWECK, dem du nicht zugestimmt hast (Zweckbindung). Cambridge Analytica war genau so ein Fall!",
                        explanationEn = "Correct! The AI collects MORE data than needed (minimization) and uses it for a PURPOSE you didn't consent to (purpose limitation). Cambridge Analytica was exactly such a case!",
                    ),
                    ContentBlock.Quiz(
                        questionDe = "Ein Fitness-Tracker teilt deine Gesundheitsdaten mit deinem Arbeitgeber. Darf er das?",
                        questionEn = "A fitness tracker shares your health data with your employer. Can it?",
                        options = listOf(
                            QuizOption("Ja, wenn es im Vertrag steht", "Yes, if it is in the contract", isCorrect = false),
                            QuizOption("Nein, Gesundheitsdaten bedürfen expliziter Einwilligung", "No, health data requires explicit consent", isCorrect = true),
                            QuizOption("Ja, legitimes Interesse des Arbeitgebers", "Yes, employer's legitimate interest", isCorrect = false),
                            QuizOption("Ja, wenn der Arbeitgeber die KI bezahlt", "Yes, if the employer pays for the AI", isCorrect = false),
                        ),
                        explanationDe = "Richtig! Art. 9 DSGVO: Gesundheitsdaten sind besonders geschützt. Selbst dein Arbeitgeber darf sie nicht ohne explizite Einwilligung verarbeiten!",
                        explanationEn = "Correct! Art. 9 GDPR: Health data is specially protected. Even your employer cannot process it without explicit consent!",
                    ),
                    ContentBlock.RiskThermometer(),
                ),
            ),
        ),
    )
}
