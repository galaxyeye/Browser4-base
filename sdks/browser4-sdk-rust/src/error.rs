use thiserror::Error;

/// Browser4 SDK error types
#[derive(Error, Debug)]
pub enum Browser4Error {
    #[error("No active session")]
    NoSession,

    #[error("HTTP error {0}: {1}")]
    HttpError(u16, String),

    #[error("Invalid response: {0}")]
    InvalidResponse(String),

    #[error("Serialization error: {0}")]
    SerializationError(#[from] serde_json::Error),

    #[error("HTTP request error: {0}")]
    RequestError(#[from] reqwest::Error),

    #[error("IO error: {0}")]
    IoError(#[from] std::io::Error),

    #[error("Parse error: {0}")]
    ParseError(String),

    #[error("Timeout error")]
    Timeout,

    #[error("Element not found: {0}")]
    ElementNotFound(String),

    #[error("Invalid selector: {0}")]
    InvalidSelector(String),

    #[error("{0}")]
    Other(String),
}

/// Result type for Browser4 SDK operations
pub type Result<T> = std::result::Result<T, Browser4Error>;
