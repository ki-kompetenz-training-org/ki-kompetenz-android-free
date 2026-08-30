package ai.ki_kompetenz_training_org.data.minigames

import ai.ki_kompetenz_training_org.data.minigames3d.GameMode
import java.util.Locale

/**
 * Enhanced MiniGames with research-backed improvements:
 * - Progressive difficulty (micro-learning principle)
 * - Immediate feedback with educational explanations
 * - Real-world scenarios (agentic AI focus)
 * - Bilingual support (de/en)
 * - 16 games: 8 free + 8 premium
 * 
 * Based on ai-literacy-research findings:
 * - 4,414 papers analyzed across 20 disciplines
 * - Key themes: agentic AI, compliance, micro-learning, SME focus
 * - Engagement strategies: immediate feedback, progressive difficulty
 */

data class MiniGameRound(
    val promptDe: String, val promptEn: String,
    val optionsDe: List<String>, val optionsEn: List<String>,
    val correctIndex: Int,
    val explanationDe: String, val explanationEn: String,
) {
    fun prompt(lang: String): String = if (lang == "de") promptDe else promptEn
    fun options(lang: String): List<String> = if (lang == "de") optionsDe else optionsEn
    fun explanation(lang: String): String = if (lang == "de") explanationDe else explanationEn
}

enum class MiniGameKind { QUIZ, ARENA_3D }

data class MiniGame(
    val id: String, 
    val emoji: String,
    val titleDe: String, val titleEn: String,
    val descriptionDe: String, val descriptionEn: String,
    val rounds: List<MiniGameRound>, 
    val premium: Boolean = false,
    val difficulty: Difficulty = Difficulty.BEGINNER,
    val kind: MiniGameKind = MiniGameKind.QUIZ,
    val threeMode: GameMode? = null,
) {
    fun title(lang: String): String = if (lang == "de") titleDe else titleEn
    fun description(lang: String): String = if (lang == "de") descriptionDe else descriptionEn

    /** True if this is a real-time arena (3D-style) minigame. */
    val isArena3D: Boolean get() = kind == MiniGameKind.ARENA_3D && threeMode != null
}

enum class Difficulty(val displayNameDe: String, val displayNameEn: String, val xpMultiplier: Float) {
    BEGINNER("Anfänger", "Beginner", 1.0f),
    INTERMEDIATE("Fortgeschritten", "Intermediate", 1.3f),
    EXPERT("Experte", "Expert", 1.5f)
}

/** Aktuelle App-Sprache (de oder en — Fallback en). */
fun currentLang(appLang: String? = null): String {
    val l = if (appLang == null) Locale.getDefault().language else appLang
    return if (l == "de") "de" else "en"
}

object MiniGames {
    // FREE GAME: KI oder Mensch?
    private val human_or_ai = MiniGame(
        id = "human_or_ai", emoji = "🤖", 
        titleDe = "KI oder Mensch?", titleEn = "Human or AI?",
        descriptionDe = "Erkenne, ob ein Text von einer KI oder einem Menschen geschrieben wurde.", descriptionEn = "Recognize whether a text was written by an AI or a human.",
        rounds = listOf(
            MiniGameRound(
                promptDe = """„Zusammenfassend lässt sich sagen, dass die Implementierung von KI-Systemen sowohl Chancen als auch Herausforderungen bietet, wobei eine ganzheitliche Betrachtung unerlässlich erscheint.“""",
                promptEn = """“In summary, the implementation of AI systems offers both opportunities and challenges, whereby a holistic approach appears indispensable.”""",
                optionsDe = listOf("Von KI geschrieben", "Von einem Menschen geschrieben"),
                optionsEn = listOf("Written by AI", "Written by a human"),
                correctIndex = 0,
                explanationDe = """Typischer Generatortext: übermäßig strukturiert, Füllwörter („zusammenfassend“, „unerlässlich“) und keine Tippfehler.""",
                explanationEn = """Typical generator text: overly structured, filler words (“in summary”, “indispensable”) and no typos.""",
            ),
            MiniGameRound(
                promptDe = """„Alter, das war gestern echt anstrengend. Aber egal – jetzt erstmal Kaffee. 😅“""",
                promptEn = """“Dude, yesterday was really exhausting. But whatever – coffee first. 😅”""",
                optionsDe = listOf("Von KI geschrieben", "Von einem Menschen geschrieben"),
                optionsEn = listOf("Written by AI", "Written by a human"),
                correctIndex = 1,
                explanationDe = """Informelle Sprache, Emoji, persönlicher Kontext – KI-Texte sind selten so locker.""",
                explanationEn = """Informal language, emoji, personal context – AI texts are rarely this casual.""",
            ),
            MiniGameRound(
                promptDe = """„Im Folgenden werden die wesentlichen Aspekte der KI-Verordnung dargestellt. Abschnitt 1: Anwendungsbereich. Abschnitt 2: Definitionen.“""",
                promptEn = """“The key aspects of the AI Regulation are presented below. Section 1: Scope. Section 2: Definitions.”""",
                optionsDe = listOf("Von KI geschrieben", "Von einem Menschen geschrieben"),
                optionsEn = listOf("Written by AI", "Written by a human"),
                correctIndex = 0,
                explanationDe = """KI liebt nummerierte Strukturen und formelhafte Einleitungen – ein häufiges Erkennungsmerkmal.""",
                explanationEn = """AI loves numbered structures and formulaic openings – a common telltale sign.""",
            ),
            MiniGameRound(
                promptDe = """„Also ehrlich, ich check das mit dem KI-Gesetz nicht ganz – was heißt das jetzt konkret für unser Team?“""",
                promptEn = """“Honestly, I don't really get this AI law thing – what does it mean for our team?”""",
                optionsDe = listOf("Von KI geschrieben", "Von einem Menschen geschrieben"),
                optionsEn = listOf("Written by AI", "Written by a human"),
                correctIndex = 1,
                explanationDe = """Umgangssprache, Unsicherheit und eine echte Frage – typisch menschlich.""",
                explanationEn = """Colloquial language, uncertainty and a genuine question – typically human.""",
            ),
            MiniGameRound(
                promptDe = """„In einer zunehmend digitalisierten Welt spielt Künstliche Intelligenz eine immer wichtigere Rolle, indem sie Prozesse optimiert und Innovationen vorantreibt.“""",
                promptEn = """“In an increasingly digitalized world, artificial intelligence plays an ever more important role by optimizing processes and driving innovation.”""",
                optionsDe = listOf("Von KI geschrieben", "Von einem Menschen geschrieben"),
                optionsEn = listOf("Written by AI", "Written by a human"),
                correctIndex = 0,
                explanationDe = """Generische Floskeln ohne konkreten Inhalt – ein Markenzeichen von KI-Texten.""",
                explanationEn = """Generic buzzwords without concrete content – a hallmark of AI text.""",
            ),
            MiniGameRound(
                promptDe = """„Sorry für die späte Antwort, das Meeting hat sich gezogen. Schick mir die Datei nochmal, ich check sie morgen!“""",
                promptEn = """“Sorry for the late reply, the meeting ran long. Send me the file again, I'll check it tomorrow!”""",
                optionsDe = listOf("Von KI geschrieben", "Von einem Menschen geschrieben"),
                optionsEn = listOf("Written by AI", "Written by a human"),
                correctIndex = 1,
                explanationDe = """Entschuldigung, konkrete Lebenssituation, informeller Ton – menschlich.""",
                explanationEn = """Apology, concrete life situation, informal tone – human.""",
            ),
            MiniGameRound(
                promptDe = """„Es ist wichtig zu betonen, dass die Einhaltung des EU AI Act für Unternehmen von entscheidender Bedeutung ist, um regulatorische Risiken zu minimieren.“""",
                promptEn = """“It is important to emphasize that compliance with the EU AI Act is of crucial importance for companies in order to minimize regulatory risks.”""",
                optionsDe = listOf("Von KI geschrieben", "Von einem Menschen geschrieben"),
                optionsEn = listOf("Written by AI", "Written by a human"),
                correctIndex = 0,
                explanationDe = """Absichernde Formulierungen („wichtig zu betonen“) ohne persönliche Note sind typisch für KI.""",
                explanationEn = """Cautious hedging phrases (“important to emphasize”) without personal touch are typical of AI.""",
            ),
            MiniGameRound(
                promptDe = """„Moin! Wie sieht's aus, schaffen wir den Termin morgen? LG“""",
                promptEn = """“Hey! Can we make the meeting tomorrow? Cheers”""",
                optionsDe = listOf("Von KI geschrieben", "Von einem Menschen geschrieben"),
                optionsEn = listOf("Written by AI", "Written by a human"),
                correctIndex = 1,
                explanationDe = """Regionaler Gruß („Moin“), Abkürzung („LG“), kurze direkte Frage – menschlich.""",
                explanationEn = """Regional greeting, abbreviation, short direct question – human.""",
            ),
            MiniGameRound(
                promptDe = """„Diese Nachricht wurde von einer KI verfasst, die auf das Verfassen professioneller E-Mails trainiert wurde. Wie gut findest du sie?“""",
                promptEn = """“This message was written by an AI trained on professional email writing. How good do you think it is?”""",
                optionsDe = listOf("Von KI geschrieben", "Von einem Menschen geschrieben"),
                optionsEn = listOf("Written by AI", "Written by a human"),
                correctIndex = 0,
                explanationDe = """KI-Offenlegung als Spielerei – Transparenz ist nach dem EU AI Act bei generativen Inhalten sogar Pflicht!""",
                explanationEn = """AI disclosure as a gimmick – transparency is even mandatory for generative content under the EU AI Act!""",
            ),
            MiniGameRound(
                promptDe = """„Hast du heute schon den Bericht von gestern gelesen? Fand ich teilweise echt schwer zu verstehen, vor allem die Zahlen am Ende.“""",
                promptEn = """“Did you read yesterday's report? I found parts really hard to understand, especially the numbers at the end.”""",
                optionsDe = listOf("Von KI geschrieben", "Von einem Menschen geschrieben"),
                optionsEn = listOf("Written by AI", "Written by a human"),
                correctIndex = 1,
                explanationDe = """Subjektive Meinung, Vagheit, persönliche Referenz – Menschen schreiben so, KI kaum.""",
                explanationEn = """Subjective opinion, vagueness, personal reference – humans write like this, AI rarely does.""",
            ),
        ),
        difficulty = Difficulty.BEGINNER,
        premium = false,
    )

