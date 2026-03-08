# Daily Memory - 2026-03-08

## coworker-script-config-loading
Standardized coworker runners/workers to load shared Copilot command config from `coworker/scripts/config.ps1` and `config.sh`, so main, delegated, streamed, and captured runs all use the same CLI flags.

**Changes**
- Replaced string-built Copilot commands with native shell command arrays in both config files.
- Updated `coworker.ps1` and `coworker.sh` to source config, validate the `COPILOT` array, and reuse it for naming/execution.
- Applied the same pattern to direct worker entry points (`refine-last-draft`, `coworker-memory-generator`, `coworker-daily-memory-generator`) in PowerShell and Bash.

**Validation**
- PowerShell parsing passed.
- Confirmed PowerShell array expands to `gh copilot --model gpt-5.4 --no-ask-user --log-level info --allow-all`.
- Verified workers append prompt/tool flags onto the shared base command.
- Validated Bash scripts with `bash -n` on LF-normalized temp copies and confirmed `config.sh` exports a valid `COPILOT` array.

**Key Insights**
- Shared CLI config should use shell-native arrays, not strings, when callers need safe argument appends.
- Any direct Copilot invoker must validate/reuse the shared array or delegated flows will drift.
- On Windows, Bash syntax checks are more reliable on LF-normalized temp copies for CRLF files.

## create-missing-draft-files
Added automatic placeholder maintenance so `coworker/tasks/0draft` always contains `1.md` through `5.md` at startup and after task completion.

**Changes**
- Added `Ensure-DraftPlaceholders` to `coworker/scripts/coworker.ps1`.
- Added `ensure_draft_placeholders` to `coworker/scripts/coworker.sh`.
- Wired both runners to recreate missing draft files before queue listing and after moving a finished task.
- Restored missing `coworker/tasks/0draft/1.md`.

**Validation**
- PowerShell parse passed.
- Bash runner passed `bash -n` on LF-normalized temp copy.
- Verified `coworker/tasks/0draft` contains `1.md` to `5.md`.

**Key Insights**
- Placeholder maintenance belongs in shared runners so Windows/Bash remain aligned.
- Checking both startup and post-completion preserves repo hygiene and automation consistency.

## webdriver-mcp-toolspec-update
Aligned MCP tool-spec generation with explicit `@MCP` annotations, added `WebDriver.drag(sourceSelector, targetSelector)`, and refreshed code-mirror artifacts so only annotated WebDriver/agent tools are exposed.

**Changes**
- Added `@MCP` coverage for WebDriver methods used by `WebDriverToolExecutor`.
- Implemented default `WebDriver.drag` via computed offsets + `dragAndDrop`.
- Updated `ToolSpecGenerator` to emit specs only for `@MCP` methods, prefer case-insensitive `@mcp` KDoc sections, and humanize names when KDoc is absent.
- Simplified `WebDriverToolExecutor`, annotated MCP-visible `PerceptiveAgent` entrypoints, added focused tests, and regenerated code-mirror JSON/text artifacts.

**Validation**
- Scripted coverage check confirmed every executor-exposed WebDriver method maps to an `@MCP`-annotated function.
- Regenerated WebDriver/PerceptiveAgent tool-spec artifacts.
- Maven validation was blocked by a pre-existing Kotlin compile-daemon failure in unrelated modules.

**Key Insights**
- Browser4’s tool-spec pipeline is source-text driven, so `@MCP` annotations and generated code-mirror artifacts must stay synchronized.
- Explicit annotation-only generation removes deprecated/helper overloads from MCP exposure and makes KDoc tagging rules prompt-visible.
- When Kotlin daemon failures block builds, scripted consistency checks still provide strong coverage evidence.


Total usage est:        1 Premium request
API time spent:         14s
Total session time:     22s
Total code changes:     +0 -0
Breakdown by AI model:
 gpt-5.4                 20.6k in, 876 out, 0 cached (Est. 1 Premium request)


## force-push-pre-release-branch
Force-synced `pre-release` to `4.6.x` so the remote pre-release branch now points at commit `8dc6bb888` (`feat: align WebDriver tools with CLI specification and enhance MCP tool-spec generation`).

**Outcome**
- Verified `4.6.x` was at `8dc6bb888` while `origin/pre-release` was at `e7f856068` before the update.
- Executed `git push origin +4.6.x:pre-release` successfully; GitHub accepted the forced update.
- Re-verified `origin/pre-release` equals `4.6.x` after the push.
- Checked the monthly memory file and confirmed it already contains the 2026-03-07 daily rollup, so no monthly append was needed.

**Lessons Learned**
- For release-alignment tasks, capture both source and target SHAs before force-pushing so the branch move is auditable.
- A post-push `rev-parse` check is a quick, reliable confirmation that the remote branch now matches the intended source branch.
