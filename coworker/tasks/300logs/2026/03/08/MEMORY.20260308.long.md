# Daily Memory - 2026-03-08

## coworker-script-config-loading
**Summary:** Standardized coworker runners/workers to load shared Copilot config from `coworker/scripts/config.ps1` and `config.sh`, so main, delegated, streamed, and captured runs use the same CLI flags.

**Changes:** Replaced string-built commands with native shell arrays; updated `coworker.ps1`/`coworker.sh` to source config, validate `COPILOT`, and reuse it for naming/execution; applied the same pattern to direct worker entry points (`refine-last-draft`, `coworker-memory-generator`, `coworker-daily-memory-generator`) in PowerShell and Bash.

**Validation:** PowerShell parsing passed; verified PowerShell array expands to `gh copilot --model gpt-5.4 --no-ask-user --log-level info --allow-all`; confirmed workers append prompt/tool flags to the shared base; validated Bash scripts with `bash -n` on LF-normalized temp copies; confirmed `config.sh` exports a valid `COPILOT` array beginning with `gh copilot`.

**Key Learnings:** Shared CLI config should use shell-native arrays, not strings, when callers need safe argument appends. Any direct Copilot entry point must validate and reuse the shared array or delegated flows drift. On Windows, Bash syntax validation is more reliable on LF-normalized temp copies for CRLF files. Stronger tooling pattern: shared shell-specific command-array config -> per-entrypoint validation -> reuse across primary/delegated flows.

## create-missing-draft-files
**Summary:** Added automatic placeholder maintenance so `coworker/tasks/0draft` always contains `1.md` through `5.md` at runner startup and after task completion.

**Changes:** Added `Ensure-DraftPlaceholders` to `coworker/scripts/coworker.ps1` and `ensure_draft_placeholders` to `coworker/scripts/coworker.sh`; wired both runners to recreate missing numbered drafts before queue listing and after moving a finished task; restored missing `coworker/tasks/0draft/1.md`.

**Validation:** PowerShell runner parsed; Bash runner passed `bash -n` on LF-normalized temp copy; verified `coworker/tasks/0draft` contains `1.md` to `5.md`.

**Key Learnings:** Placeholder maintenance belongs in shared runners so PowerShell/Bash stay aligned. Checking both at startup and after completion preserves repo hygiene and automation consistency.

## webdriver-mcp-toolspec-update
**Summary:** Tightened MCP tool-spec generation around explicit `@MCP` annotations, added `WebDriver.drag(sourceSelector, targetSelector)`, and refreshed code-mirror artifacts so only annotated WebDriver/agent tools are exposed.

**Changes:** Added `@MCP` coverage for WebDriver methods used by `WebDriverToolExecutor`; implemented default `WebDriver.drag` via computed offsets and `dragAndDrop`; updated `ToolSpecGenerator` to emit only `@MCP` methods, prefer case-insensitive `@mcp` KDoc sections, otherwise use full KDoc/humanized names; simplified `WebDriverToolExecutor`, annotated MCP-visible `PerceptiveAgent` entrypoints, added focused tests, refreshed code-mirror JSON/text artifacts.

**Validation:** Scripted coverage check confirmed every `WebDriverToolExecutor.callFunctionOn` WebDriver method maps to an `@MCP`-annotated function; regenerated code-mirror artifacts; Maven validation was blocked by a pre-existing Kotlin compile-daemon connection failure before the changed module.

**Key Learnings:** Browser4’s tool-spec pipeline is source-text driven, so `@MCP` annotations and code-mirror artifacts must stay synchronized. Restricting spec generation to explicit annotations removes deprecated/helper overloads from MCP exposure and makes KDoc tagging rules prompt-visible. When Kotlin daemon failures block builds, scripted consistency checks still provide useful evidence.

## webdriver-tool-alignment
**Summary:** Realigned browser4-cli’s supported WebDriver command surface with actual MCP/WebDriver behavior by using backend-supported tool names/args, removing unsupported exported commands, and normalizing REST snake_case aliases for legacy callers.

