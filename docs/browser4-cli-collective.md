# browser4-cli Collective Commands

## Overview

The **Collective** commands in `browser4-cli` enable multi-agent coordination for
complex tasks that benefit from parallel execution across multiple browser
sessions. A collective distributes work across multiple `BasicBrowserAgent`
instances, each operating in its own browser session.

> **Note:** Collective commands are currently in preview. The server-side
> implementation is evolving and some features may have limited functionality.

---

## Commands

### `collective-run` — Run a multi-agent task

Submit a task that will be distributed across multiple agents for parallel
execution. This is a long-running operation that returns immediately with a task
ID.

```
browser4-cli collective-run <task> [--agents=<n>]
```

**Arguments:**

| Argument | Required | Description                                              |
|----------|----------|----------------------------------------------------------|
| `task`   | Yes      | Natural language task for the collective to execute      |

**Options:**

| Option     | Description                                              |
|------------|----------------------------------------------------------|
| `--agents` | Number of agents to use (default: auto — server decides) |

**Examples:**

```bash
# Run a collective task with auto agent count
browser4-cli collective-run "scrape the top 10 products from amazon.com/bestsellers and extract name, price, rating for each"

# Specify the number of agents
browser4-cli collective-run "visit these 5 news sites and summarize the top story from each" --agents=5
```

**How it works:**

The `collective-run` command submits the task to the Browser4 server via
`POST /api/commands/plain?async=true`. The server can distribute the work across
multiple agent instances, each with its own browser session. Results are
aggregated upon completion.

---

### `collective-status` — Check collective task status

Check the status of a running collective task.

```
browser4-cli collective-status <id>
```

**Arguments:**

| Argument | Required | Description                                 |
|----------|----------|---------------------------------------------|
| `id`     | Yes      | Task ID returned by `collective-run`        |

**Examples:**

```bash
browser4-cli collective-status abc-123-def
```

**Response:**

Returns a JSON object with the current status:

```json
{
  "id": "abc-123-def",
  "status": "running",
  "agents": 5,
  "completed": 3,
  "progress": "3/5 agents completed"
}
```

---

### `collective-result` — Get collective task result

Get the aggregated result of a completed collective task.

```
browser4-cli collective-result <id>
```

**Arguments:**

| Argument | Required | Description                              |
|----------|----------|------------------------------------------|
| `id`     | Yes      | Task ID returned by `collective-run`     |

**Examples:**

```bash
browser4-cli collective-result abc-123-def
```

---

### `collective-list` — List running collective tasks

List all currently running collective tasks.

```
browser4-cli collective-list
```

**Examples:**

```bash
browser4-cli collective-list
```

> **Note:** This command has limited server-side support in the current release.

---

## Workflow Example

A typical collective workflow for data gathering:

```bash
# 1. Submit a multi-site data gathering task
browser4-cli collective-run "visit these product pages and extract price, title, and availability: \
  https://amazon.com/dp/B08PP5MSVB \
  https://amazon.com/dp/B09V3KXJPB \
  https://amazon.com/dp/B0BSHF7WHW" --agents=3

# Output: Collective task submitted: abc-123-def
# Output: Use 'browser4-cli collective-status abc-123-def' to check progress.

# 2. Monitor progress
browser4-cli collective-status abc-123-def

# 3. Retrieve aggregated results
browser4-cli collective-result abc-123-def
```

---

## Use Cases

### Parallel Data Extraction

Collect product data from multiple pages simultaneously:

```bash
browser4-cli collective-run "extract product specs from these URLs and return as JSON: \
  page1_url, page2_url, page3_url" --agents=3
```

### Multi-Site Monitoring

Monitor prices or content across multiple sites:

```bash
browser4-cli collective-run "check the current price of 'MacBook Pro 16' on: \
  amazon.com, bestbuy.com, newegg.com" --agents=3
```

### Content Aggregation

Aggregate news or content from multiple sources:

```bash
browser4-cli collective-run "get the top headline from: \
  news.ycombinator.com, reddit.com/r/technology, techcrunch.com" --agents=3
```

---

## Configuration

Collective commands share the same LLM configuration as agent commands. Ensure
the Browser4 server has an LLM provider configured:

```properties
# Example: OpenRouter provider
openrouter.api.key=sk-your-api-key-here
openrouter.model.name=bytedance-seed/seed-1.6

# Example: OpenAI-compatible provider
# openai.api.key=sk-your-api-key-here
# openai.model.name=gpt-4o
# openai.base.url=https://api.openai.com/v1
```

---

## REST API Mapping

| CLI Command          | REST Endpoint                          | Method |
|----------------------|----------------------------------------|--------|
| `collective-run`     | `/api/commands/plain?async=true`       | POST   |
| `collective-status`  | `/api/commands/{id}/status`            | GET    |
| `collective-result`  | `/api/commands/{id}/result`            | GET    |
| `collective-list`    | *(not yet supported)*                  | —      |

---

## Comparison: Agent vs Collective

| Feature              | Agent (`agent-run`)         | Collective (`collective-run`)    |
|----------------------|-----------------------------|---------------------------------|
| Browser sessions     | Single session              | Multiple parallel sessions      |
| Task complexity      | Single-flow tasks           | Distributable multi-target tasks|
| Execution model      | Sequential observe→act      | Parallel across agents          |
| Best for             | Complex single-page tasks   | Multi-page data gathering       |
| Resource usage       | Low (1 browser)             | Higher (N browsers)             |

---

## See Also

- [browser4-cli-agent.md](browser4-cli-agent.md) — Single-agent commands
- [rest-api-examples.md](rest-api-examples.md) — REST API usage examples
- [BasicBrowserAgent.kt](../pulsar-agentic/src/main/kotlin/ai/platon/pulsar/agentic/agents/BasicBrowserAgent.kt) — Agent implementation
