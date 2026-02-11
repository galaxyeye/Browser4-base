# Browser4 SDK: Kotlin vs Rust API Comparison

This document provides a comprehensive comparison between the Kotlin and Rust SDKs for Browser4.

## Overview

Both SDKs provide equivalent functionality with idiomatic implementations for their respective languages:
- **Kotlin SDK**: JVM-based, uses coroutines, Ktor HTTP client
- **Rust SDK**: Native, uses tokio async runtime, reqwest HTTP client

## Core Components

| Component | Kotlin | Rust | Status |
|-----------|--------|------|--------|
| HTTP Client | `PulsarClient` | `PulsarClient` | ✅ Aligned |
| Session | `PulsarSession` | `PulsarSession` | ✅ Aligned |
| WebDriver | `WebDriver` | `WebDriver` | ✅ Aligned |
| AI Agent | `AgenticSession` | `AgenticSession` | ✅ Aligned |
| Models | Various data classes | Serde structs | ✅ Aligned |
| Error Handling | Exceptions | `Result<T, Browser4Error>` | ✅ Aligned |

## API Comparison

### PulsarClient

#### Kotlin
```kotlin
val client = PulsarClient(baseUrl = "http://localhost:8182")
client.createSession()
val result = client.post("/path", payload)
client.deleteSession()
client.close()
```

#### Rust
```rust
let client = Arc::new(PulsarClient::with_base_url("http://localhost:8182"));
client.create_session().await?;
let result = client.post("/path", &payload).await?;
client.delete_session().await?;
```

### PulsarSession

#### Kotlin
```kotlin
val session = PulsarSession(client)
val page = session.load("https://example.com", "-expire 1d")
val document = session.parse(page)
val fields = session.extract(document, mapOf("title" to "h1"))
```

#### Rust
```rust
let session = PulsarSession::new(client);
let page = session.load("https://example.com", Some("-expire 1d")).await?;
let document = session.parse(&page);
let mut selectors = HashMap::new();
selectors.insert("title".to_string(), "h1".to_string());
let fields = session.extract(&document, &selectors);
```

### WebDriver

#### Kotlin
```kotlin
val driver = WebDriver(client)
driver.navigateTo("https://example.com")
driver.click("button.submit")
driver.fill("input[name='search']", "kotlin")
val text = driver.selectFirstTextOrNull("h1")
```

#### Rust
```rust
let mut driver = WebDriver::new(client);
driver.navigate_to("https://example.com").await?;
driver.click("button.submit").await?;
driver.fill("input[name='search']", "rust").await?;
let text = driver.select_first_text("h1").await?;
```

### AgenticSession

#### Kotlin
```kotlin
val session = AgenticSession(client)
val actResult = session.act("click the search button")
val runResult = session.run("search for 'kotlin'")
val observation = session.observe("find interactive elements")
val extraction = session.agentExtract("extract title and description")
val summary = session.summarize()
```

#### Rust
```rust
let mut session = AgenticSession::new(client);
let act_result = session.act("click the search button", false, None, None, None, None).await?;
let run_result = session.run("search for 'rust'", false, None, None, None, None).await?;
let observation = session.observe(Some("find interactive elements"), None, None, None, true).await?;
let extraction = session.extract("extract title and description", None, None, None, None).await?;
let summary = session.summarize(None, None).await?;
```

## Data Models Comparison

### WebPage

#### Kotlin
```kotlin
data class WebPage(
    val url: String,
    val location: String? = null,
    val contentType: String? = null,
    val contentLength: Int = 0,
    val protocolStatus: String? = null,
    val isNil: Boolean = false,
    val html: String? = null
)
```

#### Rust
```rust
#[derive(Debug, Clone, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct WebPage {
    pub url: String,
    pub location: Option<String>,
    pub content_type: Option<String>,
    pub content_length: i32,
    pub protocol_status: Option<String>,
    pub is_nil: bool,
    pub html: Option<String>,
}
```

### AgentActResult

