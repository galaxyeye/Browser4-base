# MCP Plugin Support

The pulsar-agentic module now supports the Model Context Protocol (MCP), allowing you to integrate external MCP servers as tool providers.

## Overview

MCP (Model Context Protocol) is a standardized way for LLM applications to provide context. The Pulsar MCP plugin support enables you to:

- Connect to MCP servers using various transports (STDIO, SSE, WebSocket)
- Automatically discover and register tools from MCP servers
- Execute MCP tools through the standard Pulsar tool execution framework
- Manage multiple MCP server connections concurrently

## Quick Start

### 1. Configure an MCP Server

```kotlin
import ai.platon.pulsar.agentic.mcp.*

val config = MCPConfig(
    serverName = "weather-server",
    transportType = MCPTransportType.STDIO,
    command = "node",
    args = listOf("path/to/weather-server.js")
)
```

### 2. Register the MCP Server

```kotlin
import ai.platon.pulsar.agentic.mcp.MCPPluginRegistry

// Register a single server
MCPPluginRegistry.instance.registerMCPServer(config)

// Or register multiple servers at once
val configs = listOf(
    MCPConfig("weather-server", MCPTransportType.STDIO, "node", listOf("weather.js")),
    MCPConfig("calendar-server", MCPTransportType.SSE, url = "http://localhost:8080/sse")
)
MCPPluginRegistry.instance.registerMCPServers(configs)
```

### 3. Use MCP Tools

Once registered, MCP tools are automatically available through the CustomToolRegistry:

```kotlin
import ai.platon.pulsar.agentic.tools.CustomToolRegistry

// List available tools
val executor = CustomToolRegistry.instance.get("mcp.weather-server")
println(executor.help())

// Execute a tool through the standard tool call mechanism
val toolCall = ToolCall(
    domain = "mcp.weather-server",
    method = "get_forecast",
    arguments = mutableMapOf("location" to "San Francisco")
)
val result = executor.callFunctionOn(toolCall)
```

## Configuration

### Transport Types

#### STDIO Transport

For local MCP servers running as separate processes:

```kotlin
val config = MCPConfig(
    serverName = "local-server",
    transportType = MCPTransportType.STDIO,
    command = "python",
    args = listOf("server.py")
)
```

#### SSE (Server-Sent Events) Transport

For HTTP-based MCP servers:

```kotlin
val config = MCPConfig(
    serverName = "remote-server",
    transportType = MCPTransportType.SSE,
    url = "http://localhost:8080/sse"
)
```

#### WebSocket Transport

For WebSocket-based MCP servers:

```kotlin
val config = MCPConfig(
    serverName = "ws-server",
    transportType = MCPTransportType.WEBSOCKET,
    url = "ws://localhost:8080/ws"
)
```

### Disabling Servers

You can temporarily disable an MCP server without removing its configuration:

```kotlin
val config = MCPConfig(
    serverName = "optional-server",
    transportType = MCPTransportType.STDIO,
    command = "node",
    args = listOf("server.js"),
    enabled = false  // Server will not be connected
)
```

## Architecture

### Components

1. **MCPConfig**: Configuration for connecting to an MCP server
2. **MCPClientManager**: Manages the lifecycle of an MCP client connection
3. **MCPToolExecutor**: Implements the ToolExecutor interface to execute MCP tools
4. **MCPPluginRegistry**: Central registry for managing multiple MCP server connections

### Integration with Pulsar Tools

MCP tools are integrated into the Pulsar tool execution framework:

- Tools are registered with domain `mcp.<serverName>`
- Tool specifications are automatically generated from MCP tool schemas
- Tool execution follows the standard ToolExecutor pattern
- Results are formatted consistently with other Pulsar tools

## Advanced Usage

### Managing Connections

```kotlin
// Check if a server is connected
val isConnected = MCPPluginRegistry.instance.getClientManager("weather-server")?.isConnected()

// Unregister a server
MCPPluginRegistry.instance.unregisterMCPServer("weather-server")

// Get all registered servers
val servers = MCPPluginRegistry.instance.getRegisteredServers()
```

### Error Handling

```kotlin
// Register multiple servers with error handling
val errors = MCPPluginRegistry.instance.registerMCPServers(configs)
errors.forEach { (serverName, exception) ->
    logger.error("Failed to register {}: {}", serverName, exception.message)
}
```

### Cleanup

```kotlin
// Close all MCP connections
MCPPluginRegistry.instance.close()
```

## Best Practices

1. **Connection Management**: Register MCP servers during application initialization
2. **Error Handling**: Always check for connection errors when registering servers
3. **Resource Cleanup**: Close the MCPPluginRegistry when shutting down
4. **Server Naming**: Use descriptive names that indicate the server's purpose
5. **Tool Discovery**: Allow time for tool discovery before using MCP tools

## Dependencies

The MCP plugin support requires:

- `io.modelcontextprotocol:kotlin-sdk` (latest compatible version)
- `io.ktor:ktor-client-cio` (Ktor client for HTTP/WebSocket transports)

These are automatically included when using the pulsar-agentic module.
Specific versions are managed through the parent POM dependency management.

## Troubleshooting

### Server Connection Fails

- Verify the command/URL is correct
- Check that the MCP server is accessible
- Review logs for detailed error messages

### Tools Not Available

- Ensure the server has been successfully connected
- Check that autoRegisterTools is enabled (default: true)
- Verify the server actually provides tools

### Tool Execution Errors

- Check tool argument types match the schema
- Review MCP server logs for issues
- Ensure the tool is still available on the server

## Examples

See the test files in `pulsar-core/pulsar-agentic/src/test/kotlin/ai/platon/pulsar/agentic/mcp/` for complete working examples.
