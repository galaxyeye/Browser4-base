# Improve run-agent-examples

## Problem

将 browser4-examples-4.6.0-SNAPSHOT.jar 的默认启动程序设为 exec.mainClass=ai.platon.pulsar.examples.agent.Browser4AgentKt，
通过 java -jar browser4-examples-4.6.0-SNAPSHOT.jar 来启动示例程序，简化运行示例程序的步骤。

## Solution

- 修改 pom.xml，使用 spring-boot-maven-plugin 插件来打包 jar 文件，并设置 exec.mainClass 属性为 ai.platon.pulsar.examples.agent.Browser4AgentKt。
- run-agent-examples 脚本中需要查找到 browser4-examples-*.jar 文件，并使用 java -jar 命令来启动示例程序。
- 提供 ps1/sh 两个版本

## References

[browser4-examples-4.6.0-SNAPSHOT.jar](../../../examples/browser4-examples/target/browser4-examples-4.6.0-SNAPSHOT.jar)
[run-agent-examples.ps1](../../../bin/run-agent-examples.ps1)
