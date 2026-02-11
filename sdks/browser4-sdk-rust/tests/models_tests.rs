use browser4::prelude::*;

#[test]
fn test_web_page_serialization() {
    let page = WebPage {
        url: "https://example.com".to_string(),
        location: Some("https://example.com/page".to_string()),
        content_type: Some("text/html".to_string()),
        content_length: 1024,
        protocol_status: Some("200".to_string()),
        is_nil: false,
        html: Some("<html></html>".to_string()),
    };

    let json_str = serde_json::to_string(&page).unwrap();
    assert!(json_str.contains("example.com"));
}

#[test]
fn test_web_page_deserialization() {
    let json_str = r#"{
        "url": "https://example.com",
        "contentLength": 1024,
        "isNil": false
    }"#;

    let page: WebPage = serde_json::from_str(json_str).unwrap();
    assert_eq!(page.url, "https://example.com");
    assert_eq!(page.content_length, 1024);
    assert!(!page.is_nil);
}

#[test]
fn test_agent_act_result_default() {
    let result = AgentActResult::default();
    assert!(!result.success);
    assert_eq!(result.message, "");
    assert!(!result.is_complete);
}

#[test]
fn test_agent_run_result_default() {
    let result = AgentRunResult::default();
    assert!(!result.success);
    assert_eq!(result.history_size, 0);
    assert_eq!(result.process_trace_size, 0);
}

#[test]
fn test_extraction_result_default() {
    let result = ExtractionResult::default();
    assert!(!result.success);
    assert!(result.data.is_none());
}

#[test]
fn test_agent_history_default() {
    let history = AgentHistory::default();
    assert_eq!(history.states.len(), 0);
    assert!(!history.has_errors);
}
