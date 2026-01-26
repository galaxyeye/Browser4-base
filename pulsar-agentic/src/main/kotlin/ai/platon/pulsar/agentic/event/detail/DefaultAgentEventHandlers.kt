package ai.platon.pulsar.agentic.event.detail

import ai.platon.pulsar.agentic.event.ActEventHandler
import ai.platon.pulsar.agentic.event.AgentFlowEventHandlers
import ai.platon.pulsar.agentic.event.ExecutionContextAgentStateEventHandler
import ai.platon.pulsar.agentic.event.ObserveEventHandler

class DefaultAgentFlowEventHandlers: AgentFlowEventHandlers {
    override val onWillObserve: ObserveEventHandler = ObserveEventHandler()
    override val onDidObserve: ObserveEventHandler = ObserveEventHandler()

    override val onWillAct: ActEventHandler = ActEventHandler()
    override val onDidAct: ActEventHandler = ActEventHandler()

    override val onInferenceWillObserve: ExecutionContextAgentStateEventHandler = ExecutionContextAgentStateEventHandler()
    override val onInferenceDidObserve: ExecutionContextAgentStateEventHandler = ExecutionContextAgentStateEventHandler()
}
