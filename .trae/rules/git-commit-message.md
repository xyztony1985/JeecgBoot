---
alwaysApply: false
description: 生成 git commit message 时生效
scene: git_message
---
# Git 提交信息规范

遵循 [Conventional Commits](https://www.conventionalcommits.org/) 规范，提交信息使用中文。

## 格式

```
<类型>(<作用域>): <简要描述>

[可选的详细说明]

[可选的关联信息]
```

## 类型

| 类型 | 说明 |
|------|------|
| feat | 新功能 |
| fix | 修复缺陷 |
| refactor | 代码重构（不新增功能、不修复缺陷） |
| perf | 性能优化 |
| style | 代码格式调整（不影响逻辑） |
| docs | 文档变更 |
| test | 测试相关 |
| build | 构建系统或外部依赖变更 |
| ci | CI/CD 配置变更 |
| chore | 其他杂项 |
| revert | 回滚提交 |

## 规则

1. **标题行**（第一行）不超过 50 个字符，使用祈使语气，末尾不加句号
2. **作用域**为可选项，用于标注影响范围，如 `auth`、`user`、`order`
3. 标题行与详细说明之间**空一行**
4. **详细说明**每行不超过 72 个字符，解释"做了什么"和"为什么"，而非"怎么做"
5. 关联信息使用 `Closes #123`、`Refs #456` 格式
6. 破坏性变更须在标题行加 `BREAKING CHANGE:` 前缀，或在详细说明中以 `BREAKING CHANGE:` 开头说明

## 示例

```
feat(user): 新增用户导出 Excel 功能

支持按部门筛选后批量导出，使用 EasyExcel 流式写入避免大数量内存溢出。

Closes #234
```

```
fix(auth): 修复 Token 过期后未跳转登录页的问题

全局响应拦截器缺少 401 状态码处理，导致页面白屏。
补充拦截逻辑，遇到 401 自动清除缓存并跳转登录页。
```

```
refactor: 抽取公共分页查询方法

将多个 Controller 中重复的分页参数解析逻辑统一提取到 BaseController。
```
