package ai.ki_kompetenz_training_org.data.forseniors

/**
 * Senior-friendly AI literacy lessons.
 * ALL content is local. No server calls. No PII collection. No tracking.
 *
 * COPPA / DSGVO compliance:
 * - No personal information collected
 * - No server communication
 * - No analytics or tracking
 * - All data stored locally on device only
 */
data class SeniorsLesson(
    val id: String,
    val emoji: String,
    val title: String,
    val description: String,
    val color: Long,
    val sections: List<SeniorsSection>,
)

data class SeniorsSection(
    val title: String,
    val emoji: String,
    val content: String,
    val keyTakeaway: String,
    val quiz: SeniorsQuiz? = null,
)

data class SeniorsQuiz(
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String,
)

object SeniorsLessons {

    // ── 1. Passwörter & Sicherheit ───────────────────────────────────
    private val lesson1 = SeniorsLesson(
        id = "seniors_01",
        emoji = "🔒",
        title = "Passwörter & Sicherheit",
        description = "Starke Passwörter erstellen und Zwei-Faktor-Authentifizierung nutzen",
        color = 0xFF4CAF50.toLong(),
        sections = listOf(
            SeniorsSection(
                title = "Ein gutes Passwort ist wie ein gutes Türschloss",
                emoji = "🔐",
                content = "So erstellen Sie ein starkes Passwort:\n\n" +
                    "• Mindestens 12 Zeichen — je länger, desto besser\n" +
                    "• Vier verschiedene Wörter kombinieren, z.B.\n  Birne-Berg-Buch-Maus\n" +
                    "• Niemals wiederholen — jedes Konto braucht sein eigenes\n" +
                    "• Nicht auf Zettel am Bildschirm aufschreiben",
                keyTakeaway = "Merksatz: Vier Wörter mit Bindestrich — das merkt man sich, und niemand errät es.\nBeispiel: Kirche-Garten-Kaffee-Donner",
                quiz = SeniorsQuiz(
                    question = "Was ist ein starkes Passwort?",
                    options = listOf("123456", "Kirche-Garten-Kaffee-Donner", "Mein Name", "Passwort"),
                    correctIndex = 1,
                    explanation = "Vier alltägliche Wörter mit Bindestrich — leicht zu merken und schwer zu erraten!",
                )
            ),
            SeniorsSection(
                title = "Zwei-Faktor-Authentifizierung (2FA)",
                emoji = "📱",
                content = "2FA bedeutet: Zwei Schlösser statt einem.\n\n" +
                    "Beim Einloggen kommt zusätzlich ein Code per\nSMS oder per App auf Ihr Handy.\n\n" +
                    "Selbst wenn jemand Ihr Passwort stiehlt, kommt\ner nicht hinein — ihm fehlt der zweite Code.",
                keyTakeaway = "Merksatz: Passwort plus Code — doppelt sicher.\nSchalten Sie 2FA überall ein, wo es angeboten wird (Bank, E-Mail, Online-Konten).",
            )
        )
    )

    // ── 2. Phishing erkennen ────────────────────────────────────────
    private val lesson2 = SeniorsLesson(
        id = "seniors_02",
        emoji = "🎣",
        title = "Phishing erkennen",
        description = "Betrugsversuche im Internet erkennen und abwehren",
        color = 0xFFFF9800.toLong(),
        sections = listOf(
            SeniorsSection(
                title = "Was ist Phishing?",
                emoji = "⚠️",
                content = "Phishing ist Betrug mit gefälschten Nachrichten.\n" +
                    "Betrüger geben sich als Bank, Behörde oder Bekannte aus,\num an Ihre Daten zu kommen.\n\n" +
                    "Woran Sie Phishing erkennen:\n" +
                    "• Dringlichkeit: Ihr Konto wird gesperrt!\n" +
                    "• Geld oder Daten werden angefordert\n" +
                    "• Unbekannter Absender oder veränderte Adresse\n" +
                    "• Rechtschreibfehler und merkwürdige Formulierungen",
                keyTakeaway = "Die 3-Phishing-Regel:\n1. Nicht klicken\n2. Nicht antworten\n3. Immer bei der echten Bank/Behörde anrufen",
                quiz = SeniorsQuiz(
                    question = "Sie erhalten eine E-Mail: Konto gesperrt! Bestätigen Sie Ihre Daten jetzt! Was tun Sie?",
                    options = listOf("Auf den Link klicken und Daten eingeben", "E-Mail löschen und bei der Bank selbst anrufen", "Antworten mit den geforderten Daten", "Den Link an Freunde weiterleiten"),
                    correctIndex = 1,
                    explanation = "Keine Bank fordert je Zugangsdaten per E-Mail an. Löschen Sie die E-Mail und rufen Sie selbst bei der Bank an!",
                )
            )
        )
    )

