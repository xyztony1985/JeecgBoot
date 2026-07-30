# CsUpload 组件使用文档

## 概述

`CsUpload` 是参考 `JUpload` 组件开发的附件上传组件，意在极简使用，文件信息统一存入 `sys_attachment` 表，业务表只存 `file_id`。

## 引入方式

`CsUpload` 已全局注册，**无需手动 import**，直接在模板中使用即可：

```vue
<CsUpload v-model:value="formModel.attachment" :bizCode="bizCode" />
```

## Props

| 属性 | 类型 | 默认值 | 必填 | 说明 |
|------|------|--------|------|------|
| `value` | `string` | `''` | 否 | 绑定值，逗号分隔的 `file_id` 字符串 |
| `bizCode` | `string` | - | **是** | 业务标识，格式：`{table_name}.{field_name}` |
| `text` | `string` | `'上传'` | 否 | 上传按钮/提示文字 |
| `fileType` | `string` | `'all'` | 否 | 上传类型：`'all'` / `'image'` / `'file'` |
| `maxCount` | `number` | `0` | 否 | 最大上传数量，`0` 表示不限 |
| `buttonVisible` | `boolean` | `true` | 否 | 是否显示上传按钮 |
| `multiple` | `boolean` | `true` | 否 | 是否允许多文件上传 |
| `mover` | `boolean` | `true` | 否 | 是否显示左右移动按钮（仅图片模式） |
| `download` | `boolean` | `true` | 否 | 是否显示下载按钮（仅图片模式） |
| `removeConfirm` | `boolean` | `false` | 否 | 删除时是否弹出确认框 |
| `beforeUpload` | `function` | - | 否 | 上传前自定义校验函数，返回 `false` 中断上传 |
| `disabled` | `boolean` | `false` | 否 | 是否禁用 |
| `replaceLastOne` | `boolean` | `false` | 否 | 超出最大数量时是否替换最后一个文件 |

## Events

| 事件名 | 参数 | 说明 |
|--------|------|------|
| `change` | `(value: string)` | 文件变化时触发，值为逗号分隔的 `file_id` |
| `update:value` | `(value: string)` | `v-model` 更新事件 |

## bizCode 规范

- **格式**：`{table_name}.{field_name}`，与数据库表名和字段名完全对应
- **字符限制**：只允许字母、数字、下划线、连字符
- **传入方式**：直接传入字符串

```typescript
const bizCode = 'my_report.attachment';
```

格式错误时组件会自动禁用并给出警告提示。

## 基本用法

### 文件上传

```vue
<template>
  <CsUpload v-model:value="formModel.attachment" :bizCode="bizCode" />
</template>

<script setup lang="ts">
import { reactive } from 'vue';

const bizCode = 'my_report.attachment';

const formModel = reactive({
  // 存储逗号分隔的 file_id，如: "123456,789012"
  attachment: '',
});
</script>
```

### 图片上传

```vue
<template>
  <CsUpload
    v-model:value="formModel.images"
    :bizCode="bizCode"
    fileType="image"
    :maxCount="5"
  />
</template>

<script setup lang="ts">
import { reactive } from 'vue';

const bizCode = 'product.images';

const formModel = reactive({
  images: '',
});
</script>
```

### 限制上传数量

```vue
<template>
  <CsUpload
    v-model:value="formModel.files"
    :bizCode="bizCode"
    :maxCount="3"
  />
</template>
```

### 替换模式（超出数量时替换最后一个）

```vue
<template>
  <CsUpload
    v-model:value="formModel.avatar"
    :bizCode="bizCode"
    fileType="image"
    :maxCount="1"
    :replaceLastOne="true"
  />
</template>
```

### 删除确认

```vue
<template>
  <CsUpload
    v-model:value="formModel.files"
    :bizCode="bizCode"
    :removeConfirm="true"
  />
</template>
```

### 自定义上传前校验

```vue
<template>
  <CsUpload
    v-model:value="formModel.files"
    :bizCode="bizCode"
    :beforeUpload="checkFileSize"
  />
</template>

<script setup lang="ts">
function checkFileSize(file: File) {
  const maxSize = 10 * 1024 * 1024; // 10MB
  if (file.size > maxSize) {
    // 使用 useMessage 提示
    return false; // 中断上传
  }
  return true;
}
</script>
```

### 禁用状态

```vue
<template>
  <CsUpload
    v-model:value="formModel.files"
    :bizCode="bizCode"
    disabled
  />
</template>
```

### 自定义按钮文字

```vue
<template>
  <CsUpload
    v-model:value="formModel.files"
    :bizCode="bizCode"
    text="选择附件"
  />
</template>
```

## 文件预览

### 图片预览

图片模式下，点击缩略图自动弹出图片预览弹窗。

### 文件预览

文件模式下，点击文件名会在新窗口打开 `/sys/file/view/{fileId}` 地址。

### 在模板中直接预览

```vue
<template>
  <!-- 直接使用 view 接口预览 -->
  <img :src="`${apiUrl}/sys/file/view/${fileId}`" />
</template>
```

## 文件下载

需要真实 URL 时（如下载场景），使用 `getFileInfo`：

```typescript
import { getFileInfo } from '/@/api/common/api';

const info = await getFileInfo(fileId);
const url = info.filePath; // local 存储为相对路径，minio/oss 为完整 URL
window.open(url);
```

## 完整表单示例

```vue
<template>
  <BasicForm @register="register" @submit="handleSubmit" />
</template>

<script setup lang="ts">
import { reactive } from 'vue';
import { defHttp } from '/@/utils/http/axios';

const bizCode = 'my_report.attachment';

const formModel = reactive({
  title: '',
  attachment: '', // 存储逗号分隔的 file_id
});

async function handleSubmit() {
  await defHttp.post({ url: '/myReport/add', params: formModel });
  // 无需手动确认关联，定时任务会自动清理未引用的孤儿附件
}
</script>
```

## 后端接口

CsUpload 依赖以下后端接口：

| 路由 | 方法 | 说明 |
|------|------|------|
| `POST /sys/file/upload` | upload | 上传文件，参数：`bizCode`（必填）、`file`（文件） |
| `GET /sys/file/view/{id}` | view | 文件预览/访问 |
| `GET /sys/file/info` | info | 根据 file_id 获取文件信息 |
| `GET /sys/file/url` | url | 根据 file_id 获取文件 URL |
| `POST /sys/file/delete` | delete | 软删除附件 |

**上传响应格式：**

```json
{
  "success": true,
  "result": {
    "fileId": "1812345678901234567",
    "fileName": "报告.pdf",
    "fileSize": 102400
  }
}
```

## 存储路径规则

后端自动生成存储路径：`{bizCode}/{yyyy-MM}/{file}`

示例：`my_report.attachment/2026-07/报告_1753689600000.pdf`

## 注意事项

1. `bizCode` 为必填参数，格式错误时组件会自动禁用
2. 绑定值为逗号分隔的 `file_id` 字符串，不是文件路径
3. 图片模式下的移动/下载按钮在鼠标悬停时显示
4. 组件不处理文件下载逻辑，下载需通过 `getFileInfo` 获取真实 URL


## 与 JUpload 的区别

| 对比项 | JUpload | CsUpload |
|--------|---------|----------|
| 模式 | 仅旧模式 | 仅托管模式 |
| `bizCode` | 不支持 | **必填** |
| 上传接口 | 固定 `/sys/common/upload` | 固定 `/sys/file/upload` |
| 返回值 | 文件路径 | 始终返回逗号分隔的 `file_id` |
| 预览 | `getFileAccessHttpUrl` | 固定 `/sys/file/view/{id}` |