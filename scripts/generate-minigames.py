#!/usr/bin/env python3
"""Generate MiniGames.kt with all 16 games"""

import os

# Game data structure
games = [
    # FREE GAMES
    {
        "id": "human_or_ai", "emoji": "🤖",
        "titleDe": "KI oder Mensch?", "titleEn": "Human or AI?",
        "descriptionDe": "Erkenne, ob ein Text von einer KI oder einem Menschen geschrieben wurde.",
        "descriptionEn": "Recognize whether a text was written by an AI or a human.",
        "difficulty": "BEGINNER", "premium": False,
        "rounds": [
            ("Zusammenfassend lässt sich sagen, dass KI-Systeme Chancen und Herausforderungen bieten.",
             "In summary, AI systems offer opportunities and challenges.", 0,
             "🔍 TYPISCH KI: Übermäßig strukturiert, Füllwörter, keine Tippfehler.",
             "🔍 TYPICALLY AI: Overly structured, filler words, no typos."),
            ("Alter, das war gestern echt anstrengend. Aber egal – jetzt erstmal Kaffee. 😅",
             "Dude, yesterday was exhausting. But whatever – coffee first. 😅", 1,
             "✅ MENSCHLICH: Umgangssprache, Emoji, persönlicher Kontext.",
             "✅ HUMAN: Colloquial language, emoji, personal context."),
            ("Im Folgenden werden die Aspekte der KI-Verordnung dargestellt. Abschnitt 1.",
             "The aspects of the AI Regulation are presented below. Section 1.", 0,
             "🔍 TYPISCH KI: Nummerierte Strukturen, formelhafte Einleitungen.",
             "🔍 TYPICALLY AI: Numbered structures, formulaic openings."),
            ("Also ehrlich, ich check das mit dem KI-Gesetz nicht ganz – was heißt das?",
             "Honestly, I don't get this AI law thing – what does it mean?", 1,
             "✅ MENSCHLICH: Umgangssprache, echte Unsicherheit, spezifische Frage.",
             "✅ HUMAN: Colloquial language, genuine uncertainty, specific question."),
            ("In einer zunehmend digitalisierten Welt spielt KI eine wichtige Rolle.",
             "In an increasingly digitalized world, AI plays an important role.", 0,
             "🔍 TYPISCH KI: Generische Floskeln ohne konkreten Inhalt.",
             "🔍 TYPICALLY AI: Generic buzzwords without concrete content."),
            ("Sorry für die späte Antwort, das Meeting hat sich gezogen.",
             "Sorry for the late reply, the meeting ran long.", 1,
             "✅ MENSCHLICH: Entschuldigung, konkrete Lebenssituation.",
             "✅ HUMAN: Apology, concrete life situation."),
            ("Es ist wichtig zu betonen, dass Compliance von entscheidender Bedeutung ist.",
             "It is important to emphasize that compliance is crucial.", 0,
             "🔍 TYPISCH KI: Absichernde Formulierungen ohne persönliche Meinung.",
             "🔍 TYPICALLY AI: Hedging phrases without personal opinion."),
            ("Moin! Wie sieht\'s aus, schaffen wir den Termin morgen? LG",
             "Hey! Can we make the meeting tomorrow? Cheers", 1,
             "✅ MENSCHLICH: Regionaler Gruß, Abkürzung, direkte Frage.",
             "✅ HUMAN: Regional greeting, abbreviation, direct question."),
            ("Es ist wichtig zu betonen, dass Compliance von entscheidender Bedeutung ist.",
             "It is important to emphasize that compliance is crucial.", 0,
             "🔍 TYPISCH KI: Absichernde Formulierungen ohne persönliche Meinung.",
             "🔍 TYPICALLY AI: Hedging phrases without personal opinion."),
            ("Moin! Wie sieht's aus, schaffen wir den Termin morgen? LG",
             "Hey! Can we make the meeting tomorrow? Cheers", 1,
             "✅ MENSCHLICH: Regionaler Gruß, Abkürzung, direkte Frage.",
             "✅ HUMAN: Regional greeting, abbreviation, direct question."),
        ]
    },
    {
        "id": "fact_or_hallucination", "emoji": "🤥",
        "titleDe": "Fakt oder Halluzination?", "titleEn": "Fact or Hallucination?",
        "descriptionDe": "Trenne wahre KI-Fakten von typischen KI-Halluzinationen.",
        "descriptionEn": "Separate true AI facts from typical AI hallucinations.",
        "difficulty": "INTERMEDIATE", "premium": False,
        "rounds": [
            ("Der EU AI Act ist am 1. August 2024 in Kraft getreten.",
             "The EU AI Act entered into force on 1 August 2024.", 0,
             "✅ RICHTIG! Gilt seit 1. August 2024, Pflichten starten gestaffelt.",
             "✅ CORRECT! Applies since 1 August 2024, obligations start in stages."),
            ("ChatGPT wurde von Microsoft entwickelt.",
             "ChatGPT was developed by Microsoft.", 1,
             "❌ FALSCH! ChatGPT stammt von OpenAI. Microsoft ist Investor.",
             "❌ FALSE! ChatGPT comes from OpenAI. Microsoft is investor."),
            ("Ein KI-System zur Kreditwürdigkeitsprüfung gilt als Hochrisiko-KI.",
             "An AI system for credit assessment is considered high-risk AI.", 0,
             "✅ RICHTIG! Kredit-Scoring steht in Anhang III des AI Act.",
             "✅ CORRECT! Credit scoring is in Annex III of the AI Act."),
            ("Ein LLM gibt immer die richtige Antwort bei klarer Fragestellung.",
             "An LLM always gives the right answer with clear questions.", 1,
             "❌ FALSCH! LLMs halluzinieren auch bei klaren Fragen!",
             "❌ FALSE! LLMs hallucinate even with clear questions!"),
            ("Die KI-Kompetenzpflicht nach Art. 4 gilt nur für große Unternehmen.",
             "The AI competence obligation under Art. 4 only applies to large companies.", 1,
             "❌ FALSCH! Art. 4 gilt für ALLE, die KI einsetzen!",
             "❌ FALSE! Art. 4 applies to EVERYONE using AI!"),
            ("Das EU AI Act verbietet Social Scoring durch Behörden.",
             "The EU AI Act prohibits social scoring by public authorities.", 0,
             "✅ RICHTIG! Social Scoring ist nach Art. 5 verboten.",
             "✅ CORRECT! Social scoring is prohibited under Art. 5."),
            ("Deepfakes sind immer legal, solange sie als solche gekennzeichnet sind.",
             "Deepfakes are always legal as long as they are labeled.", 1,
             "❌ FALSCH! Manipulative Deepfakes können trotzdem verboten sein!",
             "❌ FALSE! Manipulative deepfakes can still be prohibited!"),
            ("Die DSGVO gilt auch für KI-Systeme mit personenbezogenen Daten.",
             "The GDPR also applies to AI systems with personal data.", 0,
             "✅ RICHTIG! KI-Systeme müssen DSGVO-konform sein.",
             "✅ CORRECT! AI systems must comply with GDPR."),
        ]
    },
    {
        "id": "high_risk_blitz", "emoji": "⚠️",
        "titleDe": "Hochrisiko-Blitz", "titleEn": "High-Risk Blitz",
        "descriptionDe": "Erkenne, welche KI-Anwendungen nach dem EU AI Act eingeschränkt sind.",
        "descriptionEn": "Recognize which AI applications are restricted under the EU AI Act.",
        "difficulty": "INTERMEDIATE", "premium": False,
        "rounds": [
            ("KI-System zur Bewertung der Kreditwürdigkeit", "AI system for credit assessment", 0,
             "⚠️ HOCH RISIKO! Kredit-Scoring ist Hochrisiko (Anhang III).",
             "⚠️ HIGH RISK! Credit scoring is high-risk (Annex III)."),
            ("KI-Chatbot für Produkt-FAQs im Onlineshop", "AI chatbot for product FAQs", 1,
             "✅ UNBEDENKLICH! Kundenservice-Chatbots sind begrenztes Risiko.",
             "✅ UNPROBLEMATIC! Customer service chatbots are limited risk."),
            ("KI zur automatischen Auswahl von Bewerbungen", "AI for automatic job application selection", 0,
             "⚠️ HOCH RISIKO! KI im Personalwesen ist Hochrisiko wegen Bias.",
             "⚠️ HIGH RISK! AI in HR is high-risk due to bias."),
            ("KI-Spamfilter für E-Mails", "AI spam filter for emails", 1,
             "✅ UNBEDENKLICH! Spamfilter sind Alltags-KI mit minimalem Risiko.",
             "✅ UNPROBLEMATIC! Spam filters are everyday AI with minimal risk."),
            ("KI zur Unterstützung der medizinischen Diagnose", "AI supporting medical diagnosis", 0,
             "⚠️ HOCH RISIKO! Medizinische KI – Fehler können Leben kosten.",
             "⚠️ HIGH RISK! Medical AI – mistakes can cost lives."),
            ("KI-Notbremsassistent in Autos", "AI emergency braking assistant", 0,
             "⚠️ HOCH RISIKO! Sicherheitskritische KI in Fahrzeugen.",
             "⚠️ HIGH RISK! Safety-critical AI in vehicles."),
            ("KI-App zur Erkennung von Katzenrassen auf Fotos", "AI app for cat breeds", 1,
             "✅ UNBEDENKLICH! Unterhaltungs-KI ohne Rechtswirkungen.",
             "✅ UNPROBLEMATIC! Entertainment AI without legal effects."),
            ("Echtzeit-Gesichtserkennung durch Behörden im öffentlichen Raum", "Real-time facial recognition by authorities", 0,
             "🚫 VERBOTEN! Grundsätzlich verboten nach Art. 5!",
             "🚫 PROHIBITED! Basically prohibited under Art. 5!"),
            ("KI zur Erkennung von Prüfungsbetrug", "AI for exam cheating detection", 0,
             "⚠️ HOCH RISIKO! KI in Bildung kann über Zukunft entscheiden.",
             "⚠️ HIGH RISK! AI in education can decide learners' future."),
            ("KI-Sprachassistent für Alltagsübersetzungen", "AI voice assistant for daily translations", 1,
             "✅ UNBEDENKLICH! Übersetzungstools sind Alltags-KI.",
             "✅ UNPROBLEMATIC! Translation tools are everyday AI."),
        ]
    },
    {
        "id": "agent_ampel", "emoji": "🤖",
        "titleDe": "Agenten-Ampel", "titleEn": "Agent Traffic Light",
        "descriptionDe": "Schätze ein, wie viel Autonomie ein KI-Agent haben darf.",
        "descriptionEn": "Assess how much autonomy an AI agent may have.",
        "difficulty": "INTERMEDIATE", "premium": False,
        "rounds": [
            ("Ein KI-Agent sortiert Projekt-Dokumente nach festen Regeln.",
             "An AI agent sorts project documents according to fixed rules.", 0,
             "🟢 AUTONOM OK! Routine-Aufgabe nach festen Regeln ohne Rechtswirkung.",
             "🟢 AUTONOMY OK! Routine task with fixed rules and no legal effect."),
            ("Ein Recruiting-Agent trifft Einstellungszusagen ohne Menschen.",
             "A recruiting agent makes hiring offers without humans.", 2,
             "🔴 NICHT ERLAUBT! Automatisierte Einstellungsentscheidungen sind Hochrisiko!",
             "🔴 NOT ALLOWED! Automated hiring decisions are high-risk!"),
            ("Ein Support-Agent schlägt Antworten vor; Mensch muss freigeben.",
             "A support agent proposes answers; human must approve.", 1,
             "🟡 NUR MIT AUFSICHT! Vorschläge mit menschlicher Freigabe = Human Oversight.",
             "🟡 ONLY WITH OVERSIGHT! Proposals with human approval = human oversight."),
            ("Ein Agent verschickt automatisch Werbe-E-Mails mit manipulativen Inhalten.",
             "An agent automatically sends marketing emails with manipulative content.", 2,
             "🔴 VERBOTEN! Manipulative Systeme sind nach Art. 5 verboten!",
             "🔴 PROHIBITED! Manipulative systems are prohibited under Art. 5!"),
            ("Ein Agent erstellt und versendet Mahnungen mit rechtlichen Konsequenzen.",
             "An agent creates and sends payment reminders with legal consequences.", 1,
             "🟡 NUR MIT AUFSICHT! Rechtswirksame Außenwirkung: menschliche Prüfung nötig.",
             "🟡 ONLY WITH OVERSIGHT! Legally binding external effect: human review needed."),
            ("Ein Agent überwacht Kund:innen per Gesichtserkennung im Laden.",
             "An agent monitors customers via facial recognition in store.", 2,
             "🔴 VERBOTEN! Emotionserkennung in sensiblen Kontexten ist verboten!",
             "🔴 PROHIBITED! Emotion recognition in sensitive contexts is prohibited!"),
            ("Ein Kalender-Agent plant Meetings nach Präferenzen (keine Entscheidungsgewalt).",
             "A calendar agent schedules meetings by preferences (no decision authority).", 0,
             "🟢 AUTONOM OK! Terminkoordination ohne Rechts- oder Sachfolgen.",
             "🟢 AUTONOMY OK! Scheduling without legal or material consequences."),
        ]
    },
    {
        "id": "shadow_ai_check", "emoji": "🕵️",
        "titleDe": "Shadow-AI-Check", "titleEn": "Shadow-AI Check",
        "descriptionDe": "Erkenne Shadow AI — unerlaubte Nutzung von KI ohne Wissen des Unternehmens.",
        "descriptionEn": "Recognize shadow AI — unauthorized use of AI without company knowledge.",
        "difficulty": "BEGINNER", "premium": False,
        "rounds": [
            ("Lisa nutzt für Kundendaten ein freies Online-KI-Tool ohne Freigabe.",
             "Lisa uses a free online AI tool for customer data without approval.", 0,
             "🚨 SHADOW AI! Nicht autorisierte Nutzung mit Kundendaten = DSGVO-Risiko!",
             "🚨 SHADOW AI! Unauthorized use with customer data = GDPR risk!"),
            ("Das Team nutzt das freigegebene Agenten-Tool mit Schulung.",
             "The team uses the approved agent tool with training.", 1,
             "✅ IN ORDNUNG! Freigegeben, geschult, nach Policy.",
             "✅ OK! Approved, trained, policy-compliant."),
            ("Tom baut einen privaten Automatisierungs-Agenten für Firmen-Interna.",
             "Tom builds a private automation agent for company internals.", 0,
             "🚨 SHADOW AI! Private Agenten auf Firmendaten = Kontrollverlust!",
             "🚨 SHADOW AI! Private agents on company data = loss of control!"),
            ("Ein Vertriebler speist die Kundenliste in ein öffentliches Tool.",
             "A salesperson uploads the customer list into a public tool.", 0,
             "🚨 SHADOW AI! Kundendaten in öffentlichen Tools = Datenabfluss!",
             "🚨 SHADOW AI! Customer data in public tools = data leakage!"),
        ]
    },
    {
        "id": "prompt_profis", "emoji": "⌨️",
        "titleDe": "Prompt-Profis", "titleEn": "Prompt Pros",
        "descriptionDe": "Wähle den besseren Prompt — klare Anweisungen liefern bessere Ergebnisse.",
        "descriptionEn": "Choose the better prompt — clear instructions deliver better results.",
        "difficulty": "BEGINNER", "premium": False,
        "rounds": [
            ("Fasse den Vertrag zusammen.", "Fasse Abschnitt 3 des Mietvertrags in 5 Stichpunkten zusammen.", 1,
             "✅ BESSER! Spezifisch + strukturiert + klarer Fokus!",
             "✅ BETTER! Specific + structured + clear focus!"),
            ("Schreib eine E-Mail.", "Schreibe eine kurze, höfliche E-Mail an Frau Schmidt: Terminverschiebung.", 1,
             "✅ BESSER! Ziel, Ton und Details (wer, was, wann)!",
             "✅ BETTER! Goal, tone and details (who, what, when)!"),
            ("Gib mir Tipps.", "Gib mir 3 konkrete Tipps für ein Bewerbungsgespräch als Data Analyst.", 1,
             "✅ BESSER! Anzahl, Rolle, Themenfeld und Beispiele!",
             "✅ BETTER! Count, role, topic and examples!"),
            ("Erkläre KI.", "Erkläre Machine Learning in einfachen Worten, wie einem 12-Jährigen.", 1,
             "✅ BESSER! Zielgruppe + Analogie + Beispiel!",
             "✅ BETTER! Audience + analogy + example!"),
        ]
    },
    {
        "id": "bias_spotter", "emoji": "⚖️",
        "titleDe": "Bias-Spotter", "titleEn": "Bias Spotter",
        "descriptionDe": "Erkenne, welcher Bias in der KI-Ausgabe steckt.",
        "descriptionEn": "Recognize which bias is in the AI output.",
        "difficulty": "INTERMEDIATE", "premium": False,
        "rounds": [
            ("Das KI-Recruiting-Tool lehnt Bewerberinnen häufiger ab.",
             "The AI recruiting tool rejects female applicants more often.", 1,
             "⚠️ GESCHLECHTER-BIAS! Hochrisiko nach Anhang III!",
             "⚠️ GENDER BIAS! High-risk under Annex III!"),
            ("Die KI-Sprachsteuerung versteht Akzente schlechter.",
             "The AI voice assistant understands accents worse.", 0,
             "⚠️ SPRACH-BIAS! Modelle lernen aus standardisierten Daten!",
             "⚠️ LANGUAGE BIAS! Models learn from standardized data!"),
            ("Die Kredit-KI lehnt jüngere Antragsteller häufiger ab.",
             "The credit AI rejects younger applicants more often.", 1,
             "⚠️ ALTERS-BIAS! Altersdiskriminierung ist rechtlich heikel!",
             "⚠️ AGE BIAS! Age discrimination is legally delicate!"),
            ("Die Gesichtserkennung erkennt helle Haut zuverlässiger.",
             "Facial recognition recognizes light skin more reliably.", 0,
             "⚠️ ETHNISCHER BIAS! Daten-Schieflage in Trainingsdaten!",
             "⚠️ ETHNIC BIAS! Data skew in training data!"),
        ]
    },
    {
        "id": "dsgvo_check", "emoji": "🔐",
        "titleDe": "DSGVO-Check", "titleEn": "GDPR Check",
        "descriptionDe": "Ist die KI-Nutzung datenschutzkonform?",
        "descriptionEn": "Is the AI usage data-protection compliant?",
        "difficulty": "BEGINNER", "premium": False,
        "rounds": [
            ("Ein Unternehmen speist Kundendaten in ein öffentliches KI-Tool ein.",
             "A company enters customer data into a public AI tool.", 1,
             "❌ NICHT KONFORM! Keine Rechtsgrundlage, keine Information!",
             "❌ NOT COMPLIANT! No legal basis, no information!"),
            ("Das Team nutzt ein freigegebenes KI-Tool mit AVV.",
             "The team uses an approved AI tool with DPA.", 0,
             "✅ KONFORM! AVV + Freigabe = saubere Verarbeitung!",
             "✅ COMPLIANT! DPA + approval = clean processing!"),
            ("Mitarbeiter laden Kundendokumente in ein Tool für Training.",
             "Employees upload customer documents into a tool for training.", 1,
             "❌ NICHT KONFORM! Training mit Kundendaten ohne Einwilligung!",
             "❌ NOT COMPLIANT! Training with customer data without consent!"),
            ("Die Firma anonymisiert Daten, bevor sie an die KI gibt.",
             "The company anonymizes data before giving to AI.", 0,
             "✅ KONFORM! Anonymisierung ist eine wirksame Schutzmaßnahme!",
             "✅ COMPLIANT! Anonymization is an effective protective measure!"),
        ]
    },
    # PREMIUM GAMES
    {
        "id": "audit_trainer", "emoji": "📋",
        "titleDe": "Audit-Trainer", "titleEn": "Audit Trainer",
        "descriptionDe": "Finde die Compliance-Lücke im KI-System (Premium).",
        "descriptionEn": "Find the compliance gap in the AI system (Premium).",
        "difficulty": "EXPERT", "premium": True,
        "rounds": [
            ("Ein Hochrisiko-KI-System hat kein dokumentiertes Risikomanagement.",
             "A high-risk AI system has no documented risk management.", 1,
             "📋 ART. 9! Jedes Hochrisiko-System braucht Risikomanagement!",
             "📋 ART. 9! Every high-risk system needs risk management!"),
            ("Entwickler können nicht erklären, wie das Modell entscheidet.",
             "Developers cannot explain how the model decides.", 0,
             "📋 ART. 11! Ohne technische Dokumentation kein Audit!",
             "📋 ART. 11! Without technical documentation no auditing!"),
            ("Das KI-System trifft Entscheidungen, Menschen können nicht überstimmen.",
             "The AI system makes decisions, humans cannot override.", 1,
             "📋 ART. 14! Human Oversight bedeutet: Mensch kann eingreifen!",
             "📋 ART. 14! Human oversight means: humans can intervene!"),
            ("Es gibt keine Aufzeichnungen über KI-Entscheidungen.",
             "There are no records of AI decisions.", 0,
             "📋 ART. 12! Automatische Logs sind Pflicht für Hochrisiko-KI!",
             "📋 ART. 12! Automatic logs are mandatory for high-risk AI!"),
        ]
    },
    {
        "id": "agent_simulator", "emoji": "🔬",
        "titleDe": "Agent-Simulator", "titleEn": "Agent Simulator",
        "descriptionDe": "Wähle die richtige Konfiguration für KI-Agenten (Premium).",
        "descriptionEn": "Choose the right configuration for AI agents (Premium).",
        "difficulty": "EXPERT", "premium": True,
        "rounds": [
            ("Ein E-Mail-Agent soll eigenständig Kundenanfragen beantworten.",
             "An email agent should independently answer customer inquiries.", 1,
             "🔬 BEST PRACTICE! Kundenkommunikation: erst Mensch prüft!",
             "🔬 BEST PRACTICE! Customer communication: humans review first!"),
            ("Ein Finanz-Agent erhält Zugriff auf das Zahlungssystem.",
             "A finance agent receives access to the payment system.", 1,
             "🔬 SECURITY! Least Privilege + 4-Augen-Prinzip!",
             "🔬 SECURITY! Least privilege + four-eyes principle!"),
            ("Ein Agent soll Verträge mit Außenwirkung unterzeichnen.",
             "An agent should sign contracts with external effect.", 1,
             "🔬 GOVERNANCE! Rechtswirksame Aktionen brauchen menschliche Kontrolle!",
             "🔬 GOVERNANCE! Legally binding actions need human control!"),
        ]
    },
    {
        "id": "strategie_berater", "emoji": "🧭",
        "titleDe": "Strategie-Berater", "titleEn": "Strategy Advisor",
        "descriptionDe": "Triff die richtige strategische Entscheidung für KI-Einführung (Premium).",
        "descriptionEn": "Make the right strategic decision for AI adoption (Premium).",
        "difficulty": "EXPERT", "premium": True,
        "rounds": [
            ("Das Team will 10 KI-Projekte gleichzeitig starten.",
             "The team wants to start 10 AI projects simultaneously.", 1,
             "🧭 STRATEGIE! Fokus statt Streuung: wenige Use Cases!",
             "🧭 STRATEGY! Focus instead of spreading thin!"),
            ("Die Geschäftsleitung fragt nach dem KI-Budget.",
             "Management asks about the AI budget.", 1,
             "🧭 STRATEGIE! Kompetenz und Governance zuerst!",
             "🧭 STRATEGY! Competence and governance first!"),
            ("Mitarbeiter sorgen sich um ihren Job wegen KI.",
             "Employees worry about their jobs because of AI.", 1,
             "🧭 CHANGE MANAGEMENT! Transparenz und Qualifizierung!",
             "🧭 CHANGE MANAGEMENT! Transparency and upskilling!"),
        ]
    },
    {
        "id": "ki_schutzschild", "emoji": "🛡️",
        "titleDe": "KI-Schutzschild", "titleEn": "AI Shield",
        "descriptionDe": "Wähle die richtige Schutzmaßnahme gegen KI-Risiko.",
        "descriptionEn": "Choose the right protective measure against AI risk.",
        "difficulty": "EXPERT", "premium": True,
        "rounds": [
            ("Ein LLM soll keine vertraulichen Firmendaten preisgeben.",
             "An LLM must not leak confidential company data.", 0,
             "🛡️ ART. 4! Kompetenz und klare Regeln verhindern Datenabflüsse!",
             "🛡️ ART. 4! Competence and clear rules prevent data leaks!"),
            ("KI-Antworten sollen keine falschen Fakten verbreiten.",
             "AI answers should not spread false facts.", 0,
             "🛡️ OVERSIGHT! Verifikation und menschliche Kontrolle!",
             "🛡️ OVERSIGHT! Verification and human oversight!"),
            ("Ein KI-Agent soll keine rechtswidrigen Entscheidungen treffen.",
             "An AI agent must not make unlawful decisions.", 0,
             "🛡️ GUARDRAILS! Guardrails mit menschlicher Eskalation!",
             "🛡️ GUARDRAILS! Guardrails with human escalation!"),
        ]
    },
    {
        "id": "ki_sprachfuehrer", "emoji": "🗣️",
        "titleDe": "KI-Sprachführer", "titleEn": "AI Language Guide",
        "descriptionDe": "Was darf in die KI — und was nicht?",
        "descriptionEn": "What belongs in the AI — and what doesn't?",
        "difficulty": "INTERMEDIATE", "premium": True,
        "rounds": [
            ("Interne Budgetzahlen des Jahresabschlusses",
             "Internal budget figures of the annual report", 0,
             "🚫 VERTRAULICH! Vertrauliche Finanzdaten nicht in öffentliche Tools!",
             "🚫 CONFIDENTIAL! Confidential financial data not in public tools!"),
            ("Allgemeine Fragen zu Urlaubsregelungen",
             "General questions about vacation rules", 1,
             "✅ UNKRITISCH! Öffentlich zugängliche Informationen sind unkritisch!",
             "✅ NON-CRITICAL! Publicly available information is non-critical!"),
            ("Kundendaten mit Namen und Kontaktdaten",
             "Customer data with names and contact details", 0,
             "🚫 DSGVO-VERSTOß! Personenbezogene Daten in öffentlichen Tools!",
             "🚫 GDPR VIOLATION! Personal data in public tools!"),
        ]
    },
    {
        "id": "ki_zielscheibe", "emoji": "🎯",
        "titleDe": "KI-Zielscheibe", "titleEn": "AI Target",
        "descriptionDe": "Wofür ist KI der richtige Einsatz — und wofür nicht?",
        "descriptionEn": "Where is AI the right choice — and where not?",
        "difficulty": "BEGINNER", "premium": True,
        "rounds": [
            ("Sortierung eingehender Support-Tickets nach Dringlichkeit",
             "Sorting incoming support tickets by urgency", 0,
             "✅ GUT! Mustererkennung mit klarem Feedback!",
             "✅ GOOD! Pattern recognition with clear feedback!"),
            ("Vorhersage von Börsenkursen für garantierte Gewinne",
             "Predicting stock prices for guaranteed profits", 1,
             "❌ SCHLECHT! Kurse sind nicht zuverlässig vorhersagbar!",
             "❌ BAD! Prices cannot be reliably predicted!"),
            ("Automatische Zusammenfassung langer Vertragstexte",
             "Automatically summarizing long contract texts", 0,
             "✅ GUT! LLMs fassen gut zusammen — mit menschlicher Prüfung!",
             "✅ GOOD! LLMs summarize well – with human review!"),
        ]
    },
    {
        "id": "ki_vertrag", "emoji": "💼",
        "titleDe": "KI-Vertrag", "titleEn": "AI Contract",
        "descriptionDe": "Was gehört in den Vertrag mit dem KI-Anbieter?",
        "descriptionEn": "What belongs in the contract with the AI provider?",
        "difficulty": "INTERMEDIATE", "premium": True,
        "rounds": [
            ("Die Verarbeitung personenbezogener Daten durch den Anbieter",
             "How the provider processes personal data", 0,
             "💼 ART. 28! Ein AVV regelt Datenverarbeitung!",
             "💼 ART. 28! A DPA governs data processing!"),
            ("Nutzung der Kundendaten für das Training des Anbieters",
             "Provider using customer data for training", 0,
             "💼 VERTRAG! Training mit Kundendaten braucht Rechtsgrundlage!",
             "💼 CONTRACT! Training with customer data needs legal basis!"),
            ("Speicherort und -dauer der Daten",
             "Where and how long data is stored", 0,
             "💼 DSGVO! Speicherort (EU) und Löschfristen vertraglich regeln!",
             "💼 GDPR! Storage location (EU) and deletion periods contractual!"),
        ]
    },
    {
        "id": "change_manager", "emoji": "🧑‍🤝‍🧑",
        "titleDe": "Change-Manager", "titleEn": "Change Manager",
        "descriptionDe": "Führe KI im Team richtig ein (Premium).",
        "descriptionEn": "Introduce AI in your team properly (Premium).",
        "difficulty": "INTERMEDIATE", "premium": True,
        "rounds": [
            ("Das Team hat Angst vor KI-Automatisierung.",
             "The team fears AI automation.", 0,
             "🧑‍🤝‍🧑 CHANGE! Transparenz und Qualifizierung verwandeln Angst!",
             "🧑‍🤝‍🧑 CHANGE! Transparency and training turn fear!"),
            ("Einflussreiche Mitarbeiter blockieren die KI-Einführung.",
             "Influential employees block the AI rollout.", 0,
             "🧑‍🤝‍🧑 ADOPTION! Frühe Einbindung schafft Akzeptanz!",
             "🧑‍🤝‍🧑 ADOPTION! Early involvement creates acceptance!"),
            ("KI übernimmt Aufgaben, die Menschen vorher gemacht haben.",
             "AI takes over tasks humans did before.", 0,
             "🧑‍🤝‍🧑 RESKILLING! Umschulung und neue Rollen!",
             "🧑‍🤝‍🧑 RESKILLING! Reskilling and new roles!"),
        ]
    },
]

