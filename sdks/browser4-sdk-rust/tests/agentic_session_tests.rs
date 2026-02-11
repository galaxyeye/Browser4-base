use browser4::prelude::*;
use std::sync::Arc;

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
    let session = AgenticSession::new(client);

    let history = session.state_history();
    assert_eq!(history.states.len(), 0);
    assert!(!history.has_errors);
}
