# 自适应并行测试方案

## Maven Surefire 自适应配置

### 方案 1: 基于 CPU 核心数自适应 (推荐)

Maven Surefire 支持 `perCoreThreadCount` 参数，可以根据可用 CPU 核心数自动计算线程数。

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <configuration>
        <parallel>methods</parallel>
        
        <!-- 自适应配置：每个核心运行指定数量的线程 -->
        <perCoreThreadCount>true</perCoreThreadCount>
        <threadCountMethods>1</threadCountMethods>
        
        <!-- 或者使用倍数 -->
        <!-- <threadCount>0.5C</threadCount> 表示核心数的 50% -->
        
        <!-- 可选：设置最小和最大线程数 -->
        <properties>
            <property>
                <name>junit.jupiter.execution.parallel.config.strategy</name>
                <value>dynamic</value>
            </property>
        </properties>
    </configuration>
</plugin>
```

**实际效果**:
- 2 核机器: 2 线程
- 4 核机器: 4 线程
- 8 核机器: 8 线程
- 16 核机器: 16 线程

---

### 方案 2: 使用 Maven 属性动态配置

通过 Maven 属性和系统属性实现动态配置：

```xml
<properties>
    <!-- 默认值：CPU 核心数的 50% -->
    <test.thread.count>${env.TEST_THREADS}</test.thread.count>
    <!-- 如果环境变量未设置，使用默认值 -->
    <test.thread.count.default>4</test.thread.count.default>
</properties>

<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <configuration>
        <parallel>methods</parallel>
        <threadCount>${test.thread.count}</threadCount>
    </configuration>
</plugin>
```

**使用方式**:
```bash
# 本地开发：使用默认值
mvn test

# CI环境：根据机器配置
export TEST_THREADS=8
mvn test

# 命令行覆盖
mvn test -Dtest.thread.count=6
```

---

### 方案 3: 智能自适应脚本

创建一个 shell 脚本自动检测最佳线程数：

```bash
#!/bin/bash
# bin/adaptive-test.sh

# 获取 CPU 核心数
CPU_CORES=$(nproc)

# 获取可用内存 (GB)
AVAILABLE_MEMORY=$(free -g | awk '/^Mem:/{print $7}')

# 计算最佳线程数
# 规则：
# - 每个线程至少需要 1GB 内存
# - 不超过 CPU 核心数的 75%
# - 最少 2 个线程，最多 8 个线程

MAX_THREADS_BY_CPU=$((CPU_CORES * 3 / 4))
MAX_THREADS_BY_MEMORY=$AVAILABLE_MEMORY

if [ $MAX_THREADS_BY_MEMORY -lt $MAX_THREADS_BY_CPU ]; then
    OPTIMAL_THREADS=$MAX_THREADS_BY_MEMORY
else
    OPTIMAL_THREADS=$MAX_THREADS_BY_CPU
fi

# 限制范围
if [ $OPTIMAL_THREADS -lt 2 ]; then
    OPTIMAL_THREADS=2
elif [ $OPTIMAL_THREADS -gt 8 ]; then
    OPTIMAL_THREADS=8
fi

echo "Detected: $CPU_CORES CPU cores, ${AVAILABLE_MEMORY}GB available memory"
echo "Optimal thread count: $OPTIMAL_THREADS"

# 运行测试
mvn test -Dtest.thread.count=$OPTIMAL_THREADS "$@"
```

**使用方式**:
```bash
chmod +x bin/adaptive-test.sh
./bin/adaptive-test.sh
```

---

### 方案 4: JUnit 5 内置动态并行

如果使用 JUnit 5，可以利用其内置的动态并行策略：

```properties
# src/test/resources/junit-platform.properties

# 使用动态策略
junit.jupiter.execution.parallel.enabled=true
junit.jupiter.execution.parallel.mode.default=concurrent
junit.jupiter.execution.parallel.mode.classes.default=concurrent

# 动态配置策略
junit.jupiter.execution.parallel.config.strategy=dynamic
junit.jupiter.execution.parallel.config.dynamic.factor=1.0

