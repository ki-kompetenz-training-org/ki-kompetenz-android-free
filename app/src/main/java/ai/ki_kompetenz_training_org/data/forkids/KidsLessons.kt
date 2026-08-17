package ai.ki_kompetenz_training_org.data.forkids

/**
 * COPPA-compliant kid-friendly AI lesson.
 * ALL content is local. No server calls. No PII collection. No tracking.
 *
 * COPPA (Children's Online Privacy Protection Act) compliance:
 * - No personal information collected
 * - No server communication
 * - No analytics or tracking
 * - No advertising
 * - No social features
 * - No external links without parental gate
 * - All data stored locally on device only
 */
data class KidsLesson(
    val id: String,
    val emoji: String,
    val title: String,
    val description: String,
    val color: Long,             // ARGB color for the card theme
    val sections: List<KidsSection>,
)

data class KidsSection(
    val title: String,
    val emoji: String,
    val content: String,         // markdown-like simple text with emojis
    val funFact: String,         // interesting related fact
    val quiz: KidsQuiz? = null,   // optional quiz at end of lesson
)

data class KidsQuiz(
    val question: String,
    val options: List<String>,    // always exactly 2 options (simple for kids)
    val correctIndex: Int,
    val correctEmoji: String = "✅",
    val wrongEmoji: String = "😅",
    val explanation: String,
    val encouragement: String,       // positive feedback for both answers
)

/**
 * COPPA Notice — required to be shown when ForKids section is opened.
 * Documents that the app does NOT collect personal information from children
 * and complies with COPPA (16 CFR Part 312).
 */
object CoppaNotice {
    const val HEADING = "📖 Für Eltern — Datenschutz-Info"
    const val BODY = "KI-Kompetenz ForKids sammelt KEINE persönlichen Daten von Kindern.\n\n" +
        "• Keine Anmeldung, keine E-Mail, kein Name\n" +
        "• Kein Tracking, keine Werbung\n" +
        "• Keine Daten werden an Server gesendet\n" +
        "• Alle Fortschritte sind NUR auf diesem Gerät\n" +
        "• Eltern können die App-Daten jederzeit löschen\n\n" +
        "Diese App entspricht dem US-amerikanischen COPPA\n" +
        "(Children's Online Privacy Protection Act) und der\n" +
        "EU DSGVO für Kinder."
    const val DELETION_HINT = "Tipp: App-Daten löschen → Einstellungen → Apps → KI-Kompetenz → Speicher löschen"
    const val PARENT_GATE_PIN = "1234"  // Simple 4-digit PIN for parental gates
}

object KidsLessons {

    // ── Lektion 1: Was ist KI überhaupt? ───────────────────────────────
    private val lesson1 = KidsLesson(
        id = "kids_01",
        emoji = "🤖",
        title = "Was ist KI?",
        description = "Lerne, was KI ist — einfach erklärt!",
        color = 0xFFFFEB3B.toLong(),
        sections = listOf(
            KidsSection(
                title = "KI ist wie ein schlauer Roboter 🤖",
                emoji = "🤖",
                content = "Stell dir vor, du hast einen Roboter-Helper.\n\n" +
                    "• Er kann Fragen beantworten\n" +
                    "• Er kann Bilder erkennen\n" +
                    "• Er kann Texte schreiben\n" +
                    "• Er kann aus Beispielen lernen\n\n" +
                    "\"KI\" bedeutet: Künstliche Intelligenz —\n" +
                    "ein Computer, der ähnlich denkt wie ein Mensch!",
                funFact = "💡 Siri und Alexa sind auch KI!",
                quiz = KidsQuiz(
                    question = "Was bedeutet \"KI\"?",
                    options = listOf("Künstliche Intelligenz", "Killer Igel"),
                    correctIndex = 0,
                    explanation = "KI steht für Künstliche Intelligenz —\nein Computer, der lernen kann!",
                    encouragement = "Super! Du weißt schon, was KI bedeutet! 🌟"
                )
            )
        )
    )

    // ── Lektion 2: KI oder Mensch? ───────────────────────────────────
    private val lesson2 = KidsLesson(
        id = "kids_02",
        emoji = "🕵️",
        title = "KI oder Mensch?",
        description = "Kannst du erkennen, ob ein Mensch oder eine KI\netwas gemacht hat?",
        color = 0xFFE0F2FE.toLong(),
        sections = listOf(
            KidsSection(
                title = "Woran erkennst du KI? 🤔",
                emoji = "🤔",
                content = "KI-Texte sehen oft sehr \"perfekt\" aus:\n\n" +
                    "🚩 Keine Tippfehler\n" +
                    "🚩 Sehr lange, komplizierte Sätze\n" +
                    "🚩 Keine persönlichen Erlebnisse\n" +
                    "🚩 Immer \"zusammenfassend\" am Anfang\n\n" +
                    "Menschen schreiben oft lockerer und machen Fehler —\n" +
                    "und das ist total okay!",
                funFact = "💡 Seit August 2024 müssen KI-Systeme\nin der EU kennzeichnen, dass sie KI sind!",
                quiz = KidsQuiz(
                    question = "\"Zusammenfassend lässt sich sagen, dass die\nImplementation betrachtenswert erscheint.\"\n\nWer hat das geschrieben?",
                    options = listOf("Eine KI 🤖", "Ein Mensch 👤"),
                    correctIndex = 0,
                    explanation = "Das klingt wie ein KI-Text:\n\"Zusammenfassend\", \"gewährleistet\", keine Fehler.",
                    encouragement = "Gut erkannt! Du bist ein echter KI-Detektiv! 🕵️"
                )
            )
        )
    )

