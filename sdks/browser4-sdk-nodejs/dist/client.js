"use strict";
/**
 * Thin HTTP client over the Browser4 OpenAPI.
 */
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.PulsarClient = void 0;
const axios_1 = __importDefault(require("axios"));
class PulsarClient {
    constructor(config = {}) {
        this.baseUrl = (config.baseUrl || 'http://localhost:8182').replace(/\/$/, '');
        this.timeout = config.timeout || 30000;
        this.sessionId = config.sessionId;
        const defaultHeaders = {
            'Content-Type': 'application/json',
            ...(config.defaultHeaders || {})
        };
        this.axiosInstance = axios_1.default.create({
            baseURL: this.baseUrl,
            timeout: this.timeout,
            headers: defaultHeaders
        });
    }
    requireSession(sessionId) {
        const sid = sessionId || this.sessionId;
        if (!sid) {
            throw new Error('session_id is required; call createSession() first or pass session_id explicitly');
        }
        return sid;
    }
    async request(method, path, options = {}) {
        let finalPath = path;
        // Handle session ID in path
        if (path.includes('{sessionId}')) {
            const sid = this.requireSession(options.sessionId);
            finalPath = path.replace('{sessionId}', sid);
        }
        try {
            const response = await this.axiosInstance.request({
                method,
                url: finalPath,
                data: options.body
            });
            // WebDriver responses typically wrap in { value: ... }
            if (response.data && typeof response.data === 'object' && 'value' in response.data) {
                return response.data.value;
            }
            return response.data;
        }
        catch (error) {
            if (axios_1.default.isAxiosError(error)) {
                const axiosError = error;
                const response = axiosError.response;
                if (response) {
                    const contentType = response.headers['content-type'] || '';
                    let details = '';
                    if (contentType.includes('application/json')) {
                        try {
                            details = JSON.stringify(response.data);
                        }
                        catch {
                            details = String(response.data);
                        }
                    }
                    else {
                        details = String(response.data);
                    }
                    throw new Error(`HTTP ${response.status} (url=${axiosError.config?.url}, body=${details})`);
                }
            }
            throw error;
        }
    }
    async createSession(capabilities) {
        const value = await this.request('POST', '/session', {
            body: { capabilities: capabilities || {} }
        });
        const sessionId = typeof value === 'object' && value.sessionId ? value.sessionId : null;
        if (!sessionId) {
            throw new Error('createSession response missing sessionId');
        }
        this.sessionId = sessionId;
        return sessionId;
    }
    async deleteSession(sessionId) {
        const sid = this.requireSession(sessionId);
        await this.request('DELETE', `/session/${sid}`);
    }
    async post(path, body, sessionId) {
        return this.request('POST', path, { sessionId, body });
    }
    async get(path, sessionId) {
        return this.request('GET', path, { sessionId });
    }
    async delete(path, sessionId) {
        return this.request('DELETE', path, { sessionId });
    }
    close() {
        // Axios doesn't need explicit cleanup, but we provide this for API consistency
    }
}
exports.PulsarClient = PulsarClient;
