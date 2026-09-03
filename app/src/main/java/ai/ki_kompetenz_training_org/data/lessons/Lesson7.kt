package ai.ki_kompetenz_training_org.data.lessons

object Lesson7 {

    val lesson = InteractiveLesson(
        id = "lesson-7",
        lessonNumber = 7,
        titleDe = "Computer Vision: KI sieht die Welt",
        titleEn = "Computer Vision: AI sees the world",
        descriptionDe = "Bilderkennung, Gesichtserkennung, Objekterkennung und ethische Fragen.",
        descriptionEn = "Image recognition, facial recognition, object detection, and the ethical questions.",
        durationMinutes = 20,
        objectivesDe = listOf(
            "Computer Vision als KI-Teilgebiet erklaeren",
            "Bilderkennung vs. Objekterkennung unterscheiden",
            "Gesichtserkennung und Risiken verstehen",
            "DSGVO-Rechtsprechung zu Gesichtserkennung kennen",
        ),
        objectivesEn = listOf(
            "Explain Computer Vision as an AI subfield",
            "Distinguish image recognition vs. object detection",
            "Understand facial recognition and its risks",
            "Know GDPR case law on facial recognition",
        ),
        sections = listOf(
            LessonSection(
                titleDe = "1. Wie KI Bilder versteht",
                titleEn = "1. How AI understands images",
                blocks = listOf(
                    ContentBlock.Text(
                        textDe = "Fuer KI ist ein Bild eine Tabelle von Zahlen. Ein 224x224 Bild mit 3 Farbkanalen (RGB) = 150.528 Zahlen. Die KI lernt Muster: Kanten, Texturen, Formen, Farben.",
                        textEn = "For AI, an image is a table of numbers. A 224x224 pixel image with 3 color channels (RGB) = 150,528 numbers. The AI learns patterns: edges, textures, shapes, colors.",
                    ),
                    ContentBlock.TrueFalse(
                        statementDe = "KI erkennt Bilder durch Pixel-for-Pixel-Vergleich.",
                        statementEn = "AI recognizes images by pixel-by-pixel comparison.",
                        isTrue = false,
                        explanationDe = "Falsch! KI erkennt ABSTRAKTE Merkmale: Kanten, Formen, Texturen. Deshalb erkennt sie eine Katze auch in einer Skizze!",
                        explanationEn = "False! AI recognizes ABSTRACT FEATURES. That is why it recognizes a cat even in a sketch!",
                    ),
                    ContentBlock.KnowledgeCheck(
                        questionDe = "Wie viele Zahlen hat ein KI-Bild?",
                        questionEn = "How many numbers does a KI image have?",
                        answerDe = "150,528 fuer 224x224. Moderne Modelle nutzen 1024x1024 = ueber 3 Millionen Zahlen pro Bild!",
                        answerEn = "Over 3 million for 1024x1024 pixels!",
                    ),
                ),
            ),
            LessonSection(
                titleDe = "2. Bilderkennung vs. Objekterkennung",
                titleEn = "2. Image Classification vs. Object Detection",
                blocks = listOf(
                    ContentBlock.Classification(
                        instructionDe = "Ordne die Aufgaben zu:",
                        instructionEn = "Assign the tasks to:",
                        categories = listOf(
                            ClassificationCategory(
                                nameDe = "Bilderkennung", nameEn = "Image Classification", emoji = "🏷️",
                                items = listOf(
                                    ClassificationItem("Katze oder Hund?", "Cat or dog?"),
                                    ClassificationItem("Roentgenbild: Tumor ja/nein?", "X-ray: tumor yes/no?"),
                                    ClassificationItem("Bild: Happy oder Sad?", "Image: happy or sad?"),
                                ),
                            ),
                            ClassificationCategory(
                                nameDe = "Objekterkennung", nameEn = "Object Detection", emoji = "🔍",
                                items = listOf(
                                    ClassificationItem("Autos im Verkehr finden", "Find all cars in traffic"),
                                    ClassificationItem("Personen im Raum zaehlen", "Count people in room"),
                                    ClassificationItem("Verkehrsschilder erkennen", "Detect traffic signs"),
                                ),
                            ),
                            ClassificationCategory(
                                nameDe = "Segmentierung", nameEn = "Segmentation", emoji = "🧩",
                                items = listOf(
                                    ClassificationItem("Selbstfahrer-Spur markieren", "Mark self-driving lane"),
                                    ClassificationItem("Tumor-Gewebe abgrenzen", "Outline tumor tissue"),
                                    ClassificationItem("Hintergrund entfernen", "Remove background"),
                                ),
                            ),
                        ),
                    ),
                    ContentBlock.Text(
                        textDe = "Bilderkennung = Was ist DAS? Ein Label. Objekterkennung = Wo sind DIE? Viele Objekte mit Position. Segmentierung = Welcher Pixel gehoert wozu? Komplexitaet: Bild < Objekt < Segmentierung",
                        textEn = "Image classification = What is THIS? One label. Object detection = Where are THEY? Many objects with positions. Segmentation = Which pixel belongs to what? Complexity: Image < Object < Segmentation",
                    ),
                ),
            ),
            LessonSection(
                titleDe = "3. Gesichtserkennung: Technik und Ethik",
                titleEn = "3. Facial Recognition: Technology and Ethics",
                blocks = listOf(
                    ContentBlock.Text(
                        textDe = "Gesichtserkennung in 3 Schritten: 1. Detection: Wo ist das Gesicht? 2. Alignment: Augen-Nose-Mund positionieren. 3. Embedding: Gesicht zu 128 Zahlen. Distanz < 0.6 = dieselbe Person.",
                        textEn = "Facial recognition in 3 steps: 1. Detection. 2. Alignment. 3. Embedding: Face to 128 numbers. Distance < 0.6 = same person.",
                    ),
                    ContentBlock.TrueFalse(
                        statementDe = "Der EU AI Act verbietet Gesichtserkennung im oeffentlichen Raum komplett.",
                        statementEn = "The EU AI Act completely bans facial recognition in public spaces.",
                        isTrue = false,
                        explanationDe = "Falsch! Art. 5 verbietet nur ECHTZEIT-FERNERKENNUNG in OEFFENTLICHEN Raeumen. Arbeitsplatz und Handy ist erlaubt mit strengen Regeln.",
                        explanationEn = "False! Art. 5 only bans REAL-TIME REMOTE recognition in PUBLIC spaces. Workplace and phone is allowed with strict rules.",
                    ),
                    ContentBlock.Callout(
                        type = CalloutType.LAW,
                        textDe = "EuGH 2019: Biometrische Daten zum Einwohnermeldegesetz VERBOTEN. Danach verboten Hamburg, San Francisco und Bologna Gesichtserkennung.",
                        textEn = "CJEU 2019: Biometric data for residential laws are BANNED. Hamburg, San Francisco, and Bologna banned facial recognition.",
                    ),
                ),
            ),
            LessonSection(
                titleDe = "4. Deepfakes: KI-generierte Realitaeten",
                titleEn = "4. Deepfakes: AI-generated realities",
                blocks = listOf(
                    ContentBlock.Text(
                        textDe = "Deepfakes nutzen Computer Vision + NLP: Face Swapping, Voice Cloning, Full Body, Text-to-Video.",
                        textEn = "Deepfakes use Computer Vision + NLP: Face Swapping, Voice Cloning, Full Body, Text-to-Video.",
                    ),
                    ContentBlock.Quiz(
                        questionDe = "Welches Recht ist durch Deepfakes besonders gefaehrd?",
                        questionEn = "Which right is most endangered by deepfakes?",
                        options = listOf(
                            QuizOption("Richt auf freie Meinungsaeusserung", "Freedom of expression", isCorrect = false),
                            QuizOption("Persoenlichkeitsrecht", "Right to personality", isCorrect = true),
                            QuizOption("Recht auf Bildung", "Right to education", isCorrect = false),
                            QuizOption("Wettbewerbsrecht", "Right to competition", isCorrect = false),
                        ),
                        explanationDe = "Richtig! Deepfakes greifen Identitaet und Wuerde einer Person an. EU AI Act Art. 5(1)(c) verbietet manipulative Techniken.",
                        explanationEn = "Correct! Deepfakes attack identity and dignity. EU AI Act Art. 5(1)(c) bans manipulative techniques.",
                    ),
                    ContentBlock.Callout(
                        type = CalloutType.WARNING,
                        textDe = "KI kann jetzt aus einem Foto ein komplettes Video generieren! EU AI Act Art. 52 verlangt Kennzeichnungspflichten.",
                        textEn = "AI can now generate a complete video from ONE photo! EU AI Act Art. 52 requires labeling obligations.",
                    ),
                ),
            ),
            LessonSection(
                titleDe = "5. Wissens-Quiz",
                titleEn = "5. Knowledge Quiz",
                blocks = listOf(
                    ContentBlock.Quiz(
                        questionDe = "Ein autonomes Auto muss Fussgaenger erkennen. Welche CV-Aufgabe?",
                        questionEn = "A self-driving car must detect pedestrians. Which CV task?",
                        options = listOf(
                            QuizOption("Bilderkennung", "Image classification", isCorrect = false),
                            QuizOption("Objekterkennung", "Object detection", isCorrect = true),
                            QuizOption("Segmentierung", "Segmentation", isCorrect = false),
                            QuizOption("Bildgenerierung", "Image generation", isCorrect = false),
                        ),
                        explanationDe = "Richtig! Das Auto muss mehrere Fussgaenger gleichzeitig finden = Objekterkennung.",
                        explanationEn = "Correct! The car must find multiple pedestrians simultaneously = object detection.",
                    ),
                    ContentBlock.Quiz(
                        questionDe = "Warum verarbeiten KI-Bilder meist in 224x224 Pixel?",
                        questionEn = "Why does AI process images at 224x224 pixels?",
                        options = listOf(
                            QuizOption("Weil Bilder ohnehin klein sind", "Because images are small", isCorrect = false),
                            QuizOption("Rechenleistung: Mehr Pixel = mehr Berechnung", "Compute: more pixels = more computation", isCorrect = true),
                            QuizOption("DSGVO schreibt 224x224 vor", "GDPR requires 224x224", isCorrect = false),
                            QuizOption("Das ist die maximale Aufloesung", "That is the max resolution", isCorrect = false),
                        ),
                        explanationDe = "Richtig! 1024x1024 statt 224x224 = 20x mehr Rechenleistung!",
                        explanationEn = "Correct! 1024x1024 instead of 224x224 = 20x more compute!",
                    ),
                    ContentBlock.RiskThermometer(),
                ),
            ),
        ),
    )
}
