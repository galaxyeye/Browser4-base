# Monthly Memory - 2026-03

## 2026-03-01
- **Task**: Reimplement Mock MCP Server
- **Summary**: Reimplemented MockMCPServer using `io.modelcontextprotocol` SDK (v0.8.1). Updated `pulsar-tests-common` dependencies and refactored endpoints to use Maps for serialization. Verified with updated unit and E2E tests, resolving Jackson serialization issues with `RestTestClient`.
- **Lessons**: Explicitly converting `ObjectNode` to `Map` ensures correct serialization for `RestTestClient`. Use `-am` flag when building dependent modules.
