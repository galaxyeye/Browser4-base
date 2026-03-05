package ai.platon.pulsar.rest.api.controller

import ai.platon.pulsar.rest.api.TestHelper.MOCK_PRODUCT_LIST_URL
import ai.platon.pulsar.rest.api.TestHelper.MOCK_PRODUCT_DETAIL_URL
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.client.expectBody
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@Tag("E2ETest")
class MCPToolControllerE2ETest: RestAPITestBase() {
    private val objectMapper = jacksonObjectMapper()
    private val cliCommandToMcpTool = mapOf(
        "open" to "open_session",
        "goto" to "navigate",
        "click" to "click",
        "dblclick" to "dblclick",
        "fill" to "fill",
        "drag" to "drag",
        "hover" to "hover",
        "select" to "select_option",
        "upload" to "upload",
        "check" to "check",
        "uncheck" to "uncheck",
        "type" to "fill",
        "snapshot" to "aria_snapshot",
        "eval" to "evaluate",
        "dialog-accept" to "dialog_accept",
        "dialog-dismiss" to "dialog_dismiss",
        "resize" to "resize",
        "close" to "close_session",
        "go-back" to "go_back",
        "go-forward" to "go_forward",
        "reload" to "reload",
        "press" to "press",
        "keydown" to "keydown",
        "keyup" to "keyup",
        "mousemove" to "mousemove",
        "mousedown" to "mousedown",
        "mouseup" to "mouseup",
        "mousewheel" to "mousewheel",
        "screenshot" to "screenshot",
        "pdf" to "evaluate",
        "tab-list" to "tab_list",
        "tab-new" to "tab_new",
        "tab-close" to "tab_close",
        "tab-select" to "tab_select",
        "list" to "list_sessions",
        "close-all" to "close_all_sessions",
        "kill-all" to "kill_all_sessions",
        "delete-data" to "delete_session_data"
    )

    private fun callTool(tool: String, arguments: Map<String, Any?> = emptyMap()): Map<String, Any?> {
        val request = mapOf("tool" to tool, "arguments" to arguments)
        return client.post().uri("/mcp/call-tool")
            .contentType(MediaType.APPLICATION_JSON)
            .body(request)
            .exchange()
            .expectStatus().is2xxSuccessful
            .expectBody<Map<String, Any?>>()
            .returnResult()
            .responseBody!!
    }

    private fun textContent(response: Map<String, Any?>): String {
        @Suppress("UNCHECKED_CAST")
        val content = response["content"] as List<Map<String, Any?>>
        return content.first()["text"]?.toString().orEmpty()
    }

    private fun assertNotError(response: Map<String, Any?>) {
        assertFalse(response["isError"] == true, "Expected successful MCP response but got: $response")
    }

    private fun assertToolRecognized(tool: String, arguments: Map<String, Any?> = emptyMap()) {
        val response = callTool(tool, arguments)
        val text = textContent(response)
        assertFalse(text.contains("Unknown tool:"), "Tool $tool should be recognized, response: $response")
    }

    private fun openSession(url: String = MOCK_PRODUCT_DETAIL_URL): String {
        val openResponse = callTool("open_session", mapOf("url" to url))
        assertNotError(openResponse)
        val sessionId = objectMapper.readTree(textContent(openResponse)).path("sessionId").asText()
        assertTrue(sessionId.isNotBlank())
        return sessionId
    }

    @Test
    @DisplayName("Test MCP tools endpoint covers all browser4-cli commands")
    fun testToolsCoverAllCliCommands() {
        val payload = client.get().uri("/mcp/tools")
            .exchange()
            .expectStatus().is2xxSuccessful
            .expectBody<Map<String, Any>>()
            .returnResult()
            .responseBody
        assertNotNull(payload)

        @Suppress("UNCHECKED_CAST")
        val tools = (payload["tools"] as List<String>).toSet()
        assertTrue(cliCommandToMcpTool.values.all { it in tools })
    }

