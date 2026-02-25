/**
 * WebDriver class for browser control and element interaction.
 * Provides a subset of W3C WebDriver API tailored for Browser4.
 */
import { PulsarClient } from './client';
import { ElementRef } from './models';
export declare class WebDriver {
    private client;
    private _navigateHistory;
    constructor(client: PulsarClient);
    /**
     * Get navigation history.
     */
    get navigateHistory(): string[];
    /**
     * Navigate to a URL.
     */
    navigateTo(url: string): Promise<void>;
    /**
     * Get the current URL.
     */
    currentUrl(): Promise<string>;
    /**
     * Navigate back in browser history.
     */
    back(): Promise<void>;
    /**
     * Navigate forward in browser history.
     */
    forward(): Promise<void>;
    /**
     * Refresh the current page.
     */
    refresh(): Promise<void>;
    /**
     * Get the page title.
     */
    title(): Promise<string>;
    /**
     * Find an element using a CSS selector.
     */
    findElement(selector: string): Promise<ElementRef>;
    /**
     * Find multiple elements using a CSS selector.
     */
    findElements(selector: string): Promise<ElementRef[]>;
    /**
     * Click an element.
     */
    click(selector: string): Promise<void>;
    /**
     * Fill an input element with text.
     */
    fill(selector: string, text: string): Promise<void>;
    /**
     * Press a key on an element.
     */
    press(selector: string, key: string): Promise<void>;
    /**
     * Type text into an element.
     */
    type(selector: string, text: string): Promise<void>;
    /**
     * Get the text content of an element.
     */
    getText(selector: string): Promise<string>;
    /**
     * Get an attribute value from an element.
     */
    getAttribute(selector: string, attributeName: string): Promise<string | null>;
    /**
     * Check if an element exists.
     */
    exists(selector: string): Promise<boolean>;
    /**
     * Wait for a selector to appear.
     */
    waitForSelector(selector: string, timeout?: number): Promise<boolean>;
    /**
     * Execute JavaScript in the browser.
     */
    executeScript(script: string, args?: any[]): Promise<any>;
    /**
     * Take a screenshot.
     */
    screenshot(): Promise<string>;
    /**
     * Scroll to an element.
     */
    scrollTo(selector: string): Promise<void>;
    /**
     * Hover over an element.
     */
    hover(selector: string): Promise<void>;
    /**
     * Select an option in a select element.
     */
    select(selector: string, value: string): Promise<void>;
    /**
     * Delay execution for a specified time.
     */
    delay(ms: number): Promise<void>;
    /**
     * Extract the element ID from an ElementRef.
     */
    private getElementId;
}
