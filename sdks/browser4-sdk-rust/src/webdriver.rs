use serde_json::{json, Value};
use std::collections::HashMap;
use std::sync::Arc;

use crate::client::PulsarClient;
use crate::error::Result;

/// WebDriver provides browser control and element interaction
pub struct WebDriver {
    client: Arc<PulsarClient>,
    navigate_history: Vec<String>,
}

impl WebDriver {
    /// Creates a new WebDriver with the given client
    pub fn new(client: Arc<PulsarClient>) -> Self {
        Self {
            client,
            navigate_history: Vec::new(),
        }
    }

    /// Gets the client
    pub fn client(&self) -> &Arc<PulsarClient> {
        &self.client
    }

    /// Gets the navigation history
    pub fn navigate_history(&self) -> &[String] {
        &self.navigate_history
    }

    // ========== Navigation ==========

    /// Navigates to a URL
    pub async fn navigate_to(&mut self, url: &str) -> Result<Value> {
        let payload = json!({ "url": url });
        let result = self
            .client
            .post("/session/{sessionId}/url", &payload)
            .await?;
        self.navigate_history.push(url.to_string());
        Ok(result)
    }

    /// Gets the current URL
    pub async fn current_url(&self) -> Result<String> {
        let value = self.client.get("/session/{sessionId}/url").await?;
        Ok(value
            .as_str()
            .unwrap_or_default()
            .to_string())
    }

    /// Gets the page title
    pub async fn title(&self) -> Result<String> {
        let value = self.client.get("/session/{sessionId}/title").await?;
        Ok(value
            .as_str()
            .unwrap_or_default()
            .to_string())
    }

    /// Reloads the page
    pub async fn reload(&self) -> Result<Value> {
        self.client.post("/session/{sessionId}/refresh", &json!({})).await
    }

    /// Goes back in history
    pub async fn go_back(&self) -> Result<Value> {
        self.client.post("/session/{sessionId}/back", &json!({})).await
    }

    /// Goes forward in history
    pub async fn go_forward(&self) -> Result<Value> {
        self.client.post("/session/{sessionId}/forward", &json!({})).await
    }

    // ========== Element Interaction ==========

    /// Clicks an element
    pub async fn click(&self, selector: &str) -> Result<Value> {
        let payload = json!({ "selector": selector });
        self.client
            .post("/session/{sessionId}/element/click", &payload)
            .await
    }

    /// Fills an element with text
    pub async fn fill(&self, selector: &str, text: &str) -> Result<Value> {
        let payload = json!({
            "selector": selector,
            "text": text
        });
        self.client
            .post("/session/{sessionId}/element/fill", &payload)
            .await
    }

    /// Types text into an element
    pub async fn type_text(&self, selector: &str, text: &str) -> Result<Value> {
        let payload = json!({
            "selector": selector,
            "text": text
        });
        self.client
            .post("/session/{sessionId}/element/type", &payload)
            .await
    }

    /// Presses a key on an element
    pub async fn press(&self, selector: &str, key: &str) -> Result<Value> {
        let payload = json!({
            "selector": selector,
            "key": key
        });
        self.client
            .post("/session/{sessionId}/element/press", &payload)
            .await
    }

    /// Hovers over an element
    pub async fn hover(&self, selector: &str) -> Result<Value> {
        let payload = json!({ "selector": selector });
        self.client
            .post("/session/{sessionId}/element/hover", &payload)
            .await
    }

    /// Focuses an element
    pub async fn focus(&self, selector: &str) -> Result<Value> {
        let payload = json!({ "selector": selector });
        self.client
            .post("/session/{sessionId}/element/focus", &payload)
            .await
    }

    /// Checks a checkbox
    pub async fn check(&self, selector: &str) -> Result<Value> {
        let payload = json!({ "selector": selector });
        self.client
            .post("/session/{sessionId}/element/check", &payload)
            .await
    }

    /// Unchecks a checkbox
    pub async fn uncheck(&self, selector: &str) -> Result<Value> {
        let payload = json!({ "selector": selector });
        self.client
            .post("/session/{sessionId}/element/uncheck", &payload)
            .await
    }

    // ========== Waiting ==========

