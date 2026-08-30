# Add arena_badge string to all 3 locales.
entries = [("games_arena_badge", "Echtzeit-Arena", "Real-time arena")]

def add_strings(path, de):
    with open(path, "r", encoding="utf-8") as f:
        content = f.read()
    if "games_arena_badge" in content:
        print(f"{path}: already present, skipping")
        return
    name, d, e = entries[0]
    val = d if de else e
    insert = f'    <string name="{name}">{val}</string>\n'
    assert content.count("</resources>") == 1, f"{path}: bad resource count"
    content = content.replace("</resources>", insert + "</resources>")
    with open(path, "w", encoding="utf-8") as f:
        f.write(content)
    print(f"{path}: added games_arena_badge")

base = r"C:/Users/Tobias/git/ki-kompetenz-android-free/app/src/main/res"
add_strings(base + "/values/strings.xml", de=True)
add_strings(base + "/values-de/strings.xml", de=True)
add_strings(base + "/values-en/strings.xml", de=False)
print("DONE")
