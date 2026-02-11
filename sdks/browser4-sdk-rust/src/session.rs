use scraper::{Html, Selector};
use serde_json::json;
use std::collections::HashMap;
use std::sync::Arc;

use crate::client::PulsarClient;
use crate::error::{Browser4Error, Result};
use crate::models::{ChatResponse, NormURL, WebPage};
use crate::webdriver::WebDriver;

/// PulsarSession provides methods for loading pages, parsing, and extraction
pub struct PulsarSession {
    client: Arc<PulsarClient>,
    driver: Option<Arc<WebDriver>>,
}

impl PulsarSession {
    /// Creates a new PulsarSession with the given client
    pub fn new(client: Arc<PulsarClient>) -> Self {
        Self {
            client,
            driver: None,
        }
    }

    /// Gets the client
    pub fn client(&self) -> &Arc<PulsarClient> {
        &self.client
    }

    /// Gets the session ID
    pub async fn session_id(&self) -> Option<String> {
        self.client.session_id().await
    }

    /// Checks if the session is active
    pub async fn is_active(&self) -> bool {
        self.session_id().await.is_some()
    }

    /// Gets a display string for the session
    pub async fn display(&self) -> String {
        match self.session_id().await {
            Some(id) => {
                let short_id = id.chars().take(8).collect::<String>();
                format!("PulsarSession({}...)", short_id)
            }
            None => "PulsarSession(no-session)".to_string(),
        }
    }

    // ========== URL Normalization ==========

    /// Normalizes a URL with optional load arguments
    pub async fn normalize(
        &self,
        url: &str,
        args: Option<&str>,
        to_item_option: bool,
    ) -> Result<NormURL> {
        let mut payload = json!({
            "url": url,
            "toItemOption": to_item_option
        });

        if let Some(args) = args {
            payload["args"] = json!(args);
        }

        let value = self
            .client
            .post("/session/{sessionId}/normalize", &payload)
            .await?;

        Ok(serde_json::from_value(value)?)
    }

    /// Normalizes a URL, returning None if invalid
    pub async fn normalize_or_none(
        &self,
        url: Option<&str>,
        args: Option<&str>,
        to_item_option: bool,
    ) -> Result<Option<NormURL>> {
        match url {
            None | Some("") => Ok(None),
            Some(url) => {
                let result = self.normalize(url, args, to_item_option).await?;
                Ok(if result.is_nil { None } else { Some(result) })
            }
        }
    }

    // ========== Page Loading ==========

    /// Opens a URL immediately, bypassing local cache
    pub async fn open(&self, url: &str, args: Option<&str>) -> Result<WebPage> {
        let mut payload = json!({ "url": url });
        if let Some(args) = args {
            payload["args"] = json!(args);
        }

        let value = self.client.post("/session/{sessionId}/open", &payload).await?;
        Ok(serde_json::from_value(value)?)
    }

    /// Loads a URL from local storage or fetches from internet
    pub async fn load(&self, url: &str, args: Option<&str>) -> Result<WebPage> {
        let mut payload = json!({ "url": url });
        if let Some(args) = args {
            payload["args"] = json!(args);
        }

        let value = self.client.post("/session/{sessionId}/load", &payload).await?;
        Ok(serde_json::from_value(value)?)
    }

    /// Loads multiple URLs
    pub async fn load_all(&self, urls: &[&str], args: Option<&str>) -> Result<Vec<WebPage>> {
        let mut pages = Vec::new();
        for url in urls {
            pages.push(self.load(url, args).await?);
        }
        Ok(pages)
    }

    /// Submits a URL to the crawl pool for asynchronous processing
    pub async fn submit(&self, url: &str, args: Option<&str>) -> Result<bool> {
        let mut payload = json!({ "url": url });
        if let Some(args) = args {
            payload["args"] = json!(args);
        }

        let value = self
            .client
            .post("/session/{sessionId}/submit", &payload)
            .await?;
        Ok(value.as_bool().unwrap_or(true))
    }

