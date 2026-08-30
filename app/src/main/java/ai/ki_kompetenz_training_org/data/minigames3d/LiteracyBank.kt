package ai.ki_kompetenz_training_org.data.minigames3d

/**
 * Bilingual AI-literacy statement bank covering the 9 learning domains.
 * Each statement is either a TRUE fact (isRisk=false) or a RISK/false claim
 * (isRisk=true) that the player must learn to tell apart.
 *
 * Individualization: [MasteryBankContent] uses [MasteryTracker] to weight
 * domain selection so weak areas appear more often in games.
 */
object LiteracyBank {
    val DOMAINS: List<String> = listOf(
        "Grundlagen der KI",
        "Datenschutz & DSGVO",
        "EU AI Act & Risikoklassen",
        "Haftung & Compliance",
        "KI-Governance im Unternehmen",
        "KI-Tools im Arbeitsalltag",
        "Transparenzpflichten",
        "Erlaubte & verbotene Nutzung",
        "KI im erweiterten Kontext",
    )

    private val FACTS: Map<String, List<LiteracyStatement>> = mapOf(
        "Grundlagen der KI" to listOf(
        LiteracyStatement(
            textDe = "KI-Systeme erkennen Muster in Daten; sie verstehen Sprache nicht wie Menschen.",
            textEn = "AI systems detect patterns in data; they do not understand language like humans.",
            domain = "Grundlagen der KI",
            isRisk = false,
            explanationDe = "Ein Sprachmodell berechnet Wahrscheinlichkeiten über Wörter - es hat kein eigenes Verstehen oder Bewusstsein.",
            explanationEn = "A language model computes probabilities over words - it has no understanding or consciousness of its own.",
        ),
        LiteracyStatement(
            textDe = "KI kann selbstbewusst falsch liegen - eine plausible Antwort ist kein Beweis.",
            textEn = "AI can be confidently wrong - a plausible answer is not proof.",
            domain = "Grundlagen der KI",
            isRisk = false,
            explanationDe = "Fehler ohne Warnung kennzeichnen Halluzinationen; Ergebnisse immer gegenpruefen.",
            explanationEn = "Errors without warning mark hallucinations; always cross-check results.",
        ),
        LiteracyStatement(
            textDe = "Gute Prompts fuehren zu besseren Antworten - praezise Fragen helfen der KI.",
            textEn = "Well-formed prompts lead to better answers - precise questions help the AI.",
            domain = "Grundlagen der KI",
            isRisk = false,
            explanationDe = "Kontext, Ziel und Format in der Anfrage machen die Qualitaet aus.",
            explanationEn = "Context, goal and format in the request determine quality.",
        ),
        LiteracyStatement(
            textDe = "KI ersetzt kein Fachwissen - Ergebnisse muessen fachlich eingeordnet werden.",
            textEn = "AI does not replace expertise - results must be judged in context.",
            domain = "Grundlagen der KI",
            isRisk = false,
            explanationDe = "Verantwortung bleibt beim Menschen, der das Ergebnis nutzt.",
            explanationEn = "Responsibility stays with the human who uses the result.",
        ),
        LiteracyStatement(
            textDe = "Eine zweite unabhaengige KI-Quelle kann als Gegenprobe dienen.",
            textEn = "A second independent AI source can serve as a cross-check.",
            domain = "Grundlagen der KI",
            isRisk = false,
            explanationDe = "Unabhaengige Dienste reduzieren die Gefahr gleicher Fehlerquellen.",
            explanationEn = "Independent services reduce the risk of shared error sources.",
        ),
        LiteracyStatement(
            textDe = "Ohne Kontext kann KI keine zuverlaessige Antwort geben.",
            textEn = "Without context, AI cannot give a reliable answer.",
            domain = "Grundlagen der KI",
            isRisk = false,
            explanationDe = "Je mehr relevante Vorgaben, desto besser das Ergebnis.",
            explanationEn = "The more relevant constraints, the better the result.",
        ),
        ),
        "Datenschutz & DSGVO" to listOf(
        LiteracyStatement(
            textDe = "Personendaten duerfen nur mit Rechtsgrundlage verarbeitet werden (Art. 6 DSGVO).",
            textEn = "Personal data may only be processed with a legal basis (Art. 6 GDPR).",
            domain = "Datenschutz & DSGVO",
            isRisk = false,
            explanationDe = "Der Verantwortliche braucht eine Rechtsgrundlage wie Einwilligung oder Vertrag.",
            explanationEn = "The controller needs a legal basis such as consent or contract.",
        ),
        LiteracyStatement(
            textDe = "Keine Geheimnisse und keine personenbezogenen Daten an KI-Systeme eingeben.",
            textEn = "Never enter secrets or personal data into AI systems.",
            domain = "Datenschutz & DSGVO",
            isRisk = false,
            explanationDe = "Eingaben koennen trainiert oder gespeichert werden.",
            explanationEn = "Inputs can be trained on or stored.",
        ),
        LiteracyStatement(
            textDe = "Mit generativer KI erzeugte synthetische Personendaten unterliegen weiter der DSGVO.",
            textEn = "Synthetic personal data generated with generative AI still falls under GDPR.",
            domain = "Datenschutz & DSGVO",
            isRisk = false,
            explanationDe = "Wenn Personen identifizierbar bleiben, gilt der Datenschutz.",
            explanationEn = "As long as people remain identifiable, data protection applies.",
        ),
        LiteracyStatement(
            textDe = "Das Recht auf Loeschung (Art. 17 DSGVO) gilt auch fuer KI-gestuetzte Verarbeitungen.",
            textEn = "The right to erasure (Art. 17 GDPR) also applies to AI-supported processing.",
            domain = "Datenschutz & DSGVO",
            isRisk = false,
            explanationDe = "Betroffene koennen Loeschung ihrer Daten verlangen.",
            explanationEn = "Data subjects can request deletion of their data.",
        ),
        ),
        "EU AI Act & Risikoklassen" to listOf(
        LiteracyStatement(
            textDe = "Der EU AI Act stuft KI-Systeme nach Risiko ein: unannehmbar, hoch, begrenzt, minimal.",
            textEn = "The EU AI Act classifies AI systems by risk: unacceptable, high, limited, minimal.",
            domain = "EU AI Act & Risikoklassen",
            isRisk = false,
            explanationDe = "Hochrisiko-Systeme unterliegen strengen Pflichten.",
            explanationEn = "High-risk systems face strict obligations.",
        ),
        LiteracyStatement(
            textDe = "Hochrisiko-KI braucht Risikomanagement, Datenqualitaet und menschliche Aufsicht.",
            textEn = "High-risk AI needs risk management, data quality and human oversight.",
            domain = "EU AI Act & Risikoklassen",
            isRisk = false,
            explanationDe = "Systeme mit Einfluss auf Grundrechte gelten als hochriskant.",
            explanationEn = "Systems impacting fundamental rights count as high-risk.",
        ),
        LiteracyStatement(
            textDe = "Verboten sind KI-Praktiken mit unannehmbarem Risiko, z.B. Social Scoring.",
            textEn = "Prohibited are AI practices with unacceptable risk, e.g. social scoring.",
            domain = "EU AI Act & Risikoklassen",
            isRisk = false,
            explanationDe = "Solche Praktiken verletzen Grundrechte und sind in der EU verboten.",
            explanationEn = "Such practices violate fundamental rights and are banned in the EU.",
        ),
        LiteracyStatement(
            textDe = "Transparenzpflichten gelten fuer Systeme, die mit Menschen interagieren.",
            textEn = "Transparency obligations apply to systems interacting with humans.",
            domain = "EU AI Act & Risikoklassen",
            isRisk = false,
            explanationDe = "Nutzende muessen wissen, dass sie mit KI interagieren.",
            explanationEn = "Users must know they are interacting with AI.",
        ),
        ),
        "Haftung & Compliance" to listOf(
        LiteracyStatement(
            textDe = "Der Betreiber haftet fuer Grundrechtsbeeintraechtigungen durch sein Hochrisiko-KI-System.",
            textEn = "The deployer is liable for fundamental-rights violations caused by their high-risk AI system.",
            domain = "Haftung & Compliance",
            isRisk = false,
            explanationDe = "Betreiber muessen Aufsicht, Protokolle und Risikobewertung sicherstellen.",
            explanationEn = "Deployers must ensure oversight, logs and risk assessment.",
        ),
        LiteracyStatement(
            textDe = "Bei Hochrisiko-KI haftet der Hersteller fuer sichere Systementwicklung.",
            textEn = "For high-risk AI the manufacturer is liable for safe system development.",
            domain = "Haftung & Compliance",
            isRisk = false,
            explanationDe = "Herstellerpflichten umfassen Qualitaet und Dokumentation.",
            explanationEn = "Manufacturer duties include quality and documentation.",
        ),
        LiteracyStatement(
            textDe = "Ein Unternehmen muss KI-Nutzung dokumentieren, um Compliance nachzuweisen.",
            textEn = "A company must document AI use to demonstrate compliance.",
            domain = "Haftung & Compliance",
            isRisk = false,
            explanationDe = "Nachweispflichten schuetzen vor Haftung und Bussgeldern.",
            explanationEn = "Record-keeping duties protect against liability and fines.",
        ),
        LiteracyStatement(
            textDe = "Wer KI in Entscheidungen einsetzt, bleibt fuer die Entscheidung verantwortlich.",
            textEn = "Whoever uses AI in decisions remains responsible for the decision.",
            domain = "Haftung & Compliance",
            isRisk = false,
            explanationDe = "Automatisierte Entscheidungen entlassen Menschen nicht aus der Verantwortung.",
            explanationEn = "Automated decisions do not release humans from responsibility.",
        ),
        ),
        "KI-Governance im Unternehmen" to listOf(
        LiteracyStatement(
            textDe = "KI-Governance legt Rollen, Prozesse und Kontrollen fuer KI-Einsatz fest.",
            textEn = "AI governance defines roles, processes and controls for AI use.",
            domain = "KI-Governance im Unternehmen",
            isRisk = false,
            explanationDe = "Feste Verantwortlichkeiten senken Fehlentscheidungen.",
            explanationEn = "Clear responsibilities reduce wrong decisions.",
        ),
        LiteracyStatement(
            textDe = "Ein KI-Risikoinventar hilft, alle eingesetzten Systeme zu erfassen.",
            textEn = "An AI risk inventory helps capture all deployed systems.",
            domain = "KI-Governance im Unternehmen",
            isRisk = false,
            explanationDe = "Nur was bekannt ist, kann gesteuert und geprueft werden.",
            explanationEn = "Only what is known can be managed and audited.",
        ),
        LiteracyStatement(
            textDe = "Schulungen der Mitarbeitenden sind Teil guter KI-Governance.",
            textEn = "Staff training is part of good AI governance.",
            domain = "KI-Governance im Unternehmen",
            isRisk = false,
            explanationDe = "Kompetente Nutzung senkt Fehler und Haftungsrisiken.",
            explanationEn = "Competent use reduces errors and liability risks.",
        ),
        LiteracyStatement(
            textDe = "Menschliche Aufsicht muss fuer Hochrisiko-Systeme eingerichtet sein.",
            textEn = "Human oversight must be established for high-risk systems.",
            domain = "KI-Governance im Unternehmen",
            isRisk = false,
            explanationDe = "Menschen muessen eingreifen und Entscheidungen ueberpruefen koennen.",
            explanationEn = "Humans must be able to intervene and review decisions.",
        ),
        ),
        "KI-Tools im Arbeitsalltag" to listOf(
        LiteracyStatement(
            textDe = "KI-Tools koennen Texte entwerfen, zusammenfassen und uebersetzen.",
            textEn = "AI tools can draft, summarize and translate texts.",
            domain = "KI-Tools im Arbeitsalltag",
            isRisk = false,
            explanationDe = "Sie beschleunigen Routinearbeiten, ersetzen aber keine Urteilskraft.",
            explanationEn = "They speed up routine work but do not replace judgment.",
        ),
        LiteracyStatement(
            textDe = "Ergebnisse von KI-Tools sind auf Richtigkeit zu pruefen, bevor man sie verwendet.",
            textEn = "AI tool results must be checked for correctness before use.",
            domain = "KI-Tools im Arbeitsalltag",
            isRisk = false,
            explanationDe = "Kurze Gegenpruefung verhindert Fehler im Ergebnis.",
            explanationEn = "A quick cross-check prevents errors in the output.",
        ),
        LiteracyStatement(
            textDe = "Beim Prompten hilft es, Rolle, Ziel und Format konkret zu nennen.",
            textEn = "In prompting it helps to state role, goal and format concretely.",
            domain = "KI-Tools im Arbeitsalltag",
            isRisk = false,
            explanationDe = "Spezifische Anfragen liefern spezifische Antworten.",
            explanationEn = "Specific requests yield specific answers.",
        ),
        LiteracyStatement(
            textDe = "Vertrauliche Firmendaten gehoeren nicht ungeprueft in freie KI-Dienste.",
            textEn = "Confidential company data does not belong unchecked in free AI services.",
            domain = "KI-Tools im Arbeitsalltag",
            isRisk = false,
            explanationDe = "Vorher klären, wo verarbeitet wird und unter welchem Schutz.",
            explanationEn = "Clarify first where processing happens and under what protection.",
        ),
        ),
        "Transparenzpflichten" to listOf(
        LiteracyStatement(
            textDe = "Wenn KI mit Menschen interagiert, muss dies offengelegt werden.",
            textEn = "When AI interacts with humans, this must be disclosed.",
            domain = "Transparenzpflichten",
            isRisk = false,
            explanationDe = "Taeuschung ueber die Maschine ist in der EU unzulaessig.",
            explanationEn = "Deceiving about the machine is prohibited in the EU.",
        ),
        LiteracyStatement(
            textDe = "Deepfakes muessen als manipuliert oder generiert gekennzeichnet werden.",
            textEn = "Deepfakes must be labeled as manipulated or generated.",
            domain = "Transparenzpflichten",
            isRisk = false,
            explanationDe = "Dadurch wird die Täuschung erkennbar und haftbar.",
            explanationEn = "This makes deception recognizable and actionable.",
        ),
        LiteracyStatement(
            textDe = "Bei Hochrisiko-KI müssen Betroffene ueber die Nutzung informiert werden.",
            textEn = "For high-risk AI, affected persons must be informed about the use.",
            domain = "Transparenzpflichten",
            isRisk = false,
            explanationDe = "Transparenz ist Voraussetzung fuer individuelle Rechte.",
            explanationEn = "Transparency is a precondition for individual rights.",
        ),
        LiteracyStatement(
            textDe = "Chatbots müssen klar machen, dass sie keine Menschen sind.",
            textEn = "Chatbots must make clear that they are not humans.",
            domain = "Transparenzpflichten",
            isRisk = false,
            explanationDe = "Offenlegung schafft Vertrauen und verhindert Täuschung.",
            explanationEn = "Disclosure builds trust and prevents deception.",
        ),
        ),
        "Erlaubte & verbotene Nutzung" to listOf(
        LiteracyStatement(
            textDe = "KI darf genutzt werden, wenn Rechtsgrundlage und Zweck klar sind.",
            textEn = "AI may be used when legal basis and purpose are clear.",
            domain = "Erlaubte & verbotene Nutzung",
            isRisk = false,
            explanationDe = "Erlaubte Nutzung folgt den Regeln wie jede Datenverarbeitung.",
            explanationEn = "Permitted use follows the rules like any data processing.",
        ),
        LiteracyStatement(
            textDe = "Social Scoring durch oeffentliche Stellen ist verboten.",
            textEn = "Social scoring by public authorities is prohibited.",
            domain = "Erlaubte & verbotene Nutzung",
            isRisk = false,
            explanationDe = "Diese Praxis gilt als unannehmbares Risiko.",
            explanationEn = "This practice counts as unacceptable risk.",
        ),
        LiteracyStatement(
            textDe = "KI-gestuetzte Erkennung von Emotionen am Arbeitsplatz ist weitgehend verboten.",
            textEn = "AI-based emotion recognition in the workplace is largely prohibited.",
            domain = "Erlaubte & verbotene Nutzung",
            isRisk = false,
            explanationDe = "Eingriff in Grundrechte ist hier unverhaeltnismaessig.",
            explanationEn = "Interference with fundamental rights is disproportionate here.",
        ),
        LiteracyStatement(
            textDe = "Manipulative Systeme, die schutzbeduerftige Gruppen ausnutzen, sind verboten.",
            textEn = "Manipulative systems exploiting vulnerable groups are prohibited.",
            domain = "Erlaubte & verbotene Nutzung",
            isRisk = false,
            explanationDe = "Ausbeutung von Schwaeche ist unannehmbar.",
            explanationEn = "Exploiting vulnerability is unacceptable.",
        ),
        ),
        "KI im erweiterten Kontext" to listOf(
        LiteracyStatement(
            textDe = "KI wirkt auf Gesellschaft, Arbeitsmarkt und Demokratie.",
            textEn = "AI impacts society, labor markets and democracy.",
            domain = "KI im erweiterten Kontext",
            isRisk = false,
            explanationDe = "Wechselwirkungen ueber die einzelne Anwendung hinaus beachten.",
            explanationEn = "Consider effects beyond the single application.",
        ),
        LiteracyStatement(
            textDe = "Bias in Trainingsdaten kann Diskriminierung verstärken.",
            textEn = "Bias in training data can amplify discrimination.",
            domain = "KI im erweiterten Kontext",
            isRisk = false,
            explanationDe = "Datenlage und Fairness sollten geprueft werden.",
            explanationEn = "Data quality and fairness should be reviewed.",
        ),
        LiteracyStatement(
            textDe = "Kritische Bewertung von KI-Nachrichten schuetzt vor Fehlinformation.",
            textEn = "Critical evaluation of AI news protects against misinformation.",
            domain = "KI im erweiterten Kontext",
            isRisk = false,
            explanationDe = "Quellen und Absender hinter Behauptungen pruefen.",
            explanationEn = "Check sources and senders behind claims.",
        ),
        LiteracyStatement(
            textDe = "Digitale Kompetenz umfasst heute auch den kritischen Umgang mit KI.",
            textEn = "Digital literacy today also includes critical use of AI.",
            domain = "KI im erweiterten Kontext",
            isRisk = false,
            explanationDe = "Verstehen, hinterfragen, verantwortungsvoll einsetzen.",
            explanationEn = "Understand, question, use responsibly.",
        ),
        ),
    )