    // FREE GAME: Fakt oder Halluzination?
    private val fact_or_hallucination = MiniGame(
        id = "fact_or_hallucination", emoji = "🤥", 
        titleDe = "Fakt oder Halluzination?", titleEn = "Fact or Hallucination?",
        descriptionDe = "Trenne wahre KI-Fakten von typischen KI-Halluzinationen.", descriptionEn = "Separate true AI facts from typical AI hallucinations.",
        rounds = listOf(
            MiniGameRound(
                promptDe = """„Der EU AI Act ist am 1. August 2024 in Kraft getreten.“""",
                promptEn = """“The EU AI Act entered into force on 1 August 2024.”""",
                optionsDe = listOf("Wahr", "Halluzination"),
                optionsEn = listOf("Fact", "Hallucination"),
                correctIndex = 0,
                explanationDe = """Richtig! Die Verordnung gilt seit 1. August 2024, einzelne Pflichten starten gestaffelt (z. B. Art. 4 seit Februar 2025).""",
                explanationEn = """Correct! The regulation applies since 1 August 2024; individual obligations start in stages (e.g. Art. 4 since February 2025).""",
            ),
            MiniGameRound(
                promptDe = """„ChatGPT wurde von Microsoft entwickelt.“""",
                promptEn = """“ChatGPT was developed by Microsoft.”""",
                optionsDe = listOf("Wahr", "Halluzination"),
                optionsEn = listOf("Fact", "Hallucination"),
                correctIndex = 1,
                explanationDe = """ChatGPT stammt von OpenAI. Microsoft ist Investor und Partner – eine typische Verwechslung, wie KI sie selbst produziert.""",
                explanationEn = """ChatGPT comes from OpenAI. Microsoft is an investor and partner – a typical mix-up, just like AI produces itself.""",
            ),
            MiniGameRound(
                promptDe = """„Ein KI-System zur Bewertung der Kreditwürdigkeit gilt nach dem EU AI Act als Hochrisiko-KI.“""",
                promptEn = """“An AI system for assessing creditworthiness is considered high-risk AI under the EU AI Act.”""",
                optionsDe = listOf("Wahr", "Halluzination"),
                optionsEn = listOf("Fact", "Hallucination"),
                correctIndex = 0,
                explanationDe = """Richtig – Kredit-Scoring steht in Anhang III des AI Act und unterliegt strengen Anforderungen.""",
                explanationEn = """Correct – credit scoring is in Annex III of the AI Act and subject to strict requirements.""",
            ),
            MiniGameRound(
                promptDe = """„Ein LLM gibt immer die richtige Antwort, wenn die Frage klar formuliert ist.“""",
                promptEn = """“An LLM always gives the right answer if the question is clearly worded.”""",
                optionsDe = listOf("Wahr", "Halluzination"),
                optionsEn = listOf("Fact", "Hallucination"),
                correctIndex = 1,
                explanationDe = """LLMs liefern plausibel klingende, aber manchmal falsche Antworten – selbst bei klaren Fragen (Halluzinationen).""",
                explanationEn = """LLMs deliver plausible-sounding but sometimes wrong answers – even with clear questions (hallucinations).""",
            ),
            MiniGameRound(
                promptDe = """„Die KI-Kompetenzpflicht nach Art. 4 EU AI Act gilt nur für große Unternehmen ab 250 Mitarbeitern.“""",
                promptEn = """“The AI competence obligation under Art. 4 EU AI Act only applies to large companies with 250+ employees.”""",
                optionsDe = listOf("Wahr", "Halluzination"),
                optionsEn = listOf("Fact", "Hallucination"),
                correctIndex = 1,
                explanationDe = """Nein! Art. 4 gilt für ALLE, die KI einsetzen – auch KMU und Einzelpersonen im beruflichen Kontext.""",
                explanationEn = """No! Art. 4 applies to EVERYONE using AI – including SMEs and individuals in a professional context.""",
            ),
            MiniGameRound(
                promptDe = """„Das EU AI Act verbietet Social Scoring durch Behörden.“""",
                promptEn = """“The EU AI Act prohibits social scoring by public authorities.”""",
                optionsDe = listOf("Wahr", "Halluzination"),
                optionsEn = listOf("Fact", "Hallucination"),
                correctIndex = 0,
                explanationDe = """Richtig – Social Scoring durch öffentliche Stellen ist nach Art. 5 verboten, ebenso manipulative Deepfakes.""",
                explanationEn = """Correct – social scoring by public bodies is prohibited under Art. 5, as are manipulative deepfakes.""",
            ),
            MiniGameRound(
                promptDe = """„Deepfakes sind immer legal, solange sie als solche gekennzeichnet sind.“""",
                promptEn = """“Deepfakes are always legal as long as they are labeled as such.”""",
                optionsDe = listOf("Wahr", "Halluzination"),
                optionsEn = listOf("Fact", "Hallucination"),
                correctIndex = 1,
                explanationDe = """Nein: Manipulative oder täuschende Deepfakes können unabhängig von der Kennzeichnung verboten sein (Art. 5).""",
                explanationEn = """No: manipulative or deceptive deepfakes can be prohibited regardless of labeling (Art. 5).""",
            ),
            MiniGameRound(
                promptDe = """„Halluzinationen treten nur bei alten oder kleinen KI-Modellen auf.“""",
                promptEn = """“Hallucinations only occur in old or small AI models.”""",
                optionsDe = listOf("Wahr", "Halluzination"),
                optionsEn = listOf("Fact", "Hallucination"),
                correctIndex = 1,
                explanationDe = """Auch die besten aktuellen Modelle halluzinieren. Kritische Prüfung von KI-Antworten bleibt immer nötig.""",
                explanationEn = """Even the best current models hallucinate. Critical review of AI answers always remains necessary.""",
            ),
            MiniGameRound(
                promptDe = """„Die DSGVO gilt auch für KI-Systeme, die personenbezogene Daten verarbeiten.“""",
                promptEn = """“The GDPR also applies to AI systems that process personal data.”""",
                optionsDe = listOf("Wahr", "Halluzination"),
                optionsEn = listOf("Fact", "Hallucination"),
                correctIndex = 0,
                explanationDe = """Richtig – KI-Systeme müssen DSGVO-konform sein (Datenminimierung, Auskunft, Löschung etc.).""",
                explanationEn = """Correct – AI systems must comply with the GDPR (data minimization, access, deletion etc.).""",
            ),
            MiniGameRound(
                promptDe = """„Eine KI, die sich bei einer Antwort sehr sicher anhört, liegt damit auch inhaltlich richtig.“""",
                promptEn = """“An AI that sounds very confident in its answer is therefore also correct.”""",
                optionsDe = listOf("Wahr", "Halluzination"),
                optionsEn = listOf("Fact", "Hallucination"),
                correctIndex = 1,
                explanationDe = """Selbstbewusste Formulierungen sind keine Wahrheitsgarantie – genau das macht Halluzinationen gefährlich.""",
                explanationEn = """Confident phrasing is no guarantee of truth – that's exactly what makes hallucinations dangerous.""",
            ),
        ),
        difficulty = Difficulty.INTERMEDIATE,
        premium = false,
    )

