use browser4::prelude::*;
use std::collections::HashMap;
use std::sync::Arc;

#[tokio::main]
async fn main() -> std::result::Result<(), Box<dyn std::error::Error>> {
    let client = Arc::new(PulsarClient::new());
    client.create_session().await?;
    let mut driver = WebDriver::new(client.clone());

    // Navigation
    driver.navigate_to("https://example.com").await?;
    println!("URL: {}", driver.current_url().await?);
    println!("Title: {}", driver.title().await?);

    // Element interaction
    driver.click("button.submit").await.ok();
    driver.fill("input[name='search']", "rust").await.ok();
    driver.press("input[name='search']", "Enter").await.ok();

    // Wait for elements
    driver.wait_for_selector(".results", None).await.ok();

    // Scrolling
    driver.scroll_to_bottom().await?;

    // Content extraction
    if let Ok(texts) = driver.select_text_all(".result-item").await {
        for text in texts {
            println!("{}", text);
        }
    }

    // Extract multiple fields
    let mut fields = HashMap::new();
    fields.insert("title".to_string(), "h1".to_string());
    fields.insert("description".to_string(), ".description".to_string());
    let extracted = driver.extract(&fields).await?;
    println!("Fields: {:?}", extracted);

    // Execute JavaScript
    let result = driver
        .execute_script("return document.querySelectorAll('a').length", None)
        .await?;
    println!("Links count: {:?}", result);

    client.delete_session().await?;
    Ok(())
}
