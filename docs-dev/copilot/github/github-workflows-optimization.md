# GitHub Workflows Optimization

## Overview

This document describes the optimized GitHub workflow test strategy to make each workflow complementary and avoid overlaps.

## Workflow Roles

### 1. `pr-tests.yml` - Pull Request Testing
**Trigger:** Pull requests to main/master/develop  
**Purpose:** Fast feedback on code changes  
**Test Scope:** Fast unit tests only  
**Excluded:** Integration, E2E, SDK, Heavy, Slow, environment-dependent tests

**Rationale:** 
- Provides rapid feedback (target: <15 minutes)
- Runs on every PR to catch breaking changes early
- Does not require external services or resources
- Minimal CI resource usage

### 2. `ci.yml` - CI/CD Pipeline
**Trigger:** CI tags (`v*-ci.*`)  
**Purpose:** Validate tagged CI builds before deployment  
**Test Scope:** Fast unit tests + Docker build + Python SDK tests  
**Excluded:** Same as pr-tests.yml for test phase

**Rationale:**
- Quick validation of release candidates
- Includes Docker packaging verification
- SDK integration testing against built image
- Complementary to PR tests (adds Docker/SDK dimension)

### 3. `nightly.yml` - Comprehensive Nightly Build
**Trigger:** Daily at 00:00 UTC (scheduled) + manual  
**Purpose:** Comprehensive testing with all unit and integration tests  
**Test Scope:** 
- All unit tests (Fast + Slow)
- All integration tests
- Docker build
- Python SDK tests

**Excluded:** E2E (manual only), SDK tests (separate workflows), RequiresBrowser, RequiresAI

**Rationale:**
- Catches issues that Fast tests miss
- Runs when CI resources are available
- Consolidates integration testing (removes duplication)
- Full system validation without expensive E2E tests

**Note:** This replaces the separate `integration-tests.yml` workflow to eliminate duplication.

### 4. `e2e-tests.yml` - End-to-End Tests
**Trigger:** Manual only (workflow_dispatch)  
**Purpose:** Full E2E testing with browser and AI requirements  
**Test Scope:** 
- E2E tests (Heavy, RequiresBrowser, RequiresAI)
- Docker build
- Application deployment
- Python SDK tests

**Rationale:**
- Most expensive tests (browser automation, AI interactions)
- Run on-demand before major releases
- Not suitable for automated schedules (high resource cost)

### 5. `docker-e2e-test.yml` - Docker Deployment Testing
**Trigger:** E2E tags (`v*-e2e.*`)  
**Purpose:** Validate Docker deployment and Python SDK integration  
**Test Scope:** 
- Skip Maven tests (already validated elsewhere)
- Docker build
- Application deployment
- Python SDK tests

**Rationale:**
- Focuses purely on Docker packaging and deployment
- Complementary to e2e-tests.yml (no test overlap)
- Validates release candidate deployments

### 6. `kotlin-sdk-test.yml` - Kotlin SDK Testing
**Trigger:** 
- Push/PR to SDK paths
- Manual (workflow_dispatch)

**Purpose:** Kotlin SDK validation  
**Test Scope:** 
- Kotlin SDK unit tests
- Kotlin SDK integration tests

**Rationale:**
- Triggered by SDK code changes only
- Removed scheduled run (was duplicate of push trigger)
- Complementary to other workflows (SDK-specific)

### 7. `nodejs-sdk-test.yml` - Node.js SDK Testing
**Trigger:** 
- Push/PR to SDK paths
- Manual (workflow_dispatch)

**Purpose:** Node.js SDK validation  
**Test Scope:**
- Node.js SDK tests across multiple OS and Node versions

**Rationale:**
- Triggered by SDK code changes only
- Matrix testing for cross-platform compatibility
- Complementary to other workflows (SDK-specific)

## Test Coverage Matrix

