# test.sh 和 test.ps1 优化指南

## 📋 概述

本优化升级了 `test.sh` 和 `test.ps1` 脚本，以实现以下目标：

1. **单个 Maven 命令执行**: 当执行 `test.sh all` 或 `test.ps1 all` 时，所有 Maven 测试（fast, core, it, e2e, rest）通过**单个** `mvnw test` 命令执行，而不是分别执行多个命令
2. **即时失败退出**: 任何测试失败都会立即打印错误信息并退出脚本，不会继续执行后续的测试

## 🎯 核心改进

### 从这样：
```
test.sh all
│
├─ mvnw test                                    (fast 测试)
├─ mvnw test -DrunCoreTests=true ...           (core 测试)
├─ mvnw test -DrunITs=true                     (integration 测试)
├─ mvnw test -DrunE2ETests=true -P all-modules (e2e 测试)
└─ mvnw test -DrunRestTests=true               (rest 测试)

问题：5 次编译，5 个独立 JVM 实例
```

### 改进为：
```
test.sh all
│
└─ mvnw test -DrunITs=true -DrunE2ETests=true -DrunCoreTests=true ...

优势：1 次编译，1 个 JVM 实例，快速失败机制
```

## 📖 使用指南

### 基础用法

```bash
# 运行所有 Maven 测试（使用单个命令）
./test.sh all
./test.ps1 all

# 运行特定的 Maven 测试
./test.sh fast           # 快速测试
./test.sh it             # 集成测试
./test.sh e2e            # 端到端测试
./test.sh core           # 核心模块测试
./test.sh rest           # REST 模块测试

# 组合 Maven 测试（使用单个命令）
./test.sh fast it        # 快速测试 + 集成测试
./test.sh it e2e         # 集成测试 + 端到端测试
```

### SDK 测试

```bash
# Python SDK 测试
./test.sh python-sdk
./test.ps1 python-sdk

# Node.js SDK 测试
./test.sh nodejs-sdk
./test.ps1 nodejs-sdk

# Kotlin SDK 测试
./test.sh kotlin-sdk
./test.ps1 kotlin-sdk
```

### 混合测试（Maven + SDK）

```bash
# Maven 测试 + SDK 测试
./test.sh fast python-sdk       # 快速测试 + Python SDK
./test.sh it nodejs-sdk         # 集成测试 + Node.js SDK
./test.sh all kotlin-sdk        # 所有 Maven 测试 + Kotlin SDK
```

### 带 Maven 参数

```bash
# 使用 Maven 参数
./test.sh all -X                       # 所有测试 + 调试模式
./test.sh fast -pl pulsar-core         # 特定模块的快速测试
./test.sh fast -am -pl pulsar-core     # 及其依赖

# Python SDK 带参数
./test.sh python-sdk -m integration    # 仅运行集成测试
./test.sh python-sdk --cov             # 带代码覆盖率
```

## 🔄 执行流程说明

### test.sh all 的执行流程

1. **参数解析**
   - 识别参数 `all`
   - 清空 TestTypes，设置为 `("fast" "core" "it" "e2e" "rest")`

2. **测试分离**
   - `MavenTests = (fast core it e2e rest)`
   - `SDKTests = ()` （未指定 SDK）

3. **构建 Maven 命令**
   ```bash
   mvnw test \
     -DrunITs=true \
     -DrunE2ETests=true \
     -DrunCoreTests=true \
     -Ppulsar-core-tests \
     -pl pulsar-core,pulsar-core/pulsar-core-tests \
     -am
   ```

4. **执行和错误检查**
   - 执行单个 Maven 命令
   - 检查退出码
   - 失败 → 打印错误信息并退出
   - 成功 → 继续到 SDK 测试（如有）

### test.sh fast python-sdk 的执行流程

1. **参数解析**
   - `TestTypes = ("fast" "python-sdk")`

2. **测试分离**
   - `MavenTests = (fast)`
   - `SDKTests = (python-sdk)`

3. **执行 Maven 测试**
   ```bash
   mvnw test  # 最简单的命令
   ```
   - 检查退出码，失败则退出

4. **执行 SDK 测试**
   ```bash
   cd sdks/browser4-sdk-python
   python3 -m pytest
   ```
   - 检查退出码，失败则退出

## ⚡ 性能改进

### 编译次数

