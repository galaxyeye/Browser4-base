#!/bin/bash

script_dir=$(cd "$(dirname "$0")" > /dev/null || exit 1; pwd)
repo_root=$(git -C "$script_dir" rev-parse --show-toplevel 2>/dev/null)

if [[ -z "$repo_root" ]]; then
  repo_root="$script_dir"
  while [[ ! -f "$repo_root/VERSION" && "$repo_root" != "/" ]]; do
    repo_root=$(dirname "$repo_root")
  done
fi

if [[ ! -f "$repo_root/VERSION" ]]; then
  echo "Error: Could not locate the repository root from $script_dir" >&2
  exit 1
fi

cd "$repo_root" || exit 1

print_usage() {
  echo "Usage: test.sh [test-types...] [maven-args...]"
  echo ""
  echo "Test Types:"
  echo "  fast        Run fast unit tests only"
  echo "  it          Run integration tests"
  echo "  e2e         Run end-to-end tests"
  echo "  nodejs-sdk  Run Browser4 CLI tests from sdks/browser4-cli"
  echo "  core        Run core module supplementary tests"
  echo "  rest        Run REST module tests"
  echo "  skills      Run skills-focused agentic tests"
  echo "  mcp         Run MCP-focused agentic tests"
  echo "  browser4    Run all Browser4 main tests (fast, core, rest, it, e2e)"
  echo ""
  echo "Examples:"
  echo "  test.sh fast                       # Run fast unit tests"
  echo "  test.sh it                         # Run integration tests"
  echo "  test.sh e2e                        # Run end-to-end tests"
  echo "  test.sh nodejs-sdk                 # Run Browser4 CLI tests"
  echo "  test.sh nodejs-sdk -- --coverage   # Run Browser4 CLI tests with coverage"
  echo "  test.sh skills                     # Run skills-focused agentic tests"
  echo "  test.sh mcp                        # Run MCP-focused agentic tests"
  echo "  test.sh browser4                   # Run all Browser4 main tests"
  echo "  test.sh it -pl pulsar-core         # Pass additional Maven args through"
  exit 1
}

exit_removed_test_type() {
  local test_type=$1
  echo "Error: Test type '$test_type' is no longer available in this checkout because the corresponding module was removed. Use 'nodejs-sdk' for sdks/browser4-cli tests." >&2
  exit 1
}

