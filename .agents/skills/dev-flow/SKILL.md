---
name: dev-flow
description: "当用户要求根据设计文档/PRD/需求进行开发时，必须调用此 skill。触发词：根据文档开发、开始开发、实现功能、按设计文档开发。"
---

# 基于设计文档的开发流程

## 边界 / 不适用场景

- 已有模块的 bug 修复（不涉及新模块创建）→ 直接修复即可
- **设计文档尚未编写或审查** → 应先使用 `design-write` skill 完成设计文档

## 不做什么

- 不替代具体业务逻辑的实现细节
- 不编写或审查设计文档（使用 `design-write` 和 `design-review` skill）

---

## 执行流程

> 严格按阶段顺序执行，禁止跳步或合并阶段。每个阶段有明确的门禁条件，通过后才能进入下一阶段。

### 阶段 1：读取设计文档与影响范围评估

**目的**：充分理解需求与设计，输出完整的文件影响清单，供用户确认后再动手编码。

**必须读取**：

```bash
# 1. 设计文档（必须）
Read: spec/{需求目录}/design.md
Read: spec/{需求目录}/prd.md  # 如果存在

# 2. 开发规范（必须）
Read: dev-guide/后端规范.md
Read: dev-guide/数据库规范.md

# 3. 其他相关规范（按需）：在 `dev-guide/index.md` 检索相关文档
```

**读取后，向用户输出**：
- 简要总结需求目标与核心功能点
- 列出设计文档中的关键设计决策（模块划分、接口、数据模型等）
- 标注设计文档中不清晰或可能影响实现的问题
- 输出树状文件影响清单（格式见下方）

**树状清单格式**：

```
功能名称
│
├── 📗 新增文件（N 个）
│   ├── 数据库（N 个）
│   │   └── db/V{date}_{seq}__{module}_{action}.sql
│   ├── 后端（N 个）
│   │   └── com/cssz/modules/{模块名}/
│   │       ├── entity/XxxEntity.java
│   │       ├── mapper/XxxMapper.java
│   │       ├── service/IXxxService.java
│   │       ├── service/impl/XxxServiceImpl.java
│   │       └── controller/XxxController.java
│   └── 前端（N 个）
│       └── src/components/Xxx.vue
│
├── 📝 修改文件（N 个）
│   ├── 后端（N 个）
│   └── 前端（N 个）
│
├── 🗄️ 数据库变更
│   └── 新建表/修改表/新增字段
│
└── ❌ 删除文件
    └── 无（或列出要删除的文件）
```

**示例**：

```
附件托管模式改造
│
├── 📗 新增文件（7 个）
│   ├── 数据库（1 个）
│   │   └── db/V20260720_0__attachment_create_sys_attachment.sql
│   ├── 后端（5 个）
│   │   └── com/cssz/modules/file/
│   │       ├── entity/SysAttachment.java
│   │       ├── mapper/SysAttachmentMapper.java
│   │       ├── service/ISysAttachmentService.java
│   │       ├── service/impl/SysAttachmentServiceImpl.java
│   │       └── controller/SysFileController.java
│   └── 前端（2 个）
│       └── src/components/cssz/CsUpload/
│           ├── CsUpload.vue
│           └── index.ts
│
├── 📝 修改文件（3 个）
│   └── 前端
│       ├── JUpload.vue
│       ├── api/common/api.ts
│       └── compUtils.ts
│
├── 🗄️ 数据库变更
│   └── 新建表 sys_attachment
│
└── ❌ 删除文件
    └── 无
```

**门禁**：用户确认影响范围 → 进入阶段 2。

### 阶段 2：代码实现

> **必须按以下子步骤顺序执行，禁止并行或跳步。** 每步完成后向用户汇报进展，确认后再继续下一步。

#### 步骤 2.1：生成数据库脚本

**依据**：`dev-guide/数据库规范.md`

**门禁**：SQL 脚本已生成，符合命名与表设计规范 → 进入步骤 2.2。

#### 步骤 2.2：编写后端代码

**依据**：`dev-guide/后端规范.md`

**门禁**：代码符合分层规范与注解要求 → 进入步骤 2.3。

#### 步骤 2.3：编写前端代码

**要求**：
- 根据设计文档中的接口定义，调用后端 API
- 遵循项目现有的前端代码风格与组件结构

**门禁**：前后端接口对接完成，核心流程可走通 → 进入阶段 3。

### 阶段 3：开发后收尾

- [ ] 同步更新设计文档（如有变更）
- [ ] 更新 `implementation-notes.md`，记录关键决策、权衡、未覆盖场景
- [ ] 检查代码是否符合分层规范与注解要求

---

## 相关文档

- [后端开发规范](../../dev-guide/后端规范.md)
- [数据库规范](../../dev-guide/数据库规范.md)
- [附件上传规范](../../dev-guide/attachment-guide.md)