# 或者使用固定策略
# junit.jupiter.execution.parallel.config.strategy=fixed
# junit.jupiter.execution.parallel.config.fixed.parallelism=4
```

**动态因子说明**:
- `factor=0.5`: 使用 50% 的可用处理器
- `factor=1.0`: 使用 100% 的可用处理器
- `factor=2.0`: 使用 200% 的可用处理器 (超线程)

---

### 方案 5: 混合自适应策略 (最优)

结合环境检测和测试类型的智能策略：

```xml
<properties>
    <!-- 基础配置 -->
    <test.parallel.mode>${env.TEST_PARALLEL_MODE}</test.parallel.mode>
    <test.thread.multiplier>${env.TEST_THREAD_MULTIPLIER}</test.thread.multiplier>
    
    <!-- 默认值 -->
    <test.parallel.mode.default>methods</test.parallel.mode.default>
    <test.thread.multiplier.default>0.5</test.thread.multiplier.default>
</properties>

<profiles>
    <!-- Profile 1: 本地开发 - 保守策略 -->
    <profile>
        <id>local</id>
        <activation>
            <activeByDefault>false</activeByDefault>
            <property>
                <name>env.CI</name>
                <value>!true</value>
            </property>
        </activation>
        <build>
            <plugins>
                <plugin>
                    <groupId>org.apache.maven.plugins</groupId>
                    <artifactId>maven-surefire-plugin</artifactId>
                    <configuration>
                        <parallel>methods</parallel>
                        <perCoreThreadCount>true</perCoreThreadCount>
                        <threadCountMethods>0.5</threadCountMethods>
                        <!-- 50% CPU 核心 -->
                    </configuration>
                </plugin>
            </plugins>
        </build>
    </profile>
    
    <!-- Profile 2: CI 环境 - 激进策略 -->
    <profile>
        <id>ci</id>
        <activation>
            <property>
                <name>env.CI</name>
                <value>true</value>
            </property>
        </activation>
        <build>
            <plugins>
                <plugin>
                    <groupId>org.apache.maven.plugins</groupId>
                    <artifactId>maven-surefire-plugin</artifactId>
                    <configuration>
                        <parallel>methods</parallel>
                        <perCoreThreadCount>true</perCoreThreadCount>
                        <threadCountMethods>1</threadCountMethods>
                        <!-- 100% CPU 核心 -->
                    </configuration>
                </plugin>
            </plugins>
        </build>
    </profile>
    
    <!-- Profile 3: 快速反馈 - 最大并行 -->
    <profile>
        <id>fast</id>
        <build>
            <plugins>
                <plugin>
                    <groupId>org.apache.maven.plugins</groupId>
                    <artifactId>maven-surefire-plugin</artifactId>
                    <configuration>
                        <parallel>classes</parallel>
                        <perCoreThreadCount>true</perCoreThreadCount>
                        <threadCountClasses>1</threadCountClasses>
                    </configuration>
                </plugin>
            </plugins>
        </build>
    </profile>
</profiles>
```

**使用方式**:
```bash
# 本地开发
mvn test -Plocal

# CI 环境 (自动检测)
mvn test

# 快速反馈
mvn test -Pfast
```

---

## 推荐实施方案

### 阶段 1: 立即实施 (零配置自适应)

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <configuration>
        <parallel>methods</parallel>
        
        <!-- 自适应：每个核心 0.5 个线程 -->
        <perCoreThreadCount>true</perCoreThreadCount>
        <threadCountMethods>0.5</threadCountMethods>
        
        <!-- 在 8 核机器上 = 4 线程 -->
        <!-- 在 16 核机器上 = 8 线程 -->
        <!-- 在 2 核机器上 = 1 线程 (至少会有 1 个) -->
    </configuration>
</plugin>
```

### 阶段 2: 增强配置 (支持覆盖)

```xml
<properties>
    <test.thread.multiplier>0.5</test.thread.multiplier>
</properties>

<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <configuration>
        <parallel>methods</parallel>
        <perCoreThreadCount>true</perCoreThreadCount>
        <threadCountMethods>${test.thread.multiplier}</threadCountMethods>
    </configuration>
</plugin>
```

**使用方式**:
```bash
# 默认：50% CPU
mvn test

# 激进：100% CPU
mvn test -Dtest.thread.multiplier=1

# 保守：25% CPU
mvn test -Dtest.thread.multiplier=0.25
```

### 阶段 3: 智能脚本包装

