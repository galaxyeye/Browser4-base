use browser4::prelude::*;
use std::sync::Arc;

#[tokio::main]
async fn main() -> std::result::Result<(), Box<dyn std::error::Error>> {
    let client = Arc::new(PulsarClient::new());
    client.create_session().await?;

    let mut session = AgenticSession::new(client.clone());

    // Navigate to a page (using session's driver)
    let driver = session.session().get_or_create_driver();
    // Note: Arc doesn't support mutable operations, so we can't use driver methods
    // that require &mut self through Arc. For this example, we'll skip navigation.

    // Use natural language to interact
    let act_result = session
        .act("click the search button", false, None, None, None, None)
        .await?;
    println!("Action success: {}", act_result.success);

    // Run autonomous task
    let run_result = session
        .run(
            "search for 'rust' and click first result",
            false,
            None,
            None,
            None,
            None,
        )
        .await?;
    println!("Task success: {}", run_result.success);

    // Observe page state
    let observation = session
        .observe(Some("find all interactive elements"), None, None, None, true)
        .await?;
    for obs in observation.observations {
        if let Some(desc) = obs.description {
            println!("Found: {}", desc);
        }
    }

    // AI-powered extraction
    let extraction = session
        .extract(
            "extract the main heading and first paragraph",
            None,
            None,
            None,
            None,
        )
        .await?;
    println!("Extracted data: {:?}", extraction.data);

    // Summarize page content
    let summary = session.summarize(None, None).await?;
    println!("Summary: {}", summary);

    client.delete_session().await?;
    Ok(())
}
