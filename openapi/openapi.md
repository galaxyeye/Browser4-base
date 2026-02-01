# Browser4 WebDriver-Compatible API (Reading `openapi.yaml` and Mapping to Implementation)

> Goal: make `openapi/openapi.md` a **maintainable index between spec and code**: easy to read, easy to locate things, and easy to keep updated.
>
> Sources of truth:
> - **Spec**: `openapi/openapi.yaml`
> - **Implementation**: `pulsar-rest/src/main/kotlin/ai/platon/pulsar/rest/openapi/controller/*`
>
> Scope: this document focuses on the WebDriver-Compatible API (rooted at `/session...`). Other REST surfaces in this repo (such as `/api/*`) are not covered in detail here (see Appendix).

---

## 0. Browser4 SDK Design (Background)

1. **核心目标**：构建具备人类级浏览器操作能力的智能体（Agent）系统，支持全面的浏览器自动化任务。
2. **API 一致性**：SDK 接口设计与 Browser4 本地 API 保持完全一致，确保统一的用户体验。
   1. 确保 `ai.platon.pulsar.sdk.examples.FusedActsStyleExample` 示例可以正常运行，各个不同语言均需实现该案例。
   2. API 一致性是首要原则，其他设计均需围绕此目标展开。
3. **架构设计**：采用三层服务架构模型，由三个核心组件组成：PulsarSession、WebDriver 和 Agent。
4. **PulsarSession**：提供全生命周期的网页管理能力，涵盖链接处理、URL 规范化、页面加载、导航、交互、元素定位与操作、状态同步、状态追踪、数据提取及持久化。
5. **WebDriver**：提供标准化的底层浏览器控制接口
    1. 接口同服务器端 `ai.platon.pulsar.skeleton.crawl.fetch.driver.WebDriver` 接口保持一致，可分步骤实现
    2. 制定通讯协议时，向 W3C WebDriver 协议对齐，不追求完全一致，以 openapi.yaml 中定义的接口为准。
6. **Agent**：提供智能体决策与执行控制能力。
7. **会话模型（Session Model）**：每个 SDK Session 包含唯一的 PulsarSession、Browser 实例和 Agent 实例
   1. Browser 可管理多个 WebDriver 实例，同一会话中，可能会在多个 WebDriver 实例间切换。
8. **网络通信**：SDK 被设计为跨网络服务调用的客户端，用于远程访问 Browser4 服务。
9. **协议定位**：Browser4 SDK 不遵循 W3C WebDriver 规范，也不追求标准兼容性。不采用 BiDi 协议，无需支持所有底层浏览器操作功能。
10. **多语言支持**：SDK 计划支持 Kotlin、Java、Python 和 JavaScript，提供统一的接口规范。

## 1. OpenAPI Overview (Extracted from `openapi.yaml`)

This section summarizes spec metadata. Endpoint lists are in Section 2, and implementation mapping is in Section 3.

- OpenAPI: `3.1.0`
- Title: **Browser4 WebDriver-Compatible API**
- Server (default dev address): `http://localhost:8182`
- Style: many responses use a WebDriver-compatible wrapper:
    - Success: `{"value": ...}` or `{"value": null}`
    - Failure: `ErrorResponse.value.error` / `ErrorResponse.value.message`

### 1.1 Tags (Capability Groups)

`openapi.yaml` groups endpoints into 9 capability tags:

- `session`: session lifecycle (create/get/delete)
- `navigation`: navigation and URL information (url/documentUri/baseUri)
- `selectors`: extension: selector-first interactions (exists/waitFor/click/fill/press/outerHtml/screenshot/element(s))
- `element`: standard WebDriver element-by-id (find element(s)/click/sendKeys/attribute/text)
- `script`: execute JavaScript (sync/async)
- `control`: delay/pause/stop
- `events`: event config, subscription, query
- `agent`: AI agent (run/observe/act/extract/summarize/clearHistory)
- `pulsar`: PulsarSession capabilities (normalize/open/load/submit/scrape)

---

## 2. Endpoint Overview (By Tag)

> Tip: this is a “skeleton index”. For detailed request/response schemas and status codes, refer to `openapi.yaml`.
> W3C standard endpoints are marked with a checkmark in the W3C column.

### 2.1 session

| Method | Path | operationId | W3C | In openapi.yaml | Next Step (human) |
|---|---|---|---|---|---|
| POST | `/session` | `createSession` | ✓ | ✓ | - |
| GET | `/session/{sessionId}` | `getSession` | ✓ | ✓ | - |
| DELETE | `/session/{sessionId}` | `deleteSession` | ✓ | ✓ | - |

### 2.2 navigation