| Workflow | Fast Unit | Slow Unit | Integration | E2E | SDK | Docker | Frequency |
|----------|-----------|-----------|-------------|-----|-----|--------|-----------|
| pr-tests.yml | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | Every PR |
| ci.yml | ✅ | ❌ | ❌ | ❌ | Python* | ✅ | Tagged |
| nightly.yml | ✅ | ✅ | ✅ | ❌ | Python* | ✅ | Daily |
| e2e-tests.yml | ❌ | ❌ | ❌ | ✅ | Python* | ✅ | Manual |
| docker-e2e-test.yml | ❌ | ❌ | ❌ | ❌ | Python* | ✅ | Tagged |
| kotlin-sdk-test.yml | ❌ | ❌ | ❌ | ❌ | Kotlin | ❌ | On SDK changes |
| nodejs-sdk-test.yml | ❌ | ❌ | ❌ | ❌ | Node.js | ❌ | On SDK changes |

*Python SDK tests run in context of deployed application

## Key Optimizations

### 1. Removed Duplication
- **Deleted `integration-tests.yml`**: Was redundant with nightly.yml
- **Removed scheduled run from `kotlin-sdk-test.yml`**: SDK tests already run on push/PR

### 2. Clear Boundaries
Each workflow has a distinct purpose:
- **PR**: Fast unit tests only
- **CI**: Fast unit tests + Docker + SDK
- **Nightly**: Comprehensive (Fast + Slow + Integration)
- **E2E**: Manual, heavy tests with browser/AI
- **Docker E2E**: Deployment validation only
- **SDK Tests**: Language-specific, trigger on SDK changes

### 3. Resource Efficiency
- Fast tests run frequently (PR, CI)
- Expensive tests run infrequently (nightly) or on-demand (E2E)
- No redundant test execution

### 4. Test Strategy Alignment
Follows the test taxonomy defined in `TESTING.md`:
- Fast → PR, CI
- Slow → Nightly
- Heavy → E2E (manual)
- Integration → Nightly
- SDK → Dedicated workflows

## Workflow Decision Tree

```
Code Change
├── Is it a PR?
│   └── YES → pr-tests.yml (Fast unit tests)
│
├── Is it a CI tag?
│   └── YES → ci.yml (Fast unit tests + Docker + Python SDK)
│
├── Is it an E2E tag?
│   └── YES → docker-e2e-test.yml (Docker deployment + Python SDK)
│
├── Is it SDK code?
│   └── YES → kotlin-sdk-test.yml or nodejs-sdk-test.yml
│
├── Is it nightly?
│   └── YES → nightly.yml (Comprehensive: Fast + Slow + Integration)
│
└── Manual E2E testing needed?
    └── YES → e2e-tests.yml (Full E2E with browser/AI)
```

## Migration Notes

### Removed Workflows
- `integration-tests.yml` - Functionality moved to nightly.yml

### Modified Workflows
- `pr-tests.yml` - Removed integration tests, focus on fast unit tests only
- `nightly.yml` - Now includes all unit tests (Fast + Slow) + integration tests
- `ci.yml` - Clarified scope, same as PR tests but with Docker + SDK
- `kotlin-sdk-test.yml` - Removed scheduled run
- `e2e-tests.yml` - Clarified as manual-only comprehensive E2E
- `docker-e2e-test.yml` - Clarified as deployment testing only

## Benefits

1. **No Overlap**: Each workflow tests a distinct scope
2. **Faster PR Feedback**: PR tests complete in <15 minutes
3. **Comprehensive Coverage**: Nightly builds catch everything
4. **Cost Effective**: Expensive tests run less frequently
5. **Clear Purpose**: Each workflow has a well-defined role
6. **Maintainable**: Easy to understand and modify

## Future Considerations

1. Consider adding coverage reporting to nightly.yml (currently removed from pr-tests.yml)
2. Monitor nightly.yml execution time; if it exceeds 60 minutes, consider splitting Slow and Integration tests
3. Evaluate adding E2E tests to nightly schedule once stability improves
4. Consider matrix testing for critical Java versions in nightly builds
