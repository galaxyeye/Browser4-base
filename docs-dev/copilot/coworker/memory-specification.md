# Coworker Memory System

**Official Specification**
Status: Stable
Language: English Only
Max Length per File: 2000 words

---

# 1. Purpose

The Coworker Memory System is a structured, layered operational memory architecture designed to:

1. Prevent repeated mistakes
2. Improve execution quality over time
3. Detect recurring structural issues
4. Track capability evolution
5. Support long-term strategic forecasting

This system is not a logging mechanism.
It is a continuous reflection and evolution framework.

---

# 2. Architectural Model

The memory system follows a four-layer abstraction hierarchy:

| Layer   | Scope                  | Function                                | Abstraction Level |
| ------- | ---------------------- | --------------------------------------- | ----------------- |
| Daily   | Single execution day   | Operational reflection                  | Low               |
| Monthly | 1 month                | Pattern recognition                     | Medium            |
| Yearly  | 1 year                 | Strategic review                        | High              |
| Global  | Entire project history | Constitutional + evolutionary narrative | Highest           |

Each layer must increase abstraction.
No layer should merely repeat content from lower layers.

---

# 3. Core Principles

## 3.1 Immutability

* Daily memory files are immutable once written.
* Aggregated layers (Monthly / Yearly / Global) may be regenerated.
* Historical daily records must never be modified.

This ensures traceability and prevents narrative rewriting.

---

## 3.2 Reflection Over Reporting

Memory entries must focus on:

* Why something happened
* What patterns are emerging
* What systemic improvements are required

Simple activity reporting is insufficient.

---

## 3.3 Structural Thinking

All layers must prioritize:

* Recurring issue detection
* Root cause analysis
* Process evolution
* Capability development

---

# 4. Daily Memory Specification

File format:
`MEMORY.YYYYMMDD.md`

Structure:

```markdown
# MEMORY.YYYYMMDD.md
## Daily Memory - <Date>

### Tasks Executed
- …

### Execution Quality Review
- What worked well
- What was inefficient

### Issues Encountered
- …

### Root Cause Analysis
- …

### Process Improvement Insight
- At least one concrete improvement for future execution
```

Constraints:

* Must include at least one improvement insight.
* Maximum 2000 words.
* Written in English only.
* Append-only behavior.

Purpose:

Daily memory serves as factual operational reflection and micro-level improvement driver.

---

# 5. Monthly Memory Specification

File format:
`MEMORY.YYYYMM.md`

Structure:

```markdown
# MEMORY.YYYYMM.md
## Monthly Memory - <Month Year>

### Work Themes
- Dominant task categories

### Recurring Issues
- Pattern A
- Pattern B

### Structural Bottlenecks
- Persistent constraints affecting efficiency

### Efficiency Trend
- Improving / Stable / Degrading
- Brief justification

### System Adjustments Proposed
- 1.
- 2.
```

Constraints:

* Must be derived from all Daily records within the month.
* Must identify patterns (not isolated incidents).
* Maximum 2000 words.
* May be regenerated.
* English only.

Purpose:

Monthly memory identifies recurring patterns and structural inefficiencies.

---

# 6. Yearly Memory Specification

File format:
`MEMORY.YYYY.md`

Structure:

```markdown
# MEMORY.YYYY.md
## Annual Strategic Review - <Year>

### Project State Evolution
- How the project changed during the year

### Major Achievements
- …

### Major Failures
- …

### Structural Problems (Solved / Unsolved)
- …

### Capability Upgrades
- Skills or operational improvements gained

### Strategic Risks
- Risks entering the next year

### Project Trajectory Forecast
- 1–2 year outlook

### Three Immediate Strategic Actions
- 1.
- 2.
- 3.
```

Constraints:

* Must reflect aggregated Monthly memories.
* Must differentiate solved vs unsolved structural problems.
* Maximum 2000 words.
* English only.

Purpose:

Yearly memory provides a strategic-level evaluation and forward-looking analysis.

---

# 7. Global Memory Specification

File format:
`MEMORY.md`

Role:

Global memory functions as the Project Constitution and Evolution Chronicle.

Structure:

```markdown
# MEMORY.md

## Mission & Vision
- Why the project exists

## Core Principles
- Non-negotiable operational rules

## Evolution Phases
- Phase 1:
- Phase 2:
- …

## Major Turning Points
- …

## Long-Term Structural Challenges
- …

## Opportunity Landscape
- Strategic opportunity areas

## Three Strategic Priorities Now
- 1.
- 2.
- 3.
```

Constraints:

* Must summarize all Yearly memories.
* Must identify project phases and turning points.
* Maximum 2000 words.
* English only.
* May be regenerated but must preserve Mission & Vision unless explicitly revised.

Purpose:

Global memory defines identity, historical trajectory, and strategic direction.

---

# 8. Abstraction Rules

Each higher layer must:

1. Compress lower-layer content
2. Identify recurring patterns
3. Remove operational noise
4. Increase strategic insight

Repetition without abstraction is a violation of the system design.

---

# 9. Prohibited Behaviors

The system must avoid:

* Pure activity logs without analysis
* Emotional commentary without structural insight
* Rewriting historical facts
* Exceeding word limits

---

# 10. Evolution Philosophy

The Coworker Memory System is designed to produce:

Execution → Reflection → Pattern Detection → Structural Adjustment → Strategic Evolution

If used consistently, the system enables:

* Reduced repetition of mistakes
* Measurable efficiency improvement
* Capability tracking
* Strategic continuity

---