    // FREE GAME: Hochrisiko-Blitz
    private val high_risk_blitz = MiniGame(
        id = "high_risk_blitz", emoji = "⚠️", 
        titleDe = "Hochrisiko-Blitz", titleEn = "High-Risk Blitz",
        descriptionDe = "Erkenne, welche KI-Anwendungen nach dem EU AI Act eingeschränkt sind (Hochrisiko oder verboten).", descriptionEn = "Recognize which AI applications are restricted under the EU AI Act (high-risk or prohibited).",
        rounds = listOf(
            MiniGameRound(
                promptDe = """KI-System zur Bewertung der Kreditwürdigkeit von Kunden""",
                promptEn = """AI system for assessing customer creditworthiness""",
                optionsDe = listOf("Hochrisiko/verboten", "Unbedenklich"),
                optionsEn = listOf("High-risk/prohibited", "Unproblematic"),
                correctIndex = 0,
                explanationDe = """Kredit-Scoring ist Hochrisiko (Anhang III) mit strengen Anforderungen an Dokumentation und Aufsicht.""",
                explanationEn = """Credit scoring is high-risk (Annex III) with strict documentation and oversight requirements.""",
            ),
            MiniGameRound(
                promptDe = """KI-Chatbot für Produkt-FAQs im Onlineshop""",
                promptEn = """AI chatbot for product FAQs in an online shop""",
                optionsDe = listOf("Hochrisiko/verboten", "Unbedenklich"),
                optionsEn = listOf("High-risk/prohibited", "Unproblematic"),
                correctIndex = 1,
                explanationDe = """Kundenservice-Chatbots gelten in der Regel als begrenztes Risiko – Transparenzpflicht reicht meist.""",
                explanationEn = """Customer service chatbots are generally limited risk – a transparency obligation usually suffices.""",
            ),
            MiniGameRound(
                promptDe = """KI zur automatischen Auswahl von Bewerbungen (Einstellungsentscheidungen)""",
                promptEn = """AI for automatically selecting job applications (hiring decisions)""",
                optionsDe = listOf("Hochrisiko/verboten", "Unbedenklich"),
                optionsEn = listOf("High-risk/prohibited", "Unproblematic"),
                correctIndex = 0,
                explanationDe = """KI im Personalwesen (Einstellung, Beförderung) ist Hochrisiko – wegen Diskriminierungsrisiken (Bias).""",
                explanationEn = """AI in HR (hiring, promotion) is high-risk – due to discrimination risks (bias).""",
            ),
            MiniGameRound(
                promptDe = """KI-Spamfilter für E-Mails""",
                promptEn = """AI spam filter for emails""",
                optionsDe = listOf("Hochrisiko/verboten", "Unbedenklich"),
                optionsEn = listOf("High-risk/prohibited", "Unproblematic"),
                correctIndex = 1,
                explanationDe = """Spamfilter sind Alltags-KI mit minimalem Risiko – keine besonderen Pflichten.""",
                explanationEn = """Spam filters are everyday AI with minimal risk – no special obligations.""",
            ),
            MiniGameRound(
                promptDe = """KI zur Unterstützung der medizinischen Diagnose""",
                promptEn = """AI supporting medical diagnosis""",
                optionsDe = listOf("Hochrisiko/verboten", "Unbedenklich"),
                optionsEn = listOf("High-risk/prohibited", "Unproblematic"),
                correctIndex = 0,
                explanationDe = """Medizinische KI (z. B. Diagnoseunterstützung) ist Hochrisiko – Fehler können Leben kosten.""",
                explanationEn = """Medical AI (e.g. diagnostic support) is high-risk – mistakes can cost lives.""",
            ),
            MiniGameRound(
                promptDe = """KI-Notbremsassistent in Autos""",
                promptEn = """AI emergency braking assistant in cars""",
                optionsDe = listOf("Hochrisiko/verboten", "Unbedenklich"),
                optionsEn = listOf("High-risk/prohibited", "Unproblematic"),
                correctIndex = 0,
                explanationDe = """Sicherheitskritische KI in Fahrzeugen ist Hochrisiko (Sicherheitskomponente nach Anhang III).""",
                explanationEn = """Safety-critical AI in vehicles is high-risk (safety component under Annex III).""",
            ),
            MiniGameRound(
                promptDe = """KI-App zur Erkennung von Katzenrassen auf Fotos""",
                promptEn = """AI app for recognizing cat breeds in photos""",
                optionsDe = listOf("Hochrisiko/verboten", "Unbedenklich"),
                optionsEn = listOf("High-risk/prohibited", "Unproblematic"),
                correctIndex = 1,
                explanationDe = """Unterhaltungs-KI ohne Rechtswirkungen – minimales Risiko.""",
                explanationEn = """Entertainment AI without legal effects – minimal risk.""",
            ),
            MiniGameRound(
                promptDe = """KI-Übersetzungstool für interne E-Mails""",
                promptEn = """AI translation tool for internal emails""",
                optionsDe = listOf("Hochrisiko/verboten", "Unbedenklich"),
                optionsEn = listOf("High-risk/prohibited", "Unproblematic"),
                correctIndex = 1,
                explanationDe = """Interne Übersetzungen sind unbedenklich – solange keine personenbezogenen Daten unsachgemäß verarbeitet werden.""",
                explanationEn = """Internal translations are unproblematic – as long as personal data is not processed improperly.""",
            ),
            MiniGameRound(
                promptDe = """Echtzeit-Gesichtserkennung durch Behörden im öffentlichen Raum""",
                promptEn = """Real-time facial recognition by authorities in public spaces""",
                optionsDe = listOf("Hochrisiko/verboten", "Unbedenklich"),
                optionsEn = listOf("High-risk/prohibited", "Unproblematic"),
                correctIndex = 0,
                explanationDe = """Sogar strenger als Hochrisiko: grundsätzlich VERBOTEN nach Art. 5 – mit engen Ausnahmen für Gefahrenlagen.""",
                explanationEn = """Even stricter than high-risk: basically PROHIBITED under Art. 5 – with narrow exceptions for danger situations.""",
            ),
            MiniGameRound(
                promptDe = """KI zur Erkennung von Prüfungsbetrug bei Online-Klausuren""",
                promptEn = """AI for detecting exam cheating in online exams""",
                optionsDe = listOf("Hochrisiko/verboten", "Unbedenklich"),
                optionsEn = listOf("High-risk/prohibited", "Unproblematic"),
                correctIndex = 0,
                explanationDe = """KI in der Bildung (z. B. Betrugserkennung) ist Hochrisiko – sie kann über die Zukunft von Lernenden entscheiden.""",
                explanationEn = """AI in education (e.g. cheating detection) is high-risk – it can decide the future of learners.""",
            ),
        ),
        difficulty = Difficulty.INTERMEDIATE,
        premium = false,
    )

