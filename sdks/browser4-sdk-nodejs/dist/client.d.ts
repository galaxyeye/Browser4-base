/**
 * Thin HTTP client over the Browser4 OpenAPI.
 */
export interface PulsarClientConfig {
    baseUrl?: string;
    timeout?: number;
    sessionId?: string;
    defaultHeaders?: Record<string, string>;
}
export declare class PulsarClient {
    private baseUrl;
    private timeout;
    private axiosInstance;
    sessionId?: string;
    constructor(config?: PulsarClientConfig);
    private requireSession;
    private request;
    createSession(capabilities?: Record<string, any>): Promise<string>;
    deleteSession(sessionId?: string): Promise<void>;
    post(path: string, body: any, sessionId?: string): Promise<any>;
    get(path: string, sessionId?: string): Promise<any>;
    delete(path: string, sessionId?: string): Promise<any>;
    close(): void;
}
