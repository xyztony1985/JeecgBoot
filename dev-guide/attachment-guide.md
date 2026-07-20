# 附件上传使用规范

> 本文档整理 JeecgBoot 项目中附件上传的完整链路：前端组件 → 后端接口 → 存储方式 → 数据库设计，供开发新功能时参考。

---

## 一、整体架构概览

```
┌─────────────────────────────────────────────────────────────────────┐
│  前端                                                                │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐              │
│  │ JUpload      │  │ JImageUpload │  │ BasicUpload  │              │
│  │ (文件上传)    │  │ (图片上传)    │  │ (通用上传)    │              │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘              │
│         │                 │                 │                       │
│         └─────────────────┼─────────────────┘                       │
│                           │ POST /sys/common/upload                 │
│                           │ FormData: file + biz                    │
└───────────────────────────┼─────────────────────────────────────────┘
                            │
┌───────────────────────────┼─────────────────────────────────────────┐
│  后端                       ▼                                        │
│  ┌────────────────────────────────────────┐                         │
│  │ CommonController.upload()              │                         │
│  │   ├─ uploadType=local → uploadLocal()  │                         │
│  │   ├─ uploadType=minio → MinioUtil      │                         │
│  │   └─ uploadType=alioss → OssBootUtil   │                         │
│  └────────────────────────────────────────┘                         │
│                           │                                         │
│                           ▼                                         │
│  ┌──────────┐  ┌──────────────┐  ┌──────────────┐                 │
│  │ 本地磁盘  │  │ MinIO        │  │ 阿里云 OSS   │                 │
│  │ /opt/    │  │ 对象存储      │  │ 对象存储      │                 │
│  │ upFiles  │  │              │  │              │                 │
│  └──────────┘  └──────────────┘  └──────────────┘                 │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 二、前端上传组件

### 2.1 组件选型

| 组件 | 路径 | 适用场景 |
|------|------|---------|
| **JUpload** | `src/components/Form/src/jeecg/components/JUpload/JUpload.vue` | 通用文件上传（表单内嵌） |
| **JImageUpload** | `src/components/Form/src/jeecg/components/JImageUpload.vue` | 图片上传（表单内嵌） |
| **BasicUpload** | `src/components/Upload/src/BasicUpload.vue` | 通用上传（弹窗模式） |

### 2.2 统一上传地址

所有上传组件默认调用同一个后端接口：

```typescript
// src/api/common/api.ts
export const uploadUrl = `${baseUploadUrl}/sys/common/upload`;
```

### 2.3 JUpload 使用示例

```vue
<template>
  <JUpload v-model:value="formData.attachment" bizPath="myModule/doc" />
</template>
```

**关键 Props：**

| Prop | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `value` | `string \| string[]` | - | 绑定值，逗号分隔的路径字符串 |
| `bizPath` | `string` | `'temp'` | 业务路径，控制文件存储的子目录 |
| `fileType` | `string` | `'all'` | 文件类型：`'all'` 或 `'image'` |
| `returnUrl` | `boolean` | `true` | `true` 返回 URL 字符串；`false` 返回 `{fileName, filePath, fileSize}[]` |
| `maxCount` | `number` | `0` | 最大上传数量，0 为不限 |
| `multiple` | `boolean` | `true` | 是否允许多文件 |

### 2.4 JImageUpload 使用示例

```vue
<template>
  <JImageUpload v-model:value="formData.avatar" bizPath="user/avatar" />
</template>
```

**关键 Props：**

| Prop | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `value` | `string` | - | 绑定值 |
| `bizPath` | `string` | `'temp'` | 业务路径 |
| `listType` | `string` | `'picture-card'` | 展示样式 |
| `fileMax` | `number` | `1` | 最大上传数量 |
| `disabled` | `boolean` | `false` | 是否禁用 |

### 2.5 上传返回值格式

后端接口返回的 JSON：

```json
{
  "success": true,
  "message": "upload/doc/report_1721123456789.pdf"
}
```

- `message` 字段即为文件的存储路径/URL
- 前端组件将此值写入 `v-model:value` 绑定的字段

### 2.6 文件预览/下载地址转换

前端通过 `getFileAccessHttpUrl()` 将存储路径转换为可访问的 HTTP URL：

```typescript
// src/utils/common/compUtils.ts
export const getFileAccessHttpUrl = (fileUrl, prefix = 'http') => {
  if (fileUrl && !fileUrl.startsWith(prefix)) {
    // 相对路径 → 拼接后端代理地址
    result = `${baseApiUrl}/sys/common/static/${fileUrl}`;
  }
  // 已是完整 URL（MinIO/OSS）→ 直接返回
  return result;
};
```

**转换规则：**

| 存储值 | 转换后 URL |
|--------|-----------|
| `upload/doc/report.pdf`（相对路径） | `http://localhost:8080/jeecg-boot/sys/common/static/upload/doc/report.pdf` |
| `http://minio:9000/bucket/upload/doc/report.pdf`（绝对 URL） | 原样返回 |

