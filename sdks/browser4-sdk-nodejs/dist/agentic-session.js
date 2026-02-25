"use strict";
/**
 * AgenticSession extends PulsarSession with AI-powered agent capabilities.
 */
Object.defineProperty(exports, "__esModule", { value: true });
exports.AgenticSession = void 0;
const pulsar_session_1 = require("./pulsar-session");
const models_1 = require("./models");
/**
 * AgenticSession provides AI-powered browser automation capabilities.
 *
 * Extends PulsarSession with agent methods for autonomous task execution,
 * mirroring the Kotlin AgenticSession interface.
 */
class AgenticSession extends pulsar_session_1.PulsarSession {
    constructor() {
        super(...arguments);
        this._processTrace = [];
        this._stateHistory = [];
    }
    /**
     * Get the process trace.
     */
    get processTrace() {
        return [...this._processTrace];
    }
    /**
     * Get the state history.
     */
    get stateHistory() {
        return {
            states: [...this._stateHistory],
            hasErrors: this._stateHistory.some(s => !s.success),
            finalResult: this._stateHistory.length > 0
                ? this._stateHistory[this._stateHistory.length - 1].result
                : undefined
        };
    }
    /**
     * Get the companion agent (returns self).
     */
    get companionAgent() {
        return this;
    }
    /**
     * Execute a single action.
     */
    async act(action) {
        const result = await this.client.post('/session/{sessionId}/agent/act', { action });
        const actResult = (0, models_1.createAgentActResult)(result);
        // Update state history
        this._stateHistory.push({
            step: this._stateHistory.length + 1,
            action,
            result: actResult.result,
            success: actResult.success,
            message: actResult.message
        });
        // Update process trace
        if (actResult.trace) {
            this._processTrace.push(...actResult.trace);
        }
        return actResult;
    }
    /**
     * Run an autonomous task.
     */
    async run(instruction) {
        return this.agentRun(instruction);
    }
    /**
     * Run an autonomous task (explicit method name).
     */
    async agentRun(instruction) {
        const result = await this.client.post('/session/{sessionId}/agent/run', { instruction });
        const runResult = (0, models_1.createAgentRunResult)(result);
        // Update state history
        this._stateHistory.push({
            step: this._stateHistory.length + 1,
            action: `run: ${instruction}`,
            result: runResult.finalResult,
            success: runResult.success,
            message: runResult.message
        });
        // Update process trace
        if (runResult.trace) {
            this._processTrace.push(...runResult.trace);
        }
        return runResult;
    }
    /**
     * Observe the page and get suggestions.
     */
    async observe(instruction) {
        const result = await this.client.post('/session/{sessionId}/agent/observe', {
            instruction: instruction || ''
        });
        return (0, models_1.createAgentObservation)(result);
    }
    /**
     * Summarize the page content.
     */
    async summarize(instruction) {
        return this.client.post('/session/{sessionId}/agent/summarize', {
            instruction: instruction || ''
        });
    }
    /**
     * Extract data using AI.
     */
    async agentExtract(instruction, schema) {
        const result = await this.client.post('/session/{sessionId}/agent/extract', {
            instruction,
            schema: schema || {}
        });
        return (0, models_1.createExtractionResult)(result);
    }
    /**
     * Clear agent history.
     */
    async clearHistory() {
        const result = await this.client.post('/session/{sessionId}/agent/clearHistory', {});
        this._processTrace = [];
        this._stateHistory = [];
        return result === true;
    }
    /**
     * Get agent history from server.
     */
    async getHistory() {
        const result = await this.client.get('/session/{sessionId}/agent/history');
        return (0, models_1.createAgentHistory)(result);
    }
}
exports.AgenticSession = AgenticSession;
