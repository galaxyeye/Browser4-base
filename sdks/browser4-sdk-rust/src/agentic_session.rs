use serde_json::{json, Value};
use std::collections::HashMap;
use std::sync::Arc;

use crate::client::PulsarClient;
use crate::error::Result;
use crate::models::{AgentActResult, AgentHistory, AgentObservation, AgentRunResult, AgentState, ExtractionResult};
use crate::session::PulsarSession;

/// AgenticSession extends PulsarSession with AI-powered browser automation
pub struct AgenticSession {
    session: PulsarSession,
    state_history: Vec<AgentState>,
    process_trace: Vec<String>,
}

impl AgenticSession {
    /// Creates a new AgenticSession with the given client
    pub fn new(client: Arc<PulsarClient>) -> Self {
        Self {
            session: PulsarSession::new(client),
            state_history: Vec::new(),
            process_trace: Vec::new(),
        }
    }

    /// Gets the underlying PulsarSession
    pub fn session(&self) -> &PulsarSession {
        &self.session
    }

    /// Gets the underlying PulsarSession mutably
    pub fn session_mut(&mut self) -> &mut PulsarSession {
        &mut self.session
    }

    /// Gets the client
    pub fn client(&self) -> &Arc<PulsarClient> {
        self.session.client()
    }

    /// Gets the agent state history
    pub fn state_history(&self) -> AgentHistory {
        AgentHistory {
            states: self.state_history.clone(),
            has_errors: self.state_history.iter().any(|s| !s.success),
        }
    }

    /// Gets the process trace
    pub fn process_trace(&self) -> &[String] {
        &self.process_trace
    }

    // ========== Agentic Operations ==========

    /// Executes a single action described in natural language
    pub async fn act(
        &mut self,
        action: &str,
        multi_act: bool,
        model_name: Option<&str>,
        variables: Option<&HashMap<String, String>>,
        dom_settle_timeout_ms: Option<u64>,
        timeout_ms: Option<u64>,
    ) -> Result<AgentActResult> {
        let mut payload = json!({ "action": action });

        if multi_act {
            payload["multiAct"] = json!(multi_act);
        }
        if let Some(model) = model_name {
            payload["modelName"] = json!(model);
        }
        if let Some(vars) = variables {
            payload["variables"] = json!(vars);
        }
        if let Some(timeout) = dom_settle_timeout_ms {
            payload["domSettleTimeoutMs"] = json!(timeout);
        }
        if let Some(timeout) = timeout_ms {
            payload["timeoutMs"] = json!(timeout);
        }

        let value = self
            .client()
            .post("/session/{sessionId}/agent/act", &payload)
            .await?;

        let result: AgentActResult = serde_json::from_value(value.clone())?;

        // Update trace
        if let Some(trace) = &result.trace {
            self.process_trace.extend(trace.clone());
        }

        // Update state history
        let step = self.state_history.len() + 1;
        self.state_history.push(AgentState {
            step,
            action: action.to_string(),
            result: result.result.clone(),
            success: result.success,
            message: result.message.clone(),
        });

        Ok(result)
    }

    /// Runs an autonomous agent task
    pub async fn run(
        &mut self,
        task: &str,
        multi_act: bool,
        model_name: Option<&str>,
        variables: Option<&HashMap<String, String>>,
        dom_settle_timeout_ms: Option<u64>,
        timeout_ms: Option<u64>,
    ) -> Result<AgentRunResult> {
        let mut payload = json!({ "task": task });

        if multi_act {
            payload["multiAct"] = json!(multi_act);
        }
        if let Some(model) = model_name {
            payload["modelName"] = json!(model);
        }
        if let Some(vars) = variables {
            payload["variables"] = json!(vars);
        }
        if let Some(timeout) = dom_settle_timeout_ms {
            payload["domSettleTimeoutMs"] = json!(timeout);
        }
        if let Some(timeout) = timeout_ms {
            payload["timeoutMs"] = json!(timeout);
        }

        let value = self
            .client()
            .post("/session/{sessionId}/agent/run", &payload)
            .await?;

        let result: AgentRunResult = serde_json::from_value(value.clone())?;

        // Update trace
        if let Some(trace) = &result.trace {
            self.process_trace.extend(trace.clone());
        }

        // Update state history
        let step = self.state_history.len() + 1;
        self.state_history.push(AgentState {
            step,
            action: format!("run: {}", task),
            result: result.final_result.clone(),
            success: result.success,
            message: result.message.clone(),
        });

        Ok(result)
    }