| Method | Path | operationId | W3C | In openapi.yaml | Next Step (human) |
|---|---|---|---|---|---|
| POST | `/session/{sessionId}/url` | `navigateTo` | ✓ | ✓ | - |
| GET | `/session/{sessionId}/url` | `getCurrentUrl` | ✓ | ✓ | - |
| GET | `/session/{sessionId}/documentUri` | `getDocumentUri` | ✗ | ✓ | - |
| GET | `/session/{sessionId}/baseUri` | `getBaseUri` | ✗ | ✓ | - |

### 2.3 selectors (selector-first extension)

| Method | Path | operationId | W3C | In openapi.yaml | Next Step (human) |
|---|---|---|---|---|---|
| POST | `/session/{sessionId}/selectors/exists` | `selectorExists` | ✗ | ✓ | - |
| POST | `/session/{sessionId}/selectors/waitFor` | `waitForSelector` | ✗ | ✓ | - |
| POST | `/session/{sessionId}/selectors/element` | `findElementBySelector` | ✗ | ✓ | - |
| POST | `/session/{sessionId}/selectors/elements` | `findElementsBySelector` | ✗ | ✓ | - |
| POST | `/session/{sessionId}/selectors/click` | `clickBySelector` | ✗ | ✓ | - |
| POST | `/session/{sessionId}/selectors/fill` | `fillBySelector` | ✗ | ✓ | - |
| POST | `/session/{sessionId}/selectors/press` | `pressBySelector` | ✗ | ✓ | - |
| POST | `/session/{sessionId}/selectors/outerHtml` | `getOuterHtmlBySelector` | ✗ | ✓ | - |
| POST | `/session/{sessionId}/selectors/screenshot` | `screenshotBySelector` | ✗ | ✓ | - |

### 2.4 element (standard WebDriver element)

| Method | Path | operationId | W3C | In openapi.yaml | Next Step (human) |
|---|---|---|---|---|---|
| POST | `/session/{sessionId}/element` | `findElement` | ✓ | ✓ | - |
| POST | `/session/{sessionId}/elements` | `findElements` | ✓ | ✓ | - |
| POST | `/session/{sessionId}/element/{elementId}/click` | `clickElement` | ✓ | ✓ | - |
| POST | `/session/{sessionId}/element/{elementId}/value` | `sendKeysToElement` | ✓ | ✓ | - |
| GET | `/session/{sessionId}/element/{elementId}/attribute/{name}` | `getElementAttribute` | ✓ | ✓ | - |
| GET | `/session/{sessionId}/element/{elementId}/text` | `getElementText` | ✓ | ✓ | - |

### 2.5 script

| Method | Path                                 | operationId | W3C | In openapi.yaml | Next Step (human) |
|---|--------------------------------------|---|---|---|---|
| POST | `/session/{sessionId}/execute/sync`  | `executeSync` | ✓ | ✓ | - |
| POST | `/session/{sessionId}/execute/async` | `executeAsync` | ✓ | ✓ | - |

### 2.6 control

| Method | Path | operationId | W3C | In openapi.yaml | Next Step (human) |
|---|---|---|---|---|---|
| POST | `/session/{sessionId}/control/delay` | `delay` | ✗ | ✓ | - |
| POST | `/session/{sessionId}/control/pause` | `pause` | ✗ | ✓ | - |
| POST | `/session/{sessionId}/control/stop` | `stop` | ✗ | ✓ | - |

### 2.7 events

| Method | Path | operationId | W3C | In openapi.yaml | Next Step (human) |
|---|---|---|---|---|---|
| POST | `/session/{sessionId}/event-configs` | `createEventConfig` | ✗ | ✓ | - |
| GET | `/session/{sessionId}/event-configs` | `getEventConfigs` | ✗ | ✓ | - |
| GET | `/session/{sessionId}/events` | `getEvents` | ✗ | ✓ | - |
| POST | `/session/{sessionId}/events/subscribe` | `subscribeToEvents` | ✗ | ✓ | - |

### 2.8 agent

| Method | Path | operationId | W3C | In openapi.yaml | Next Step (human) |
|---|---|---|---|---|---|
| POST | `/session/{sessionId}/agent/run` | `run` | ✗ | ✓ | - |
| POST | `/session/{sessionId}/agent/observe` | `observe` | ✗ | ✓ | - |
| POST | `/session/{sessionId}/agent/act` | `act` | ✗ | ✓ | - |
| POST | `/session/{sessionId}/agent/extract` | `extract` | ✗ | ✓ | - |
| POST | `/session/{sessionId}/agent/summarize` | `summarize` | ✗ | ✓ | - |
| POST | `/session/{sessionId}/agent/clearHistory` | `clearHistory` | ✗ | ✓ | - |

