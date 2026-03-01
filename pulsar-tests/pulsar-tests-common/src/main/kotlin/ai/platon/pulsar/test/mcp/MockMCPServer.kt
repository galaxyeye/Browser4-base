package ai.platon.pulsar.test.mcp

import ai.platon.pulsar.common.getLogger
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.server.ServerOptions
import io.modelcontextprotocol.kotlin.sdk.types.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.jsonObject
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import java.io.Closeable
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean

/**
 * A minimal MCP server implementation for testing purposes, built on the official MCP SDK.
 *
 * This implementation replaces the legacy MockMCPServer while maintaining the same HTTP/REST API contract.
 * It uses the official `io.modelcontextprotocol.kotlin.sdk.server.Server` to manage tools and execution logic,
 * ensuring consistency with Browser4MCPServer.
 */
@RestController
@RequestMapping("/mcp")
class MockMCPServer(
    private val serverName: String = "test-mcp-server",
    private val serverVersion: String = "1.0.0"
) : Closeable {

    private val logger = getLogger(this)
    private val objectMapper: ObjectMapper = jacksonObjectMapper()
    private val isRunning = AtomicBoolean(false)

    // We keep a local map of handlers to support the legacy REST API pattern which invokes tools by name.
    // In a pure MCP transport (Stdio/SSE), the Server class would handle dispatching.
    // However, since we must preserve the REST API contract for existing tests, we bridge the gap here.
    private val toolHandlers = ConcurrentHashMap<String, suspend (CallToolRequest) -> CallToolResult>()
    
    // We also keep schemas for list_tools endpoint
    private val toolSchemas = ConcurrentHashMap<String, ToolSchema>()
    private val toolDescriptions = ConcurrentHashMap<String, String>()

    // The official SDK Server instance
    val server: Server = Server(
        serverInfo = Implementation(name = serverName, version = serverVersion),
        options = ServerOptions(
            capabilities = ServerCapabilities(
                tools = ServerCapabilities.Tools(listChanged = false)
            )
        ),
        instructions = "Test Server"
    ) {
        // Register default tools
        registerDefaultTools()
    }

    init {
        isRunning.set(true)
        logger.info("MockMCPServer (SDK-based) initialized with name: {}, version: {}", serverName, serverVersion)
    }

    /**
     * Returns server information.
     */
    @GetMapping("/info", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getInfo(): Map<String, Any> {
        return mapOf(
            "name" to serverName,
            "version" to serverVersion,
            "capabilities" to mapOf(
                "tools" to mapOf<String, Any>()
            )
        )
    }

    /**
     * Lists all available tools.
     */
    @PostMapping("/list_tools", produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    fun listTools(): Map<String, Any> {
        logger.debug("Received list_tools request")
        
        // We reconstruct the response format expected by legacy tests
        val toolsList = toolHandlers.keys.map { name ->
            mapOf(
                "name" to name,
                "description" to (toolDescriptions[name] ?: ""),
                "inputSchema" to (toolSchemas[name]?.let { schemaToJsonMap(it) } ?: emptyMap<String, Any>())
            )
        }

        return mapOf("tools" to toolsList)
    }

    /**
     * Executes a tool with the given arguments.
     */
    @PostMapping("/call_tool", produces = [MediaType.APPLICATION_JSON_VALUE], consumes = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    fun callTool(@RequestBody request: Map<String, Any>): Map<String, Any> {
        val toolName = request["name"] as? String
            ?: throw IllegalArgumentException("Tool name is required")

        logger.debug("Received call_tool request for tool: {}", toolName)
        
        val handler = toolHandlers[toolName]
            ?: throw IllegalArgumentException("Tool not found: $toolName")

        return try {
            // Convert arguments to JsonObject for the SDK
            @Suppress("UNCHECKED_CAST")
            val argsMap = request["arguments"] as? Map<String, Any?> ?: emptyMap()
            val arguments = mapToJsonObject(argsMap)
            
            // Create SDK request object
            val callToolRequest = CallToolRequest(
                params = CallToolRequestParams(
                    name = toolName,
                    arguments = arguments
                )
            )

            // Execute using the handler
            val result = kotlinx.coroutines.runBlocking {
                handler(callToolRequest)
            }
            
            if (result.isError == true) {
                // Map SDK error result to legacy error response
                val errorText = result.content.joinToString("") { (it as? TextContent)?.text ?: "" }
                mapOf(
                    "isError" to true,
                    "content" to listOf(
                        mapOf("type" to "text", "text" to errorText)
                    )
                )
            } else {
                // Map SDK success result to legacy success response
                val successText = result.content.joinToString("") { (it as? TextContent)?.text ?: "" }
                mapOf(
                    "content" to listOf(
                        mapOf("type" to "text", "text" to successText)
                    )
                )
            }
        } catch (e: Exception) {
            logger.error("Error executing tool {}: {}", toolName, e.message, e)
            mapOf(
                "isError" to true,
                "content" to listOf(
                    mapOf("type" to "text", "text" to "Error: ${e.message}")
                )
            )
        }
    }

    fun isRunning(): Boolean = isRunning.get()

    override fun close() {
        isRunning.set(false)
        toolHandlers.clear()
        toolSchemas.clear()
        toolDescriptions.clear()
        logger.info("MockMCPServer closed")
    }

    private fun Server.registerDefaultTools() {
        // Echo tool
        addMockTool(
            name = "echo",
            description = "Echoes back the input message",
            inputSchema = ToolSchema(
                properties = JsonObject(mapOf(
                    "message" to JsonObject(mapOf(
                        "type" to JsonPrimitive("string"),
                        "description" to JsonPrimitive("The message to echo back")
                    ))
                )),
                required = listOf("message")
            )
        ) { request ->
            val args = request.params.arguments
            val message = args?.get("message")?.jsonPrimitive?.content
                ?: throw IllegalArgumentException("message argument is required")
            CallToolResult(content = listOf(TextContent(text = message)))
        }

        // Add tool
        addMockTool(
            name = "add",
            description = "Adds two numbers together",
            inputSchema = ToolSchema(
                properties = JsonObject(mapOf(
                    "a" to JsonObject(mapOf("type" to JsonPrimitive("number"), "description" to JsonPrimitive("First number"))),
                    "b" to JsonObject(mapOf("type" to JsonPrimitive("number"), "description" to JsonPrimitive("Second number")))
                )),
                required = listOf("a", "b")
            )
        ) { request ->
            val args = request.params.arguments
            val a = args?.get("a")?.jsonPrimitive?.content?.toDoubleOrNull()
                ?: throw IllegalArgumentException("a argument must be a number")
            val b = args?.get("b")?.jsonPrimitive?.content?.toDoubleOrNull()
                ?: throw IllegalArgumentException("b argument must be a number")
            CallToolResult(content = listOf(TextContent(text = (a + b).toString())))
        }

        // Multiply tool
        addMockTool(
            name = "multiply",
            description = "Multiplies two numbers together",
            inputSchema = ToolSchema(
                properties = JsonObject(mapOf(
                    "a" to JsonObject(mapOf("type" to JsonPrimitive("number"), "description" to JsonPrimitive("First number"))),
                    "b" to JsonObject(mapOf("type" to JsonPrimitive("number"), "description" to JsonPrimitive("Second number")))
                )),
                required = listOf("a", "b")
            )
        ) { request ->
            val args = request.params.arguments
            val a = args?.get("a")?.jsonPrimitive?.content?.toDoubleOrNull()
                ?: throw IllegalArgumentException("a argument must be a number")
            val b = args?.get("b")?.jsonPrimitive?.content?.toDoubleOrNull()
                ?: throw IllegalArgumentException("b argument must be a number")
            CallToolResult(content = listOf(TextContent(text = (a * b).toString())))
        }
    }

    private fun Server.addMockTool(
        name: String,
        description: String,
        inputSchema: ToolSchema,
        handler: suspend (CallToolRequest) -> CallToolResult
    ) {
        // Register with SDK Server
        addTool(name = name, description = description, inputSchema = inputSchema, handler = handler)
        
        // Store locally for REST API access
        toolHandlers[name] = handler
        toolSchemas[name] = inputSchema
        toolDescriptions[name] = description
    }

    // Helper to convert Kotlin Map to Kotlinx JsonObject
    private fun mapToJsonObject(map: Map<String, Any?>): JsonObject {
        return buildJsonObject {
            for ((k, v) in map) {
                when (v) {
                    is String -> put(k, v)
                    is Number -> put(k, v)
                    is Boolean -> put(k, v)
                    null -> {} // ignore nulls
                    else -> put(k, v.toString()) // Fallback
                }
            }
        }
    }

    // Helper to convert ToolSchema to Map for JSON serialization
    private fun schemaToJsonMap(schema: ToolSchema): Map<String, Any> {
        val map = mutableMapOf<String, Any>("type" to "object")
        
        val propsMap = mutableMapOf<String, Any>()
        schema.properties?.forEach { (key, value) ->
             val propDetails = mutableMapOf<String, Any>()
             if (value is JsonObject) {
                 value["type"]?.let { propDetails["type"] = it.jsonPrimitive.content }
                 value["description"]?.let { propDetails["description"] = it.jsonPrimitive.content }
             }
             propsMap[key] = propDetails
        }
        map["properties"] = propsMap
        
        val required = schema.required
        if (required != null) {
            map["required"] = required
        }
        
        return map
    }
}
