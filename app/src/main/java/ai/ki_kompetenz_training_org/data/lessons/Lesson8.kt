package ai.ki_kompetenz_training_org.data.lessons

object Lesson8 {

    val lesson = InteractiveLesson(
        id = "lesson-8",
        lessonNumber = 8,
        titleDe = "KI-Ethik: Verantwortungsvolle KI",
        titleEn = "AI Ethics: Responsible AI Development",
        descriptionDe = "KI-Bias, Fairness, Transparenz und menschliche Aufsicht.",
        descriptionEn = "AI bias, fairness, transparency, and human oversight.",
        durationMinutes = 20,
        objectivesDe = listOf(
            "KI-Bias erkennen und verstehen",
            "Fairness-Metriken fuer KI-Systeme kennen",
            "Transparenz als Grundprinzip erklaeren",
            "Menschliche Aufsicht im EU AI Act verstehen",
        ),
        objectivesEn = listOf(
            "Recognize and understand AI bias",
            "Know fairness metrics for AI systems",
            "Explain transparency as core principle",
            "Understand human oversight in EU AI Act",
        ),
        sections = listOf(
            LessonSection(
                titleDe = "1. Was ist KI-Bias?",
                titleEn = "1. What is AI Bias?",
                blocks = listOf(
                    ContentBlock.Text(
                        textDe = "KI-Bias = Systematische Fehler in KI-Entscheidungen durch voreingenommene Daten. Daten-Bias: Trainingsdaten repraesentieren nicht alle Gruppen. Algorithmus-Bias: Modell bevorzugt Muster. Bestaetigungs-Bias: KI verstaerkt Vorurteile. Interaktions-Bias: Feedback macht das Modell einseitig.",
                        textEn = "AI Bias = Systematic errors in AI decisions due to biased data. Data Bias: Training data does not represent all groups. Algorithm Bias: Model prefers certain patterns. Confirmation Bias: AI reinforces prejudices. Interaction Bias: Feedback makes the model one-sided.",
                    ),
                    ContentBlock.Classification(
                        instructionDe = "Welche Bias-Arten stecken in diesen Szenarien?",
                        instructionEn = "Which bias types are hidden?",
                        categories = listOf(
                            ClassificationCategory(
                                nameDe = "Daten-Bias",
                                nameEn = "Data Bias",
                                emoji = "📊",
                                items = listOf(
                                    ClassificationItem("HR-KI mit 80% Maenner-Profilen", "HR AI with 80% male profiles"),
                                    ClassificationItem("Kredit-KI nur mit Daten einer Stadt", "Credit AI only with data from one city"),
                                    ClassificationItem("Medizin-KI hauptsaechlich mit Weissen", "Medical AI mainly with white data"),
                                ),
                            ),
                            ClassificationCategory(
                                nameDe = "Algorithmus-Bias",
                                nameEn = "Algorithm Bias",
                                emoji = "⚙️",
                                items = listOf(
                                    ClassificationItem("Sprachmodell unterschaetzt nicht-englische Accents", "Language model undervalues non-English accents"),
                                    ClassificationItem("Bild-KI erkennt dunkle Gesichter schlechter", "Image AI recognizes darker faces worse"),
                                ),
                            ),
                            ClassificationCategory(
                                nameDe = "Interaktions-Bias",
                                nameEn = "Interaction Bias",
                                emoji = "👥",
                                items = listOf(
                                    ClassificationItem("Empfehlungen zeigen nur Mainstream-Content", "Recommendations show only mainstream content"),
                                    ClassificationItem("Social Media verstaerkt extreme Meinungen", "Social media amplifies extreme opinions"),
                                ),
                            ),
                        ),
                    ),
                    ContentBlock.Text(
                        textDe = "Der Unterschied:\nBilderkennung = Was ist DAS? Ein Label pro Bild. Objekterkennung = Wo sind DIE? Viele Objekte mit Position. Segmentierung = Welcher Pixel gehoert wozu? Komplexitaet: Bild < Objekt < Segmentierung",
                        textEn = "The difference:\nImage classification = What is THIS? One label per image. Object detection = Where are THEY? Many objects with positions. Segmentation = Which pixel belongs to what? Complexity: Image < Object < Segmentation",
                    ),
                ),
            ),
            LessonSection(
                titleDe = "2. Fairness: Wie messen wir Gerechtigkeit?",
                titleEn = "2. Fairness: How do we measure fairness?",
                blocks = listOf(
                    ContentBlock.Text(
                        textDe = "Fairness-Metriken:\nDemographic Parity: Gleiche Positive Rate. Equal Opportunity: Gleiche True-Positive-Rate. Predictive Parity: Gleiche Fehlerquote. Individual Fairness: Aehnlich qualifizierte Personen erhalten aehnliche Ergebnisse.",
                        textEn = "Fairness metrics:\nDemographic Parity: Same positive rate. Equal Opportunity: Same true-positive rate. Predictive Parity: Same error rate. Individual Fairness: Similarly qualified people get similar results.",
                    ),
                    ContentBlock.TrueFalse(
                        statementDe = "Eine KI kann gleichzeitig fair nach allen Fairness-Metriken sein.",
                        statementEn = "An AI can be simultaneously fair per all fairness metrics.",
                        isTrue = false,
                        explanationDe = "Falsch! Das ist der Fairness-Paradoxon. Du musst die richtige Metrik fuer den Use-Case waehlen.",
                        explanationEn = "False! That is the Fairness Paradox.",
                    ),
                    ContentBlock.Quiz(
                        questionDe = "Eine Bewerbungs-KI hat True-Positive-Rate 90% fuer Maenner und 75% fuer Frauen. Welche Fairness-Metrik ist verletzt?",
                        questionEn = "A recruitment AI has true-positive rate 90% for men and 75% for women. Which metric is violated?",
                        options = listOf(
                            QuizOption("Demographic Parity", "Demographic Parity", isCorrect = false),
                            QuizOption("Equal Opportunity", "Equal Opportunity", isCorrect = true),
                            QuizOption("Predictive Parity", "Predictive Parity", isCorrect = false),
                            QuizOption("Individual Fairness", "Individual Fairness", isCorrect = false),
                        ),
                        explanationDe = "Richtig! Equal Opportunity ist verletzt: Qualifizierte Frauen werden seltener erkannt. Die Amazon HR-KI hatte genau dieses Problem!",
                        explanationEn = "Correct! Equal Opportunity is violated: Qualified women are less often recognized.",
                    ),
                ),
            ),
            LessonSection(
                titleDe = "3. Transparenz: Explainable AI",
                titleEn = "3. Transparency: Explainable AI",
                blocks = listOf(
                    ContentBlock.Text(
                        textDe = "Warum ist Transparenz wichtig?\nRecht auf Erklaerung: EU AI Act Art. 13. Verantwortung: Wer haftet bei Fehlern? Vertrauen: Nutzer muessen KI vertrauen koennen. Debugging: Entwickler muessen Fehler finden koennen.",
                        textEn = "Why is transparency important?\nRight to explanation: EU AI Act Art. 13. Accountability: Who is liable for errors? Trust: Users must be able to trust AI. Debugging: Developers must be able to find errors.",
                    ),
                    ContentBlock.KnowledgeCheck(
                        questionDe = "Warum ist es schwer zu erklaeren WARUM eine KI eine Entscheidung getroffen hat?",
                        questionEn = "Why is it hard to explain WHY an AI made a decision?",
                        answerDe = "Deep Learning Modelle sind Black Boxes mit Millionen von Gewichten. Aktuelle Forschung: SHAP, LIME, Attention-Maps.",
                        answerEn = "Deep Learning models are Black Boxes with millions of weights. Current research: SHAP, LIME, Attention Maps.",
                    ),
                    ContentBlock.TrueFalse(
                        statementDe = "Der EU AI Act verlangt, dass Nutzer die KI-Entscheidung verstehen koennen.",
                        statementEn = "The EU AI Act requires users to understand the AI decision.",
                        isTrue = true,
                        explanationDe = "Richtig! Art. 13 EU AI Act: Betroffene haben das Recht auf eine verstaendliche Erklaerung.",
                        explanationEn = "Correct! Art. 13 EU AI Act: Affected persons have the right to a comprehensible explanation.",
                    ),
                ),
            ),
            LessonSection(
                titleDe = "4. Menschliche Aufsicht",
                titleEn = "4. Human Oversight",
                blocks = listOf(
                    ContentBlock.Text(
                        textDe = "EU AI Act Art. 14: Menschliche Aufsicht fuer Hochrisiko-KI:\nMensch kann KI-Entscheidung jederzeit korrigieren. Mensch wird bei Risikosituationen informiert. Mensch kann den KI-Betrieb komplett stoppen. KI darf nicht autonom endgueltige Entscheidungen treffen. Beispiel: KI schlaegt Kandidaten vor, Mensch entscheidet.",
                        textEn = "EU AI Act Art. 14: Human oversight for high-risk AI:\nHuman can correct AI decision at any time. Human is informed in risk situations. Human can completely stop AI. AI must not autonomously make final decisions.",
                    ),
                    ContentBlock.Quiz(
                        questionDe = "Eine KI lehnt eine Kreditsanfrage automatisch ab. Ist das nach EU AI Act erlaubt?",
                        questionEn = "An AI automatically rejects a credit application. Is this allowed?",
                        options = listOf(
                            QuizOption("Ja, KI ist schneller", "Yes, AI is faster", isCorrect = false),
                            QuizOption("Nein, Hochrisiko-KI braucht menschliche Aufsicht", "No, high-risk AI needs human oversight", isCorrect = true),
                            QuizOption("Ja, wenn Genauigkeit >95%", "Yes, if accuracy >95%", isCorrect = false),
                            QuizOption("Nein, nur fuer Staat-KI", "No, only for government AI", isCorrect = false),
                        ),
                        explanationDe = "Richtig! Kredit-KI = Hochrisiko. Art. 14: KI kann Vorschlaege machen, aber ein Mensch muss die endgueltige Entscheidung treffen.",
                        explanationEn = "Correct! Credit AI = High risk. Art. 14: AI can SUGGEST but a human must make the final decision.",
                    ),
                    ContentBlock.RiskThermometer(),
                ),
            ),
            LessonSection(
                titleDe = "5. Wissens-Quiz",
                titleEn = "5. Knowledge Quiz",
                blocks = listOf(
                    ContentBlock.Quiz(
                        questionDe = "Was ist der Unterschied zwischen Explainable AI und Interpretierbarer AI?",
                        questionEn = "What is the difference between Explainable AI and Interpretable AI?",
                        options = listOf(
                            QuizOption("Kein Unterschied", "No difference", isCorrect = false),
                            QuizOption("Explainable = nachtraegliche Erklaerung; Interpretierbar = von Natur aus erklaerbar", "Explainable = post-hoc; Interpretable = by design", isCorrect = true),
                            QuizOption("Explainable ist besser", "Explainable is better", isCorrect = false),
                            QuizOption("Beides gibt es nicht", "Neither exists", isCorrect = false),
                        ),
                        explanationDe = "Richtig! Interpretierbare KI (Entscheidungsbaeume) sind von Natur aus erklaerbar. Explainable AI (Deep Learning) braucht SHAP, LIME.",
                        explanationEn = "Correct! Interpretable AI (decision trees) are explainable by nature. Explainable AI (Deep Learning) needs SHAP, LIME.",
                    ),
                    ContentBlock.Quiz(
                        questionDe = "Amazon musste eine HR-KI abschalten. Was war das Hauptproblem?",
                        questionEn = "Amazon had to shut down an HR AI. What was the main problem?",
                        options = listOf(
                            QuizOption("Die KI war zu langsam", "The AI was too slow", isCorrect = false),
                            QuizOption("Sie diskriminierte gegen Frauen", "It discriminated against women", isCorrect = true),
                            QuizOption("Die KI kostete zu viel", "The AI was too expensive", isCorrect = false),
                            QuizOption("Niemand nutzte sie", "Nobody used it", isCorrect = false),
                        ),
                        explanationDe = "Richtig! Die KI lernte aus historischen Daten mit ueberwiegend Maennern. Sie bewertete maennliche Codierer hoeher und penalisierte Frauen.",
                        explanationEn = "Correct! The AI learned from historical data predominantly from men. It rated male coders higher and penalized women.",
                    ),
                    ContentBlock.Quiz(
                        questionDe = "Welcher Schritt zur verantwortungsvollen KI ist am wichtigsten?",
                        questionEn = "Which step to responsible AI is most important?",
                        options = listOf(
                            QuizOption("Bessere Algorithmen", "Better algorithms", isCorrect = false),
                            QuizOption("Trainingsdaten auf Bias pruefen und korrigieren", "Check and correct training data for bias", isCorrect = true),
                            QuizOption("Mehr KI einsetzen", "Use more AI", isCorrect = false),
                            QuizOption("Fairness-Metriken berechnen", "Calculate fairness metrics", isCorrect = false),
                        ),
                        explanationDe = "Richtig! Datenqualitaet ist die Basis. Garbage in, garbage out!",
                        explanationEn = "Correct! Data quality is the foundation. Garbage in, garbage out!",
                    ),
                    ContentBlock.RiskThermometer(),
                ),
            ),
        ),
    )
}
