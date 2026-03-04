package ai.platon.pulsar.agentic.inference.detail

import ai.platon.pulsar.agentic.ActResult
import ai.platon.pulsar.agentic.model.*
import ai.platon.pulsar.common.Strings

/**
 * A helper class to help ActResult keeping small.
 * */
object ActResultHelper {

    fun toString(actResult: ActResult): String {
        val eval = Strings.compactInline(actResult.tcEvalValue?.toString(), 50)
        return "[${actResult.action}] expr: ${actResult.expression} eval: $eval message: ${actResult.message}"
    }

    fun failed(message: String, action: String? = null) = ActResult(false, message, action)

    fun failed(message: String, detail: DetailedActResult) = ActResult(
        false,
        message,
        detail = detail,
    )

    fun complete(actionDescription: ActionDescription): ActResult {
        val detailedActResult = DetailedActResult(actionDescription, null, true, actionDescription.summary)
        // val toolCall = ToolCall("agent", "done")
        return ActResult(
            true,
            "completed",
            actionDescription.instruction,
            null,
            detailedActResult
        )
    }
}

/**
 * Enhanced error classification for better retry strategies
 */
sealed class PerceptiveAgentError(message: String, cause: Throwable? = null) : Exception(message, cause) {
    class TransientError(message: String, cause: Throwable? = null) : PerceptiveAgentError(message, cause)
    open class PermanentError(message: String, cause: Throwable? = null) : PerceptiveAgentError(message, cause)
    class TimeoutError(message: String, cause: Throwable? = null) : PerceptiveAgentError(message, cause)
    class ResourceExhaustedError(message: String, cause: Throwable? = null) : PerceptiveAgentError(message, cause)
    class ValidationError(message: String, cause: Throwable? = null) : PerceptiveAgentError(message, cause)
}
