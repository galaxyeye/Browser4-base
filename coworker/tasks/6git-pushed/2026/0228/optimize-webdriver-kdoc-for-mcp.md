# 优化 pulsar-skeleton 模块的 WebDriver 接口文档

- [x] 所有 KDoc 注释第一个段落的内容，作为 SourceCodeToToolCallSpec 的 description 字段的值，这个字段将会被 MCP 服务的工具描述使用。
第一个段落的内容应该确保 description 中只包含核心信息，避免冗长内容干扰模型提取工具调用规范的准确性。

- [x] 所有 KDoc 注释第一个段落尾部插入 #mcp 标签，作为 MCP 服务工具描述的标识，确保 MCP 服务能够正确识别和使用这些描述信息。
