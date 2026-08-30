# Add arena game strings to all 3 locales cleanly.
entries = [
    ("games_start", "Starten", "Start"),
    ("games_score", "Punkte", "Score"),
    ("games_time", "Zeit", "Time"),
    ("games_streak", "Serie", "Streak"),
    ("games_retry", "Nochmal", "Retry"),
    ("games_arena_howto_orb",
     "Scanne Orbs, lies die KI-Aussage und entscheide: Fakt oder Risiko?",
     "Scan orbs, read the AI statement and decide: fact or risk?"),
    ("games_arena_howto_maze",
     "Steuere zum gr\u00fcnen Ziel. Dort wartet eine KI-Aussage: Fakt oder Risiko?",
     "Steer to the green goal. An AI statement awaits there: fact or risk?"),
    ("games_arena_howto_snipe",
     "Sammle blaue Fakten, zerst\u00f6re rote Falschmeldungen mit Feuer. Triff keine Fakten!",
     "Collect blue facts, blast red fakes with fire. Do not hit facts!"),
    ("games_arena_won", "Geschafft!", "Achieved!"),
    ("games_arena_lost", "Fast! Nochmal versuchen", "Close! Try again"),
    ("games_arena_classified", "Eingestuft: %1$d von %2$d richtig", "Classified: %1$d of %2$d correct"),
    ("games_arena_review", "Zum Wiederholen empfohlen", "Recommended for review"),
    ("games_arena_xp", "Punkte: %1$d / Ziel %2$d  |  XP +%3$d", "Score: %1$d / Target %2$d  |  XP +%3$d"),
]

def add_strings(path, de):
    with open(path, "r", encoding="utf-8") as f:
        content = f.read()
    if "games_start" in content:
        print(f"{path}: already present, skipping")
        return
    insert = ""
    for name, d, e in entries:
        val = d if de else e
        insert += f'    <string name="{name}">{val}</string>\n'
    assert content.count("</resources>") == 1, f"{path}: bad resource count"
    content = content.replace("</resources>", insert + "</resources>")
    with open(path, "w", encoding="utf-8") as f:
        f.write(content)
    print(f"{path}: added {len(entries)} strings")

base = r"C:/Users/Tobias/git/ki-kompetenz-android-free/app/src/main/res"
add_strings(base + "/values/strings.xml", de=True)
add_strings(base + "/values-de/strings.xml", de=True)
add_strings(base + "/values-en/strings.xml", de=False)
print("DONE")
