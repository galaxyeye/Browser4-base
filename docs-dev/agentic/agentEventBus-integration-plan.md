# AgentEventBus 集成计划

本文档描述如何在 `pulsar-agentic` 模块的 Agent 实现中关键节点插入 `AgentEventBus` 事件处理，参考 `PulsarEventBus` 的使用模式。

## 1. 现状分析

### 1.1 AgentEventBus 已有功能

`AgentEventBus` (`ai.platon.pulsar.agentic.event.AgentEventBus`) 已实现：

```kotlin
object AgentEventBus {
    // Agent 生命周期事件
    fun emitAgentEvent(eventType: String, agentId: String?, message: String?, metadata: Map<String, Any?>)
    
    // LLM 推理事件
    fun emitInferenceEvent(eventType: String, agentId: String?, message: String?, metadata: Map<String, Any?>)
    
    // 工具调用事件
    fun emitToolEvent(eventType: String, agentId: String?, message: String?, metadata: Map<String, Any?>)
    
    // MCP 协议事件
    fun emitMCPEvent(eventType: String, agentId: String?, message: String?, metadata: Map<String, Any?>)
    
    // 技能执行事件
    fun emitSkillEvent(eventType: String, agentId: String?, message: String?, metadata: Map<String, Any?>)
    
    // 通用事件（自定义阶段）
    fun emitEvent(eventType: String, eventPhase: String, agentId: String?, message: String?, metadata: Map<String, Any?>)
    
    // 协程级别处理器隔离
    suspend fun <T> withServerSideAgentEventHandlers(handlers: ServerSideAgentEventHandlers?, block: suspend () -> T): T
}
```

### 1.2 PulsarEventBus 参考模式

`PulsarEventBus` 在关键节点的使用模式：

```kotlin
// 操作前
PulsarEventBus.emitLoadEvent("onWillLoad", page)

// 操作后
PulsarEventBus.emitLoadEvent("onLoaded", page)
```

事件类型命名规范：`onWill{Operation}` / `onDid{Operation}` 或 `on{Operation}ed`

### 1.3 当前实现

当前 `pulsar-agentic` 使用通用 `EventBus.emit()` 而非专用 `AgentEventBus`：

```kotlin
// 当前代码 (BasicBrowserAgent.kt)
EventBus.emit(AgenticEvents.PerceptiveAgent.RUN_WILL_EXECUTE, mapOf(...))
```

## 2. 集成计划

### 2.1 关键节点识别

#### 2.1.1 BasicBrowserAgent (agents/BasicBrowserAgent.kt)

| 位置 | 事件类型 | AgentEventBus 方法 | 元数据 |
|------|----------|-------------------|--------|
| `run()` 执行前 | `onWillRun` | `emitAgentEvent()` | action, uuid |
| `run()` 执行后 | `onDidRun` | `emitAgentEvent()` | action, uuid, result, stateHistory |
| `observe()` 执行前 | `onWillObserve` | `emitAgentEvent()` | options, uuid |
| `observe()` 执行后 | `onDidObserve` | `emitAgentEvent()` | options, uuid, observeResults |
| `act()` 执行前 | `onWillAct` | `emitAgentEvent()` | action, uuid |
| `act()` 执行后 | `onDidAct` | `emitAgentEvent()` | action, uuid, result |
| `extract()` 执行前 | `onWillExtract` | `emitAgentEvent()` | options, uuid |
| `extract()` 执行后 | `onDidExtract` | `emitAgentEvent()` | options, uuid, result |
| `summarize()` 执行前 | `onWillSummarize` | `emitAgentEvent()` | instruction, selector, uuid |
| `summarize()` 执行后 | `onDidSummarize` | `emitAgentEvent()` | instruction, uuid, result |

#### 2.1.2 InferenceEngine (inference/InferenceEngine.kt)

| 位置 | 事件类型 | AgentEventBus 方法 | 元数据 |
|------|----------|-------------------|--------|
| `observe()` LLM 调用前 | `onWillInfer` | `emitInferenceEvent()` | context, messages |
| `observe()` LLM 调用后 | `onDidInfer` | `emitInferenceEvent()` | context, actionDescription, tokenUsage |
| `extract()` LLM 调用前 | `onWillExtractInfer` | `emitInferenceEvent()` | params |
| `extract()` LLM 调用后 | `onDidExtractInfer` | `emitInferenceEvent()` | params, result, tokenUsage |
| `summarize()` LLM 调用前 | `onWillSummarizeInfer` | `emitInferenceEvent()` | instruction, textContentLength |
| `summarize()` LLM 调用后 | `onDidSummarizeInfer` | `emitInferenceEvent()` | instruction, result, tokenUsage |

