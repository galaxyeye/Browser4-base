## Daily Memory - 2026-03-08

### coworker script standardization
Unified all `gh copilot` calls in coworker/worker scripts behind shared PowerShell/Bash helpers (`workers/gh-copilot.ps1/.sh`). Main runners and worker entry points now source shared config, validate `COPILOT`, build arguments with shell-native arrays, and avoid brittle quoting; PowerShell `GH_DEBUG` example was fixed. Validation: PowerShell parse checks, `bash -n`, and smoke tests confirmed consistent calls like `gh copilot --model gpt-5.4 --no-ask-user --log-level info --allow-all ...`.  
**Learning:** Shared Copilot invocation logic must live in reusable shell-native helpers, not string-built commands; every direct entry point should reuse the same base to avoid drift. On Windows, LF-normalized temp copies plus temp working dirs make Bash validation reliable.

### draft placeholder maintenance
Added automatic placeholder enforcement so `coworker/tasks/0draft` always contains `1.md`-`5.md`. PowerShell and Bash runners now ensure placeholders before queue listing and again after task completion; missing `0draft/1.md` was restored.  
**Learning:** Placeholder hygiene belongs in shared runners, and enforcing it at both startup and completion keeps PS/Bash behavior aligned.

### MCP tool-spec tightening
Restricted MCP tool-spec generation to explicitly annotated `@MCP` methods, added `WebDriver.drag(sourceSelector, targetSelector)`, and regenerated code-mirror artifacts so only intended WebDriver/agent tools are exposed. `ToolSpecGenerator` now prefers explicit annotations and case-insensitive `@mcp` KDoc sections; executor-used WebDriver and exposed `PerceptiveAgent` methods were annotated. Validation used scripted coverage/consistency checks because Maven was blocked by a pre-existing Kotlin compile-daemon issue.  
**Learning:** The tool-spec pipeline is source-text driven, so annotations and generated artifacts must stay in sync; explicit `@MCP` exposure prevents deprecated/helper overload leakage.

### WebDriver CLI/backend alignment
Aligned `browser4-cli` commands to real MCP/backend names and argument shapes (`navigate`, `go_back`, `press`, `type`, `select_option`, `tab_*`, etc.), removed unsupported exports (`console`, `pdf`), and updated snapshot/open/screenshot flows to use `open`, `aria_snapshot`, and `screenshot` with base64 file saving. Added snake_case and legacy payload normalization in `MCPToolController.kt` for backward compatibility. Validation: `npx jest tests/commands.test.ts --runInBand` and `npx tsc --noEmit` passed; targeted Maven tests hit the same pre-existing Kotlin daemon issue.  
**Learning:** Safest CLI integration is matching actual MCP tool names/params rather than keeping partial Playwright-style aliases; small controller-side normalization preserves compatibility without alias drift.

### draft refinement pipeline
Added a dedicated refinement flow moving drafts from `0draft/refine/1ready` -> `2working` -> `3done`. New `refine-drafts.ps1/.sh` resolve repo root from script location, move inputs into working, invoke shared Copilot helpers, and only move successful rewrites to done; failures remain visible in working. Added periodic runners and documented flow in `coworker/README.md` and `README.zh.md`.  
**Learning:** Standalone coworker helpers should resolve repo root from script location, not caller cwd. Using `2working` as the in-progress state and only promoting successful output to `3done` preserves failed inputs for inspection.

### Python coworker runner
Added `coworker/scripts/coworker.py`, a Python implementation of the coworker task runner mirroring repo-root discovery, task-folder lifecycle, placeholder maintenance, config-driven Copilot invocation, task naming, memory init, logging, approved-task git-sync, and completion moves. Docs were updated to cover the Python entry point and correct script paths. Validation: `py_compile`, `--help`, and a temp-repo smoke test with fake Copilot/memory helpers all passed.  
**Learning:** A Python port can stay aligned with `coworker.ps1` by preserving the same folder contract and helper-script interfaces while loading `COPILOT` config directly; temp repos with fake Copilot commands give safe end-to-end coverage without touching live queues.


Total usage est:        1 Premium request
API time spent:         14s
Total session time:     22s
Total code changes:     +0 -0
Breakdown by AI model:
 gpt-5.4                 21.0k in, 971 out, 17.9k cached (Est. 1 Premium request)


### Browser4 MCP server startup verification
Verified the Browser4 MCP server startup path from this checkout. The packaged `browser4\browser4-agents\target\Browser4.jar` is the normal Spring Boot app; the MCP server actually starts from `pulsar-agentic` via `.\mvnw.cmd -q -f pulsar-agentic\pom.xml org.codehaus.mojo:exec-maven-plugin:3.6.1:java -Dexec.mainClass=ai.platon.pulsar.agentic.mcp.server.Browser4MCPServerRunnerKt`. Validation showed the runner initialized Browser4, launched Chrome, registered 121 MCP tools, and then exited cleanly when STDIO closed because no MCP client kept stdin open.  
**Learning:** Browser4 MCP is a STDIO subprocess, not a long-running HTTP server; to keep it alive, it must be launched by an MCP client (for example Claude Desktop) or another process that keeps stdin open. When documenting or testing startup, distinguish the normal `Browser4.jar` app from the dedicated `Browser4MCPServerRunnerKt` entry point.
