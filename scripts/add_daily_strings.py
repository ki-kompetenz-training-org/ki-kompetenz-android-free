#!/usr/bin/env python3
"""Add daily challenge strings to values-de and values-en."""
import sys

de_strings = [
    '    <string name="daily_challenge_title">T\u00e4gliche Herausforderung</string>',
    '    <string name="daily_challenge_subtitle">Spiele das Mini-Spiel des Tages</string>',
    '    <string name="daily_challenge_start">Starten</string>',
    '    <string name="daily_challenge_completed">Heute erledigt!</string>',
    '    <string name="daily_challenge_come_back">Morgen wieder da. Streak: %1$d</string>',
    '    <string name="daily_challenge_streak">Streak: %1$d</string>',
    '    <string name="daily_challenge_xp_preview">+%1$d XP</string>',
    '    <string name="daily_challenge_xp_earned">+%1$d XP verdient!</string>',
]

en_strings = [
    '    <string name="daily_challenge_title">Daily Challenge</string>',
    '    <string name="daily_challenge_subtitle">Play today\\\'s mini-game challenge</string>',
    '    <string name="daily_challenge_start">Start</string>',
    '    <string name="daily_challenge_completed">Done for today!</string>',
    '    <string name="daily_challenge_come_back">Come back tomorrow. Streak: %1$d</string>',
    '    <string name="daily_challenge_streak">Streak: %1$d</string>',
    '    <string name="daily_challenge_xp_preview">+%1$d XP</string>',
    '    <string name="daily_challenge_xp_earned">+%1$d XP earned!</string>',
]

def update_file(path, strings):
    with open(path, 'r', encoding='utf-8') as f:
        content = f.read()
    old_end = '    <string name="notif_permission_grant">Benachrichtigungen erlauben</string>\n</resources>'
    new_end = '    <string name="notif_permission_grant">Benachrichtigungen erlauben</string>\n' + '\n'.join(strings) + '\n</resources>'
    if old_end not in content:
        # Try English variant
        old_end = '    <string name="notif_permission_grant">Allow notifications</string>\n</resources>'
        new_end = '    <string name="notif_permission_grant">Allow notifications</string>\n' + '\n'.join(strings) + '\n</resources>'
    content = content.replace(old_end, new_end)
    with open(path, 'w', encoding='utf-8') as f:
        f.write(content)
    print(f"Updated {path}")

update_file('app/src/main/res/values-de/strings.xml', de_strings)
update_file('app/src/main/res/values-en/strings.xml', en_strings)