def generate_options(game_title):
    """Generate appropriate options based on game type"""
    if "KI oder Mensch" in game_title:
        return ["Von KI geschrieben", "Von einem Menschen geschrieben"]
    elif "Fakt oder Halluzination" in game_title:
        return ["Wahr", "Halluzination"]
    elif "Hochrisiko" in game_title:
        return ["Hochrisiko/verboten", "Unbedenklich"]
    elif "Agenten-Ampel" in game_title:
        return ["Autonom ok", "Nur mit Aufsicht", "Nicht erlaubt"]
    elif "Shadow-AI" in game_title:
        return ["Shadow AI", "In Ordnung"]
    elif "Prompt-Profis" in game_title:
        return ["Schlechter Prompt", "Besserer Prompt"]
    elif "Bias-Spotter" in game_title:
        return ["Alters-Bias", "Geschlechter-Bias", "Sprach-Bias"]
    elif "DSGVO" in game_title:
        return ["DSGVO-konform", "Nicht konform"]
    elif "Sprachführer" in game_title:
        return ["Nicht in öffentliche KI", "Darf in die KI"]
    elif "Vertrag" in game_title:
        return ["AVV (Art. 28 DSGVO)", "Nur Werbebanner", "Mündliche Zusage"]
    elif "Zielscheibe" in game_title:
        return ["Guter KI-Einsatz", "Schlechter KI-Einsatz"]
    elif "Schutzschild" in game_title:
        return ["Schulung + klare Nutzungsregeln", "Mehr Rechenleistung", "Größeres Modell"]
    elif "Change" in game_title:
        return ["Transparent kommunizieren + Qualifizierung anbieten", "KI einführen ohne Ankündigung", "Angst ignorieren"]
    elif "Strategie" in game_title:
        return ["Fokus auf 2-3 Use Cases mit klarem ROI", "Alle Projekte parallel starten", "Kein Projekt starten"]
    elif "Audit" in game_title:
        return ["Human Oversight (Art. 14)", "Risikomanagement-System (Art. 9)", "Kennzeichnungspflicht (Art. 50)"]
    elif "Agent-Simulator" in game_title:
        return ["Voller Autonomiebetrieb", "Nur Entwürfe erstellen — Mensch sendet", "Agent bekommt keine Vorgaben"]
    else:
        return ["Option A", "Option B"]

