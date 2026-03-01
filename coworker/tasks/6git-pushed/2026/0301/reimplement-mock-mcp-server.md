# Implement Modern Mock MCP Server

使用 io.modelcontextprotocol 库来重新实现 Mock MCP Server，使其更符合 MCP 规范，并且更易于扩展和维护。

保留现有 ai.platon.pulsar.test.mcp.legacy 包。
在 ai.platon.pulsar.test.mcp 包下创建新的实现，实现同 Browser4MCPServer 保持一致。
原有使用 Mock MCP Server 的测试用例全部换成新的实现，确保功能不受影响。

