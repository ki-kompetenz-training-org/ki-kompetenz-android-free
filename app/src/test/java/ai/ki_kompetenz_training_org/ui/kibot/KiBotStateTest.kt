package ai.ki_kompetenz_training_org.ui.kibot

import org.junit.Assert.assertEquals
import org.junit.Test

class KiBotStateTest {
    @Test fun `growth stage is Neonate for levels 1-2`() {
        assertEquals(GrowthStage.NEONATE, GrowthStage.forLevel(1))
        assertEquals(GrowthStage.NEONATE, GrowthStage.forLevel(2))
    }
    @Test fun `growth stage is Learner for levels 3-5`() {
        assertEquals(GrowthStage.LEARNER, GrowthStage.forLevel(3))
        assertEquals(GrowthStage.LEARNER, GrowthStage.forLevel(5))
    }
    @Test fun `growth stage is Thinker for levels 6-9`() {
        assertEquals(GrowthStage.THINKER, GrowthStage.forLevel(6))
        assertEquals(GrowthStage.THINKER, GrowthStage.forLevel(9))
    }
    @Test fun `growth stage is Expert for levels 10-14`() {
        assertEquals(GrowthStage.EXPERT, GrowthStage.forLevel(10))
        assertEquals(GrowthStage.EXPERT, GrowthStage.forLevel(14))
    }
    @Test fun `growth stage is Expert for levels above 14`() {
        assertEquals(GrowthStage.EXPERT, GrowthStage.forLevel(99))
    }
    @Test fun `emotional baseline is sleepy when not checked in for 2+ days`() {
        assertEquals(EmotionalState.SLEEPY, EmotionalState.baseline(streak = 0, daysSinceCheckIn = 3, checkedInToday = false))
    }
    @Test fun `emotional baseline is sleepy when streak is 0 and not checked in`() {
        assertEquals(EmotionalState.SLEEPY, EmotionalState.baseline(streak = 0, daysSinceCheckIn = 2, checkedInToday = false))
    }
    @Test fun `emotional baseline is idle when checked in today`() {
        assertEquals(EmotionalState.IDLE, EmotionalState.baseline(streak = 5, daysSinceCheckIn = 0, checkedInToday = true))
    }
    @Test fun `emotional baseline is idle when checked in yesterday with streak`() {
        assertEquals(EmotionalState.IDLE, EmotionalState.baseline(streak = 3, daysSinceCheckIn = 1, checkedInToday = false))
    }
    @Test fun `KiBotState derives from gamification data`() {
        val state = KiBotState.from(
            level = 4, xp = 200, xpIntoLevel = 100, xpNeeded = 300,
            streak = 3, daysSinceCheckIn = 0, checkedInToday = true,
        )
        assertEquals(GrowthStage.LEARNER, state.growthStage)
        assertEquals(EmotionalState.IDLE, state.emotionalBaseline)
        assertEquals(4, state.level)
        assertEquals(200, state.xp)
    }
    @Test fun `KiBotState handles sleepy user`() {
        val state = KiBotState.from(
            level = 2, xp = 50, xpIntoLevel = 0, xpNeeded = 100,
            streak = 0, daysSinceCheckIn = 5, checkedInToday = false,
        )
        assertEquals(GrowthStage.NEONATE, state.growthStage)
        assertEquals(EmotionalState.SLEEPY, state.emotionalBaseline)
    }
}