```bash
#!/bin/bash
# bin/smart-test.sh

CPU_CORES=$(nproc)
AVAILABLE_MEMORY_GB=$(free -g | awk '/^Mem:/{print $7}')

# 根据测试类型调整倍数
if [[ "$1" == "integration" ]]; then
    # 集成测试：保守策略 (每个测试消耗更多资源)
    MULTIPLIER=0.25
elif [[ "$1" == "unit" ]]; then
    # 单元测试：激进策略
    MULTIPLIER=1.0
else
    # 默认策略
    MULTIPLIER=0.5
fi

# 内存限制检查
REQUIRED_MEMORY_PER_THREAD=2
MAX_THREADS_BY_MEMORY=$((AVAILABLE_MEMORY_GB / REQUIRED_MEMORY_PER_THREAD))
CALCULATED_THREADS=$(echo "$CPU_CORES * $MULTIPLIER" | bc | awk '{print int($1+0.5)}')

if [ $CALCULATED_THREADS -gt $MAX_THREADS_BY_MEMORY ]; then
    THREADS=$MAX_THREADS_BY_MEMORY
    echo "⚠️  Memory limited: using $THREADS threads (calculated $CALCULATED_THREADS)"
else
    THREADS=$CALCULATED_THREADS
    echo "✓ Using $THREADS threads ($MULTIPLIER × $CPU_CORES cores)"
fi

# 确保至少 1 个线程
if [ $THREADS -lt 1 ]; then
    THREADS=1
fi

mvn test -Dtest.thread.multiplier=$MULTIPLIER "${@:2}"
```

---

## 性能对比

| 策略 | 2核 | 4核 | 8核 | 16核 | 优点 | 缺点 |
|------|-----|-----|-----|------|------|------|
| 固定 (4线程) | 4 | 4 | 4 | 4 | 可预测 | 不适应 |
| 50% CPU | 1 | 2 | 4 | 8 | 平衡 | 保守 |
| 100% CPU | 2 | 4 | 8 | 16 | 最快 | 资源竞争 |
| 智能脚本 | 1 | 2 | 4 | 6 | 稳定 | 复杂 |

---

## 监控和调优

### 添加性能日志

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <configuration>
        <!-- 输出并行执行统计 -->
        <reportFormat>plain</reportFormat>
        <useFile>true</useFile>
        
        <!-- 记录每个测试的时间 -->
        <printSummary>true</printSummary>
        
        <!-- JVM 性能监控 -->
        <argLine>
            -XX:+PrintGCDetails
            -XX:+PrintGCTimeStamps
        </argLine>
    </configuration>
</plugin>
```

### 分析脚本

```bash
#!/bin/bash
# bin/analyze-test-performance.sh

REPORT_DIR="target/surefire-reports"

echo "=== Test Performance Analysis ==="
echo ""

# 总测试数
TOTAL_TESTS=$(grep -h '<testcase' $REPORT_DIR/TEST-*.xml | wc -l)

# 总耗时
TOTAL_TIME=$(grep -h 'time="[^"]*"' $REPORT_DIR/TEST-*.xml | \
    sed 's/.*time="//;s/".*//' | \
    awk '{sum+=$1} END {print sum}')

# 平均耗时
AVG_TIME=$(echo "scale=2; $TOTAL_TIME / $TOTAL_TESTS" | bc)

# 线程利用率 (假设测试运行了 X 分钟)
WALL_CLOCK_TIME=$(ls -lt $REPORT_DIR/TEST-*.xml | head -1 | awk '{print $9}' | xargs stat -c %Y)
# ... 计算实际墙钟时间

echo "Total tests: $TOTAL_TESTS"
echo "Total CPU time: ${TOTAL_TIME}s"
echo "Average per test: ${AVG_TIME}s"
echo ""

# 找出瓶颈测试
echo "=== Top 5 Slowest Tests ==="
grep -h '<testcase' $REPORT_DIR/TEST-*.xml | \
    awk -F'"' '{
        for(i=1; i<=NF; i++) {
            if($i ~ /classname=/) classname=$(i+1);
            if($i ~ / name=/) name=$(i+1);
            if($i ~ /time=/) time=$(i+1);
        }
        print time " " classname "." name
    }' | sort -rn | head -5
```

---

## 结论

**最简单有效的自适应方案**:

```xml
<perCoreThreadCount>true</perCoreThreadCount>
<threadCountMethods>0.5</threadCountMethods>
```

这将自动根据机器的 CPU 核心数调整并行度，无需手动配置，适用于各种环境。
