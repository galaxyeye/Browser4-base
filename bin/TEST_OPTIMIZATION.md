# test.sh 和 test.ps1 优化说明

## 优化目标
- 当执行 `test.sh all` 或 `test.ps1 all` 时，使用单个 Maven 命令执行所有测试
- 如果任何测试失败，立即退出，不继续执行后续测试
- 支持混合测试（例如：`test.sh fast kotlin-sdk` 可以同时运行 Maven 的 fast 测试和 Kotlin SDK 测试）

## 核心改进

### 1. 分离 Maven 和 SDK 测试
- Maven 测试类型：fast, it, e2e, core, rest
- SDK 测试类型：python-sdk, nodejs-sdk, kotlin-sdk
- 当遇到 `all` 时，展开为所有 Maven 测试类型：fast, core, it, e2e, rest

### 2. Maven 测试合并执行
- 所有 Maven 测试现在合并为一个 `mvnw test` 命令
- 根据指定的测试类型，添加相应的 Maven 参数标志：
  - `-DrunITs=true` (用于 it)
  - `-DrunE2ETests=true` (用于 e2e)
  - `-DrunCoreTests=true -Ppulsar-core-tests -pl pulsar-core,pulsar-core/pulsar-core-tests -am` (用于 core)

### 3. SDK 测试独立执行
- 如果指定了任何 SDK 测试，它们仍然单独执行
- 每个 SDK 测试在自己的环境中运行（例如 Python venv, Node.js, 或 Kotlin Maven）

### 4. 即时失败处理
- 执行完每个测试（Maven 或 SDK）后，检查退出码
- 如果任何测试失败（退出码非 0），立即打印错误信息并退出脚本
- 不会继续执行后续的测试

## 使用示例

```bash
# 运行所有 Maven 测试（fast, core, it, e2e, rest）作为单一命令
./test.sh all

# 运行特定的 Maven 测试
./test.sh fast
./test.sh it
./test.sh fast it e2e

# 运行 SDK 测试
./test.sh python-sdk
./test.sh kotlin-sdk
./test.sh nodejs-sdk

# 混合测试（Maven + SDK）
./test.sh fast python-sdk         # 运行 Maven fast + Python SDK
./test.sh it nodejs-sdk           # 运行 Maven it + Node.js SDK

# 带有 Maven 参数的测试
./test.sh all -X                  # 所有 Maven 测试 + 调试模式
./test.sh fast -pl pulsar-core    # 只测试 pulsar-core 模块的 fast 测试
```

## 命令执行流程

### test.sh all
1. 解析命令行参数，识别 `all`
2. 将 `all` 展开为 `fast core it e2e rest`
3. 分离 Maven 和 SDK 测试 → 所有为 Maven 测试
4. 构建单个 Maven 命令：`mvnw test -DrunITs=true -DrunE2ETests=true -DrunCoreTests=true ...`
5. 执行 Maven 命令
6. 检查退出码，失败则立即退出

### test.ps1 all
1. 解析命令行参数，识别 `all`
2. 将 `all` 展开为 `fast core it e2e rest`
3. 分离 Maven 和 SDK 测试 → 所有为 Maven 测试
4. 检查哪些测试类型被指定，构建 Maven 参数数组
5. 执行单个 Maven 命令
6. 检查 `$LASTEXITCODE`，失败则立即退出

## 文件修改说明

### test.sh
- 新增逻辑：将 Maven 测试合并为单个命令
- 新增错误处理：在 Maven 命令执行后检查退出码
- 保留：SDK 测试的单独执行机制

### test.ps1
- 新增逻辑：将 Maven 测试合并为单个 PowerShell 命令
- 新增错误处理：在每个测试命令执行后检查 `$LASTEXITCODE`
- 保留：SDK 测试的单独执行机制和现有的 try-catch 错误处理

