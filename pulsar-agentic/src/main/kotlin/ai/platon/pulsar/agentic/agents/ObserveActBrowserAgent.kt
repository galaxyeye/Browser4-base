package ai.platon.pulsar.agentic.agents

import ai.platon.pulsar.agentic.ActionOptions
import ai.platon.pulsar.agentic.AgenticSession
import ai.platon.pulsar.agentic.model.ExecutionContext
import ai.platon.pulsar.common.getLogger
import kotlinx.coroutines.delay

/**
 * A reasoning agent that uses [observe -> act -> observe -> act -> ...] pattern to resolve browser use problems.
 * */
class ObserveActBrowserAgent constructor(
    session: AgenticSession, maxSteps: Int = 100, config: AgentConfig = AgentConfig(maxSteps = maxSteps)
) : BrowserPerceptiveAgent(session, maxSteps, config) {
    private val logger = getLogger(ObserveActBrowserAgent::class)

    override suspend fun step(action: ActionOptions, context: ExecutionContext, noOpsIn: Int): StepProcessingResult {
        var consecutiveNoOps = noOpsIn

        // Execute the tool call with enhanced error handling
        val actResult = act(action)

        if (actResult.isComplete) {
            onTaskCompletion(actResult, context)
            return StepProcessingResult(context, consecutiveNoOps, true)
        }

        if (!actResult.success) {
            consecutiveNoOps++
            val stop = handleConsecutiveNoOps(consecutiveNoOps, context)
            if (stop) {
                return StepProcessingResult(context, consecutiveNoOps, true)
            }
        }

        delay(calculateAdaptiveDelay())
        return StepProcessingResult(context, consecutiveNoOps, false)
    }
}
