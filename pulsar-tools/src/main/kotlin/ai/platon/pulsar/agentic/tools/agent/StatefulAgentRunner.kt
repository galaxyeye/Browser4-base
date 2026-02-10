package ai.platon.pulsar.agentic.tools.agent

import ai.platon.pulsar.agentic.AgenticSession
import ai.platon.pulsar.common.ResourceStatus
import ai.platon.pulsar.common.concurrent.ConcurrentExpiringLRUCache
import ai.platon.pulsar.common.getLogger
import kotlinx.coroutines.*
import java.time.Duration
import java.util.concurrent.Executors

class StatefulAgentRunner(
    val session: AgenticSession
) {
    private val logger = getLogger(StatefulAgentRunner::class)

    // Create a dedicated dispatcher for long-running command operations
    private val scrapingExecutor = Executors.newFixedThreadPool(10)
    private val commandDispatcher = scrapingExecutor.asCoroutineDispatcher()
    private val commanderScope: CoroutineScope = CoroutineScope(
        commandDispatcher + SupervisorJob() + CoroutineName("commander")
    )
    private val statusCache = ConcurrentExpiringLRUCache<String, AgentTaskStatus>(Duration.ofHours(2))

    fun create(): AgentTaskStatus {
        return createCachedStatus()
    }

    fun submit(plainCommand: String): AgentTaskStatus {
        val status = createCachedStatus()
        commanderScope.launch { execute(plainCommand, status) }
        return status
    }

    /**
     * Execute a plain command using the agent's run method.
     *
     * This method creates a cached status, executes the agent's run method, and updates
     * the status with the result.
     *
     * @param plainCommand The plain text command for the agent to execute.
     * @return AgentStatus containing the execution result.
     */
    suspend fun execute(plainCommand: String): AgentTaskStatus {
        val status = createCachedStatus()
        execute(plainCommand, status)
        return status
    }

    /**
     * Internal method to execute agent command with a pre-created status.
     *
     * The status is updated with the agent's state history reference, allowing callers
     * to access the latest agent state via [AgentTaskStatus.currentAgentState] during execution.
     */
    suspend fun execute(plainCommand: String, status: AgentTaskStatus) {
        try {
            status.refresh(ResourceStatus.SC_PROCESSING)
            val agent = session.companionAgent

            // Set agent history reference to allow real-time state tracking
            status.agentHistory = agent.stateHistory

            val history = agent.run(plainCommand)
            val finalState = history.finalResult

            // AgentState has 'summary' for the final result message
            val resultSummary = finalState?.summary ?: finalState?.description ?: ""
            status.message = resultSummary
            status.refresh(ResourceStatus.SC_OK)
        } catch (e: Exception) {
            logger.error("Failed to execute agent command: {}", plainCommand, e)
            status.failed(ResourceStatus.SC_EXPECTATION_FAILED)
            status.message = e.message
        } finally {
            status.done()
        }
    }

    fun getStatus(id: String) = statusCache.getDatum(id)

    fun getResult(id: String) = statusCache.getDatum(id)?.agentHistory?.lastOrNull()

    private fun createCachedStatus(): AgentTaskStatus {
        val status = AgentTaskStatus()
        statusCache.putDatum(status.id, status)
        status.emitEvent("StatefulAgentRunner.created")
        return status
    }
}
