"use strict";
/**
 * Data models for the NodeJS SDK.
 *
 * These models correspond to the Kotlin data classes and provide a consistent
 * interface for working with Browser4 API responses.
 */
Object.defineProperty(exports, "__esModule", { value: true });
exports.PageEventHandlers = void 0;
exports.createWebPage = createWebPage;
exports.createNormURL = createNormURL;
exports.createAgentRunResult = createAgentRunResult;
exports.createAgentActResult = createAgentActResult;
exports.createObserveResult = createObserveResult;
exports.createAgentObservation = createAgentObservation;
exports.createExtractionResult = createExtractionResult;
exports.createAgentHistory = createAgentHistory;
exports.createChatResponse = createChatResponse;
/**
 * Placeholder for page event handlers.
 *
 * This class will be implemented in future tasks to support event-driven
 * page interactions similar to the Kotlin PageEventHandlers interface.
 */
class PageEventHandlers {
    constructor() {
        this._browseEventHandlers = {};
        this._loadEventHandlers = {};
        this._crawlEventHandlers = {};
    }
    get browseEventHandlers() {
        return this._browseEventHandlers;
    }
    get loadEventHandlers() {
        return this._loadEventHandlers;
    }
    get crawlEventHandlers() {
        return this._crawlEventHandlers;
    }
}
exports.PageEventHandlers = PageEventHandlers;
// Helper functions for creating models from API responses
function createWebPage(data) {
    return {
        url: data.url || '',
        location: data.location,
        contentType: data.contentType,
        contentLength: data.contentLength || 0,
        protocolStatus: data.protocolStatus,
        isNil: data.isNil || false,
        html: data.html
    };
}
function createNormURL(data) {
    return {
        spec: data.spec || '',
        url: data.url || '',
        args: data.args,
        isNil: data.isNil || false
    };
}
function createAgentRunResult(data) {
    return {
        success: data.success || false,
        message: data.message || '',
        historySize: data.historySize || 0,
        processTraceSize: data.processTraceSize || 0,
        finalResult: data.finalResult,
        trace: data.trace
    };
}
function createAgentActResult(data) {
    return {
        success: data.success || false,
        message: data.message || '',
        action: data.action,
        isComplete: data.isComplete || false,
        expression: data.expression,
        result: data.result,
        trace: data.trace
    };
}
function createObserveResult(data) {
    return {
        locator: data.locator,
        domain: data.domain,
        method: data.method,
        arguments: data.arguments,
        description: data.description,
        screenshotContentSummary: data.screenshotContentSummary,
        currentPageContentSummary: data.currentPageContentSummary,
        nextGoal: data.nextGoal,
        thinking: data.thinking,
        summary: data.summary,
        keyFindings: data.keyFindings,
        nextSuggestions: data.nextSuggestions
    };
}
function createAgentObservation(data) {
    if (Array.isArray(data)) {
        return {
            observations: data.map(item => typeof item === 'object' ? createObserveResult(item) : item)
        };
    }
    return { observations: [] };
}
function createExtractionResult(data) {
    return {
        success: data.success || false,
        message: data.message || '',
        data: data.data
    };
}
function createAgentHistory(data) {
    const statesList = data.states || [];
    const states = statesList.map((state) => ({
        step: state.step || 0,
        action: state.action,
        result: state.result,
        success: state.success || false,
        message: state.message || ''
    }));
    return {
        states,
        hasErrors: data.hasErrors || false,
        finalResult: data.finalResult
    };
}
function createChatResponse(data) {
    if (typeof data === 'string') {
        return {
            content: data,
            role: 'assistant'
        };
    }
    if (typeof data === 'object' && data !== null) {
        return {
            content: data.content || '',
            role: data.role || 'assistant',
            model: data.model
        };
    }
    return {
        content: '',
        role: 'assistant'
    };
}
