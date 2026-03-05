# Daily Memory - 2026-03-05

## Task: Fix Coworker Memory Generator Paths
- **Goal**: Fix `Path not absolute` error in `coworker-memory-generator` scripts when `gh copilot` creates memory files.
- **Outcome**: Updated `coworker-memory-generator.ps1`, `coworker-memory-generator.sh`, `coworker-daily-memory-generator.ps1`, and `coworker-daily-memory-generator.sh` to use absolute paths derived from the repository root. This ensures `gh copilot` receives valid absolute paths for file creation regardless of execution context.
- **Lessons**: Always resolve paths to absolute paths when passing them to external CLI tools like `gh` to avoid ambiguity and strict path requirements.

## Task: Improve Coworker Memory Generator
- **Goal**: Refactor memory initialization logic from `coworker.ps1` and `coworker.sh` into `coworker-memory-generator.ps1` and `.sh` to centralize maintenance and support dual daily memory versions.
- **Outcome**: 
    - Implemented `init` mode in `coworker-memory-generator.ps1/sh` to handle directory creation, memory compression (to `long.md`), and context generation.
    - Updated `coworker.ps1` and `.sh` to invoke the generator script and parse the JSON output for memory context and instructions.
    - Ensured consistent behavior across PowerShell and Bash environments.
- **Lessons**: Centralizing logic simplifies maintenance. Using structured JSON output via stdout is a robust way to pass complex data between scripts, provided stdout is kept clean of log messages.
