#!/bin/bash
# Adaptive test runner for Kotlin SDK tests
# Automatically determines optimal thread count based on system resources

set -e

# Color output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Get script directory
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}Kotlin SDK Adaptive Test Runner${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

# Detect system resources
CPU_CORES=$(nproc 2>/dev/null || sysctl -n hw.ncpu 2>/dev/null || echo "4")
TOTAL_MEMORY_GB=$(free -g 2>/dev/null | awk '/^Mem:/{print $2}' || echo "8")
AVAILABLE_MEMORY_GB=$(free -g 2>/dev/null | awk '/^Mem:/{print $7}' || echo "4")

echo -e "${GREEN}System Resources:${NC}"
echo "  CPU Cores: $CPU_CORES"
echo "  Total Memory: ${TOTAL_MEMORY_GB}GB"
echo "  Available Memory: ${AVAILABLE_MEMORY_GB}GB"
echo ""

# Determine test strategy
TEST_TYPE="${1:-integration}"
MULTIPLIER=""

case "$TEST_TYPE" in
    fast)
        echo -e "${YELLOW}Strategy: Fast feedback (aggressive parallelism)${NC}"
        MULTIPLIER=1.0
        ;;
    integration)
        echo -e "${YELLOW}Strategy: Integration tests (balanced)${NC}"
        MULTIPLIER=0.5
        ;;
    conservative)
        echo -e "${YELLOW}Strategy: Conservative (minimal resource usage)${NC}"
        MULTIPLIER=0.25
        ;;
    *)
        echo -e "${YELLOW}Strategy: Default (balanced)${NC}"
        MULTIPLIER=0.5
        ;;
esac

# Calculate optimal threads
CALCULATED_THREADS=$(echo "$CPU_CORES * $MULTIPLIER" | bc | awk '{printf "%.0f", $1}')

# Memory-based limit (assume 2GB per thread for integration tests)
MEMORY_PER_THREAD=2
MAX_THREADS_BY_MEMORY=$((AVAILABLE_MEMORY_GB / MEMORY_PER_THREAD))

if [ "$CALCULATED_THREADS" -gt "$MAX_THREADS_BY_MEMORY" ] && [ "$MAX_THREADS_BY_MEMORY" -gt 0 ]; then
    OPTIMAL_THREADS=$MAX_THREADS_BY_MEMORY
    echo -e "${YELLOW}⚠️  Memory constraint: limiting threads to $OPTIMAL_THREADS (calculated $CALCULATED_THREADS)${NC}"
else
    OPTIMAL_THREADS=$CALCULATED_THREADS
fi

# Ensure at least 1 thread
if [ "$OPTIMAL_THREADS" -lt 1 ]; then
    OPTIMAL_THREADS=1
fi

echo ""
echo -e "${GREEN}Test Configuration:${NC}"
echo "  Multiplier: ${MULTIPLIER}x"
echo "  Thread Count: ${OPTIMAL_THREADS}"
echo "  Parallel Mode: methods"
echo ""

# Build Maven command
MVN_CMD="./mvnw"
if [ ! -x "$PROJECT_ROOT/mvnw" ]; then
    MVN_CMD="mvn"
fi

# Shift to remove first argument (test type)
shift 2>/dev/null || true

# Run tests
echo -e "${BLUE}Running tests...${NC}"
echo ""

cd "$PROJECT_ROOT/sdks/kotlin-sdk-tests"

$MVN_CMD clean test \
    -Dtest.thread.multiplier=$MULTIPLIER \
    "$@"

EXIT_CODE=$?

echo ""
if [ $EXIT_CODE -eq 0 ]; then
    echo -e "${GREEN}✓ Tests passed successfully!${NC}"
else
    echo -e "${RED}✗ Tests failed with exit code $EXIT_CODE${NC}"
fi

exit $EXIT_CODE
