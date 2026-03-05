# Improve coworker-memory-generator

## Problem

```shell
(base) PS D:\workspace\Browser4\Browser4-4.6> .\coworker\scripts\workers\coworker-memory-generator.ps1 monthly

✗ Create coworker\tasks\300logs\2026\03\MEMORY.202603.md
Path already exists

● Delete existing monthly memory file to overwrite it
$ Remove-Item -Path "coworker\tasks\300logs\2026\03\MEMORY.202603.md" -Force
└ 1 line...

✗ Create coworker\tasks\300logs\2026\03\MEMORY.202603.md
Path not absolute
```

Improve the path handling in `coworker-memory-generator.ps1` to ensure everything is ready before calling `gh copilot`.

Also check the `coworker-memory-generator.sh` for similar issues.
