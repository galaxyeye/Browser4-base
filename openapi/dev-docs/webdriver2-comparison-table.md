# W3C WebDriver2 vs Browser4 API 快速对照表

> **更新日期**: 2026-01-20
> **用途**: 快速查看 Browser4 对 W3C WebDriver2 标准的支持情况
> **详细分析**: 请参考 `webdriver2-gap-analysis.md` 和 `webdriver2-gap-analysis.zh.md`

---

## 图例

| 符号 | 含义 |
|------|------|
| ✅ | 已实现，完全兼容 W3C 标准 |
| ⚠️ | 已实现，但路径或语义与 W3C 有差异 |
| 🔵 | Browser4 扩展功能（非 W3C 标准）|
| ❌ | 未实现 |
| P0 | 优先级 - 关键功能 |
| P1 | 优先级 - 重要功能 |
| P2 | 优先级 - 有用功能 |
| P3 | 优先级 - 高级功能 |

---

## 1. Sessions (会话管理)

| 方法 | W3C 标准路径 | Browser4 状态 | Browser4 路径 | 优先级 |
|------|-------------|---------------|---------------|--------|
| POST | `/session` | ✅ | `/session` | - |
| DELETE | `/session/{sessionId}` | ✅ | `/session/{sessionId}` | - |
| GET | `/status` | ❌ | - | P0 |
| GET | `/session/{sessionId}` | 🔵 扩展 | `/session/{sessionId}` | - |

**覆盖率**: 2/3 = 67%

---

## 2. Navigation (导航)

| 方法 | W3C 标准路径 | Browser4 状态 | Browser4 路径 | 优先级 |
|------|-------------|---------------|---------------|--------|
| POST | `/session/{sessionId}/url` | ✅ | `/session/{sessionId}/url` | - |
| GET | `/session/{sessionId}/url` | ⚠️ | `/session/{sessionId}/url` | - |
| POST | `/session/{sessionId}/back` | ❌ | - | P1 |
| POST | `/session/{sessionId}/forward` | ❌ | - | P1 |
| POST | `/session/{sessionId}/refresh` | ❌ | - | P1 |
| GET | `/session/{sessionId}/title` | ❌ | - | P0 |
| GET | - | 🔵 扩展 | `/session/{sessionId}/documentUri` | - |
| GET | - | 🔵 扩展 | `/session/{sessionId}/baseUri` | - |

**覆盖率**: 2/6 = 33%
**注意**: `GET /url` 返回会话存储的 URL，而非浏览器实时 URL

---

## 3. Contexts (窗口与框架)

| 方法 | W3C 标准路径 | Browser4 状态 | 优先级 |
|------|-------------|---------------|--------|
| GET | `/session/{sessionId}/window` | ❌ | P2 |
| DELETE | `/session/{sessionId}/window` | ❌ | P2 |
| POST | `/session/{sessionId}/window` | ❌ | P2 |
| GET | `/session/{sessionId}/window/handles` | ❌ | P2 |
| POST | `/session/{sessionId}/window/new` | ❌ | P2 |
| POST | `/session/{sessionId}/frame` | ❌ | P2 |
| POST | `/session/{sessionId}/frame/parent` | ❌ | P2 |
| GET | `/session/{sessionId}/window/rect` | ❌ | P2 |
| POST | `/session/{sessionId}/window/rect` | ❌ | P2 |
| POST | `/session/{sessionId}/window/maximize` | ❌ | P2 |
| POST | `/session/{sessionId}/window/minimize` | ❌ | P2 |
| POST | `/session/{sessionId}/window/fullscreen` | ❌ | P2 |

**覆盖率**: 0/12 = 0%
**影响**: 无法处理多窗口、标签页、iframe 场景

---

## 4. Elements - Finding (元素查找)

| 方法 | W3C 标准路径 | Browser4 状态 | Browser4 路径 | 优先级 |
|------|-------------|---------------|---------------|--------|
| POST | `/session/{sessionId}/element` | ✅ | `/session/{sessionId}/element` | - |
| POST | `/session/{sessionId}/elements` | ✅ | `/session/{sessionId}/elements` | - |
| POST | `/session/{sessionId}/element/{elementId}/element` | ❌ | - | P2 |
| POST | `/session/{sessionId}/element/{elementId}/elements` | ❌ | - | P2 |
| GET | `/session/{sessionId}/element/active` | ❌ | - | P3 |

