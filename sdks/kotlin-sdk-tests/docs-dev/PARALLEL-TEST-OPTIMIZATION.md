# Kotlin SDK 并行测试优化方案

## 当前配置分析

### 现状
- **并行策略**: `methods` (方法级并行)
- **线程数**: 2
- **CPU 核心**: 8 核
- **测试总数**: 160 个
- **总执行时间**: 60.8 分钟 (3648 秒)
- **平均单测耗时**: 22.8 秒

### 瓶颈分析
1. **低线程利用率**: 只用了 2/8 核心 (25% CPU 利用率)
2. **Spring Boot 上下文**: 所有测试共享 Spring 上下文，限制了并行度
3. **重量级测试**: AgenticSession 测试平均 50+ 秒，占用大量资源

## 优化方案

### 方案 1: 增加线程数 (推荐，简单有效)

**修改配置**:
```xml
<parallel>methods</parallel>
<threadCount>4</threadCount>
<useUnlimitedThreads>false</useUnlimitedThreads>
```

**预期效果**:
- 理论加速: 2x (从 2 线程到 4 线程)
- 实际预估: 1.6-1.8x (考虑资源竞争)
- 预计时间: 35-40 分钟

**优点**:
- 配置简单，只需改一个数字
- 风险低，不影响测试逻辑

**缺点**:
- Spring Boot 上下文仍是共享的，存在竞争
- 可能触发 Chrome/浏览器资源限制

---

### 方案 2: 类级别并行 (中等复杂度)

**修改配置**:
```xml
<parallel>classes</parallel>
<threadCount>4</threadCount>
<perCoreThreadCount>false</perCoreThreadCount>
```

**预期效果**:
- 更好的隔离性
- 减少方法间资源竞争
- 预计时间: 30-35 分钟

**优点**:
- 测试类之间完全隔离
- 更稳定的并行执行

**缺点**:
- 慢测试类会成为瓶颈 (AgenticSessionIntegrationTest 12.3 分钟)

---

### 方案 3: 混合并行 + 分组执行 (最优，复杂)

**第一步：分离快慢测试**

**快速测试** (< 5 分钟):
- PulsarClientIntegrationTest (1.6m)
- PulsarSessionAdvancedTest (1.4m)
- ModelsTest (< 1s)
- AgenticContextsTest (< 1s)

**中速测试** (5-10 分钟):
- WebDriverAdvancedTest (4.9m)
- PulsarSessionIntegrationTest (4.7m)
- WebDriverClickAndAttributeTest (4.0m)
- WebDriverKeyboardAndFocusTest (6.8m)
- ErrorHandlingAndEdgeCasesTest (7.5m)
- WebDriverIntegrationTest (7.7m)

**慢速测试** (> 10 分钟):
- AgenticSessionAdvancedTest (9.9m)
- AgenticSessionIntegrationTest (12.3m)

**配置策略**:

```xml
<!-- Profile 1: 快速测试 - 高并发 -->
<profile>
    <id>fast-tests</id>
    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-surefire-plugin</artifactId>
                <configuration>
                    <parallel>methods</parallel>
                    <threadCount>6</threadCount>
                    <includes>
                        <include>**/PulsarClientIntegrationTest.java</include>
                        <include>**/PulsarSessionAdvancedTest.java</include>
                        <include>**/ModelsTest.java</include>
                        <include>**/AgenticContextsTest.java</include>
                    </includes>
                </configuration>
            </plugin>
        </plugins>
    </build>
</profile>

<!-- Profile 2: 中速测试 - 中等并发 -->
<profile>
    <id>medium-tests</id>
    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-surefire-plugin</artifactId>
                <configuration>
                    <parallel>classes</parallel>
                    <threadCount>4</threadCount>
                    <includes>
                        <include>**/WebDriver*Test.java</include>
                        <include>**/PulsarSessionIntegrationTest.java</include>
                        <include>**/ErrorHandlingAndEdgeCasesTest.java</include>
                    </includes>
                </configuration>
            </plugin>
        </plugins>
    </build>
</profile>

<!-- Profile 3: 慢速测试 - 低并发 -->
<profile>
    <id>slow-tests</id>
    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-surefire-plugin</artifactId>
                <configuration>
                    <parallel>methods</parallel>
                    <threadCount>2</threadCount>
                    <includes>
                        <include>**/AgenticSession*Test.java</include>
                    </includes>
                </configuration>
            </plugin>
        </plugins>
    </build>
</profile>
```

**并行执行**:
```bash
# 同时运行三个 Maven 进程
mvn test -P fast-tests &
mvn test -P medium-tests &
mvn test -P slow-tests &
wait
```

**预期效果**:
- 预计时间: 12-15 分钟 (最慢组决定总时间)
- 加速比: 4-5x

---

### 方案 4: 动态线程数 (自适应)

```xml
<parallel>methods</parallel>
<perCoreThreadCount>true</perCoreThreadCount>
<threadCountClasses>2</threadCountClasses>
<threadCountMethods>4</threadCountMethods>
```

---

## 推荐执行策略

### 短期方案 (立即实施)
**增加到 4 线程**:
```xml
<threadCount>4</threadCount>
```

### 中期方案 (1 周内)
**改为类级别并行**:
```xml
<parallel>classes</parallel>
<threadCount>4</threadCount>
```

### 长期方案 (优化架构)
1. **拆分慢测试**: 将 50+ 秒的测试拆分成多个小测试
2. **Mock AI 服务**: 对非关键路径的 AI 调用使用 Mock
3. **测试数据优化**: 减少大数据集的测试用例
4. **资源池化**: 复用 Chrome 实例而非每次启动

---

## 风险评估

### 并发安全性
- ✅ **测试隔离性**: 每个测试有独立的 session
- ⚠️ **Spring 上下文**: 共享上下文可能有竞争
- ⚠️ **浏览器资源**: 同时打开多个 Chrome 可能超出系统限制
- ⚠️ **端口冲突**: MockServer 使用 RANDOM_PORT，应该安全

### 建议监控
- CPU 使用率
- 内存占用
- Chrome 进程数
- 测试失败率

---

## 实施步骤

### Step 1: 测试 4 线程
```bash
# 修改 pom.xml
vim sdks/kotlin-sdk-tests/pom.xml

# 运行测试
cd sdks/kotlin-sdk-tests
../../mvnw test
```

### Step 2: 监控结果
- 检查测试通过率
- 记录执行时间
- 观察 CPU/内存

### Step 3: 逐步调优
- 如果稳定，尝试 6 线程
- 如果不稳定，回退到 3 线程
- 考虑切换到类级别并行

---

## 性能预测

| 方案 | 线程数 | 并行策略 | 预计时间 | 加速比 |
|------|--------|----------|----------|--------|
| 当前 | 2 | methods | 31 分钟 | 1.0x |
| 方案1 | 4 | methods | 18-20 分钟 | 1.6x |
| 方案1+ | 6 | methods | 13-15 分钟 | 2.2x |
| 方案2 | 4 | classes | 16-18 分钟 | 1.8x |
| 方案3 | 4+4+2 | 分组 | 12-14 分钟 | 2.5x |

---

## 结论

**立即行动**: 将 `threadCount` 从 2 改为 4，预计可节省 10-13 分钟。

**后续优化**: 根据稳定性，逐步尝试更高的并发度或分组策略。
