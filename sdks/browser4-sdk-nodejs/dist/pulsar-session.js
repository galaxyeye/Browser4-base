"use strict";
/**
 * PulsarSession and AgenticSession classes for browser automation.
 *
 * This module provides high-level session management for browser automation,
 * combining page loading, parsing, extraction, and AI-powered agent capabilities.
 */
Object.defineProperty(exports, "__esModule", { value: true });
exports.PulsarSession = void 0;
const webdriver_1 = require("./webdriver");
const models_1 = require("./models");
/**
 * PulsarSession provides methods for loading pages from storage or internet,
 * parsing them, and extracting data.
 *
 * This class mirrors the Kotlin PulsarSession interface, providing a consistent
 * API across languages for web scraping and data extraction tasks.
 */
class PulsarSession {
    constructor(client) {
        this._id = 0;
        this.client = client;
    }
    /**
     * Get the session ID (numeric).
     */
    get id() {
        return this._id;
    }
    /**
     * Get the session UUID.
     */
    get uuid() {
        return this.client.sessionId || '';
    }
    /**
     * Get a short descriptive display text.
     */
    get display() {
        return this.uuid
            ? `PulsarSession(${this.uuid.substring(0, 8)}...)`
            : 'PulsarSession(no-session)';
    }
    /**
     * Check if the session is active.
     */
    get isActive() {
        return this.client.sessionId !== undefined;
    }
    /**
     * Get the bound WebDriver instance.
     */
    get driver() {
        if (!this._driver) {
            this._driver = new webdriver_1.WebDriver(this.client);
        }
        return this._driver;
    }
    /**
     * Get or create the bound WebDriver instance.
     */
    getOrCreateBoundDriver() {
        return this.driver;
    }
    /**
     * Open a URL immediately (bypass cache).
     */
    async open(url, args) {
        const result = await this.client.post('/session/{sessionId}/open', {
            url,
            args: args || ''
        });
        return (0, models_1.createWebPage)(result);
    }
    /**
     * Load a URL from cache or fetch from internet.
     */
    async load(url, args) {
        const result = await this.client.post('/session/{sessionId}/load', {
            url,
            args: args || ''
        });
        return (0, models_1.createWebPage)(result);
    }
    /**
     * Submit URL to crawl pool for async processing.
     */
    async submit(url, args) {
        return this.client.post('/session/{sessionId}/submit', {
            url,
            args: args || ''
        });
    }
    /**
     * Normalize a URL with load arguments.
     */
    async normalize(url, args) {
        const result = await this.client.post('/session/{sessionId}/normalize', {
            url,
            args: args || ''
        });
        return (0, models_1.createNormURL)(result);
    }
    /**
     * Parse a page into a document.
     */
    async parse(page) {
        return this.client.post('/session/{sessionId}/parse', {
            url: page.url,
            html: page.html
        });
    }
    /**
     * Extract fields from a document using CSS selectors.
     */
    async extract(document, fields) {
        const result = await this.client.post('/session/{sessionId}/extract', {
            document,
            fields
        });
        return { fields: result };
    }
    /**
     * Load, parse, and extract in one operation.
     */
    async scrape(url, fields, args) {
        const page = await this.load(url, args);
        const document = await this.parse(page);
        return this.extract(document, fields);
    }
    /**
     * Chat with the LLM.
     */
    async chat(userMessage, systemMessage) {
        const result = await this.client.post('/session/{sessionId}/chat', {
            userMessage,
            systemMessage
        });
        return (0, models_1.createChatResponse)(result);
    }
    /**
     * Close the session.
     */
    async close() {
        if (this.client.sessionId) {
            await this.client.deleteSession();
        }
    }
}
exports.PulsarSession = PulsarSession;
