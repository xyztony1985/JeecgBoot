# 附件上传使用规范

> **重要提示**：新开发功能**必须使用托管模式**，旧模式已逐步淘汰。

---

## 一、快速开始

### 前端使用

```vue
<template>
  <JUpload v-model:value="formModel.file_id" bizCode="my_report.file_id" />
</template>

<script setup>
const formModel = reactive({
  file_id: '' // 存储 file_id
});
</script>
```

### 数据库字段

```sql
-- 业务表添加附件字段
ALTER TABLE my_report ADD COLUMN file_id varchar(36) COMMENT '附件file_id';

-- 多附件场景
ALTER TABLE my_report ADD COLUMN file_ids varchar(2000) COMMENT '附件file_id，逗号分隔';
```

### 文件预览

```vue
<template>
  <img :src="`${apiUrl}/sys/file/view/${fileId}`" />
</template>
```

---

## 二、前端使用指南

### 2.1 关键 Props

| Prop | 说明 |
|------|------|
| `bizCode` | **必填**，业务标识，格式：`{表名}.{字段名}` |
| `value` | 绑定值，逗号分隔的 file_id |
| `returnUrl` | `true` 返回 file_id 字符串；`false` 返回包含 `fileId` 的 JSON 数组 |
| `fileType` | 文件类型：`'all'`（默认）或 `'image'`（仅图片） |
| `maxCount` | 最大上传数量，0 为不限 |

### 2.2 bizCode 格式说明

`bizCode` 格式为 `{表名}.{字段名}`，只允许字母、数字、下划线、连字符：

```typescript
// 正确示例
bizCode="my_report.file_id"
bizCode="my-report.file_id"
bizCode="sys_user.file_id"
bizCode="contract.file_id"

// 错误示例（会提示格式错误并禁用上传）
bizCode="my_report"          // 缺少字段名
bizCode="my report.file_id"  // 包含空格
```

### 2.3 图片上传场景

使用 JUpload 的 `fileType="image"` 限制只能上传图片：

```vue
<template>
  <!-- 单张图片上传 -->
  <JUpload v-model:value="formModel.file_id" bizCode="sys_user.file_id" fileType="image" :maxCount="1" />
  
  <!-- 多张图片上传 -->
  <JUpload v-model:value="formModel.file_ids" bizCode="product.file_ids" fileType="image" :maxCount="9" />
</template>

<script setup>
const formModel = reactive({
  file_id: '',   // 存储单个 file_id
  file_ids: ''   // 存储多个 file_id，逗号分隔
});
</script>
```

### 2.4 文件预览

```vue
<!-- 预览 URL 格式 -->
<img :src="`${apiUrl}/sys/file/view/${fileId}`" />
```

- `apiUrl` 从配置文件读取（`VITE_GLOB_API_URL`）
- local 存储：后端直接返回文件流
- minio/oss 存储：重定向到完整 URL

---

## 三、数据库字段规范

### 3.1 字段长度

| 场景 | 字段类型 | 说明 |
|------|---------|------|
| 单附件 | `varchar(36)` | 与 sys_attachment.id 一致 |
| 多附件 | `varchar(2000)` | 逗号分隔的 file_id |

### 3.2 命名规范

`bizCode` 格式为 `{table_name}.{field_name}`，与数据库表名和字段名完全对应：

```
示例：
  my_report.file_id     → 报表附件
  sys_user.file_id      → 用户附件
  contract.file_id      → 合同附件
```

### 3.3 注意事项

1. **不要创建新的附件表**：统一使用 `sys_attachment`
2. **业务表只存 file_id**：文件路径由系统统一管理
3. **保留原始文件名**：系统自动在 `sys_attachment` 表中记录

---

## 四、后端说明

> 大多数业务开发**不需要关注后端**。前端使用 JUpload 组件传入 `bizCode`，已自动关联后端 `/sys/file/upload` 接口，完成上传、入库、预览全流程。

以下为后端接口参考，仅在需要程序化调用时查阅：

### 4.1 接口列表

| 路由 | 说明 |
|------|------|
| `POST /sys/file/upload` | 托管模式上传，返回 file_id |
| `GET /sys/file/info` | 根据 file_id 获取文件信息 |
| `GET /sys/file/url` | 根据 file_id 获取文件访问 URL |
| `GET /sys/file/view/{id}` | 文件访问入口，local 直接返回文件流，minio/oss 重定向 |
| `POST /sys/file/delete` | 软删除附件 |

### 4.2 上传接口

**接口地址：** `POST /sys/file/upload`

**请求参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `file` | `MultipartFile` | 是 | 上传的文件 |
| `bizCode` | `string` | 是 | 业务标识，格式：`{表名}.{字段名}` |

**返回示例：**

```json
{
  "success": true,
  "message": "1812345678901234567"
}
```

`message` 字段即为 `file_id`。

### 4.3 开发要求

1. **不要新建上传接口**：统一使用 `/sys/file/upload`
2. **业务表只存 file_id**：不直接存储文件路径
3. **bizCode 必须校验**：使用正则 `^[a-zA-Z0-9_-]+$` 校验，防止 SQL 注入

---

## 五、淘汰清单

> **警告**：以下内容已逐步淘汰，**新开发功能禁止使用**，仅用于维护现有代码时参考。

### 5.1 淘汰组件

| 组件 | 替代方案 |
|------|---------|
| JImageUpload | 改用 JUpload + `fileType="image"` |
| BasicUpload | 改用 JUpload |

### 5.2 旧上传模式

旧模式下，业务表直接存储文件路径/URL，不经过 `sys_attachment` 表管理：

| 维度 | 旧模式 |
|------|--------|
| 业务表存储 | 文件路径/URL（逗号分隔） |
| 上传接口 | `/sys/common/upload` |
| 文件管理 | 业务方自行处理，无孤儿清理 |

**前端使用**：

```vue
<!-- ❌ 禁止：未传入 bizCode（使用旧模式） -->
<JUpload v-model:value="formData.files" bizPath="myModule/doc" />
```
