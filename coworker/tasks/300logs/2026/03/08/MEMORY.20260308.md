## Daily Memory - 2026-03-08

### coworker script standardization
Standardized all `gh copilot` calls behind shared shell-native helpers: `coworker/scripts/workers/gh-copilot.ps1` and `.sh`. Runners and worker entry points now source shared config, validate `COPILOT`, build args with native arrays, and avoid brittle quoting; PowerShell `GH_DEBUG` handling was corrected. Validation covered PowerShell parse checks, `bash -n`, and smoke tests.
**Learning:** Centralize Copilot invocation in reusable helpers and make every entry point consume them to prevent drift. On Windows, LF-normalized temp copies plus temp working dirs make Bash validation dependable.

### draft placeholder maintenance
Added automatic enforcement so `coworker/tasks/0draft` always contains `1.md`-`5.md`. PowerShell and Bash runners now repair placeholders both before queue listing and after task completion; missing `0draft/1.md` was restored.
**Learning:** Placeholder hygiene belongs in shared runners, and enforcing it at startup + completion keeps PS/Bash behavior aligned.

### MCP tool-spec tightening
Tool-spec generation now exposes only explicitly annotated `@MCP` methods. Added `WebDriver.drag(sourceSelector, targetSelector)`, updated `ToolSpecGenerator` to prefer explicit annotations and case-insensitive `@mcp` KDoc sections, annotated executor-used `WebDriver` and exposed `PerceptiveAgent` methods, and regenerated code-mirror artifacts.
**Learning:** The spec pipeline is source-text driven, so annotations and generated artifacts must stay synchronized. Explicit `@MCP` exposure prevents deprecated/helper overload leakage.

### WebDriver CLI/backend alignment
Aligned `browser4-cli` commands with real MCP/backend tool names and payloads (`navigate`, `go_back`, `press`, `type`, `select_option`, `tab_*`, etc.), removed unsupported exports (`console`, `pdf`), and updated snapshot/open/screenshot flows to use `open`, `aria_snapshot`, and `screenshot` with base64 file saving. Added snake_case and legacy payload normalization in `MCPToolController.kt` for backward compatibility.
**Learning:** Match actual MCP tool names/params instead of preserving partial Playwright-style aliases; small controller-side normalization preserves compatibility without alias drift.

### draft refinement pipeline
Added a dedicated refinement flow: `0draft/refine/1ready -> 2working -> 3done`. New `refine-drafts.ps1/.sh` resolve repo root from script location, move inputs into working, invoke shared Copilot helpers, and promote only successful rewrites to done; failures remain in working. Docs updated in `coworker/README.md` and `README.zh.md`.
**Learning:** Standalone helpers should resolve repo root from script location, not caller cwd. Using `2working` as explicit in-progress state preserves failed inputs for inspection.

### Python coworker runner
Added `coworker/scripts/coworker.py`, a Python runner matching the existing coworker lifecycle: repo-root discovery, task-folder lifecycle, placeholder maintenance, config-driven Copilot invocation, naming, memory init, logging, approved-task git sync, and completion moves. Docs updated; validation used `py_compile`, `--help`, and temp-repo smoke tests with fake helpers.
**Learning:** A Python port can stay aligned by preserving the same folder contract and helper interfaces while loading `COPILOT` config directly. Temp repos with fake Copilot commands provide safe end-to-end coverage.

### Browser4 MCP server startup verification
Verified that Browser4 MCP does **not** start from the packaged `browser4\browser4-agents\target\Browser4.jar`. The MCP server is launched from `pulsar-agentic` via the Maven exec entry point `Browser4MCPServerRunnerKt`, which initializes Browser4, launches Chrome, registers tools, and exits when STDIO closes.
**Learning:** Browser4 MCP is a STDIO subprocess, not a long-running HTTP server. Keep docs/tests clear about the difference between the normal Spring Boot app and the dedicated MCP runner.


Total usage est:        1 Premium request
API time spent:         39s
Total session time:     50s
Total code changes:     +0 -0
Breakdown by AI model:
 gpt-5.4                 20.9k in, 892 out, 18.9k cached (Est. 1 Premium request)


### unified scheduled coworker tasks
Implemented a new PowerShell-first scheduler via `coworker/scripts/coworker-scheduler.ps1` and `coworker/scripts/coworker-scheduler.config.psd1` so a single Windows Task Scheduler trigger can manage recurring coworker jobs. The scheduler launches coworker, draft refinement, and task-source monitoring in separate child processes, writes per-task stdout/stderr logs plus a shared status JSON file, and supports config-driven enablement, intervals, arguments, and dependency ordering through `DependsOn`; `run_coworker_periodically.ps1` gained `-Once` so the legacy flow still works while the scheduler can invoke it safely. Validation covered PowerShell parse checks and temp-config smoke tests that verified dependency-aware one-shot runs and optional config defaults.
**Learning:** A unified one-shot scheduler still needs dependency awareness, otherwise producer tasks can finish after consumer checks and leave new work for the next cycle. Under `Set-StrictMode`, imported config hashtables should be read via key checks/default helpers instead of direct property access, or optional settings become accidental hard requirements.