**Browser4 扩展 (Selector-First)**:
| 方法 | Browser4 路径 | 说明 |
|------|---------------|------|
| POST | `/session/{sessionId}/selectors/exists` | 🔵 检查选择器存在性 |
| POST | `/session/{sessionId}/selectors/waitFor` | 🔵 等待选择器出现 |
| POST | `/session/{sessionId}/selectors/element` | 🔵 通过选择器查找元素 |
| POST | `/session/{sessionId}/selectors/elements` | 🔵 通过选择器查找多个元素 |

**覆盖率**: 2/5 = 40%

---

## 5. Elements - Interaction (元素交互)

| 方法 | W3C 标准路径 | Browser4 状态 | Browser4 路径 | 优先级 |
|------|-------------|---------------|---------------|--------|
| POST | `/session/{sessionId}/element/{elementId}/click` | ✅ | `/session/{sessionId}/element/{elementId}/click` | - |
| POST | `/session/{sessionId}/element/{elementId}/clear` | ❌ | - | P1 |
| POST | `/session/{sessionId}/element/{elementId}/value` | ✅ | `/session/{sessionId}/element/{elementId}/value` | - |
| GET | `/session/{sessionId}/element/{elementId}/text` | ✅ | `/session/{sessionId}/element/{elementId}/text` | - |
| GET | `/session/{sessionId}/element/{elementId}/name` | ❌ | - | P1 |
| GET | `/session/{sessionId}/element/{elementId}/attribute/{name}` | ✅ | `/session/{sessionId}/element/{elementId}/attribute/{name}` | - |
| GET | `/session/{sessionId}/element/{elementId}/property/{name}` | ❌ | - | P2 |
| GET | `/session/{sessionId}/element/{elementId}/css/{propertyName}` | ❌ | - | P1 |
| GET | `/session/{sessionId}/element/{elementId}/selected` | ❌ | - | P1 |
| GET | `/session/{sessionId}/element/{elementId}/enabled` | ❌ | - | P1 |
| GET | `/session/{sessionId}/element/{elementId}/displayed` | ❌ | - | P1 |
| GET | `/session/{sessionId}/element/{elementId}/rect` | ❌ | - | P1 |
| GET | `/session/{sessionId}/element/{elementId}/screenshot` | ❌ | - | P1 |

**Browser4 扩展 (Selector-First)**:
| 方法 | Browser4 路径 | 说明 |
|------|---------------|------|
| POST | `/session/{sessionId}/selectors/click` | 🔵 直接点击选择器 |
| POST | `/session/{sessionId}/selectors/fill` | 🔵 直接填充输入框 |
| POST | `/session/{sessionId}/selectors/press` | 🔵 按键操作 |
| POST | `/session/{sessionId}/selectors/outerHtml` | 🔵 获取外部 HTML |
| POST | `/session/{sessionId}/selectors/screenshot` | 🔵 选择器截图 |

**覆盖率**: 4/13 = 31%

---

## 6. Document (文档)

| 方法 | W3C 标准路径 | Browser4 状态 | 优先级 |
|------|-------------|---------------|--------|
| GET | `/session/{sessionId}/source` | ❌ | P0 |

**覆盖率**: 0/1 = 0%

---

## 7. Cookies (Cookie 管理)

| 方法 | W3C 标准路径 | Browser4 状态 | 优先级 |
|------|-------------|---------------|--------|
| GET | `/session/{sessionId}/cookie` | ❌ | P0 |
| GET | `/session/{sessionId}/cookie/{name}` | ❌ | P0 |
| POST | `/session/{sessionId}/cookie` | ❌ | P0 |
| DELETE | `/session/{sessionId}/cookie/{name}` | ❌ | P0 |
| DELETE | `/session/{sessionId}/cookie` | ❌ | P0 |

**覆盖率**: 0/5 = 0%
**影响**: 无法管理登录状态和会话

---

## 8. Timeouts (超时配置)

| 方法 | W3C 标准路径 | Browser4 状态 | 优先级 |
|------|-------------|---------------|--------|
| GET | `/session/{sessionId}/timeouts` | ❌ | P0 |
| POST | `/session/{sessionId}/timeouts` | ❌ | P0 |

**覆盖率**: 0/2 = 0%
**注意**: 部分端点有独立的 timeout 参数（如 `waitFor`）

---

## 9. Actions (用户交互序列)

| 方法 | W3C 标准路径 | Browser4 状态 | 优先级 |
|------|-------------|---------------|--------|
| POST | `/session/{sessionId}/actions` | ❌ | P3 |
| DELETE | `/session/{sessionId}/actions` | ❌ | P3 |

