use browser4::prelude::*;

#[test]
fn test_client_creation_with_defaults() {
    let client = PulsarClient::new();
    assert_eq!(client.base_url(), "http://localhost:8182");
}

#[test]
fn test_client_creation_with_custom_base_url() {
    let client = PulsarClient::with_base_url("http://custom-server:9999");
    assert_eq!(client.base_url(), "http://custom-server:9999");
}

#[tokio::test]
async fn test_client_creation_with_initial_session_id() {
    let client = PulsarClient::with_session_id("test-session-123");
    assert_eq!(
        client.session_id().await,
        Some("test-session-123".to_string())
    );
}

#[tokio::test]
async fn test_client_session_id_management() {
    let client = PulsarClient::new();
    assert!(client.session_id().await.is_none());

    client.set_session_id(Some("new-session-id".to_string())).await;
    assert_eq!(
        client.session_id().await,
        Some("new-session-id".to_string())
    );
}

#[test]
fn test_client_builder() {
    let client = PulsarClient::builder()
        .base_url("http://test:8080")
        .session_id("test-session")
        .header("X-Custom-Header", "value")
        .build();

    assert_eq!(client.base_url(), "http://test:8080");
}
