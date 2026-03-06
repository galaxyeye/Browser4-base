package ai.platon.pulsar.test.mcp

import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*

/**
 * Comprehensive tests demonstrating how MockMCPServer can be used for MCP plugin testing.
 */
@Tag("mcp")
@Tag("unit")
class TestMCPServerForPluginMock {

    private lateinit var server: MockMCPServer
    private val objectMapper = jacksonObjectMapper()

    @BeforeEach
    fun setUp() {
        server = MockMCPServer(
            serverName = "plugin-test-server",
            serverVersion = "1.0.0"
        )
    }

    @AfterEach
    fun tearDown() {
        server.close()
    }

    // ========== Server Lifecycle Tests ==========

    @Test
    @DisplayName("server initializes and is running")
    fun serverInitializesAndIsRunning() {
        assertTrue(server.isRunning(), "Server should be running after initialization")
    }

    @Test
    @DisplayName("server can be closed and stopped")
    fun serverCanBeClosedAndStopped() {
        assertTrue(server.isRunning())
        server.close()
        assertFalse(server.isRunning(), "Server should be stopped after close()")
    }

    @Test
    @DisplayName("server info provides required MCP metadata")
    fun serverInfoProvidesRequiredMcpMetadata() {
        val info = server.getInfo()

        Assertions.assertNotNull(info)
        assertEquals("plugin-test-server", info["name"], "Server name should match")
        assertEquals("1.0.0", info["version"], "Server version should match")
        assertTrue(info.containsKey("capabilities"), "Should include capabilities")

        @Suppress("UNCHECKED_CAST")
        val capabilities = info["capabilities"] as Map<String, Any>
        assertTrue(capabilities.containsKey("tools"), "Capabilities should declare tool support")
    }

    // ========== Tool Discovery Tests ==========

    @Test
    @DisplayName("list_tools returns all available tools")
    fun listToolsReturnsAllAvailableTools() {
        val result = server.listTools()

        Assertions.assertNotNull(result)
        assertTrue(result.containsKey("tools"), "Response should contain tools array")

        @Suppress("UNCHECKED_CAST")
        val tools = result["tools"] as List<Map<String, Any>>
        assertEquals(3, tools.size, "Should have 3 default tools")

        val toolNames = tools.map { it["name"] as String }.toSet()
        assertTrue(toolNames.contains("echo"), "Should have echo tool")
        assertTrue(toolNames.contains("add"), "Should have add tool")
        assertTrue(toolNames.contains("multiply"), "Should have multiply tool")
    }

    @Test
    @DisplayName("tool schemas are MCP-compliant")
    fun toolSchemasAreMcpCompliant() {
        val result = server.listTools()

        @Suppress("UNCHECKED_CAST")
        val tools = result["tools"] as List<Map<String, Any>>

        tools.forEach { tool ->
            assertTrue(tool.containsKey("name"), "Tool must have name")
            assertTrue(tool.containsKey("description"), "Tool must have description")
            assertTrue(tool.containsKey("inputSchema"), "Tool must have inputSchema")

            @Suppress("UNCHECKED_CAST")
            val schema = tool["inputSchema"] as Map<String, Any>
            assertEquals("object", schema["type"], "Schema type must be 'object'")
            assertTrue(schema.containsKey("properties"), "Schema must have properties")
            assertTrue(schema.containsKey("required"), "Schema must list required fields")
        }
    }

    @Test
    @DisplayName("tool schemas describe arguments correctly")
    fun toolSchemasDescribeArgumentsCorrectly() {
        val result = server.listTools()

        @Suppress("UNCHECKED_CAST")
        val tools = result["tools"] as List<Map<String, Any>>

        @Suppress("UNCHECKED_CAST")
        val echoTool = tools.first { (it["name"] as String) == "echo" }
        @Suppress("UNCHECKED_CAST")
        val echoSchema = echoTool["inputSchema"] as Map<String, Any>
        @Suppress("UNCHECKED_CAST")
        val echoProps = echoSchema["properties"] as Map<String, Any>
        @Suppress("UNCHECKED_CAST")
        val echoRequired = echoSchema["required"] as List<String>

        assertTrue(echoProps.containsKey("message"), "Echo should have message property")
        assertEquals("string", (echoProps["message"] as Map<*, *>)["type"], "Message should be string type")
        assertTrue(echoRequired.contains("message"), "Message should be required")

        @Suppress("UNCHECKED_CAST")
        val addTool = tools.first { (it["name"] as String) == "add" }
        @Suppress("UNCHECKED_CAST")
        val addSchema = addTool["inputSchema"] as Map<String, Any>
        @Suppress("UNCHECKED_CAST")
        val addProps = addSchema["properties"] as Map<String, Any>
        @Suppress("UNCHECKED_CAST")
        val addRequired = addSchema["required"] as List<String>

        assertTrue(addProps.containsKey("a"), "Add should have parameter 'a'")
        assertTrue(addProps.containsKey("b"), "Add should have parameter 'b'")
        assertEquals("number", (addProps["a"] as Map<*, *>)["type"], "Parameter 'a' should be number")
        assertEquals("number", (addProps["b"] as Map<*, *>)["type"], "Parameter 'b' should be number")
        assertTrue(addRequired.containsAll(listOf("a", "b")), "Both parameters should be required")
    }

