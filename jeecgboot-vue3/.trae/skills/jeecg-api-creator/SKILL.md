---
name: "jeecg-api-creator"
description: "生成 JeecgBoot Vue3 API 接口代码，使用 defHttp 和统一的请求封装。当用户需要创建 API 接口、定义数据模型或封装后端接口时调用。"
---

# JeecgBoot API 生成器

此 skill 用于生成符合 JeecgBoot Vue3 项目规范的 API 接口代码。

## 使用场景

- 创建新的 API 接口模块
- 封装后端接口
- 定义请求/响应数据模型
- 实现 CRUD 操作

## 输出结构

生成的代码包含：
1. API 枚举定义（接口地址）
2. 请求/响应数据模型接口
3. 使用 defHttp 封装的 API 函数
4. 统一的错误处理

## 代码模板

```typescript
import { defHttp } from '/@/utils/http/axios';
import type { AxiosResponse } from 'axios';

// ==================== API 枚举 ====================
enum Api {
  List = '/sys/user/list',
  Detail = '/sys/user/detail',
  Save = '/sys/user/save',
  Update = '/sys/user/update',
  Delete = '/sys/user/delete',
  DeleteBatch = '/sys/user/deleteBatch',
  Export = '/sys/user/export',
  Import = '/sys/user/import',
}

// ==================== 数据模型 ====================

/**
 * 查询参数
 */
export interface ListParams {
  username?: string;
  status?: number;
  pageNo?: number;
  pageSize?: number;
}

/**
 * 列表项数据
 */
export interface ListItem {
  id: string;
  username: string;
  realname: string;
  status: number;
  createTime: string;
}

/**
 * 列表响应
 */
export interface ListResult {
  records: ListItem[];
  total: number;
}

/**
 * 表单数据
 */
export interface FormData {
  id?: string;
  username: string;
  realname: string;
  email?: string;
  phone?: string;
  status: number;
}

// ==================== API 函数 ====================

/**
 * 获取列表数据
 */
export function getList(params: ListParams) {
  return defHttp.get<ListResult>({
    url: Api.List,
    params,
  });
}

/**
 * 获取详情
 */
export function getDetail(id: string) {
  return defHttp.get<FormData>({
    url: Api.Detail,
    params: { id },
  });
}

/**
 * 保存数据（新增）
 */
export function save(data: FormData) {
  return defHttp.post({
    url: Api.Save,
    data,
  });
}

/**
 * 更新数据
 */
export function update(data: FormData) {
  return defHttp.put({
    url: Api.Update,
    data,
  });
}

/**
 * 删除数据
 */
export function deleteById(id: string) {
  return defHttp.delete({
    url: Api.Delete,
    params: { id },
  });
}

/**
 * 批量删除
 */
export function deleteBatch(ids: string[]) {
  return defHttp.delete({
    url: Api.DeleteBatch,
    data: { ids },
  });
}

/**
 * 导出数据
 */
export function exportData(params: ListParams) {
  return defHttp.download({
    url: Api.Export,
    params,
  });
}

/**
 * 导入数据
 */
export function importData(file: File) {
  const formData = new FormData();
  formData.append('file', file);
  return defHttp.post({
    url: Api.Import,
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  });
}
```

## defHttp 方法

| 方法 | 用途 | 示例 |
|------|------|------|
| get | GET 请求 | `defHttp.get<T>({ url, params })` |
| post | POST 请求 | `defHttp.post<T>({ url, data })` |
| put | PUT 请求 | `defHttp.put<T>({ url, data })` |
| delete | DELETE 请求 | `defHttp.delete<T>({ url, params })` |
| download | 文件下载 | `defHttp.download({ url, params })` |
| upload | 文件上传 | `defHttp.upload({ url, data })` |

## 请求配置选项

| 属性 | 类型 | 说明 |
|------|------|------|
| url | string | 接口地址 |
| params | Object | URL 查询参数（GET/DELETE） |
| data | Object | 请求体数据（POST/PUT） |
| headers | Object | 自定义请求头 |
| timeout | number | 超时时间（毫秒） |
| errorMessageMode | string | 错误提示模式：'message' / 'modal' / 'none' |
| isTransformResponse | boolean | 是否转换响应数据 |
| isReturnNativeResponse | boolean | 是否返回原始响应 |

## 文件组织

```
api/
├── sys/                    # 系统管理模块
│   ├── user.ts            # 用户管理 API
│   ├── role.ts            # 角色管理 API
│   └── model/             # 数据模型
│       ├── userModel.ts
│       └── roleModel.ts
├── demo/                   # 示例模块
└── common/                 # 通用接口
```

## 使用说明

1. 根据后端接口文档定义 API 枚举
2. 定义请求参数和响应数据的 TypeScript 接口
3. 使用 defHttp 封装 API 函数
4. 导出类型定义供组件使用
5. 使用 `/@/` 路径别名导入
6. 遵循 project_rules.md 中的命名规范
