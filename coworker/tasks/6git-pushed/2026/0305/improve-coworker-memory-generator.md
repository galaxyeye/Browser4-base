# Improve Coworker Memory Generator

Coworker uses `coworker-memory-generator` to maintain a memory system that tracks the project's evolution and key learnings.

- do not write memory generation logic in coworker.ps1/sh, but call `coworker-memory-generator` instead, so that all memory generation logic is in one place and can be easily maintained.
- for daily memory, generate two version: MEMORY.yyyyMMdd.md and MEMORY.yyyyMMdd.long.md
- update daily memory after each coworker task
- monthly memory are based on daily memory
- annual/yearly/global memory are based on root AGENTS.md and monthly memory

## References

- [coworker-memory-generator.ps1](../../scripts/workers/coworker-memory-generator.ps1)
- [coworker-memory-generator.sh](../../scripts/workers/coworker-memory-generator.sh)