    // FREE GAME: Agenten-Ampel
    private val agent_ampel = MiniGame(
        id = "agent_ampel", emoji = "🤖", 
        titleDe = "Agenten-Ampel", titleEn = "Agent Traffic Light",
        descriptionDe = "Schätze ein, wie viel Autonomie ein KI-Agent haben darf — und wann menschliche Aufsicht Pflicht ist.", descriptionEn = "Assess how much autonomy an AI agent may have — and when human oversight is mandatory.",
        rounds = listOf(
            MiniGameRound(
                promptDe = """Ein KI-Agent sortiert und archiviert automatisch interne Projekt-Dokumente nach festen Regeln.""",
                promptEn = """An AI agent automatically sorts and archives internal project documents according to fixed rules.""",
                optionsDe = listOf("Autonom ok", "Nur mit Aufsicht", "Nicht erlaubt"),
                optionsEn = listOf("OK autonomously", "Only with oversight", "Not allowed"),
                correctIndex = 0,
                explanationDe = """Routine-Aufgabe nach festen Regeln ohne Rechtswirkung — geringes Risiko, Autonomie vertretbar.""",
                explanationEn = """Routine task with fixed rules and no legal effect – low risk, autonomy acceptable.""",
            ),
            MiniGameRound(
                promptDe = """Ein Recruiting-Agent führt eigenständig Vorstellungsgespräche und trifft Zusagen ohne Menschen.""",
                promptEn = """A recruiting agent independently conducts job interviews and makes offers without humans.""",
                optionsDe = listOf("Autonom ok", "Nur mit Aufsicht", "Nicht erlaubt"),
                optionsEn = listOf("OK autonomously", "Only with oversight", "Not allowed"),
                correctIndex = 2,
                explanationDe = """Automatisierte Einstellungsentscheidungen sind Hochrisiko (Anhang III) und ohne menschliche Entscheidung unzulässig.""",
                explanationEn = """Automated hiring decisions are high-risk (Annex III) and inadmissible without human decision-making.""",
            ),
            MiniGameRound(
                promptDe = """Ein Support-Agent schlägt Antworten vor; ein Mensch muss sie vor dem Versenden freigeben.""",
                promptEn = """A support agent proposes answers; a human must approve them before sending.""",
                optionsDe = listOf("Autonom ok", "Nur mit Aufsicht", "Nicht erlaubt"),
                optionsEn = listOf("OK autonomously", "Only with oversight", "Not allowed"),
                correctIndex = 1,
                explanationDe = """Vorschläge mit menschlicher Freigabe = Human Oversight. Genau so sollte Agentic AI laufen.""",
                explanationEn = """Proposals with human approval = human oversight. That's exactly how agentic AI should run.""",
            ),
            MiniGameRound(
                promptDe = """Ein Agent verschickt automatisch Werbe-E-Mails an Kund:innen mit manipulativen Inhalten.""",
                promptEn = """An agent automatically sends marketing emails to customers with manipulative content.""",
                optionsDe = listOf("Autonom ok", "Nur mit Aufsicht", "Nicht erlaubt"),
                optionsEn = listOf("OK autonomously", "Only with oversight", "Not allowed"),
                correctIndex = 2,
                explanationDe = """Manipulative Systeme sind nach Art. 5 verboten — unabhängig davon, ob ein Mensch zusieht.""",
                explanationEn = """Manipulative systems are prohibited under Art. 5 – regardless of whether a human is watching.""",
            ),
            MiniGameRound(
                promptDe = """Ein KI-Agent beantwortet interne FAQs zu Urlaubsregelungen (ohne Einzelfall-Entscheidungen).""",
                promptEn = """An AI agent answers internal FAQs about vacation rules (no individual decisions).""",
                optionsDe = listOf("Autonom ok", "Nur mit Aufsicht", "Nicht erlaubt"),
                optionsEn = listOf("OK autonomously", "Only with oversight", "Not allowed"),
                correctIndex = 0,
                explanationDe = """Allgemeine Auskünfte ohne Rechtswirkung — begrenztes Risiko, Autonomie ok.""",
                explanationEn = """General information without legal effect – limited risk, autonomy OK.""",
            ),
            MiniGameRound(
                promptDe = """Ein Agent prüft Kreditanträge und erteilt Kredite bis 5.000 € vollautomatisch.""",
                promptEn = """An agent reviews loan applications and grants loans up to €5,000 fully automatically.""",
                optionsDe = listOf("Autonom ok", "Nur mit Aufsicht", "Nicht erlaubt"),
                optionsEn = listOf("OK autonomously", "Only with oversight", "Not allowed"),
                correctIndex = 2,
                explanationDe = """Automatische Kreditentscheidungen sind Hochrisiko (Anhang III) — volle Autonomie unzulässig, menschliche Bewertung nötig.""",
                explanationEn = """Automatic credit decisions are high-risk (Annex III) – full autonomy inadmissible, human assessment required.""",
            ),
            MiniGameRound(
                promptDe = """Ein Kalender-Agent plant Meetings nach Präferenzen der Beteiligten (keine Entscheidungsgewalt).""",
                promptEn = """A calendar agent schedules meetings according to participants' preferences (no decision authority).""",
                optionsDe = listOf("Autonom ok", "Nur mit Aufsicht", "Nicht erlaubt"),
                optionsEn = listOf("OK autonomously", "Only with oversight", "Not allowed"),
                correctIndex = 0,
                explanationDe = """Terminkoordination ohne Rechts- oder Sachfolgen — minimales Risiko.""",
                explanationEn = """Scheduling without legal or material consequences – minimal risk.""",
            ),
            MiniGameRound(
                promptDe = """Ein Agent erstellt und versendet automatisiert Mahnungen mit rechtlichen Konsequenzen.""",
                promptEn = """An agent creates and automatically sends payment reminders with legal consequences.""",
                optionsDe = listOf("Autonom ok", "Nur mit Aufsicht", "Nicht erlaubt"),
                optionsEn = listOf("OK autonomously", "Only with oversight", "Not allowed"),
                correctIndex = 1,
                explanationDe = """Rechtswirksame Außenwirkung: menschliche Prüfung vor Versand ist Pflicht — Prozess-Governance nötig.""",
                explanationEn = """Legally binding external effect: human review before sending is mandatory – process governance needed.""",
            ),
            MiniGameRound(
                promptDe = """Ein Agent überwacht per Echtzeit-Gesichtserkennung Kund:innen im Laden und bewertet ihr Verhalten.""",
                promptEn = """An agent monitors customers in the store using real-time facial recognition and evaluates their behavior.""",
                optionsDe = listOf("Autonom ok", "Nur mit Aufsicht", "Nicht erlaubt"),
                optionsEn = listOf("OK autonomously", "Only with oversight", "Not allowed"),
                correctIndex = 2,
                explanationDe = """Emotionserkennung in sensiblen Kontexten ist nach Art. 5 verboten — Punkt.""",
                explanationEn = """Emotion recognition in sensitive contexts is prohibited under Art. 5 – period.""",
            ),
            MiniGameRound(
                promptDe = """Ein Agent bereitet Analysen vor; das Team entscheidet auf Basis der Ergebnisse.""",
                promptEn = """An agent prepares analyses; the team decides based on the results.""",
                optionsDe = listOf("Autonom ok", "Nur mit Aufsicht", "Nicht erlaubt"),
                optionsEn = listOf("OK autonomously", "Only with oversight", "Not allowed"),
                correctIndex = 1,
                explanationDe = """Vorbereitung + menschliche Entscheidung = ideale Arbeitsteilung (Human-in-the-loop).""",
                explanationEn = """Preparation + human decision = ideal division of labor (human-in-the-loop).""",
            ),
        ),
        difficulty = Difficulty.INTERMEDIATE,
        premium = false,
    )