def generate_kotlin_code():
    """Generate the complete Kotlin file"""
    
    header = '''package ai.ki_kompetenz_training_org.data.minigames

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

data class MiniGame(
    val id: String, 
    val emoji: String,
    val titleDe: String, val titleEn: String,
    val descriptionDe: String, val descriptionEn: String,
    val rounds: List<MiniGameRound>, 
    val premium: Boolean = false,
    val difficulty: Difficulty = Difficulty.BEGINNER,
) {
    fun title(lang: String): String = if (lang == "de") titleDe else titleEn
    fun description(lang: String): String = if (lang == "de") descriptionDe else descriptionEn
}

enum class Difficulty(val displayNameDe: String, val displayNameEn: String, val xpMultiplier: Float) {
    BEGINNER("Anfänger", "Beginner", 1.0f),
    INTERMEDIATE("Fortgeschritten", "Intermediate", 1.3f),
    EXPERT("Experte", "Expert", 1.5f)
}

/** Aktuelle App-Sprache (de oder en — Fallback de). */
fun currentLang(): String {
    val l = Locale.getDefault().language
    return if (l == "de") "de" else "en"
}

object MiniGames {

'''
    
    game_code = ""
    for game in games:
        options = generate_options(game['titleDe'])
        
        game_code += f'''    // ── {game['titleDe']} ──
    private val {game['id']} = MiniGame(
        id = "{game['id']}", emoji = "{game['emoji']}", 
        titleDe = "{game['titleDe']}", titleEn = "{game['titleEn']}",
        descriptionDe = "{game['descriptionDe']}", descriptionEn = "{game['descriptionEn']}",
        rounds = listOf(
'''
        
        for round_data in game['rounds']:
            prompt_de, prompt_en, correct, expl_de, expl_en = round_data
            opts_de = ', '.join([f'"{opt}"' for opt in options])
            opts_en = ', '.join([f'"{opt}"' for opt in options])
            
            game_code += f'''            MiniGameRound(
                promptDe = """{prompt_de}""",
                promptEn = """{prompt_en}""",
                optionsDe = listOf({opts_de}),
                optionsEn = listOf({opts_en}),
                correctIndex = {correct},
                explanationDe = """{expl_de}""",
                explanationEn = """{expl_en}""",
            ),
'''
        
        game_code += f'''        ),
        difficulty = Difficulty.{game['difficulty']},
        premium = {str(game['premium']).lower()},
    )

'''
    
    free_games = [g['id'] for g in games if not g['premium']]
    premium_games = [g['id'] for g in games if g['premium']]
    
    free_line = ",\n        ".join(free_games)
    premium_line = ",\n        ".join(premium_games)
    footer = '''
    val ALL: List<MiniGame> = listOf(
        // FREE GAMES (''' + str(len(free_games)) + ''')
        ''' + free_line + ''',
        // PREMIUM GAMES (''' + str(len(premium_games)) + ''')
        ''' + premium_line + ''',
    )
    
    val FREE: List<MiniGame> = ALL.filter { !it.premium }
    val PREMIUM: List<MiniGame> = ALL.filter { it.premium }
    
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
'''
    
    return header + game_code + footer

def main():
    output_path = "app/src/main/java/ai/ki_kompetenz_training_org/data/minigames/MiniGames.kt"
    
    kotlin_code = generate_kotlin_code()
    
    with open(output_path, 'w', encoding='utf-8') as f:
        f.write(kotlin_code)
    
    total_rounds = sum(len(g['rounds']) for g in games)
    free_count = len([g for g in games if not g['premium']])
    premium_count = len([g for g in games if g['premium']])
    
    print(f"✅ Successfully generated MiniGames.kt")
    print(f"   Games: {len(games)} total ({free_count} free + {premium_count} premium)")
    print(f"   Rounds: {total_rounds} total")
    print(f"   Output: {output_path}")

if __name__ == "__main__":
    main()
