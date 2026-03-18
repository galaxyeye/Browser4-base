package ai.platon.pulsar.agentic.model

import ai.platon.browser4.driver.chrome.dom.model.BrowserUseState
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class AgentHistoryJacksonSerializationTest {

    private val mapper = jacksonObjectMapper()

    @Test
    @DisplayName("agent history jackson serialization only includes states")
    fun agentHistoryJacksonSerializationOnlyIncludesStates() {
        val history = AgentHistory(
            mutableListOf(
                AgentState(
                    step = 1,
                    instruction = "Open example.com",
                    browserUseState = BrowserUseState.DUMMY,
                )
            )
        )

        val node = mapper.readTree(mapper.writeValueAsString(history))

        assertEquals(1, node.size())
        assertTrue(node.has("states"))
    }
}

