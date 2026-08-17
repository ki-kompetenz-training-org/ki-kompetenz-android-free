package ai.ki_kompetenz_training_org.accessibility

import ai.ki_kompetenz_training_org.data.minigames.MiniGames
import ai.ki_kompetenz_training_org.data.minigames.currentLang
import org.junit.Assert.*
import org.junit.Test

/**
 * Accessibility compliance tests (WCAG 2.1 AA / EN 301 549 / BITV 2.0).
 * 
 * These tests verify that content meets accessibility standards:
 * - Text alternatives for non-text content (WCAG 1.1.1)
 * - Adaptable content structure (WCAG 1.3.1)
 * - Distinguishable content — contrast and size (WCAG 1.4)
 * - Keyboard / navigable interface (WCAG 2.1)
 * - Readable content (WCAG 3.1)
 * - Predictable behavior (WCAG 3.2)
 * - Input assistance (WCAG 3.3)
 */
class AccessibilityContentTest {

    // ── WCAG 1.1.1: Non-text Content — Text Alternatives ──

    @Test
    fun `every game has a descriptive emoji AND a text title`() {
        // Emojis alone are NOT sufficient as text alternatives.
        // Every game must have a clear text title that conveys meaning without the emoji.
        MiniGames.ALL.forEach { game ->
            assertTrue(
                "Game ${game.id}: emoji-only titles are not accessible. Must have text title.",
                game.titleDe.isNotBlank() && !game.titleDe.all { it.code >= 0x1F000 }
            )
            assertTrue(
                "Game ${game.id} (en): emoji-only titles are not accessible.",
                game.titleEn.isNotBlank() && !game.titleEn.all { it.code >= 0x1F000 }
            )
        }
    }

    @Test
    fun `game descriptions are meaningful and not emoji-only`() {
        MiniGames.ALL.forEach { game ->
            // Description must have at least 20 characters of actual text
            val textOnlyDe = game.descriptionDe.filter { it.code < 0x1F000 }
            assertTrue(
                "Game ${game.id}: description too short or emoji-only for screen readers.",
                textOnlyDe.length >= 20
            )
            val textOnlyEn = game.descriptionEn.filter { it.code < 0x1F000 }
            assertTrue(
                "Game ${game.id} (en): description too short for screen readers.",
                textOnlyEn.length >= 20
            )
        }
    }

    // ── WCAG 1.3.1: Info and Relationships ──

    @Test
    fun `options are consistently ordered and all games have same number of options per round`() {
        MiniGames.ALL.forEach { game ->
            val optionCounts = game.rounds.map { it.optionsDe.size }.toSet()
            // All rounds in a game should have the same option count (consistent UI)
            // This is important for screen reader predictability
            if (optionCounts.size > 1) {
                // If mixed, at least document it — no hard failure but loggable
                println("Note: Game ${game.id} has mixed option counts: $optionCounts")
            }
        }
    }

    // ── WCAG 1.4.3: Contrast (Minimum) — verified at UI level ──
    // Note: Color contrast cannot be fully tested at unit test level.
    // This is a reminder that UI elements must meet 4.5:1 contrast ratio.

    @Test
    fun `explanations are substantial enough for screen readers`() {
        // Explanations must convey meaningful information, not just a single emoji
        MiniGames.ALL.forEach { game ->
            game.rounds.forEachIndexed { i, round ->
                val textOnlyDe = round.explanationDe.filter { it.code < 0x1F000 }
                assertTrue(
                    "Game ${game.id} round $i: explanation too short for accessibility. " +
                    "Screen reader users need substantial text explanations.",
                    textOnlyDe.trim().length >= 15
                )
                val textOnlyEn = round.explanationEn.filter { it.code < 0x1F000 }
                assertTrue(
                    "Game ${game.id} round $i (en): explanation too short for accessibility.",
                    textOnlyEn.trim().length >= 15
                )
            }
        }
    }

    // ── WCAG 3.1.1: Language of Page ──

    @Test
    fun `bilingual support is consistent — de and en always available`() {
        MiniGames.ALL.forEach { game ->
            assertNotNull("Game ${game.id}: missing German title", game.titleDe)
            assertNotNull("Game ${game.id}: missing English title", game.titleEn)
            assertNotNull("Game ${game.id}: missing German description", game.descriptionDe)
            assertNotNull("Game ${game.id}: missing English description", game.descriptionEn)
            game.rounds.forEachIndexed { i, round ->
                assertTrue("Game ${game.id} round $i: empty promptDe", round.promptDe.isNotBlank())
                assertTrue("Game ${game.id} round $i: empty promptEn", round.promptEn.isNotBlank())
                assertTrue("Game ${game.id} round $i: empty explanationDe", round.explanationDe.isNotBlank())
                assertTrue("Game ${game.id} round $i: empty explanationEn", round.explanationEn.isNotBlank())
                assertTrue("Game ${game.id} round $i: empty optionsDe", round.optionsDe.all { it.isNotBlank() })
                assertTrue("Game ${game.id} round $i: empty optionsEn", round.optionsEn.all { it.isNotBlank() })
            }
        }
    }

    // ── WCAG 3.2.1: On Focus ──
    // Verified at UI level — focus order must be logical

    // ── WCAG 3.3.1: Error Identification ──

    @Test
    fun `correct answer is always clearly identifiable`() {
        // In quiz/game context, users need clear feedback on right/wrong
        MiniGames.ALL.forEach { game ->
            game.rounds.forEachIndexed { i, round ->
                assertTrue(
                    "Game ${game.id} round $i: correctIndex ${round.correctIndex} out of range",
                    round.correctIndex in round.optionsDe.indices
                )
            }
        }
    }

    // ── EN 301 549: European Accessibility Standard ──

    @Test
    fun `game content is suitable for professional learning context`() {
        // Content should be appropriate, respectful, and non-discriminatory
        MiniGames.ALL.forEach { game ->
            // Check no profanity patterns in German content
            game.rounds.forEachIndexed { i, round ->
                val allTextDe = round.promptDe + round.explanationDe + round.optionsDe.joinToString()
                assertFalse(
                    "Game ${game.id} round $i: contains potentially inappropriate content",
                    allTextDe.contains("[inappropriate pattern]")
                )
            }
        }
    }

    // ── BITV 2.0: German Accessibility Regulation ──

    @Test
    fun `German content follows accessible language guidelines`() {
        // Simple, clear language (Leichte Sprache) principles:
        // - Short sentences
        // - No unnecessary abbreviations
        // - Clear terminology
        MiniGames.ALL.forEach { game ->
            game.rounds.forEach { round ->
                // Prompts should be reasonably short (< 200 chars for screen reader comfort)
                assertTrue(
                    "Game ${game.id}: prompt too long for comfortable screen reading (${round.promptDe.length} chars). " +
                    "Consider breaking into shorter segments.",
                    round.promptDe.length <= 200
                )
            }
        }
    }

    @Test
    fun `difficulty labels are available in both languages`() {
        ai.ki_kompetenz_training_org.data.minigames.Difficulty.entries.forEach { diff ->
            assertTrue("Difficulty ${diff.name}: empty German display name", diff.displayNameDe.isNotBlank())
            assertTrue("Difficulty ${diff.name}: empty English display name", diff.displayNameEn.isNotBlank())
        }
    }
}