### 2.9 pulsar

| Method | Path | operationId | W3C | In openapi.yaml | Next Step (human) |
|---|---|---|---|---|---|
| POST | `/session/{sessionId}/normalize` | `normalize` | ✗ | ✓ | - |
| POST | `/session/{sessionId}/open` | `open` | ✗ | ✓ | - |
| POST | `/session/{sessionId}/load` | `load` | ✗ | ✓ | - |
| POST | `/session/{sessionId}/submit` | `submit` | ✗ | ✓ | - |

### 2.10 W3C Standard Endpoints Missing From `openapi.yaml`

> These endpoints are part of the W3C WebDriver standard but are not listed in `openapi.yaml` yet.
> For real/mock implementation coverage, see Section 4.

#### 2.10.1 status / timeouts

| Method | Path | operationId | W3C | In openapi.yaml | Next Step (human) |
|---|---|---|---|---|-------------------|
| GET | `/status` | `getStatus` | ✓ | ✗ | 5                 |
| GET | `/session/{sessionId}/timeouts` | `getTimeouts` | ✓ | ✗ | 5                 |
| POST | `/session/{sessionId}/timeouts` | `setTimeouts` | ✓ | ✗ | 5                 |
| POST | `/session/{sessionId}/file` | `uploadFile` | ✓ | ✗ | 5                 |

#### 2.10.2 navigation extras

| Method | Path | operationId | W3C | In openapi.yaml | Next Step (human) |
|---|---|---|---|---|-------------------|
| POST | `/session/{sessionId}/back` | `goBack` | ✓ | ✗ | 1                 |
| POST | `/session/{sessionId}/forward` | `goForward` | ✓ | ✗ | 1                 |
| POST | `/session/{sessionId}/refresh` | `refresh` | ✓ | ✗ | 1                 |
| GET | `/session/{sessionId}/title` | `getTitle` | ✓ | ✗ | 1                 |
| GET | `/session/{sessionId}/source` | `getPageSource` | ✓ | ✗ | 1                 |

#### 2.10.3 window / frame

| Method | Path | operationId | W3C | In openapi.yaml | Next Step (human) |
|---|---|---|---|---|-------------------|
| GET | `/session/{sessionId}/window` | `getWindowHandle` | ✓ | ✗ | 5                 |
| DELETE | `/session/{sessionId}/window` | `closeWindow` | ✓ | ✗ | 5                 |
| POST | `/session/{sessionId}/window` | `switchToWindow` | ✓ | ✗ | 5                 |
| GET | `/session/{sessionId}/window/handles` | `getWindowHandles` | ✓ | ✗ | 5                 |
| POST | `/session/{sessionId}/window/new` | `newWindow` | ✓ | ✗ | 5                 |
| GET | `/session/{sessionId}/window/rect` | `getWindowRect` | ✓ | ✗ | 5                 |
| POST | `/session/{sessionId}/window/rect` | `setWindowRect` | ✓ | ✗ | 5                 |
| POST | `/session/{sessionId}/window/maximize` | `maximizeWindow` | ✓ | ✗ | 5                 |
| POST | `/session/{sessionId}/window/minimize` | `minimizeWindow` | ✓ | ✗ | 5                 |
| POST | `/session/{sessionId}/window/fullscreen` | `fullscreenWindow` | ✓ | ✗ | 5                 |
| POST | `/session/{sessionId}/frame` | `switchToFrame` | ✓ | ✗ | 5                 |
| POST | `/session/{sessionId}/frame/parent` | `switchToParentFrame` | ✓ | ✗ | 5                 |

#### 2.10.4 element extras

| Method | Path | operationId | W3C | In openapi.yaml | Next Step (human) |
|---|---|---|---|---|-------------------|
| POST | `/session/{sessionId}/element/active` | `getActiveElement` | ✓ | ✗ | 2                 |
| POST | `/session/{sessionId}/element/{elementId}/element` | `findElementFromElement` | ✓ | ✗ | 2                 |
| POST | `/session/{sessionId}/element/{elementId}/elements` | `findElementsFromElement` | ✓ | ✗ | 2                 |
| POST | `/session/{sessionId}/element/{elementId}/clear` | `clearElement` | ✓ | ✗ | 2                 |
| GET | `/session/{sessionId}/element/{elementId}/selected` | `isElementSelected` | ✓ | ✗ | 2                 |
| GET | `/session/{sessionId}/element/{elementId}/enabled` | `isElementEnabled` | ✓ | ✗ | 2                 |
| GET | `/session/{sessionId}/element/{elementId}/displayed` | `isElementDisplayed` | ✓ | ✗ | 2                 |
| GET | `/session/{sessionId}/element/{elementId}/name` | `getElementTagName` | ✓ | ✗ | 2                 |
| GET | `/session/{sessionId}/element/{elementId}/rect` | `getElementRect` | ✓ | ✗ | 2                 |
| GET | `/session/{sessionId}/element/{elementId}/property/{name}` | `getElementProperty` | ✓ | ✗ | 2                 |
| GET | `/session/{sessionId}/element/{elementId}/css/{propertyName}` | `getElementCssValue` | ✓ | ✗ | 2                 |