---

## 三、后端上传接口

### 3.1 统一入口

**接口地址：** `POST /sys/common/upload`

**Controller：** `org.jeecg.modules.system.controller.CommonController`

**请求参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `file` | `MultipartFile` | 是 | 上传的文件 |
| `biz` | `string` | 否 | 业务路径（子目录），默认 `temp` |

### 3.2 调度逻辑

```java
// 读取配置：jeecg.uploadType
@Value("${jeecg.uploadType}")
private String uploadType;

@PostMapping("/upload")
public Result<?> upload(HttpServletRequest request, ...) {
    String bizPath = request.getParameter("biz");
    MultipartFile file = ...;

    // 1. 安全校验
    SsrfFileTypeFilter.checkUploadFileType(file, bizPath);

    // 2. 根据配置分发
    if ("local".equals(uploadType)) {
        savePath = this.uploadLocal(file, bizPath);     // 本地存储
    } else {
        savePath = CommonUtils.upload(file, bizPath, uploadType);  // MinIO / OSS
    }

    // 3. 返回存储路径（不入库！）
    return Result.OK(savePath);
}
```

### 3.3 三种存储方式对比

| 维度 | local（本地） | minio | alioss（阿里云 OSS） |
|------|--------------|-------|---------------------|
| **savePath 格式** | 相对路径 | 绝对 URL | 绝对 URL |
| **示例** | `upload/doc/xxx_123.pdf` | `http://minio:9000/bucket/upload/doc/xxx_123.pdf` | `https://bucket.oss-cn-xxx.aliyuncs.com/upload/doc/xxx_123.pdf` |
| **配置项** | `jeecg.path.upload` | `jeecg.minio.*` | `jeecg.oss.*` |
| **预览方式** | 后端代理 `/sys/common/static/**` | 直接访问 URL | 直接访问 URL |
| **工具类** | `CommonController.uploadLocal()` | `MinioUtil` | `OssBootUtil` |

### 3.4 配置文件示例

```yaml
jeecg:
  # 上传类型：local / minio / alioss
  uploadType: local
  # 本地存储根目录
  path:
    upload: /opt/upFiles
  # MinIO 配置
  minio:
    minio_url: http://minio.jeecg.com
    minio_name: ??
    minio_pass: ??
    bucketName: otatest
  # 阿里云 OSS 配置
  oss:
    endpoint: oss-cn-beijing.aliyuncs.com
    accessKey: ??
    secretKey: ??
    bucketName: jeecgdev
    staticDomain: ??   # 自定义域名（可选）
```

### 3.5 编程式调用（后端代码中上传）

当需要在后端代码中主动上传文件时，使用 `CommonUtils` 工具类：

```java
@Value("${jeecg.uploadType}")
private String uploadType;

// 标准上传（MinIO / OSS）
String savePath = CommonUtils.upload(file, bizPath, uploadType);

// 本地上传
String savePath = CommonUtils.uploadLocal(file, bizPath, uploadpath);

// 自定义桶上传
String savePath = CommonUtils.upload(file, bizPath, uploadType, customBucket);
```

### 3.6 文件下载/预览接口

**接口地址：** `GET /sys/common/static/**`

仅用于**本地存储**模式，后端从磁盘读取文件并通过流式输出返回。

MinIO / OSS 模式下，文件通过其自身 URL 直接访问，不经过后端。

---

## 四、数据库设计

### 4.1 核心原则

**统一上传接口不会将文件信息写入任何表。** 文件路径/URL 由业务模块自行存储。

### 4.2 存储方式选择

| 场景 | 推荐方式 | 说明 |
|------|---------|------|
| 单文件字段（头像、封面图等） | 业务表直接存路径字段 | 字段类型为 `varchar(500)` |
| 多文件/附件列表 | 业务表 + 关联子表 | 子表存储每个文件的路径 |
| Online 表单/评论附件 | `sys_form_file` 关联表 | 通过 `table_name` + `table_data_id` 关联 |

### 4.3 业务表直接存储（推荐）

**大多数场景推荐在业务表中直接存储文件路径。**

```sql
-- 示例：单附件字段
ALTER TABLE my_business_table ADD COLUMN attachment varchar(500) COMMENT '附件路径';

-- 示例：多附件字段（逗号分隔）
ALTER TABLE my_business_table ADD COLUMN attachments varchar(2000) COMMENT '附件路径，逗号分隔';
```