**覆盖率**: 0/2 = 0%
**影响**: 无法执行复杂交互序列（拖拽、悬停、组合键等）

---

## 10. User Prompts (对话框)

| 方法 | W3C 标准路径 | Browser4 状态 | 优先级 |
|------|-------------|---------------|--------|
| POST | `/session/{sessionId}/alert/dismiss` | ❌ | P0 |
| POST | `/session/{sessionId}/alert/accept` | ❌ | P0 |
| GET | `/session/{sessionId}/alert/text` | ❌ | P0 |
| POST | `/session/{sessionId}/alert/text` | ❌ | P0 |

**覆盖率**: 0/4 = 0%
**影响**: 无法处理 alert/confirm/prompt 弹窗

---

## 11. Screen Capture (截图)

| 方法 | W3C 标准路径 | Browser4 状态 | Browser4 路径 | 优先级 |
|------|-------------|---------------|---------------|--------|
| GET | `/session/{sessionId}/screenshot` | ❌ | - | P1 |
| GET | `/session/{sessionId}/element/{elementId}/screenshot` | ❌ | - | P1 |

**Browser4 扩展**:
| 方法 | Browser4 路径 | 说明 |
|------|---------------|------|
| POST | `/session/{sessionId}/selectors/screenshot` | 🔵 通过选择器截图（POST 方式）|

**覆盖率**: 0/2 = 0%（标准方式）
**注意**: Browser4 提供了不同的截图方式

---

## 12. Print (打印)

| 方法 | W3C 标准路径 | Browser4 状态 | 优先级 |
|------|-------------|---------------|--------|
| POST | `/session/{sessionId}/print` | ❌ | P4 |

**覆盖率**: 0/1 = 0%

---

## 13. Script Execution (脚本执行)

| 方法 | W3C 标准路径 | Browser4 状态 | Browser4 路径 |
|------|-------------|---------------|---------------|
| POST | `/session/{sessionId}/execute/sync` | ✅ | `/session/{sessionId}/execute/sync` |
| POST | `/session/{sessionId}/execute/async` | ✅ | `/session/{sessionId}/execute/async` |

**覆盖率**: 2/2 = 100% ✅
**这是唯一完全兼容的模块**

---

## 14. Browser4 独有扩展功能 🚀

### 14.1 Agent (AI 自动化)

| 方法 | Browser4 路径 | 说明 |
|------|---------------|------|
| POST | `/session/{sessionId}/agent/run` | 🔵 运行自主任务 |
| POST | `/session/{sessionId}/agent/observe` | 🔵 观察页面 |
| POST | `/session/{sessionId}/agent/act` | 🔵 执行单个动作 |
| POST | `/session/{sessionId}/agent/extract` | 🔵 提取结构化数据 |
| POST | `/session/{sessionId}/agent/summarize` | 🔵 总结页面内容 |
| POST | `/session/{sessionId}/agent/clearHistory` | 🔵 清除历史 |

### 14.2 Pulsar (高级页面加载)

| 方法 | Browser4 路径 | 说明 |
|------|---------------|------|
| POST | `/session/{sessionId}/normalize` | 🔵 标准化 URL |
| POST | `/session/{sessionId}/open` | 🔵 立即打开（绕过缓存）|
| POST | `/session/{sessionId}/load` | 🔵 智能加载（缓存控制）|
| POST | `/session/{sessionId}/submit` | 🔵 提交到爬取队列 |

### 14.3 Control (执行控制)

| 方法 | Browser4 路径 | 说明 |
|------|---------------|------|
| POST | `/session/{sessionId}/control/delay` | 🔵 延迟执行 |
| POST | `/session/{sessionId}/control/pause` | 🔵 暂停会话 |
| POST | `/session/{sessionId}/control/stop` | 🔵 停止会话 |

### 14.4 Events (事件系统)

| 方法 | Browser4 路径 | 说明 |
|------|---------------|------|
| POST | `/session/{sessionId}/event-configs` | 🔵 创建事件配置 |
| GET | `/session/{sessionId}/event-configs` | 🔵 获取事件配置 |
| GET | `/session/{sessionId}/events` | 🔵 获取事件 |
| POST | `/session/{sessionId}/events/subscribe` | 🔵 订阅事件 |
| GET | `/session/{sessionId}/events/stream` | 🔵 SSE 事件流 |

---

## 总体统计

### 按模块覆盖率

