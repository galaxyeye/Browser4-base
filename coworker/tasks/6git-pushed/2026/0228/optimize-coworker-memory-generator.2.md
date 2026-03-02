# Improve coworker-daily-memory-generator

优化 coworker-daily-memory-generator，使其能够更高效地分析日志并生成记忆总结。

- 读取 coworker.ps1 理解 coworker 的日志格式和内容，确保脚本能够正确解析日志文件。
- 利用日志的结构，提取关键信息，仅将关键信息输入到 AI 模型中，以节约 Token 消耗。
- *.copilot.log 文件中包含了 AI 模型的输出，关键信息在尾部，尾部信息通常包含了任务总结和 Token 消耗小结。
- copilot log 会记录 copilot 的工具调用，工具调用日志格式一般以 `● Read`, `● Edit`, `● Run` 开头

输入到 AI 模型中的信息可以包括：

- 每个 copilot log 文件头部 10 行
- 最后一个动作之后的日志信息，包括任务总结和 Token 消耗小结

## References

- [coworker-daily-memory-generator.ps1](/coworker/scripts/workers/coworker-daily-memory-generator.ps1)
- [coworker-daily-memory-generator.sh](/coworker/scripts/workers/coworker-daily-memory-generator.sh)
