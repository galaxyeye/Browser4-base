use serde::{Deserialize, Serialize};
use std::collections::HashMap;

/// Represents a loaded web page
#[derive(Debug, Clone, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct WebPage {
    pub url: String,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub location: Option<String>,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub content_type: Option<String>,
    #[serde(default)]
    pub content_length: i32,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub protocol_status: Option<String>,
    #[serde(default)]
    pub is_nil: bool,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub html: Option<String>,
}

impl Default for WebPage {
    fn default() -> Self {
        Self {
            url: String::new(),
            location: None,
            content_type: None,
            content_length: 0,
            protocol_status: None,
            is_nil: false,
            html: None,
        }
    }
}

/// Normalized URL with parsed load arguments
#[derive(Debug, Clone, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct NormURL {
    pub url: String,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub args: Option<String>,
    #[serde(default)]
    pub is_nil: bool,
}

/// Result from agent act operation
#[derive(Debug, Clone, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct AgentActResult {
    #[serde(default)]
    pub success: bool,
    #[serde(default)]
    pub message: String,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub action: Option<String>,
    #[serde(default)]
    pub is_complete: bool,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub expression: Option<String>,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub result: Option<serde_json::Value>,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub trace: Option<Vec<String>>,
}

impl Default for AgentActResult {
    fn default() -> Self {
        Self {
            success: false,
            message: String::new(),
            action: None,
            is_complete: false,
            expression: None,
            result: None,
            trace: None,
        }
    }
}

/// Result from agent run operation
#[derive(Debug, Clone, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct AgentRunResult {
    #[serde(default)]
    pub success: bool,
    #[serde(default)]
    pub message: String,
    #[serde(default)]
    pub history_size: i32,
    #[serde(default)]
    pub process_trace_size: i32,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub final_result: Option<serde_json::Value>,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub trace: Option<Vec<String>>,
}

impl Default for AgentRunResult {
    fn default() -> Self {
        Self {
            success: false,
            message: String::new(),
            history_size: 0,
            process_trace_size: 0,
            final_result: None,
            trace: None,
        }
    }
}

/// Single observation result from agent observe operation
#[derive(Debug, Clone, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct ObserveResult {
    #[serde(skip_serializing_if = "Option::is_none")]
    pub locator: Option<String>,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub domain: Option<String>,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub method: Option<String>,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub arguments: Option<HashMap<String, serde_json::Value>>,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub description: Option<String>,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub screenshot_content_summary: Option<String>,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub current_page_content_summary: Option<String>,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub next_goal: Option<String>,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub thinking: Option<String>,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub summary: Option<String>,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub key_findings: Option<String>,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub next_suggestions: Option<Vec<String>>,
}

/// Agent observation with multiple observation results
#[derive(Debug, Clone, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct AgentObservation {
    #[serde(default)]
    pub observations: Vec<ObserveResult>,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub summary: Option<String>,
}

impl Default for AgentObservation {
    fn default() -> Self {
        Self {
            observations: Vec::new(),
            summary: None,
        }
    }
}

/// Extraction result from agent extract operation
#[derive(Debug, Clone, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct ExtractionResult {
    #[serde(default)]
    pub success: bool,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub data: Option<serde_json::Value>,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub message: Option<String>,
}

impl Default for ExtractionResult {
    fn default() -> Self {
        Self {
            success: false,
            data: None,
            message: None,
        }
    }
}

/// Chat response from LLM
#[derive(Debug, Clone, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct ChatResponse {
    #[serde(default)]
    pub response: String,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub model: Option<String>,
}

impl Default for ChatResponse {
    fn default() -> Self {
        Self {
            response: String::new(),
            model: None,
        }
    }
}

/// Agent state in history
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct AgentState {
    pub step: usize,
    pub action: String,
    pub result: Option<serde_json::Value>,
    pub success: bool,
    pub message: String,
}

/// Agent history
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct AgentHistory {
    pub states: Vec<AgentState>,
    pub has_errors: bool,
}

impl Default for AgentHistory {
    fn default() -> Self {
        Self {
            states: Vec::new(),
            has_errors: false,
        }
    }
}