#### 2.10.5 actions / alerts / cookies / screenshots / print

| Method | Path | operationId | W3C | In openapi.yaml | Next Step (human) |
|---|---|---|---|---|-------------------|
| POST | `/session/{sessionId}/actions` | `performActions` | ✓ | ✗ | 5                 |
| DELETE | `/session/{sessionId}/actions` | `releaseActions` | ✓ | ✗ | 5                 |
| GET | `/session/{sessionId}/alert/text` | `getAlertText` | ✓ | ✗ | 5                 |
| POST | `/session/{sessionId}/alert/text` | `sendAlertText` | ✓ | ✗ | 5                 |
| POST | `/session/{sessionId}/alert/accept` | `acceptAlert` | ✓ | ✗ | 5                 |
| POST | `/session/{sessionId}/alert/dismiss` | `dismissAlert` | ✓ | ✗ | 5                 |
| GET | `/session/{sessionId}/cookie` | `getAllCookies` | ✓ | ✗ | 2                 |
| POST | `/session/{sessionId}/cookie` | `addCookie` | ✓ | ✗ | 2                 |
| DELETE | `/session/{sessionId}/cookie` | `deleteAllCookies` | ✓ | ✗ | 2                 |
| DELETE | `/session/{sessionId}/cookie/{name}` | `deleteCookie` | ✓ | ✗ |                   |
| GET | `/session/{sessionId}/screenshot` | `takeScreenshot` | ✓ | ✗ | 2                 |
| GET | `/session/{sessionId}/element/{elementId}/screenshot` | `takeElementScreenshot` | ✓ | ✗ | 2                 |
| POST | `/session/{sessionId}/print` | `printPage` | ✓ | ✗ | 5                 |

#### 2.10.6 shadow root

| Method | Path | operationId | W3C | In openapi.yaml | Next Step (human) |
|---|---|---|---|---|-------------------|
| GET | `/session/{sessionId}/element/{elementId}/shadow` | `getShadowRoot` | ✓ | ✗ | 5                 |
| POST | `/session/{sessionId}/shadow/{shadowId}/element` | `findElementFromShadowRoot` | ✓ | ✗ | 5                 |
| POST | `/session/{sessionId}/shadow/{shadowId}/elements` | `findElementsFromShadowRoot` | ✓ | ✗ | 5                 |

---

## 3. Spec → Controller Mapping (Implementation Map)

The WebDriver-Compatible API implementation is mostly located under:

- `pulsar-rest/src/main/kotlin/ai/platon/pulsar/rest/openapi/controller/`

Controller files by tag:

| Tag | Controller |
|---|---|
| session | `SessionController.kt` |
| navigation | `NavigationController.kt` |
| selectors | `SelectorController.kt` |
| element | `ElementController.kt` |
| script | `ScriptController.kt` |
| control | `ControlController.kt` |
| events | `EventsController.kt` |
| agent | `AgentController.kt` |
| pulsar | `PulsarSessionController.kt` |

Key dependencies (used to differentiate real vs mock):

- entry for real sessions and real capabilities: `pulsar-rest/.../openapi/service/SessionManager.kt` (when present/injected, controllers enable the real branch)
- mock/demo storage: `pulsar-rest/.../openapi/store/InMemoryStore.kt`

---

## 4. Implementation Coverage Matrix (real / mock)

> Definitions:
> - **real**: the controller obtains a session through `SessionManager` and invokes real actions via `session.pulsarSession.*` or `session.pulsarSession.getOrCreateBoundDriver()` / `session.agent.*`.
> - **mock**: the controller only operates on sessions/elements/events stored in `InMemoryStore`, returning demo data.
>
> Note: the current implementation allows real and mock to coexist (the same endpoint can take different branches under different runtime configurations).

