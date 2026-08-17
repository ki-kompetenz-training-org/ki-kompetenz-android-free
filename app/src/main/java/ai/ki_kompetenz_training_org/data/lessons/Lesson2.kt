package ai.ki_kompetenz_training_org.data.lessons

object Lesson2 {

    val lesson = InteractiveLesson(
        id = "lesson-2",
        lessonNumber = 2,
        titleDe = "Die drei Arten von KI: Schmal, Allgemein, Super",
        titleEn = "The three types of AI: Narrow, General, Super",
        descriptionDe = "KI-Arten unterscheiden, reale Beispiele einordnen und verstehen, warum General AI noch Science Fiction ist.",
        descriptionEn = "Distinguish AI types, classify real examples, and understand why General AI is still science fiction.",
        durationMinutes = 15,
        objectivesDe = listOf(
            "Narrow AI, General AI und Superintelligenz definieren",
            "Reale KI-Systeme korrekt als Narrow AI einordnen",
            "Die Turing-Test Idee erklaren",
            "Warum General AI noch nicht existiert",
        ),
        objectivesEn = listOf(
            "Define Narrow AI, General AI, and Superintelligence",
            "Correctly classify real AI systems as Narrow AI",
            "Explain the Turing Test idea",
            "Why General AI does not exist yet",
        ),
        sections = listOf(
            // ── Section 1 ──
            LessonSection(
                titleDe = "1. Die KI-Leiter",
                titleEn = "1. The AI Ladder",
                blocks = listOf(
                    ContentBlock.Text(
                        textDe = "Stell dir KI wie eine Leiter vor:\n\n" +
                            "Schritt 1: Schmale KI = EINE Aufgabe, meisterhaft gelost\n" +
                            "Schritt 2: Allgemeine KI = JEDE intellektuelle Aufgabe\n" +
                            "Schritt 3: Superintelligenz = ALLES besser als jeder Mensch",
                        textEn = "Think of AI like a ladder:\n\n" +
                            "Step 1: Narrow AI = ONE task, mastered perfectly\n" +
                            "Step 2: General AI = ANY intellectual task\n" +
                            "Step 3: Superintelligence = EVERYTHING better than any human",
                    ),
                    ContentBlock.KnowledgeCheck(
                        questionDe = "Auf welcher Stufe stehen wir heute?",
                        questionEn = "Which level are we at today?",
                        answerDe = "Schritt 1 — Schmale KI. Jede heute existierende KI ist fur EINE Aufgabe trainiert. Selbst GPT-4 kann keinen Kaffee kochen!",
                        answerEn = "Step 1 — Narrow AI. Every existing AI today is trained for ONE task. Even GPT-4 cannot make coffee!",
                    ),
                ),
            ),

            // ── Section 2: Sorting Game ──
            LessonSection(
                titleDe = "2. Sortier-Spiel: Wo gehort diese KI hin?",
                titleEn = "2. Sorting Game: Where does this AI belong?",
                blocks = listOf(
                    ContentBlock.Text(
                        textDe = "Jede KI unten ist schmale KI — aber in welcher Kategorie?",
                        textEn = "Every AI below is narrow AI — but in which category?",
                    ),
                    ContentBlock.Classification(
                        instructionDe = "Prufe die Zuordnung:",
                        instructionEn = "Review the assignments:",
                        categories = listOf(
                            ClassificationCategory(
                                nameDe = "Wahrnehmung",
                                nameEn = "Perception",
                                emoji = "👁️",
                                items = listOf(
                                    ClassificationItem("Gesichtserkennung (Face ID)", "Face recognition (Face ID)"),
                                    ClassificationItem("Stimmerkennung (Siri, Alexa)", "Voice recognition (Siri, Alexa)"),
                                    ClassificationItem("Bildgenerierung (Midjourney)", "Image generation (Midjourney)"),
                                ),
                            ),
                            ClassificationCategory(
                                nameDe = "Entscheidung",
                                nameEn = "Decision",
                                emoji = "🧠",
                                items = listOf(
                                    ClassificationItem("Spam-Filter (Gmail)", "Spam filter (Gmail)"),
                                    ClassificationItem("Empfehlungsalgorithmen (Netflix)", "Recommendation algorithms (Netflix)"),
                                    ClassificationItem("Schachcomputer (Stockfish)", "Chess computer (Stockfish)"),
                                ),
                            ),
                            ClassificationCategory(
                                nameDe = "Sprache",
                                nameEn = "Language",
                                emoji = "💬",
                                items = listOf(
                                    ClassificationItem("Ubersetzung (DeepL)", "Translation (DeepL)"),
                                    ClassificationItem("Textzusammenfassung (ChatGPT)", "Text summarization (ChatGPT)"),
                                    ClassificationItem("Autokorrektur (Handy)", "Autocorrect (phone)"),
                                ),
                            ),
                            ClassificationCategory(
                                nameDe = "Bewegung",
                                nameEn = "Movement",
                                emoji = "🚗",
                                items = listOf(
                                    ClassificationItem("Autonomes Fahren (Tesla)", "Self-driving (Tesla)"),
                                    ClassificationItem("Robotergreifer (Industrie)", "Robot gripper (factory)"),
                                    ClassificationItem("Drohnen-Navigation", "Drone navigation"),
                                ),
                            ),
                        ),
                    ),
                    ContentBlock.Callout(
                        type = CalloutType.TIP,
                        textDe = "Merkregel: Schmale KI ist nicht schwach — sie ist HYPER-spezialisiert. AlphaZero kann jeden Schachweltmeister schlagen, aber keinen Tisch decken.",
                        textEn = "Remember: Narrow AI is not weak — it is HYPER-specialized. AlphaZero can beat any chess champion, but cannot set a table.",
                    ),
                ),
            ),

            // ── Section 3: Mythbusters ──
            LessonSection(
                titleDe = "3. KI-Mythbusters: Wahr oder Falsch?",
                titleEn = "3. AI Mythbusters: True or False?",
                blocks = listOf(
                    ContentBlock.TrueFalse(
                        statementDe = "ChatGPT ist eine Allgemeine KI, weil sie alles verstehen kann.",
                        statementEn = "ChatGPT is a General AI because it can understand everything.",
                        isTrue = false,
                        explanationDe = "Myth busted! ChatGPT ist schmale KI — trainiert fur Textgenerierung. Sie kann kein Auto fahren und keinen Kaffee kochen.",
                        explanationEn = "Myth busted! ChatGPT is narrow AI — trained for text generation. It cannot drive a car or make coffee.",
                    ),
                    ContentBlock.TrueFalse(
                        statementDe = "Schmale KI wird niemals gefahrlich sein.",
                        statementEn = "Narrow AI will never be dangerous.",
                        isTrue = false,
                        explanationDe = "Falsch! Schmale KI kann sehr gefahrlich sein — wenn sie fehlerhaft arbeitet: Autonome Autos, HR-Software mit Bias, Gesichtsrekognition mit Rassismus.",
                        explanationEn = "False! Narrow AI can be very dangerous — when it fails: Self-driving cars, HR software with bias, facial recognition with racism.",
                    ),
                    ContentBlock.TrueFalse(
                        statementDe = "Superintelligenz existiert bereits im Geheimen.",
                        statementEn = "Superintelligence already exists in secret.",
                        isTrue = false,
                        explanationDe = "Verschworungstheorie! Es gibt KEINE Superintelligenz — die besten KI-Modelle (GPT-4, Claude, Gemini) sind alle schmale KI.",
                        explanationEn = "Conspiracy theory! There is NO superintelligence — the best AI models (GPT-4, Claude, Gemini) are all narrow AI.",
                    ),
                ),
            ),

            // ── Section 4: Fill Blanks ──
            LessonSection(
                titleDe = "4. Luckentext: Die wichtigsten Begriffe",
                titleEn = "4. Fill in the blanks: Key concepts",
                blocks = listOf(
                    ContentBlock.FillBlank(
                        sentenceDe = "Der ___ testet, ob eine Maschine menschliche Intelligenz vortauschen kann.",
                        sentenceEn = "The ___ tests whether a machine can fake human intelligence.",
                        blankKey = "Turing-Test",
                        choices = listOf("Bechdel-Test", "Turing-Test", "Stresstest", "Blind-Test"),
                        correctIndex = 1,
                        explanationDe = "Richtig! Alan Turing schlug 1950 den Imitation Game Test vor.",
                        explanationEn = "Correct! Alan Turing proposed the Imitation Game in 1950.",
                    ),
                    ContentBlock.FillBlank(
                        sentenceDe = "___ bedeutet, dass eine KI fur genau EINE Aufgabe trainiert wurde.",
                        sentenceEn = "___ means an AI was trained for exactly ONE task.",
                        blankKey = "Schmale KI",
                        choices = listOf("Schmale KI", "Superintelligenz", "Neural Network", "Deep Learning"),
                        correctIndex = 0,
                        explanationDe = "Schmale KI = Narrow AI. Spezialisierung ist ihre Starke.",
                        explanationEn = "Narrow AI. Specialization is its strength.",
                    ),
                    ContentBlock.FillBlank(
                        sentenceDe = "___ KI ware in der Lage, jeden Job auf der Welt zu machen.",
                        sentenceEn = "___ AI would be capable of doing every job in the world.",
                        blankKey = "Allgemeine",
                        choices = listOf("Schmale", "Allgemeine", "Quanten", "Bio"),
                        correctIndex = 1,
                        explanationDe = "Allgemeine KI (AGI) existiert noch nicht!",
                        explanationEn = "General AI (AGI) does not exist yet!",
                    ),
                ),
            ),

            // ── Section 5: Why no General AI ──
            LessonSection(
                titleDe = "5. Warum gibt es noch keine General AI?",
                titleEn = "5. Why does General AI not exist yet?",
                blocks = listOf(
                    ContentBlock.Callout(
                        type = CalloutType.EXAMPLE,
                        textDe = "Das Gehirn-Problem:\nDas menschliche Gehirn hat 86 Milliarden Neuronen und 100 Billionen Verbindungen. Es verbraucht nur 20 Watt. Das grosste KI-Modell verbraucht Megawatt-Stunden zum Trainieren — und kann trotzdem kein Fahrrad fahren!",
                        textEn = "The Brain Problem:\nThe human brain has 86 billion neurons and 100 trillion connections. It uses only 20 watts. The largest AI model consumes megawatt-hours to train — and still cannot ride a bicycle!",
                    ),
                    ContentBlock.Text(
                        textDe = "Die drei grossten Hurden:\n1. Generalisierung — KI kann nicht trainiertes Wissen auf neue Bereiche ubertragen\n2. Weltverstandnis — KI versteht keine Konzepte, sie erkennt nur Muster\n3. Effizienz — Das Gehirn ist millionenfach effizienter",
                        textEn = "The three biggest hurdles:\n1. Generalization — AI cannot transfer knowledge to new domains\n2. World understanding — AI does not understand concepts, only patterns\n3. Efficiency — The brain is millions of times more efficient",
                    ),
                    ContentBlock.KnowledgeCheck(
                        questionDe = "Bonus: Ware General AI automatisch hohes Risiko nach EU AI Act?",
                        questionEn = "Bonus: Would General AI automatically be high risk?",
                        answerDe = "Gute Frage! Der EU AI Act ist fur den heutigen Stand geschrieben. General AI wuerde wahrscheinlich NEUE Regeln brauchen!",
                        answerEn = "Great question! The EU AI Act is written for today. General AI would probably need NEW rules!",
                    ),
                ),
            ),

            // ── Section 6: Quiz ──
            LessonSection(
                titleDe = "6. Wissens-Quiz",
                titleEn = "6. Knowledge Quiz",
                blocks = listOf(
                    ContentBlock.Quiz(
                        questionDe = "AlphaGo kann den Weltmeister im Go schlagen, aber keinen Burger braten. Warum?",
                        questionEn = "AlphaGo can beat the world champion in Go but cannot grill a burger. Why?",
                        options = listOf(
                            QuizOption("Es hat keinen Korper", "It has no body", isCorrect = true),
                            QuizOption("Go ist einfacher als Braten", "Go is easier than grilling", isCorrect = false),
                            QuizOption("AlphaGo existiert nicht", "AlphaGo does not exist", isCorrect = false),
                            QuizOption("Es braucht mehr Strom", "It needs more electricity", isCorrect = false),
                        ),
                        explanationDe = "AlphaGo ist schmale KI — trainiert fur Go. Ohne Korper kann es keine Handgriffe erlernen.",
                        explanationEn = "AlphaGo is narrow AI — trained for Go. Without a body it cannot learn manual tasks.",
                    ),
                    ContentBlock.Quiz(
                        questionDe = "Was passiert, wenn man eine Schach-KI mit Daten zum Kochen trainiert?",
                        questionEn = "What happens if you train a chess AI with cooking data?",
                        options = listOf(
                            QuizOption("Sie kann dann beides", "It can then do both", isCorrect = false),
                            QuizOption("Sie verliert die Schach-Fahigkeit", "It loses chess ability", isCorrect = true),
                            QuizOption("Sie wird General AI", "It becomes General AI", isCorrect = false),
                            QuizOption("Sie bleibt ein Schach-Programm", "It stays a chess program", isCorrect = false),
                        ),
                        explanationDe = "Wenn du die Trainingsdaten aenderst, verlernt die KI die alte Aufgabe (catastrophic forgetting).",
                        explanationEn = "If you change the training data, the AI forgets the old task (catastrophic forgetting).",
                    ),
                    ContentBlock.Quiz(
                        questionDe = "Welcher Film zeigt eine superintelligente KI realistisch?",
                        questionEn = "Which movie shows a superintelligent AI realistically?",
                        options = listOf(
                            QuizOption("Ex Machina", "Ex Machina", isCorrect = false),
                            QuizOption("Keiner — Super-KI existiert nicht", "None — super AI does not exist", isCorrect = true),
                            QuizOption("2001: A Space Odyssey", "2001: A Space Odyssey", isCorrect = false),
                            QuizOption("The Matrix", "The Matrix", isCorrect = false),
                        ),
                        explanationDe = "Korrekt! Superintelligenz ist rein fiktiv.",
                        explanationEn = "Correct! Superintelligence is purely fictional.",
                    ),
                    ContentBlock.Quiz(
                        questionDe = "Der EU AI Act regelt hauptsachlich welche Art von KI?",
                        questionEn = "The EU AI Act primarily regulates which type of AI?",
                        options = listOf(
                            QuizOption("Superintelligenz", "Superintelligence", isCorrect = false),
                            QuizOption("General AI", "General AI", isCorrect = false),
                            QuizOption("Schmale KI in bestimmten Bereichen", "Narrow AI in specific domains", isCorrect = true),
                            QuizOption("Alle KI-Arten gleich", "All AI types equally", isCorrect = false),
                        ),
                        explanationDe = "Genau! Der EU AI Act reguliert schmale KI in Risikobereichen.",
                        explanationEn = "Exactly! The EU AI Act regulates narrow AI in risk areas.",
                    ),
                    ContentBlock.RiskThermometer(),
                ),
            ),
        ),
    )
}
