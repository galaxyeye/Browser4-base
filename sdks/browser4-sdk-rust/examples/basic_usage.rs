use browser4::prelude::*;
use std::collections::HashMap;
use std::sync::Arc;

#[tokio::main]
async fn main() -> std::result::Result<(), Box<dyn std::error::Error>> {
    // Create client and session
    let client = Arc::new(PulsarClient::new());
    client.create_session().await?;

    let session = PulsarSession::new(client.clone());

    // Load a page
    let page = session.load("https://example.com", Some("-expire 1d")).await?;
    println!("Loaded page: {}", page.url);

    // Parse and extract data
    if let Some(document) = session.parse(&page) {
        let mut selectors = HashMap::new();
        selectors.insert("title".to_string(), "h1".to_string());
        let fields = session.extract(&document, &selectors);
        println!("Title: {:?}", fields.get("title"));
    }

    // Clean up
    client.delete_session().await?;

    Ok(())
}
