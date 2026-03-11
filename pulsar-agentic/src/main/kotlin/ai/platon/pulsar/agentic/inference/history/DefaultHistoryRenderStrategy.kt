package ai.platon.pulsar.agentic.inference.history

import ai.platon.pulsar.agentic.model.AgentHistory
import ai.platon.pulsar.agentic.model.AgentState
import ai.platon.pulsar.common.Strings
import ai.platon.pulsar.common.serialize.json.Pson

class DefaultHistoryRenderStrategy(
    private val maxCharacters: Int = 4_000,
    private val recentStepsToKeep: Int = 6,
    private val summaryStepLimit: Int = 6,
) : HistoryRenderStrategy {

    override fun render(agentHistory: AgentHistory): String {
        val history = agentHistory.states
        if (history.isEmpty()) {
            return ""
        }

        val recentStates = history.takeLast(recentStepsToKeep)
        val olderStates = history.dropLast(recentStates.size)
        val summarizedOlderStates = summarizeOlderStates(olderStates)
        val recentJsonl = recentStates.joinToString("\n") { renderDetailedState(it) }

        val sections = buildList {
            add("## Execution History")
            add("")
            add(
                if (olderStates.isEmpty()) {
                    "(showing all ${history.size} recorded steps)"
                } else {
                    "(compressed ${olderStates.size} earlier steps, keeping ${recentStates.size} most recent steps in detail)"
                }
            )
            add("")
            if (summarizedOlderStates.isNotEmpty()) {
                add("### Earlier Steps Summary")
                add(summarizedOlderStates)
                add("")
            }
            add("### Recent Steps")
            add("<agent_history>")
            add(recentJsonl)
            add("</agent_history>")
            if (olderStates.isNotEmpty()) {
                add("")
                add("Older steps remain available in persisted task logs and checkpoints.")
            }
            add("")
            add("---")
        }

        val rendered = sections.joinToString("\n")
        return trimToBudget(rendered, history.size, olderStates.isNotEmpty())
    }

    private fun summarizeOlderStates(states: List<AgentState>): String {
        if (states.isEmpty()) {
            return ""
        }

        return states
            .chunked(summaryStepLimit)
            .joinToString("\n") { chunk ->
                val start = chunk.first().step
                val end = chunk.last().step
                val methods = chunk.mapNotNull { it.method }.distinct().joinToString(", ").ifBlank { "N/A" }
                val nextGoals = chunk.mapNotNull { it.nextGoal }.take(2).joinToString(" | ")
                val failures = chunk.count { it.hasErrors }
                val notes = listOfNotNull(
                    "\"stepRange\":\"$start-$end\"",
                    "\"methods\":\"${Strings.compactInline(methods, 80)}\"",
                    "\"failures\":$failures",
                    nextGoals.takeIf { it.isNotBlank() }?.let { "\"nextGoals\":\"${escapeJson(Strings.compactInline(it, 200))}\"" }
                )
                "{${notes.joinToString(",")}}"
            }
    }

    private fun renderDetailedState(state: AgentState): String {
        return Pson.toJson(
            mapOf(
                "step" to state.step,
                "toolCall" to state.actionDescription?.pseudoExpression,
                "nextGoal" to state.nextGoal,
                "thinking" to state.thinking,
                "exception" to state.exception?.message,
                "summary" to state.summary,
                "keyFindings" to state.keyFindings
            )
        )
    }

    private fun trimToBudget(rendered: String, totalSteps: Int, hasCompressedHistory: Boolean): String {
        if (rendered.length <= maxCharacters) {
            return rendered
        }

        val truncated = Strings.compactInline(rendered, maxCharacters)
        val suffix = if (hasCompressedHistory) {
            "\n\n(history budget applied; see persisted task logs for full details)"
        } else {
            "\n\n(history budget applied over $totalSteps steps)"
        }
        return truncated + suffix
    }

    private fun escapeJson(value: String): String {
        return value
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
    }
}