| 模块 | W3C 端点数 | Browser4 已实现 | 覆盖率 | 评级 |
|------|------------|----------------|--------|------|
| Sessions | 3 | 2 | 67% | 🟡 |
| Navigation | 6 | 2 | 33% | 🔴 |
| Contexts | 12 | 0 | 0% | 🔴 |
| Elements - Finding | 5 | 2 | 40% | 🔴 |
| Elements - Interaction | 13 | 4 | 31% | 🔴 |
| Document | 1 | 0 | 0% | 🔴 |
| Cookies | 5 | 0 | 0% | 🔴 |
| Timeouts | 2 | 0 | 0% | 🔴 |
| Actions | 2 | 0 | 0% | 🔴 |
| User Prompts | 4 | 0 | 0% | 🔴 |
| Screen Capture | 2 | 0 | 0% | 🔴 |
| Print | 1 | 0 | 0% | 🔴 |
| **Script Execution** | **2** | **2** | **100%** | **🟢** |
| **总计** | **58** | **12** | **21%** | **🔴** |

### 按优先级缺失统计

| 优先级 | 缺失端点数 | 主要模块 |
|--------|------------|----------|
| P0 | ~16 | Cookies, Timeouts, Alerts, Document |
| P1 | ~10 | Navigation, Elements |
| P2 | ~15 | Contexts, Elements |
| P3 | ~5 | Actions, Active Element |
| P4 | ~2 | Print |

### Browser4 扩展统计

| 扩展类别 | 端点数 | 核心价值 |
|----------|--------|----------|
| Selectors | 9 | 简化元素操作 |
| Agent | 6 | AI 驱动自动化 |
| Pulsar | 4 | 高级页面加载 |
| Control | 3 | 执行流程控制 |
| Events | 5 | 事件监听订阅 |
| **总计** | **27** | **差异化竞争力** |

---

## 改进路线图

### 第一阶段（1-3 个月）- 达到 50% 覆盖率

**目标**: 补齐 P0 优先级端点

- [ ] Cookie 管理（5 个端点）
- [ ] 超时配置（2 个端点）
- [ ] 对话框处理（4 个端点）
- [ ] 页面源码（1 个端点）
- [ ] 页面标题（1 个端点）
- [ ] 服务器状态（1 个端点）
- [ ] 基础导航（2 个端点：back/forward）

**新增**: 16 个端点
**预期覆盖率**: 28/58 = **48%**

### 第二阶段（3-6 个月）- 达到 70% 覆盖率

**目标**: 补齐 P1 优先级端点

- [ ] 窗口管理（12 个端点）
- [ ] 元素状态查询（5 个端点）
- [ ] 标准截图（2 个端点）
- [ ] 元素清除（1 个端点）

**新增**: 20 个端点
**预期覆盖率**: 48/58 = **83%**

### 第三阶段（6-12 个月）- 达到 90%+ 覆盖率

**目标**: 补齐 P2/P3 优先级端点

- [ ] Actions API（2 个端点）
- [ ] 子元素查找（2 个端点）
- [ ] 元素属性完整性（3 个端点）
- [ ] 活动元素（1 个端点）

**新增**: 8 个端点
**预期覆盖率**: 56/58 = **97%**

---

## 使用建议

### 对于新用户

**如果你的场景是**：
- ✅ 基础页面自动化 → Browser4 基本满足
- ✅ 需要 AI 辅助 → 使用 Browser4 Agent API
- ✅ 大规模爬取 → 使用 Browser4 Pulsar API
- ❌ 需要多窗口管理 → 暂不支持
- ❌ 需要 Cookie 管理 → 暂不支持
- ❌ 需要处理弹窗 → 暂不支持

### 对于从 Selenium 迁移

**可直接迁移的功能**：
- ✅ 会话管理
- ✅ 基础导航（navigate, getCurrentUrl）
- ✅ 元素查找和点击
- ✅ 脚本执行

**需要等待的功能**：
- ❌ Cookie 管理
- ❌ 多窗口/iframe
- ❌ Actions 链
- ❌ Alert 处理

**可替代方案**：
- 🔵 使用 Selectors API 替代多步元素查找
- 🔵 使用 Agent API 实现智能化操作

---

**更新记录**:
- 2026-01-20: v1.0 初始版本

**相关文档**:
- 详细分析: `webdriver2-gap-analysis.md` (英文)
- 简版分析: `webdriver2-gap-analysis.zh.md` (中文)
- API 规范: `openapi.yaml`
