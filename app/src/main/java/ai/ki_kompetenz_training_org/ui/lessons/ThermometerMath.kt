/*
 * Copyright 2026 Tobias Weiss
 * SPDX-License-Identifier: Apache-2.0
 */
package ai.ki_kompetenz_training_org.ui.lessons

/**
 * Reine Mathematik/Label-Helfer für den EU-AI-Act-Risiko-Thermometer.
 * Absichtlich ohne Compose-Abhängigkeiten, damit die Logik unit-testbar ist
 * (junit-only, ohne Robolectric).
 *
 * Hintergrund (BUG 2026-09-01):
 * - Der Glow-Indikator benutzte `(level * 1f/levels * 260).dp` relativ zur
 *   Column-Unterseite → falsche Position + Layout-Verschiebung.
 *   Fix: [glowCenterFraction] liefert den relativen Segmentmittelpunkt
 *   (0f..1f), das Composable multipliziert mit der REAL gemessenen Höhe.
 * - "Beispiele:"/"Pflichten:" waren in beiden Locales deutsch.
 *   Fix: [examplesLabel]/[obligationsLabel].
 */
object ThermometerMath {

    /**
     * Relativer Mittelpunkt (0f..1f) des Segments [selectedLevel] innerhalb
     * von [totalLevels] gleich große Segmente.
     *
     * Beispiel: 4 Segmente, Level 0 → 0.125f (Mitte des oberen Viertels).
     *
     * @throws IllegalArgumentException bei Index außerhalb 0 until totalLevels
     *         oder totalLevels <= 0.
     */
    fun glowCenterFraction(selectedLevel: Int, totalLevels: Int): Float {
        require(totalLevels > 0) { "totalLevels must be > 0 (was $totalLevels)" }
        require(selectedLevel in 0 until totalLevels) {
            "selectedLevel $selectedLevel out of bounds for $totalLevels levels"
        }
        return (selectedLevel + 0.5f) / totalLevels
    }

    /** Label über der Beispielliste, sprachabhängig. */
    fun examplesLabel(locale: String): String =
        if (locale == "en") "Examples:" else "Beispiele:"

    /** Label über der Pflichtenliste, sprachabhängig. */
    fun obligationsLabel(locale: String): String =
        if (locale == "en") "Obligations:" else "Pflichten:"
}
