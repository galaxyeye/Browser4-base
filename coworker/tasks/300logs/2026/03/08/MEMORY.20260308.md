## Daily Memory - 2026-03-08

### coworker-script-config-loading / standardize-gh-copilot-calls
**Summary:** Standardized coworker and worker gh copilot invocation around shared shell-specific config/helpers so all main, delegated, streamed, captured, and direct worker runs use the same base CLI flags and argument-building behavior.

**Changes:** Updated `coworker.ps1`/`coworker.sh` and worker entry points (`refine-last-draft`, `coworker-memory-generator`, `coworker-daily-memory-generator`, `git-sync`, `rename`) to source shared config/helpers, validate `COPILOT`, reuse shell-native command arrays, and avoid fragile string-built quoting. Added reusable `workers/gh-copilot.ps1` and `gh-copilot.sh` helpers for config loading, arg formatting, invocation, and process launching; fixed the invalid PowerShell `GH_DEBUG` example.

**Validation:** PowerShell parsing passed; helper smoke tests produced the expected `gh copilot --model gpt-5.4 --no-ask-user --log-level info --allow-all ...` command; Bash checks passed via `bash -n` on LF-normalized temp copies and temp-dir smoke tests.

**Key Learnings:** Shared Copilot command config should be shell-native arrays/helpers, not strings. Every direct Copilot entry point should validate and reuse the shared base or behavior drifts. On Windows, LF-normalized temp copies/temp working dirs make Bash validation reliable for CRLF/path-conversion issues.

### create-missing-draft-files
**Summary:** Added automatic placeholder maintenance so `coworker/tasks/0draft` always contains `1.md` through `5.md`.

**Changes:** Added placeholder ensure functions to both PowerShell and Bash runners; wired them to run before queue listing and after moving a finished task; restored missing `coworker/tasks/0draft/1.md`.

**Validation:** PowerShell parser passed; Bash validation passed on LF-normalized temp copy; confirmed `0draft` contains `1.md`-`5.md`.

**Key Learnings:** Placeholder hygiene belongs in shared runners, and enforcing it both at startup and after task completion keeps PowerShell/Bash behavior aligned.

### webdriver-mcp-toolspec-update
**Summary:** Tightened MCP tool-spec generation to explicit `@MCP` annotations, added `WebDriver.drag(sourceSelector, targetSelector)`, and refreshed code-mirror artifacts so only annotated WebDriver/agent tools are exposed.

**Changes:** Added `@MCP` coverage for WebDriver methods used by `WebDriverToolExecutor`; implemented default drag via offsets + `dragAndDrop`; updated `ToolSpecGenerator` to emit only annotated methods, prefer case-insensitive `@mcp` KDoc sections, otherwise fall back to full KDoc/humanized names; simplified executor wiring; annotated MCP-visible `PerceptiveAgent` entrypoints; refreshed generated artifacts and focused tests.

**Validation:** Scripted coverage check confirmed executor-called WebDriver methods map to `@MCP` methods; code-mirror artifacts regenerated; Maven validation was blocked by a pre-existing Kotlin compile-daemon connection failure.

**Key Learnings:** Browser4’s tool-spec pipeline is source-text driven, so annotations and generated code-mirror artifacts must stay synchronized. Restricting exposure to explicit `@MCP` removes deprecated/helper overload leakage; when Maven is blocked, scripted consistency checks still provide useful evidence.

### webdriver-tool-alignment
**Summary:** Realigned browser4-cli’s supported WebDriver command surface with actual MCP/backend behavior and added REST-side compatibility normalization for legacy callers.

**Changes:** Updated CLI commands to use real MCP names/args (`navigate`, `go_back`, `press`, `type`, `select_option`, `tab_*`, etc.); removed unsupported exported commands like `console`/`pdf`; updated `program.ts` snapshot/open/screenshot flows to call `open`, `aria_snapshot`, and `screenshot` with base64 file saving; added snake_case and legacy payload normalization in `MCPToolController.kt`; refreshed parser/controller/E2E expectations.

**Validation:** Passed `npx jest tests/commands.test.ts --runInBand` and `npx tsc --noEmit` in `sdks/browser4-cli`; targeted Maven controller test remained blocked by the same pre-existing Kotlin compile-daemon issue.

**Key Learnings:** Safest CLI alignment is to match real MCP tool names and parameter shapes, not partial Playwright-style aliases. Small controller-side normalization preserves backward compatibility without reintroducing alias drift.


Total usage est:        1 Premium request
API time spent:         15s
Total session time:     23s
Total code changes:     +0 -0
Breakdown by AI model:
 gpt-5.4                 21.3k in, 1.0k out, 16.9k cached (Est. 1 Premium request)

