package ai.ki_kompetenz_training_org.data.lessons

object Lesson3 {

    val lesson = InteractiveLesson(
        id = "lesson-3",
        lessonNumber = 3,
        titleDe = "Maschinelles Lernen: Wie KI aus Daten lernt",
        titleEn = "Machine Learning: How AI learns from data",
        descriptionDe = "ML-Grundlagen verstehen: Trainingsdaten, Algorithmen, Überanpassung und die Rolle von Datenqualität.",
        descriptionEn = "Understand ML basics: training data, algorithms, overfitting, and data quality.",
        durationMinutes = 20,
        objectivesDe = listOf(
            "Die 3 ML-Lernarten (Supervised, Unsupervised, Reinforcement) unterscheiden",
            "Trainings-, Validierungs- und Testdaten erklären",
            "Überanpassung (Overfitting) erkennen und vermeiden",
            "Datenqualität als KI-Erfolgsfaktor verstehen",
        ),
        objectivesEn = listOf(
            "Distinguish 3 ML learning types (Supervised, Unsupervised, Reinforcement)",
            "Explain training, validation, and test data",
            "Recognize and avoid overfitting",
            "Understand data quality as AI success factor",
        ),
        sections = listOf(
            LessonSection(
                titleDe = "1. Die drei Lernarten",
                titleEn = "1. The three learning types",
                blocks = listOf(
                    ContentBlock.Text(
                        textDe = "ML-Systeme lernen auf drei grundlegende Arten:\n\n" +
                            "Supervised Learning = Mit Lehrer\nDie KI bekommt Beispiele MIT Lösung. \"Das ist Spam\" / \"Das ist kein Spam.\"\n\n" +
                            "Unsupervised Learning = Selbstentdeckung\nDie KI findet Muster OHNE Lösung. \"Diese Kunden kaufenaehnlich aehnlich.\"\n\n" +
                            "Reinforcement Learning = Versuch und Irrtum\nDie KI lernt durch Belohnung und Bestrafung. Wie ein Hund, der Tricks lernt!",
                        textEn = "ML systems learn in three fundamental ways:\n\n" +
                            "Supervised Learning = With teacher\nAI gets examples WITH answers. \"This is spam\" / \"This is not spam.\"\n\n" +
                            "Unsupervised Learning = Self-discovery\nAI finds patterns WITHOUT answers. \"These customers buy similarly.\"\n\n" +
                            "Reinforcement Learning = Trial and error\nAI learns through reward and punishment. Like a dog learning tricks!",
                    ),
                    ContentBlock.Classification(
                        instructionDe = "Ordne die Beispiele der richtigen Lernart zu:",
                        instructionEn = "Assign each example to the correct learning type:",
                        categories = listOf(
                            ClassificationCategory(
                                nameDe = "Supervised",
                                nameEn = "Supervised",
                                emoji = "👨‍🏫",
                                items = listOf(
                                    ClassificationItem("Spam-Erkennung mit gelabelten E-Mails", "Spam detection with labeled emails"),
                                    ClassificationItem("Bilderkennung (Katze vs Hund)", "Image classification (cat vs dog)"),
                                    ClassificationItem("Ubersetzung (DE->EN Paare)", "Translation (DE->EN pairs)"),
                                ),
                            ),
                            ClassificationCategory(
                                nameDe = "Unsupervised",
                                nameEn = "Unsupervised",
                                emoji = "🔍",
                                items = listOf(
                                    ClassificationItem("Kundensegmentierung", "Customer segmentation"),
                                    ClassificationItem("Anomalieerkennung (Betrug)", "Anomaly detection (fraud)"),
                                    ClassificationItem("Themen-Clustering (Artikel)", "Topic clustering (articles)"),
                                ),
                            ),
                            ClassificationCategory(
                                nameDe = "Reinforcement",
                                nameEn = "Reinforcement",
                                emoji = "🎮",
                                items = listOf(
                                    ClassificationItem("Schach-AI (AlphaZero)", "Chess AI (AlphaZero)"),
                                    ClassificationItem("Robotergreifer lernen", "Robot gripper learning"),
                                    ClassificationItem("Autonomes Fahren", "Self-driving car"),
                                ),
                            ),
                        ),
                    ),
                ),
            ),

            LessonSection(
                titleDe = "2. Der ML-Pipeline: Von Daten zu Vorhersagen",
                titleEn = "2. The ML Pipeline: From data to predictions",
                blocks = listOf(
                    ContentBlock.Text(
                        textDe = "Jedes ML-System durchlaeuft dieselbe Pipeline:\n\n" +
                            "1. Datensammlung = Beispiele sammeln\n2. Datenvorbereitung = Bereinigen, Normalisieren\n3. Training = Algorithmus aus Daten lernen\n4. Validierung = Prüfen, ob es generalisiert\n5. Test = Finale Bewertung auf ungesehenen Daten\n6. Einsatz = Vorhersagen treffen",
                        textEn = "Every ML system goes through the same pipeline:\n\n" +
                            "1. Data collection = Gather examples\n2. Data preparation = Clean, normalize\n3. Training = Algorithm learns from data\n4. Validation = Check if it generalizes\n5. Testing = Final evaluation on unseen data\n6. Deployment = Make predictions",
                    ),
                    ContentBlock.FillBlank(
                        sentenceDe = "___ Daten werden verwendet, um die finale Leistung des Modells zu bewerten.",
                        sentenceEn = "___ data is used to evaluate the final model performance.",
                        blankKey = "Test",
                        choices = listOf("Trainings", "Validierungs", "Test", "Produktions"),
                        correctIndex = 2,
                        explanationDe = "Richtig! Testdaten hat das Modell noch nie gesehen — sie sind die ehrlichste Bewertung.",
                        explanationEn = "Correct! Test data has never been seen by the model — it is the most honest evaluation.",
                    ),
                    ContentBlock.FillBlank(
                        sentenceDe = "___ passiert, wenn ein Modell die Trainingsdaten auswendig lernt, aber neue Daten nicht versteht.",
                        sentenceEn = "___ happens when a model memorizes training data but fails on new data.",
                        blankKey = "Overfitting",
                        choices = listOf("Underfitting", "Overfitting", "Regularisierung", "Normalisierung"),
                        correctIndex = 1,
                        explanationDe = "Richtig! Overfitting = Auswendiglernen statt verstehen. Wie ein Schuler, der nur die Pruefungsaufgaben auswendig kann, aber die Theorie nicht versteht.",
                        explanationEn = "Correct! Overfitting = memorizing instead of understanding. Like a student who only memorizes exam questions but doesn't understand the theory.",
                    ),
                    ContentBlock.TrueFalse(
                        statementDe = "Mehr Trainingsdaten fuehren immer zu einem besseren Modell.",
                        statementEn = "More training data always leads to a better model.",
                        isTrue = false,
                        explanationDe = "Falsch! Schlechte Daten + mehr Daten = schlechteres Modell. \"Garbage in, garbage out.\" Qualitaet ist wichtiger als Quantitaet!",
                        explanationEn = "False! Bad data + more data = worse model. \"Garbage in, garbage out.\" Quality is more important than quantity!",
                    ),
                ),
            ),

            LessonSection(
                titleDe = "3. Overfitting: Wenn KI auswendig lernt",
                titleEn = "3. Overfitting: When AI memorizes",
                blocks = listOf(
                    ContentBlock.Callout(
                        type = CalloutType.EXAMPLE,
                        textDe = "Das Praezisions-Recall-Dilemma:\n\nEin Spam-Filter kann:\n- Jede Spam-Mail blockieren (aber auch wichtige Mails!)\n- Keine wichtige Mail blockieren (aber Spam durchlassen!)\n\nDas ist der Trade-off zwischen Praezision (genau richtig) und Recall (findet alles).",
                        textEn = "The Precision-Recall Dilemma:\n\nA spam filter can:\n- Block every spam email (but also important ones!)\n- Never block important emails (but let spam through!)\n\nThis is the trade-off between precision (exactly right) and recall (finds everything).",
                    ),
                    ContentBlock.KnowledgeCheck(
                        questionDe = "Wie wuerdest du Overfitting bei einem KI-Modell erkennen?",
                        questionEn = "How would you recognize overfitting in an AI model?",
                        answerDe = "Das Modell hat 99% Genauigkeit auf Trainingsdaten, aber nur 60% auf Testdaten. Die Luecke zwischen Trainings- und Test-Performance ist der beste Indikator fuer Overfitting!",
                        answerEn = "The model has 99% accuracy on training data but only 60% on test data. The gap between training and test performance is the best indicator of overfitting!",
                    ),
                    ContentBlock.TrueFalse(
                        statementDe = "Ein Modell mit 100% Trainingsgenauigkeit ist immer das beste Modell.",
                        statementEn = "A model with 100% training accuracy is always the best model.",
                        isTrue = false,
                        explanationDe = "Falsch! 100% Trainingsgenauigkeit ist oft ein Warnsignal fuer Overfitting. Ein Modell mit 85% Trainings- und 83% Testgenauigkeit ist wahrscheinlich besser als eines mit 100%/60%.",
                        explanationEn = "False! 100% training accuracy is often a warning sign for overfitting. A model with 85% training and 83% test accuracy is probably better than one with 100%/60%.",
                    ),
                ),
            ),

            LessonSection(
                titleDe = "4. Datenqualitaet: Der wichtigste Erfolgsfaktor",
                titleEn = "4. Data quality: The most important success factor",
                blocks = listOf(
                    ContentBlock.Text(
                        textDe = "Die 5 Dimensionen guter Daten:\n\n" +
                            "1. Vollstaendigkeit = Keine fehlenden Werte\n2. Korrektheit = Keine falschen Labels\n3. Konsistenz = Einheitliches Format\n4. Aktualitaet = Nicht veraltet\n5. Repraesentativitaet = Spiegelt die Realitaet wider",
                        textEn = "The 5 dimensions of good data:\n\n" +
                            "1. Completeness = No missing values\n2. Correctness = No wrong labels\n3. Consistency = Uniform format\n4. Timeliness = Not outdated\n5. Representativeness = Reflects reality",
                    ),
                    ContentBlock.Quiz(
                        questionDe = "Ein Gesichtsrekennungs-System wird nur mit hellhautigen Gesichtern trainiert. Welches Datenproblem liegt vor?",
                        questionEn = "A facial recognition system is only trained with light-skinned faces. Which data problem is this?",
                        options = listOf(
                            QuizOption("Vollstaendigkeit", "Completeness", isCorrect = false),
                            QuizOption("Repraesentativitaet (Bias)", "Representativeness (Bias)", isCorrect = true),
                            QuizOption("Aktualitaet", "Timeliness", isCorrect = false),
                            QuizOption("Konsistenz", "Consistency", isCorrect = false),
                        ),
                        explanationDe = "Richtig! Das ist ein Repraesentativitaets-Problem (Selection Bias). Das EU AI Act verpflichtet Anbieter, ihre Trainingsdaten auf Bias zu pruefen (Art. 10).",
                        explanationEn = "Correct! This is a representativeness problem (Selection Bias). The EU AI Act requires providers to check training data for bias (Art. 10).",
                    ),
                    ContentBlock.Callout(
                        type = CalloutType.LAW,
                        textDe = "EU AI Act Datenpflichten:\nArt. 10: Trainingsdaten muessen frei von Fehlern und Bias sein.\nArt. 10(3): Daten muessen die dem System zugewiesenen Zielgruppen repraesentieren.\nArt. 12: Aufzeichnungspflicht fuer Leistungsmetriken.",
                        textEn = "EU AI Act Data Obligations:\nArt. 10: Training data must be free of errors and bias.\nArt. 10(3): Data must represent the target groups.\nArt. 12: Recording obligation for performance metrics.",
                    ),
                    ContentBlock.TrueFalse(
                        statementDe = "Eine KI kann unfair sein, selbst wenn der Code fehlerfrei ist.",
                        statementEn = "An AI can be unfair even if the code is bug-free.",
                        isTrue = true,
                        explanationDe = "Richtig! Bias kommt oft aus den DATEN, nicht aus dem Code. Perfekter Algorithmus + voreingenommene Daten = voreingenommene KI. Deshalb ist Datenqualitaet so wichtig!",
                        explanationEn = "Correct! Bias often comes from the DATA, not the code. Perfect algorithm + biased data = biased AI. That is why data quality is so important!",
                    ),
                ),
            ),

            LessonSection(
                titleDe = "5. EU AI Act: Was heisst das fuer ML-Systeme?",
                titleEn = "5. EU AI Act: What does this mean for ML systems?",
                blocks = listOf(
                    ContentBlock.Text(
                        textDe = "Hochrisiko-KI-Systeme muessen nachweisen:\n\n- Korrekte und repraesentative Trainingsdaten\n- Getestete Leistung auf unparteiischen Testdaten\n- Dokumentation des Trainingsprozesses\n- Menschliche Aufsicht\n- Cybersicherheit\n- Aufzeichnung aller Leistungsmetriken",
                        textEn = "High-risk AI systems must demonstrate:\n\n- Correct and representative training data\n- Tested performance on unbiased test data\n- Documentation of the training process\n- Human oversight\n- Cybersecurity\n- Recording of all performance metrics",
                    ),
                    ContentBlock.RiskThermometer(),
                    ContentBlock.Quiz(
                        questionDe = "Welche ML-Massnahme ist nach EU AI Act fuer hochriskante KI PFLICHT?",
                        questionEn = "Which ML measure is MANDATORY for high-risk AI under EU AI Act?",
                        options = listOf(
                            QuizOption("Reinforcement Learning verwenden", "Use reinforcement learning", isCorrect = false),
                            QuizOption("Dokumentation der Trainingsdaten", "Training data documentation", isCorrect = true),
                            QuizOption("Open-Source Code veroeffentlichen", "Publish open-source code", isCorrect = false),
                            QuizOption("100% Genauigkeit erreichen", "Achieve 100% accuracy", isCorrect = false),
                        ),
                        explanationDe = "Richtig! Art. 10 EU AI Act: Anbieter von Hochrisiko-KI muessen die Trainingsdaten dokumentieren — Herkunft, Qualitaet, Bias-Pruefung. Open-Source und 100% Genauigkeit sind nicht Pflicht.",
                        explanationEn = "Correct! Art. 10 EU AI Act: High-risk AI providers must document training data — origin, quality, bias check. Open-source and 100% accuracy are not mandatory.",
                    ),
                ),
            ),

            LessonSection(
                titleDe = "6. Wissens-Quiz",
                titleEn = "6. Knowledge Quiz",
                blocks = listOf(
                    ContentBlock.Quiz(
                        questionDe = "Welche Lernart passt am besten: Ein Schachcomputer lernt durch Millionen Selbstanalysierter Partien?",
                        questionEn = "Which learning type fits best: A chess computer learns through millions of self-played games?",
                        options = listOf(
                            QuizOption("Supervised Learning", "Supervised Learning", isCorrect = false),
                            QuizOption("Unsupervised Learning", "Unsupervised Learning", isCorrect = false),
                            QuizOption("Reinforcement Learning", "Reinforcement Learning", isCorrect = true),
                            QuizOption("Transfer Learning", "Transfer Learning", isCorrect = false),
                        ),
                        explanationDe = "Richtig! AlphaZero nutzt Reinforcement Learning — es spielt gegen sich selbst und erhaelt Belohnung fuer Siege. Kein Mensch muss die Zuege bewerten.",
                        explanationEn = "Correct! AlphaZero uses reinforcement learning — it plays against itself and gets rewarded for wins. No human needs to rate the moves.",
                    ),
                    ContentBlock.Quiz(
                        questionDe = "Ein Bilderkennungs-System hat 98% Trainings- aber nur 72% Testgenauigkeit. Was ist wahrscheinlich los?",
                        questionEn = "An image recognition system has 98% training but only 72% test accuracy. What is probably going on?",
                        options = listOf(
                            QuizOption("Underfitting", "Underfitting", isCorrect = false),
                            QuizOption("Overfitting", "Overfitting", isCorrect = true),
                            QuizOption("Perfektes Modell", "Perfect model", isCorrect = false),
                            QuizOption("Fehlerhafte Testdaten", "Broken test data", isCorrect = false),
                        ),
                        explanationDe = "Korrekt! Die 26-Prozent-Luecke ist klassisches Overfitting. Das Modell hat die Trainingsbilder auswendig gelernt, verallgemeinert aber nicht. Massnahme: Regularisierung, mehr Daten, einfacheres Modell.",
                        explanationEn = "Correct! The 26-percent gap is classic overfitting. The model memorized training images but doesn't generalize. Fix: regularization, more data, simpler model.",
                    ),
                    ContentBlock.Quiz(
                        questionDe = "Warum ist Datenqualitaet wichtiger als die Menge der Daten?",
                        questionEn = "Why is data quality more important than data quantity?",
                        options = listOf(
                            QuizOption("KI braucht weniger Daten als gedacht", "AI needs less data than thought", isCorrect = false),
                            QuizOption("Fehlerhafte Daten produzieren fehlerhafte Vorhersagen", "Flawed data produces flawed predictions", isCorrect = true),
                            QuizOption("Mehr Daten macht KI immer schneller", "More data always makes AI faster", isCorrect = false),
                            QuizOption("Datenqualitaet ist egal bei Deep Learning", "Data quality doesn't matter for deep learning", isCorrect = false),
                        ),
                        explanationDe = "Garbage in, garbage out! Selbst das beste Modell produziert falsche Ergebnisse mit falschen Daten. Bias in Trainingsdaten fuehrt zu diskriminierenden KI-Entscheidungen.",
                        explanationEn = "Garbage in, garbage out! Even the best model produces wrong results with wrong data. Bias in training data leads to discriminatory AI decisions.",
                    ),
                    ContentBlock.RiskThermometer(),
                ),
            ),
        ),
    )
}