    /// Observes the page and returns potential actions
    pub async fn observe(
        &self,
        instruction: Option<&str>,
        model_name: Option<&str>,
        dom_settle_timeout_ms: Option<u64>,
        return_action: Option<bool>,
        draw_overlay: bool,
    ) -> Result<AgentObservation> {
        let mut payload = json!({ "drawOverlay": draw_overlay });

        if let Some(instr) = instruction {
            payload["instruction"] = json!(instr);
        }
        if let Some(model) = model_name {
            payload["modelName"] = json!(model);
        }
        if let Some(timeout) = dom_settle_timeout_ms {
            payload["domSettleTimeoutMs"] = json!(timeout);
        }
        if let Some(ret_action) = return_action {
            payload["returnAction"] = json!(ret_action);
        }

        let value = self
            .client()
            .post("/session/{sessionId}/agent/observe", &payload)
            .await?;

        Ok(serde_json::from_value(value)?)
    }

    /// Extracts structured data from the page using AI
    pub async fn extract(
        &self,
        instruction: &str,
        schema: Option<&HashMap<String, Value>>,
        selector: Option<&str>,
        model_name: Option<&str>,
        dom_settle_timeout_ms: Option<u64>,
    ) -> Result<ExtractionResult> {
        let mut payload = json!({ "instruction": instruction });

        // Convert schema format
        if let Some(schema_map) = schema {
            let schema_dto = json!({
                "type": "object",
                "properties": schema_map,
                "required": schema_map.keys().collect::<Vec<_>>()
            });
            payload["schema"] = schema_dto;
        }

        if let Some(sel) = selector {
            payload["selector"] = json!(sel);
        }
        if let Some(model) = model_name {
            payload["modelName"] = json!(model);
        }
        if let Some(timeout) = dom_settle_timeout_ms {
            payload["domSettleTimeoutMs"] = json!(timeout);
        }

        let value = self
            .client()
            .post("/session/{sessionId}/agent/extract", &payload)
            .await?;

        Ok(serde_json::from_value(value)?)
    }

    /// Summarizes page content
    pub async fn summarize(
        &self,
        instruction: Option<&str>,
        selector: Option<&str>,
    ) -> Result<String> {
        let mut payload = json!({});

        if let Some(instr) = instruction {
            payload["instruction"] = json!(instr);
        }
        if let Some(sel) = selector {
            payload["selector"] = json!(sel);
        }

        let value = self
            .client()
            .post("/session/{sessionId}/agent/summarize", &payload)
            .await?;

        let summary = if let Some(obj) = value.as_object() {
            obj.get("summary")
                .or_else(|| obj.get("value"))
                .and_then(|v| v.as_str())
                .unwrap_or("")
                .to_string()
        } else if let Some(s) = value.as_str() {
            s.to_string()
        } else {
            String::new()
        };

        Ok(summary)
    }

    /// Clears the agent's history
    pub async fn clear_history(&mut self) -> Result<bool> {
        let value = self
            .client()
            .post("/session/{sessionId}/agent/clearHistory", &json!({}))
            .await?;

        self.process_trace.clear();
        self.state_history.clear();

        Ok(value.as_bool().unwrap_or(true))
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    #[tokio::test]
    async fn test_agentic_session_creation() {
        let client = Arc::new(PulsarClient::with_session_id("test-session"));
        let session = AgenticSession::new(client);

        assert_eq!(session.state_history().states.len(), 0);
        assert_eq!(session.process_trace().len(), 0);
    }

    #[tokio::test]
    async fn test_state_history_tracking() {
        let client = Arc::new(PulsarClient::with_session_id("test-session"));
        let mut session = AgenticSession::new(client);

        // Manually add a state for testing
        session.state_history.push(AgentState {
            step: 1,
            action: "test action".to_string(),
            result: None,
            success: true,
            message: "success".to_string(),
        });

        let history = session.state_history();
        assert_eq!(history.states.len(), 1);
        assert!(!history.has_errors);
    }
}
