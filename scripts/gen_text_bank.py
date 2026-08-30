# -*- coding: utf-8 -*-
"""Generate TextGameBank.kt (Fake-or-Echt content bank). Part 1/3 (rounds A)."""
import io

ROUNDS_A = [
 ("r01", True,  "BEGINNER", "Hallo! Ich bin ein Sprachassistent. Ich helfe gern bei Fragen und Aufgaben. Mein Ziel ist es, Wissen klar und freundlich weiterzugeben. Frag mich einfach!", "Hello! I am a language assistant. I enjoy helping with questions and tasks. My goal is to share knowledge clearly and kindly. Just ask me!", "Der Text wirkt persönlich und direkt und stammt von einem KI-Assistenten. Solche Botschaften sind oft freundlich und antworten sofort.", "The text feels personal and direct and comes from an AI assistant. Such messages are often friendly and respond immediately."),
 ("r02", False, "BEGINNER", "Liebe Oma, vielen Dank für den Geburtstagskuchen! Er war wunderbar. Wir haben den Abend zusammen mit einem Spaziergang beendet. Bis bald, deine Lena.", "Dear Grandma, thank you so much for the birthday cake! It was wonderful. We ended the evening with a walk together. See you soon, your Lena.", "Ein persönlicher Brief an die Oma mit persönlichen Details und einem Namen wirkt wie von einem echten Menschen geschrieben.", "A personal letter to grandma with personal details and a name reads like it was written by a real human."),
 ("r03", True,  "BEGINNER", "Warum tragen manche Menschen eine Brille? Brillen helfen, scharf zu sehen, wenn die Augen eine Sehstärke brauchen. Seit Jahrhunderten gibt es sie in vielen Formen und Farben.", "Why do some people wear glasses? Glasses help see sharply when the eyes need a corrective lens. For centuries, they have come in many shapes and colors.", "Der sachliche, allgemeine Erklärtext gibt typisches KI-Wissen wieder, ohne persönliche Erfahrung.", "The factual, generic explanation presents typical AI knowledge without personal experience."),
 ("r04", False, "BEGINNER", "Gestern beim Markt habe ich die besten Erdbeeren meines Lebens probiert. Der Händler liess mich sogar kosten. Mein Nachbar war skeptisch, dann kaufte er zwei Körbchen.", "Yesterday at the market I tasted the best strawberries of my life. The vendor even let me sample one. My neighbor was skeptical, then bought two baskets.", "Kleine persönliche Anekdote mit spezifischen Details und Gesprächen wirkt menschlich.", "A small personal anecdote with specific details and conversations feels human."),
 ("r05", True,  "BEGINNER", "Zusammenfassung: Der Vortrag erklärte die Grundlagen des maschinellen Lernens. Beispiele waren Bilderkennung, Sprachassistenten und Empfehlungssysteme. Am Ende gab es eine Fragerunde.", "Summary: The talk explained the basics of machine learning. Examples were image recognition, voice assistants, and recommendation systems. At the end there was a Q&A session.", "Eine formal aufgebaute Zusammenfassung mit klarer Struktur ist typisch für automatisierte Notizen.", "A formally structured summary with a clear outline is typical of automated notes."),
 ("r06", False, "BEGINNER", "Mein Hund hat heute den Postboten angebellt, weil dieser eine Regenjacke trug. Danach hat er sich geschämt und sich hinter dem Sofa versteckt. So ein Tollpatsch!", "My dog barked at the mail carrier today because he was wearing a raincoat. Afterward he felt embarrassed and hid behind the sofa. What a goofball!", "Umgangssprache und Gefühle, die dem Haustier zugeschrieben werden, wirken sehr menschlich.", "Colloquial speech and emotions attributed to a pet feel very human."),
 ("r07", True,  "INTERMEDIATE", "Das neue Smartphone bietet eine Akkulaufzeit von zwei Tagen und eine Kamera mit Nachtmodus. Der Test zeigte: Deutlich besser als das Vorgängermodell, aber teurer.", "The new smartphone offers two days of battery life and a camera with night mode. The test showed: clearly better than the previous model, but more expensive.", "Der Text listet markige Vergleiche und Kennzahlen ohne eigene Erfahrung – typisch für generierte Produktberichte.", "The text lists punchy comparisons and metrics without personal experience - typical of generated product reports."),
 ("r08", False, "BEGINNER", "Rezept für Pfannkuchen: Mehl, Milch, Eier und eine Prise Salz verrühren. In der Pfanne goldbraun backen. Mit Apfelmus servieren. Guten Appetit!", "Pancake recipe: Whisk flour, milk, eggs, and a pinch of salt. Fry golden brown in a pan. Serve with applesauce. Enjoy!", "Ein kurz und persönlich formuliertes Familienrezept, wie es oft ein Mensch aufschreibt.", "A short, personally phrased family recipe the way a human often writes it down."),
 ("r09", True,  "INTERMEDIATE", "Zusammenfassung der Studie: Tägliches Lesen verbessert die Konzentration. Die Forscher empfehlen mindestens 20 Minuten am Tag. Die Ergebnisse beruhen auf 1.200 Teilnehmern.", "Study summary: Daily reading improves concentration. The researchers recommend at least 20 minutes a day. The results are based on 1,200 participants.", "Klar erklärte Kennzahlen und Empfehlungen ohne persönliche Meinung deuten auf eine KI-Zusammenfassung hin.", "Clearly explained figures and recommendations without personal opinion point to an AI summary."),
 ("r10", False, "BEGINNER", "Ich habe meinen ersten Marathon gelaufen und bin nach 26 Kilometern fast umgekippt. Aber die Zielgerade! Die Menge hat gejubelt. Ehrlich, ich weine immer noch ein bisschen.", "I ran my first marathon and nearly collapsed at 26 kilometers. But that finish line! The crowd was cheering. Honestly, I am still tearing up a little.", "Sehr emotionale Erzählung mit persönlicher Schwäche wirkt echt menschlich.", "A highly emotional account with personal weakness feels genuinely human."),
 ("r11", True,  "INTERMEDIATE", "Tipps für besseren Schlaf: Halte feste Zeiten ein, vermeide Koffein am Nachmittag und schalte Bildschirme eine Stunde vorher aus. Ein kühles Zimmer hilft ebenfalls.", "Tips for better sleep: Keep a regular schedule, avoid afternoon caffeine, and switch off screens an hour before bed. A cool room also helps.", "Allgemeine, zeitlose Ratschläge in Listenform sind ein Kennzeichen generierter Ratgebertexte.", "Generic, timeless advice in list form is a hallmark of generated guide texts."),
 ("r12", False, "INTERMEDIATE", "Mein Tag als Busfahrer: Um 5 Uhr aufstehen, pünktlich losfahren, freundlich grüssen. Heute hat ein Kind mir einen selbstgemalten Drachen geschenkt. Das mache ich seit zehn Jahren.", "My day as a bus driver: Up at 5 a.m., leave on time, greet everyone kindly. Today a child gave me a hand-drawn dragon. I have been doing this for ten years.", "Die Details eines konkreten Berufsalltags mit persönlicher Erinnerung sind menschlich.", "The details of a concrete working day with a personal memory are human."),
 ("r13", True,  "INTERMEDIATE", "Analyse: Der Energieverbrauch stieg im ersten Quartal um vier Prozent. Hauptsächlich durch kühle Temperaturen und mehr Homeoffice. Für das zweite Quartal wird ein leichter Rückgang erwartet.", "Analysis: Energy consumption rose four percent in the first quarter, mainly due to cool temperatures and more working from home. A slight decline is expected for the second quarter.", "Die strukturierte Datenanalyse mit Vorhersage wirkt wie ein KI-generierter Bericht.", "The structured data analysis with a forecast reads like an AI-generated report."),
 ("r14", False, "BEGINNER", "Oma Greta hat mir beigebracht, wie man Marmelade einkocht. Jetzt riecht die ganze Küche nach Erdbeere. Nächstes Jahr mache ich es allein – versprochen!", "Grandma Greta taught me how to can jam. Now the whole kitchen smells of strawberries. Next year I will do it alone - promised!", "Die Erinnerung an eine Person und ein Versprechen für die Zukunft wirken echt.", "The memory of a person and a promise for the future feel authentic."),
]
ROUNDS_B = [
 ("r15", True,  "BEGINNER", "Liebes Tagebuch der KI: Heute habe ich 1.000 Anfragen beantwortet, 500 davon über das Wetter. Ich habe dazu gelernt und freue mich auf morgen.", "Dear AI diary: Today I answered 1,000 requests, 500 of them about the weather. I learned something new and I am looking forward to tomorrow.", "Die Verbindung von Zahlen und scheinbarer Gefühlsregung ist typisch für einen KI-Text.", "The mix of numbers and seeming emotion is typical of an AI text."),
 ("r16", False, "BEGINNER", "Meine Katze sitzt gern auf der Tastatur, genau dann, wenn ich arbeite. Heute hat sie versehentlich eine E-Mail an meinen Chef geschickt: 'fjhsdksa'. Zum Glück nur an mich.", "My cat likes to sit on the keyboard, exactly when I work. Today she accidentally sent an email to my boss: 'fjhsdksa'. Luckily only to me.", "Der humorvolle, persönliche Fehlerbericht mit absurdem Detail ist menschlich.", "The humorous, personal mishap report with an absurd detail is human."),
 ("r17", True,  "INTERMEDIATE", "Schritt für Schritt: Software installieren. 1. Datei herunterladen. 2. Installationsassistenten starten. 3. Lizenz akzeptieren. 4. Fertig. Bei Fragen hilft die Dokumentation.", "Step by step: Install software. 1. Download the file. 2. Start the installer. 3. Accept the license. 4. Done. For questions, see the documentation.", "Neutrale, vollständige Anweisungen ohne persönliche Zwischentöne sind typisch für KI-Anleitungen.", "Neutral, complete instructions without personal asides are typical of AI guides."),
 ("r18", False, "BEGINNER", "Beim Wandern habe ich heute einen Fuchs getroffen. Er blieb stehen, schaute mich an und trottete weiter. Ich stand noch Minuten lang still vor Glück.", "While hiking today I met a fox. It stopped, looked at me, and trotted on. I stood still for minutes out of pure joy.", "Die poetische persönliche Beobachtung eines einzelnen Moments wirkt echt menschlich.", "The poetic personal observation of a single moment feels genuinely human."),
 ("r19", True,  "INTERMEDIATE", "Nachrichten: Stadt eröffnet neuen Park. Der Park bietet Spielplätze, Bäume und einen Teich. Die Eröffnung findet am Samstag um 10 Uhr statt. Alle Bürger sind eingeladen.", "News: City opens a new park. The park offers playgrounds, trees, and a pond. The opening takes place on Saturday at 10 a.m. All citizens are invited.", "Formelhafte Pressesprache mit Ort, Zeit und Einladung wirkt generiert.", "Formulaic press language with place, time, and invitation looks generated."),
 ("r20", False, "BEGINNER", "Ich habe heute zum ersten Mal Brot gebacken. Es ist außen knusprig und innen fast roh. Aber mein Sohn sagt, es schmeckt wie vom Bäcker. Ich glaube ihm nicht.", "I baked bread for the first time today. It is crusty outside and almost raw inside. But my son says it tastes like it is from the bakery. I do not believe him.", "Selbstironie und eine kleine Familiengeschichte sind Zeichen eines echten Menschen.", "Self-irony and a small family anecdote are signs of a real human."),
]

