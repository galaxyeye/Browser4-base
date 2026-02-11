//! # Browser4 Rust SDK
//!
//! Rust SDK for Browser4 based on OpenAPI specification.
//!
//! This SDK provides a Rust interface to the Browser4 browser automation platform,
//! enabling web scraping, data extraction, and AI-powered browser interaction.
//!
//! ## Features
//!
//! - **Session Management**: Create, manage, and delete browser sessions
//! - **Navigation**: Navigate to URLs, go back/forward, reload pages
//! - **Element Interaction**: Click, fill, type, press keys, hover, focus
//! - **Scrolling**: Scroll down/up, scroll to elements, scroll to top/bottom
//! - **Content Extraction**: Extract text, attributes, and HTML content
//! - **Screenshots**: Capture screenshots of pages or elements
//! - **Script Execution**: Execute JavaScript in the browser
//! - **AI-Powered Automation**: Natural language commands for browser interaction
//!
//! ## Quick Start
//!
//! ```no_run
//! use browser4::prelude::*;
//! use std::sync::Arc;
//! use std::collections::HashMap;
//!
//! #[tokio::main]
//! async fn main() -> Result<(), Box<dyn std::error::Error>> {
//!     // Create client and session
//!     let client = Arc::new(PulsarClient::new());
//!     client.create_session().await?;
//!
//!     let mut session = PulsarSession::new(client.clone());
//!
//!     // Load a page
//!     let page = session.load("https://example.com", Some("-expire 1d")).await?;
//!     println!("Loaded page: {}", page.url);
//!
//!     // Parse and extract data
//!     if let Some(document) = session.parse(&page) {
//!         let mut selectors = HashMap::new();
//!         selectors.insert("title".to_string(), "h1".to_string());
//!         let fields = session.extract(&document, &selectors);
//!         println!("Title: {:?}", fields.get("title"));
//!     }
//!
//!     // Use WebDriver
//!     let mut driver = session.get_or_create_driver();
//!     let title = driver.title().await?;
//!     println!("Page title: {}", title);
//!
//!     Ok(())
//! }
//! ```
//!
//! ## AI-Powered Automation
//!
//! ```no_run
//! use browser4::prelude::*;
//! use std::sync::Arc;
//!
//! #[tokio::main]
//! async fn main() -> Result<(), Box<dyn std::error::Error>> {
//!     let client = Arc::new(PulsarClient::new());
//!     client.create_session().await?;
//!
//!     let mut session = AgenticSession::new(client);
//!
//!     // Use natural language to interact
//!     let result = session.act("click the search button", false, None, None, None, None).await?;
//!     println!("Action success: {}", result.success);
//!
//!     // Run autonomous task
//!     let run_result = session.run("search for 'rust' and click first result", false, None, None, None, None).await?;
//!     println!("Task success: {}", run_result.success);
//!
//!     Ok(())
//! }
//! ```

pub mod agentic_session;
pub mod client;
pub mod error;
pub mod models;
pub mod session;
pub mod webdriver;

/// Prelude module for convenient imports
pub mod prelude {
    pub use crate::agentic_session::AgenticSession;
    pub use crate::client::{PulsarClient, PulsarClientBuilder};
    pub use crate::error::{Browser4Error, Result};
    pub use crate::models::*;
    pub use crate::session::PulsarSession;
    pub use crate::webdriver::WebDriver;
}

// Re-export main types at crate level
pub use agentic_session::AgenticSession;
pub use client::PulsarClient;
pub use error::{Browser4Error, Result};
pub use models::*;
pub use session::PulsarSession;
pub use webdriver::WebDriver;