    // ── 3. Sicher online einkaufen ────────────────────────────────────
    private val lesson3 = SeniorsLesson(
        id = "seniors_03",
        emoji = "🛒",
        title = "Sicher online einkaufen",
        description = "Online-Shopping sicher und ohne Risiko nutzen",
        color = 0xFF2196F3.toLong(),
        sections = listOf(
            SeniorsSection(
                title = "Sicher einkaufen im Internet",
                emoji = "🛍️",
                content = "Online-Shopping ist bequem — mit diesen Regeln\nbleibt es sicher:\n\n" +
                    "• Nur bei bekannten Händlern kaufen\n" +
                    "• Mit Kreditkarte oder PayPal (Käuferschutz) bezahlen\n" +
                    "• Impressum prüfen — Name und Adresse müssen da sein\n" +
                    "• Achtung bei Riesenrabatten — 70% auf alles ist oft ein Lockangebot\n" +
                    "• Adresse muss mit https:// und Schloss-Symbol beginnen",
                keyTakeaway = "Merksatz: Zahlung mit Schutz = Geld mit Sicherheit.\nBezahlen Sie online nur mit Käuferschutz.",
                quiz = SeniorsQuiz(
                    question = "Woran erkennen Sie einen seriösen Online-Shop?",
                    options = listOf("Große Rabatte (70% auf alles)", "Vorhandenes Impressum mit Name und Adresse", "Nur Überweisung als Zahlungsmethode", "Keine AGB"),
                    correctIndex = 1,
                    explanation = "Ein seriöser Händler hat immer ein Impressum mit vollständigen Angaben!",
                )
            )
        )
    )

    // ── 4. Telefon & KI ──────────────────────────────────────────────
    private val lesson4 = SeniorsLesson(
        id = "seniors_04",
        emoji = "📞",
        title = "KI-Telefone erkennen",
        description = "KI kann Stimmen nachahmen — so schützen Sie sich",
        color = 0xFF9C27B0.toLong(),
        sections = listOf(
            SeniorsSection(
                title = "KI kann Stimmen fälschen",
                emoji = "🎙️",
                content = "KI kann heute Stimmen nachahmen — auch Ihre\nStimme oder die Ihrer Kinder und Enkelkinder.\n\n" +
                    "Woran Sie ein KI-Telefon erkennen:\n" +
                    "• Der Anrufer klingt leicht mechanisch oder wiederholt sich\n" +
                    "• Er fragt nach Geld, Daten oder Codes\n" +
                    "• Die Stimme eines Verwandten bittet dringend um Geld",
                keyTakeaway = "Merksatz: Unbekanntes Telefon? Auflegen und selbst zurückrufen — unter einer Nummer, die Sie kennen.\nKI-Stimmen klingen inzwischen sehr echt — vertrauen Sie nie einer Stimme am Telefon, wenn sie Geld verlangt.",
                quiz = SeniorsQuiz(
                    question = "Ihre Enkeltochter ruft an und braucht dringend Geld für einen Unfall im Ausland. Was tun Sie?",
                    options = listOf("Sofort Geld überweisen", "Auflegen und die Enkeltochter auf Ihrer gespeicherten Nummer zurückrufen", "Den Anruferschein abfragen und das Geld überweisen", "Geld per Western Union schicken"),
                    correctIndex = 1,
                    explanation = "KI-Stimmen können echte Stimmen täuschend echt nachahmen! Rufen Sie immer auf einer gespeicherten Nummer zurück — niemals Geld am Telefon versprechen!",
                )
            )
        )
    )

