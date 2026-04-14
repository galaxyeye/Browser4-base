#!/usr/bin/env bash
# Browser4 CLI Installer
# Installs Java, Chrome, Rust, and the browser4-cli tool.

set -euo pipefail

LABEL="[browser4-cli installer]"
REPO="platonai/Browser4"
INSTALL_DIR="${INSTALL_DIR:-$HOME/.local/bin}"
JAR_DIR="${JAR_DIR:-$HOME/.browser4}"
DEFAULT_VERSION="v4.6.0"

log()  { echo "$LABEL $*"; }
warn() { echo "$LABEL WARNING: $*" >&2; }
die()  { echo "$LABEL ERROR: $*" >&2; exit 1; }

# ---------------------------------------------------------------------------
# Java
# ---------------------------------------------------------------------------
install_java() {
    log "Installing Java 21..."
    if command -v apt-get &>/dev/null; then
        sudo apt-get update -q && sudo apt-get install -y openjdk-21-jdk-headless
    elif command -v brew &>/dev/null; then
        brew install openjdk@21
    else
        die "Cannot install Java automatically. Please install Java 21+ and re-run."
    fi
}

check_java() {
    if command -v java &>/dev/null; then
        JAVA_VER=$(java -version 2>&1 | awk -F '"' '/version/{print $2}' | cut -d. -f1)
        if [ "${JAVA_VER:-0}" -ge 17 ] 2>/dev/null; then
            log "Java ${JAVA_VER} is already installed."
            return 0
        fi
    fi
    install_java
}

# ---------------------------------------------------------------------------
# Google Chrome
# ---------------------------------------------------------------------------
install_chrome() {
    log "Installing Google Chrome..."
    if command -v apt-get &>/dev/null; then
        TMP=$(mktemp -d)
        wget -q -O "$TMP/chrome.deb" \
            "https://dl.google.com/linux/direct/google-chrome-stable_current_amd64.deb"
        sudo apt-get install -y "$TMP/chrome.deb" || sudo dpkg -i "$TMP/chrome.deb" || true
        sudo apt-get install -f -y
        rm -rf "$TMP"
    elif command -v brew &>/dev/null; then
        brew install --cask google-chrome
    else
        warn "Cannot install Chrome automatically. Please install Google Chrome and re-run."
    fi
}

check_chrome() {
    if command -v google-chrome &>/dev/null || command -v google-chrome-stable &>/dev/null; then
        log "Google Chrome is already installed."
    else
        install_chrome
    fi
}

# ---------------------------------------------------------------------------
# Rust toolchain
# ---------------------------------------------------------------------------
install_rust() {
    log "Installing Rust toolchain..."
    curl --proto '=https' --tlsv1.2 -sSf https://sh.rustup.rs | sh -s -- -y --no-modify-path
    # shellcheck source=/dev/null
    source "$HOME/.cargo/env"
}

check_rust() {
    if command -v cargo &>/dev/null; then
        log "Rust toolchain already installed."
    else
        install_rust
    fi
    # Ensure cargo is on PATH for the rest of this script.
    if [ -f "$HOME/.cargo/env" ]; then
        # shellcheck source=/dev/null
        source "$HOME/.cargo/env"
    fi
}

# ---------------------------------------------------------------------------
# Determine the latest Browser4 release tag
# ---------------------------------------------------------------------------
get_latest_release_tag() {
    local tag=""

    # Method 1: GitHub REST API
    if command -v curl &>/dev/null; then
        tag=$(curl -sSf \
            -H "Accept: application/vnd.github.v3+json" \
            "https://api.github.com/repos/${REPO}/releases/latest" 2>/dev/null \
            | grep '"tag_name"' \
            | sed -E 's/.*"tag_name"[[:space:]]*:[[:space:]]*"([^"]+)".*/\1/')
    fi

    # Method 2: Follow the /releases/latest redirect
    if [ -z "$tag" ] && command -v curl &>/dev/null; then
        tag=$(curl -sSfL -o /dev/null -w '%{url_effective}' \
            "https://github.com/${REPO}/releases/latest" 2>/dev/null \
            | grep -o 'v[0-9]\+\.[0-9]\+\.[0-9]\+[^/]*$' || true)
    fi

    # Method 3: wget fallback
    if [ -z "$tag" ] && command -v wget &>/dev/null; then
        tag=$(wget -qO- \
            "https://api.github.com/repos/${REPO}/releases/latest" 2>/dev/null \
            | grep '"tag_name"' \
            | sed -E 's/.*"tag_name"[[:space:]]*:[[:space:]]*"([^"]+)".*/\1/' || true)
    fi

    # Fallback: use known default
    if [ -z "$tag" ]; then
        warn "Could not fetch the latest release tag; falling back to ${DEFAULT_VERSION}."
        tag="$DEFAULT_VERSION"
    fi

    echo "$tag"
}

# ---------------------------------------------------------------------------
# Download Browser4.jar
# ---------------------------------------------------------------------------
download_jar() {
    local tag="$1"
    local url="https://github.com/${REPO}/releases/download/${tag}/Browser4.jar"
    local dest="${JAR_DIR}/Browser4.jar"

    mkdir -p "$JAR_DIR"

    if [ -f "$dest" ]; then
        log "Browser4.jar already present at ${dest}. Skipping download."
        return
    fi

    log "Downloading Browser4.jar (${tag}) to ${dest} ..."
    if command -v curl &>/dev/null; then
        curl -fSL --progress-bar -o "$dest" "$url"
    else
        wget -q --show-progress -O "$dest" "$url"
    fi
    log "Download complete."
}

# ---------------------------------------------------------------------------
# Build and install browser4-cli
# ---------------------------------------------------------------------------
build_cli() {
    local script_dir
    script_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

    log "Building browser4-cli from source..."
    (cd "$script_dir" && cargo build --release --quiet)

    mkdir -p "$INSTALL_DIR"
    cp "$script_dir/target/release/browser4-cli" "$INSTALL_DIR/browser4-cli"
    log "Installed browser4-cli to ${INSTALL_DIR}/browser4-cli"

    if [[ ":$PATH:" != *":${INSTALL_DIR}:"* ]]; then
        warn "${INSTALL_DIR} is not in PATH. Add the following to your shell profile:"
        warn "  export PATH=\"${INSTALL_DIR}:\$PATH\""
    fi
}

# ---------------------------------------------------------------------------
# Main
# ---------------------------------------------------------------------------
main() {
    log "Starting installation..."

    check_java
    check_chrome
    check_rust

    local tag
    tag=$(get_latest_release_tag)
    log "Using release: ${tag}"

    download_jar "$tag"
    build_cli

    log "Installation complete."
    log "Run 'browser4-cli --help' to get started."
}

main "$@"
