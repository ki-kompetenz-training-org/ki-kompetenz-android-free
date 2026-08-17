package ai.ki_kompetenz_training_org.data.lessons

object Lesson5 {

    val lesson = InteractiveLesson(
        id = "lesson-5",
        lessonNumber = 5,
        titleDe = "Neuronale Netze: Das Gehirn der KI",
        titleEn = "Neural Networks: The brain of AI",
        descriptionDe = "Wie neuronale Netze funktionieren, Schichten, Aktivierungsfunktionen und warum sie der KI zugrunde liegen.",
        descriptionEn = "How neural networks work, layers, activation functions, and why they underpin AI.",
        durationMinutes = 20,
        objectivesDe = listOf(
            "Neuronale Netze als KI-Grundbaustein erklären",
            "Input-, Hidden- und Output-Schichten unterscheiden",
            "Aktivierungsfunktionen verstehen",
            "Deep Learning von einfachem ML unterscheiden",
        ),
        objectivesEn = listOf(
            "Explain neural networks as the AI building block",
            "Distinguish input, hidden, and output layers",
            "Understand activation functions",
            "Differentiate deep learning from simple ML",
        ),
        sections = listOf(
            LessonSection(
                titleDe = "1. Vom biologischen zum künstlichen Neuron",
                titleEn = "1. From biological to artificial neuron",
                blocks = listOf(
                    ContentBlock.Text(
                        textDe = "Biologisches Neuron:\n- Empfängt Signale über Synapsen\n- Verstärkt oder schwächt das Signal\n- Feuert wenn Schwellenwert erreicht\n\n" +
                            "Künstliches Neuron:\n- Empfängt Zahlen (Inputs)\n- Multipliziert mit Gewichten\n- Addiert Bias\n- Gibt Ergebnis durch Aktivierungsfunktion",
                        textEn = "Biological neuron:\n- Receives signals via synapses\n- Amplifies or weakens the signal\n- Fires when threshold reached\n\n" +
                            "Artificial neuron:\n- Receives numbers (inputs)\n- Multiplies by weights\n- Adds bias\n- Outputs result through activation function",
                    ),
                    ContentBlock.KnowledgeCheck(
                        questionDe = "Was sind \"Gewichte\" in einem neuronalen Netz?",
                        questionEn = "What are \"weights\" in a neural network?",
                        answerDe = "Gewichte sind Zahlen, die bestimmen, wie wichtig jedes Input-Signal ist. Beim Training werden diese Gewichte automatisch angepasst — genau wie ein Kind lernt, indem es seine \"Gewichte\" (Aufmerksamkeit) anpasst.\n\nBeispiel: Wenn du eine Katze erkennst, hat das Merkmal \"Punktohren\" ein höheres Gewicht als \"Anzahl der Beine\".",
                        answerEn = "Weights are numbers determining how important each input signal is. During training, these weights are automatically adjusted — like a child learning by adjusting its \"weights\" (attention).\n\nExample: When recognizing a cat, \"pointy ears\" has a higher weight than \"number of legs\".",
                    ),
                    ContentBlock.TrueFalse(
                        statementDe = "Neuronale Netze arbeiten genauso wie das menschliche Gehirn.",
                        statementEn = "Neural networks work exactly like the human brain.",
                        isTrue = false,
                        explanationDe = "Falsch! Neuronale Netze sind INSPIRIERT vom Gehirn, aber eine extreme Vereinfachung. Dein Gehirn hat 86 Milliarden Neuronen mit komplexer Chemie. Ein KNN hat Millionen von simplen Rechenoperationen. Ähnliche Idee, völlig andere Implementierung.",
                        explanationEn = "False! Neural networks are INSPIRED by the brain but are extremely simplified. Your brain has 86 billion neurons with complex chemistry. An ANN has millions of simple math operations. Similar idea, completely different implementation.",
                    ),
                ),
            ),

            LessonSection(
                titleDe = "2. Schichten-Architektur",
                titleEn = "2. Layer Architecture",
                blocks = listOf(
                    ContentBlock.Text(
                        textDe = "Ein neuronales Netz hat 3 Schichttypen:\n\n" +
                            "Input-Schicht = Empfängt die Rohdaten\n(Bildpixel, Textwörter, Zahlen)\n\n" +
                            "Hidden-Schichten = Extrahieren Merkmale\n(Kanten, Muster, Konzepte)\nJe mehr Hidden-Schichten, desto \"tiefer\" das Netz → Deep Learning!\n\n" +
                            "Output-Schicht = Gibt die Antwort\n(Klasse: Katze/Hund, Wahrscheinlichkeit, Text)",
                        textEn = "A neural network has 3 layer types:\n\n" +
                            "Input Layer = Receives raw data\n(Image pixels, text words, numbers)\n\n" +
                            "Hidden Layers = Extract features\n(Edges, patterns, concepts)\nMore hidden layers = \"deeper\" network → Deep Learning!\n\n" +
                            "Output Layer = Gives the answer\n(Class: Cat/Dog, probability, text)",
                    ),
                    ContentBlock.FillBlank(
                        sentenceDe = "Wenn ein Netz mehr als eine Hidden-Schicht hat, nennt man es ___ Learning.",
                        sentenceEn = "When a network has more than one hidden layer, it is called ___ Learning.",
                        blankKey = "Deep",
                        choices = listOf("Machine", "Deep", "Transfer", "Reinforcement"),
                        correctIndex = 1,
                        explanationDe = "Richtig! \"Deep\" bezieht sich auf die TIEFE des Netzes — die Anzahl der Hidden-Schichten. GPT-4 hat über 100 Schichten!",
                        explanationEn = "Correct! \"Deep\" refers to the DEPTH of the network — the number of hidden layers. GPT-4 has over 100 layers!",
                    ),
                    ContentBlock.Classification(
                        instructionDe = "Ordne die Netz-Komponenten der richtigen Schicht zu:",
                        instructionEn = "Assign each network component to the correct layer:",
                        categories = listOf(
                            ClassificationCategory(
                                nameDe = "Input-Schicht",
                                nameEn = "Input Layer",
                                emoji = "📥",
                                items = listOf(
                                    ClassificationItem("Bildpixel (224x224x3)", "Image pixels (224x224x3)"),
                                    ClassificationItem("Wort-Vektoren (Embeddings)", "Word vectors (embeddings)"),
                                    ClassificationItem("Temperatur-Sensorwert", "Temperature sensor value"),
                                ),
                            ),
                            ClassificationCategory(
                                nameDe = "Hidden-Schicht",
                                nameEn = "Hidden Layer",
                                emoji = "🔮",
                                items = listOf(
                                    ClassificationItem("Kantenerkennung", "Edge detection"),
                                    ClassificationItem("Textmuster-Extraktion", "Text pattern extraction"),
                                    ClassificationItem("Feature-Kombination", "Feature combination"),
                                ),
                            ),
                            ClassificationCategory(
                                nameDe = "Output-Schicht",
                                nameEn = "Output Layer",
                                emoji = "📤",
                                items = listOf(
                                    ClassificationItem("Katze (95%), Hund (5%)", "Cat (95%), Dog (5%)"),
                                    ClassificationItem("SPAM / KEIN SPAM", "SPAM / NOT SPAM"),
                                    ClassificationItem("Preisvorhersage: 42,50 EUR", "Price prediction: 42.50 EUR"),
                                ),
                            ),
                        ),
                    ),
                ),
            ),

            LessonSection(
                titleDe = "3. Aktivierungsfunktionen",
                titleEn = "3. Activation Functions",
                blocks = listOf(
                    ContentBlock.Text(
                        textDe = "Aktivierungsfunktionen entscheiden, ob ein Neuron \"feuert\":\n\n" +
                            "Sigmoid: Gibt Werte zwischen 0 und 1 — gut für Wahrscheinlichkeiten\n" +
                            "ReLU: Wenn >0 dann durchgeben, sonst 0 — einfach und schnell (Standard!)\n" +
                            "Softmax: Konvertiert Zahlen in Wahrscheinlichkeiten — für die Output-Schicht",
                        textEn = "Activation functions decide whether a neuron \"fires\":\n\n" +
                            "Sigmoid: Outputs values between 0 and 1 — good for probabilities\n" +
                            "ReLU: If >0 pass through, else 0 — simple and fast (standard!)\n" +
                            "Softmax: Converts numbers to probabilities — for the output layer",
                    ),
                    ContentBlock.FillBlank(
                        sentenceDe = "___ ist die meistverwendete Aktivierungsfunktion in Hidden-Schichten, weil sie einfach und schnell ist.",
                        sentenceEn = "___ is the most commonly used activation function in hidden layers because it is simple and fast.",
                        blankKey = "ReLU",
                        choices = listOf("Sigmoid", "ReLU", "Tanh", "Softmax"),
                        correctIndex = 1,
                        explanationDe = "Richtig! ReLU (Rectified Linear Unit) ist der Standard. Einfach: Wenn der Wert positiv ist, gib ihn durch. Sonst 0. Keine komplexe Berechnung — daher extrem schnell.",
                        explanationEn = "Correct! ReLU (Rectified Linear Unit) is the standard. Simple: If the value is positive, pass it through. Otherwise 0. No complex calculation — extremely fast.",
                    ),
                    ContentBlock.Quiz(
                        questionDe = "Welche Aktivierungsfunktion nutzt man für die Output-Schicht einer Katze-Hund-Klassifikation?",
                        questionEn = "Which activation function do you use for the output layer of a cat-dog classifier?",
                        options = listOf(
                            QuizOption("ReLU", "ReLU", isCorrect = false),
                            QuizOption("Softmax", "Softmax", isCorrect = true),
                            QuizOption("Sigmoid", "Sigmoid", isCorrect = false),
                            QuizOption("Keine Aktivierung", "No activation", isCorrect = false),
                        ),
                        explanationDe = "Richtig! Softmax konvertiert die Output-Zahlen in Wahrscheinlichkeiten, die sich zu 100% summieren: Katze 85%, Hund 15%. ReLU wäre für die Output-Schicht falsch.",
                        explanationEn = "Correct! Softmax converts output numbers to probabilities that sum to 100%: Cat 85%, Dog 15%. ReLU would be wrong for the output layer.",
                    ),
                ),
            ),

            LessonSection(
                titleDe = "4. Training: Wie das Netz lernt",
                titleEn = "4. Training: How the network learns",
                blocks = listOf(
                    ContentBlock.Text(
                        textDe = "Der Trainingszyklus:\n\n1. Forward Pass = Daten durch das Netz schicken → Vorhersage\n2. Fehler berechnen = Vorhersage vs. korrekte Antwort\n3. Backpropagation = Fehler rückwärts durch das Netz verteilen\n4. Gewichte anpassen = Kleine Korrekturen an den Gewichten\n5. Wiederholen = Millionen Male bis das Netz gut genug ist\n\n" +
                            "Das ist wie beim Golfspielen:\n- Du triffst den Ball (Vorhersage)\n- Du siehst, wie weit du daneben warst (Fehler)\n- Du passt deinen Schwung an (Gewichte anpassen)\n- Du übst weiter (Wiederholung)",
                        textEn = "The training cycle:\n\n1. Forward Pass = Push data through the network → prediction\n2. Calculate error = Prediction vs. correct answer\n3. Backpropagation = Distribute error backwards through the network\n4. Adjust weights = Small corrections to the weights\n5. Repeat = Millions of times until the network is good enough\n\n" +
                            "It is like playing golf:\n- You hit the ball (prediction)\n- You see how far off you were (error)\n- You adjust your swing (adjust weights)\n- You keep practicing (repeat)",
                    ),
                    ContentBlock.KnowledgeCheck(
                        questionDe = "Warum braucht man Millionen von Trainingsrunden?",
                        questionEn = "Why do you need millions of training rounds?",
                        answerDe = "Ein typisches KNN hat MILLIONEN von Gewichten. Jede Runde korrigiert sie nur minimal. Nach Millionen Runden konvergiert das Netz zur optimalen Lösung. GPT-4 wurde mit BILLIONEN von Textbeispielen trainiert!",
                        answerEn = "A typical ANN has MILLIONS of weights. Each round only corrects them minimally. After millions of rounds, the network converges to the optimal solution. GPT-4 was trained on BILLIONS of text examples!",
                    ),
                    ContentBlock.TrueFalse(
                        statementDe = "Nach dem Training kann man die Gewichte eines neuronalen Netzes einfach ändern.",
                        statementEn = "After training, you can easily change the weights of a neural network.",
                        isTrue = false,
                        explanationDe = "Falsch! Wenn du die Gewichte änderst, verlernt das Netz alles. Die Gewichte SIND das Wissen des Netzes. Es ist wie ein Golfschwung — wenn du ihn änderst, triffst du plötzlich ganz anders.",
                        explanationEn = "False! If you change the weights, the network unlearns everything. The weights ARE the network's knowledge. It is like a golf swing — change it and suddenly you hit completely differently.",
                    ),
                ),
            ),

            LessonSection(
                titleDe = "5. Wissens-Quiz",
                titleEn = "5. Knowledge Quiz",
                blocks = listOf(
                    ContentBlock.Quiz(
                        questionDe = "Was passiert in der Backpropagation?",
                        questionEn = "What happens in backpropagation?",
                        options = listOf(
                            QuizOption("Daten werden vorwärts geschickt", "Data is pushed forward", isCorrect = false),
                            QuizOption("Fehler werden rückwärts durch das Netz verteilt", "Errors are distributed backwards through the network", isCorrect = true),
                            QuizOption("Das Netz wird gelöscht", "The network is deleted", isCorrect = false),
                            QuizOption("Neue Schichten werden hinzugefügt", "New layers are added", isCorrect = false),
                        ),
                        explanationDe = "Richtig! Backpropagation berechnet den Fehler an der Output-Schicht und verteilt ihn rückwärts durch alle Hidden-Schichten, sodass jedes Gewicht weiß, wie viel es zum Fehler beigetragen hat.",
                        explanationEn = "Correct! Backpropagation calculates the error at the output layer and distributes it backwards through all hidden layers so each weight knows how much it contributed to the error.",
                    ),
                    ContentBlock.Quiz(
                        questionDe = "Ein Netz mit 50 Schichten ist besser als eines mit 5 Schichten. Wahr oder falsch?",
                        questionEn = "A network with 50 layers is better than one with 5 layers. True or false?",
                        options = listOf(
                            QuizOption("Wahr, mehr Schichten = mehr Intelligenz", "True, more layers = more intelligence", isCorrect = false),
                            QuizOption("Falsch, zu viele Schichten = Overfitting + Langsamer", "False, too many layers = overfitting + slower", isCorrect = true),
                            QuizOption("Wahr, aber nur mit ReLU", "True, but only with ReLU", isCorrect = false),
                            QuizOption("Falsch, 5 Schichten ist immer optimal", "False, 5 layers is always optimal", isCorrect = false),
                        ),
                        explanationDe = "Richtig! Zu tiefe Netze leiden unter Vanishing Gradient Problem (Backpropagation wird schwächer) und Overfitting. Es gibt ein Optimum — nicht zu wenig, nicht zu viele Schichten.",
                        explanationEn = "Correct! Too-deep networks suffer from vanishing gradient problem (backpropagation gets weaker) and overfitting. There is an optimum — not too few, not too many layers.",
                    ),
                    ContentBlock.RiskThermometer(),
                ),
            ),
        ),
    )
}
