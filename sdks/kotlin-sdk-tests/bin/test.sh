#!/bin/bash

set -e

echo "=== Kotlin SDK Test Runner ==="

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 解析参数
RUN_UNIT=true
RUN_INTEGRATION=false
RUN_ALL=false

while [[ $# -gt 0 ]]; do
    case $1 in
        --integration)
            RUN_INTEGRATION=true
            RUN_UNIT=false
            shift
            ;;
        --all)
            RUN_ALL=true
            RUN_UNIT=true
            RUN_INTEGRATION=true
            shift
            ;;
        --unit)
            RUN_UNIT=true
            shift
            ;;
        *)
            echo "Unknown option: $1"
            echo "Usage: $0 [--unit|--integration|--all]"
            exit 1
            ;;
    esac
done

# 运行单元测试
if [ "$RUN_UNIT" = true ]; then
    echo -e "${YELLOW}Running unit tests...${NC}"
    mvn test
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✓ Unit tests passed${NC}"
    else
        echo -e "${RED}✗ Unit tests failed${NC}"
        exit 1
    fi
fi

# 运行集成测试
if [ "$RUN_INTEGRATION" = true ]; then
    echo -e "${YELLOW}Running integration tests...${NC}"

    # 检查 Chrome 是否安装
    if ! command -v google-chrome &> /dev/null; then
        echo -e "${YELLOW}Warning: Chrome not found. Some tests may fail.${NC}"
    fi

    # 运行集成测试
    mvn test -Pintegration-test
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✓ Integration tests passed${NC}"
    else
        echo -e "${RED}✗ Integration tests failed${NC}"
        exit 1
    fi
fi

echo -e "${GREEN}=== All tests completed successfully ===${NC}"
