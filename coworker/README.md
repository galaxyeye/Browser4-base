# Builtin AI Coworker

The Builtin AI Coworker is an agent that can assist you with various tasks in your repository.
The coworker processes task files that you create, executes them, and can even commit changes back to your repository.

## Coworker Workflow

1. **Draft**: Create your task files in `coworker/tasks/0prepare`.
2. **Queue**: Move the task files to `coworker/tasks/1created` when ready.
3. **Execute**: Run `coworker.ps1` (Windows) or `coworker.sh` (Linux/macOS) to process the task.
4. **Review**: The task moves to `coworker/tasks/3complete` after execution. Review the changes.
5. **Approve**: Move the task to `coworker/tasks/5approved` if you want it to be automatically committed/pushed by the periodic runner.

## Prerequisites

GitHub CLI (`gh`) must be installed and authenticated to the repository.

See https://github.com/cli/cli#installation for installation instructions.

## Tags

You can use tags in task files to provide additional information about the task.

Supported tags:

- `#auto-approve`: Automatically approve the task after completion, moving it to `5approved` instead of `3complete`. This is useful for tasks that you trust the agent to execute without manual review, allowing them to be automatically committed and pushed to the repository by the periodic runner.

## Mentions

** Experimental **

You can use mentions in task files to notify the agent.

When you mention `@coworker` in a task file, the agent will be notified to process the task.

## Syncing with Git

After tasks are completed, you can use the commit scripts to push changes to your repository.

**Windows (PowerShell):**

```powershell
.\coworker\scripts\git-sync.ps1
```

**Linux/macOS (Bash):**

```bash
./coworker/scripts/git-sync.sh
```

## Periodical Runner

The periodic runner monitors `1created` and `5approved` folders and executes tasks automatically.

**Windows (PowerShell):**

```powershell
.\coworker\scripts\run_coworker_periodically.ps1
```

**Linux/macOS (Bash):**

```bash
./coworker/scripts/run_coworker_periodically.sh
```