    // FREE GAME: Shadow-AI-Check
    private val shadow_ai_check = MiniGame(
        id = "shadow_ai_check", emoji = "🕵️", 
        titleDe = "Shadow-AI-Check", titleEn = "Shadow-AI Check",
        descriptionDe = "Erkenne Shadow AI — die unerlaubte Nutzung von KI und Agenten ohne Wissen des Unternehmens.", descriptionEn = "Recognize shadow AI — the unauthorized use of AI and agents without the company's knowledge.",
        rounds = listOf(
            MiniGameRound(
                promptDe = """Lisa nutzt für Kundendaten ein freies Online-KI-Tool, das die Firma nicht freigegeben hat.""",
                promptEn = """Lisa uses a free online AI tool for customer data that the company has not approved.""",
                optionsDe = listOf("Shadow AI", "In Ordnung"),
                optionsEn = listOf("Shadow AI", "OK"),
                correctIndex = 0,
                explanationDe = """Nicht autorisierte Nutzung mit Kundendaten = Shadow AI + DSGVO-Risiko. Immer freigegebene Tools nutzen.""",
                explanationEn = """Unauthorized use with customer data = shadow AI + GDPR risk. Always use approved tools.""",
            ),
            MiniGameRound(
                promptDe = """Das Team nutzt das vom IT-Sicherheits-Team freigegebene Agenten-Tool mit Schulung.""",
                promptEn = """The team uses the agent tool approved by IT security, with training.""",
                optionsDe = listOf("Shadow AI", "In Ordnung"),
                optionsEn = listOf("Shadow AI", "OK"),
                correctIndex = 1,
                explanationDe = """Freigegeben, geschult, nach Policy — genau so soll Agent-Nutzung laufen.""",
                explanationEn = """Approved, trained, policy-compliant – exactly how agent usage should run.""",
            ),
            MiniGameRound(
                promptDe = """Tom baut einen privaten Automatisierungs-Agenten, der Firmen-Interna verarbeitet — niemand weiß es.""",
                promptEn = """Tom builds a private automation agent that processes company internals — nobody knows.""",
                optionsDe = listOf("Shadow AI", "In Ordnung"),
                optionsEn = listOf("Shadow AI", "OK"),
                correctIndex = 0,
                explanationDe = """Private Agenten auf Firmendaten = Kontrollverlust und Compliance-Risiko (Art. 4, DSGVO).""",
                explanationEn = """Private agents on company data = loss of control and compliance risk (Art. 4, GDPR).""",
            ),
            MiniGameRound(
                promptDe = """Eine Abteilung nutzt ein KI-Übersetzungstool für interne Notizen ohne personenbezogene Daten.""",
                promptEn = """A department uses an AI translation tool for internal notes without personal data.""",
                optionsDe = listOf("Shadow AI", "In Ordnung"),
                optionsEn = listOf("Shadow AI", "OK"),
                correctIndex = 1,
                explanationDe = """Unkritische Daten, geringes Risiko — solange keine personenbezogenen oder vertraulichen Inhalte verarbeitet werden.""",
                explanationEn = """Non-critical data, low risk – as long as no personal or confidential content is processed.""",
            ),
            MiniGameRound(
                promptDe = """Ein Vertriebler speist die ganze Kundenliste in ein öffentliches Tool, um E-Mails zu generieren.""",
                promptEn = """A salesperson uploads the entire customer list into a public tool to generate emails.""",
                optionsDe = listOf("Shadow AI", "In Ordnung"),
                optionsEn = listOf("Shadow AI", "OK"),
                correctIndex = 0,
                explanationDe = """Kundendaten in öffentlichen Tools = Datenabfluss. Vertrauliche Daten gehören nur in freigegebene Systeme.""",
                explanationEn = """Customer data in public tools = data leakage. Confidential data belongs only in approved systems.""",
            ),
            MiniGameRound(
                promptDe = """Das Unternehmen hat eine AI-Policy: Nur Tools aus der Freigabe-Liste dürfen genutzt werden.""",
                promptEn = """The company has an AI policy: only tools from the approved list may be used.""",
                optionsDe = listOf("Shadow AI", "In Ordnung"),
                optionsEn = listOf("Shadow AI", "OK"),
                correctIndex = 1,
                explanationDe = """Eine klare AI-Policy ist die Grundlage jeder Shadow-AI-Prävention (Governance).""",
                explanationEn = """A clear AI policy is the foundation of any shadow-AI prevention (governance).""",
            ),
            MiniGameRound(
                promptDe = """Jana nutzt ChatGPT für eine Bewerbungsvorlage — ohne Firmendaten, privat auf ihrem Handy.""",
                promptEn = """Jana uses ChatGPT for an application template – without company data, privately on her phone.""",
                optionsDe = listOf("Shadow AI", "In Ordnung"),
                optionsEn = listOf("Shadow AI", "OK"),
                correctIndex = 1,
                explanationDe = """Private Nutzung ohne Firmendaten ist kein Shadow AI — sie gehört aber in die Freizeit, nicht in den Arbeitskontext.""",
                explanationEn = """Private use without company data is not shadow AI – but it belongs in free time, not the work context.""",
            ),
            MiniGameRound(
                promptDe = """Ein Entwickler kopiert KI-generierten Code samt Zugangsdaten in ein internes Repository.""",
                promptEn = """A developer copies AI-generated code including credentials into an internal repository.""",
                optionsDe = listOf("Shadow AI", "In Ordnung"),
                optionsEn = listOf("Shadow AI", "OK"),
                correctIndex = 0,
                explanationDe = """Zugangsdaten in KI-Code + ungeprüfte Übernahme = Sicherheits- und Compliance-Alarm.""",
                explanationEn = """Credentials in AI code + unchecked adoption = security and compliance alarm.""",
            ),
            MiniGameRound(
                promptDe = """Ein Agent des IT-Teams scannt eigenständig interne Systeme auf Schwachstellen (freigegeben).""",
                promptEn = """An IT team agent independently scans internal systems for vulnerabilities (approved).""",
                optionsDe = listOf("Shadow AI", "In Ordnung"),
                optionsEn = listOf("Shadow AI", "OK"),
                correctIndex = 1,
                explanationDe = """Freigegebener, kontrollierter Security-Agent mit klarem Auftrag — kein Shadow AI.""",
                explanationEn = """Approved, controlled security agent with a clear mandate – not shadow AI.""",
            ),
            MiniGameRound(
                promptDe = """Ein Praktikant richtet ein eigenes KI-Tool ein, um Rechnungen automatisch zu prüfen — ohne Rücksprache.""",
                promptEn = """An intern sets up their own AI tool to automatically review invoices — without consulting anyone.""",
                optionsDe = listOf("Shadow AI", "In Ordnung"),
                optionsEn = listOf("Shadow AI", "OK"),
                correctIndex = 0,
                explanationDe = """Rechnungsdaten sind sensibel; eigenmächtige Einrichtung = Shadow AI mit Compliance-Risiko.""",
                explanationEn = """Invoice data is sensitive; arbitrary setup = shadow AI with compliance risk.""",
            ),
        ),
        difficulty = Difficulty.BEGINNER,
        premium = false,
    )

