package ai.platon.pulsar.agentic.inference.history

import ai.platon.pulsar.agentic.model.AgentHistory

fun interface HistoryRenderStrategy {
    fun render(agentHistory: AgentHistory, logPath: String?): String

    fun render(agentHistory: AgentHistory) = render(agentHistory, null)
}
