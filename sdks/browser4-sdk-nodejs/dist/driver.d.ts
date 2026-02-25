/**
 * Browser4Driver manages the lifecycle of a local Browser4.jar process.
 *
 * Provides automatic download, startup, and shutdown of the Browser4 server.
 */
export interface Browser4DriverConfig {
    /**
     * Directory to store the Browser4.jar file.
     * Default: ~/.browser4
     */
    homeDir?: string;
    /**
     * Port for the Browser4 server.
     * Default: 8182
     */
    port?: number;
    /**
     * Startup timeout in milliseconds.
     * Default: 60000 (60 seconds)
     */
    startupTimeout?: number;
    /**
     * Download URL for Browser4.jar.
     */
    downloadUrl?: string;
    /**
     * Whether to auto-download if jar not found.
     * Default: true
     */
    autoDownload?: boolean;
    /**
     * Java executable path.
     * Default: 'java'
     */
    javaPath?: string;
}
export declare class Browser4Driver {
    private homeDir;
    private port;
    private startupTimeout;
    private downloadUrl;
    private autoDownload;
    private javaPath;
    private process?;
    private isRunning;
    constructor(config?: Browser4DriverConfig);
    /**
     * Get the base URL for the server.
     */
    get baseUrl(): string;
    /**
     * Get the jar file path.
     */
    private get jarPath();
    /**
     * Check if the server is running.
     */
    get running(): boolean;
    /**
     * Start the Browser4 server.
     */
    start(): Promise<void>;
    /**
     * Stop the Browser4 server.
     */
    stop(): Promise<void>;
    /**
     * Download the Browser4.jar file.
     */
    private downloadJar;
    /**
     * Wait for the server to be ready.
     */
    private waitForServer;
    /**
     * Use with async/await pattern (similar to context manager).
     */
    use<T>(callback: (driver: Browser4Driver) => Promise<T>): Promise<T>;
}
