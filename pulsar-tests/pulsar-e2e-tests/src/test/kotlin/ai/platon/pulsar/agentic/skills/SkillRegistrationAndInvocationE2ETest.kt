package ai.platon.pulsar.agentic.skills

import ai.platon.pulsar.agentic.context.AgenticContexts
import ai.platon.pulsar.agentic.event.AgenticEvents
import ai.platon.pulsar.agentic.model.ActionDescription
import ai.platon.pulsar.agentic.model.ToolCall
import ai.platon.pulsar.agentic.skills.examples.WebScrapingSkill
import ai.platon.pulsar.agentic.skills.tools.SkillToolExecutor
import ai.platon.pulsar.agentic.tools.CustomToolRegistry
import ai.platon.pulsar.common.event.EventBus
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import java.util.concurrent.ConcurrentHashMap

/**
 * End-to-end test for skill registration and invocation.
 *
 * This test validates the complete skill integration by:
 * 1. Registering a skill to SkillRegistry
 * 2. Wiring the skill to CustomToolRegistry via SkillToolExecutor
 * 3. Creating an agent that can use the skill
 * 4. Running a task that invokes the skill
 * 5. Capturing the GENERATE_DID_EXECUTE event to verify the skill was called
 * 6. Validating that the actionDescription.toolCall is skill-related
 */
@Tag("E2ETest")
@Tag("skills")
class SkillRegistrationAndInvocationE2ETest {

    companion object {
        private val capturedEvents = ConcurrentHashMap<String, MutableList<Map<String, Any?>>>()
        
        @BeforeAll
        @JvmStatic
        fun setupEventHandlers() {
            // Register event handler for GENERATE_DID_EXECUTE
            EventBus.register(AgenticEvents.ContextToAction.GENERATE_DID_EXECUTE) { payload ->
                val map = payload as? Map<String, Any?> ?: return@register null
                capturedEvents.computeIfAbsent(AgenticEvents.ContextToAction.GENERATE_DID_EXECUTE) { 
                    mutableListOf() 
                }.add(map)
                payload
            }
        }

        @AfterAll
        @JvmStatic
        fun cleanup() {
            // Unregister event handler
            EventBus.unregister(AgenticEvents.ContextToAction.GENERATE_DID_EXECUTE)
        }
    }

    private lateinit var registry: SkillRegistry
    private lateinit var context: SkillContext

    @BeforeEach
    fun setup() = runBlocking {
        // Clear captured events
        capturedEvents.clear()
        
        // Initialize skill registry
        registry = SkillRegistry.instance
        context = SkillContext(sessionId = "e2e-test-session-${System.currentTimeMillis()}")
        registry.clear(context)
        
        // Unregister custom tool to ensure clean state
        CustomToolRegistry.instance.unregister("skill")
    }

    @AfterEach
    fun tearDown() = runBlocking {
        // Clean up skill registry
        registry.clear(context)
        
        // Unregister custom tool
        CustomToolRegistry.instance.unregister("skill")
    }

    @Test
    fun `test skill registration and invocation through agent`() = runBlocking {
        // Step 1: Register WebScrapingSkill
        val webScrapingSkill = WebScrapingSkill()
        registry.register(webScrapingSkill, context)
        
        // Verify skill is registered
        assertTrue(registry.contains("web-scraping"), "Skill should be registered")
        
        // Step 2: Wire skill tools to CustomToolRegistry
        val skillDomain = "skill.debug.scraping"
        val customRegistry = CustomToolRegistry.instance
        if (!customRegistry.contains(skillDomain)) {
            customRegistry.register(SkillToolExecutor(registry))
        }
        
        // Verify custom tool is registered
        assertTrue(customRegistry.contains(skillDomain), "Custom tool should be registered")
        
        // Step 3: Create agent
        val agent = AgenticContexts.getOrCreateAgent()
        assertNotNull(agent, "Agent should be created")
        
        // Step 4: Run agent with a skill-oriented task
        val task = """
            Use skill.debug.scraping to scrape https://agentskills.io/specification
        """.trimIndent()
        
        val history = agent.run(task)
        
        // Allow time for event processing
        Thread.sleep(500)
        
        // Step 5: Verify the agent ran and completed
        assertNotNull(history, "History should not be null")
        assertTrue(history.size > 0, "History should contain at least one execution")
        
        // Step 6: Verify GENERATE_DID_EXECUTE event was captured
        val events = capturedEvents[AgenticEvents.ContextToAction.GENERATE_DID_EXECUTE]
        assertNotNull(events, "GENERATE_DID_EXECUTE events should be captured")
        assertTrue(events!!.isNotEmpty(), "At least one GENERATE_DID_EXECUTE event should be captured")
        
        // Step 7: Extract actionDescription from the captured events
        val actionDescriptions = events.mapNotNull { it["actionDescription"] as? ActionDescription }
        assertTrue(actionDescriptions.isNotEmpty(), "At least one ActionDescription should be present")
        
        // Step 8: Verify that at least one actionDescription contains a skill-related toolCall
        val hasSkillToolCall = actionDescriptions.any { actionDescription ->
            val toolCall = actionDescription.toolCall
            toolCall != null && isSkillRelatedToolCall(toolCall)
        }
        
        assertTrue(hasSkillToolCall, "At least one ActionDescription should have a skill-related toolCall")
    }

    /**
     * Determines if a toolCall is related to skills.
     * A toolCall is considered skill-related if:
     * - The domain starts with "skill"
     * - The domain contains "skill.debug.scraping"
     */
    private fun isSkillRelatedToolCall(toolCall: ToolCall): Boolean {
        return toolCall.domain.startsWith("skill") ||
               toolCall.domain.contains("skill.debug.scraping")
    }
}
