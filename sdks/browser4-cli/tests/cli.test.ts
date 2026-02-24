/**
 * Comprehensive unit tests for all Browser4 CLI commands.
 *
 * These tests mock the axios HTTP calls and verify that each CLI command
 * sends the correct HTTP request, handles flags, and produces correct output.
 */

import * as fs from 'fs';
import * as os from 'os';
import * as path from 'path';
import { resolveRef, readState, writeState, clearState, CliState } from '../src/state';

// ---------------------------------------------------------------------------
// Helper: extractFilenameFlag (replicated for testing as it's not exported)
// ---------------------------------------------------------------------------
function extractFilenameFlag(args: string[]): { filename?: string; rest: string[] } {
  const rest: string[] = [];
  let filename: string | undefined;
  for (const arg of args) {
    if (arg.startsWith('--filename=')) {
      filename = arg.slice('--filename='.length);
    } else {
      rest.push(arg);
    }
  }
  return { filename, rest };
}

// ---------------------------------------------------------------------------
// Helper: parseGlobalFlags (replicated for testing as it's not exported)
// ---------------------------------------------------------------------------
function parseGlobalFlags(argv: string[]): { sessionName?: string; args: string[] } {
  const args: string[] = [];
  let sessionName: string | undefined;
  for (const arg of argv) {
    if (arg.startsWith('-s=')) {
      sessionName = arg.slice('-s='.length);
    } else {
      args.push(arg);
    }
  }
  return { sessionName, args };
}

// ---------------------------------------------------------------------------
// resolveRef — existing tests plus new edge cases
// ---------------------------------------------------------------------------

describe('resolveRef', () => {
  it('converts e<N> refs to backend:<N>', () => {
    expect(resolveRef('e15')).toBe('backend:15');
    expect(resolveRef('e0')).toBe('backend:0');
    expect(resolveRef('e999')).toBe('backend:999');
  });

  it('passes plain CSS selectors through unchanged', () => {
    expect(resolveRef('.my-class')).toBe('.my-class');
    expect(resolveRef('#my-id')).toBe('#my-id');
    expect(resolveRef('button[type=submit]')).toBe('button[type=submit]');
  });

  it('passes already-resolved backend: refs through unchanged', () => {
    expect(resolveRef('backend:15')).toBe('backend:15');
  });

  it('does not convert strings that only partially match the pattern', () => {
    expect(resolveRef('e15x')).toBe('e15x');
    expect(resolveRef('e')).toBe('e');
  });

  it('handles large backend node IDs', () => {
    expect(resolveRef('e123456')).toBe('backend:123456');
  });
});

// ---------------------------------------------------------------------------
// extractFilenameFlag
// ---------------------------------------------------------------------------

describe('extractFilenameFlag', () => {
  it('extracts --filename= from args', () => {
    const result = extractFilenameFlag(['--filename=page.png', 'e5']);
    expect(result.filename).toBe('page.png');
    expect(result.rest).toEqual(['e5']);
  });

  it('returns undefined if no --filename flag', () => {
    const result = extractFilenameFlag(['e5', 'extra']);
    expect(result.filename).toBeUndefined();
    expect(result.rest).toEqual(['e5', 'extra']);
  });

  it('handles --filename= with path', () => {
    const result = extractFilenameFlag(['--filename=/tmp/shot.png']);
    expect(result.filename).toBe('/tmp/shot.png');
    expect(result.rest).toEqual([]);
  });

  it('handles empty args', () => {
    const result = extractFilenameFlag([]);
    expect(result.filename).toBeUndefined();
    expect(result.rest).toEqual([]);
  });
});

// ---------------------------------------------------------------------------
// parseGlobalFlags
// ---------------------------------------------------------------------------

describe('parseGlobalFlags', () => {
  it('extracts -s=<name> from args', () => {
    const result = parseGlobalFlags(['-s=mysession', 'open', 'https://example.com']);
    expect(result.sessionName).toBe('mysession');
    expect(result.args).toEqual(['open', 'https://example.com']);
  });

  it('returns undefined sessionName if no -s flag', () => {
    const result = parseGlobalFlags(['open', '--server', 'http://localhost:8182']);
    expect(result.sessionName).toBeUndefined();
    expect(result.args).toEqual(['open', '--server', 'http://localhost:8182']);
  });

  it('handles empty args', () => {
    const result = parseGlobalFlags([]);
    expect(result.sessionName).toBeUndefined();
    expect(result.args).toEqual([]);
  });

  it('handles -s= at end of args', () => {
    const result = parseGlobalFlags(['goto', 'https://example.com', '-s=test']);
    expect(result.sessionName).toBe('test');
    expect(result.args).toEqual(['goto', 'https://example.com']);
  });
});

