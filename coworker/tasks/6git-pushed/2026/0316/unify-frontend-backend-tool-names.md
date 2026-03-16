# 统一前后端 tool name 命名

1. 在前端使用了 declareCommand 定义 tool name 和参数
2. 目前前端为了和后端保持一致，使用了LEGACY_TOOL_NAME_ALIASES来映射 tool name

新需求是：移除LEGACY_TOOL_NAME_ALIASES，在后端直接使用前端定义的 tool name，保持前后端一致。
