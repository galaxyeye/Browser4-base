use browser4::prelude::*;
use std::sync::Arc;

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
async fn test_session_display_with_session() {
    let client = Arc::new(PulsarClient::with_session_id("abcdefgh12345678"));
    let session = PulsarSession::new(client);

    let display = session.display().await;
    assert!(display.contains("abcdefgh"));
}

#[tokio::test]
async fn test_session_display_without_session() {
    let client = Arc::new(PulsarClient::new());
    let session = PulsarSession::new(client);

    assert!(!session.is_active().await);
    let display = session.display().await;
    assert!(display.contains("no-session"));
}

#[tokio::test]
async fn test_driver_lazy_creation() {
    let client = Arc::new(PulsarClient::with_session_id("test-session"));
    let mut session = PulsarSession::new(client);

    // driver should be None initially
    assert!(session.driver().is_none());

    // get_or_create_driver should create it
    let _driver = session.get_or_create_driver();
    assert!(session.driver().is_some());
}
