package ai.ki_kompetenz_training_org.data.lessons

/**
 * Lesson 1: Was ist Künstliche Intelligenz?
 * Interactive lesson with classification, knowledge checks, true/false,
 * fill-in-the-blank and quiz blocks.
 */
object Lesson1 {

    val lesson = InteractiveLesson(
        id = "lesson-1",
        lessonNumber = 1,
        titleDe = "Was ist Künstliche Intelligenz?",
        titleEn = "What is Artificial Intelligence?",
        descriptionDe = "Grundlagen: KI definieren, von klassischer Software unterscheiden, erste Beispiele einordnen.",
        descriptionEn = "Basics: Define AI, distinguish from classical software, categorize first examples.",
        durationMinutes = 15,
        objectivesDe = listOf(
            "KI von klassischer Software unterscheiden",
            "Die drei KI-Arten (Narrow, General, Super) benennen",
            "KI-Anwendungen im Alltag erkennen",
            "Maschinelles Lernen als KI-Teilgebiet erklären",
        ),
        objectivesEn = listOf(
            "Distinguish AI from classical software",
            "Name the three types of AI (Narrow, General, Super)",
            "Recognize AI applications in daily life",
            "Explain machine learning as a subfield of AI",
        ),
        sections = listOf(
            // ═══════════════════════════════════════════════════════════════
            // Section 1: Definition
            // ═══════════════════════════════════════════════════════════════
            LessonSection(
                titleDe = "1. Was macht KI besonderes?",
                titleEn = "1. What makes AI special?",
                blocks = listOf(
                    ContentBlock.Callout(
                        type = CalloutType.DEFINITION,
                        textDe = "Künstliche Intelligenz (KI) ist die Fähigkeit von Systemen, Aufgaben zu lösen, die normalerweise menschliche Intelligenz erfordern — wie Lernen, Verstehen und Entscheiden.",
                        textEn = "Artificial Intelligence (AI) is the ability of systems to solve tasks that normally require human intelligence — such as learning, understanding, and deciding.",
                    ),
                    ContentBlock.Text(
                        textDe = "Der entscheidende Unterschied: Klassische Software folgt **festen Regeln**, die ein Programmierer geschrieben hat. KI-Systeme **lernen aus Daten** und verbessern sich selbstständig.",
                        textEn = "The key difference: Classical software follows **fixed rules** written by a programmer. AI systems **learn from data** and improve on their own.",
                    ),
                    ContentBlock.KnowledgeCheck(
                        questionDe = "💡 Denk mal nach: Welches System nutzt KI?",
                        questionEn = "💡 Think about it: Which system uses AI?",
                        answerDe = "Beispiel: Netflix-Empfehlungen. Netflix analysiert, was du gesehen hast, vergleicht es mit Millionen anderer Nutzer und schlägt passende Serien vor — ohne dass jemand eine Regel dafür geschrieben hat.",
                        answerEn = "Example: Netflix recommendations. Netflix analyzes what you watched, compares it with millions of other users, and suggests matching shows — without anyone writing a rule for it.",
                    ),
                ),
            ),

            // ═══════════════════════════════════════════════════════════════
            // Section 2: Interaktiv — Classification
            // ═══════════════════════════════════════════════════════════════
            LessonSection(
                titleDe = "2. KI oder klassische Software?",
                titleEn = "2. AI or classical software?",
                blocks = listOf(
                    ContentBlock.Text(
                        textDe = "Ordne die folgenden Beispiele in die richtige Kategorie ein. KI-Systeme lernen aus Daten, klassische Software folgt festen Regeln.",
                        textEn = "Categorize the following examples. AI systems learn from data, classical software follows fixed rules.",
                    ),
                    ContentBlock.Classification(
                        instructionDe = "Tippe auf das Beispiel, um es der richtigen Kategorie zuzuordnen:",
                        instructionEn = "Tap on the example to assign it to the correct category:",
                        categories = listOf(
                            ClassificationCategory(
                                nameDe = "🤖 KI-System",
                                nameEn = "🤖 AI System",
                                emoji = "🤖",
                                items = listOf(
                                    ClassificationItem("Gesichtserkennung am Handy", "Face recognition on phone"),
                                    ClassificationItem("Spam-Filter in E-Mail", "Email spam filter"),
                                    ClassificationItem("Sprachassistent (Alexa/Siri)", "Voice assistant (Alexa/Siri)"),
                                    ClassificationItem("Autonomes Fahren", "Self-driving car"),
                                    ClassificationItem("Google Translate", "Google Translate"),
                                ),
                            ),
                            ClassificationCategory(
                                nameDe = "⚙️ Klassische Software",
                                nameEn = "⚙️ Classical Software",
                                emoji = "⚙️",
                                items = listOf(
                                    ClassificationItem("Taschenrechner", "Calculator"),
                                    ClassificationItem("Textverarbeitung (Speichern)", "Word processor (saving)"),
                                    ClassificationItem("Excel-Formel SUMME(A1:A10)", "Excel formula SUM(A1:A10)"),
                                    ClassificationItem("Passwort-Hashing (SHA-256)", "Password hashing (SHA-256)"),
                                    ClassificationItem("DNS-Auflösung", "DNS resolution"),
                                ),
                            ),
                        ),
                    ),
                    ContentBlock.Callout(
                        type = CalloutType.TIP,
                        textDe = "💡 Merkregel: Wenn das System aus Mustern in Daten lernt → KI. Wenn ein Mensch die Regeln vorher festgelegt hat → klassische Software.",
                        textEn = "💡 Rule of thumb: If the system learns from patterns in data → AI. If a human defined the rules in advance → classical software.",
                    ),
                ),
            ),

            // ═══════════════════════════════════════════════════════════════
            // Section 3: Die drei KI-Arten
            // ═══════════════════════════════════════════════════════════════
            LessonSection(
                titleDe = "3. Die drei Arten von KI",
                titleEn = "3. The three types of AI",
                blocks = listOf(
                    ContentBlock.TrueFalse(
                        statementDe = "\"Es gibt bereits eine KI, die genauso intelligent ist wie ein Mensch.\"",
                        statementEn = "\"There is already an AI that is just as intelligent as a human.\"",
                        isTrue = false,
                        explanationDe = "Falsch! General AI (AGI) existiert noch nicht. Heute nutzen wir ausschließlich Narrow AI — Systeme, die für EINE bestimmte Aufgabe trainiert sind.",
                        explanationEn = "False! General AI (AGI) does not exist yet. Today we only use Narrow AI — systems trained for ONE specific task.",
                    ),
                    ContentBlock.Text(
                        textDe = "Die drei KI-Arten:\n\n**1. Schmale KI (Narrow AI)** — Für eine Aufgabe trainiert. Beispiel: Schachcomputer, Bilderkennung. ✅ Heute verfügbar.\n\n**2. Allgemeine KI (General AI / AGI)** — Kann jede intellektuelle Aufgabe eines Menschen. 🚧 Noch nicht erreicht.\n\n**3. Superintelligenz** — Übertrifft menschliche Intelligenz in allem. ⏳ Science Fiction (bisher).",
                        textEn = "The three types of AI:\n\n**1. Narrow AI** — Trained for one task. Example: Chess computer, image recognition. ✅ Available today.\n\n**2. General AI (AGI)** — Can do any intellectual task a human can. 🚧 Not yet achieved.\n\n**3. Superintelligence** — Surpasses human intelligence in everything. ⏳ Science fiction (so far).",
                    ),
                    ContentBlock.FillBlank(
                        sentenceDe = "Dein Smartphone nutzt ___ KI, die speziell für Gesichtserkennung trainiert wurde.",
                        sentenceEn = "Your smartphone uses ___ AI, specifically trained for face recognition.",
                        blankKey = "Narrow AI / Schmale KI",
                        choices = listOf("Superintelligenz", "Allgemeine", "Schmale", "Quanten-"),
                        correctIndex = 2,
                        explanationDe = "Richtig! Schmale KI (Narrow AI) ist für genau eine Aufgabe optimiert — wie Gesichtserkennung am Handy.",
                        explanationEn = "Correct! Narrow AI is optimized for exactly one task — like face recognition on your phone.",
                    ),
                ),
            ),

            // ═══════════════════════════════════════════════════════════════
            // Section 4: Maschinelles Lernen
            // ═══════════════════════════════════════════════════════════════
            LessonSection(
                titleDe = "4. Wie lernt KI? Maschinelles Lernen",
                titleEn = "4. How does AI learn? Machine learning",
                blocks = listOf(
                    ContentBlock.Text(
                        textDe = "Maschinelles Lernen (ML) ist der wichtigste Teilbereich der KI. Statt Regeln zu programmieren, füttert man die KI mit **Beispieldaten**. Die KI findet selbstständig Muster und Regeln.",
                        textEn = "Machine Learning (ML) is the most important subfield of AI. Instead of programming rules, you feed the AI with **example data**. The AI finds patterns and rules on its own.",
                    ),
                    ContentBlock.Callout(
                        type = CalloutType.EXAMPLE,
                        textDe = "📋 Beispiel Spam-Filter:\n1. Du markierst 1000 E-Mails als \"Spam\" und 5000 als \"Kein Spam\"\n2. Der ML-Algorithmus analysiert die Muster (Wörter, Absender, Uhrzeit)\n3. Bei neuen E-Mails wendet der Algorithmus die gelernten Muster an\n→ Kein Programmierer musste eine Regel schreiben!",
                        textEn = "📋 Example spam filter:\n1. You mark 1000 emails as \"spam\" and 5000 as \"not spam\"\n2. The ML algorithm analyzes patterns (words, sender, time)\n3. For new emails, the algorithm applies the learned patterns\n→ No programmer had to write a rule!",
                    ),
                    ContentBlock.KnowledgeCheck(
                        questionDe = "💡 Warum reicht maschinelles Lernen allein nicht aus, um eine KI zu bauen?",
                        questionEn = "💡 Why isn't machine learning alone enough to build an AI?",
                        answerDe = "ML ist nur ein Teilbereich. Eine vollständige KI braucht auch: Daten (Trainingsmaterial), Rechenleistung, Algorithmen, und oft auch menschliches Feedback. ML ist der \"Motor\", aber die KI braucht auch \"Treibstoff\" (Daten) und eine \"Karosserie\" (Anwendung).",
                        answerEn = "ML is just one subfield. A complete AI also needs: data (training material), computing power, algorithms, and often human feedback. ML is the \"engine\", but the AI also needs \"fuel\" (data) and a \"chassis\" (application).",
                    ),
                    ContentBlock.TrueFalse(
                        statementDe = "\"Maschinelles Lernen ist dasselbe wie Künstliche Intelligenz.\"",
                        statementEn = "\"Machine learning is the same as Artificial Intelligence.\"",
                        isTrue = false,
                        explanationDe = "Falsch! ML ist ein TEILBEREICH der KI. KI umfasst auch andere Bereiche wie Expertensysteme, Robotics, und Natural Language Processing.",
                        explanationEn = "False! ML is a SUBFIELD of AI. AI also includes other areas like expert systems, robotics, and natural language processing.",
                    ),
                ),
            ),

            // ═══════════════════════════════════════════════════════════════
            // Section 5: EU AI Act Bezug
            // ═══════════════════════════════════════════════════════════════
            LessonSection(
                titleDe = "5. Warum ist das für den EU AI Act wichtig?",
                titleEn = "5. Why does this matter for the EU AI Act?",
                blocks = listOf(
                    ContentBlock.Callout(
                        type = CalloutType.LAW,
                        textDe = "⚖️ EU AI Act Art. 4 definiert: „Künstliche Intelligenz\" ist ein System, das mit einer gewissen Anpassungsfähigkeit ausgestattet ist und für autonomes Entscheiden durch Algorithmen und Daten trainiert wird.",
                        textEn = "⚖️ EU AI Act Art. 4 defines: \"Artificial intelligence\" is a system designed to operate with a certain level of autonomy and trained through algorithms and data for autonomous decision-making.",
                    ),
                    ContentBlock.Text(
                        textDe = "Der EU AI Act unterscheidet KI-Systeme nach ihrem **Risiko**:\n\n• **Unannehmbares Risiko** → verboten (z.B. Social Scoring)\n• **Hohes Risiko** → strenge Pflichten (z.B. HR-Software, Medizin-KI)\n• **Geringes Risiko** → Transparenz (z.B. Chatbots)\n• **Minimales Risiko** → keine Pflichten",
                        textEn = "The EU AI Act classifies AI systems by their **risk**:\n\n• **Unacceptable risk** → banned (e.g. social scoring)\n• **High risk** → strict obligations (e.g. HR software, medical AI)\n• **Low risk** → transparency (e.g. chatbots)\n• **Minimal risk** → no obligations",
                    ),
                    ContentBlock.Quiz(
                        questionDe = "Ein System analysiert Lebensläufe und schlägt Bewerber:innen vor oder ab. Welches Risiko-Stufe nach EU AI Act?",
                        questionEn = "A system analyzes resumes and suggests or rejects applicants. Which risk level under the EU AI Act?",
                        options = listOf(
                            QuizOption("Minimales Risiko", "Minimal risk", isCorrect = false),
                            QuizOption("Geringes Risiko", "Low risk", isCorrect = false),
                            QuizOption("Hohes Risiko", "High risk", isCorrect = true),
                            QuizOption("Unannehmbares Risiko", "Unacceptable risk", isCorrect = false),
                        ),
                        explanationDe = "Richtig! HR/KI-Bewerbungssoftware gilt als **Hohes Risiko** (Anhang III EU AI Act). Sie beeinflusst Menschen in entscheidenden Lebensbereichen (Arbeit) und unterliegt strengen Pflichten: Transparenz, menschliche Aufsicht, Dokumentation.",
                        explanationEn = "Correct! HR/recruitment AI is classified as **High risk** (Annex III EU AI Act). It influences people in critical life areas (employment) and has strict obligations: transparency, human oversight, documentation.",
                    ),
                    ContentBlock.RiskThermometer(),
                ),
            ),

            // ═══════════════════════════════════════════════════════════════
            // Section 6: Final Quiz
            // ═══════════════════════════════════════════════════════════════
            LessonSection(
                titleDe = "6. Wissens-Check",
                titleEn = "6. Knowledge check",
                blocks = listOf(
                    ContentBlock.Quiz(
                        questionDe = "Was ist der Hauptunterschied zwischen klassischer Software und KI?",
                        questionEn = "What is the main difference between classical software and AI?",
                        options = listOf(
                            QuizOption("KI ist immer schneller", "AI is always faster", isCorrect = false),
                            QuizOption("KI braucht kein Internet", "AI doesn't need internet", isCorrect = false),
                            QuizOption("KI lernt selbstständig aus Daten", "AI learns from data on its own", isCorrect = true),
                            QuizOption("KI ist nur für große Unternehmen", "AI is only for large companies", isCorrect = false),
                        ),
                        explanationDe = "KI-Systeme verbessern sich durch Erfahrung mit Daten, während klassische Software statische Regeln befolgt, die ein Programmierer geschrieben hat.",
                        explanationEn = "AI systems improve through experience with data, while classical software follows static rules written by a programmer.",
                    ),
                    ContentBlock.Quiz(
                        questionDe = "Welche Art von KI wird heute am häufigsten eingesetzt?",
                        questionEn = "Which type of AI is most commonly used today?",
                        options = listOf(
                            QuizOption("Schmale KI (Narrow AI)", "Narrow AI", isCorrect = true),
                            QuizOption("Allgemeine KI (General AI)", "General AI", isCorrect = false),
                            QuizOption("Superintelligente KI", "Superintelligence", isCorrect = false),
                            QuizOption("Emotionale KI", "Emotional AI", isCorrect = false),
                        ),
                        explanationDe = "Schmale KI (Narrow AI) ist die am weitesten verbreitete Form — für spezifische Aufgaben wie Bilderkennung, Sprachübersetzung oder Empfehlungssysteme optimiert.",
                        explanationEn = "Narrow AI is the most widespread form — optimized for specific tasks like image recognition, translation, or recommendation systems.",
                    ),
                    ContentBlock.Quiz(
                        questionDe = "Ein Social-Scoring-System, das Bürger bewertet, ist nach EU AI Act...",
                        questionEn = "A social scoring system that rates citizens is, under the EU AI Act...",
                        options = listOf(
                            QuizOption("Hohes Risiko", "High risk", isCorrect = false),
                            QuizOption("Geringes Risiko", "Low risk", isCorrect = false),
                            QuizOption("Unannehmbares Risiko (verboten)", "Unacceptable risk (banned)", isCorrect = true),
                            QuizOption("Minimal Risiko", "Minimal risk", isCorrect = false),
                        ),
                        explanationDe = "Richtig! Social Scoring ist nach Art. 5 EU AI Act **verboten** (unannehmbares Risiko). Es verstößt gegen Grundrechte und europäische Werte.",
                        explanationEn = "Correct! Social scoring is **banned** under Art. 5 EU AI Act (unacceptable risk). It violates fundamental rights and European values.",
                    ),
                    ContentBlock.Quiz(
                        questionDe = "Maschinelles Lernen ist ein Teilbereich der KI. Was braucht ein ML-System, um zu lernen?",
                        questionEn = "Machine learning is a subfield of AI. What does an ML system need to learn?",
                        options = listOf(
                            QuizOption("Nur einen Programmierer", "Only a programmer", isCorrect = false),
                            QuizOption("Trainingsdaten und Algorithmen", "Training data and algorithms", isCorrect = true),
                            QuizOption("Nur einen leistungsstarken Computer", "Only a powerful computer", isCorrect = false),
                            QuizOption("Internetverbindung", "Internet connection", isCorrect = false),
                        ),
                        explanationDe = "ML-Systeme brauchen Trainingsdaten (Beispiele), Algorithmen (Lernmethoden) und Rechenleistung. Der Programmierer wählt den Algorithmus — das System lernt die Regeln selbst.",
                        explanationEn = "ML systems need training data (examples), algorithms (learning methods), and computing power. The programmer chooses the algorithm — the system learns the rules itself.",
                    ),
                ),
            ),
        ),
    )
}
