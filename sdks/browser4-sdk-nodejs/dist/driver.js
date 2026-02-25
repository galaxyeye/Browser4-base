"use strict";
/**
 * Browser4Driver manages the lifecycle of a local Browser4.jar process.
 *
 * Provides automatic download, startup, and shutdown of the Browser4 server.
 */
var __createBinding = (this && this.__createBinding) || (Object.create ? (function(o, m, k, k2) {
    if (k2 === undefined) k2 = k;
    var desc = Object.getOwnPropertyDescriptor(m, k);
    if (!desc || ("get" in desc ? !m.__esModule : desc.writable || desc.configurable)) {
      desc = { enumerable: true, get: function() { return m[k]; } };
    }
    Object.defineProperty(o, k2, desc);
}) : (function(o, m, k, k2) {
    if (k2 === undefined) k2 = k;
    o[k2] = m[k];
}));
var __setModuleDefault = (this && this.__setModuleDefault) || (Object.create ? (function(o, v) {
    Object.defineProperty(o, "default", { enumerable: true, value: v });
}) : function(o, v) {
    o["default"] = v;
});
var __importStar = (this && this.__importStar) || (function () {
    var ownKeys = function(o) {
        ownKeys = Object.getOwnPropertyNames || function (o) {
            var ar = [];
            for (var k in o) if (Object.prototype.hasOwnProperty.call(o, k)) ar[ar.length] = k;
            return ar;
        };
        return ownKeys(o);
    };
    return function (mod) {
        if (mod && mod.__esModule) return mod;
        var result = {};
        if (mod != null) for (var k = ownKeys(mod), i = 0; i < k.length; i++) if (k[i] !== "default") __createBinding(result, mod, k[i]);
        __setModuleDefault(result, mod);
        return result;
    };
})();
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.Browser4Driver = void 0;
const child_process_1 = require("child_process");
const fs = __importStar(require("fs"));
const path = __importStar(require("path"));
const https = __importStar(require("https"));
const os = __importStar(require("os"));
const axios_1 = __importDefault(require("axios"));
class Browser4Driver {
    constructor(config = {}) {
        this.isRunning = false;
        this.homeDir = config.homeDir || path.join(os.homedir(), '.browser4');
        this.port = config.port || 8182;
        this.startupTimeout = config.startupTimeout || 60000;
        this.downloadUrl = config.downloadUrl ||
            'https://github.com/platonai/Browser4/releases/latest/download/Browser4.jar';
        this.autoDownload = config.autoDownload !== false;
        this.javaPath = config.javaPath || 'java';
    }
    /**
     * Get the base URL for the server.
     */
    get baseUrl() {
        return `http://localhost:${this.port}`;
    }
    /**
     * Get the jar file path.
     */
    get jarPath() {
        return path.join(this.homeDir, 'Browser4.jar');
    }
    /**
     * Check if the server is running.
     */
    get running() {
        return this.isRunning;
    }
    /**
     * Start the Browser4 server.
     */
    async start() {
        if (this.isRunning) {
            return;
        }
        // Ensure home directory exists
        if (!fs.existsSync(this.homeDir)) {
            fs.mkdirSync(this.homeDir, { recursive: true });
        }
        // Download jar if needed
        if (!fs.existsSync(this.jarPath)) {
            if (!this.autoDownload) {
                throw new Error(`Browser4.jar not found at ${this.jarPath} and autoDownload is disabled`);
            }
            await this.downloadJar();
        }
        // Start the server
        this.process = (0, child_process_1.spawn)(this.javaPath, [
            '-jar',
            this.jarPath,
            `--server.port=${this.port}`
        ]);
        // Handle process events
        this.process.on('error', (error) => {
            console.error('Browser4 process error:', error);
            this.isRunning = false;
        });
        this.process.on('exit', (code) => {
            console.log(`Browser4 process exited with code ${code}`);
            this.isRunning = false;
        });
        // Wait for server to be ready
        await this.waitForServer();
        this.isRunning = true;
    }
    /**
     * Stop the Browser4 server.
     */
    async stop() {
        if (!this.isRunning || !this.process) {
            return;
        }
        return new Promise((resolve) => {
            if (this.process) {
                this.process.on('exit', () => {
                    this.isRunning = false;
                    resolve();
                });
                this.process.kill();
            }
            else {
                resolve();
            }
        });
    }
    /**
     * Download the Browser4.jar file.
     */
    async downloadJar() {
        console.log(`Downloading Browser4.jar from ${this.downloadUrl}...`);
        return new Promise((resolve, reject) => {
            const file = fs.createWriteStream(this.jarPath);
            https.get(this.downloadUrl, (response) => {
                // Handle redirects
                if (response.statusCode === 301 || response.statusCode === 302) {
                    const redirectUrl = response.headers.location;
                    if (redirectUrl) {
                        https.get(redirectUrl, (redirectResponse) => {
                            redirectResponse.pipe(file);
                            file.on('finish', () => {
                                file.close();
                                console.log('Download complete');
                                resolve();
                            });
                        }).on('error', (err) => {
                            fs.unlinkSync(this.jarPath);
                            reject(err);
                        });
                    }
                    else {
                        reject(new Error('Redirect without location header'));
                    }
                    return;
                }
                response.pipe(file);
                file.on('finish', () => {
                    file.close();
                    console.log('Download complete');
                    resolve();
                });
            }).on('error', (err) => {
                fs.unlinkSync(this.jarPath);
                reject(err);
            });
        });
    }
    /**
     * Wait for the server to be ready.
     */
    async waitForServer() {
        const startTime = Date.now();
        const checkInterval = 1000;
        while (Date.now() - startTime < this.startupTimeout) {
            try {
                const response = await axios_1.default.get(`${this.baseUrl}/actuator/health`, {
                    timeout: 2000
                });
                if (response.status === 200) {
                    return;
                }
            }
            catch (error) {
                // Server not ready yet, continue waiting
            }
            await new Promise(resolve => setTimeout(resolve, checkInterval));
        }
        throw new Error(`Server failed to start within ${this.startupTimeout}ms`);
    }
    /**
     * Use with async/await pattern (similar to context manager).
     */
    async use(callback) {
        try {
            await this.start();
            return await callback(this);
        }
        finally {
            await this.stop();
        }
    }
}
exports.Browser4Driver = Browser4Driver;
