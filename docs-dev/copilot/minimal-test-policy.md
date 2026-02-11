# Minimal Test Policy (Copilot / Claude)

Last updated: 2026-01-30

This repository is a multi-module Maven project. **Speed matters**.
Our default workflow should avoid running full test suites on every small change.

## Goals

- Keep feedback loops fast during iterative development.
- Still catch obvious compiler/config breakages early.
- Make test scope proportional to change risk.

## Baseline Commands (Windows PowerShell)

> Always use the root Maven Wrapper: `D:\workspace\Browser4\browser4-4.5\\mvnw.cmd`.

### 1) Minimal compile / dependency check (default)

Use this for most small refactors and localized changes.

```powershell
cd D:\workspace\Browser4\browser4-4.5
.\mvnw.cmd -q -D"skipTests" test
```

Why `test` + `-DskipTests`?

- It typically compiles main + test sources (fails fast on compilation errors).
- It skips actually executing tests.

### 2) Fast regression (recommended)

Run only **core unit tests** (fast, broad signal).

```powershell
.\mvnw.cmd -pl pulsar-core -am test -D"surefire.failIfNoSpecifiedTests=false"
```

### 3) Narrowest verification (single module)

For changes scoped to one module:

```powershell
.\mvnw.cmd -pl <module-artifactId-or-path> -am test -D"surefire.failIfNoSpecifiedTests=false"
```

Examples:

- `-pl pulsar-rest -am test`
- `-pl sdks\kotlin-sdk-tests -am test`

### 4) Narrowest verification (single test)

Prefer running only the impacted test class (or method) when possible.

> Note: the exact property (`test`, `it.test`, etc.) depends on Surefire/Failsafe config.
> Use the module-level test command above if single-test selection isnt wired.

```powershell
.\mvnw.cmd -pl sdks\kotlin-sdk-tests -am test -D"test=ai.platon.pulsar.sdk.integration.SomeTest"
```

## Decision Rules (Default Behavior)

### Default (most tasks)

- Run **Minimal compile / dependency check** only.
- Then run **one** small, relevant test scope if there is new/changed logic.

### Upgrade test scope when risk increases

Run `-pl <affected> -am test` (or repo scripts `bin/build.ps1 -test`) when any apply:

- Cross-module changes (touching more than one Maven module)
- Public API / DTO / serialization changes
- Spring configuration / wiring changes
- Dependency version changes
- Concurrency, I/O, retry logic changes
- Anything that could break startup (REST server, agent runtime, browser/CDP)

### Full suite guidance

Do **not** run the heavy suites by default.
Consider running larger suites when:

- Preparing a PR for merge
- CI failures reproduce only in integration/e2e
- You changed browser automation/browser/CDP lifecycle

## Known Trade-offs (Why we dont always run full tests)

- **Lower early bug detection**: minimal checks may miss runtime wiring issues.
- **Delayed failures**: problems may surface later in CI.
- **Cross-module regressions**: a change in module A may break module Bs tests.

To manage this, we keep minimal checks on every iteration and scale up tests when risk increases.

## Reporting Standard (for AI edits)

When an AI completes a task, it should report:

- What was run (compile/tests) and the scope
- What was intentionally skipped
- Any remaining risk areas
