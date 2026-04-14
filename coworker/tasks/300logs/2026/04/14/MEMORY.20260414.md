# Daily Memory - 2026-04-14

## Task: fix-install-sh-release-tag

**Status:** ✅ Completed

**What was done:**
- Investigated `sdks/browser4-cli/install.sh` – the file did not exist yet in the repository.
- Created `sdks/browser4-cli/install.sh` as a clean, executable installer script.
- The key fix was implementing a three-method fallback chain for determining the latest Browser4 release tag:
  1. GitHub REST API with `Accept: application/vnd.github.v3+json` header + `grep`/`sed` parsing
  2. Follow the `/releases/latest` redirect URL and extract the tag
  3. `wget` fallback against the GitHub API
  4. Hard-coded `DEFAULT_VERSION` as a last resort (never fails with hard error)
- The installer also: checks/installs Java 17+, Google Chrome, and Rust toolchain; downloads `Browser4.jar` to `~/.browser4/`; builds `browser4-cli` from source and installs to `~/.local/bin/`.
- Committed as `feat(browser4-cli): add install.sh with robust release tag detection`.

**Root cause of the error:**
The original installer (referenced in the task description but non-existent in the repo) was likely using a bare `curl` call to the GitHub API without an `Accept` header or proper error handling, causing it to fail on rate-limited networks or return unexpected data.

**Lessons learned:**
- Always include `Accept: application/vnd.github.v3+json` when calling the GitHub REST API for releases.
- Provide multiple fallback strategies and a hard-coded default version — never let an installer abort just because it can't reach the network.
- Use `set -euo pipefail` with explicit error messages so failures are immediately obvious.