    /// Submits multiple URLs to the crawl pool
    pub async fn submit_all(&self, urls: &[&str], args: Option<&str>) -> Result<bool> {
        for url in urls {
            if !self.submit(url, args).await? {
                return Ok(false);
            }
        }
        Ok(true)
    }

    // ========== Parsing and Extraction ==========

    /// Parses a WebPage into an HTML document
    pub fn parse(&self, page: &WebPage) -> Option<Html> {
        page.html.as_ref().map(|html| Html::parse_document(html))
    }

    /// Extracts fields from an HTML document using CSS selectors
    pub fn extract(
        &self,
        document: &Html,
        field_selectors: &HashMap<String, String>,
    ) -> HashMap<String, Option<String>> {
        field_selectors
            .iter()
            .map(|(field, selector)| {
                let value = Selector::parse(selector)
                    .ok()
                    .and_then(|sel| document.select(&sel).next())
                    .and_then(|element| Some(element.text().collect::<String>()));
                (field.clone(), value)
            })
            .collect()
    }

    /// Extracts fields from a document using CSS selectors (selector becomes field name)
    pub fn extract_with_selectors(
        &self,
        document: &Html,
        selectors: &[&str],
    ) -> HashMap<String, Option<String>> {
        let field_selectors: HashMap<String, String> = selectors
            .iter()
            .map(|&s| (s.to_string(), s.to_string()))
            .collect();
        self.extract(document, &field_selectors)
    }

    /// Loads a page, parses it, and extracts fields in one operation
    pub async fn scrape(
        &self,
        url: &str,
        args: &str,
        field_selectors: &HashMap<String, String>,
    ) -> Result<HashMap<String, Option<String>>> {
        let page = self.load(url, Some(args)).await?;
        let document = self
            .parse(&page)
            .ok_or_else(|| Browser4Error::ParseError("Failed to parse HTML".to_string()))?;
        Ok(self.extract(&document, field_selectors))
    }

    // ========== Chat/LLM Operations ==========

    /// Sends a prompt to the LLM and returns the response
    pub async fn chat(&self, prompt: &str) -> Result<ChatResponse> {
        let payload = json!({ "prompt": prompt });
        let value = self.client.post("/session/{sessionId}/chat", &payload).await?;
        Ok(serde_json::from_value(value)?)
    }

    /// Sends a user message and system message to the LLM
    pub async fn chat_with_system(
        &self,
        user_message: &str,
        system_message: &str,
    ) -> Result<ChatResponse> {
        let payload = json!({
            "userMessage": user_message,
            "systemMessage": system_message
        });
        let value = self.client.post("/session/{sessionId}/chat", &payload).await?;
        Ok(serde_json::from_value(value)?)
    }

    // ========== Driver Management ==========

    /// Gets or creates a bound WebDriver
    pub fn get_or_create_driver(&mut self) -> Arc<WebDriver> {
        if self.driver.is_none() {
            self.driver = Some(Arc::new(WebDriver::new(self.client.clone())));
        }
        self.driver.as_ref().unwrap().clone()
    }

    /// Gets the bound driver
    pub fn driver(&self) -> Option<&Arc<WebDriver>> {
        self.driver.as_ref()
    }

    /// Binds a WebDriver to this session
    pub fn bind_driver(&mut self, driver: Arc<WebDriver>) {
        self.driver = Some(driver);
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    #[tokio::test]
    async fn test_session_creation() {
        let client = Arc::new(PulsarClient::with_session_id("test-session"));
        let session = PulsarSession::new(client);

        assert_eq!(
            session.session_id().await,
            Some("test-session".to_string())
        );
        assert!(session.is_active().await);
    }

    #[tokio::test]
    async fn test_session_display() {
        let client = Arc::new(PulsarClient::with_session_id("abcdefgh12345678"));
        let session = PulsarSession::new(client);

        let display = session.display().await;
        assert!(display.contains("abcdefgh"));
    }

    #[tokio::test]
    async fn test_session_no_session() {
        let client = Arc::new(PulsarClient::new());
        let session = PulsarSession::new(client);

        assert!(!session.is_active().await);
        let display = session.display().await;
        assert!(display.contains("no-session"));
    }
}
