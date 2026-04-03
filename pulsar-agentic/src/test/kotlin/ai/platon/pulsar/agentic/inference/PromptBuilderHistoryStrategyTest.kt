package ai.platon.pulsar.agentic.inference

import ai.platon.browser4.driver.chrome.dom.model.BrowserUseState
import ai.platon.pulsar.agentic.model.AgentHistory
import ai.platon.pulsar.agentic.model.AgentState
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class PromptBuilderHistoryStrategyTest {

    @Test
    fun emptyHistoryRendersEmptyMessage() {
        val builder = PromptBuilder()

        assertEquals("", builder.buildAgentStateHistoryMessage(AgentHistory()))
    }

    @Test
    fun smallHistoryKeepsDetailedEntries() {
        val builder = PromptBuilder()
        val history = AgentHistory(
            mutableListOf(
                createState(1, "navigate", nextGoal = "Open the product page"),
                createState(2, "click", nextGoal = "Inspect the details tab")
            )
        )

        val rendered = builder.buildAgentStateHistoryMessage(history)

        assertTrue(rendered.contains("showing all 2 recorded steps"))
        assertTrue(rendered.contains("\"step\":1"))
        assertTrue(rendered.contains("\"step\":2"))
        assertTrue(rendered.contains("### Recent Steps"))
    }

    @Test
    fun largeHistoryCompressesEarlierStepsAndKeepsRecentOnes() {
        val builder = PromptBuilder()
        val history = AgentHistory(
            (1..12).mapTo(mutableListOf()) { step ->
                createState(step, "click", nextGoal = "goal-$step")
            }
        )

        val rendered = builder.buildAgentStateHistoryMessage(history)

        assertTrue(rendered.contains("### Earlier Steps Summary"))
        assertTrue(rendered.contains("Older steps remain available in persisted task logs and checkpoints."))
        assertTrue(rendered.contains("\"step\":12"))
        assertTrue(rendered.contains("goal-12"))
    }

    private fun createState(step: Int, method: String, nextGoal: String): AgentState {
        return AgentState(
            step = step,
            instruction = "Do something",
            browserUseState = BrowserUseState.DUMMY,
            method = method,
            nextGoal = nextGoal,
        )
    }
}