**存储值示例：**

| uploadType | 存储值 |
|-----------|--------|
| local | `upload/doc/report_1721123456789.pdf` |
| minio | `http://minio:9000/bucket/upload/doc/report_1721123456789.pdf` |
| alioss | `https://bucket.oss-cn-xxx.aliyuncs.com/upload/doc/report_1721123456789.pdf` |

### 4.4 附件子表（多附件场景）

当业务需要管理多个附件（如每个附件有名称、类型、大小等元数据），建议创建独立的附件子表：

```sql
CREATE TABLE `my_business_attachment` (
  `id` varchar(32) NOT NULL COMMENT '主键ID',
  `business_id` varchar(32) NOT NULL COMMENT '业务数据ID',
  `file_name` varchar(200) NOT NULL COMMENT '原始文件名',
  `file_path` varchar(500) NOT NULL COMMENT '文件存储路径/URL',
  `file_size` bigint DEFAULT 0 COMMENT '文件大小（字节）',
  `file_type` varchar(20) DEFAULT NULL COMMENT '文件类型（pdf/doc/xlsx/image等）',
  `sort_order` int DEFAULT 0 COMMENT '排序号',
  `create_by` varchar(32) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  INDEX `idx_business_id` (`business_id`)
) COMMENT='业务附件表';
```

### 4.5 `sys_form_file` 关联表

这是系统已有的关联表，主要用于 **Online 表单** 和 **评论功能**：

```sql
CREATE TABLE `sys_form_file` (
  `id` varchar(32) NOT NULL,
  `table_name` varchar(50) NOT NULL COMMENT '表名',
  `table_data_id` varchar(32) NOT NULL COMMENT '数据ID',
  `file_id` varchar(32) DEFAULT NULL COMMENT '关联文件ID',
  `file_type` varchar(20) DEFAULT NULL COMMENT '文件类型',
  `create_by` varchar(32) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  INDEX `idx_table_form` (`table_name`, `table_data_id`),
  INDEX `index_file_id` (`file_id`)
);
```

**注意：** 此表只存关联关系，不存文件路径。新业务模块一般不需要直接使用此表。

---

## 五、开发规范

### 5.1 bizPath 命名规范

`bizPath` 决定文件存储的子目录，应按模块/功能划分：

```
格式：{模块名}/{功能名}

示例：
  myModule/doc         → 我的模块/文档附件
  myModule/image       → 我的模块/图片
  user/avatar          → 用户头像
  report/cover         → 报表封面
```

### 5.2 前端使用规范

1. **表单内嵌上传**：优先使用 `JUpload`（文件）或 `JImageUpload`（图片）
2. **必须指定 `bizPath`**：避免所有文件都堆在 `temp` 目录
3. **绑定值格式**：`JUpload` 默认返回逗号分隔的路径字符串（`returnUrl=true`）
4. **预览**：使用 `getFileAccessHttpUrl()` 转换路径，不要手动拼接

```vue
<!-- ✅ 正确用法 -->
<JUpload v-model:value="formData.files" bizPath="myModule/doc" />

<!-- ❌ 错误：未指定 bizPath -->
<JUpload v-model:value="formData.files" />
```

### 5.3 后端使用规范

1. **不要新建上传接口**：统一使用 `/sys/common/upload`
2. **编程式上传**：使用 `CommonUtils.upload()`，不要直接调用 `MinioUtil` 或 `OssBootUtil`
3. **上传接口不入库**：上传接口只返回路径，入库操作由业务接口完成
4. **兼容三种存储**：业务代码不应假设存储方式，路径字段统一用 `varchar(500)` 以上

```java
// ✅ 正确：通过 CommonUtils 上传
String savePath = CommonUtils.upload(file, "myModule/doc", uploadType);

// ❌ 错误：直接调用特定存储工具
String savePath = MinioUtil.upload(file, "myModule/doc");
```

### 5.4 数据库设计规范

1. **路径字段长度**：`varchar(500)` 起步（MinIO/OSS 返回完整 URL）
2. **多附件用逗号分隔**或独立子表：根据业务复杂度选择
3. **不要创建全局附件表**：文件路径直接存在业务表中
4. **保留原始文件名**：如需展示原始文件名，应额外存储 `file_name` 字段

---

## 六、完整调用链路示例

### 场景：用户在新建报表时上传一个 PDF 附件

**1. 前端**

```vue
<template>
  <a-form-item label="报表文件">
    <JUpload v-model:value="formModel.reportFile" bizPath="report/doc" />
  </a-form-item>
</template>
```

**2. 上传过程**

