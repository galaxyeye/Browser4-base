/**
 * AgenticSession extends PulsarSession with AI-powered agent capabilities.
 */
import { PulsarSession } from './pulsar-session';
import { AgentRunResult, AgentActResult, AgentObservation, ExtractionResult, AgentHistory } from './models';
/**
 * AgenticSession provides AI-powered browser automation capabilities.
 *
 * Extends PulsarSession with agent methods for autonomous task execution,
 * mirroring the Kotlin AgenticSession interface.
 */
export declare class AgenticSession extends PulsarSession {
    private _processTrace;
    private _stateHistory;
    /**
     * Get the process trace.
     */
    get processTrace(): string[];
    /**
     * Get the state history.
     */
    get stateHistory(): AgentHistory;
    /**
     * Get the companion agent (returns self).
     */
    get companionAgent(): AgenticSession;
    /**
     * Execute a single action.
     */
    act(action: string): Promise<AgentActResult>;
    /**
     * Run an autonomous task.
     */
    run(instruction: string): Promise<AgentRunResult>;
    /**
     * Run an autonomous task (explicit method name).
     */
    agentRun(instruction: string): Promise<AgentRunResult>;
    /**
     * Observe the page and get suggestions.
     */
    observe(instruction?: string): Promise<AgentObservation>;
    /**
     * Summarize the page content.
     */
    summarize(instruction?: string): Promise<string>;
    /**
     * Extract data using AI.
     */
    agentExtract(instruction: string, schema?: any): Promise<ExtractionResult>;
    /**
     * Clear agent history.
     */
    clearHistory(): Promise<boolean>;
    /**
     * Get agent history from server.
     */
    getHistory(): Promise<AgentHistory>;
}
