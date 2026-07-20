---
name: dev-guide
description: "基于设计文档的开发流程：从设计文档到代码实现的完整规范。适用于已审查通过的设计文档（含 PRD），指导影响范围评估、代码实现、文档同步。"
---

# 基于设计文档的开发流程

## 边界 / 不适用场景

- 已有模块的 bug 修复（不涉及新模块创建）→ 直接修复即可
- **设计文档尚未编写或审查** → 应先使用 `design-write` skill 完成设计文档

## 不做什么

- 不替代具体业务逻辑的实现细节
- 不编写或审查设计文档（使用 `design-write` 和 `design-review` skill）

---

## 一、执行流程

### 阶段 1：读取设计文档与规范

**必须先读取**：

```bash
# 1. 设计文档（必须）
Read: spec/{需求目录}/design.md
Read: spec/{需求目录}/prd.md  # 如果存在

# 2. 后端开发规范
Read: ../../dev-guide/后端规范.md

# 3. 数据库规范（如涉及数据库变更）
Read: ../../dev-guide/数据库规范.md
```

**设计文档应包含**：
- 需求背景与目标
- 架构设计（模块拆分、职责划分）
- 接口设计（RESTful API、参数、响应结构）
- 数据模型（表结构、字段、索引）
- 异常处理与边界场景
- 安全设计（鉴权、SQL 注入防护等）

### 阶段 2：影响范围评估

**在开始编码前，输出树状文件影响清单，等待用户确认**：

```
功能名称
│
├── 📗 新增文件（N 个）
│   ├── 后端（N 个）
│   │   └── com/cssz/modules/{模块名}/
│   │       ├── entity/XxxEntity.java
│   │       ├── mapper/XxxMapper.java
│   │       ├── service/IXxxService.java
│   │       ├── service/impl/XxxServiceImpl.java
│   │       └── controller/XxxController.java
│   ├── 前端（N 个）
│   │   └── src/components/Xxx.vue
│   └── 数据库（N 个）
│       └── sql/V{date}_{seq}__{module}_{action}.sql
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
│   ├── 后端（5 个）
│   │   └── com/cssz/modules/file/
│   │       ├── entity/SysAttachment.java
│   │       ├── mapper/SysAttachmentMapper.java
│   │       ├── service/ISysAttachmentService.java
│   │       ├── service/impl/SysAttachmentServiceImpl.java
│   │       └── controller/SysFileController.java
│   ├── 前端（1 个）
│   │   └── src/utils/common/fileHelper.ts
│   └── 数据库（1 个）
│       └── sql/V20260720_0__attachment_create_sys_attachment.sql
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

**用户确认后，方可进入编码阶段**。

### 阶段 3：代码实现

按照 `后端规范.md` 和 `数据库规范.md` 中的规范实现代码，重点关注：

- 分层规范（Entity、Mapper、Service、Controller 的基类与注解）
- 数据库脚本命名与表设计规范
- 通用响应类、权限控制、文件存储等公共能力的使用

### 阶段 4：开发后收尾

- [ ] 同步更新设计文档（如有变更）
- [ ] 更新 `implementation-notes.md`，记录关键决策、权衡、未覆盖场景
- [ ] 测试验证核心流程
- [ ] 检查代码是否符合分层规范与注解要求

---

## 二、开发检查清单

### 开发前

- [ ] 设计文档已审查通过（无 P0 阻塞问题）
- [ ] 输出影响范围评估，用户已确认
- [ ] SQL 脚本按命名规范创建
- [ ] 确认后端文件放在 `com/cssz/modules/{模块名}` 下

### 开发中

- [ ] Entity 继承 `CsEntity`
- [ ] Mapper 继承 `BaseMapper<Entity>`
- [ ] Service 接口继承 `IService<Entity>`
- [ ] Service 实现继承 `ServiceImpl<Mapper, Entity>`
- [ ] Controller 使用 `@RestController` 和 `@RequestMapping`
- [ ] Controller 使用 `@RequiresPermissions` 控制权限
- [ ] Controller 使用 `Result<T>` 返回统一格式
- [ ] 使用 `@Schema` 添加接口文档
- [ ] 使用 `@Slf4j` 记录日志
- [ ] 事务方法添加 `@Transactional`

### 开发后

- [ ] 设计文档与实现同步更新
- [ ] `implementation-notes.md` 记录关键决策
- [ ] 测试验证核心流程

---

## 三、新模块开发检查清单

创建新业务模块时，额外需要完成：

- [ ] 在 `jeecg-system-biz/src/main/java/com/cssz/modules/` 下创建模块目录
- [ ] 创建包结构：`com.cssz.modules.{模块名}`
- [ ] 创建 Entity、Mapper、Service、Controller 层
- [ ] 配置权限标识（`@RequiresPermissions`）
- [ ] 添加必要的配置项到 `application.yml`
- [ ] 执行数据库建表 SQL
- [ ] 在系统权限管理中添加权限

---

## 四、相关文档

- [后端开发规范](../../dev-guide/后端规范.md)
- [数据库规范](../../dev-guide/数据库规范.md)
