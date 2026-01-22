package ai.platon.pulsar.examples.agent

import ai.platon.pulsar.agentic.context.AgenticContexts

suspend fun main() {
    val agent = AgenticContexts.getOrCreateAgent()

    // Demostrate how to use MCP agent here
    val task = """
        """.trimIndent()

    val history = agent.run(task)
    println(history.finalResult)
}