ROUNDS_C = [
 ("r21", True,  "EXPERT", "Wissenschaftlicher Text: Die Untersuchung zeigt einen statistisch signifikanten Zusammenhang zwischen Bewegungsart und Kognition. Weitere Studien sind nötig, um Kausalität zu belegen.", "Scientific text: The study shows a statistically significant relationship between exercise type and cognition. Further studies are necessary to establish causation.", "Präzise, vorsichtige Wissenschaftssprache mit Einschränkungen ist typisch für generierte Zusammenfassungen.", "Precise, cautious scientific language with caveats is typical of generated abstracts."),
 ("r22", False, "INTERMEDIATE", "Mein Vater erklärt mir die Sterne, seit ich klein bin. Den Großen Bär, die Milchstraße und welcher Stern unser Nachbar ist. Gestern hat er zur Abwechslung ein Foto vom Mond gezeigt.", "My father has been explaining the stars to me since I was little. The Big Dipper, the Milky Way, and which star is our neighbor. Yesterday, for a change, he showed a photo of the moon.", "Die persönliche Familiengeschichte mit kleinen Details wirkt authentisch menschlich.", "The personal family story with small details feels authentically human."),
 ("r23", True,  "INTERMEDIATE", "Bewertung: Der Staubsauger saugt gut, ist aber laut. Für den Preis eine solide Wahl. Vier von fünf Sternen.", "Review: The vacuum cleaner sucks well but is loud. Solid value for the price. Four out of five stars.", "Eine knappe, abwägende Bewertung ohne persönliche Anekdote ist typisch generiert.", "A concise, balanced review without personal anecdote is typically generated."),
 ("r24", False, "BEGINNER", "Kurznachricht an Franz: Kommst du heute zum Training? Ich warte am Eingang, du Tollpatsch. Bring bitte die Pumpe mit! – Dein Tom.", "Text message to Franz: Are you coming to training today? I am waiting at the entrance, you goof. Please bring the pump! - Your Tom.", "Umgangssprache und Späße mit einem Freund wirken wie eine echte Kurznachricht.", "Colloquial language and banter with a friend feel like a real text message."),
 ("r25", True,  "EXPERT", "Rechtlicher Hinweis: Diese Mitteilung enthält vertrauliche Informationen. Falls Sie nicht der Adressat sind, löschen Sie die Nachricht und informieren Sie den Absender.", "Legal notice: This message contains confidential information. If you are not the addressee, delete the message and notify the sender.", "Der formelhafte rechtliche Standardpassus ist typisch generiert.", "The formulaic legal standard passage is typically generated."),
 ("r26", False, "INTERMEDIATE", "Ich repariere seit zwanzig Jahren Fahrräder. Heute kam eine Frau mit einem Fahrrad von 1975 herein. Die Aufschrift 'Made in Germany' glänzte noch. Wir haben es gerettet.", "I have been fixing bicycles for twenty years. Today a woman came in with a 1975 bike. The 'Made in Germany' badge still shone. We saved it.", "Berufserfahrung mit einem konkreten, persönlichen Fall wirkt menschlich.", "Career experience with a concrete personal case feels human."),
 ("r27", True,  "INTERMEDIATE", "Motivationszitat: Der Weg ist das Ziel. Jeder Schritt zählt, auch die kleinen. Vertraue auf deine Kraft und bleibe freundlich zu dir selbst. Mehr dazu im nächsten Beitrag.", "Motivational quote: The journey is the destination. Every step counts, even the small ones. Trust your strength and stay kind to yourself. More in the next post.", "Ein allgemeines, zeitloses Motivationszitat ohne konkreten Anlass ist typisch generiert.", "A generic, timeless motivational quote without a concrete occasion is typically generated."),
 ("r28", False, "BEGINNER", "Als Kind habe ich Regenwürmer gerettet, die nach dem Regen auf dem Weg lagen. Meine Mama fand das seltsam. Heute mache ich es immer noch, aber heimlich.", "As a child I rescued earthworms that lay on the path after the rain. My mom found that strange. Today I still do it, but secretly.", "Die spezifische, leicht peinliche Kindheitserinnerung ist sehr menschlich.", "The specific, slightly embarrassing childhood memory is very human."),
 ("r29", True,  "EXPERT", "An alle Teammitglieder: Die nächste Produktfreigabe ist am Freitag. Bitte reicht eure Beiträge bis Mittwoch ein. Danke für die Zusammenarbeit. Euer Projektteam.", "To all team members: The next product release is on Friday. Please submit your contributions by Wednesday. Thanks for the collaboration. Your project team.", "Der formal höfliche, unpersönliche Teamstandardtext ist typisch generiert.", "The formally polite, impersonal team standard text is typically generated."),
 ("r30", False, "BEGINNER", "Meine Grossmutter sagt immer: Wolken sind die Haare der Berge. Heute habe ich am Fenster gesessen und ihr einfach so eine Postkarte geschrieben. Manchmal reicht das.", "My grandmother always says: Clouds are the hair of the mountains. Today I sat by the window and simply wrote her a postcard. Sometimes that is enough.", "Eine Familienweisheit und ruhige persönliche Gesten wirken echt menschlich.", "A family saying and quiet personal gestures feel genuinely human."),
]

