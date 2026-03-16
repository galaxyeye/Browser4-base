# Daily Memory - 2026-03-16

- **Unify frontend/backend Browser4 tool names:** Removed the browser4-cli legacy tool-name alias bridge from sdks/browser4-cli/src/program.ts, moved the name/shape compatibility handling into pulsar-rest MCPToolController, and updated /mcp/tools so the backend now advertises and accepts the frontend-declared rowser_* tool names directly (including rowser_tabs, rowser_handle_dialog, and rowser_click variants).

- **Validation outcome:** 
pm run build && npm test -- --runInBand tests/program.test.ts tests/commands.test.ts passed in sdks/browser4-cli; ./mvnw.cmd -f pulsar-rest/pom.xml "-Dtest=MCPToolControllerTest" -D"surefire.failIfNoSpecifiedTests=false" test passed; and after installing pulsar-rest, the focused REST E2E slice ./mvnw.cmd -f pulsar-tests/pom.xml -pl pulsar-rest-tests -am -DrunE2ETests=true "-Dtest=MCPToolControllerE2ETest#testToolsEndpointCoversAllCliCommands+testFrontendToolNamesAreRecognized" -D"surefire.failIfNoSpecifiedTests=false" test also passed.

- **Lesson learned:** When the frontend command catalog defines both tool names and argument shapes, the compatibility layer belongs at the backend HTTP boundary instead of the CLI wrapper. For cross-module REST E2E verification in this repo, install the changed module locally first, then run the smallest pulsar-tests slice with -DrunE2ETests=true.
- **Correction:** The frontend tool prefix referenced above is browser_* (for example: browser_navigate, browser_tabs, browser_handle_dialog, browser_click). The earlier line contains a PowerShell formatting artifact, but the intended summary is that the backend now accepts those frontend-declared names directly.