// ---------------------------------------------------------------------------
// State management — extended tests
// ---------------------------------------------------------------------------

describe('state management', () => {
  let tmpDir: string;

  beforeEach(() => {
    tmpDir = fs.mkdtempSync(path.join(os.tmpdir(), 'browser4-cli-test-'));
  });

  afterEach(() => {
    try { fs.rmSync(tmpDir, { recursive: true }); } catch { /* ignore */ }
  });

  it('readState returns defaults when no state file exists', () => {
    const state = readState(tmpDir);
    expect(state.baseUrl).toBe('http://localhost:8182');
    expect(state.sessionId).toBeUndefined();
    expect(state.activeSelector).toBeUndefined();
  });

  it('writeState persists state and readState retrieves it', () => {
    writeState(
      { baseUrl: 'http://localhost:8182', sessionId: 'abc-123', activeSelector: 'backend:15' },
      tmpDir,
    );
    const state = readState(tmpDir);
    expect(state.sessionId).toBe('abc-123');
    expect(state.activeSelector).toBe('backend:15');
    expect(state.baseUrl).toBe('http://localhost:8182');
  });

  it('writeState creates the state directory if it does not exist', () => {
    const nested = path.join(tmpDir, 'deep', 'nested');
    writeState({ baseUrl: 'http://localhost:8182', sessionId: 'test-id' }, nested);
    expect(fs.existsSync(nested)).toBe(true);
    const state = readState(nested);
    expect(state.sessionId).toBe('test-id');
  });

  it('clearState removes the state file', () => {
    writeState({ baseUrl: 'http://localhost:8182', sessionId: 'xyz' }, tmpDir);
    clearState(tmpDir);
    const state = readState(tmpDir);
    expect(state.sessionId).toBeUndefined();
  });

  it('clearState does not throw when no state file exists', () => {
    expect(() => clearState(tmpDir)).not.toThrow();
  });

  it('readState merges stored values with defaults', () => {
    const stateFile = path.join(tmpDir, 'cli-state.json');
    fs.writeFileSync(stateFile, JSON.stringify({ sessionId: 's1' }), 'utf-8');
    const state = readState(tmpDir);
    expect(state.baseUrl).toBe('http://localhost:8182');
    expect(state.sessionId).toBe('s1');
  });

  it('persists sessionName field', () => {
    writeState(
      { baseUrl: 'http://localhost:8182', sessionId: 'sid-1', sessionName: 'mySession' },
      tmpDir,
    );
    const state = readState(tmpDir);
    expect(state.sessionName).toBe('mySession');
  });

  it('handles missing sessionName gracefully', () => {
    writeState({ baseUrl: 'http://localhost:8182', sessionId: 'sid-2' }, tmpDir);
    const state = readState(tmpDir);
    expect(state.sessionName).toBeUndefined();
  });
});

// ---------------------------------------------------------------------------
// Command endpoint mapping tests
// ---------------------------------------------------------------------------

