# Daily Memory - 2026-03-15

- **browser4-cli process shutdown:** Implemented tracked process shutdown for `sdks/browser4-cli` so CLI-started Browser4 JVMs are recorded when spawned, `close-all` first asks reachable servers to close sessions and then stops tracked JVMs gracefully, and `kill-all` force-kills the tracked JVMs by PID. Added `managedProcesses.ts`, skipped daemon auto-start for shutdown commands, and added focused Jest coverage for process tracking and shutdown behavior.

- **Validation outcome:** `npm run build` succeeded in `sdks/browser4-cli`, and focused shutdown tests passed with `npm test -- --runInBand tests/program.test.ts tests/managedProcesses.test.ts`. The package-level Jest suite still has an unrelated pre-existing failure in `tests/commands.test.ts` where `commands['list']` is undefined, so shutdown validation was completed with targeted tests plus a successful TypeScript build.

- **Lesson learned:** shutdown commands should never auto-start the local server they are supposed to stop, and PID tracking is more reliable than brittle command-line scans for detached Java processes. For graceful Windows JVM shutdown, try a JVM-aware exit path first (`jcmd ... VM.exit`) before falling back to process termination.
