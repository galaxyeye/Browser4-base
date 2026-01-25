根据文档 [specification.md](specification.md)，以下是从 LLM 视角使用 Skill 的流程：

1. 发现（Discovery）
   启动时，LLM 只加载每个 skill 的 name 和 description（约 50-100 tokens），了解有哪些能力可用。

2. 匹配（Matching）
   当用户提出任务时，LLM 根据 skill 的描述判断哪个 skill 与任务相关。

3. 激活（Activation）
   匹配到相关 skill 后，LLM 读取完整的 SKILL.md 指令到上下文中。

4. 执行（Execution）
   LLM 按照指令执行任务，按需加载引用文件（references/）或运行脚本（scripts/）。

这种渐进式披露（Progressive Disclosure）设计让 LLM 保持快速响应，同时能按需获取更多上下文。