#### Kotlin
```kotlin
data class AgentActResult(
    val success: Boolean = false,
    val message: String = "",
    val action: String? = null,
    val isComplete: Boolean = false,
    val expression: String? = null,
    val result: Any? = null,
    val trace: List<String>? = null
)
```

#### Rust
```rust
#[derive(Debug, Clone, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct AgentActResult {
    pub success: bool,
    pub message: String,
    pub action: Option<String>,
    pub is_complete: bool,
    pub expression: Option<String>,
    pub result: Option<serde_json::Value>,
    pub trace: Option<Vec<String>>,
}
```

## Key Differences

### 1. Async/Await
- **Kotlin**: Uses coroutines with `suspend` functions
- **Rust**: Uses tokio async runtime with `async/await`

### 2. Null Safety
- **Kotlin**: Nullable types with `?` operator
- **Rust**: `Option<T>` type

### 3. Error Handling
- **Kotlin**: Exceptions (can be caught or propagated)
- **Rust**: `Result<T, E>` type with `?` operator

### 4. Ownership
- **Kotlin**: Garbage collected, shared references
- **Rust**: Ownership system, `Arc` for shared references

### 5. Mutability
- **Kotlin**: `var` for mutable, `val` for immutable
- **Rust**: `mut` keyword for mutability

## Test Coverage

Both SDKs have comprehensive test suites:

### Kotlin SDK Tests
- `PulsarClientTest.kt` - Client tests
- `SessionTest.kt` - Session and WebDriver tests
- `ModelsTest.kt` - Data model tests
- `JsoupParsingTest.kt` - HTML parsing tests
- `Browser4DriverTest.kt` - Driver integration tests

### Rust SDK Tests
- `client_tests.rs` - Client tests (5 tests)
- `session_tests.rs` - Session tests (4 tests)
- `webdriver_tests.rs` - WebDriver tests (1 test)
- `agentic_session_tests.rs` - Agentic session tests (2 tests)
- `models_tests.rs` - Data model tests (6 tests)
- **Total**: 27 tests (9 unit + 18 integration)

## Examples

Both SDKs provide equivalent examples:

### Kotlin SDK Examples
1. Basic usage with PulsarSession
2. WebDriver usage
3. AgenticSession with AI automation
4. Local driver example

### Rust SDK Examples
1. `basic_usage.rs` - Basic usage with PulsarSession
2. `webdriver_example.rs` - WebDriver usage
3. `agentic_session_example.rs` - AgenticSession with AI automation

## Dependencies

### Kotlin SDK
- kotlin-stdlib
- kotlinx-coroutines-core
- ktor-client (HTTP client)
- gson (JSON serialization)
- pulsar-jsoup (HTML parsing)

### Rust SDK
- tokio (Async runtime)
- reqwest (HTTP client)
- serde/serde_json (Serialization)
- scraper (HTML parsing)
- thiserror/anyhow (Error handling)
- futures/async-trait (Async utilities)

## Missing Features (To Be Implemented)

Both SDKs are missing:
1. ❌ Local driver support (Browser4.jar download and launch)
2. ❌ SSE client for real-time event streaming
3. ❌ Page event handlers with subscriptions

These features exist in the Kotlin SDK but are marked as TODO or future work.

## Performance Characteristics

| Aspect | Kotlin SDK | Rust SDK |
|--------|-----------|----------|
| Memory | JVM heap, garbage collected | Native, manual/automatic with Arc |
| Startup | JVM warmup time | Instant native execution |
| Runtime | JVM overhead | Native performance |
| Concurrency | Coroutines on JVM threads | Tokio async runtime |
| Type Safety | Strong with nullable types | Strong with ownership system |

## Recommendation

- **Use Kotlin SDK when**: Working in JVM ecosystem, need Java interop, prefer garbage collection
- **Use Rust SDK when**: Need native performance, minimal memory footprint, systems integration

Both SDKs provide equivalent functionality with idiomatic implementations for their respective ecosystems.
