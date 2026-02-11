use browser4::prelude::*;
use std::sync::Arc;

#[tokio::test]
async fn test_driver_creation() {
    let client = Arc::new(PulsarClient::with_session_id("test-session"));
    let driver = WebDriver::new(client);

    assert_eq!(driver.navigate_history().len(), 0);
}