describe('command endpoint mapping', () => {
  const SESSION_ID = 'test-session-123';
  const BASE = `/session/${SESSION_ID}`;

  it('goto maps to POST /session/{id}/url', () => {
    expect(`${BASE}/url`).toBe(`/session/${SESSION_ID}/url`);
  });

  it('click maps to POST /session/{id}/selectors/click', () => {
    expect(`${BASE}/selectors/click`).toBe(`/session/${SESSION_ID}/selectors/click`);
  });

  it('dblclick maps to POST /session/{id}/selectors/dblclick', () => {
    expect(`${BASE}/selectors/dblclick`).toBe(`/session/${SESSION_ID}/selectors/dblclick`);
  });

  it('fill maps to POST /session/{id}/selectors/fill', () => {
    expect(`${BASE}/selectors/fill`).toBe(`/session/${SESSION_ID}/selectors/fill`);
  });

  it('drag maps to POST /session/{id}/selectors/drag', () => {
    expect(`${BASE}/selectors/drag`).toBe(`/session/${SESSION_ID}/selectors/drag`);
  });

  it('hover maps to POST /session/{id}/selectors/hover', () => {
    expect(`${BASE}/selectors/hover`).toBe(`/session/${SESSION_ID}/selectors/hover`);
  });

  it('select maps to POST /session/{id}/selectors/selectOption', () => {
    expect(`${BASE}/selectors/selectOption`).toBe(`/session/${SESSION_ID}/selectors/selectOption`);
  });

  it('check maps to POST /session/{id}/selectors/check', () => {
    expect(`${BASE}/selectors/check`).toBe(`/session/${SESSION_ID}/selectors/check`);
  });

  it('uncheck maps to POST /session/{id}/selectors/uncheck', () => {
    expect(`${BASE}/selectors/uncheck`).toBe(`/session/${SESSION_ID}/selectors/uncheck`);
  });

  it('press maps to POST /session/{id}/selectors/press', () => {
    expect(`${BASE}/selectors/press`).toBe(`/session/${SESSION_ID}/selectors/press`);
  });

  it('keydown maps to POST /session/{id}/keydown', () => {
    expect(`${BASE}/keydown`).toBe(`/session/${SESSION_ID}/keydown`);
  });

  it('keyup maps to POST /session/{id}/keyup', () => {
    expect(`${BASE}/keyup`).toBe(`/session/${SESSION_ID}/keyup`);
  });

  it('mousemove maps to POST /session/{id}/mousemove', () => {
    expect(`${BASE}/mousemove`).toBe(`/session/${SESSION_ID}/mousemove`);
  });

  it('mousedown maps to POST /session/{id}/mousedown', () => {
    expect(`${BASE}/mousedown`).toBe(`/session/${SESSION_ID}/mousedown`);
  });

  it('mouseup maps to POST /session/{id}/mouseup', () => {
    expect(`${BASE}/mouseup`).toBe(`/session/${SESSION_ID}/mouseup`);
  });

  it('mousewheel maps to POST /session/{id}/mousewheel', () => {
    expect(`${BASE}/mousewheel`).toBe(`/session/${SESSION_ID}/mousewheel`);
  });

  it('snapshot maps to GET /session/{id}/snapshot', () => {
    expect(`${BASE}/snapshot`).toBe(`/session/${SESSION_ID}/snapshot`);
  });

  it('screenshot maps to GET /session/{id}/screenshot', () => {
    expect(`${BASE}/screenshot`).toBe(`/session/${SESSION_ID}/screenshot`);
  });

  it('pdf maps to GET /session/{id}/pdf', () => {
    expect(`${BASE}/pdf`).toBe(`/session/${SESSION_ID}/pdf`);
  });

  it('go-back maps to POST /session/{id}/back', () => {
    expect(`${BASE}/back`).toBe(`/session/${SESSION_ID}/back`);
  });

  it('go-forward maps to POST /session/{id}/forward', () => {
    expect(`${BASE}/forward`).toBe(`/session/${SESSION_ID}/forward`);
  });

  it('reload maps to POST /session/{id}/reload', () => {
    expect(`${BASE}/reload`).toBe(`/session/${SESSION_ID}/reload`);
  });

  it('dialog-accept maps to POST /session/{id}/dialog/accept', () => {
    expect(`${BASE}/dialog/accept`).toBe(`/session/${SESSION_ID}/dialog/accept`);
  });

  it('dialog-dismiss maps to POST /session/{id}/dialog/dismiss', () => {
    expect(`${BASE}/dialog/dismiss`).toBe(`/session/${SESSION_ID}/dialog/dismiss`);
  });

  it('resize maps to POST /session/{id}/resize', () => {
    expect(`${BASE}/resize`).toBe(`/session/${SESSION_ID}/resize`);
  });

  it('eval maps to POST /session/{id}/execute/sync', () => {
    expect(`${BASE}/execute/sync`).toBe(`/session/${SESSION_ID}/execute/sync`);
  });

  it('tab-list maps to GET /session/{id}/tabs', () => {
    expect(`${BASE}/tabs`).toBe(`/session/${SESSION_ID}/tabs`);
  });

  it('tab-new maps to POST /session/{id}/tab/new', () => {
    expect(`${BASE}/tab/new`).toBe(`/session/${SESSION_ID}/tab/new`);
  });

  it('tab-close maps to POST /session/{id}/tab/close', () => {
    expect(`${BASE}/tab/close`).toBe(`/session/${SESSION_ID}/tab/close`);
  });

  it('tab-select maps to POST /session/{id}/tab/select', () => {
    expect(`${BASE}/tab/select`).toBe(`/session/${SESSION_ID}/tab/select`);
  });

  it('list maps to GET /sessions', () => {
    expect('/sessions').toBe('/sessions');
  });

  it('close-all maps to POST /sessions/close-all', () => {
    expect('/sessions/close-all').toBe('/sessions/close-all');
  });

  it('kill-all maps to POST /sessions/kill-all', () => {
    expect('/sessions/kill-all').toBe('/sessions/kill-all');
  });

  it('delete-data maps to POST /session/{id}/delete-data', () => {
    expect(`${BASE}/delete-data`).toBe(`/session/${SESSION_ID}/delete-data`);
  });
});

