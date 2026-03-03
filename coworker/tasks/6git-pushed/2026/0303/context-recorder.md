# Context Recorder

## Problem

Agents love information, we should record every context, every movement, every decision,
every thought, every action, and every result.
This will help the agent understand how itself works, and also help itself debug and analyze
his behavior.

AgentStateManager is responsible for recording the state of the agent, including the context,
the movement, the decision, the thought, the action, and the result.

Currently, agent state are written in logs/agent, but it can be organized in a better file structure.

## Base log dir

- base log dir for each run: logs/agent/{year}/{month}/{day}/{agentId}/{runId}/
- agent context: write to {runLogDir}/{step}.context.log
- agent state: state before each step write to {runLogDir}/{step}.state.log
- agent action result: the result after each step write to {runLogDir}/{step}.result.log
- LLM raw request and response: {runLogDir}/{step}.chat.{yyyyMMdd.HHmmss}.{actionType}.{messageType}.log, {runLogDir}/{step}.{step}.chat.{yyyyMMdd.HHmmss}.{actionType}.{messageType}.log
  - Example: 2.chat.20260302.220354.chat.user.log, 2.chat.20260302.220355.chat.assistant.log, 2.chat.20260302.220356.cta.system.log

## Implementation Hint

base log dir -> AgentStateManager#auxLogDir

writers:
- AgentStateManager#writeExecutionContext
- AgentStateManager#writeAgentState,
- AgentStateManager#writeProcessTrace
the log dir for writers should be upgraded according to `## Base log dir` section

Whenever ExecutionContext, AgentState and ProcessTrace changes, we should write to log file.

## Reference

- [agent](../../../../logs/agent)
- [AgentStateManager.kt](../../../../pulsar-agentic/src/main/kotlin/ai/platon/pulsar/agentic/inference/detail/AgentStateManager.kt)
