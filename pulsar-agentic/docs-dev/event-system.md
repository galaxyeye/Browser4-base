# Agentic Event System

## Overview

The pulsar-agentic module uses a centralized event system based on `DangerousEventBus` to provide observability and extensibility for agent operations. All event types are defined in the `AgenticEvents` object.

## Event Types

### PerceptiveAgent Events

Events emitted by `PerceptiveAgent` implementations during their execution:

| Event | Timing | Payload |
|-------|--------|---------|
| `ON_WILL_RUN` | Before run method | `action` (ActionOptions), `uuid` (UUID) |
| `ON_DID_RUN` | After run method | `action` (ActionOptions), `uuid` (UUID), `result` (ActResult), `stateHistory` (AgentHistory) |
| `ON_WILL_OBSERVE` | Before observe method | `options` (ObserveOptions), `uuid` (UUID) |
| `ON_DID_OBSERVE` | After observe method | `options` (ObserveOptions), `uuid` (UUID), `observeResults` (List<ObserveResult>), `actionDescription` (ActionDescription) |
| `ON_WILL_ACT` | Before act method | `action` (ActionOptions), `uuid` (UUID) |
| `ON_DID_ACT` | After act method | `action` (ActionOptions), `uuid` (UUID), `result` (ActResult) |
| `ON_WILL_EXTRACT` | Before extract method | `options` (ExtractOptions), `uuid` (UUID) |
| `ON_DID_EXTRACT` | After extract method | `options` (ExtractOptions), `uuid` (UUID), `result` (ExtractResult) |
| `ON_WILL_SUMMARIZE` | Before summarize method | `instruction` (String?), `selector` (String?), `uuid` (UUID) |
| `ON_DID_SUMMARIZE` | After summarize method | `instruction` (String?), `selector` (String?), `uuid` (UUID), `result` (String) |

### InferenceEngine Events

Events emitted during inference operations:

| Event | Timing | Payload |
|-------|--------|---------|
| `ON_WILL_OBSERVE` | Before observe inference | `messages` (AgentMessageList) |
| `ON_DID_OBSERVE` | After observe inference | `actionDescription` (ActionDescription) |
| `ON_WILL_EXTRACT` | Before extract inference | `params` (ExtractParams) |
| `ON_DID_EXTRACT` | After extract inference | `params` (ExtractParams), `result` (ObjectNode), `extractedNode` (ObjectNode), `metaNode` (ObjectNode) |
| `ON_WILL_SUMMARIZE` | Before summarize inference | `instruction` (String?), `messages` (AgentMessageList), `textContent` (String) |
| `ON_DID_SUMMARIZE` | After summarize inference | `instruction` (String?), `textContentLength` (Int), `result` (String), `tokenUsage` (TokenUsage) |

### ContextToAction Events

Events emitted during action generation:

| Event | Timing | Payload |
|-------|--------|---------|
| `ON_WILL_GENERATE` | Before generating action | `context` (ExecutionContext), `messages` (AgentMessageList) |
| `ON_DID_GENERATE` | After generating action | `context` (ExecutionContext), `messages` (AgentMessageList), `actionDescription` (ActionDescription) |

## Usage Examples

### Registering Event Handlers

```kotlin
// Register a handler for agent actions
DangerousEventBus.register(AgenticEvents.PerceptiveAgent.ON_WILL_ACT) { payload ->
    val map = payload as? Map<String, Any?> ?: return@register null
    val action = map["action"]
    println("Starting action: $action")
    payload
}

DangerousEventBus.register(AgenticEvents.PerceptiveAgent.ON_DID_ACT) { payload ->
    val map = payload as? Map<String, Any?> ?: return@register null
    val result = map["result"]
    println("Action completed: $result")
    payload
}
```

### Performance Monitoring

```kotlin
val executionTimes = ConcurrentHashMap<String, Long>()

// Track execution time
DangerousEventBus.register(AgenticEvents.InferenceEngine.ON_WILL_EXTRACT) { payload ->
    val map = payload as? Map<String, Any?> ?: return@register null
    val params = map["params"] as? ExtractParams
    executionTimes[params?.requestId ?: "unknown"] = System.currentTimeMillis()
    payload
}

DangerousEventBus.register(AgenticEvents.InferenceEngine.ON_DID_EXTRACT) { payload ->
    val map = payload as? Map<String, Any?> ?: return@register null
    val params = map["params"] as? ExtractParams
    val requestId = params?.requestId ?: "unknown"
    val startTime = executionTimes[requestId] ?: return@register null
    val duration = System.currentTimeMillis() - startTime
    println("Extract completed in ${duration}ms")
    payload
}
```

### Iterating All Events

```kotlin
// Register a common handler for all events
AgenticEvents.getAllEventTypes().forEach { eventType ->
    DangerousEventBus.register(eventType) { payload ->
        println("Event: $eventType")
        payload
    }
}

// Cleanup
AgenticEvents.getAllEventTypes().forEach { eventType ->
    DangerousEventBus.unregister(eventType)
}
```

## Best Practices

1. **Always use constants**: Use `AgenticEvents.*` constants instead of hardcoded strings
2. **Type safety**: Cast payloads safely and check for null
3. **Return payload**: Event handlers should return the payload (or modified version) to allow chaining
4. **Cleanup**: Unregister event handlers when no longer needed
5. **Non-blocking**: Event handlers run asynchronously via coroutines, so keep them lightweight

## Testing

All events are tested in:
- `EventBusObservabilityTest.kt` - Unit tests for all event types
- `EventBusObservabilityExample.kt` - Example usage patterns

## Migration from Hardcoded Strings

If you have existing code using hardcoded event strings, migrate as follows:

| Old String | New Constant |
|------------|--------------|
| `"PerceptiveAgent.onWillAct"` | `AgenticEvents.PerceptiveAgent.ON_WILL_ACT` |
| `"InferenceEngine.onWillExtract"` | `AgenticEvents.InferenceEngine.ON_WILL_EXTRACT` |
| `"ContextToAction.onWillGenerate"` | `AgenticEvents.ContextToAction.ON_WILL_GENERATE` |

See `AgenticEvents.kt` for the complete list of available constants.