    // ========== Tool Execution Tests ==========

    @Test
    @DisplayName("echo tool executes correctly")
    fun echoToolExecutesCorrectly() {
        val testMessage = "Hello from MCP plugin test!"
        val request = objectMapper.createObjectNode().apply {
            put("name", "echo")
            set<ObjectNode>("arguments", objectMapper.createObjectNode().apply {
                put("message", testMessage)
            })
        }

        val result = server.callTool(request)

        Assertions.assertNotNull(result)
        assertTrue(result.containsKey("content"), "Result should contain content")
        assertFalse(result.containsKey("isError"), "Should not have error flag on success")

        @Suppress("UNCHECKED_CAST")
        val content = result["content"] as List<Map<String, Any>>
        assertEquals(1, content.size, "Should have one content item")
        assertEquals("text", content[0]["type"], "Content type should be 'text'")
        assertEquals(testMessage, content[0]["text"], "Echo should return exact input")
    }

    @Test
    @DisplayName("add tool calculates sum correctly")
    fun addToolCalculatesSumCorrectly() {
        val request = objectMapper.createObjectNode().apply {
            put("name", "add")
            set<ObjectNode>("arguments", objectMapper.createObjectNode().apply {
                put("a", 15)
                put("b", 27)
            })
        }

        val result = server.callTool(request)

        @Suppress("UNCHECKED_CAST")
        val content = result["content"] as List<Map<String, Any>>
        assertEquals("42.0", content[0]["text"], "Add should calculate correct sum")
    }

    @Test
    @DisplayName("add tool handles decimal numbers")
    fun addToolHandlesDecimalNumbers() {
        val request = objectMapper.createObjectNode().apply {
            put("name", "add")
            set<ObjectNode>("arguments", objectMapper.createObjectNode().apply {
                put("a", 3.14)
                put("b", 2.86)
            })
        }

        val result = server.callTool(request)

        @Suppress("UNCHECKED_CAST")
        val content = result["content"] as List<Map<String, Any>>
        val resultValue = (content[0]["text"] as String).toDouble()
        assertEquals(6.0, resultValue, 0.01, "Add should handle decimal numbers")
    }

    @Test
    @DisplayName("multiply tool calculates product correctly")
    fun multiplyToolCalculatesProductCorrectly() {
        val request = objectMapper.createObjectNode().apply {
            put("name", "multiply")
            set<ObjectNode>("arguments", objectMapper.createObjectNode().apply {
                put("a", 6)
                put("b", 7)
            })
        }

        val result = server.callTool(request)

        @Suppress("UNCHECKED_CAST")
        val content = result["content"] as List<Map<String, Any>>
        assertEquals("42.0", content[0]["text"], "Multiply should calculate correct product")
    }

    // ========== Error Handling Tests ==========

    @Test
    @DisplayName("calling non-existent tool throws exception")
    fun callingNonExistentToolThrowsException() {
        val request = objectMapper.createObjectNode().apply {
            put("name", "non_existent_tool")
            set<ObjectNode>("arguments", objectMapper.createObjectNode())
        }

        assertThrows(IllegalArgumentException::class.java) {
            server.callTool(request)
        }
    }

    @Test
    @DisplayName("calling tool without name throws exception")
    fun callingToolWithoutNameThrowsException() {
        val request = objectMapper.createObjectNode().apply {
            set<ObjectNode>("arguments", objectMapper.createObjectNode())
            // Missing "name" field
        }

        assertThrows(IllegalArgumentException::class.java) {
            server.callTool(request)
        }
    }

    @Test
    @DisplayName("calling tool without required argument returns error response")
    fun callingToolWithoutRequiredArgumentReturnsErrorResponse() {
        val request = objectMapper.createObjectNode().apply {
            put("name", "echo")
            set<ObjectNode>("arguments", objectMapper.createObjectNode()) // Missing 'message'
        }

        val result = server.callTool(request)

        Assertions.assertNotNull(result)
        assertTrue(result.containsKey("isError"), "Response should indicate error")
        assertEquals(true, result["isError"], "isError flag should be true")
        assertTrue(result.containsKey("content"), "Error response should have content")

        @Suppress("UNCHECKED_CAST")
        val content = result["content"] as List<Map<String, Any>>
        val errorText = content[0]["text"] as String
        assertTrue(
            errorText.contains("required", ignoreCase = true) ||
                    errorText.contains("argument", ignoreCase = true),
            "Error message should mention required argument"
        )
    }

