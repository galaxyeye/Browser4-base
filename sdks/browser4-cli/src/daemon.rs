//! Server daemon management for the Browser4 CLI.
//!
//! Ensures a Browser4 server is running before executing commands.
//! Only manages localhost instances; remote servers are not touched.

use std::fs;
use std::path::PathBuf;
use std::process::{Command, Stdio};
use std::time::{Duration, Instant};

use reqwest::Client;

use crate::managed_processes::{register_managed_server_process, ManagedServerProcess};
use crate::state::read_state;

/// Ensure the Browser4 server is running, starting it if necessary.
///
/// Only acts on `localhost` / `127.0.0.1` URLs.
pub async fn ensure_server_running(base_url: &str) -> Result<(), String> {
    // Skip remote servers
    if !base_url.contains("localhost") && !base_url.contains("127.0.0.1") {
        return Ok(());
    }

    let client = Client::builder()
        .timeout(std::time::Duration::from_secs(5))
        .build()
        .map_err(|e| e.to_string())?;

    match probe_server_state(&client, base_url).await {
        ServerState::Ready => return Ok(()),
        ServerState::Starting(_) => {
            return wait_for_server_ready(&client, base_url, Duration::from_secs(60)).await;
        }
        ServerState::Unreachable(_) => {}
    }

    eprintln!("Browser4 server not running. Starting...");

    let jar_path = find_or_download_jar().await?;
    eprintln!("Jar path: {}", jar_path.display());

    let port = extract_port(base_url);
    start_server(&jar_path, base_url, port).await
}

fn extract_port(base_url: &str) -> u16 {
    if let Ok(url) = reqwest::Url::parse(base_url) {
        url.port().unwrap_or(8182)
    } else {
        8182
    }
}

async fn find_or_download_jar() -> Result<PathBuf, String> {
    // Check environment variable
    if let Ok(env_path) = std::env::var("BROWSER4_JAR_PATH") {
        let p = PathBuf::from(&env_path);
        if p.exists() {
            return Ok(p);
        }
    }

    // Common candidate locations
    let home = dirs::home_dir().unwrap_or_else(|| PathBuf::from("."));
    let candidates = vec![
        PathBuf::from("../../browser4/browser4-agents/target/Browser4.jar"),
        PathBuf::from("browser4/browser4-agents/target/Browser4.jar"),
        PathBuf::from("target/Browser4.jar"),
        home.join(".browser4").join("lib").join("Browser4.jar"),
    ];

    for candidate in candidates {
        if candidate.exists() {
            return Ok(candidate);
        }
    }

    // Download if not found
    let download_path = home.join(".browser4").join("lib").join("Browser4.jar");
    download_jar(&download_path).await?;
    Ok(download_path)
}

async fn download_jar(target_path: &PathBuf) -> Result<(), String> {
    if let Some(dir) = target_path.parent() {
        fs::create_dir_all(dir).map_err(|e| e.to_string())?;
    }

    let url = "https://github.com/platonai/Browser4/releases/latest/download/Browser4.jar";
    eprintln!("Downloading Browser4.jar from {}...", url);

    let client = Client::builder()
        .timeout(std::time::Duration::from_secs(300))
        .build()
        .map_err(|e| e.to_string())?;

    let response = client
        .get(url)
        .send()
        .await
        .map_err(|e| format!("Download failed: {e}"))?;

    if !response.status().is_success() {
        return Err(format!(
            "Download failed with status: {}",
            response.status()
        ));
    }

    let bytes = response.bytes().await.map_err(|e| e.to_string())?;
    fs::write(target_path, &bytes).map_err(|e| e.to_string())?;

    eprintln!("Download complete.");
    Ok(())
}

async fn start_server(jar_path: &PathBuf, base_url: &str, port: u16) -> Result<(), String> {
    eprintln!(
        "Starting server from {} on port {}...",
        jar_path.display(),
        port
    );

    let child = Command::new("java")
        .args([
            "-jar",
            jar_path.to_str().unwrap_or("Browser4.jar"),
            &format!("--server.port={}", port),
        ])
        .stdin(Stdio::null())
        .stdout(Stdio::null())
        .stderr(Stdio::null())
        .spawn()
        .map_err(|e| format!("Failed to start server: {e}"))?;

    let pid = child.id();

    register_managed_server_process(
        ManagedServerProcess {
            pid,
            base_url: base_url.to_string(),
            port,
            jar_path: jar_path.to_string_lossy().to_string(),
            started_at: chrono::Utc::now().to_rfc3339(),
        },
        None,
    );

    // Detach: we drop the Child handle here. The spawned process continues
    // running independently because we set all stdio to null and call drop().
    // On Unix this means the child is still a child of this process until we
    // exit, at which point it is re-parented to init/systemd. That is
    // sufficient for our use-case where the parent CLI exits immediately.
    drop(child);

    let client = Client::builder()
        .timeout(std::time::Duration::from_secs(5))
        .build()
        .map_err(|e| e.to_string())?;

    wait_for_server_ready(&client, base_url, Duration::from_secs(60)).await?;
    eprintln!("Server is up and running.");
    Ok(())
}

/// Resolve the base URL from CLI state + optional server override arg.
pub fn resolve_base_url(override_url: Option<&str>, session_name: Option<&str>) -> String {
    let state = read_state(None, session_name);
    let base = override_url
        .map(|s| s.to_string())
        .unwrap_or(state.base_url);
    base.trim_end_matches('/').to_string()
}

enum ServerState {
    Ready,
    Starting(String),
    Unreachable(String),
}

async fn probe_server_state(client: &Client, base_url: &str) -> ServerState {
    let trimmed = base_url.trim_end_matches('/');
    let health_url = format!("{trimmed}/actuator/health");
    let tools_url = format!("{trimmed}/mcp/tools");

    let health_response = match client.get(&health_url).send().await {
        Ok(response) => response,
        Err(error) => return ServerState::Unreachable(error.to_string()),
    };
    let health_body = match health_response.text().await {
        Ok(body) => body,
        Err(error) => return ServerState::Starting(error.to_string()),
    };
    if !health_body.contains("\"status\":\"UP\"") {
        return ServerState::Starting(health_body);
    }

    let tools_response = match client.get(&tools_url).send().await {
        Ok(response) => response,
        Err(error) => return ServerState::Starting(error.to_string()),
    };
    let tools_body = match tools_response.text().await {
        Ok(body) => body,
        Err(error) => return ServerState::Starting(error.to_string()),
    };
    if tools_body.contains("open_session") && tools_body.contains("browser_navigate") {
        ServerState::Ready
    } else {
        ServerState::Starting(format!("MCP tools endpoint not ready: {tools_body}"))
    }
}

async fn wait_for_server_ready(
    client: &Client,
    base_url: &str,
    timeout: Duration,
) -> Result<(), String> {
    let start = Instant::now();
    let mut last_error = String::from("unknown");

    while start.elapsed() <= timeout {
        match probe_server_state(client, base_url).await {
            ServerState::Ready => return Ok(()),
            ServerState::Starting(error) | ServerState::Unreachable(error) => {
                last_error = error;
            }
        }
        tokio::time::sleep(Duration::from_secs(1)).await;
    }

    Err(format!(
        "Server failed to become MCP-ready within {}s: {}",
        timeout.as_secs(),
        last_error
    ))
}
