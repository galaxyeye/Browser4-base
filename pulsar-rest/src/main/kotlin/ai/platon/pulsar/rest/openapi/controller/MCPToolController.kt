package ai.platon.pulsar.rest.openapi.controller

import ai.platon.pulsar.agentic.agents.BasicBrowserAgent
import ai.platon.pulsar.agentic.mcp.server.Browser4MCPServer
import ai.platon.pulsar.agentic.model.ToolCall
import ai.platon.pulsar.agentic.tools.AgentToolManager
import ai.platon.pulsar.rest.openapi.service.SessionManager
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.concurrent.ConcurrentHashMap

// ---------------------------------------------------------------------------
// DTOs
// ---------------------------------------------------------------------------

/**
 * Request body for calling an MCP tool.
 */
data class MCPToolCallRequest(
    @param:JsonProperty("tool") val tool: String,
    @param:JsonProperty("arguments") val arguments: Map<String, Any?>? = null
)

/**
 * Response from an MCP tool call.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
data class MCPToolCallResponse(
    @param:JsonProperty("content") val content: List<MCPContent>,
    @param:JsonProperty("isError") val isError: Boolean = false
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class MCPContent(
    @param:JsonProperty("type") val type: String = "text",
    @param:JsonProperty("text") val text: String
)

// ---------------------------------------------------------------------------
// Controller
// ---------------------------------------------------------------------------

/**
 * REST controller that exposes Browser4 MCP tools over HTTP.
 *
 * This allows the browser4-cli (and any HTTP client) to invoke MCP tools
 * through a simple REST endpoint instead of STDIO.
 *
 * Session management tools (open_session, close_session, list_sessions, etc.)
 * are handled directly by this controller.  All other tools are dispatched to
 * a per-session [Browser4MCPServer] instance that wraps the session's WebDriver.
 */
