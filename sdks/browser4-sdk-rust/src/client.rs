use reqwest::{Client as HttpClient, Response};
use serde_json::{json, Value};
use std::collections::HashMap;
use std::sync::Arc;
use tokio::sync::RwLock;

use crate::error::{Browser4Error, Result};

/// PulsarClient provides low-level HTTP client for Browser4 API communication
pub struct PulsarClient {
    base_url: String,
    session_id: Arc<RwLock<Option<String>>>,
    http_client: HttpClient,
    default_headers: HashMap<String, String>,
}

impl PulsarClient {
    /// Creates a new PulsarClient with default settings
    pub fn new() -> Self {
        Self::builder().build()
    }

    /// Creates a new PulsarClient with custom base URL
    pub fn with_base_url(base_url: impl Into<String>) -> Self {
        Self::builder().base_url(base_url).build()
    }

    /// Creates a new PulsarClient with initial session ID
    pub fn with_session_id(session_id: impl Into<String>) -> Self {
        Self::builder().session_id(session_id).build()
    }

    /// Creates a builder for PulsarClient
    pub fn builder() -> PulsarClientBuilder {
        PulsarClientBuilder::default()
    }

    /// Gets the current session ID
    pub async fn session_id(&self) -> Option<String> {
        self.session_id.read().await.clone()
    }

    /// Sets the session ID
    pub async fn set_session_id(&self, session_id: Option<String>) {
        *self.session_id.write().await = session_id;
    }

    /// Gets the resolved base URL
    pub fn base_url(&self) -> &str {
        &self.base_url
    }

    /// Creates a new session
    pub async fn create_session(&self) -> Result<String> {
        let response = self.post("/session", &json!({})).await?;
        let session_id = response
            .get("sessionId")
            .and_then(|v| v.as_str())
            .ok_or_else(|| Browser4Error::InvalidResponse("Missing sessionId".to_string()))?
            .to_string();

        self.set_session_id(Some(session_id.clone())).await;
        Ok(session_id)
    }

    /// Deletes the current session
    pub async fn delete_session(&self) -> Result<()> {
        let session_id = self
            .session_id()
            .await
            .ok_or_else(|| Browser4Error::NoSession)?;

        self.delete(&format!("/session/{}", session_id)).await?;
        self.set_session_id(None).await;
        Ok(())
    }

    /// Makes a POST request
    pub async fn post(&self, path: &str, body: &Value) -> Result<Value> {
        let url = self.resolve_url(path).await?;
        let response = self
            .http_client
            .post(&url)
            .headers(self.build_headers())
            .json(body)
            .send()
            .await?;

        self.parse_response(response).await
    }

    /// Makes a GET request
    pub async fn get(&self, path: &str) -> Result<Value> {
        let url = self.resolve_url(path).await?;
        let response = self
            .http_client
            .get(&url)
            .headers(self.build_headers())
            .send()
            .await?;

        self.parse_response(response).await
    }

    /// Makes a DELETE request
    pub async fn delete(&self, path: &str) -> Result<Value> {
        let url = self.resolve_url(path).await?;
        let response = self
            .http_client
            .delete(&url)
            .headers(self.build_headers())
            .send()
            .await?;

        self.parse_response(response).await
    }

    /// Resolves URL with session ID replacement
    async fn resolve_url(&self, path: &str) -> Result<String> {
        let url = if path.contains("{sessionId}") {
            let session_id = self
                .session_id()
                .await
                .ok_or_else(|| Browser4Error::NoSession)?;
            path.replace("{sessionId}", &session_id)
        } else {
            path.to_string()
        };

        Ok(format!("{}{}", self.base_url.trim_end_matches('/'), url))
    }

    /// Builds HTTP headers
    fn build_headers(&self) -> reqwest::header::HeaderMap {
        let mut headers = reqwest::header::HeaderMap::new();
        headers.insert(
            reqwest::header::CONTENT_TYPE,
            "application/json".parse().unwrap(),
        );
        
        for (key, value) in &self.default_headers {
            if let Ok(header_name) = reqwest::header::HeaderName::from_bytes(key.as_bytes()) {
                if let Ok(header_value) = reqwest::header::HeaderValue::from_str(value) {
                    headers.insert(header_name, header_value);
                }
            }
        }
        
        headers
    }

    /// Parses HTTP response
    async fn parse_response(&self, response: Response) -> Result<Value> {
        let status = response.status();
        
        if !status.is_success() {
            let error_text = response.text().await?;
            return Err(Browser4Error::HttpError(status.as_u16(), error_text));
        }

        let value: Value = response.json().await?;
        Ok(value)
    }

    /// Gets the raw HTTP client
    pub fn http_client(&self) -> &HttpClient {
        &self.http_client
    }
}

impl Default for PulsarClient {
    fn default() -> Self {
        Self::new()
    }
}

/// Builder for PulsarClient
pub struct PulsarClientBuilder {
    base_url: String,
    session_id: Option<String>,
    default_headers: HashMap<String, String>,
}

impl Default for PulsarClientBuilder {
    fn default() -> Self {
        Self {
            base_url: "http://localhost:8182".to_string(),
            session_id: None,
            default_headers: HashMap::new(),
        }
    }
}

impl PulsarClientBuilder {
    /// Sets the base URL
    pub fn base_url(mut self, base_url: impl Into<String>) -> Self {
        self.base_url = base_url.into();
        self
    }

    /// Sets the initial session ID
    pub fn session_id(mut self, session_id: impl Into<String>) -> Self {
        self.session_id = Some(session_id.into());
        self
    }

    /// Adds a default header
    pub fn header(mut self, key: impl Into<String>, value: impl Into<String>) -> Self {
        self.default_headers.insert(key.into(), value.into());
        self
    }

    /// Builds the PulsarClient
    pub fn build(self) -> PulsarClient {
        PulsarClient {
            base_url: self.base_url,
            session_id: Arc::new(RwLock::new(self.session_id)),
            http_client: HttpClient::new(),
            default_headers: self.default_headers,
        }
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_client_creation() {
        let client = PulsarClient::new();
        assert_eq!(client.base_url(), "http://localhost:8182");
    }

    #[test]
    fn test_client_with_base_url() {
        let client = PulsarClient::with_base_url("http://custom-server:9999");
        assert_eq!(client.base_url(), "http://custom-server:9999");
    }

    #[tokio::test]
    async fn test_session_id_management() {
        let client = PulsarClient::new();
        assert!(client.session_id().await.is_none());

        client.set_session_id(Some("test-session-123".to_string())).await;
        assert_eq!(client.session_id().await, Some("test-session-123".to_string()));
    }
}