| Tag | Endpoint (representative) | Real | Mock | Notes |
|---|---|---:|---:|---|
| session | `/session` `/session/{id}` | ✅ | ✅ | `SessionController`: switches real/mock depending on whether `SessionManager` is injected |
| navigation | `/session/{id}/url` | ✅ | ✅ | real mode calls `pulsarSession.load(url)`, but `GET url/documentUri/baseUri` currently mostly returns the “stored url” |
| selectors | `/selectors/exists` `/waitFor` `/click` `/fill` `/press` `/outerHtml` `/screenshot` | ✅ | ✅ | real mode executes via bound driver; mock mode is mostly demo (e.g., exists always true, screenshot returns mock base64) |
| selectors | `/selectors/element(s)` | ❌ | ✅ | currently element(s) lookup is still based on generating elementId from the store (real mode is not aligned to “find via driver”) |
| element | `/element/{elementId}/*` | ✅ (partial) | ✅ | real mode maps elementId → selector (via store) and then operates via driver; elementId still originates from the store |
| script | `/execute/sync` `/execute/async` | ✅ | ✅ | real uses driver.evaluate; mock returns null |
| control | `/control/*` | ✅ (partial) | ✅ | `/control/delay` sleeps for requested duration (capped at 30s); `/control/pause` and `/control/stop` update session status in `SessionManager`. No direct driver-level pause/stop yet. |
| events | `/event-configs` `/events` `/events/subscribe` | ❌ | ✅ | mock only (in-memory event system), not a real browser event stream |
| agent | `/agent/*` | ✅ | ✅ | real calls `session.agent.*`; mock returns demo responses |
| pulsar | `/normalize` `/open` `/load` `/submit` | ✅ | ✅ | real calls `pulsarSession.*`; mock returns a demo WebPageResult |

---

## 5. Known Semantic Differences and Notes (spec vs implementation)

### 5.1 The meaning of “current URL” in navigation

- `POST /url` (real) triggers a load: `pulsarSession.load(request.url)`, and writes the url into `SessionManager`.
- `GET /url` / `GET /documentUri` / `GET /baseUri` (real) currently mostly uses the **url stored in SessionManager / the session object**.
    - This is not fully equivalent to the WebDriver semantics of “read the current address/document URI from the real browser”.

### 5.2 elementId semantics in selectors / element

- `elementId` currently behaves more like a “server-side session store handle”.
- In real mode, element click/fill/text/attribute operations map elementId back to a selector, then execute via the driver.
    - That means elementId lifetime/validity is controlled by the store, not a native browser reference.

### 5.3 control / events status

- `control` endpoints now have partial real implementation:
    - `/control/delay`: sleeps for the requested duration (capped at 30 seconds to prevent resource exhaustion).
    - `/control/pause` and `/control/stop`: update session status in `SessionManager`, but do not yet pause/stop driver-level operations.
- `events` currently have no real branch: they mainly serve as demos/placeholders (in-memory event system).
- To fully align with WebDriver / real browser event streams, driver-side capabilities and a clearer state machine/subscription model would be needed.

---

## 6. Maintenance Suggestions (Keeping spec and implementation in sync)

1. **Treat `openapi.yaml` as the single source of spec truth**: when adding/modifying endpoints, update the yaml first, then add the controller logic and update this mapping/matrix.
2. **Make demo-only endpoints explicit**: consider consistent markings in controllers or docs (e.g., `@Deprecated("demo-only")` or README labels) to avoid accidental misuse.
3. **Prioritize real implementation gaps (by value)**:
    - P0: align real find for `selectors/element(s)` (find via driver and return a stable elementId strategy)
    - P1: design real semantics for `events` (browser event stream integration, if publicly promised)
    - P1: further align navigation “current URL/documentUri” retrieval
    - P2: extend `control/pause` and `control/stop` to pause/stop driver-level operations
4. **Add minimal contract tests (MockMvc/WebTestClient)** covering at least:
    - `POST /session` → returns sessionId
    - 404 error body matches `ErrorResponse`
    - basic success path for `POST /session/{id}/load` or `POST /session/{id}/url`

---

## 7. Quick Verification (Windows / PowerShell / Maven Wrapper)

> Note: this repo is a multi-module Maven project. Use `mvnw.cmd` at the project root.

```powershell
# 1) Quick build (skip tests)
.\mvnw.cmd -q -DskipTests package

# 2) Run tests for the REST module only (will build dependent modules)
.\mvnw.cmd -pl pulsar-rest -am test -D"surefire.failIfNoSpecifiedTests=false"
```

---

## Appendix A: Other REST surfaces

There are also REST controllers outside `/session...` in this repo (for example, `/api/*` commands, chat, extraction, etc.). Whether those endpoints are included in OpenAPI (and whether they belong to the same contract surface as this doc) should be described in a separate document to avoid mixing them with the WebDriver-Compatible API.