    /// Waits for a selector to appear
    pub async fn wait_for_selector(&self, selector: &str, timeout_ms: Option<u64>) -> Result<Value> {
        let mut payload = json!({ "selector": selector });
        if let Some(timeout) = timeout_ms {
            payload["timeout"] = json!(timeout);
        }
        self.client
            .post("/session/{sessionId}/element/wait", &payload)
            .await
    }

    /// Waits for navigation to complete
    pub async fn wait_for_navigation(&self) -> Result<Value> {
        self.client
            .post("/session/{sessionId}/wait/navigation", &json!({}))
            .await
    }

    /// Checks if an element exists
    pub async fn exists(&self, selector: &str) -> Result<bool> {
        let payload = json!({ "selector": selector });
        let value = self
            .client
            .post("/session/{sessionId}/element/exists", &payload)
            .await?;
        Ok(value.as_bool().unwrap_or(false))
    }

    /// Checks if an element is visible
    pub async fn is_visible(&self, selector: &str) -> Result<bool> {
        let payload = json!({ "selector": selector });
        let value = self
            .client
            .post("/session/{sessionId}/element/visible", &payload)
            .await?;
        Ok(value.as_bool().unwrap_or(false))
    }

    /// Checks if an element is hidden
    pub async fn is_hidden(&self, selector: &str) -> Result<bool> {
        Ok(!self.is_visible(selector).await?)
    }

    // ========== Scrolling ==========

    /// Scrolls down
    pub async fn scroll_down(&self, count: Option<u32>) -> Result<Value> {
        let payload = json!({ "count": count.unwrap_or(1) });
        self.client
            .post("/session/{sessionId}/scroll/down", &payload)
            .await
    }

    /// Scrolls up
    pub async fn scroll_up(&self, count: Option<u32>) -> Result<Value> {
        let payload = json!({ "count": count.unwrap_or(1) });
        self.client
            .post("/session/{sessionId}/scroll/up", &payload)
            .await
    }

    /// Scrolls to an element
    pub async fn scroll_to(&self, selector: &str) -> Result<Value> {
        let payload = json!({ "selector": selector });
        self.client
            .post("/session/{sessionId}/scroll/to", &payload)
            .await
    }

    /// Scrolls to the top
    pub async fn scroll_to_top(&self) -> Result<Value> {
        self.client
            .post("/session/{sessionId}/scroll/top", &json!({}))
            .await
    }

    /// Scrolls to the bottom
    pub async fn scroll_to_bottom(&self) -> Result<Value> {
        self.client
            .post("/session/{sessionId}/scroll/bottom", &json!({}))
            .await
    }

    /// Scrolls to the middle
    pub async fn scroll_to_middle(&self, ratio: Option<f64>) -> Result<Value> {
        let payload = json!({ "ratio": ratio.unwrap_or(0.5) });
        self.client
            .post("/session/{sessionId}/scroll/middle", &payload)
            .await
    }

    // ========== Content Extraction ==========

    /// Selects first text matching selector
    pub async fn select_first_text(&self, selector: &str) -> Result<Option<String>> {
        let payload = json!({ "selector": selector });
        let value = self
            .client
            .post("/session/{sessionId}/element/text/first", &payload)
            .await?;
        Ok(value.as_str().map(|s| s.to_string()))
    }

    /// Selects all text matching selector
    pub async fn select_text_all(&self, selector: &str) -> Result<Vec<String>> {
        let payload = json!({ "selector": selector });
        let value = self
            .client
            .post("/session/{sessionId}/element/text/all", &payload)
            .await?;
        Ok(value
            .as_array()
            .map(|arr| {
                arr.iter()
                    .filter_map(|v| v.as_str().map(|s| s.to_string()))
                    .collect()
            })
            .unwrap_or_default())
    }

    /// Selects first attribute matching selector
    pub async fn select_first_attribute(
        &self,
        selector: &str,
        attribute: &str,
    ) -> Result<Option<String>> {
        let payload = json!({
            "selector": selector,
            "attribute": attribute
        });
        let value = self
            .client
            .post("/session/{sessionId}/element/attribute/first", &payload)
            .await?;
        Ok(value.as_str().map(|s| s.to_string()))
    }

    /// Selects all attributes matching selector
    pub async fn select_attribute_all(
        &self,
        selector: &str,
        attribute: &str,
    ) -> Result<Vec<String>> {
        let payload = json!({
            "selector": selector,
            "attribute": attribute
        });
        let value = self
            .client
            .post("/session/{sessionId}/element/attribute/all", &payload)
            .await?;
        Ok(value
            .as_array()
            .map(|arr| {
                arr.iter()
                    .filter_map(|v| v.as_str().map(|s| s.to_string()))
                    .collect()
            })
            .unwrap_or_default())
    }

