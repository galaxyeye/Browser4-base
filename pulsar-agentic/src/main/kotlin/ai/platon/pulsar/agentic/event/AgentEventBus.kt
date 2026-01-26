package ai.platon.pulsar.agentic.event

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

object AgentEventBus {
    private val eventScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    var agentEventHandlers: AgentEventHandlers? = null
    var serverSideAgentEventHandlers: ServerSideAgentEventHandlers? = null
}