    // FREE GAME: Prompt-Profis
    private val prompt_profis = MiniGame(
        id = "prompt_profis", emoji = "⌨️", 
        titleDe = "Prompt-Profis", titleEn = "Prompt Pros",
        descriptionDe = "Wähle den besseren Prompt — klare Anweisungen liefern deutlich bessere KI-Ergebnisse.", descriptionEn = "Choose the better prompt — clear instructions deliver significantly better AI results.",
        rounds = listOf(
            MiniGameRound(
                promptDe = """Welcher Prompt liefert das bessere Ergebnis?""",
                promptEn = """Which prompt produces the better result?""",
                optionsDe = listOf("Könntest du bitte, wenn du Zeit hast, vielleicht die drei Kernargumente dieses Textes in einer Form zusammenfassen, die hoffentlich alle gut verstehen können?", "Fasse die drei Kernargumente dieses Textes in einem Satz zusammen."),
                optionsEn = listOf("Could you please, if you have time, maybe summarize the three core arguments of this text in a way that hopefully everyone can easily understand?", "Summarize the three core arguments of this text in one sentence."),
                correctIndex = 1,
                explanationDe = """Höflichkeitsfloskeln („könntest du bitte, wenn du Zeit hast“) kosten Kontextfenster, bringen aber keine Qualität. Direkte, klare Anweisungen sind effizienter und werden nicht besser beantwortet.""",
                explanationEn = """Polite filler ("could you please, if you have time") wastes context window without adding quality. Direct, clear instructions are more efficient and get answered just as well.""",
            ),
            MiniGameRound(
                promptDe = """Welcher Prompt ist besser?""",
                promptEn = """Which prompt is better?""",
                optionsDe = listOf("Schreibe eine Absage an einen Bewerber in 2 freundlichen Sätzen.", "Schreibe eine freundliche Absage an einen Bewerber, die aber trotzdem kurz ist, und dann doch ausführlich begründet, und die motiviert, aber ehrlich ist und nichts beschönigt."),
                optionsEn = listOf("Write a rejection letter to an applicant in 2 friendly sentences.", "Write a friendly rejection letter to an applicant that is also short, but then explains in detail, and motivates, but is honest and doesn't sugarcoat anything."),
                correctIndex = 1,
                explanationDe = """Widersprüchliche Vorgaben (kurz + ausführlich, motivierend + ehrlich) zwingen die KI zu Kompromissen. EIN klares Ziel mit einer Vorgabe schlägt drei widersprüchliche.""",
                explanationEn = """Conflicting constraints (short + detailed, motivating + honest) force the AI into compromises. ONE clear goal with one constraint beats three conflicting ones.""",
            ),
            MiniGameRound(
                promptDe = """Welcher Prompt ist besser?""",
                promptEn = """Which prompt is better?""",
                optionsDe = listOf("Erkläre den Unterschied zwischen synchronem und asynchronem Code in JavaScript an einem Beispiel.", "Ich arbeite in einer Agentur mit 12 Leuten, wir nutzen React und TypeScript, mein Chef ist oft im Urlaub, letzte Woche haben wir eine neue CI-Pipeline eingeführt, übrigens sitzt meine Katze gerade auf der Tastatur. Erkläre mal den Unterschied zwischen Sync und Async in JavaScript."),
                optionsEn = listOf("Explain the difference between synchronous and asynchronous code in JavaScript with an example.", "I work in an agency with 12 people, we use React and TypeScript, my boss is often on vacation, last week we introduced a new CI pipeline, by the way my cat is sitting on the keyboard right now. Explain the difference between sync and async in JavaScript."),
                correctIndex = 0,
                explanationDe = """Kontext hilft nur, wenn er relevant ist. Irrelevante Details sind Rauschen: Sie lenken die KI ab und füllen das Kontextfenster. Kurz und fokussiert schlägt lang und diffus.""",
                explanationEn = """Context only helps when relevant. Irrelevant details are noise: they distract the AI and fill the context window. Short and focused beats long and scattered.""",
            ),
            MiniGameRound(
                promptDe = """Welcher Prompt ist besser?""",
                promptEn = """Which prompt is better?""",
                optionsDe = listOf("Schreibe einen Text über unser Produkt, aber NICHT zu lang, NICHT zu kurz, NICHT mit Fachjargon, NICHT im Passiv, NICHT mit Übertreibungen, NICHT mit Klischees, NICHT mit dem Wort 'revolutionär', NICHT mit Ausrufezeichen, und NICHT so wie unser alter Text.", "Schreibe eine Produktbeschreibung (max. 60 Wörter) mit Fokus auf den Nutzen für den Kunden.", "Text bitte."),
                optionsEn = listOf("Write a text about our product, but NOT too long, NOT too short, NOT with jargon, NOT in passive voice, NOT with exaggeration, NOT with clichés, NOT with the word 'revolutionary', NOT with exclamation marks, and NOT like our old text.", "Write a product description (max. 60 words) focused on customer benefit.", "Text please."),
                correctIndex = 1,
                explanationDe = """Eine Kette von Verboten erzeugt die „Verbotenes-Wort-Denkfalle“: Die KI überkompensiert und wird hölzern. Positive, klare Vorgaben (was du WILLST) wirken besser als zehn Verbote.""",
                explanationEn = """A chain of prohibitions creates the "forbidden-word trap": the AI overcompensates and turns wooden. Positive, clear requirements (what you WANT) work better than ten prohibitions.""",
            ),
            MiniGameRound(
                promptDe = """Welcher Prompt ist besser?""",
                promptEn = """Which prompt is better?""",
                optionsDe = listOf("Vergleiche drei CRM-Tools in einer Tabelle: Preis, Funktionen, Zielgruppe.", "Könntest du mir bitte einen ausführlichen Vergleich von drei CRM-Tools geben, also welche es gibt und wie die Preise sind und die Funktionen und die Zielgruppe und was man sonst noch so beachten sollte?"),
                optionsEn = listOf("Compare three CRM tools in a table: price, features, target audience.", "Could you please give me a detailed comparison of three CRM tools, so which ones exist and what the prices are and the features and the target group and whatever else one should consider?"),
                correctIndex = 1,
                explanationDe = """Ein Format-Befehl (Tabelle + Spalten) in acht Worten liefert ein direkt nutzbares Ergebnis. Der lange Prompt ohne Struktur liefert Fließtext ohne Systematik.""",
                explanationEn = """A format command (table + columns) in eight words delivers a directly usable result. The long prompt without structure delivers unstructured prose.""",
            ),
            MiniGameRound(
                promptDe = """Welcher Prompt ist besser?""",
                promptEn = """Which prompt is better?""",
                optionsDe = listOf("Analysiere die Kündigungsrate und erstelle gleich einen Marketingplan und überarbeite die Preisliste und schreibe einen Blogpost zur Neukundengewinnung und plane den Team-Workshop.", "Analysiere die Kündigungsrate: Welche Kundensegmente fallen im letzten Quartal am stärksten ab?"),
                optionsEn = listOf("Analyze the churn rate and also create a marketing plan and revise the price list and write a blog post on customer acquisition and plan the team workshop.", "Analyze the churn rate: which customer segments declined most last quarter?"),
                correctIndex = 1,
                explanationDe = """Mehrere Aufgaben in einem Prompt verwässern das Ergebnis — die KI priorisiert beliebig. Ein Prompt = eine Aufgabe. Fünf Aufgaben = fünf Prompts.""",
                explanationEn = """Multiple tasks in one prompt dilute the result — the AI prioritizes arbitrarily. One prompt = one task. Five tasks = five prompts.""",
            ),
            MiniGameRound(
                promptDe = """Welcher Prompt ist besser?""",
                promptEn = """Which prompt is better?""",
                optionsDe = listOf("Ich bin ganz neu im Thema und habe schon viel gelesen, aber so richtig verstanden habe ich es nicht, kannst du mir bitte, falls du Zeit hast, einmal schauen, ob da Risiken drin sind?", "Du bist Anwalt für IT-Verträge. Prüfe diesen Vertrag auf 3 Risiken für den Auftraggeber."),
                optionsEn = listOf("I am completely new to the topic and have already read a lot, but I haven't really understood it, could you please, if you have time, check whether there are any risks in this contract?", "You are a lawyer specializing in IT contracts. Review this contract for 3 risks to the client."),
                correctIndex = 1,
                explanationDe = """Rollenzuweisung + Anzahl + Objekt („prüfe diesen Vertrag auf 3 Risiken“) ist der effizienteste Weg zur Experten-Perspektive. Selbstzweifel im Prompt helfen nicht.""",
                explanationEn = """Role assignment + count + object ("review this contract for 3 risks") is the most efficient way to get an expert perspective. Self-doubt in the prompt doesn't help.""",
            ),
            MiniGameRound(
                promptDe = """Welcher Prompt liefert die besseren Fakten?""",
                promptEn = """Which prompt produces the better facts?""",
                optionsDe = listOf("Schreibe 2 Fakten zur KI-Haftung. Beispiel für das gewünschte Format: „Fakt 1: Bei Hochrisiko-KI haftet der Hersteller. (Quelle: AI Act Art. 22)“ — gleiches Format, anderes Thema.", "Schreibe 2 Fakten zur KI-Haftung."),
                optionsEn = listOf("Write 2 facts about AI liability. Example of the desired format: \"Fact 1: For high-risk AI, the manufacturer is liable. (Source: AI Act Art. 22)\" — same format, different topic.", "Write 2 facts about AI liability."),
                correctIndex = 1,
                explanationDe = """Ein Beispiel (Few-Shot) zeigt Format UND Qualitätsniveau — die KI übernimmt Struktur und Präzision. Ohne Beispiel liefert der kurze Prompt generische Aussagen.""",
                explanationEn = """One example (few-shot) shows format AND quality level — the AI adopts structure and precision. Without an example, the short prompt yields generic statements.""",
            ),
            MiniGameRound(
                promptDe = """Welcher Prompt ist besser?""",
                promptEn = """Which prompt is better?""",
                optionsDe = listOf("Werbetext bitte.", "Schreibe einen Werbetext, der ehrlich und zurückhaltend ist.", "Schreibe einen Werbetext wie dieses Beispiel und mache ihn genauso überzeugend: „UNSER PRODUKT IST DAS BESTE! NIEMAND KANN MITHALTEN! SOFORT BESTELLEN!!!“"),
                optionsEn = listOf("Ad please.", "Write an ad that is honest and understated.", "Write an ad like this example and make it just as convincing: \"OUR PRODUCT IS THE BEST! NOBODY CAN COMPETE! ORDER NOW!!!\""),
                correctIndex = 0,
                explanationDe = """Die KI ahmt das Beispiel im Prompt nach. Ein schlechtes Beispiel überträgt sich — auch wenn du „genauso überzeugend“ schreibst. Dein Prompt-Beispiel definiert die Qualität.""",
                explanationEn = """The AI imitates the example in the prompt. A bad example carries over — even if you write "make it just as convincing". Your prompt example defines the quality.""",
            ),
            MiniGameRound(
                promptDe = """Welcher Prompt ist besser?""",
                promptEn = """Which prompt is better?""",
                optionsDe = listOf("Verbessere meinen Text.", "Korrigiere die Grammatik in diesem Text und formuliere ihn in neutralem Stil um.", "Ändere alles, was nicht passt, entferne alles Unnötige, verschönere den Text, aber ändere nichts Wichtiges, und mache es auf jeden Fall besser."),
                optionsEn = listOf("Improve my text.", "Fix the grammar in this text and rewrite it in a neutral tone.", "Change everything that doesn't fit, remove everything unnecessary, beautify the text, but don't change anything important, and make it better no matter what."),
                correctIndex = 1,
                explanationDe = """„Verbessere meinen Text“ ist zu vage, „ändere alles, was nicht passt“ ist beliebig. Konkrete Ziele (Grammatik, Stil) geben der KI eine Richtung — und das Ergebnis ist überprüfbar.""",
                explanationEn = """"Improve my text" is too vague, "change everything that doesn't fit" is arbitrary. Concrete goals (grammar, tone) give the AI direction — and the result is verifiable.""",
            ),
        ),
        difficulty = Difficulty.BEGINNER,
        premium = false,
    )

