package ai.platon.pulsar.agentic.inference.history

import ai.platon.pulsar.agentic.model.AgentHistory

fun interface HistoryRenderStrategy {
    fun render(agentHistory: AgentHistory): String
}
