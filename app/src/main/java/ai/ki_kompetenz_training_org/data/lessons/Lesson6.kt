package ai.ki_kompetenz_training_org.data.lessons

object Lesson6 {

    val lesson = InteractiveLesson(
        id = "lesson-6",
        lessonNumber = 6,
        titleDe = "Natürliche Sprachverarbeitung (NLP)",
        titleEn = "Natural Language Processing (NLP)",
        descriptionDe = "Wie KI Sprache versteht, Chatbots funktionieren und Text generiert.",
        descriptionEn = "How AI understands language, chatbots work, and generates text.",
        durationMinutes = 20,
        objectivesDe = listOf(
            "NLP als KI-Teilgebiet erklären",
            "Tokenisierung und Embeddings verstehen",
            "Transformer-Architektur einfach erklären",
            "Halluzinationen und ihre Ursachen kennen",
        ),
        objectivesEn = listOf(
            "Explain NLP as an AI subfield",
            "Understand tokenization and embeddings",
            "Simply explain Transformer architecture",
            "Know hallucinations and their causes",
        ),
        sections = listOf(
            LessonSection(
                titleDe = "1. Wie versteht KI Sprache?",
                titleEn = "1. How does AI understand language?",
                blocks = listOf(
                    ContentBlock.Text(
                        textDe = "KI versteht Sprache nicht wie wir. Sie rechnet mit Zahlen:\n\n" +
                            "Schritt 1: Tokenisierung → Text in Stücke zerlegen\n\"KI ist toll\" → [\"KI\", \" ist\", \" toll\"]\n\n" +
                            "Schritt 2: Embeddings → Wörter werden zu Vektoren\n\"KI\" → [0.23, -0.71, 0.45, ...] (768 Zahlen!)\n\n" +
                            "Schritt 3: Attention → Beziehungen zwischen Wörtern finden\n\"KI\" und \"Intelligenz\" gehören zusammen\n\n" +
                            "Schritt 4: Generation → Wahrscheinlichkeit des nächsten Wortes\nP(KI | \"EU Act ist wichtig für\") = 85%",
                        textEn = "AI doesn't understand language like we do. It calculates with numbers:\n\n" +
                            "Step 1: Tokenization → Break text into pieces\n\"AI is great\" → [\"AI\", \" is\", \" great\"]\n\n" +
                            "Step 2: Embeddings → Words become vectors\n\"AI\" → [0.23, -0.71, 0.45, ...] (768 numbers!)\n\n" +
                            "Step 3: Attention → Find relationships between words\n\"AI\" and \"intelligence\" belong together\n\n" +
                            "Step 4: Generation → Probability of next word\nP(AI | \"EU Act is important for\") = 85%",
                    ),
                    ContentBlock.FillBlank(
                        sentenceDe = "___ ist der Prozess, Text in kleine Stücke (Tokens) zu zerlegen.",
                        sentenceEn = "___ is the process of breaking text into small pieces (tokens).",
                        blankKey = "Tokenisierung",
                        choices = listOf("Embedding", "Tokenisierung", "Attention", "Generation"),
                        correctIndex = 1,
                        explanationDe = "Richtig! Tokenisierung ist Schritt 1 der NLP-Pipeline. ChatGPT hat ~100.000 verschiedene Tokens.",
                        explanationEn = "Correct! Tokenization is step 1 of the NLP pipeline. ChatGPT has ~100,000 different tokens.",
                    ),
                    ContentBlock.TrueFalse(
                        statementDe = "KI kann die Bedeutung von Wörtern verstehen, weil sie Bewusstsein hat.",
                        statementEn = "AI can understand the meaning of words because it has consciousness.",
                        isTrue = false,
                        explanationDe = "Falsch! KI hat kein Bewusstsein. Sie versteht Bedeutung nur als Muster in Zahlen. \"König - Mann + Frau = Königin\" funktioniert, weil diese Muster in den Trainingsdaten existieren.",
                        explanationEn = "False! AI has no consciousness. It understands meaning only as patterns in numbers. \"King - Man + Woman = Queen\" works because these patterns exist in training data.",
                    ),
                ),
            ),

            LessonSection(
                titleDe = "2. Transformer: Die Revolution",
                titleEn = "2. Transformers: The Revolution",
                blocks = listOf(
                    ContentBlock.Callout(
                        type = CalloutType.DEFINITION,
                        textDe = "Der Transformer (2017, Google) ist die Architektur hinter GPT, BERT, Claude, Gemini und fast allen modernen Sprach-KIs.\n\nKernidee: Attention — statt Wörter sequenziell zu lesen, schaut sich die KI gleichzeitig auf ALLE Wörter und entscheidet, welche wichtig sind.",
                        textEn = "The Transformer (2017, Google) is the architecture behind GPT, BERT, Claude, Gemini, and almost all modern language AIs.\n\nCore idea: Attention — instead of reading words sequentially, the AI looks at ALL words simultaneously and decides which are important.",
                    ),
                    ContentBlock.Classification(
                        instructionDe = "Ordne die KI-Systeme der richtigen Architektur zu:",
                        instructionEn = "Assign each AI system to the correct architecture:",
                        categories = listOf(
                            ClassificationCategory(
                                nameDe = "Transformer-basiert",
                                nameEn = "Transformer-based",
                                emoji = "🤖",
                                items = listOf(
                                    ClassificationItem("GPT-4", "GPT-4"),
                                    ClassificationItem("BERT", "BERT"),
                                    ClassificationItem("Claude", "Claude"),
                                    ClassificationItem("Gemini", "Gemini"),
                                ),
                            ),
                            ClassificationCategory(
                                nameDe = "Recurrent (älter)",
                                nameEn = "Recurrent (older)",
                                emoji = "🔄",
                                items = listOf(
                                    ClassificationItem("LSTM (2015)", "LSTM (2015)"),
                                    ClassificationItem("Google Translate (vor 2017)", "Google Translate (before 2017)"),
                                    ClassificationItem("Siri (ursprünglich)", "Siri (originally)"),
                                ),
                            ),
                            ClassificationCategory(
                                nameDe = "Keine KI / Regeln",
                                nameEn = "No AI / Rule-based",
                                emoji = "⚙️",
                                items = listOf(
                                    ClassificationItem("Autokorrektur (Einfach)", "Autocorrect (Simple)"),
                                    ClassificationItem("Regex-Suche", "Regex search"),
                                    ClassificationItem("Entscheidungsbäume", "Decision trees"),
                                ),
                            ),
                        ),
                    ),
                    ContentBlock.Text(
                        textDe = "Warum sind Transformer besser als ältere Modelle?\n\n" +
                            "Parallelisierung: GPUs können alle Wörter gleichzeitig verarbeiten\n" +
                            "Langstrecken-Beziehungen: Wort 1 und Wort 1000 können direkt verknüpft werden\n" +
                            "Skalierbarkeit: Mehr Daten + mehr Rechenleistung = bessere Ergebnisse\n" +
                            "Vielseitigkeit: Funktioniert für Text, Bild, Audio, Video, Code...",
                        textEn = "Why are Transformers better than older models?\n\n" +
                            "Parallelization: GPUs can process all words simultaneously\n" +
                            "Long-range relationships: Word 1 and word 1000 can be directly linked\n" +
                            "Scalability: More data + more compute = better results\n" +
                            "Versatility: Works for text, image, audio, video, code...",
                    ),
                ),
            ),

            LessonSection(
                titleDe = "3. Halluzinationen: Wenn KI erfindet",
                titleEn = "3. Hallucinations: When AI invents",
                blocks = listOf(
                    ContentBlock.Callout(
                        type = CalloutType.WARNING,
                        textDe = "Halluzinationen sind KEIN Bug — sie sind ein Feature der Funktionsweise!\n\nKI generiert das wahrscheinlichste nächste Wort — nicht das WAHRE. Wenn die KI etwas nicht weiß, erfindet sie trotzdem etwas Plausibles. Das sieht aus wie Wissen, ist aber Rat.",
                        textEn = "Hallucinations are NOT a bug — they are a feature of how it works!\n\nAI generates the most PROBABLE next word — not the CORRECT one. When AI doesn't know something, it still invents something plausible. It looks like knowledge, but it's guesswork.",
                    ),
                    ContentBlock.TrueFalse(
                        statementDe = "KI halluziniert nur, wenn sie falsch programmiert wurde.",
                        statementEn = "AI only hallucinates when it was incorrectly programmed.",
                        isTrue = false,
                        explanationDe = "Falsch! Halluzinationen sind ein inhärentes Problem der Textgenerierung. JEDE KI, die Text generiert, kann halluzinieren — ChatGPT, Claude, Gemini, alle. Es gibt keine 100% Lösung.",
                        explanationEn = "False! Hallucinations are an inherent problem of text generation. EVERY AI that generates text can hallucinate — ChatGPT, Claude, Gemini, all of them. There is no 100% solution.",
                    ),
                    ContentBlock.Classification(
                        instructionDe = "Welche Aussagen sind Halluzinationen, welche sind korrekt?",
                        instructionEn = "Which statements are hallucinations, which are correct?",
                        categories = listOf(
                            ClassificationCategory(
                                nameDe = "Korrekt",
                                nameEn = "Correct",
                                emoji = "✅",
                                items = listOf(
                                    ClassificationItem("EU AI Act trat August 2024 in Kraft", "EU AI Act entered force August 2024"),
                                    ClassificationItem("KI ist ein Teilgebiet der Informatik", "AI is a subfield of computer science"),
                                ),
                            ),
                            ClassificationCategory(
                                nameDe = "Halluzination",
                                nameEn = "Hallucination",
                                emoji = "⚠️",
                                items = listOf(
                                    ClassificationItem("EU AI Act verpflichtet KI zum Lachen", "EU AI Act requires AI to laugh"),
                                    ClassificationItem("KI wurde 1956 am MIT erfunden", "AI was invented at MIT in 1956"),
                                    ClassificationItem("Die Turing-Goldene Regel wurde 1984 verabschiedet", "The Turing Golden Rule was passed in 1984"),
                                ),
                            ),
                        ),
                    ),
                ),
            ),

            LessonSection(
                titleDe = "4. NLP im EU AI Act",
                titleEn = "4. NLP in the EU AI Act",
                blocks = listOf(
                    ContentBlock.Text(
                        textDe = "NLP-Systeme im EU AI Act:\n\n" +
                            "Chatbots (Risiko: Gering) → Transparenzpflicht\n" +
                            "Übersetzungs-KI (Risiko: Minimal) → Keine Pflichten\n" +
                            "KI-Bewerbungssystem (Risiko: Hoch) → Strenge Pflichten\n" +
                            "KI-Inhaltsmoderation (Risiko: Hoch) → Strenge Pflichten\n" +
                            "Deepfake-Erkennung (Risiko: Gering) → Transparenzpflicht",
                        textEn = "NLP systems in the EU AI Act:\n\n" +
                            "Chatbots (Risk: Low) → Transparency obligation\n" +
                            "Translation AI (Risk: Minimal) → No obligations\n" +
                            "AI recruitment system (Risk: High) → Strict obligations\n" +
                            "AI content moderation (Risk: High) → Strict obligations\n" +
                            "Deepfake detection (Risk: Low) → Transparency obligation",
                    ),
                    ContentBlock.Quiz(
                        questionDe = "Ein KI-Chatbot gibt medizinische Ratschläge. Welche Risikostufe?",
                        questionEn = "An AI chatbot gives medical advice. What risk level?",
                        options = listOf(
                            QuizOption("Minimal", "Minimal", isCorrect = false),
                            QuizOption("Gering", "Low", isCorrect = false),
                            QuizOption("Hoch", "High", isCorrect = true),
                            QuizOption("Unannehmbares Risiko", "Unacceptable", isCorrect = false),
                        ),
                        explanationDe = "Richtig! Medizinische KI = Anhang III Hochrisiko. Ein Chatbot, der Diagnosen stellt oder Behandlungen empfiehlt, beeinflusst die Gesundheit von Menschen — höchste Sorgfalt erforderlich!",
                        explanationEn = "Correct! Medical AI = Annex III High risk. A chatbot that diagnoses or recommends treatments affects people's health — highest care required!",
                    ),
                    ContentBlock.RiskThermometer(),
                ),
            ),

            LessonSection(
                titleDe = "5. Wissens-Quiz",
                titleEn = "5. Knowledge Quiz",
                blocks = listOf(
                    ContentBlock.Quiz(
                        questionDe = "Was ist ein Embedding in der NLP-Welt?",
                        questionEn = "What is an embedding in the NLP world?",
                        options = listOf(
                            QuizOption("Ein versteckter Text", "A hidden text", isCorrect = false),
                            QuizOption("Ein Zahlenvektor, der die Bedeutung eines Wortes repräsentiert", "A number vector representing word meaning", isCorrect = true),
                            QuizOption("Ein Bild, das in Text eingebettet ist", "An image embedded in text", isCorrect = false),
                            QuizOption("Ein Programmierfehler", "A programming error", isCorrect = false),
                        ),
                        explanationDe = "Richtig! Embeddings sind Zahlenvektoren (z.B. 768 Dimensionen), die semantische Beziehungen kodieren. \"König\" und \"Königin\" haben ähnliche Embeddings — die KI \"versteht\" die Ähnlichkeit.",
                        explanationEn = "Correct! Embeddings are number vectors (e.g. 768 dimensions) encoding semantic relationships. \"King\" and \"Queen\" have similar embeddings — the AI \"understands\" the similarity.",
                    ),
                    ContentBlock.Quiz(
                        questionDe = "Warum halluziniert ChatGPT manchmal Berufserfahrungen, die es nie gab?",
                        questionEn = "Why does ChatGPT sometimes hallucinate work experiences that never existed?",
                        options = listOf(
                            QuizOption("Die Daten sind korrupt", "The data is corrupt", isCorrect = false),
                            QuizOption("Es generiert plausibel klingende, aber falsche Texte", "It generates plausible-sounding but false texts", isCorrect = true),
                            QuizOption("Benutzer geben falsche Informationen ein", "Users enter false information", isCorrect = false),
                            QuizOption("Das Modell ist zu klein", "The model is too small", isCorrect = false),
                        ),
                        explanationDe = "Richtig! Die KI kennt die WIRKLICHKEIT nicht — sie kennt nur Muster in Trainingsdaten. Wenn ein Pattern plausibel klingt, generiert sie es — egal ob es wahr ist.",
                        explanationEn = "Correct! The AI doesn't know REALITY — it only knows patterns in training data. If a pattern sounds plausible, it generates it — regardless of whether it is true.",
                    ),
                    ContentBlock.Quiz(
                        questionDe = "Warum lösten Transformer das Problem rekurrenter Netze (RNNs)?",
                        questionEn = "Why did Transformers solve the problem of recurrent networks (RNNs)?",
                        options = listOf(
                            QuizOption("Sie sind schneller und billiger zu trainieren", "They are faster and cheaper to train", isCorrect = false),
                            QuizOption("Sie können alle Wörter gleichzeitig betrachten (Attention)", "They can look at all words simultaneously (Attention)", isCorrect = true),
                            QuizOption("Sie brauchen keine Trainingsdaten", "They don't need training data", isCorrect = false),
                            QuizOption("Sie haben weniger Schichten", "They have fewer layers", isCorrect = false),
                        ),
                        explanationDe = "Richtig! RNNs lesen sequenziell — sie vergessen den Anfang eines langen Textes. Attention im Transformer löst das: Jedes Wort kann direkt auf jedes andere Wort zugreifen.",
                        explanationEn = "Correct! RNNs read sequentially — they forget the beginning of a long text. Attention in Transformers solves this: Every word can directly access every other word.",
                    ),
                ),
            ),
        ),
    )
}