| 命令 | 之前 | 之后 | 改进 |
|------|------|------|------|
| `test.sh all` | 5 次 | 1 次 | 减少 80% |
| `test.sh fast it e2e` | 3 次 | 1 次 | 减少 67% |
| `test.sh core` | 1 次 | 1 次 | - |

### 执行时间示例

假设每次 Maven 编译约 3 分钟，每次测试约 2 分钟：

| 命令 | 之前 | 之后 | 节省 |
|------|------|------|------|
| `test.sh all` | 5×(3+2)=25min | 3+(2×5)=13min | **48%** |
| `test.sh fast it e2e` | 3×(3+2)=15min | 3+(2×3)=9min | **40%** |

## 🛡️ 错误处理

所有命令都支持即时失败机制：

```bash
./test.sh all

# 如果 fast 测试失败：
# ❌ Maven tests failed with exit code 1
# 脚本立即退出，不继续运行 core, it, e2e, rest 测试

# 如果 fast 成功但 python-sdk 失败：
# ❌ python-sdk tests failed with exit code 1
# 脚本立即退出
```

## 📝 参数说明

### 测试类型

| 类型 | 说明 | 执行方式 |
|-----|------|--------|
| `fast` | 快速单元测试（默认） | Maven |
| `it` | 集成测试 | Maven |
| `e2e` | 端到端测试 | Maven |
| `core` | 核心模块补充测试 | Maven |
| `rest` | REST 模块测试 | Maven |
| `all` | 所有 Maven 测试 | Maven（单个命令） |
| `python-sdk` | Python SDK 测试 | pytest |
| `nodejs-sdk` | Node.js SDK 测试 | npm test |
| `kotlin-sdk` | Kotlin SDK 测试 | Maven |

### Maven 参数

所有 Maven 参数都会被传递到最终的 `mvnw test` 命令：

```bash
# -pl: 指定特定模块
./test.sh all -pl pulsar-core

# -am: 同时构建依赖
./test.sh fast -am -pl pulsar-core

# -X: 调试模式
./test.sh all -X

# -DskipTests: 跳过测试（仅构建）
./test.sh all -DskipTests
```

## 🔍 调试

### 查看完整的 Maven 命令

运行时使用 `-X` 参数：

```bash
./test.sh all -X

# Maven 会输出详细信息，包括完整的命令行
```

### 测试特定模块

```bash
# 仅测试 pulsar-core
./test.sh all -pl pulsar-core

# 测试 pulsar-core 及其依赖
./test.sh all -am -pl pulsar-core
```

### 跳过特定模块

```bash
# 跳过某个模块的测试
./test.sh all -pl '!pulsar-tests'
```

## ✅ 向后兼容性

所有原有的命令格式完全保持兼容：

```bash
# 所有这些命令都仍然有效
./test.sh                    # 显示帮助
./test.sh -h                 # 显示帮助
./test.sh fast               # ✅
./test.sh it                 # ✅
./test.sh python-sdk         # ✅
./test.sh all -X             # ✅
./test.sh fast -pl module    # ✅
```

## 📚 详细文档

- `OPTIMIZATION_SUMMARY.md` - 优化的详细说明
- `bin/TEST_OPTIMIZATION.md` - 测试优化指南
- `bin/COMPLETION_REPORT.md` - 完成报告
- `bin/VERIFICATION_CHECKLIST.md` - 验证清单

## 🐛 问题排查

### 问题：test.sh: command not found

**解决方案**：确保脚本有执行权限
```bash
chmod +x bin/test.sh
```

### 问题：Maven wrapper not found

**解决方案**：运行脚本时应在项目根目录
```bash
cd /path/to/Browser4-4.6
./bin/test.sh all
```

### 问题：Python/Node.js 未找到

**解决方案**：确保环境中已安装必要工具
```bash
# Python
python3 --version
pip install pytest

# Node.js
node --version
npm install
```

## 💡 最佳实践

1. **在 CI/CD 中使用 `test.sh all`**
   - 快速反馈：编译次数减少 80%
   - 一致性：单个 JVM 实例

2. **本地开发使用 `test.sh fast`**
   - 快速反馈：只运行快速测试
   - 节省时间

3. **提交前运行 `test.sh all`**
   - 完整的测试覆盖
   - 确保不破坏其他测试

4. **使用 Maven 参数进行细粒度测试**
   ```bash
   ./test.sh fast -pl your-module -DskipIT  # 快速检查特定模块
   ```

---

**最后更新**: 2026-02-15
**文件**: `bin/test.sh`, `bin/test.ps1`
**状态**: ✅ 生产就绪

