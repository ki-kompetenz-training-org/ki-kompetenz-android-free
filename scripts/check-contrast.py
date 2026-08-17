#!/usr/bin/env python3
"""WCAG 2.1 contrast ratio calculator for app color combinations"""

import math

def hex_to_rgb(hex_color):
    hex_color = hex_color.lstrip('#')
    return tuple(int(hex_color[i:i+2], 16)/255.0 for i in (0, 2, 4))

def relative_luminance(rgb):
    def linearize(c):
        if c <= 0.04045:
            return c / 12.92
        return ((c + 0.055) / 1.055) ** 2.4
    r, g, b = [linearize(c) for c in rgb]
    return 0.2126 * r + 0.7152 * g + 0.0722 * b

def contrast_ratio(hex1, hex2):
    l1 = relative_luminance(hex_to_rgb(hex1))
    l2 = relative_luminance(hex_to_rgb(hex2))
    lighter = max(l1, l2)
    darker = min(l1, l2)
    return (lighter + 0.05) / (darker + 0.05)

combos = [
    ('#FFFFFF', '#2563EB', 'White on Blue hero'),
    ('#FFFFFF', '#4F46E5', 'White on Indigo hero'),
    ('#FFFFFF', '#7C3AED', 'White on Purple hero'),
    ('#FDE68A', '#2563EB', 'Gold subtitle on Blue'),
    ('#FFFFFF', '#D97706', 'White on Amber button'),
    ('#1D4ED8', '#FFFFFF', 'Blue text on White button'),
    ('#DC2626', '#FFFFFF', 'Red error on White'),
    ('#16A34A', '#FFFFFF', 'Green check on White'),
    ('#EF4444', '#FFFFFF', 'Red badge on White'),
    ('#0A66C2', '#FFFFFF', 'Blue share on White'),
]

print('=== WCAG 2.1 AA Contrast Checks (4.5:1 text, 3:1 large text) ===')
print(f'{"Combination":<35} {"Ratio":>6} {"AA Text":>8} {"AA Large":>9}')
print('-'*60)
all_pass = True
for fg, bg, label in combos:
    ratio = contrast_ratio(fg, bg)
    text_pass = ratio >= 4.5
    large_pass = ratio >= 3.0
    if not large_pass:
        all_pass = False
    status = "PASS" if text_pass else ("LARGE" if large_pass else "FAIL")
    print(f'{label:<35} {ratio:>6.2f}:1 {status:>8} {"PASS" if large_pass else "FAIL":>9}')
print()
print(f'Overall: {"ALL PASS" if all_pass else "CHECK FAILURES"}')