    // ── 5. Deepfakes ────────────────────────────────────────────────
    private val lesson5 = SeniorsLesson(
        id = "seniors_05",
        emoji = "🖼️",
        title = "Deepfakes erkennen",
        description = "KI-gefälschte Bilder und Videos erkennen",
        color = 0xFFFF5722.toLong(),
        sections = listOf(
            SeniorsSection(
                title = "Was sind Deepfakes?",
                emoji = "🎭",
                content = "Deepfakes sind Bilder oder Videos, die mit KI\ngenfälscht wurden. Bekannte Menschen scheinen Dinge\nzu sagen oder zu tun, die nie passiert sind.\n\n" +
                    "So erkennen Sie Deepfakes:\n" +
                    "• Unnatürliche Bewegungen: Augen blinzeln selten\n" +
                    "• Lippen passen nicht zum Ton\n" +
                    "• Fehler an Händen, Ohren oder Hintergrund\n" +
                    "• Würde diese Person wirklich so etwas sagen?",
                keyTakeaway = "Deepfakes werden immer besser — Sie können nicht alles erkennen.\nWenn es unglaublich klingt, prüfen Sie es bei einer vertrauenswürdigen Nachrichtenquelle.",
                quiz = SeniorsQuiz(
                    question = "Ein Video zeigt einen bekannten Politiker mit skandalöser Aussage. Es wirkt echt. Was tun Sie?",
                    options = listOf("Es sofort in der Familie weiterleiten", "Bei vertrauenswürdigen Nachrichten prüfen — es könnte ein Deepfake sein", "Videos sind nie gefälscht — also glauben", "Den Politiker direkt anrufen"),
                    correctIndex = 1,
                    explanation = "Bei unglaublichen Nachrichten: Erst bei vertrauenswürdigen Quellen prüfen! Deepfakes werden immer besser.",
                )
            )
        )
    )

    // ── 6. KI-Chatbots ──────────────────────────────────────────────
    private val lesson6 = SeniorsLesson(
        id = "seniors_06",
        emoji = "💬",
        title = "Sicher mit KI-Chatbots",
        description = "KI-Chatbots richtig und sicher nutzen",
        color = 0xFF009688.toLong(),
        sections = listOf(
            SeniorsSection(
                title = "KI-Chatbots richtig nutzen",
                emoji = "🤖",
                content = "KI-Chatbots (wie ChatGPT) beantworten Fragen\nzu allem. Praktisch — aber mit Regeln:\n\n" +
                    "• Keine persönlichen Daten an Chatbots geben\n  (Name, Adresse, Kontonummer)\n" +
                    "• Nicht alles glauben — Chatbots können sich irren\n" +
                    "• Keine Ratschläge zu Medizin, Recht oder Geld\n  ohne Fachperson befolgen\n" +
                    "• Zum Lernen nutzen: Fragen stellen, sich Dinge\n  erklären lassen — aber wichtige Infos immer prüfen",
                keyTakeaway = "Chatbots sind Helfer, keine Experten.\nBei medizinischen, rechtlichen oder finanziellen Fragen immer einen Fachmann fragen!",
                quiz = SeniorsQuiz(
                    question = "Ein Chatbot empfiehlt Ihnen ein Wundermittel gegen hohen Blutdruck. Was tun Sie?",
                    options = listOf("Es sofort bestellen", "Beim Arzt oder Apotheker nachfragen", "Die Einnahme mit jetziger Medizin kombinieren", "Es allen Freunden weiterempfehlen"),
                    correctIndex = 1,
                    explanation = "Chatbots geben keine sicheren Medizin-Ratschläge! Fragen Sie immer Ihren Arzt oder Apotheker.",
                )
            )
        )
    )

    val all: List<SeniorsLesson> = listOf(
        lesson1, lesson2, lesson3, lesson4, lesson5, lesson6,
    )
}
