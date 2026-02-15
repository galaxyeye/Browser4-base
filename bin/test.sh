#!/bin/bash

# Find the first parent directory that contains a VERSION file
APP_HOME=$(cd "$(dirname "$0")">/dev/null || exit 1; pwd)
while [[ ! -f "$APP_HOME/VERSION" && "$APP_HOME" != "/" ]]; do
  APP_HOME=$(dirname "$APP_HOME")
done
[[ -f "$APP_HOME/VERSION" ]] && cd "$APP_HOME" || exit 1

function print_usage {
  echo "Usage: test.sh [test-types...] [maven-args...]"
  echo ""
  echo "Test Types:"
  echo "  fast        Run fast unit tests only"
  echo "  it          Run integration tests"
  echo "  e2e         Run end-to-end tests"
  echo "  kotlin-sdk  Run Kotlin SDK tests"
  echo "  python-sdk  Run Python SDK tests"
  echo "  nodejs-sdk  Run NodeJS SDK tests"
  echo "  core        Run core module supplementary tests"
  echo "  rest        Run REST module tests"
  echo "  all         Run all tests (fast, core, it, e2e, rest)"
  echo ""
  echo "Examples:"
  echo "  test.sh fast                       # Run fast unit tests"
  echo "  test.sh it                         # Run integration tests"
  echo "  test.sh e2e                        # Run end-to-end tests"
  echo "  test.sh kotlin-sdk                 # Run Kotlin SDK tests"
  echo "  test.sh it kotlin-sdk              # Run integration and Kotlin SDK tests"
  echo "  test.sh python-sdk                 # Run Python SDK tests"
  echo "  test.sh python-sdk -m integration  # Run Python SDK integration tests only"
  echo "  test.sh nodejs-sdk                 # Run NodeJS SDK tests"
  echo "  test.sh nodejs-sdk --coverage      # Run NodeJS SDK tests with coverage"
  echo "  test.sh all                        # Run all tests"
  echo "  test.sh it -pl pulsar-core         # Run integration tests for pulsar-core only"
  exit 1
}

# Maven command
MvnCmd="./mvnw"

# Validate Maven wrapper exists and is executable
if [[ ! -x "$APP_HOME/mvnw" ]]; then
    echo "Error: Maven wrapper not found or not executable at $APP_HOME/mvnw"
    exit 1
fi

# Default test type is fast if no args provided
TestTypes=()
AdditionalMvnArgs=()