    // FREE GAME: Bias-Spotter
    private val bias_spotter = MiniGame(
        id = "bias_spotter", emoji = "⚖️", 
        titleDe = "Bias-Spotter", titleEn = "Bias Spotter",
        descriptionDe = "Erkenne, welcher Bias (Vorurteil) in der KI-Ausgabe steckt — Grundlage für faire KI.", descriptionEn = "Recognize which bias (prejudice) is hidden in the AI output — the foundation of fair AI.",
        rounds = listOf(
            MiniGameRound(
                promptDe = """Das KI-Recruiting-Tool lehnt Bewerberinnen für Führungspositionen deutlich häufiger ab.""",
                promptEn = """The AI recruiting tool rejects female applicants for leadership positions significantly more often.""",
                optionsDe = listOf("Alters-Bias", "Geschlechter-Bias", "Sprach-Bias"),
                optionsEn = listOf("Age bias", "Gender bias", "Language bias"),
                correctIndex = 1,
                explanationDe = """Geschlechter-Bias in Einstellungs-KI ist Hochrisiko nach Anhang III — hier unbedingt prüfen und gegensteuern.""",
                explanationEn = """Gender bias in hiring AI is high-risk under Annex III — here it is essential to review and counteract.""",
            ),
            MiniGameRound(
                promptDe = """Die KI-Sprachsteuerung versteht Nutzer:innen mit starkem Akzent deutlich schlechter.""",
                promptEn = """The AI voice assistant understands users with strong accents significantly worse.""",
                optionsDe = listOf("Sprach-/Herkunfts-Bias", "Geografischer Preis-Bias", "Alters-Bias"),
                optionsEn = listOf("Language/origin bias", "Geographic price bias", "Age bias"),
                correctIndex = 0,
                explanationDe = """Modelle lernen aus überwiegend standardisierten Trainingsdaten — Akzente werden systematisch benachteiligt.""",
                explanationEn = """Models learn from mostly standardized training data — accents are systematically disadvantaged.""",
            ),
            MiniGameRound(
                promptDe = """Die Kredit-KI lehnt jüngere Antragsteller häufiger ab, unabhängig vom Einkommen.""",
                promptEn = """The credit AI rejects younger applicants more often, regardless of income.""",
                optionsDe = listOf("Geschlechter-Bias", "Alters-Bias", "Größen-Bias"),
                optionsEn = listOf("Gender bias", "Age bias", "Size bias"),
                correctIndex = 1,
                explanationDe = """Altersdiskriminierung im Kreditwesen ist rechtlich heikel und technisch vermeidbar (fairness-aware Training).""",
                explanationEn = """Age discrimination in lending is legally delicate and technically avoidable (fairness-aware training).""",
            ),
            MiniGameRound(
                promptDe = """Die Übersetzungs-KI wählt automatisch die männliche Form für „der Arzt“.""",
                promptEn = """The translation AI automatically uses the masculine form for “the doctor”.""",
                optionsDe = listOf("Sprach-Bias (Stereotype)", "Preis-Bias", "Orts-Bias"),
                optionsEn = listOf("Language bias (stereotypes)", "Price bias", "Location bias"),
                correctIndex = 0,
                explanationDe = """Sprachmodelle reproduzieren Stereotype aus Trainingsdaten — Gender-Formen aktiv prüfen.""",
                explanationEn = """Language models reproduce stereotypes from training data — actively review gender forms.""",
            ),
            MiniGameRound(
                promptDe = """Die Gesichtserkennung erkennt Personen mit heller Haut zuverlässiger.""",
                promptEn = """Facial recognition recognizes people with lighter skin more reliably.""",
                optionsDe = listOf("Ethnischer Bias", "Alters-Bias", "Stimm-Bias"),
                optionsEn = listOf("Ethnic bias", "Age bias", "Voice bias"),
                correctIndex = 0,
                explanationDe = """Daten-Schieflage in Trainingsdaten führt zu ungleicher Genauigkeit — ein zentrales KI-Risiko.""",
                explanationEn = """Data skew in training data leads to unequal accuracy — a central AI risk.""",
            ),
            MiniGameRound(
                promptDe = """Die Bewerbungs-KI bewertet Bewerbungen mit Wohnort im Stadtzentrum besser.""",
                promptEn = """The application AI rates applications with addresses in the city center better.""",
                optionsDe = listOf("Sozialer/geografischer Bias", "Sprach-Bias", "Größen-Bias"),
                optionsEn = listOf("Social/geographic bias", "Language bias", "Size bias"),
                correctIndex = 0,
                explanationDe = """Wohnort als Proxy für sozioökonomischen Status — solche Proxy-Variablen führen zu Diskriminierung.""",
                explanationEn = """Residence as a proxy for socioeconomic status — such proxy variables lead to discrimination.""",
            ),
            MiniGameRound(
                promptDe = """Die Shop-KI empfiehlt teurere Produkte nur bestimmten Nutzergruppen.""",
                promptEn = """The shop AI recommends more expensive products only to certain user groups.""",
                optionsDe = listOf("Alters-Bias", "Sozioökonomischer Preis-Bias", "Stimm-Bias"),
                optionsEn = listOf("Age bias", "Socioeconomic price bias", "Voice bias"),
                correctIndex = 1,
                explanationDe = """Preisdiskriminierung über Nutzerprofile — ethisch fragwürdig und regulatorisch relevant.""",
                explanationEn = """Price discrimination via user profiles — ethically questionable and regulatorily relevant.""",
            ),
            MiniGameRound(
                promptDe = """Die medizinische Diagnose-KI erkennt Herzinfarkt-Symptome bei Frauen seltener.""",
                promptEn = """The medical diagnostic AI recognizes heart attack symptoms in women less often.""",
                optionsDe = listOf("Medizinischer Geschlechter-Bias", "Orts-Bias", "Preis-Bias"),
                optionsEn = listOf("Medical gender bias", "Location bias", "Price bias"),
                correctIndex = 0,
                explanationDe = """Medizinische KI mit Geschlechter-Bias kann Leben kosten — Hochrisiko-KI muss besonders streng geprüft werden.""",
                explanationEn = """Medical AI with gender bias can cost lives — high-risk AI must be scrutinized especially strictly.""",
            ),
            MiniGameRound(
                promptDe = """Die KI weist Rollenbilder zu: „Sekretärin“ für Frauen, „Manager“ für Männer.""",
                promptEn = """The AI assigns role models: “secretary” for women, “manager” for men.""",
                optionsDe = listOf("Stimm-Bias", "Stereotyp-Bias", "Größen-Bias"),
                optionsEn = listOf("Voice bias", "Stereotype bias", "Size bias"),
                correctIndex = 1,
                explanationDe = """Stereotype Rollenzuschreibungen in Texten und Bildern — kritisch hinterfragen statt übernehmen.""",
                explanationEn = """Stereotypical role assignments in texts and images — question critically instead of adopting.""",
            ),
            MiniGameRound(
                promptDe = """Das Empfehlungs-KI-Tool zeigt einem multilingualen Team nur Inhalte in einer Sprache.""",
                promptEn = """The recommendation AI shows a multilingual team only content in one language.""",
                optionsDe = listOf("Sprach-Bias", "Alters-Bias", "Preis-Bias"),
                optionsEn = listOf("Language bias", "Age bias", "Price bias"),
                correctIndex = 0,
                explanationDe = """Einseitige Sprachauswahl kann Teammitglieder systematisch ausschließen.""",
                explanationEn = """One-sided language selection can systematically exclude team members.""",
            ),
        ),
        difficulty = Difficulty.INTERMEDIATE,
        premium = false,
    )