ROUNDS = ROUNDS_A + ROUNDS_B + ROUNDS_C

def k(s):
    return s.replace("\\", "\\\\").replace('"', '\\"')

def line(id_, is_ai, diff, d, e, xd, xe):
    return ('        TextRound(\n'
            f'            id = "{id_}",\n'
            f'            textDe = "{k(d)}",\n'
            f'            textEn = "{k(e)}",\n'
            f'            isAi = {"true" if is_ai else "false"},\n'
            f'            explanationDe = "{k(xd)}",\n'
            f'            explanationEn = "{k(xe)}",\n'
            f'            difficulty = Difficulty.{diff},\n'
            '        ),')

L = []
L.append("package ai.ki_kompetenz_training_org.data.minigames")
L.append("")
L.append("/**")
L.append(" * Content bank for the Fake-or-Echt game (Fake or Real).")
L.append(" * Each round shows one text; the user decides whether it was written by")
L.append(" * a human or by AI. Balance is 15 AI / 15 human, difficulty mix, ids r01..r30.")
L.append(" */")
L.append("data class TextRound(")
L.append("    val id: String,")
L.append("    val textDe: String, val textEn: String,")
L.append("    val isAi: Boolean,")
L.append("    val explanationDe: String, val explanationEn: String,")
L.append("    val difficulty: Difficulty,")
L.append(") {")
L.append("    fun toMiniGameRound(): MiniGameRound = MiniGameRound(")
L.append("        promptDe = textDe, promptEn = textEn,")
L.append("        optionsDe = listOf(\"KI\", \"Mensch\"),")
L.append("        optionsEn = listOf(\"AI\", \"Human\"),")
L.append("        correctIndex = if (isAi) 0 else 1,")
L.append("        explanationDe = explanationDe, explanationEn = explanationEn,")
L.append("    )")
L.append("}")
L.append("")
L.append("object TextGameBank {")
L.append("    val ALL: List<TextRound> = listOf(")
for r in ROUNDS:
    L.append(line(*r))
L.append("    )")
L.append("}")
L.append("")

out = "\n".join(L)
io.open("app/src/main/java/ai/ki_kompetenz_training_org/data/minigames/TextGameBank.kt", "w", encoding="utf-8", newline="\n").write(out)
print("written", len(ROUNDS), "rounds;", len(out), "bytes")
