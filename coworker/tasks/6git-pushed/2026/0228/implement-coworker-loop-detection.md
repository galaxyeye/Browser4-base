# Monitor Coworker

Coworker 有时会失控，陷入死循环。 在 run_coworker_periodically 脚本中，需要设计一个机制，来监测 coworker 的输出，
如果发现 coworker 一直输出日志，但 3 分钟内没有执行新动作，就认为 coworker 陷入了死循环。

1. coworker 有动作的定义是：本次日志输出包含 `● `, 如`● Read`, `● Edit`, `● Run`
2. 检测机制每10秒检测一次 coworker 的输出日志，每次提取日志最后 500 行，计算 r = count(`● `) / count(`\n`)，如果连续三分钟 r < 5%，就认为 coworker 陷入了死循环。
3. 一旦陷入了死循环，需要放弃本次任务，将任务文件移入 [3_5aborted](../3_5aborted)。

一个失控案例：

[154157-optimize-coworker-daily-memory-generator.copilot.log.stdout](../300logs/2026/02/28/154157-optimize-coworker-daily-memory-generator.copilot.log.stdout)

[run_coworker_periodically.ps1](../../scripts/run_coworker_periodically.ps1)