#### 2.1.3 MCPToolExecutor (mcp/MCPToolExecutor.kt)

| 位置 | 事件类型 | AgentEventBus 方法 | 元数据 |
|------|----------|-------------------|--------|
| `callFunctionOn()` 调用前 | `onWillCallMCP` | `emitMCPEvent()` | domain, toolName, args |
| `callFunctionOn()` 调用后 | `onDidCallMCP` | `emitMCPEvent()` | domain, toolName, result, duration |
| MCP 连接成功 | `onMCPConnected` | `emitMCPEvent()` | serverName |
| MCP 断开连接 | `onMCPDisconnected` | `emitMCPEvent()` | serverName, reason |
| MCP 调用失败 | `onMCPError` | `emitMCPEvent()` | serverName, toolName, error |

#### 2.1.4 SkillToolExecutor (skills/tools/SkillToolExecutor.kt)

| 位置 | 事件类型 | AgentEventBus 方法 | 元数据 |
|------|----------|-------------------|--------|
| `run` 执行前 | `onWillRunSkill` | `emitSkillEvent()` | skillId, params |
| `run` 执行后 | `onDidRunSkill` | `emitSkillEvent()` | skillId, result, duration |
| `activate` 调用 | `onSkillActivated` | `emitSkillEvent()` | skillId |
| `list` 调用 | `onSkillsListed` | `emitSkillEvent()` | count |
| 技能执行失败 | `onSkillError` | `emitSkillEvent()` | skillId, error |

#### 2.1.5 AgentToolManager (tools/AgentToolManager.kt)

| 位置 | 事件类型 | AgentEventBus 方法 | 元数据 |
|------|----------|-------------------|--------|
| 工具执行前 | `onWillExecuteTool` | `emitToolEvent()` | domain, method, args |
| 工具执行后 | `onDidExecuteTool` | `emitToolEvent()` | domain, method, result, duration |
| 工具执行失败 | `onToolError` | `emitToolEvent()` | domain, method, error |

### 2.2 实现步骤

#### 步骤 1：定义事件类型常量

在 `AgenticEvents.kt` 中添加 AgentEventBus 专用事件常量：

```kotlin
object AgenticEvents {
    // ... 现有代码 ...
    
    /**
     * AgentEventBus 事件类型常量
     * 用于 AgentEventBus.emitXxxEvent() 的 eventType 参数
     */
    object AgentEventTypes {
        // Agent 生命周期
        const val ON_WILL_RUN = "onWillRun"
        const val ON_DID_RUN = "onDidRun"
        const val ON_WILL_OBSERVE = "onWillObserve"
        const val ON_DID_OBSERVE = "onDidObserve"
        const val ON_WILL_ACT = "onWillAct"
        const val ON_DID_ACT = "onDidAct"
        const val ON_WILL_EXTRACT = "onWillExtract"
        const val ON_DID_EXTRACT = "onDidExtract"
        const val ON_WILL_SUMMARIZE = "onWillSummarize"
        const val ON_DID_SUMMARIZE = "onDidSummarize"
    }
    
    object InferenceEventTypes {
        const val ON_WILL_INFER = "onWillInfer"
        const val ON_DID_INFER = "onDidInfer"
        const val ON_WILL_EXTRACT_INFER = "onWillExtractInfer"
        const val ON_DID_EXTRACT_INFER = "onDidExtractInfer"
        const val ON_WILL_SUMMARIZE_INFER = "onWillSummarizeInfer"
        const val ON_DID_SUMMARIZE_INFER = "onDidSummarizeInfer"
    }
    
    object ToolEventTypes {
        const val ON_WILL_EXECUTE_TOOL = "onWillExecuteTool"
        const val ON_DID_EXECUTE_TOOL = "onDidExecuteTool"
        const val ON_TOOL_ERROR = "onToolError"
    }
    
    object MCPEventTypes {
        const val ON_WILL_CALL_MCP = "onWillCallMCP"
        const val ON_DID_CALL_MCP = "onDidCallMCP"
        const val ON_MCP_CONNECTED = "onMCPConnected"
        const val ON_MCP_DISCONNECTED = "onMCPDisconnected"
        const val ON_MCP_ERROR = "onMCPError"
    }
    
    object SkillEventTypes {
        const val ON_WILL_RUN_SKILL = "onWillRunSkill"
        const val ON_DID_RUN_SKILL = "onDidRunSkill"
        const val ON_SKILL_ACTIVATED = "onSkillActivated"
        const val ON_SKILLS_LISTED = "onSkillsListed"
        const val ON_SKILL_ERROR = "onSkillError"
    }
}
```

