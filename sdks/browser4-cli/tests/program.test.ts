/**
 * Tests for browser4-cli program.ts — argument parsing and command dispatch.
 */

import * as fs from 'fs';
import * as os from 'os';
import * as path from 'path';

const mockPost = jest.fn();
const mockEnsureServerRunning = jest.fn();

type MockCliState = {
    sessionId?: string;
    baseUrl: string;
    activeSelector?: string;
    sessionName?: string;
    sessions?: Record<string, {sessionId?: string; activeSelector?: string}>;
};

let mockState: MockCliState = {baseUrl: 'http://localhost:8182'};

function cloneState(state: MockCliState): MockCliState {
    return JSON.parse(JSON.stringify(state));
}

function resolveSessionState(state: MockCliState, sessionName?: string): MockCliState {
    if (!sessionName) {
        return cloneState(state);
    }

    const named = state.sessions?.[sessionName];
    if (!named) {
        return {
            ...cloneState(state),
            sessionName,
            sessionId: undefined,
            activeSelector: undefined,
        };
    }

    return {
        ...cloneState(state),
        sessionName,
        sessionId: named.sessionId,
        activeSelector: named.activeSelector,
    };
}

function writeSessionState(
    state: MockCliState,
    sessionName: string | undefined,
    sessionState: {sessionId?: string; activeSelector?: string},
): MockCliState {
    if (!sessionName) {
        return {
            ...state,
            sessionId: sessionState.sessionId,
            activeSelector: sessionState.activeSelector,
        };
    }

    return {
        ...state,
        sessionName,
        sessions: {
            ...(state.sessions ?? {}),
            [sessionName]: sessionState,
        },
    };
}

function clearSessionState(state: MockCliState, sessionName?: string): MockCliState {
    if (!sessionName) {
        const {sessionId: _sessionId, activeSelector: _activeSelector, ...rest} = state;
        return rest;
    }

    const sessions = {...(state.sessions ?? {})};
    delete sessions[sessionName];
    return {
        ...state,
        sessionName: state.sessionName === sessionName ? undefined : state.sessionName,
        sessions,
    };
}

jest.mock('axios', () => ({
    __esModule: true,
    default: {
        create: jest.fn(() => ({
            post: mockPost,
        })),
    },
}));

jest.mock('../src/state', () => ({
    readState: jest.fn(() => cloneState(mockState)),
    writeState: jest.fn((nextState: MockCliState) => {
        mockState = cloneState(nextState);
    }),
    clearState: jest.fn(() => {
        mockState = {baseUrl: 'http://localhost:8182'};
    }),
    resolveSessionState: jest.fn((state: MockCliState, sessionName?: string) => resolveSessionState(state, sessionName)),
    writeSessionState: jest.fn((state: MockCliState, sessionName: string | undefined, sessionState: {sessionId?: string; activeSelector?: string}) => (
        writeSessionState(state, sessionName, sessionState)
    )),
    clearSessionState: jest.fn((state: MockCliState, sessionName?: string) => clearSessionState(state, sessionName)),
}));

jest.mock('../src/cli/daemon/daemon', () => ({
    ensureServerRunning: jest.fn((args: string[]) => mockEnsureServerRunning(args)),
}));

import {parseGlobalFlags, parseRawArgs, runCli} from '../src/program';

function toolResponse(text: string, isError = false) {
    return {
        data: {
            content: [{text}],
            isError,
        },
    };
}