    private val RISKS: Map<String, List<LiteracyStatement>> = mapOf(
        "Grundlagen der KI" to listOf(
        LiteracyStatement(
            textDe = "KI-Antworten gelten nur als Fakten, wenn sie gegen Quellen geprueft wurden.",
            textEn = "AI answers only count as facts once checked against sources.",
            domain = "Grundlagen der KI",
            isRisk = true,
            explanationDe = "Eine Antwort ist nur so gut wie ihre Belegbarkeit.",
            explanationEn = "An answer is only as good as its verifiability.",
        ),
        LiteracyStatement(
            textDe = "Jede KI-Antwort ist automatisch wahr.",
            textEn = "Every AI answer is automatically true.",
            domain = "Grundlagen der KI",
            isRisk = true,
            explanationDe = "KI irrt regelmaessig - Antworten muessen geprueft werden.",
            explanationEn = "AI errs regularly - answers must be checked.",
        ),
        ),
        "Datenschutz & DSGVO" to listOf(
        LiteracyStatement(
            textDe = "Es ist in Ordnung, Klartext-Passwoerter in einen Chatbot einzugeben.",
            textEn = "It is fine to enter plaintext passwords into a chatbot.",
            domain = "Datenschutz & DSGVO",
            isRisk = true,
            explanationDe = "Zugangsdaten gehoeren nie in KI-Eingaben.",
            explanationEn = "Credentials never belong in AI inputs.",
        ),
        LiteracyStatement(
            textDe = "Ohne Einwilligung duerfen Kundendaten bedenkenlos in jede KI geladen werden.",
            textEn = "Customer data may be loaded into any AI without consent.",
            domain = "Datenschutz & DSGVO",
            isRisk = true,
            explanationDe = "Verarbeitung ohne Rechtsgrundlage verstoesst gegen die DSGVO.",
            explanationEn = "Processing without legal basis violates GDPR.",
        ),
        ),
        "EU AI Act & Risikoklassen" to listOf(
        LiteracyStatement(
            textDe = "Der EU AI Act ist vollstaendig zu jeder Zeit fuer alle Chatbots anwendbar.",
            textEn = "The EU AI Act fully applies to all chatbots at all times.",
            domain = "EU AI Act & Risikoklassen",
            isRisk = true,
            explanationDe = "Anwendung hängt von Risikoklasse und Zeitplan der Verordnung ab.",
            explanationEn = "Application depends on risk class and the regulation's timeline.",
        ),
        LiteracyStatement(
            textDe = "Nur grosse Tech-Konzerne sind vom EU AI Act betroffen.",
            textEn = "Only big tech companies are affected by the EU AI Act.",
            domain = "EU AI Act & Risikoklassen",
            isRisk = true,
            explanationDe = "Auch Anbieter und Betreiber kleinerer Systeme sind betroffen.",
            explanationEn = "Providers and deployers of smaller systems are also affected.",
        ),
        ),
        "Haftung & Compliance" to listOf(
        LiteracyStatement(
            textDe = "Ein Chatbot uebernimmt automatisch die volle Rechtshaftung.",
            textEn = "A chatbot automatically assumes full legal liability.",
            domain = "Haftung & Compliance",
            isRisk = true,
            explanationDe = "Rechtssubjekte sind natuerliche oder juristische Personen, keine Software.",
            explanationEn = "Legal subjects are natural or legal persons, not software.",
        ),
        LiteracyStatement(
            textDe = "Ohne jede Dokumentation ist die KI-Compliance eines Betriebs leicht belegbar.",
            textEn = "Without any documentation, a company's AI compliance is easy to prove.",
            domain = "Haftung & Compliance",
            isRisk = true,
            explanationDe = "Compliance verlangt gerade Nachweise und Protokolle.",
            explanationEn = "Compliance requires exactly such evidence and logs.",
        ),
        ),
        "KI-Governance im Unternehmen" to listOf(
        LiteracyStatement(
            textDe = "KI-Governance ist nur eine IT-Frage und braucht keine Fachabteilungen.",
            textEn = "AI governance is only an IT matter and needs no business units.",
            domain = "KI-Governance im Unternehmen",
            isRisk = true,
            explanationDe = "Governance betrifft alle Bereiche, die KI einsetzen.",
            explanationEn = "Governance affects every area that uses AI.",
        ),
        LiteracyStatement(
            textDe = "Einmal festgelegte KI-Prozesse muessen nie wieder angepasst werden.",
            textEn = "Once set, AI processes never need adjustment again.",
            domain = "KI-Governance im Unternehmen",
            isRisk = true,
            explanationDe = "Risiken und Technik aendern sich - Prozesse muessen mitwachsen.",
            explanationEn = "Risks and technology change - processes must evolve.",
        ),
        ),
        "KI-Tools im Arbeitsalltag" to listOf(
        LiteracyStatement(
            textDe = "KI-Ergebnisse koennen ohne Pruefung direkt an Kunden weitergegeben werden.",
            textEn = "AI results can be passed to customers directly without checking.",
            domain = "KI-Tools im Arbeitsalltag",
            isRisk = true,
            explanationDe = "Ohne Pruefung riskierst du falsche oder irrefuehrende Inhalte.",
            explanationEn = "Without checking you risk false or misleading content.",
        ),
        LiteracyStatement(
            textDe = "Ein KI-generierter Terminplan ist immer fehlerfrei und bindend.",
            textEn = "An AI-generated schedule is always error-free and binding.",
            domain = "KI-Tools im Arbeitsalltag",
            isRisk = true,
            explanationDe = "Tools koennen Termine oder Details verwechseln.",
            explanationEn = "Tools can mix up appointments or details.",
        ),
        ),
        "Transparenzpflichten" to listOf(
        LiteracyStatement(
            textDe = "Es ist legal, Deepfakes zu erstellen, ohne sie zu kennzeichnen.",
            textEn = "It is legal to create deepfakes without labeling them.",
            domain = "Transparenzpflichten",
            isRisk = true,
            explanationDe = "Ungekennzeichnete Manipulation ist in der EU verboten.",
            explanationEn = "Unlabeled manipulation is banned in the EU.",
        ),
        LiteracyStatement(
            textDe = "Unternehmen muessen nie offenlegen, dass ein Chatbot KI ist.",
            textEn = "Companies never have to disclose that a chatbot is AI.",
            domain = "Transparenzpflichten",
            isRisk = true,
            explanationDe = "Interaktion mit KI muss offengelegt werden.",
            explanationEn = "Interaction with AI must be disclosed.",
        ),
        ),
        "Erlaubte & verbotene Nutzung" to listOf(
        LiteracyStatement(
            textDe = "Kinder-Apps duerfen beliebig manipulatives KI-Verhalten einsetzen.",
            textEn = "Kids' apps may use arbitrarily manipulative AI behavior.",
            domain = "Erlaubte & verbotene Nutzung",
            isRisk = true,
            explanationDe = "Gegenueber Kindern sind manipulative Praktiken verboten.",
            explanationEn = "Manipulative practices toward children are banned.",
        ),
        LiteracyStatement(
            textDe = "Jede KI-Nutzung ist automatisch erlaubt.",
            textEn = "Any AI use is automatically permitted.",
            domain = "Erlaubte & verbotene Nutzung",
            isRisk = true,
            explanationDe = "Nutzung unterliegt DSGVO, AI Act und Fachrecht.",
            explanationEn = "Use is subject to GDPR, AI Act and sector law.",
        ),
        ),
        "KI im erweiterten Kontext" to listOf(
        LiteracyStatement(
            textDe = "KI hat keinerlei Auswirkungen auf Medien und Meinungsbildung.",
            textEn = "AI has no impact on media and opinion formation.",
            domain = "KI im erweiterten Kontext",
            isRisk = true,
            explanationDe = "Generative KI beeinflusst Nachrichten und Wahrnehmung.",
            explanationEn = "Generative AI influences news and perception.",
        ),
        LiteracyStatement(
            textDe = "Alles, was eine KI behauptet, entspricht dem Stand der Wissenschaft.",
            textEn = "Everything an AI claims matches the state of science.",
            domain = "KI im erweiterten Kontext",
            isRisk = true,
            explanationDe = "KI spiegelt Trainingsdaten, nicht gepruefte Wahrheit.",
            explanationEn = "AI mirrors training data, not verified truth.",
        ),
        ),
    )

    fun facts(domain: String): List<LiteracyStatement> = FACTS[domain] ?: emptyList()
    fun risks(domain: String): List<LiteracyStatement> = RISKS[domain] ?: emptyList()

    /** Total statement count for tests. */
    fun totalFacts(): Int = FACTS.values.sumOf { it.size }
    fun totalRisks(): Int = RISKS.values.sumOf { it.size }
}

/**
 * Content provider that selects statements weighted by mastery.
 * Weak/never-seen domains surface more often (individualized learning).
 */
class MasteryBankContent(private val tracker: MasteryTracker?) : LiteracyContentProvider {

    override fun randomFact(rng: () -> Double): LiteracyStatement {
        val domain = pickDomain(rng)
        val pool = LiteracyBank.facts(domain)
        return pool[rng().toInt().mod(pool.size)]
    }

    override fun randomRisk(rng: () -> Double): LiteracyStatement {
        val domain = pickDomain(rng)
        val pool = LiteracyBank.risks(domain)
        return pool[rng().toInt().mod(pool.size)]
    }

    private fun pickDomain(rng: () -> Double): String =
        tracker?.selectDomain(rng) ?: LiteracyBank.DOMAINS[(rng() * LiteracyBank.DOMAINS.size).toInt()]
}