"use strict";
/**
 * WebDriver class for browser control and element interaction.
 * Provides a subset of W3C WebDriver API tailored for Browser4.
 */
Object.defineProperty(exports, "__esModule", { value: true });
exports.WebDriver = void 0;
class WebDriver {
    constructor(client) {
        this._navigateHistory = [];
        this.client = client;
    }
    /**
     * Get navigation history.
     */
    get navigateHistory() {
        return [...this._navigateHistory];
    }
    /**
     * Navigate to a URL.
     */
    async navigateTo(url) {
        await this.client.post('/session/{sessionId}/url', { url });
        this._navigateHistory.push(url);
    }
    /**
     * Get the current URL.
     */
    async currentUrl() {
        return this.client.get('/session/{sessionId}/url');
    }
    /**
     * Navigate back in browser history.
     */
    async back() {
        await this.client.post('/session/{sessionId}/back', {});
    }
    /**
     * Navigate forward in browser history.
     */
    async forward() {
        await this.client.post('/session/{sessionId}/forward', {});
    }
    /**
     * Refresh the current page.
     */
    async refresh() {
        await this.client.post('/session/{sessionId}/refresh', {});
    }
    /**
     * Get the page title.
     */
    async title() {
        return this.client.get('/session/{sessionId}/title');
    }
    /**
     * Find an element using a CSS selector.
     */
    async findElement(selector) {
        return this.client.post('/session/{sessionId}/element', {
            using: 'css selector',
            value: selector
        });
    }
    /**
     * Find multiple elements using a CSS selector.
     */
    async findElements(selector) {
        return this.client.post('/session/{sessionId}/elements', {
            using: 'css selector',
            value: selector
        });
    }
    /**
     * Click an element.
     */
    async click(selector) {
        const element = await this.findElement(selector);
        await this.client.post('/session/{sessionId}/element/click', {
            elementId: this.getElementId(element)
        });
    }
    /**
     * Fill an input element with text.
     */
    async fill(selector, text) {
        const element = await this.findElement(selector);
        await this.client.post('/session/{sessionId}/element/value', {
            elementId: this.getElementId(element),
            text
        });
    }
    /**
     * Press a key on an element.
     */
    async press(selector, key) {
        const element = await this.findElement(selector);
        await this.client.post('/session/{sessionId}/element/press', {
            elementId: this.getElementId(element),
            key
        });
    }
    /**
     * Type text into an element.
     */
    async type(selector, text) {
        await this.fill(selector, text);
    }
    /**
     * Get the text content of an element.
     */
    async getText(selector) {
        const element = await this.findElement(selector);
        return this.client.get(`/session/{sessionId}/element/${this.getElementId(element)}/text`);
    }
    /**
     * Get an attribute value from an element.
     */
    async getAttribute(selector, attributeName) {
        const element = await this.findElement(selector);
        return this.client.get(`/session/{sessionId}/element/${this.getElementId(element)}/attribute/${attributeName}`);
    }
    /**
     * Check if an element exists.
     */
    async exists(selector) {
        const result = await this.client.post('/session/{sessionId}/selectors/exists', { selector });
        return result?.exists || false;
    }
    /**
     * Wait for a selector to appear.
     */
    async waitForSelector(selector, timeout = 30000) {
        const result = await this.client.post('/session/{sessionId}/selectors/waitFor', {
            selector,
            timeout
        });
        return result?.exists || false;
    }
    /**
     * Execute JavaScript in the browser.
     */
    async executeScript(script, args = []) {
        return this.client.post('/session/{sessionId}/execute/sync', {
            script,
            args
        });
    }
    /**
     * Take a screenshot.
     */
    async screenshot() {
        return this.client.get('/session/{sessionId}/screenshot');
    }
    /**
     * Scroll to an element.
     */
    async scrollTo(selector) {
        await this.client.post('/session/{sessionId}/element/scrollTo', { selector });
    }
    /**
     * Hover over an element.
     */
    async hover(selector) {
        await this.client.post('/session/{sessionId}/element/hover', { selector });
    }
    /**
     * Select an option in a select element.
     */
    async select(selector, value) {
        await this.client.post('/session/{sessionId}/element/select', {
            selector,
            value
        });
    }
    /**
     * Delay execution for a specified time.
     */
    async delay(ms) {
        await this.client.post('/session/{sessionId}/control/delay', { ms });
    }
    /**
     * Extract the element ID from an ElementRef.
     */
    getElementId(element) {
        return element['element-6066-11e4-a52e-4f735466cecf'];
    }
}
exports.WebDriver = WebDriver;