describe('parseRawArgs', () => {
    it('should parse positional arguments', () => {
        const result = parseRawArgs(['goto', 'https://example.com']);
        expect(result._).toEqual(['goto', 'https://example.com']);
    });

    it('should parse --key=value options', () => {
        const result = parseRawArgs(['screenshot', '--filename=page.png']);
        expect(result._).toEqual(['screenshot']);
        expect(result.filename).toBe('page.png');
    });

    it('should parse --flag as boolean true', () => {
        const result = parseRawArgs(['type', 'hello', '--submit']);
        expect(result._).toEqual(['type', 'hello']);
        expect(result.submit).toBe(true);
    });

    it('should parse --flag=true as boolean true', () => {
        const result = parseRawArgs(['fill', 'e5', 'text', '--submit=true']);
        expect(result._).toEqual(['fill', 'e5', 'text']);
        expect(result.submit).toBe(true);
    });

    it('should parse --flag=false as boolean false', () => {
        const result = parseRawArgs(['fill', 'e5', 'text', '--submit=false']);
        expect(result._).toEqual(['fill', 'e5', 'text']);
        expect(result.submit).toBe(false);
    });

    it('should handle multiple options', () => {
        const result = parseRawArgs(['screenshot', '--filename=page.png', '--full-page']);
        expect(result._).toEqual(['screenshot']);
        expect(result.filename).toBe('page.png');
        expect(result['full-page']).toBe(true);
    });

    it('should handle empty args', () => {
        const result = parseRawArgs([]);
        expect(result._).toEqual([]);
    });
});

describe('parseGlobalFlags', () => {
    it('should extract -s=<name> session flag', () => {
        const result = parseGlobalFlags(['-s=mySession', 'goto', 'https://example.com']);
        expect(result.sessionName).toBe('mySession');
        expect(result.args).toEqual(['goto', 'https://example.com']);
    });

    it('should extract --server=<url> flag', () => {
        const result = parseGlobalFlags(['--server=http://remote:8182', 'goto', 'https://example.com']);
        expect(result.serverUrl).toBe('http://remote:8182');
        expect(result.args).toEqual(['goto', 'https://example.com']);
    });

    it('should extract --server <url> flag (space separated)', () => {
        const result = parseGlobalFlags(['--server', 'http://remote:8182', 'goto', 'https://example.com']);
        expect(result.serverUrl).toBe('http://remote:8182');
        expect(result.args).toEqual(['goto', 'https://example.com']);
    });
});