run_maven_tests() {
  local -a test_types=("$@")
  local -a mvn_test_args=("test" "-P=-examples")
  local -a modules=()
  local -a test_patterns=()
  local joined_modules=""
  local joined_patterns=""
  local has_fast=false
  local has_it=false
  local has_e2e=false
  local has_core=false
  local has_rest=false
  local has_skills=false
  local has_mcp=false

  echo "=========================================="
  echo "Running Maven tests: ${test_types[*]}"
  echo "=========================================="

  for type in "${test_types[@]}"; do
    case "$type" in
      fast) has_fast=true ;;
      it) has_it=true ;;
      e2e) has_e2e=true ;;
      core) has_core=true ;;
      rest) has_rest=true ;;
      skills) has_skills=true ;;
      mcp) has_mcp=true ;;
    esac
  done

  [[ "$has_it" == "true" ]] && mvn_test_args+=("-DrunITs=true")
  [[ "$has_e2e" == "true" ]] && mvn_test_args+=("-DrunE2ETests=true")
  if [[ "$has_core" == "true" ]]; then
    mvn_test_args+=("-DrunCoreTests=true" "-Ppulsar-core-tests")
    modules+=(
      "pulsar-core/pulsar-resources"
      "pulsar-core/pulsar-common"
      "pulsar-core/pulsar-dom"
      "pulsar-core/pulsar-persist"
      "pulsar-core/pulsar-plugins"
      "pulsar-core/pulsar-third"
      "pulsar-core/pulsar-skeleton"
      "pulsar-core/pulsar-browser"
      "pulsar-core/pulsar-spring-support"
      "pulsar-core/pulsar-ql-common"
      "pulsar-core/pulsar-ql"
      "pulsar-core/pulsar-core-tests"
      "pulsar-core/pulsar-core-tests/pulsar-common-tests"
      "pulsar-core/pulsar-core-tests/pulsar-dom-tests"
      "pulsar-core/pulsar-core-tests/pulsar-ql-tests"
    )
  fi

  if [[ "$has_skills" == "true" || "$has_mcp" == "true" ]]; then
    modules+=("pulsar-agentic")

    if [[ "$has_fast" == "false" && "$has_it" == "false" && "$has_e2e" == "false" && "$has_core" == "false" && "$has_rest" == "false" ]]; then
      [[ "$has_skills" == "true" ]] && test_patterns+=("*Skill*")
      [[ "$has_mcp" == "true" ]] && test_patterns+=("*MCP*")

      if [[ ${#test_patterns[@]} -gt 0 ]]; then
        joined_patterns=$(IFS=, ; echo "${test_patterns[*]}")
        mvn_test_args+=("-Dtest=$joined_patterns" "-Dsurefire.failIfNoSpecifiedTests=false")
      fi
    fi
  fi

  if [[ "$has_fast" == "true" || "$has_rest" == "true" ]]; then
    modules=()
  fi

  if [[ ${#modules[@]} -gt 0 ]]; then
    joined_modules=$(IFS=, ; echo "${modules[*]}")
    mvn_test_args+=("-pl" "$joined_modules" "-am")
  fi

  mvn_test_args+=("${AdditionalMvnArgs[@]}")

  ./mvnw "${mvn_test_args[@]}"
  local exit_code=$?
  if [[ $exit_code -ne 0 ]]; then
    echo ""
    echo "=========================================="
    echo "❌ Maven tests failed with exit code $exit_code"
    echo "=========================================="
    exit $exit_code
  fi

  echo ""
  echo "=========================================="
  echo "✅ Maven tests completed successfully"
  echo "=========================================="
}

run_nodejs_sdk_tests() {
  local nodejs_sdk_dir="$repo_root/sdks/browser4-cli"

  echo "=========================================="
  echo "Running nodejs-sdk tests..."
  echo "=========================================="

  if [[ ! -d "$nodejs_sdk_dir" ]]; then
    echo "Error: Browser4 CLI directory not found at $nodejs_sdk_dir" >&2
    exit 1
  fi

  if ! command -v node >/dev/null 2>&1; then
    echo "Error: node is not installed or not in PATH" >&2
    exit 1
  fi

  pushd "$nodejs_sdk_dir" > /dev/null || exit 1
  echo "Working directory: $(pwd)"

  if [[ ! -d "$nodejs_sdk_dir/node_modules" ]]; then
    echo "Installing dependencies..."
    npm install
    if [[ $? -ne 0 ]]; then
      echo "Error: Failed to install dependencies" >&2
      popd > /dev/null || true
      exit 1
    fi
  fi

  if [[ ! -f "$nodejs_sdk_dir/node_modules/.bin/jest" ]]; then
    echo "Error: jest is not installed. Install it with: npm install" >&2
    popd > /dev/null || true
    exit 1
  fi

  npm test -- "${AdditionalMvnArgs[@]}"
  local exit_code=$?
  popd > /dev/null || true

  if [[ $exit_code -ne 0 ]]; then
    echo ""
    echo "=========================================="
    echo "❌ nodejs-sdk tests failed with exit code $exit_code"
    echo "=========================================="
    exit $exit_code
  fi

  echo ""
  echo "=========================================="
  echo "✅ nodejs-sdk tests completed successfully"
  echo "=========================================="
}

KnownTestTypes=(fast it e2e nodejs-sdk core rest skills mcp browser4)
RemovedTestTypes=(kotlin-sdk python-sdk)
TestTypes=()
MavenTests=()
SDKTests=()
AdditionalMvnArgs=()
ParsingTestTypes=true

if [[ $# -eq 0 ]]; then
  print_usage
fi

while [[ $# -gt 0 ]]; do
  case "$1" in
    -h|-help|--help)
      print_usage
      ;;
    kotlin-sdk|python-sdk)
      exit_removed_test_type "$1"
      ;;
    fast|it|e2e|nodejs-sdk|core|rest|skills|mcp|browser4)
      if [[ "$ParsingTestTypes" == "true" ]]; then
        TestTypes+=("$1")
      else
        AdditionalMvnArgs+=("$1")
      fi
      ;;
    *)
      ParsingTestTypes=false
      AdditionalMvnArgs+=("$1")
      ;;
  esac
  shift
done

if [[ ${#TestTypes[@]} -eq 0 ]]; then
  TestTypes=(fast)
fi

for type in "${TestTypes[@]}"; do
  if [[ "$type" == "browser4" ]]; then
    MavenTests+=(fast core it e2e rest)
  elif [[ "$type" == "nodejs-sdk" ]]; then
    SDKTests+=("$type")
  else
    MavenTests+=("$type")
  fi
done

UniqueMavenTests=()
for type in "${MavenTests[@]}"; do
  found=false
  for known in "${UniqueMavenTests[@]}"; do
    if [[ "$known" == "$type" ]]; then
      found=true
      break
    fi
  done

  if [[ "$found" == "false" ]]; then
    UniqueMavenTests+=("$type")
  fi
done
MavenTests=("${UniqueMavenTests[@]}")

UniqueSDKTests=()
for type in "${SDKTests[@]}"; do
  found=false
  for known in "${UniqueSDKTests[@]}"; do
    if [[ "$known" == "$type" ]]; then
      found=true
      break
    fi
  done

  if [[ "$found" == "false" ]]; then
    UniqueSDKTests+=("$type")
  fi
done
SDKTests=("${UniqueSDKTests[@]}")

if [[ ${#MavenTests[@]} -gt 0 ]]; then
  run_maven_tests "${MavenTests[@]}"
fi

for test_type in "${SDKTests[@]}"; do
  case "$test_type" in
    nodejs-sdk)
      run_nodejs_sdk_tests
      ;;
  esac
done

exit 0
