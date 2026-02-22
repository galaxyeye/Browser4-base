#!/usr/bin/env bash

# rename.sh - Generates a descriptive filename for a task file using GitHub Copilot
# Usage: ./rename.sh <file_path>

file="$1"

if [[ -z "$file" ]]; then
    echo "Usage: $0 <file_path>"
    exit 1
fi

if [[ ! -f "$file" ]]; then
    echo "Error: File not found: $file"
    exit 1
fi

# Read file content
content=$(cat "$file")

# Initialize variables
title=""
description=""
prompt=""

# Try to parse structured content
if [[ $content =~ ^Title:[[:space:]]*([^$'\n']+)$'\n'Description:[[:space:]]*([^$'\n']+)$'\n'Prompt:[[:space:]]*(.*)$ ]]; then
    title="${BASH_REMATCH[1]}"
    description="${BASH_REMATCH[2]}"
    prompt="${BASH_REMATCH[3]}"
else
    title="$(basename "$file" | sed 's/\.[^.]*$//')"
    description="Task from $(basename "$file")"
    prompt="$content"
fi

# Truncate prompt if too long for the naming request
promptSample="${prompt:0:600}"

namingPrompt="Create a short, descriptive task name in English kebab-case (3-6 words max). Output only the name.
Title: $title
Description: $description
Prompt: $promptSample"

# Call gh copilot to generate name
# We match the syntax used in coworker.sh: gh copilot -p "..."
generatedName=$(gh copilot -p "$namingPrompt" --allow-all-tools --allow-all-paths 2>/dev/null)


# Clean up the generated name
# Take the last line or look for the name if there's extra text.
# The prompt asks for "Output only the name".
# But sometimes LLMs are chatty.

# Filter to get just the kebab-case line
cleanedName=$(echo "$generatedName" | grep -E "^[a-z0-9]+(-[a-z0-9]+)*$" | head -n 1)

if [[ -z "$cleanedName" ]]; then
    # Fallback to simple cleaning of the output if regex didn't match perfectly
    # Take the first non-empty line
    firstLine=$(echo "$generatedName" | grep -v '^[[:space:]]*$' | head -n 1)
    cleanedName=$(echo "$firstLine" | tr -d '\r' | sed 's/[^a-zA-Z0-9-]/-/g' | sed 's/-\{2,\}/-/g' | sed 's/^-//;s/-$//')
fi

# If still empty or failed, fallback to safe title from filename
if [[ -z "$cleanedName" ]]; then
    safeTitle=$(echo "$title" | sed 's/[\/\\*?:"<>|]/_/g')
    echo "$safeTitle"
else
    echo "$cleanedName"
fi