describe('runCli', () => {
    let tempDir: string;
    let originalCwd: string;
    let logSpy: jest.SpyInstance;

    beforeEach(() => {
        mockPost.mockReset();
        mockEnsureServerRunning.mockReset();
        mockState = {baseUrl: 'http://localhost:8182'};
        originalCwd = process.cwd();
        tempDir = fs.mkdtempSync(path.join(os.tmpdir(), 'browser4-cli-test-'));
        process.chdir(tempDir);
        logSpy = jest.spyOn(console, 'log').mockImplementation(() => undefined);
    });

    afterEach(() => {
        logSpy.mockRestore();
        process.chdir(originalCwd);
        fs.rmSync(tempDir, {recursive: true, force: true});
    });

    it('persists the active selector after element commands and writes a snapshot file', async () => {
        mockState = {
            baseUrl: 'http://localhost:8182',
            sessionId: 'sid-1',
        };
        mockPost.mockImplementation(async (_url: string, body: {tool: string}) => {
            switch (body.tool) {
                case 'click':
                    return toolResponse('clicked');
                case 'page_url':
                    return toolResponse('https://example.com/');
                case 'page_title':
                    return toolResponse('Example Domain');
                case 'aria_snapshot':
                    return toolResponse('page:\n  ref: e15\n');
                default:
                    throw new Error(`Unexpected tool: ${body.tool}`);
            }
        });

        await runCli(['click', 'e15']);

        expect(mockEnsureServerRunning).toHaveBeenCalledWith(['e15']);
        expect(mockPost).toHaveBeenCalledWith('/mcp/call-tool', {
            tool: 'click',
            arguments: {
                selector: 'e15',
                sessionId: 'sid-1',
            },
        });
        expect(mockState.activeSelector).toBe('e15');

        const snapshotDir = path.join(tempDir, '.browser4-cli', 'snapshot');
        const snapshotFiles = fs.readdirSync(snapshotDir);
        expect(snapshotFiles).toHaveLength(1);
        expect(fs.readFileSync(path.join(snapshotDir, snapshotFiles[0]), 'utf-8')).toContain('ref: e15');
    });

    it('uses the persisted active selector for type', async () => {
        mockState = {
            baseUrl: 'http://localhost:8182',
            sessionId: 'sid-1',
            activeSelector: 'e15',
        };
        mockPost.mockImplementation(async (_url: string, body: {tool: string}) => {
            switch (body.tool) {
                case 'type':
                    return toolResponse('typed');
                case 'page_url':
                case 'page_title':
                case 'aria_snapshot':
                    return toolResponse('ok');
                default:
                    throw new Error(`Unexpected tool: ${body.tool}`);
            }
        });

        await runCli(['type', 'hello world']);

        expect(mockPost).toHaveBeenCalledWith('/mcp/call-tool', {
            tool: 'type',
            arguments: {
                selector: 'e15',
                text: 'hello world',
                sessionId: 'sid-1',
            },
        });
    });

    it('submits Enter after fill when --submit is provided', async () => {
        mockState = {
            baseUrl: 'http://localhost:8182',
            sessionId: 'sid-1',
        };
        mockPost.mockImplementation(async (_url: string, body: {tool: string}) => {
            switch (body.tool) {
                case 'fill':
                    return toolResponse('filled');
                case 'press':
                    return toolResponse('pressed');
                case 'page_url':
                case 'page_title':
                case 'aria_snapshot':
                    return toolResponse('ok');
                default:
                    throw new Error(`Unexpected tool: ${body.tool}`);
            }
        });

        await runCli(['fill', 'e5', 'abc', '--submit']);

        expect(mockPost).toHaveBeenCalledWith('/mcp/call-tool', {
            tool: 'fill',
            arguments: {
                selector: 'e5',
                text: 'abc',
                sessionId: 'sid-1',
            },
        });
        expect(mockPost).toHaveBeenCalledWith('/mcp/call-tool', {
            tool: 'press',
            arguments: {
                selector: 'e5',
                key: 'Enter',
                sessionId: 'sid-1',
            },
        });
    });

    it('stores named sessions and reuses them for later commands', async () => {
        mockPost.mockImplementation(async (_url: string, body: {tool: string; arguments: Record<string, unknown>}) => {
            switch (body.tool) {
                case 'open_session':
                    return toolResponse('{"sessionId":"named-1"}');
                case 'navigate':
                    return toolResponse('navigated');
                case 'click':
                    return toolResponse('clicked');
                case 'page_url':
                case 'page_title':
                case 'aria_snapshot':
                    return toolResponse('ok');
                default:
                    throw new Error(`Unexpected tool: ${body.tool}`);
            }
        });

        await runCli(['-s=work', 'open', 'https://example.com', '--persistent', '--profile=C:\\temp\\profile']);

        expect(mockPost).toHaveBeenCalledWith('/mcp/call-tool', {
            tool: 'open_session',
            arguments: {
                capabilities: {
                    persistent: true,
                    profile: 'C:\\temp\\profile',
                },
            },
        });
        expect(mockState.sessions?.work?.sessionId).toBe('named-1');

        mockPost.mockClear();
        mockPost.mockImplementation(async (_url: string, body: {tool: string}) => {
            switch (body.tool) {
                case 'click':
                    return toolResponse('clicked');
                case 'page_url':
                case 'page_title':
                case 'aria_snapshot':
                    return toolResponse('ok');
                default:
                    throw new Error(`Unexpected tool: ${body.tool}`);
            }
        });

        await runCli(['-s=work', 'click', 'e6']);

        expect(mockPost).toHaveBeenCalledWith('/mcp/call-tool', {
            tool: 'click',
            arguments: {
                selector: 'e6',
                sessionId: 'named-1',
            },
        });
        expect(mockState.sessions?.work?.activeSelector).toBe('e6');
    });

    it('throws when type is used without an active element', async () => {
        mockState = {
            baseUrl: 'http://localhost:8182',
            sessionId: 'sid-1',
        };
        mockPost.mockResolvedValue(toolResponse('unused'));

        await expect(runCli(['type', 'hello world'])).rejects.toThrow(
            'No active element for type. Use an element-targeting command like click, hover, or fill first.',
        );
    });
});