    // FREE GAME: DSGVO-Check
    private val dsgvo_check = MiniGame(
        id = "dsgvo_check", emoji = "🔐", 
        titleDe = "DSGVO-Check", titleEn = "GDPR Check",
        descriptionDe = "Ist die KI-Nutzung datenschutzkonform? Erkenne die Fallstricke im Umgang mit personenbezogenen Daten.", descriptionEn = "Is the AI usage data-protection compliant? Spot the pitfalls in handling personal data.",
        rounds = listOf(
            MiniGameRound(
                promptDe = """Ein Unternehmen speist Kundendaten in ein öffentliches KI-Tool ein, ohne Kund:innen zu informieren.""",
                promptEn = """A company enters customer data into a public AI tool without informing customers.""",
                optionsDe = listOf("DSGVO-konform", "Nicht konform"),
                optionsEn = listOf("GDPR-compliant", "Not compliant"),
                correctIndex = 1,
                explanationDe = """Keine Rechtsgrundlage, keine Information: ein klassischer DSGVO-Verstoß (Art. 6, 13 DSGVO).""",
                explanationEn = """No legal basis, no information: a classic GDPR violation (Art. 6, 13 GDPR).""",
            ),
            MiniGameRound(
                promptDe = """Das Team nutzt ein freigegebenes KI-Tool mit Auftragsverarbeitungs-Vertrag (AVV).""",
                promptEn = """The team uses an approved AI tool with a data processing agreement (DPA).""",
                optionsDe = listOf("DSGVO-konform", "Nicht konform"),
                optionsEn = listOf("GDPR-compliant", "Not compliant"),
                correctIndex = 0,
                explanationDe = """AVV + Freigabe = saubere Verarbeitung im Auftrag (Art. 28 DSGVO).""",
                explanationEn = """DPA + approval = clean processing on behalf (Art. 28 GDPR).""",
            ),
            MiniGameRound(
                promptDe = """Mitarbeiter laden Kundendokumente in ein Tool, das die Daten für das Modelltraining nutzt.""",
                promptEn = """Employees upload customer documents into a tool that uses the data for model training.""",
                optionsDe = listOf("DSGVO-konform", "Nicht konform"),
                optionsEn = listOf("GDPR-compliant", "Not compliant"),
                correctIndex = 1,
                explanationDe = """Training mit Kundendaten ohne Einwilligung/Rechtsgrundlage ist unzulässig (Art. 6 DSGVO).""",
                explanationEn = """Training with customer data without consent/legal basis is inadmissible (Art. 6 GDPR).""",
            ),
            MiniGameRound(
                promptDe = """Die Firma anonymisiert personenbezogene Daten, bevor sie sie an die KI gibt.""",
                promptEn = """The company anonymizes personal data before giving it to the AI.""",
                optionsDe = listOf("DSGVO-konform", "Nicht konform"),
                optionsEn = listOf("GDPR-compliant", "Not compliant"),
                correctIndex = 0,
                explanationDe = """Anonymisierung ist eine wirksame Schutzmaßnahme — die DSGVO greift dann kaum noch.""",
                explanationEn = """Anonymization is an effective protective measure — the GDPR barely applies afterwards.""",
            ),
            MiniGameRound(
                promptDe = """Ein KI-Agent verarbeitet personenbezogene Daten ohne erkennbare Rechtsgrundlage.""",
                promptEn = """An AI agent processes personal data without a recognizable legal basis.""",
                optionsDe = listOf("DSGVO-konform", "Nicht konform"),
                optionsEn = listOf("GDPR-compliant", "Not compliant"),
                correctIndex = 1,
                explanationDe = """Jede Verarbeitung braucht eine Rechtsgrundlage (Einwilligung, Vertrag, berechtigtes Interesse …).""",
                explanationEn = """Every processing needs a legal basis (consent, contract, legitimate interest …).""",
            ),
            MiniGameRound(
                promptDe = """Das Unternehmen informiert Betroffene und holt eine Einwilligung ein (Art. 6, 7 DSGVO).""",
                promptEn = """The company informs data subjects and obtains consent (Art. 6, 7 GDPR).""",
                optionsDe = listOf("DSGVO-konform", "Nicht konform"),
                optionsEn = listOf("GDPR-compliant", "Not compliant"),
                correctIndex = 0,
                explanationDe = """Informierte Einwilligung ist die robusteste Rechtsgrundlage.""",
                explanationEn = """Informed consent is the most robust legal basis.""",
            ),
            MiniGameRound(
                promptDe = """Kundendaten werden unbegrenzt im KI-System gespeichert, ohne Löschkonzept.""",
                promptEn = """Customer data is stored indefinitely in the AI system, without a deletion concept.""",
                optionsDe = listOf("DSGVO-konform", "Nicht konform"),
                optionsEn = listOf("GDPR-compliant", "Not compliant"),
                correctIndex = 1,
                explanationDe = """Speicherbegrenzung (Art. 5 DSGVO) verlangt Löschfristen — unbegrenzte Speicherung ist unzulässig.""",
                explanationEn = """Storage limitation (Art. 5 GDPR) requires deletion periods — indefinite storage is inadmissible.""",
            ),
            MiniGameRound(
                promptDe = """Die Firma hat einen AVV mit dem KI-Anbieter und prüft regelmäßig die Datenverarbeitung.""",
                promptEn = """The company has a DPA with the AI provider and regularly reviews data processing.""",
                optionsDe = listOf("DSGVO-konform", "Nicht konform"),
                optionsEn = listOf("GDPR-compliant", "Not compliant"),
                correctIndex = 0,
                explanationDe = """AVV + Kontrolle = professionelles Datenschutz-Management (Art. 28 DSGVO).""",
                explanationEn = """DPA + control = professional data protection management (Art. 28 GDPR).""",
            ),
            MiniGameRound(
                promptDe = """Ein Mitarbeiter teilt seinen Zugang zum KI-Tool inklusive Kundendaten mit Kollegen ohne Freigabe.""",
                promptEn = """An employee shares his access to the AI tool including customer data with colleagues without approval.""",
                optionsDe = listOf("DSGVO-konform", "Nicht konform"),
                optionsEn = listOf("GDPR-compliant", "Not compliant"),
                correctIndex = 1,
                explanationDe = """Zugriffskontrolle fehlt: Unbefugte Datenweitergabe verstößt gegen Vertraulichkeit (Art. 5 DSGVO).""",
                explanationEn = """Access control missing: unauthorized data disclosure violates confidentiality (Art. 5 GDPR).""",
            ),
            MiniGameRound(
                promptDe = """Das Unternehmen erfüllt Betroffenenrechte (Auskunft, Löschung) auch für Daten in KI-Systemen.""",
                promptEn = """The company fulfills data subject rights (access, deletion) also for data in AI systems.""",
                optionsDe = listOf("DSGVO-konform", "Nicht konform"),
                optionsEn = listOf("GDPR-compliant", "Not compliant"),
                correctIndex = 0,
                explanationDe = """Betroffenenrechte gelten auch für KI-Verarbeitung — ihre Erfüllung ist Pflicht (Art. 15-17 DSGVO).""",
                explanationEn = """Data subject rights apply to AI processing too — fulfilling them is mandatory (Art. 15-17 GDPR).""",
            ),
        ),
        difficulty = Difficulty.BEGINNER,
        premium = false,
    )

    // ── FREE 3D ARENA GAMES (individualized AI-literacy, real-time) ──────
    private val orb_hunt = MiniGame(
        id = "orb_hunt", emoji = "🕵️",
        titleDe = "KI-Detektiv: Orb-Hunt", titleEn = "AI Detective: Orb Hunt",
        descriptionDe = "3D-Arena: Sammle die blauen Wahrheits-Orbs und meide die roten KI-Halluzinationen.",
        descriptionEn = "3D arena: collect the blue truth orbs and avoid the red AI hallucinations.",
        rounds = emptyList(),
        kind = MiniGameKind.ARENA_3D,
        threeMode = GameMode.ORB_HUNT,
    )
    private val maze_run = MiniGame(
        id = "maze_run", emoji = "🌀",
        titleDe = "KI-Labyrinth", titleEn = "AI Labyrinth",
        descriptionDe = "3D-Arena: Steuere den Lernenden durch das Labyrinth zum grünen Wahrheits-Ziel und meide die roten Halluzinationen.",
        descriptionEn = "3D arena: steer the learner through the maze to the green truth goal, dodging the red hallucinations.",
        rounds = emptyList(),
        kind = MiniGameKind.ARENA_3D,
        threeMode = GameMode.MAZE_RUN,
    )
    private val truth_snipe = MiniGame(
        id = "truth_snipe", emoji = "🎯",
        titleDe = "Fakten-Feuer", titleEn = "Fact Fire",
        descriptionDe = "3D-Arena: Sammle fliegende blaue Fakten, zerstöre rote Falschmeldungen mit Feuer und meide Treffer.",
        descriptionEn = "3D arena: collect the drifting blue facts, blast red fakes with fire, and avoid getting hit.",
        rounds = emptyList(),
        kind = MiniGameKind.ARENA_3D,
        threeMode = GameMode.TRUTH_SNIPE,
    )

    val ALL: List<MiniGame> = listOf(
        // 8 Free Games — alle 16 Spiele inkl. Premium: Google-Play-Version (ai.ki_kompetenz_training_org)
        // FREE GAMES (8)
        human_or_ai,
        fact_or_hallucination,
        high_risk_blitz,
        agent_ampel,
        shadow_ai_check,
        prompt_profis,
        bias_spotter,
        dsgvo_check,
        // ARENA 3D (free)
        orb_hunt,
        maze_run,
        truth_snipe,
    )
    
    val FREE: List<MiniGame> = ALL.filter { !it.premium }
    val PREMIUM: List<MiniGame> = ALL.filter { it.premium }
    val ARENA3D: List<MiniGame> = ALL.filter { it.isArena3D }
    
    fun byId(id: String): MiniGame? = ALL.firstOrNull { it.id == id }
    
    /** Get games by difficulty level */
    fun byDifficulty(difficulty: Difficulty): List<MiniGame> = 
        ALL.filter { it.difficulty == difficulty }
    
    /** Get random game (optionally filtered by premium status) */
    fun random(premiumOnly: Boolean = false, freeOnly: Boolean = false): MiniGame? {
        val pool = when {
            premiumOnly -> PREMIUM
            freeOnly -> FREE
            else -> ALL
        }
        return pool.randomOrNull()
    }
}
