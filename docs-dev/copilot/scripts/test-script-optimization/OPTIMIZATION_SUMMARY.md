# 优化总结：test.sh 和 test.ps1

## 优化内容

### 核心改进
✅ **单个 Maven 命令执行**: 当执行 `test.sh all` 或 `test.ps1 all` 时，所有 Maven 测试（fast, core, it, e2e, rest）现在通过**单个** `mvnw test` 命令执行，而不是分别执行多个命令。

✅ **即时失败退出**: 任何测试失败都会立即打印错误信息并退出脚本，不会继续执行后续的测试。

### 实现细节

#### test.sh
**关键改动点：**

1. **参数分析与分离** (行 88-101)
   ```bash
   # 分离 Maven 和 SDK 测试
   MavenTests=()
   SDKTests=()
   for type in "${TestTypes[@]}"; do
     if [[ "$type" == "all" ]]; then
       MavenTests=("fast" "core" "it" "e2e" "rest")
       break
     elif [[ "$type" == "python-sdk" || "$type" == "nodejs-sdk" || "$type" == "kotlin-sdk" ]]; then
       SDKTests+=("$type")
     else
       MavenTests+=("$type")
     fi
   done
   ```

2. **合并执行 Maven 测试** (行 122-161)
   - 构建单个 Maven 命令
   - 根据指定的测试类型添加对应的 Maven 参数标志
   - 执行命令并检查退出码
   - 失败则立即退出

3. **SDK 测试单独执行** (行 164-284)
   - SDK 测试仍然单独执行（保持原有行为）
   - 每个 SDK 测试后检查退出码，失败则立即退出

#### test.ps1
**关键改动点：**

1. **参数分析与分离** (行 82-100)
   ```powershell
   # 分离 Maven 和 SDK 测试
   $MavenTests = @()
   $SDKTests = @()

   foreach ($type in $TestTypes) {
       if ($type -eq "all") {
           $MavenTests += "fast", "core", "it", "e2e", "rest"
           break
       } elseif ($type -in "python-sdk", "nodejs-sdk", "kotlin-sdk") {
           $SDKTests += $type
       } else {
           $MavenTests += $type
       }
   }
   ```

2. **合并执行 Maven 测试** (行 105-154)
   - 用 PowerShell 数组构建 Maven 命令参数
   - 检查哪些测试类型被指定，添加对应标志
   - 执行命令：`& $MvnCmd @MvnTestArgs`
   - 检查 `$LASTEXITCODE` 和 `$LASTEXITCODE -ne 0` 时立即退出

3. **SDK 测试单独执行** (行 157-273)
   - SDK 测试在单独的循环中执行
   - 每个 SDK 测试后检查退出码，失败则立即退出

## 行为变化

### 之前（修改前）
```
test.sh all
├─ Running fast tests...
├─ Running core tests...
├─ Running it tests...
├─ Running e2e tests...
└─ Running rest tests...
```
**问题**: 5个独立的 Maven 命令，5次项目编译

### 之后（修改后）
```
test.sh all
└─ Running Maven tests: fast, core, it, e2e, rest
   (单个 mvnw test 命令，1次编译)
```
**优势**:
- 只编译一次项目
- 更快的执行时间
- 一致的测试环境

## 错误处理改进

### test.sh
```bash
if [[ $ExitCode -ne 0 ]]; then
  echo ""
  echo "=========================================="
  echo "❌ Maven tests failed with exit code $ExitCode"
  echo "=========================================="
  exit $ExitCode
fi
```

### test.ps1
```powershell
if ($ExitCode -ne 0) {
    Write-Host ""
    Write-Host "=========================================="
    Write-Host "❌ Maven tests failed with exit code $ExitCode"
    Write-Host "=========================================="
    exit $ExitCode
}
```

## 支持的用例

### Maven 测试
```bash
./test.sh all              # 所有 Maven 测试（单个命令）
./test.sh fast             # 只运行 fast 测试
./test.sh it               # 只运行 it 测试
./test.sh fast it e2e      # 三种 Maven 测试（单个命令）
./test.sh all -X           # 所有 Maven 测试 + 调试参数
```

### SDK 测试
```bash
./test.sh python-sdk       # Python SDK 测试
./test.sh nodejs-sdk       # Node.js SDK 测试
./test.sh kotlin-sdk       # Kotlin SDK 测试
```

### 混合测试
```bash
./test.sh fast python-sdk  # Maven fast + Python SDK
./test.sh it nodejs-sdk    # Maven it + Node.js SDK
```

### 带 Maven 参数
```bash
./test.sh fast -pl pulsar-core         # 特定模块的 fast 测试
./test.sh python-sdk -m integration    # Python SDK 的 integration 测试
```

## 向后兼容性
✅ 所有现有的命令和参数仍然有效
✅ 使用说明和 help 信息保持不变
✅ SDK 测试的行为完全相同
✅ 仅改变了 Maven 测试的执行方式（多个命令 → 单个命令）

## 文件修改
- `D:\workspace\Browser4\Browser4-4.6\bin\test.sh` (284 行)
- `D:\workspace\Browser4\Browser4-4.6\bin\test.ps1` (273 行)

