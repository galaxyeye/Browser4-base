# 🚀 快速参考指南

## ⚡ 常用命令

```bash
# 所有 Maven 测试（推荐用于 CI/CD）
./test.sh all
./test.ps1 all

# 快速开发测试
./test.sh fast

# 集成测试
./test.sh it

# 特定模块
./test.sh fast -pl pulsar-core

# SDK 测试
./test.sh python-sdk
./test.sh nodejs-sdk

# 混合测试
./test.sh all python-sdk
```

## 📊 性能对比

| 命令 | 编译次数 | 改进 |
|-----|--------|------|
| `test.sh all` | 1 (之前 5) | **-80%** ↓ |
| `test.sh fast it` | 1 (之前 2) | **-50%** ↓ |

## 🔍 文件位置

**修改的文件**:
- `bin/test.sh` (284 行) - Bash 版本
- `bin/test.ps1` (273 行) - PowerShell 版本

**新增文档**:
- `FINAL_SUMMARY.md` - 完整总结
- `OPTIMIZATION_SUMMARY.md` - 技术总结
- `bin/USAGE_GUIDE.md` - 使用指南
- `bin/COMPLETION_REPORT.md` - 完成报告
- `bin/VERIFICATION_CHECKLIST.md` - 验证清单
- `bin/TEST_OPTIMIZATION.md` - 优化细节

## ✨ 关键特性

✅ 单个 Maven 命令（从 5 个减少到 1 个）
✅ 即时失败退出（不继续执行失败后的测试）
✅ 完全向后兼容（所有原有命令都有效）
✅ 支持所有 Maven 参数
✅ 支持混合 Maven + SDK 测试

## 🛠️ 故障排除

| 问题 | 解决方案 |
|------|---------|
| Command not found | `chmod +x bin/test.sh` |
| Maven 未找到 | 确保在项目根目录运行 |
| Python 不可用 | `pip install pytest` |
| Node.js 不可用 | `npm install` |

## 📖 深入了解

- 完整用法: 见 `bin/USAGE_GUIDE.md`
- 技术细节: 见 `FINAL_SUMMARY.md`
- 故障排除: 见 `bin/USAGE_GUIDE.md` 中的问题排查部分

---

**更新**: 2026-02-15 | **状态**: ✅ 生产就绪

