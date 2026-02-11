# Adaptive Parallel Testing

## 概述

Kotlin SDK 测试套件现已支持**自适应并行测试**，可根据系统资源自动调整线程数，无需手动配置。

## 工作原理

测试系统会根据以下因素自动计算最优线程数：
- **CPU 核心数**: 使用 `nproc` 检测
- **可用内存**: 确保不会因内存不足而失败
- **测试类型**: 不同类型的测试使用不同的并行策略

计算公式：
```
线程数 = CPU 核心数 × 倍数系数
```

## 使用方法

### 1. 标准 Maven 命令（推荐）

```bash
# 默认策略：50% CPU 核心
cd sdks/kotlin-sdk-tests
../../mvnw test

# 在 8 核机器上 = 4 线程
# 在 16 核机器上 = 8 线程
# 在 2 核机器上 = 1 线程
```

### 2. 自定义并行度

```bash
# 快速策略：8 threads
mvn test -Dtest.thread.count=8

# 默认策略：4 threads
mvn test -Dtest.thread.count=4

# 保守策略：2 threads
mvn test -Dtest.thread.count=2
```

### 3. 使用智能脚本（最简单）

```bash
cd sdks/kotlin-sdk-tests

# 默认策略（50% CPU）
./adaptive-test.sh

# 快速反馈（100% CPU）
./adaptive-test.sh fast

# 保守模式（25% CPU）
./adaptive-test.sh conservative

# 自定义参数
./adaptive-test.sh integration -DskipTests=false
```

## 策略对比

| 策略 | 倍数 | 2核 | 4核 | 8核 | 16核 | 适用场景 |
|------|------|-----|-----|-----|------|----------|
| conservative | 0.25 | 1 | 1 | 2 | 4 | 本地调试 |
| integration | 0.5 | 1 | 2 | 4 | 8 | 日常开发 |
| fast | 1.0 | 2 | 4 | 8 | 16 | CI/快速反馈 |

## 性能预估

### 8 核机器示例

| 配置 | 线程数 | 预估时间 | 加速比 |
|------|--------|----------|--------|
| 固定 2 线程 | 2 | 31 分钟 | 1.0x |
| 自适应 25% | 2 | 31 分钟 | 1.0x |
| 自适应 50% | 4 | 18 分钟 | 1.7x |
| 自适应 100% | 8 | 12 分钟 | 2.6x |

### 16 核机器示例

| 配置 | 线程数 | 预估时间 | 加速比 |
|------|--------|----------|--------|
| 固定 2 线程 | 2 | 31 分钟 | 1.0x |
| 自适应 50% | 8 | 12 分钟 | 2.6x |
| 自适应 100% | 16 | 8 分钟 | 3.9x |

## 环境变量

可以通过环境变量配置默认行为：

```bash
# 设置默认倍数
export TEST_THREAD_MULTIPLIER=0.75

# CI 环境自动检测
export CI=true  # 自动使用 100% CPU
```

## CI/CD 集成

### GitHub Actions

```yaml
- name: Run Kotlin SDK Tests (Adaptive)
  run: |
    cd sdks/kotlin-sdk-tests
    ../../mvnw test
  # 自动根据 runner 的 CPU 核心数调整
```

### GitLab CI

```yaml
test:
  script:
    - cd sdks/kotlin-sdk-tests
    - ../../mvnw test -Dtest.thread.multiplier=1.0
  # 使用 100% CPU 加速 CI
```

## 监控和调试

### 查看实际线程数

```bash
mvn test -X | grep "parallel"
```

### 性能分析

测试完成后，分析报告：

```bash
cd target/surefire-reports
grep -h 'time="[^"]*"' TEST-*.xml | wc -l  # 测试总数
```

## 故障排除

### 问题 1: 测试不稳定/失败率增加

**原因**: 并行度过高导致资源竞争

**解决方案**:
```bash
# 降低并行度
mvn test -Dtest.thread.multiplier=0.25
```

### 问题 2: 内存不足 (OOM)

**原因**: 每个线程消耗大量内存

**解决方案**:
```bash
# 限制 JVM 堆大小
mvn test -Dtest.thread.multiplier=0.5 -DargLine="-Xmx4g"
```

### 问题 3: CPU 利用率低

**原因**: 线程数不足

**解决方案**:
```bash
# 增加并行度
mvn test -Dtest.thread.multiplier=1.0
```

## 技术细节

### Maven Surefire 配置

```xml
<perCoreThreadCount>true</perCoreThreadCount>
<threadCountMethods>${test.thread.multiplier}</threadCountMethods>
```

这使用 Maven Surefire 的内置自适应功能，根据 `Runtime.getRuntime().availableProcessors()` 动态计算线程数。

### 为什么选择 50% 作为默认值？

1. **平衡性能和稳定性**: 留出资源给操作系统和其他进程
2. **避免资源竞争**: Spring Boot 测试需要共享上下文
3. **浏览器限制**: Chrome 实例过多会导致性能下降
4. **内存考虑**: 每个测试线程需要 1-2GB 内存

## 参考文档

- [ADAPTIVE-PARALLEL-TESTING.md](docs-dev/ADAPTIVE-PARALLEL-TESTING.md) - 详细设计文档
- [PARALLEL-TEST-OPTIMIZATION.md](docs-dev/PARALLEL-TEST-OPTIMIZATION.md) - 优化策略
- [Maven Surefire 官方文档](https://maven.apache.org/surefire/maven-surefire-plugin/examples/fork-options-and-parallel-execution.html)

## 贡献

如果您在特定环境中发现更好的配置，欢迎提交 PR 更新默认值或添加新的预设策略。