    @Test
    @DisplayName("calling add without required argument returns error")
    fun callingAddWithoutRequiredArgumentReturnsError() {
        val request = objectMapper.createObjectNode().apply {
            put("name", "add")
            set<ObjectNode>("arguments", objectMapper.createObjectNode().apply {
                put("a", 5)
                // Missing 'b' argument
            })
        }

        val result = server.callTool(request)

        assertTrue(result.containsKey("isError"))
        assertEquals(true, result["isError"])
    }

    // ========== Multiple Operations Tests ==========

    @Test
    @DisplayName("server handles multiple sequential tool calls")
    fun serverHandlesMultipleSequentialToolCalls() {
        var request = objectMapper.createObjectNode().apply {
            put("name", "echo")
            set<ObjectNode>("arguments", objectMapper.createObjectNode().apply {
                put("message", "First call")
            })
        }
        var result = server.callTool(request)
        assertFalse(result.containsKey("isError"), "First call should succeed")

        request = objectMapper.createObjectNode().apply {
            put("name", "add")
            set<ObjectNode>("arguments", objectMapper.createObjectNode().apply {
                put("a", 10)
                put("b", 20)
            })
        }
        result = server.callTool(request)
        assertFalse(result.containsKey("isError"), "Second call should succeed")

        @Suppress("UNCHECKED_CAST")
        var content = result["content"] as List<Map<String, Any>>
        assertEquals("30.0", content[0]["text"])

        request = objectMapper.createObjectNode().apply {
            put("name", "multiply")
            set<ObjectNode>("arguments", objectMapper.createObjectNode().apply {
                put("a", 5)
                put("b", 8)
            })
        }
        result = server.callTool(request)
        assertFalse(result.containsKey("isError"), "Third call should succeed")

        @Suppress("UNCHECKED_CAST")
        content = result["content"] as List<Map<String, Any>>
        assertEquals("40.0", content[0]["text"])

        assertTrue(server.isRunning(), "Server should still be running after multiple calls")
    }

    @Test
    @DisplayName("server maintains state across mixed successful and failed calls")
    fun serverMaintainsStateAcrossMixedSuccessfulAndFailedCalls() {
        var request = objectMapper.createObjectNode().apply {
            put("name", "echo")
            set<ObjectNode>("arguments", objectMapper.createObjectNode().apply {
                put("message", "Success")
            })
        }
        var result = server.callTool(request)
        assertFalse(result.containsKey("isError"))

        request = objectMapper.createObjectNode().apply {
            put("name", "add")
            set<ObjectNode>("arguments", objectMapper.createObjectNode().apply {
                put("a", 5)
                // Missing 'b'
            })
        }
        result = server.callTool(request)
        assertTrue(result.containsKey("isError"))

        request = objectMapper.createObjectNode().apply {
            put("name", "multiply")
            set<ObjectNode>("arguments", objectMapper.createObjectNode().apply {
                put("a", 3)
                put("b", 4)
            })
        }
        result = server.callTool(request)
        assertFalse(result.containsKey("isError"))

        @Suppress("UNCHECKED_CAST")
        val content = result["content"] as List<Map<String, Any>>
        assertEquals("12.0", content[0]["text"])

        assertTrue(server.isRunning())
    }

    // ========== MCP Protocol Compliance Tests ==========

    @Test
    @DisplayName("response format is MCP-compliant for successful tool execution")
    fun responseFormatIsMcpCompliantForSuccessfulToolExecution() {
        val request = objectMapper.createObjectNode().apply {
            put("name", "echo")
            set<ObjectNode>("arguments", objectMapper.createObjectNode().apply {
                put("message", "test")
            })
        }

        val result = server.callTool(request)

        assertTrue(result.containsKey("content"))
        @Suppress("UNCHECKED_CAST")
        val content = result["content"] as List<Map<String, Any>>

        content.forEach { item ->
            assertTrue(item.containsKey("type"))
            assertTrue(item.containsKey("text") || item.containsKey("data"))
        }
    }

    @Test
    @DisplayName("response format is MCP-compliant for error cases")
    fun responseFormatIsMcpCompliantForErrorCases() {
        val request = objectMapper.createObjectNode().apply {
            put("name", "echo")
            set<ObjectNode>("arguments", objectMapper.createObjectNode())
        }

        val result = server.callTool(request)

        assertTrue(result.containsKey("isError"))
        assertEquals(true, result["isError"])
        assertTrue(result.containsKey("content"))

        @Suppress("UNCHECKED_CAST")
        val content = result["content"] as List<Map<String, Any>>
        assertTrue(content.isNotEmpty())
        assertEquals("text", content[0]["type"])
        assertTrue((content[0]["text"] as String).isNotEmpty())
    }
}
