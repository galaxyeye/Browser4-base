#!/usr/bin/env bash

# Resolve the directory of this script, then get the parent directory as repo root
# This works even if called via symlink or from another directory
SCRIPT_DIR=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
REPO_ROOT=$(cd "$SCRIPT_DIR/.." && pwd)

cd "$REPO_ROOT" || exit 1

# Try to find the JAR file, excluding files with "original" in the name
# We look for files ending in .jar in the target directory
JAR_FILES=$(ls examples/browser4-examples/target/browser4-examples-*.jar 2>/dev/null | grep -v "original")

if [ -z "$JAR_FILES" ]; then
    echo "JAR file not found in examples/browser4-examples/target/"
    echo "Building it now..."
    ./mvnw clean package -pl examples/browser4-examples -DskipTests
    
    JAR_FILES=$(ls examples/browser4-examples/target/browser4-examples-*.jar 2>/dev/null | grep -v "original")
    if [ -z "$JAR_FILES" ]; then
        echo "Build failed or JAR not found."
        exit 1
    fi
fi

# Pick the first one found
JAR_PATH=$(echo "$JAR_FILES" | head -n 1)

echo "Running $JAR_PATH..."
java -Dfile.encoding=UTF-8 -jar "$JAR_PATH"
