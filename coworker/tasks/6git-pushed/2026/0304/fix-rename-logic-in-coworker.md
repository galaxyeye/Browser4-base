# Fix rename logic in coworker

## Problem

When coworker accepts a task in `1created` directory, it should rename the task file according to its content using script
rename.ps1 or rename.sh. However, the current implementation does not work as expected, and the task files remain with their original names.

Make sure coworker is correctly executing the rename script after accepting a task and properly handle retry logic if the rename operation fails.

In 164933-coworker.log, we can see the following error message:

```
[2026-03-04 16:50:34] [WARN] GH Copilot naming timed out after 60s
[2026-03-04 16:50:34] [DEBUG] Naming Copilot STDERR (Timeout):
[2026-03-04 16:50:34] [INFO] Moved to working: D:\workspace\Browser4\Browser4-4.6\coworker\tasks\2working\1.md
```

## Reference

[coworker.ps1](../../scripts/coworker.ps1)
[coworker.sh](../../scripts/coworker.sh)
[rename.ps1](../../scripts/workers/rename.ps1)
[rename.sh](../../scripts/workers/rename.sh)
[164933-coworker.log](../300logs/2026/03/04/164933-coworker.log)