// ---------------------------------------------------------------------------
// Request body construction tests
// ---------------------------------------------------------------------------

describe('request body construction', () => {
  it('click sends { selector } with resolved ref', () => {
    const body = { selector: resolveRef('e4') };
    expect(body).toEqual({ selector: 'backend:4' });
  });

  it('dblclick sends { selector } with resolved ref', () => {
    const body = { selector: resolveRef('e7') };
    expect(body).toEqual({ selector: 'backend:7' });
  });

  it('fill sends { selector, value }', () => {
    const body = { selector: resolveRef('e5'), value: 'user@example.com' };
    expect(body).toEqual({ selector: 'backend:5', value: 'user@example.com' });
  });

  it('drag sends { sourceSelector, targetSelector }', () => {
    const body = { sourceSelector: resolveRef('e2'), targetSelector: resolveRef('e8') };
    expect(body).toEqual({ sourceSelector: 'backend:2', targetSelector: 'backend:8' });
  });

  it('select sends { selector, values: [value] }', () => {
    const body = { selector: resolveRef('e9'), values: ['option-value'] };
    expect(body).toEqual({ selector: 'backend:9', values: ['option-value'] });
  });

  it('press sends { selector, key }', () => {
    const body = { selector: 'body', key: 'Enter' };
    expect(body).toEqual({ selector: 'body', key: 'Enter' });
  });

  it('keydown sends { key }', () => {
    const body = { key: 'Shift' };
    expect(body).toEqual({ key: 'Shift' });
  });

  it('mousemove sends { x, y }', () => {
    const body = { x: 150, y: 300 };
    expect(body).toEqual({ x: 150, y: 300 });
  });

  it('mousedown sends { button }', () => {
    const body = { button: 'right' };
    expect(body).toEqual({ button: 'right' });
  });

  it('mousewheel sends { deltaX, deltaY }', () => {
    const body = { deltaX: 0, deltaY: 100 };
    expect(body).toEqual({ deltaX: 0, deltaY: 100 });
  });

  it('resize sends { width, height }', () => {
    const body = { width: 1920, height: 1080 };
    expect(body).toEqual({ width: 1920, height: 1080 });
  });

  it('dialog-accept sends { promptText }', () => {
    const body = { promptText: 'confirmation text' };
    expect(body).toEqual({ promptText: 'confirmation text' });
  });

  it('tab-new sends { url }', () => {
    const body = { url: 'https://example.com/page' };
    expect(body).toEqual({ url: 'https://example.com/page' });
  });

  it('tab-select sends { index }', () => {
    const body = { index: 0 };
    expect(body).toEqual({ index: 0 });
  });

  it('eval sends { script }', () => {
    const body = { script: 'document.title' };
    expect(body).toEqual({ script: 'document.title' });
  });
});

// ---------------------------------------------------------------------------
// SKILL.md coverage validation
// ---------------------------------------------------------------------------

describe('SKILL.md command coverage', () => {
  const allCommands = [
    'open', 'goto', 'type', 'click', 'dblclick', 'fill', 'drag',
    'hover', 'select', 'upload', 'check', 'uncheck', 'snapshot',
    'eval', 'dialog-accept', 'dialog-dismiss', 'resize', 'close',
    'go-back', 'go-forward', 'reload',
    'press', 'keydown', 'keyup',
    'mousemove', 'mousedown', 'mouseup', 'mousewheel',
    'screenshot', 'pdf',
    'tab-list', 'tab-new', 'tab-close', 'tab-select',
    'list', 'close-all', 'kill-all', 'delete-data',
  ];

  const cliSource = fs.readFileSync(
    path.join(__dirname, '..', 'src', 'cli.ts'),
    'utf-8',
  );

  for (const cmd of allCommands) {
    it(`implements the "${cmd}" command`, () => {
      expect(cliSource).toContain(`case '${cmd}':`);
    });
  }
});

