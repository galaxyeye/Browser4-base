# Daily Memory - 2026-03-08

## Task: coworker-script-config-loading
**Summary:** Standardized coworker runners and worker scripts to load shared Copilot command config from `coworker/scripts/config.ps1` and `config.sh`, ensuring consistent CLI flags across main, delegated, streamed, and captured runs.

**Changes:**
- Replaced string-built Copilot commands with native shell command arrays in both config files.
- Updated `coworker.ps1` and `coworker.sh` to source config, validate the `COPILOT` array, and reuse it for task naming/execution.
- Applied the same pattern to direct worker entry points: `refine-last-draft`, `coworker-memory-generator`, and `coworker-daily-memory-generator` in both PowerShell and Bash.
- All direct Copilot calls now flow through the shared executable + argument array.

**Validation:**
- PowerShell scripts parsed successfully.
- Verified PowerShell array expands to `gh copilot --model gpt-5.4 --no-ask-user --log-level info --allow-all`.
- Confirmed worker scripts append prompt/tool flags on top of the shared base command.
- Validated Bash scripts with `bash -n` on LF-normalized temp copies.
- Confirmed `config.sh` exports a valid `COPILOT` bash array starting with `gh copilot`.

**Key Learnings / Structural Insight:**
- Shared CLI config should use native shell arrays, not strings, when scripts need safe argument appends.
- Any script invoking Copilot directly must validate and reuse the shared array or delegated flows will drift.
- On Windows, Bash syntax validation is more reliable on LF-normalized temp copies when repo files use CRLF.
- Coworker tooling now follows a stronger pattern: shared shell-specific command-array config -> per-entrypoint validation -> reuse across primary and delegated flows.

## Task: create-missing-draft-files
**Summary:** Added automatic placeholder maintenance so `coworker/tasks/0draft` always contains `1.md` through `5.md` at runner startup and after task completion.

**Changes:**
- Added `Ensure-DraftPlaceholders` to `coworker/scripts/coworker.ps1`.
- Added `ensure_draft_placeholders` to `coworker/scripts/coworker.sh`.
- Wired both runners to recreate missing numbered draft files before queue listing and after moving a finished task.
- Restored missing `coworker/tasks/0draft/1.md`.

**Validation:**
- PowerShell runner parsed successfully.
- Bash runner passed `bash -n` on an LF-normalized temp copy.
- Verified `coworker/tasks/0draft` contains `1.md` to `5.md`.

**Key Learnings:**
- Placeholder maintenance belongs in shared runners so Windows and Bash stay aligned.
- Checking at startup and after completion preserves repo hygiene and automation consistency.
- No monthly memory append was needed because the 2026-03-07 rollup already existed.

## Task: webdriver-mcp-toolspec-update
**Summary:** Aligned MCP tool-spec generation with explicit `@MCP` annotations, added `WebDriver.drag(sourceSelector, targetSelector)`, and refreshed code-mirror tool-spec resources so only annotated WebDriver/agent tools are exposed.

**Changes:**
- Added `@MCP` coverage across WebDriver methods used by `WebDriverToolExecutor`.
- Implemented default `WebDriver.drag` using computed source/target offsets and `dragAndDrop`.
- Updated `ToolSpecGenerator` to emit specs only for `@MCP` methods, prefer `@mcp` KDoc sections (case-insensitive), fall back to full KDoc, and humanize method names if KDoc is absent.
- Simplified `WebDriverToolExecutor` to reuse interface drag logic/spec, annotated MCP-visible `PerceptiveAgent` entrypoints, added focused tests, and refreshed `code-mirror` JSON/text artifacts.

**Validation:**
- Scripted coverage check confirmed every `WebDriverToolExecutor.callFunctionOn` WebDriver method maps to an `@MCP`-annotated WebDriver function.
- Regenerated `code-mirror` WebDriver/PerceptiveAgent text and tool-spec JSON artifacts.
- Maven validation was attempted but blocked by a pre-existing Kotlin compile-daemon connection failure in unrelated modules before reaching the changed module.

**Key Learnings:**
- Browser4's tool-spec pipeline is source-text driven, so `@MCP` annotations and code-mirror JSON/text artifacts must stay synchronized.
- Restricting spec generation to explicit annotations removes deprecated/helper overloads from MCP exposure and makes KDoc tagging rules prompt-visible.
- When Kotlin daemon failures block module builds, scripted consistency checks still provide useful coverage evidence while the blocker is documented.


Total usage est:        1 Premium request
API time spent:         13s
Total session time:     20s
Total code changes:     +0 -0
Breakdown by AI model:
 gpt-5.4                 20.6k in, 1.1k out, 17.9k cached (Est. 1 Premium request)