    // ── Lektion 3: Gefahren von KI ───────────────────────────────────
    private val lesson3 = KidsLesson(
        id = "kids_03",
        emoji = "⚠️",
        title = "KI kann auch gefährlich sein",
        description = "Nicht alles mit KI ist gut — lerne,\nworauf du aufpassen musst!",
        color = 0xFFFFCCCB.toLong(),
        sections = listOf(
            KidsSection(
                title = "Nicht alles online ist wahr! 🔍",
                emoji = "🔍",
                content = "Wichtig für Kids:\n\n" +
                    "• 🚫 Glaube nicht alles, was du online siehst\n" +
                    "• 🔍 Prüfe Informationen mit 2+ Quellen\n" +
                    "• 🧠 Frag Erwachsene, wenn du dir unsicher bist\n" +
                    "• 🚫 Klicke nie auf verdächtige Links\n" +
                    "• 🔒 Teile NIE persönliche Daten online\n\n" +
                    "Du bist schlau genug, um aufzupassen!",
                funFact = "💡 Frag dich: \"Kann das auch ein\nfalscher Artikel sein?\""
            ),
            KidsSection(
                title = "Quiz: Was ist richtig?",
                emoji = "❓",
                content = "",
                funFact = "",
                quiz = KidsQuiz(
                    question = "Was solltest du tun, wenn\nKI dir eine falsche Info gibt?",
                    options = listOf("Alles glauben, KI macht keine Fehler", "Erwachsene fragen und Quellen prüfen"),
                    correctIndex = 1,
                    explanation = "KI kann sich irren — immer\nErwachsene fragen und Quellen prüfen!",
                    encouragement = "Genau! Du bist klug und kritisch! 🧠"
                )
            )
        )
    )

    // ── Lektion 4: KI hilft dir in der Schule ───────────────────────────
    private val lesson4 = KidsLesson(
        id = "kids_04",
        emoji = "📚",
        title = "KI als Helfer",
        description = "KI kann dir beim Lernen helfen —\naber sie ersetzt nicht dein Gehirn!",
        color = 0xFFC3EEDC.toLong(),
        sections = listOf(
            KidsSection(
                title = "So nutzt du KI RICHTIG 🎯",
                emoji = "✅",
                content = "🟢 KI fragen: \"Erkläre mir X wie\n   einem 10-Jährigen\"\n" +
                    "🟢 Verstehen, dann selbst aufschreiben\n" +
                    "🟢 KI-Ergebnis mit deinem Buch vergleichen\n" +
                    "🟢 Bei Problemen KI um Tipps bitten\n" +
                    "🟢 Zuerst SELBST denken, dann KI fragen",
                funFact = "💡 Wer zuerst selbst denkt und\nnach KI fragt, lernt am meisten!",
                quiz = KidsQuiz(
                    question = "Du nutzt KI, um eine Aufgabe\nverständlich zu machen. Was machst du als Erstes?",
                    options = listOf("Kopiere die KI-Antwort", "Verstehe die KI-Erklärung und schreibe selbst"),
                    correctIndex = 1,
                    explanation = "Kopieren bringt nichts — verstehen\nund selbst schreiben ist echtes Lernen!",
                    encouragement = "Genau! Du nutzt KI als Helfer,\nnicht als Cheater! 💪"
                )
            )
        )
    )

    // ── Lektion 5: Du bist schon ein KI-Experte! ──────────────────────
    private val lesson5 = KidsLesson(
        id = "kids_05",
        emoji = "🌟",
        title = "Du bist schon ein KI-Experte!",
        description = "Du weißt jetzt, worauf du beim\nUmgang mit KI achten musst!",
        color = 0xFFBBDEFB.toLong(),
        sections = listOf(
            KidsSection(
                title = "Dein KI-Wissensstand 🏆",
                emoji = "🏆",
                content = "Du hast jetzt gelernt:\n\n" +
                    "🤖 Was KI ist und wie sie funktioniert\n" +
                    "🕵️ Wie man KI-Texte erkennt\n" +
                    "⚠️ Dass KI Halluzinationen hat\n" +
                    "🛡️ Wie man sich online schützt\n" +
                    "📚 Wie man KI richtig als Helfer nutzt\n\n" +
                    "Du bist besser vorbereitet als viele\n" +
                    "Erwachsene! 🎉",
                funFact = "💡 Teile dein Wissen mit der\nFamilie — sie werden beeindruckt sein!"
            ),
            KidsSection(
                title = "Quiz: Abschlusstest",
                emoji = "🎯",
                content = "",
                funFact = "",
                quiz = KidsQuiz(
                    question = "Warum solltest du KI-Ergebnisse\nimmer von Erwachsenen überprüfen lassen?",
                    options = listOf("Weil ich KI-Ergebnisse blind vertrauen kann", "Weil KI sich irren und halluzinieren kann"),
                    correctIndex = 1,
                    explanation = "KI kann sehr überzeugend klingen\nund trotzdem komplett falsch sein!",
                    encouragement = "🎉 Du hast alle 5 Lektionen geschafft!\nDu bist jetzt ein KI-Sicherheitsexperte! 🏆"
                )
            )
        )
    )

    val all: List<KidsLesson> = listOf(lesson1, lesson2, lesson3, lesson4, lesson5)
}