    @Test
    @DisplayName("Test MCPToolController call-tool covers all cli mapped tools on mock pages")
    fun testCallToolCoverageForCliMappedToolsOnMockPages() {
        val sessionId = openSession(MOCK_PRODUCT_DETAIL_URL)

        val defaultArgs = mapOf("sessionId" to sessionId)
        val toolArguments = mapOf(
            "navigate" to defaultArgs + ("url" to MOCK_PRODUCT_LIST_URL),
            "click" to defaultArgs + ("selector" to "body"),
            "dblclick" to defaultArgs + ("selector" to "body"),
            "fill" to defaultArgs + ("selector" to "#productTitle") + ("text" to "Browser4"),
            "drag" to defaultArgs + ("source_selector" to "body") + ("target_selector" to "body"),
            "hover" to defaultArgs + ("selector" to "body"),
            "select_option" to defaultArgs + ("selector" to "body") + ("value" to "1"),
            "upload" to defaultArgs + ("selector" to "body") + ("paths" to listOf("README.md")),
            "check" to defaultArgs + ("selector" to "body"),
            "uncheck" to defaultArgs + ("selector" to "body"),
            "aria_snapshot" to defaultArgs,
            "evaluate" to defaultArgs + ("expression" to "document.title"),
            "dialog_accept" to defaultArgs + ("prompt_text" to "ok"),
            "dialog_dismiss" to defaultArgs,
            "resize" to defaultArgs + ("width" to 1200) + ("height" to 900),
            "go_back" to defaultArgs,
            "go_forward" to defaultArgs,
            "reload" to defaultArgs,
            "press" to defaultArgs + ("selector" to "body") + ("key" to "Enter"),
            "keydown" to defaultArgs + ("key" to "A"),
            "keyup" to defaultArgs + ("key" to "A"),
            "mousemove" to defaultArgs + ("x" to 100) + ("y" to 200),
            "mousedown" to defaultArgs + ("button" to "left"),
            "mouseup" to defaultArgs + ("button" to "left"),
            "mousewheel" to defaultArgs + ("delta_x" to 0) + ("delta_y" to 120),
            "screenshot" to defaultArgs,
            "tab_list" to defaultArgs,
            "tab_new" to defaultArgs + ("url" to MOCK_PRODUCT_DETAIL_URL),
            "tab_close" to defaultArgs,
            "tab_select" to defaultArgs + ("index" to 0),
            "list_sessions" to emptyMap<String, Any?>(),
            "delete_session_data" to defaultArgs
        )

        val toolsToVerify = cliCommandToMcpTool.values.toSet() - setOf(
            "open_session", "close_session", "close_all_sessions", "kill_all_sessions"
        )
        toolsToVerify.forEach { tool ->
            assertToolRecognized(tool, toolArguments[tool] ?: defaultArgs)
        }

        val closeResponse = callTool("close_session", defaultArgs)
        assertNotError(closeResponse)
        assertTrue(textContent(closeResponse).contains("Session closed"))

        val sessionA = openSession(MOCK_PRODUCT_DETAIL_URL)
        val sessionB = openSession(MOCK_PRODUCT_LIST_URL)
        val closeAllResponse = callTool("close_all_sessions")
        assertNotError(closeAllResponse)
        assertTrue(textContent(closeAllResponse).contains("Closed"))
        val listAfterCloseAll = callTool("list_sessions")
        assertNotError(listAfterCloseAll)
        assertFalse(textContent(listAfterCloseAll).contains(sessionA))
        assertFalse(textContent(listAfterCloseAll).contains(sessionB))

        val killSession = openSession(MOCK_PRODUCT_DETAIL_URL)
        val killAllResponse = callTool("kill_all_sessions")
        assertNotError(killAllResponse)
        assertTrue(textContent(killAllResponse).contains("Killed"))
        val listAfterKillAll = callTool("list_sessions")
        assertNotError(listAfterKillAll)
        assertFalse(textContent(listAfterKillAll).contains(killSession))
    }

}