#### 步骤 2：更新 BasicBrowserAgent

```kotlin
// BasicBrowserAgent.kt
override suspend fun run(action: ActionOptions): AgentHistory {
    // uuid 是 BasicBrowserAgent 的成员属性: override val uuid get() = _uuid
    val agentId = this.uuid.toString()
    
    // 发送 AgentEventBus 事件
    AgentEventBus.emitAgentEvent(
        eventType = AgenticEvents.AgentEventTypes.ON_WILL_RUN,
        agentId = agentId,
        message = "Starting run with action: ${action.action.take(100)}",
        metadata = mapOf("action" to action)
    )
    
    // 保留现有 EventBus 调用（向后兼容）
    EventBus.emit(AgenticEvents.PerceptiveAgent.RUN_WILL_EXECUTE, mapOf(
        "action" to action,
        "uuid" to uuid
    ))
    
    var result = act(action)
    // ... 执行逻辑 ...
    
    AgentEventBus.emitAgentEvent(
        eventType = AgenticEvents.AgentEventTypes.ON_DID_RUN,
        agentId = agentId,
        message = "Run completed",
        metadata = mapOf(
            "action" to action,
            "result" to result,
            "stateHistory" to stateHistory
        )
    )
    
    EventBus.emit(AgenticEvents.PerceptiveAgent.RUN_DID_EXECUTE, mapOf(...))
    
    return stateHistory
}
```

#### 步骤 3：更新 InferenceEngine

```kotlin
// InferenceEngine.kt
suspend fun observe(params: ObserveParams, context: ExecutionContext): ActionDescription {
    // 使用 context.uuid 作为 agentId，保持与 Agent 的关联
    val agentId = context.uuid
    
    AgentEventBus.emitInferenceEvent(
        eventType = AgenticEvents.InferenceEventTypes.ON_WILL_INFER,
        agentId = agentId,
        message = "Starting LLM inference",
        metadata = mapOf(
            "context" to context.sid,
            "step" to context.step,
            "instruction" to context.agentState.instruction.take(100)
        )
    )
    
    val startTime = System.currentTimeMillis()
    val actionDescription = cta.generate(messages, context)
    val duration = System.currentTimeMillis() - startTime
    
    AgentEventBus.emitInferenceEvent(
        eventType = AgenticEvents.InferenceEventTypes.ON_DID_INFER,
        agentId = agentId,
        message = "LLM inference completed",
        metadata = mapOf(
            "context" to context.sid,
            "duration" to duration,
            "tokenUsage" to actionDescription.modelResponse?.tokenUsage
        )
    )
    
    return actionDescription
}
```

#### 步骤 4：更新 MCPToolExecutor

```kotlin
// MCPToolExecutor.kt
override suspend fun callFunctionOn(tc: ToolCall, target: Any): TcEvaluate {
    val toolName = tc.method
    val serverName = clientManager.getServerName()
    // MCP 事件中 agentId 可为 null，因为 MCP 调用可能不直接关联特定 Agent
    // 如果需要关联，可以通过 metadata 或上下文传递
    
    AgentEventBus.emitMCPEvent(
        eventType = AgenticEvents.MCPEventTypes.ON_WILL_CALL_MCP,
        agentId = null, // MCP 层面不直接持有 agentId，可通过调用方传递
        message = "Calling MCP tool: $serverName.$toolName",
        metadata = mapOf(
            "serverName" to serverName,
            "toolName" to toolName,
            "args" to tc.arguments
        )
    )
    
    val startTime = System.currentTimeMillis()
    
    return try {
        val result = clientManager.callTool(toolName, convertArgumentsForMCP(tc.arguments))
        val duration = System.currentTimeMillis() - startTime
        
        AgentEventBus.emitMCPEvent(
            eventType = AgenticEvents.MCPEventTypes.ON_DID_CALL_MCP,
            agentId = null,
            message = "MCP tool call completed",
            metadata = mapOf(
                "serverName" to serverName,
                "toolName" to toolName,
                "duration" to duration,
                "success" to true
            )
        )
        
        // ... 返回结果 ...
    } catch (e: Exception) {
        AgentEventBus.emitMCPEvent(
            eventType = AgenticEvents.MCPEventTypes.ON_MCP_ERROR,
            agentId = null,
            message = "MCP tool call failed: ${e.message}",
            metadata = mapOf(
                "serverName" to serverName,
                "toolName" to toolName,
                "error" to e.message
            )
        )
        // ... 错误处理 ...
    }
}
```