    /// Gets outer HTML of an element
    pub async fn outer_html(&self, selector: &str) -> Result<Option<String>> {
        let payload = json!({ "selector": selector });
        let value = self
            .client
            .post("/session/{sessionId}/element/outerHtml", &payload)
            .await?;
        Ok(value.as_str().map(|s| s.to_string()))
    }

    /// Gets text content of an element
    pub async fn text_content(&self, selector: &str) -> Result<Option<String>> {
        let payload = json!({ "selector": selector });
        let value = self
            .client
            .post("/session/{sessionId}/element/textContent", &payload)
            .await?;
        Ok(value.as_str().map(|s| s.to_string()))
    }

    /// Extracts multiple fields at once
    pub async fn extract(&self, fields: &HashMap<String, String>) -> Result<HashMap<String, Option<String>>> {
        let payload = json!({ "fields": fields });
        let value = self
            .client
            .post("/session/{sessionId}/extract", &payload)
            .await?;

        let mut result = HashMap::new();
        if let Some(obj) = value.as_object() {
            for (key, val) in obj {
                result.insert(key.clone(), val.as_str().map(|s| s.to_string()));
            }
        }
        Ok(result)
    }

    // ========== Screenshots ==========

    /// Captures a screenshot
    pub async fn capture_screenshot(
        &self,
        selector: Option<&str>,
        full_page: bool,
    ) -> Result<Option<String>> {
        let mut payload = json!({ "fullPage": full_page });
        if let Some(sel) = selector {
            payload["selector"] = json!(sel);
        }
        let value = self
            .client
            .post("/session/{sessionId}/screenshot", &payload)
            .await?;
        Ok(value.as_str().map(|s| s.to_string()))
    }

    /// Captures a screenshot of an element
    pub async fn screenshot(&self, selector: Option<&str>) -> Result<Option<String>> {
        self.capture_screenshot(selector, false).await
    }

    // ========== Script Execution ==========

    /// Executes JavaScript
    pub async fn execute_script(&self, script: &str, args: Option<Vec<Value>>) -> Result<Value> {
        let payload = json!({
            "script": script,
            "args": args.unwrap_or_default()
        });
        self.client
            .post("/session/{sessionId}/execute/sync", &payload)
            .await
    }

    /// Executes async JavaScript
    pub async fn execute_async_script(
        &self,
        script: &str,
        args: Option<Vec<Value>>,
        timeout_ms: Option<u64>,
    ) -> Result<Value> {
        let mut payload = json!({
            "script": script,
            "args": args.unwrap_or_default()
        });
        if let Some(timeout) = timeout_ms {
            payload["timeout"] = json!(timeout);
        }
        self.client
            .post("/session/{sessionId}/execute/async", &payload)
            .await
    }

    /// Evaluates a JavaScript expression
    pub async fn evaluate(&self, expression: &str) -> Result<Value> {
        let payload = json!({ "expression": expression });
        self.client
            .post("/session/{sessionId}/evaluate", &payload)
            .await
    }

    // ========== Control ==========

    /// Delays execution
    pub async fn delay(&self, millis: u64) -> Result<()> {
        tokio::time::sleep(tokio::time::Duration::from_millis(millis)).await;
        Ok(())
    }

    /// Pauses execution (for debugging)
    pub async fn pause(&self) -> Result<Value> {
        self.client
            .post("/session/{sessionId}/pause", &json!({}))
            .await
    }

    /// Stops execution
    pub async fn stop(&self) -> Result<Value> {
        self.client
            .post("/session/{sessionId}/stop", &json!({}))
            .await
    }

    // ========== Event Subscription ==========

    /// Subscribes to events
    pub async fn subscribe_events(&self, options: &HashMap<String, Value>) -> Result<Value> {
        self.client
            .post("/session/{sessionId}/events/subscribe", &json!(options))
            .await
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    #[tokio::test]
    async fn test_driver_creation() {
        let client = Arc::new(PulsarClient::with_session_id("test-session"));
        let driver = WebDriver::new(client);

        assert_eq!(driver.navigate_history().len(), 0);
    }
}
