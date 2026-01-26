@file:Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE", "unused")

package ai.platon.pulsar.agentic.event

import ai.platon.pulsar.agentic.ActionOptions
import ai.platon.pulsar.agentic.ObserveOptions
import ai.platon.pulsar.agentic.inference.detail.ExecutionContext
import ai.platon.pulsar.agentic.model.AgentState
import ai.platon.pulsar.common.lang.AbstractChainedFunction1
import ai.platon.pulsar.common.lang.AbstractChainedPDFunction2
import ai.platon.pulsar.skeleton.crawl.ServerSideEvent
import ai.platon.pulsar.skeleton.crawl.ServerSideEventHandlers
import kotlinx.coroutines.flow.SharedFlow

open class ObserveEventHandler : AbstractChainedFunction1<ObserveOptions, Any?>() {
    override fun invoke(options: ObserveOptions): Any? {
        return super.invoke(param = options)
    }
}

open class ActEventHandler : AbstractChainedFunction1<ActionOptions, Any?>() {
    override fun invoke(options: ActionOptions): Any? {
        return super.invoke(param = options)
    }
}

open class ExecutionContextAgentStateEventHandler : AbstractChainedPDFunction2<ExecutionContext, AgentState, Any?>() {
    override suspend fun invoke(context: ExecutionContext, agentState: AgentState): Any? {
        return super.invoke(param = context, param2 = agentState)
    }
}

interface AgentFlowEventHandlers {
    val onWillObserve: ObserveEventHandler
    val onDidObserve: ObserveEventHandler

    val onWillAct: ActEventHandler
    val onDidAct: ActEventHandler

    val onInferenceWillObserve: ExecutionContextAgentStateEventHandler
    val onInferenceDidObserve: ExecutionContextAgentStateEventHandler
}

interface ToolCallEventHandlers {
}

interface MCPEventHandlers {
}

interface SkillEventHandlers {
}

interface ServerSideAgentEventHandlers {
    /**
     * The shared flow of server-side events.
     * Subscribers can collect from this flow to receive all events.
     */
    val eventFlow: SharedFlow<ServerSideEvent>
}

interface AgentEventHandlers {
    var agentFlowHandlers: AgentFlowEventHandlers
    var toolCallEventHandlers: ToolCallEventHandlers
    var mcpEventHandlers: MCPEventHandlers
    var skillEventHandlers: SkillEventHandlers
    var serverSideEventHandlers: ServerSideEventHandlers
}

