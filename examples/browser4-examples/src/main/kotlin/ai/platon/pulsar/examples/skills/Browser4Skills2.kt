package ai.platon.pulsar.examples.skills

import ai.platon.pulsar.agentic.context.AgenticContexts

suspend fun main() {
    val agent = AgenticContexts.getOrCreateAgent()

    // Use agent with a skill-oriented task
    val task = """
        Use skill.debug.scraping.extract to scrape https://example.com
        """.trimIndent()

    val history = agent.run(task)
    println("Agent result: ${history.finalResult}")
}