**Changes:** Updated `sdks/browser4-cli/src/cli/daemon/commands.ts` to use real MCP names/args (`navigate`, `go_back`, `press`, `type`, `select_option`, `tab_*`, etc.); removed unsupported `console`/`pdf` from exported supported commands; updated `program.ts` snapshot/open/screenshot flows to call `open`, `aria_snapshot`, and `screenshot`, saving screenshot output from base64; added normalization in `pulsar-rest/.../MCPToolController.kt` for snake_case and legacy payload variants; refreshed CLI parser tests and controller/E2E expectations.

**Validation:** Passed `cd sdks/browser4-cli && npx jest tests/commands.test.ts --runInBand` and `npx tsc --noEmit`; targeted Maven test for `MCPToolControllerTest` remained blocked by the same pre-existing Kotlin compile-daemon failure in `pulsar-common`.

**Key Learnings:** Safest browser4-cli alignment is to match real MCP tool names/parameter shapes, not partially mapped Playwright-style aliases. Small controller-side snake_case normalization gives compatibility without reintroducing alias drift. Targeted JS/TS validation remains meaningful when Kotlin daemon issues block Maven runs.


Total usage est:        1 Premium request
API time spent:         14s
Total session time:     22s
Total code changes:     +0 -0
Breakdown by AI model:
 gpt-5.4                 21.1k in, 1.2k out, 17.9k cached (Est. 1 Premium request)


## standardize-gh-copilot-calls
**Summary:** Standardized coworker gh copilot invocation around shared shell-specific helpers so coworker, git-sync, refine-last-draft, and rename build prompts/extra flags consistently from the configured COPILOT base command.

**Changes:** Expanded coworker/scripts/workers/gh-copilot.ps1 into a reusable helper module with repo-root/config loading, argument building, formatting, direct invocation, and Start-Process helpers; added matching coworker/scripts/workers/gh-copilot.sh; updated coworker.ps1/coworker.sh, git-sync.*, efine-last-draft.*, and ename.* to source the helper instead of duplicating config loading and fragile gh argument assembly. Also commented the invalid GH_DEBUG=api PowerShell line into a valid optional env-var example so coworker.ps1 parses again.

**Validation:** PowerShell parser validation passed for the touched .ps1 scripts; helper smoke test produced gh copilot --model gpt-5.4 --no-ask-user --log-level info --allow-all -- -p 'hello world' --allow-all-tools --allow-all-paths; Bash validation passed with ash -n on LF-normalized temp copies plus a helper smoke test that formatted gh copilot -- -p hello\ world --allow-all-tools --allow-all-paths.

**Key Learnings:** Reusing one helper per shell is safer than repeating ad-hoc quoting logic in each worker. For Windows-hosted Bash validation, running ash -n and helper smoke tests from a temp working directory avoids CRLF and path-conversion issues while still proving the shared invocation contract.

## standardize-gh-copilot-calls-correction
**Summary:** Corrected the previous memory entry formatting for the shared gh copilot helper task.

**Changes:** Expanded coworker/scripts/workers/gh-copilot.ps1 into a reusable helper module with repo-root/config loading, argument building, formatting, direct invocation, and Start-Process helpers; added matching coworker/scripts/workers/gh-copilot.sh; updated coworker.ps1/coworker.sh, git-sync.*, refine-last-draft.*, and rename.* to source the helper instead of duplicating config loading and fragile gh argument assembly. Also commented the invalid PowerShell GH_DEBUG example into a valid optional env-var example so coworker.ps1 parses again.

**Validation:** PowerShell parser validation passed for the touched .ps1 scripts. Bash validation passed with bash -n on LF-normalized temp copies, and the helper smoke test formatted a consistent gh copilot command with prompt and extra flags.

**Key Learnings:** Reusing one helper per shell is safer than repeating ad-hoc quoting logic in each worker. On Windows-hosted Bash checks, validating from a temp working directory is a reliable way to avoid CRLF and path-conversion noise.
