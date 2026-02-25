/**
 * PulsarSession and AgenticSession classes for browser automation.
 *
 * This module provides high-level session management for browser automation,
 * combining page loading, parsing, extraction, and AI-powered agent capabilities.
 */
import { PulsarClient } from './client';
import { WebDriver } from './webdriver';
import { WebPage, NormURL, FieldsExtraction, ChatResponse } from './models';
/**
 * PulsarSession provides methods for loading pages from storage or internet,
 * parsing them, and extracting data.
 *
 * This class mirrors the Kotlin PulsarSession interface, providing a consistent
 * API across languages for web scraping and data extraction tasks.
 */
export declare class PulsarSession {
    protected client: PulsarClient;
    private _driver?;
    private _id;
    constructor(client: PulsarClient);
    /**
     * Get the session ID (numeric).
     */
    get id(): number;
    /**
     * Get the session UUID.
     */
    get uuid(): string;
    /**
     * Get a short descriptive display text.
     */
    get display(): string;
    /**
     * Check if the session is active.
     */
    get isActive(): boolean;
    /**
     * Get the bound WebDriver instance.
     */
    get driver(): WebDriver;
    /**
     * Get or create the bound WebDriver instance.
     */
    getOrCreateBoundDriver(): WebDriver;
    /**
     * Open a URL immediately (bypass cache).
     */
    open(url: string, args?: string): Promise<WebPage>;
    /**
     * Load a URL from cache or fetch from internet.
     */
    load(url: string, args?: string): Promise<WebPage>;
    /**
     * Submit URL to crawl pool for async processing.
     */
    submit(url: string, args?: string): Promise<boolean>;
    /**
     * Normalize a URL with load arguments.
     */
    normalize(url: string, args?: string): Promise<NormURL>;
    /**
     * Parse a page into a document.
     */
    parse(page: WebPage): Promise<any>;
    /**
     * Extract fields from a document using CSS selectors.
     */
    extract(document: any, fields: Record<string, string>): Promise<FieldsExtraction>;
    /**
     * Load, parse, and extract in one operation.
     */
    scrape(url: string, fields: Record<string, string>, args?: string): Promise<FieldsExtraction>;
    /**
     * Chat with the LLM.
     */
    chat(userMessage: string, systemMessage?: string): Promise<ChatResponse>;
    /**
     * Close the session.
     */
    close(): Promise<void>;
}
