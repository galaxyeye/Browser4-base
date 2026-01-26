参考 PulsarEventBus 实现 AgentEventBus

- AgentEventBus 包含 AgentEventHandlers 和 ServerSideAgentEventHandlers
- AgentEventHandlers 处理 Agent 内部事件，限定在 pulsar-agentic 模块内
- ServerSideAgentEventHandlers 负责将 Agent 内部事件发送到客户端，使用 PulsarEventBus.serverSideEventHandlers 相同机制
- AgentEventHandlers 参考 PageEventHandlers 实现。
  - AgenticEvents 定义了需要处理的事件，原有机制是使用 EventBus 进行通用事件，保留，现需要特化以更好地支持 Agent 相关事件

示例：

```kotlin
class DefaultAgentFlowEventHandlers: AgentFlowEventHandlers {
    override val onWillObserve: ObserveEventHandler = ObserveEventHandler()
    override val onDidObserve: ObserveEventHandler = ObserveEventHandler()

    override val onWillAct: ActEventHandler = ActEventHandler()
    override val onDidAct: ActEventHandler = ActEventHandler()

    override val onInferenceWillObserve: ExecutionContextAgentStateEventHandler = ExecutionContextAgentStateEventHandler()
    override val onInferenceDidObserve: ExecutionContextAgentStateEventHandler = ExecutionContextAgentStateEventHandler()
}
```
