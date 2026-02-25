"use strict";
/**
 * NodeJS SDK for Browser4 AgenticSession and WebDriver-compatible API.
 *
 * This SDK provides a NodeJS interface to the Browser4 browser automation platform,
 * enabling web scraping, data extraction, and AI-powered browser interaction.
 *
 * Key classes:
 * - Browser4Driver: Automatic download and lifecycle management of Browser4 server
 * - PulsarClient: Low-level HTTP client for API communication
 * - PulsarSession: Session management for page loading and extraction
 * - AgenticSession: AI-powered browser automation (extends PulsarSession)
 * - WebDriver: Browser control and element interaction
 *
 * Quick start:
 * ```typescript
 * import { Browser4Driver, PulsarClient, AgenticSession } from '@platonai/browser4-sdk';
 *
 * // Start Browser4 server automatically
 * const driver = new Browser4Driver();
 * await driver.use(async (d) => {
 *   // Create client and session
 *   const client = new PulsarClient({ baseUrl: d.baseUrl });
 *   const sessionId = await client.createSession();
 *   const session = new AgenticSession(client);
 *
 *   // Navigate and interact
 *   await session.driver.navigateTo('https://example.com');
 *   console.log(await session.driver.currentUrl());
 *
 *   // Use AI-powered actions
 *   const result = await session.run('scroll to the bottom of the page');
 *   console.log(result.success);
 *
 *   // Clean up
 *   await session.close();
 * });
 * ```
 */
Object.defineProperty(exports, "__esModule", { value: true });
exports.createChatResponse = exports.createAgentHistory = exports.createExtractionResult = exports.createAgentObservation = exports.createObserveResult = exports.createAgentActResult = exports.createAgentRunResult = exports.createNormURL = exports.createWebPage = exports.PageEventHandlers = exports.WebDriver = exports.AgenticSession = exports.PulsarSession = exports.Browser4Driver = exports.PulsarClient = void 0;
// Export client
var client_1 = require("./client");
Object.defineProperty(exports, "PulsarClient", { enumerable: true, get: function () { return client_1.PulsarClient; } });
// Export driver
var driver_1 = require("./driver");
Object.defineProperty(exports, "Browser4Driver", { enumerable: true, get: function () { return driver_1.Browser4Driver; } });
// Export sessions
var pulsar_session_1 = require("./pulsar-session");
Object.defineProperty(exports, "PulsarSession", { enumerable: true, get: function () { return pulsar_session_1.PulsarSession; } });
var agentic_session_1 = require("./agentic-session");
Object.defineProperty(exports, "AgenticSession", { enumerable: true, get: function () { return agentic_session_1.AgenticSession; } });
// Export webdriver
var webdriver_1 = require("./webdriver");
Object.defineProperty(exports, "WebDriver", { enumerable: true, get: function () { return webdriver_1.WebDriver; } });
// Export models
var models_1 = require("./models");
// Events
Object.defineProperty(exports, "PageEventHandlers", { enumerable: true, get: function () { return models_1.PageEventHandlers; } });
// Helper functions
Object.defineProperty(exports, "createWebPage", { enumerable: true, get: function () { return models_1.createWebPage; } });
Object.defineProperty(exports, "createNormURL", { enumerable: true, get: function () { return models_1.createNormURL; } });
Object.defineProperty(exports, "createAgentRunResult", { enumerable: true, get: function () { return models_1.createAgentRunResult; } });
Object.defineProperty(exports, "createAgentActResult", { enumerable: true, get: function () { return models_1.createAgentActResult; } });
Object.defineProperty(exports, "createObserveResult", { enumerable: true, get: function () { return models_1.createObserveResult; } });
Object.defineProperty(exports, "createAgentObservation", { enumerable: true, get: function () { return models_1.createAgentObservation; } });
Object.defineProperty(exports, "createExtractionResult", { enumerable: true, get: function () { return models_1.createExtractionResult; } });
Object.defineProperty(exports, "createAgentHistory", { enumerable: true, get: function () { return models_1.createAgentHistory; } });
Object.defineProperty(exports, "createChatResponse", { enumerable: true, get: function () { return models_1.createChatResponse; } });
