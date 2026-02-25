/**
 * Data models for the NodeJS SDK.
 *
 * These models correspond to the Kotlin data classes and provide a consistent
 * interface for working with Browser4 API responses.
 */
/**
 * Reference to a DOM element, matching WebDriver element identifier.
 */
export interface ElementRef {
    'element-6066-11e4-a52e-4f735466cecf': string;
}
/**
 * Represents a web page result from load/open operations.
 * Mirrors the Kotlin WebPage class.
 */
export interface WebPage {
    url: string;
    location?: string;
    contentType?: string;
    contentLength: number;
    protocolStatus?: string;
    isNil: boolean;
    html?: string;
}
/**
 * Normalized URL result.
 * Mirrors the Kotlin NormURL class.
 */
export interface NormURL {
    spec: string;
    url: string;
    args?: string;
    isNil: boolean;
}
/**
 * Result from agent run operation.
 */
export interface AgentRunResult {
    success: boolean;
    message: string;
    historySize: number;
    processTraceSize: number;
    finalResult?: any;
    trace?: string[];
}
/**
 * Result from agent act operation.
 */
export interface AgentActResult {
    success: boolean;
    message: string;
    action?: string;
    isComplete: boolean;
    expression?: string;
    result?: any;
    trace?: string[];
}
/**
 * Single observation result from agent observe operation.
 */
export interface ObserveResult {
    locator?: string;
    domain?: string;
    method?: string;
    arguments?: Record<string, any>;
    description?: string;
    screenshotContentSummary?: string;
    currentPageContentSummary?: string;
    nextGoal?: string;
    thinking?: string;
    summary?: string;
    keyFindings?: string;
    nextSuggestions?: string[];
}
/**
 * Result from agent observe operation.
 */
export interface AgentObservation {
    observations: ObserveResult[];
}
/**
 * Result from agent extract operation.
 */
export interface ExtractionResult {
    success: boolean;
    message: string;
    data?: any;
}
/**
 * Result of field extraction with CSS selectors.
 */
export interface FieldsExtraction {
    fields: Record<string, any>;
}
/**
 * Result of a tool call execution.
 * Mirrors the Kotlin ToolCallResult class.
 */
export interface ToolCallResult {
    success: boolean;
    message: string;
    data?: any;
}
/**
 * Description of an action to be performed.
 * Mirrors the Kotlin ActionDescription class.
 */
export interface ActionDescription {
    description: string;
    parameters?: Record<string, any>;
}
/**
 * Represents a single state in agent history.
 * Contains information about a step in the agent's execution.
 * Mirrors the Kotlin AgentState class.
 */
export interface AgentState {
    step: number;
    action?: string;
    result?: any;
    success: boolean;
    message: string;
}
/**
 * Agent history tracking execution states.
 * Provides memory of what actions have been performed.
 * Mirrors the Kotlin AgentHistory class.
 */
export interface AgentHistory {
    states: AgentState[];
    hasErrors: boolean;
    finalResult?: any;
}
/**
 * Chat response from the LLM.
 * Mirrors the Kotlin ChatResponse class.
 */
export interface ChatResponse {
    content: string;
    role: string;
    model?: string;
}
/**
 * Placeholder for page event handlers.
 *
 * This class will be implemented in future tasks to support event-driven
 * page interactions similar to the Kotlin PageEventHandlers interface.
 */
export declare class PageEventHandlers {
    private _browseEventHandlers;
    private _loadEventHandlers;
    private _crawlEventHandlers;
    get browseEventHandlers(): Record<string, any>;
    get loadEventHandlers(): Record<string, any>;
    get crawlEventHandlers(): Record<string, any>;
}
export declare function createWebPage(data: any): WebPage;
export declare function createNormURL(data: any): NormURL;
export declare function createAgentRunResult(data: any): AgentRunResult;
export declare function createAgentActResult(data: any): AgentActResult;
export declare function createObserveResult(data: any): ObserveResult;
export declare function createAgentObservation(data: any): AgentObservation;
export declare function createExtractionResult(data: any): ExtractionResult;
export declare function createAgentHistory(data: any): AgentHistory;
export declare function createChatResponse(data: any): ChatResponse;
