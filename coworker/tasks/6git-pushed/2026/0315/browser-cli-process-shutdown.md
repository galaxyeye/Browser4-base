# Implement program shutting down by browser-cli

## Problem

Properly implement handleCloseAll/handleKillAll in program.ts to shut down running programs started by browser-cli.

HandleCloseAll calls MCPToolController.handleCloseAllSessions(), which should close all sessions and the associated processes.
- the kotlin processes should be closed gracefully, allowing them to clean up resources and save any necessary state before exiting.

HandleKillAll should kill all running processes immediately in the browser-cli context, system command can be used to kill processes by their IDs.