@RestController
@CrossOrigin
@RequestMapping(
    path = ["/mcp"],
    produces = [MediaType.APPLICATION_JSON_VALUE]
)
@ConditionalOnBean(SessionManager::class)
class MCPToolController(
    private val sessionManager: SessionManager
) {
    private val logger = LoggerFactory.getLogger(MCPToolController::class.java)

    /** Cache of MCP server instances keyed by sessionId. */
    private val mcpServers = ConcurrentHashMap<String, Browser4MCPServer>()
    private val objectMapper = com.fasterxml.jackson.databind.ObjectMapper()

    // =========================================================================
    // Tool call endpoint
    // =========================================================================

    /**
     * Call an MCP tool.
     *
     * Session management tools (`open_session`, `close_session`, `list_sessions`,
     * `close_all_sessions`, `kill_all_sessions`, `delete_session_data`) do not
     * require a sessionId.
     *
     * All other tools require the `sessionId` to be provided in the request body
     * or via the path variable.
     */
    @PostMapping("/call-tool", consumes = [MediaType.APPLICATION_JSON_VALUE])
    suspend fun callTool(
        @RequestBody request: MCPToolCallRequest,
        response: HttpServletResponse
    ): ResponseEntity<MCPToolCallResponse> {
        ControllerUtils.addRequestId(response)

        return try {
            when (request.tool) {
                // Session management tools
                "open_session" -> handleOpenSession(request)
                "close_session" -> handleCloseSession(request)
                "list_sessions" -> handleListSessions()
                "close_all_sessions" -> handleCloseAllSessions()
                "kill_all_sessions" -> handleKillAllSessions()
                "delete_session_data" -> handleDeleteSessionData(request)
                // All other tools are dispatched to the session's MCP Server
                else -> dispatchToMCPServer(request)
            }
        } catch (e: Exception) {
            logger.error("MCP tool call failed | tool={} | {}", request.tool, e.message, e)
            ResponseEntity.ok(errorResponse("${request.tool} failed: ${e.message}"))
        }
    }

    /**
     * List available MCP tools.
     */
    @GetMapping("/tools")
    fun listTools(
        response: HttpServletResponse
    ): ResponseEntity<Any> {
        ControllerUtils.addRequestId(response)

        val tools = listOf(
            // Session management
            "open_session", "close_session", "list_sessions",
            "close_all_sessions", "kill_all_sessions", "delete_session_data",
            // Driver tools
            "navigate", "reload", "go_back", "go_forward",
            "wait_for_selector", "exists", "is_visible", "focus",
            "hover", "click", "fill", "type", "upload", "press",
            "check", "uncheck",
            "scroll_to", "scroll_to_top", "scroll_to_bottom", "scroll_to_middle", "scroll_by",
            "text_content", "get_text", "delay",
            "aria_snapshot", "page_url", "page_title",
            "screenshot", "dblclick", "drag", "select_option",
            "evaluate", "dialog_accept", "dialog_dismiss", "resize",
            "keydown", "keyup",
            "mousemove", "mousedown", "mouseup", "mousewheel",
            // Browser tools
            "switch_tab", "close_tab", "tab_list", "tab_new", "tab_close", "tab_select"
        )
        return ResponseEntity.ok(mapOf("tools" to tools))
    }

    // =========================================================================
    // Session management handlers
    // =========================================================================

    private fun handleOpenSession(request: MCPToolCallRequest): ResponseEntity<MCPToolCallResponse> {
        val capabilities = request.arguments?.get("capabilities") as? Map<String, Any?>
        val session = sessionManager.createSession(capabilities)

        // Navigate to initial URL if provided
        val url = request.arguments?.get("url")?.toString()

        logger.info("MCP open_session: created session {}", session.sessionId)
        return ResponseEntity.ok(
            textResponse("""{"sessionId":"${session.sessionId}"}""")
        )
    }

    private fun handleCloseSession(request: MCPToolCallRequest): ResponseEntity<MCPToolCallResponse> {
        val sessionId = requireSessionId(request)
        mcpServers.remove(sessionId)
        val deleted = sessionManager.deleteSession(sessionId)
        return if (deleted) {
            ResponseEntity.ok(textResponse("Session closed"))
        } else {
            ResponseEntity.ok(errorResponse("Session not found: $sessionId"))
        }
    }

    private fun handleListSessions(): ResponseEntity<MCPToolCallResponse> {
        val sessions = sessionManager.getAllSessions().map { s ->
            """{"sessionId":"${s.sessionId}","url":"${s.url ?: ""}","status":"${s.status}"}"""
        }
        return ResponseEntity.ok(textResponse("[${sessions.joinToString(",")}]"))
    }

    private fun handleCloseAllSessions(): ResponseEntity<MCPToolCallResponse> {
        mcpServers.clear()
        val count = sessionManager.deleteAllSessions()
        return ResponseEntity.ok(textResponse("Closed $count session(s)"))
    }

    private fun handleKillAllSessions(): ResponseEntity<MCPToolCallResponse> {
        mcpServers.clear()
        val count = sessionManager.deleteAllSessions()
        return ResponseEntity.ok(textResponse("Killed $count session(s)"))
    }

    private suspend fun handleDeleteSessionData(request: MCPToolCallRequest): ResponseEntity<MCPToolCallResponse> {
        val sessionId = requireSessionId(request)
        val managed = sessionManager.getSession(sessionId)
            ?: return ResponseEntity.ok(errorResponse("Session not found: $sessionId"))

        managed.withLock {
            driver.clearBrowserCookies()
            driver.evaluate("localStorage.clear(); sessionStorage.clear()")
        }
        return ResponseEntity.ok(textResponse("User data deleted for session"))
    }

    // =========================================================================
    // Dispatch to per-session AgentToolManager
    // =========================================================================

    /**
     * Dispatch a tool call to the session's AgentToolManager.
     *
     * This replaces the manual tool implementation by delegating to the central
     * tool registry in [AgentToolManager].
     */
    private suspend fun dispatchToMCPServer(request: MCPToolCallRequest): ResponseEntity<MCPToolCallResponse> {
        val sessionId = requireSessionId(request)
        val managed = sessionManager.getSession(sessionId)
            ?: return ResponseEntity.ok(errorResponse("Session not found: $sessionId"))

        val agent = managed.pulsarSession.companionAgent as? BasicBrowserAgent
            ?: return ResponseEntity.ok(errorResponse("Session agent does not support tools"))
        
        val toolName = request.tool
        val args = request.arguments ?: emptyMap()
        
        // Find the matching tool in AgentToolManager
        val toolCall = resolveToolCall(toolName, args, agent)
            ?: return ResponseEntity.ok(errorResponse("Unknown tool: $toolName"))

        return try {
            val result = agent.toolManager.executeToolCall(toolCall)
            val exception = result.exception
            if (exception != null) {
                ResponseEntity.ok(errorResponse("${toolName} failed: ${exception.message}"))
            } else {
                val value = result.value?.toString() ?: ""
                ResponseEntity.ok(textResponse(value))
            }
        } catch (e: Exception) {
            logger.error("MCP tool execution failed | tool={} | {}", toolName, e.message, e)
            ResponseEntity.ok(errorResponse("${toolName} failed: ${e.message}"))
        }
    }

    private fun resolveToolCall(toolName: String, args: Map<String, Any?>, agent: BasicBrowserAgent): ToolCall? {
        val specs = agent.toolManager.getAllToolSpecs()
        for ((domain, methods) in specs) {
            for ((method, _) in methods) {
                val mcpName = toMcpToolName(domain, method)
                if (mcpName == toolName) {
                    return ToolCall(domain, method, args)
                }
            }
        }
        return null
    }

    /**
     * Convert domain+method to snake_case MCP tool name.
     * Must match logic in Browser4MCPServer.
     */
    private fun toMcpToolName(domain: String, method: String): String {
        val snake = method.replace(Regex("([A-Z])")) { "_${it.groupValues[1].lowercase()}" }
        return when (domain) {
            "driver", "system" -> snake
            else -> "${domain}_$snake"
        }
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private fun requireSessionId(request: MCPToolCallRequest): String {
        return request.arguments?.get("sessionId")?.toString()
            ?: throw IllegalArgumentException("Missing required parameter: sessionId")
    }

    private fun requireArg(args: Map<String, Any?>, key: String): String {
        return args[key]?.toString()
            ?: throw IllegalArgumentException("Missing required parameter: $key")
    }

    private fun textResponse(text: String): MCPToolCallResponse =
        MCPToolCallResponse(content = listOf(MCPContent(text = text)))

    private fun errorResponse(message: String): MCPToolCallResponse =
        MCPToolCallResponse(content = listOf(MCPContent(text = "ERROR: $message")), isError = true)
}