#### 步骤 5：更新 SkillToolExecutor

```kotlin
// SkillToolExecutor.kt
override suspend fun callFunctionOn(
    domain: String,
    functionName: String,
    args: Map<String, Any?>,
    target: Any
): Any? {
    when (functionName) {
        "run" -> {
            val id = paramString(args, "id", functionName)!!
            // Skill 执行层面不直接持有 agentId，可通过 SkillContext 或调用方传递
            // 如果 target 是 SkillToolTarget，可以尝试从 context 获取
            val agentId = (target as? SkillToolTarget)?.context?.sessionId
            
            AgentEventBus.emitSkillEvent(
                eventType = AgenticEvents.SkillEventTypes.ON_WILL_RUN_SKILL,
                agentId = agentId,
                message = "Running skill: $id",
                metadata = mapOf("skillId" to id, "params" to args["params"])
            )
            
            val startTime = System.currentTimeMillis()
            val result = target.execute(id, paramsMap)
            val duration = System.currentTimeMillis() - startTime
            
            AgentEventBus.emitSkillEvent(
                eventType = AgenticEvents.SkillEventTypes.ON_DID_RUN_SKILL,
                agentId = agentId,
                message = "Skill completed: $id",
                metadata = mapOf(
                    "skillId" to id,
                    "duration" to duration,
                    "success" to (result != null)
                )
            )
            
            return result
        }
        // ... 其他方法 ...
    }
}
```

### 2.3 事件订阅示例

```kotlin
// 设置全局事件处理器
AgentEventBus.serverSideAgentEventHandlers = DefaultServerSideAgentEventHandlers()

// 订阅所有事件
AgentEventBus.serverSideAgentEventHandlers?.eventFlow?.collect { event ->
    when (event.eventPhase) {
        "agent" -> println("Agent event: ${event.eventType}")
        "inference" -> println("Inference event: ${event.eventType}")
        "tool" -> println("Tool event: ${event.eventType}")
        "mcp" -> println("MCP event: ${event.eventType}")
        "skill" -> println("Skill event: ${event.eventType}")
    }
}

// 使用协程级别隔离处理器
AgentEventBus.withServerSideAgentEventHandlers(customHandlers) {
    agent.run(task)
}
```

## 3. 兼容性考虑

### 3.1 向后兼容

- 保留现有 `EventBus.emit()` 调用
- `AgentEventBus` 事件是补充，不是替代
- 现有订阅者不受影响

### 3.2 逐步迁移

1. 第一阶段：添加 `AgentEventBus` 事件，保留 `EventBus` 事件
2. 第二阶段：评估后可选择性移除重复的 `EventBus` 事件
3. 第三阶段：统一到 `AgentEventBus` 体系

## 4. 测试计划

### 4.1 单元测试

- 测试每个事件类型的发射
- 测试元数据完整性
- 测试协程隔离处理器

### 4.2 集成测试

- 测试完整 Agent 生命周期的事件流
- 测试 SSE 流式传输
- 测试多 Agent 并发事件隔离

## 5. 预期收益

1. **统一事件系统**：所有 Agent 操作使用一致的事件处理
2. **服务端流式传输**：事件可通过 SSE 流式传输到客户端
3. **请求级隔离**：使用 `withServerSideAgentEventHandlers()` 实现隔离事件处理
4. **可观测性**：完整的 Agent 生命周期可见性，便于调试和监控
5. **性能监控**：通过事件元数据中的 duration 和 tokenUsage 进行性能分析

---

*创建日期: 2026-01-26*
*任务: 仅计划 - 使用 AgentEventBus 在 Agent 实现中关键节点插入事件处理*