```
用户选择文件 → JUpload 组件自动 POST /sys/common/upload
  请求参数: file=report.pdf, biz=report/doc
  后端根据 uploadType 存储到本地/MinIO/OSS
  返回: { success: true, message: "report/doc/report_1721123456789.pdf" }
  JUpload 将 message 写入 v-model 绑定的 formModel.reportFile
```

**3. 保存业务数据**

```
用户点击保存 → POST /myReport/add
  请求体: { name: "月报", reportFile: "report/doc/report_1721123456789.pdf" }
  后端将 reportFile 字段存入业务表
```

**4. 数据库**

```sql
-- my_report 表
INSERT INTO my_report (name, report_file) VALUES ('月报', 'report/doc/report_1721123456789.pdf');
```

**5. 预览/下载**

```
前端读取 report_file 字段值
→ getFileAccessHttpUrl() 转换：
  - local 模式: http://localhost:8080/jeecg-boot/sys/common/static/report/doc/report_1721123456789.pdf
  - minio/oss 模式: 直接使用完整 URL
→ 浏览器打开链接即可预览/下载
```

---

## 七、附件托管模式（Managed Mode）

### 7.1 模式说明

附件托管模式是一种**集中管理附件**的方式，与旧模式（业务表直接存储文件路径）形成对比：

| 维度 | 旧模式 | 托管模式 |
|------|--------|----------|
| 业务表存储 | 文件路径（逗号分隔） | file_id（逗号分隔） |
| 上传接口 | `/sys/common/upload` | `/sys/file/upload` |
| 文件管理 | 业务方自行处理 | 系统统一管理 |
| 孤儿清理 | 无 | 定时任务通过 bizCode 反查 |

### 7.2 数据库硬约束

#### 表名规范

- **必须使用** `sys_attachment` 作为附件管理表名
- **禁止使用** `file` 或 `sys_file`（MySQL 保留字冲突）

#### 文件引用方式

- 业务表**必须存储 file_id**，不直接存储文件路径
- 文件路径由 `sys_attachment` 表统一管理
- 调用方通过 file_id 获取文件信息

#### 软删除校验

- **所有文件访问必须检查 `delete_time` 字段**
- `delete_time` 不为空表示已删除，禁止访问
- 防止通过旧路径访问已删除文件

### 7.3 接口硬约束

#### bizCode 规范

- **格式**：`{table_name}.{field_name}`，与数据库表名和字段名完全对应
- **校验**：必须使用正则 `^[a-zA-Z0-9_]+$` 校验 tableName 和 fieldName，防止 SQL 注入
- **推导**：前端通过 `buildBizCode(tableName, fieldName)` 自动推导

#### 接口调用规范

- **禁止 Controller 之间互相调用**
- 前端需要上传文件时，直接请求 `/sys/file/upload`
- 不要通过业务 Controller 转发上传请求

#### 上传方法拆分

- `upload()` 方法：处理非托管模式（旧模式）
- `uploadManaged()` 方法：处理托管模式（新模式）
- 两个方法职责分离，避免混淆

### 7.4 前端硬约束

#### JUpload 组件规范

- `returnUrl=false` 模式下，返回 JSON 数组必须包含 `fileId` 字段
- 示例：`[{ fileName: "xxx.pdf", filePath: "/upload/xxx.pdf", fileSize: 12345, fileId: "1234567890" }]`

#### 文件访问

- **view 接口**：使用路径参数 `/sys/file/view/{id}`，不用查询参数
- **预览**：使用 `/sys/file/view/${fileId}` 路径

### 7.5 云存储路径匹配

- 使用 MyBatis-Plus 的 `likeLeft` 进行路径匹配
- 示例：`WHERE file_path LIKE '%/upload/xxx.pdf'`
- 防止路径前缀不一致导致的安全问题

---

## 八、关键文件索引

| 文件 | 说明 |
|------|------|
| `jeecg-module-system/.../controller/CommonController.java` | 统一上传/下载入口 |
| `jeecg-boot-base-core/.../util/CommonUtils.java` | 上传工具类（静态方法） |
| `jeecg-boot-base-core/.../util/MinioUtil.java` | MinIO 工具类 |
| `jeecg-boot-base-core/.../util/oss/OssBootUtil.java` | 阿里云 OSS 工具类 |
| `jeecg-boot-base-core/.../constant/CommonConstant.java` | 上传类型常量定义 |
| `jeecgboot-vue3/src/components/Form/src/jeecg/components/JUpload/` | 文件上传组件 |
| `jeecgboot-vue3/src/components/Form/src/jeecg/components/JImageUpload.vue` | 图片上传组件 |
| `jeecgboot-vue3/src/api/common/api.ts` | 前端上传 API 定义 |
| `jeecgboot-vue3/src/utils/common/compUtils.ts` | `getFileAccessHttpUrl()` 路径转换 |
