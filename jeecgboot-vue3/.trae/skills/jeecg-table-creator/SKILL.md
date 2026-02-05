---
name: "jeecg-table-creator"
description: "生成 JeecgBoot Vue3 表格代码，使用 BasicTable、useTable 和 BasicColumn。当用户需要创建表格页面、列表页面或生成表格列时调用。"
---

# JeecgBoot 表格生成器

此 skill 用于生成符合 JeecgBoot Vue3 项目规范的表格代码。

## 使用场景

- 创建数据列表页面
- 添加表格组件
- 生成 BasicColumn 列定义
- 创建带搜索的表格页面

## 输出结构

生成的代码包含：
1. BasicTable 组件配合 `useTable` 钩子的使用
2. BasicColumn 数组列定义
3. 搜索表单配置（可选）
4. 操作列配置（编辑/删除/更多）
5. 批量操作工具栏

## 代码模板

```vue
<template>
  <div>
    <BasicTable @register="registerTable" :rowSelection="rowSelection">
      <template #tableTitle>
        <a-button type="primary" @click="handleCreate">新增</a-button>
      </template>
      <template #action="{ record }">
        <TableAction :actions="getTableActions(record)" />
      </template>
    </BasicTable>
  </div>
</template>

<script lang="ts" setup>
  import { BasicTable, useTable, TableAction } from '/@/components/Table';
  import type { BasicColumn } from '/@/components/Table';
  import { useListPage } from '/@/hooks/system/useListPage';
  
  defineOptions({ name: '{{component-name}}' });
  
  // 列定义
  const columns: BasicColumn[] = [
    // 列将在此处生成
  ];
  
  // 搜索表单配置
  const searchFormSchema = [
    // 搜索字段将在此处生成
  ];
  
  // 使用 useListPage 简化表格配置
  const { tableContext, onExportXls } = useListPage({
    tableProps: {
      title: '列表标题',
      api: fetchListApi,
      columns,
      formConfig: {
        schemas: searchFormSchema,
      },
      rowKey: 'id',
    },
  });
  
  const [registerTable, { reload, getSelectRows }] = tableContext;
  
  // 行选择配置
  const rowSelection = {
    type: 'checkbox',
    onChange: (selectedRowKeys, selectedRows) => {
      console.log(selectedRowKeys, selectedRows);
    },
  };
  
  // 操作按钮
  function getTableActions(record): ActionItem[] {
    return [
      {
        label: '编辑',
        onClick: () => handleEdit(record),
      },
      {
        label: '删除',
        popConfirm: {
          title: '确认删除？',
          confirm: () => handleDelete(record.id),
        },
      },
    ];
  }
</script>
```

## BasicColumn 列类型

| 类型 | 配置 | 说明 |
|------|------|------|
| 文本 | `dataIndex: 'name'` | 普通文本显示 |
| 日期 | `dataIndex: 'createTime'`, `format: 'YYYY-MM-DD'` | 日期格式化 |
| 字典 | `dataIndex: 'status'`, `customRender: ({ text }) => renderDict(text, 'status_dict')` | 字典值转换 |
| 图片 | `dataIndex: 'avatar'`, `slots: { customRender: 'img' }` | 图片显示 |
| 操作 | `slots: { customRender: 'action' }` | 操作按钮列 |
| 金额 | `dataIndex: 'amount'`, `format: (val) => '¥' + val` | 金额格式化 |

## 列定义示例

### 基础文本列
```typescript
{
  title: '用户名',
  dataIndex: 'username',
  width: 120,
  sorter: true,
}
```

### 日期列
```typescript
{
  title: '创建时间',
  dataIndex: 'createTime',
  width: 180,
  format: 'YYYY-MM-DD HH:mm:ss',
}
```

### 字典列
```typescript
{
  title: '状态',
  dataIndex: 'status',
  width: 100,
  customRender: ({ text }) => {
    return renderDict(text, 'status_dict');
  },
}
```

### 操作列
```typescript
{
  title: '操作',
  dataIndex: 'action',
  slots: { customRender: 'action' },
  fixed: 'right',
  width: 200,
}
```

## 搜索表单字段

```typescript
const searchFormSchema = [
  {
    field: 'username',
    label: '用户名',
    component: 'Input',
    colProps: { span: 8 },
  },
  {
    field: 'status',
    label: '状态',
    component: 'Select',
    componentProps: {
      options: [
        { label: '启用', value: 1 },
        { label: '禁用', value: 0 },
      ],
    },
    colProps: { span: 8 },
  },
];
```

## useTable 配置选项

| 属性 | 类型 | 说明 |
|------|------|------|
| api | Function | 数据请求函数 |
| columns | BasicColumn[] | 列定义 |
| formConfig | Object | 搜索表单配置 |
| rowKey | string | 行唯一标识 |
| showIndexColumn | boolean | 显示序号列 |
| showTableSetting | boolean | 显示表格设置 |
| canResize | boolean | 可调整列宽 |
| striped | boolean | 斑马纹 |

## 使用说明

1. 根据数据模型定义 BasicColumn 列
2. 配置搜索表单字段（可选）
3. 实现 API 数据获取函数
4. 添加操作列按钮（编辑/删除/查看）
5. 配置批量操作工具栏
6. 使用 `/@/` 路径别名导入
7. 遵循 project_rules.md 中的命名规范
