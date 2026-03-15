# Browser4 CLI

A command-line interface for controlling a [Browser4](https://github.com/platonai/Browser4)
server. Designed for use by AI agents through SKILLS + CLI.

## Prerequisites

- Node.js ≥ 16
- A running Browser4 server (default port **8182**)

## Installation

```bash
npm install -g @platonai/browser4-cli
```

Or build from source:

```bash
cd sdks/browser4-cli
npm install
npm run build
npm link        # makes browser4-cli available globally
```

For automatic recompilation while editing locally:

```bash
npm run build:watch
```

If you want the globally linked `browser4-cli` command to pick up changes as you save,
run `npm link` once and keep the watch build running in a separate terminal.

## Usage

```
browser4-cli <command> [options]
```

### Commands

| Command | Description |
|---|---|
| `open [--server <url>]` | Open a new browser session |
| `goto <url>` | Navigate to a URL |
| `click <ref>` | Click an element |
| `type <text>` | Type text into the active element |
| `press <key>` | Press a keyboard key |
| `keydown <key>` | Press and hold a keyboard key |
| `keyup <key>` | Release a keyboard key |
| `mousemove <x> <y>` | Move the mouse to viewport coordinates |
| `mousedown [button]` | Press a mouse button at the current cursor position |
| `mouseup [button]` | Release a mouse button at the current cursor position |
| `mousewheel <dx> <dy>` | Scroll the mouse wheel by deltas |
| `screenshot [<file>]` | Take a screenshot |
| `snapshot` | Print the accessibility snapshot |
| `close` | Close the active session |
| `help` | Show usage information |

### Element References

The `snapshot` command returns an accessibility tree where every interactive
node is labelled with a short identifier such as `e15`. Pass this identifier
directly to `click`, `type`, or `press`; the CLI automatically converts it to
the `backend:15` selector format required by the server.

You can also pass plain CSS selectors (e.g. `.my-button`, `#search-input`) or
fully-qualified `backend:<N>` refs directly.

## Examples

```shell
# Open a new browser window
browser4-cli open

# Navigate to a page
browser4-cli goto https://browser4.io

# Inspect the page — note the eN labels on interactive nodes
browser4-cli snapshot

# Interact using refs from the snapshot
# e15 → backend:15 is handled automatically
browser4-cli click e15
browser4-cli type "page.click"
browser4-cli press Enter
browser4-cli keydown Shift
browser4-cli mousemove 150 300
browser4-cli mousewheel 0 100
browser4-cli keyup Shift

# Take a screenshot and save it to disk
browser4-cli screenshot output.png

# Use a custom server URL
browser4-cli open --server http://localhost:9090

# Close the session when done
browser4-cli close
```

## State

The CLI persists the active session ID and the last-clicked element
(for `type` / `press`) to `~/.browser4/cli-state.json`. This file is
created automatically on `open` and removed on `close`.