# Parse command-line arguments
if [[ $# -eq 0 ]]; then
  print_usage
fi

ParsingTestTypes=true
while [[ $# -gt 0 ]]; do
  case $1 in
    fast|it|e2e|kotlin-sdk|python-sdk|nodejs-sdk|core|rest|all)
      if [[ "$ParsingTestTypes" == "true" ]]; then
          TestTypes+=("$1")
      else
        AdditionalMvnArgs+=("$1")
      fi
      ;;
    -h|-help|--help)
      print_usage
      ;;
    *)
      ParsingTestTypes=false
      AdditionalMvnArgs+=("$1")
      ;;
  esac
  shift
done

if [[ ${#TestTypes[@]} -eq 0 ]]; then
  # No test types provided, check if we have any args
  if [[ ${#AdditionalMvnArgs[@]} -eq 0 ]]; then
    # Should have been caught by $# -eq 0 check, but safe fallback
    TestTypes=("fast")
  else
    # Only maven args, assume fast
    TestTypes=("fast")
  fi
fi

# Separate Maven tests from SDK tests
MavenTests=()
SDKTests=()
for type in "${TestTypes[@]}"; do
  if [[ "$type" == "all" ]]; then
    # For 'all', include all Maven tests and skip SDK tests (they're optional)
    MavenTests=("fast" "core" "it" "e2e" "rest")
    break
  elif [[ "$type" == "python-sdk" || "$type" == "nodejs-sdk" || "$type" == "kotlin-sdk" ]]; then
    SDKTests+=("$type")
  else
    # Maven test types
    MavenTests+=("$type")
  fi
done

# Remove duplicates from MavenTests (preserve order)
UniqueMavenTests=()
for type in "${MavenTests[@]}"; do
  found=false
  for u in "${UniqueMavenTests[@]}"; do
    if [[ "$u" == "$type" ]]; then
      found=true
      break
    fi
  done
  if [[ "$found" == "false" ]]; then
    UniqueMavenTests+=("$type")
  fi
done
MavenTests=("${UniqueMavenTests[@]}")

# If we have Maven tests, execute them as a single command
if [[ ${#MavenTests[@]} -gt 0 ]]; then
    echo "=========================================="
    echo "Running Maven tests: ${MavenTests[*]}"
    echo "=========================================="

    # Build Maven command with appropriate flags
    MvnTestArgs=("test")

    # Check which test types are requested
    HasFast=false
    HasIT=false
    HasE2E=false
    HasCore=false
    HasRest=false

    for type in "${MavenTests[@]}"; do
      case $type in
        fast) HasFast=true ;;
        it) HasIT=true ;;
        e2e) HasE2E=true ;;
        core) HasCore=true ;;
        rest) HasRest=true ;;
      esac
    done

    # Add flags based on what's needed
    [[ "$HasIT" == "true" ]] && MvnTestArgs+=("-DrunITs=true")
    [[ "$HasE2E" == "true" ]] && MvnTestArgs+=("-DrunE2ETests=true")
    [[ "$HasCore" == "true" ]] && MvnTestArgs+=("-DrunCoreTests=true" "-Ppulsar-core-tests" "-pl" "pulsar-core,pulsar-core/pulsar-core-tests" "-am")

    # Add any additional Maven args
    MvnTestArgs+=("${AdditionalMvnArgs[@]}")

    # Execute Maven test command
    $MvnCmd "${MvnTestArgs[@]}"
    ExitCode=$?

    if [[ $ExitCode -ne 0 ]]; then
      echo ""
      echo "=========================================="
      echo "❌ Maven tests failed with exit code $ExitCode"
      echo "=========================================="
      exit $ExitCode
    fi

    echo ""
    echo "=========================================="
    echo "✅ Maven tests completed successfully"
    echo "=========================================="
fi

# Execute SDK tests
for TestType in "${SDKTests[@]}"; do
    echo "=========================================="
    echo "Running $TestType tests..."
    echo "=========================================="

    ExitCode=0

    case $TestType in
      python-sdk)
        echo "Running Python SDK tests..."
        PythonSdkDir="$APP_HOME/sdks/browser4-sdk-python"

        if [[ ! -d "$PythonSdkDir" ]]; then
          echo "Error: Python SDK directory not found at $PythonSdkDir"
          exit 1
        fi

        # Check if Python is available
        if ! command -v python3 &> /dev/null; then
          echo "Error: python3 is not installed or not in PATH"
          exit 1
        fi

        cd "$PythonSdkDir"
        echo "Working directory: $(pwd)"

        # Check if venv exists and activate it
        if [[ -d "$PythonSdkDir/venv" ]]; then
          echo "Activating virtual environment..."
          source "$PythonSdkDir/venv/bin/activate"
        fi

        # Check if pytest is available
        if ! python3 -m pytest --version &> /dev/null; then
          echo "Error: pytest is not installed. Install it with: pip install pytest"
          echo "Or install all dev dependencies with: pip install -e \".[dev]\" in $PythonSdkDir"
          exit 1
        fi

        python3 -m pytest "${AdditionalMvnArgs[@]}"
        ExitCode=$?
        cd "$APP_HOME"
        ;;
      nodejs-sdk)
        echo "Running NodeJS SDK tests..."
        NodejsSdkDir="$APP_HOME/sdks/browser4-sdk-nodejs"

        if [[ ! -d "$NodejsSdkDir" ]]; then
          echo "Error: NodeJS SDK directory not found at $NodejsSdkDir"
          exit 1
        fi

        # Check if Node.js is available
        if ! command -v node &> /dev/null; then
          echo "Error: node is not installed or not in PATH"
          exit 1
        fi

        cd "$NodejsSdkDir"
        echo "Working directory: $(pwd)"

        # Check if node_modules exists
        if [[ ! -d "$NodejsSdkDir/node_modules" ]]; then
          echo "Installing dependencies..."
          npm install
          if [[ $? -ne 0 ]]; then
            echo "Error: Failed to install dependencies"
            cd "$APP_HOME"
            exit 1
          fi
        fi

        # Check if jest is available
        if [[ ! -f "$NodejsSdkDir/node_modules/.bin/jest" ]]; then
          echo "Error: jest is not installed. Install it with: npm install"
          cd "$APP_HOME"
          exit 1
        fi

        npm test -- "${AdditionalMvnArgs[@]}"
        ExitCode=$?
        cd "$APP_HOME"
        ;;
      kotlin-sdk)
        echo "Running Kotlin SDK tests..."
        $MvnCmd test -DrunSDKTests=true -P all-modules -pl sdks/kotlin-sdk-tests -am "${AdditionalMvnArgs[@]}"
        ExitCode=$?
        ;;
      *)
        echo "Error: Unknown SDK test type '$TestType'"
        exit 1
        ;;
    esac

    # Check if test failed and exit immediately
    if [[ $ExitCode -ne 0 ]]; then
      echo ""
      echo "=========================================="
      echo "❌ $TestType tests failed with exit code $ExitCode"
      echo "=========================================="
      exit $ExitCode
    fi

    echo ""
    echo "=========================================="
    echo "✅ $TestType tests completed successfully"
    echo "=========================================="

done
exit 0
