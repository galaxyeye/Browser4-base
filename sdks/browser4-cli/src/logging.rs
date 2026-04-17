//! Logging initialisation for the Browser4 CLI.
//!
//! The logger is configured via environment variables using standard
//! `env_logger` conventions.  When no variable is set the default filter
//! is `warn`, which means only warnings and errors are shown.
//!
//! # Configuration
//!
//! The log level is resolved in the following order:
//! 1. `RUST_LOG` — standard `env_logger` filter syntax (e.g. `browser4_cli=debug`).
//! 2. `BROWSER4_LOG` — simple level name as a project-specific convenience
//!    (e.g. `debug`, `info`, `warn`, `error`).
//! 3. Built-in default: `warn`.
//!
//! # Examples
//!
//! ```bash
//! # Show debug output for browser4-cli only
//! RUST_LOG=browser4_cli=debug browser4-cli snapshot
//!
//! # Use the convenience variable for a simple level
//! BROWSER4_LOG=info browser4-cli open https://example.com
//! ```
//!
//! In code, initialise once at application startup:
//!
//! ```
//! use browser4_cli::logging::init_logger;
//!
//! init_logger();
//! ```

use env_logger::Builder;
use log::LevelFilter;

/// Initialise the global logger.
///
/// Must be called **once** at process startup before any logging macros are
/// used.  Subsequent calls are silently ignored.
///
/// The log level is resolved in the following order:
/// 1. `RUST_LOG` environment variable (full `env_logger` filter syntax).
/// 2. `BROWSER4_LOG` environment variable (simple level name such as `debug`).
/// 3. Built-in default: `warn`.
pub fn init_logger() {
    let mut builder = Builder::new();

    // Apply RUST_LOG if present (env_logger native variable).
    if let Ok(rust_log) = std::env::var("RUST_LOG") {
        if !rust_log.trim().is_empty() {
            builder.parse_filters(&rust_log);
            let _ = builder.try_init();
            return;
        }
    }

    // Apply BROWSER4_LOG if present (project-specific convenience variable).
    if let Ok(b4_log) = std::env::var("BROWSER4_LOG") {
        if let Ok(level) = b4_log.trim().parse::<LevelFilter>() {
            builder.filter_level(level);
            let _ = builder.try_init();
            return;
        }
    }

    // Default: warn and above.
    builder.filter_level(LevelFilter::Warn);
    let _ = builder.try_init();
}

#[cfg(test)]
mod tests {
    use super::*;

    /// Calling init_logger multiple times must not panic.
    #[test]
    fn test_init_logger_idempotent() {
        init_logger();
        init_logger();
    }

    /// BROWSER4_LOG with an invalid level string falls back to the default
    /// without panicking.
    #[test]
    fn test_init_logger_invalid_browser4_log_falls_back() {
        // Temporarily set an invalid value; the function must not panic.
        std::env::set_var("BROWSER4_LOG", "not_a_valid_level");
        init_logger();
        std::env::remove_var("BROWSER4_LOG");
    }
}
