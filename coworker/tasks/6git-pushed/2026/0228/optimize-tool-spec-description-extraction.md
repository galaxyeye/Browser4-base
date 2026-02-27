# 优化 SourceCodeToToolCallSpec

## 1. 现有问题

- 目前 description 中的内容过于冗长，包含了大量不必要的信息，导致模型难以准确提取工具调用规范。
- 需要将 description 中的内容精简为核心信息

## 2. 优化方案

- 如果 KDoc 注释中某个段落包含 #mcp 标签，则将该段落作为 description 的内容，去掉其他段落。该标签大小写不敏感，可以放在段落的任意位置。
- 如果 KDoc 注释中没有 #mcp 标签，则仅保留第一段作为 description，去掉后续的详细说明、示例等信息。
