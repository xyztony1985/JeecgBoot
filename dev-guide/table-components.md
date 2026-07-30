# 表格组件使用规范

> 本文档涵盖 BasicTable 数据表格和 JVxeTable 可编辑表格的使用规范。

---

## 一、BasicTable 数据表格

**导入方式**：

```typescript
import { BasicTable, useTable, TableAction } from '/@/components/Table';
```

### 1.1 关键 Props

| Prop | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `api` | `Function` | - | 请求函数 |
| `columns` | `BasicColumn[]` | `[]` | 列配置 |
| `dataSource` | `Recordable[]` | - | 数据源（不请求接口时使用） |
| `useSearchForm` | `boolean` | - | 是否使用搜索表单 |
| `formConfig` | `FormProps` | - | 搜索表单配置 |
| `pagination` | `PaginationProps \| boolean` | - | 分页配置 |
| `rowSelection` | `TableRowSelection \| null` | `null` | 行选择配置 |
| `actionColumn` | `BasicColumn` | - | 操作列配置 |
| `showIndexColumn` | `boolean` | `true` | 是否显示序号列 |
| `showActionColumn` | `boolean` | `true` | 是否显示操作列 |
| `bordered` | `boolean` | - | 是否显示边框 |
| `striped` | `boolean` | `false` | 是否斑马纹 |
| `canResize` | `boolean` | `true` | 是否可调整高度 |
| `immediate` | `boolean` | `true` | 是否立即请求 |
| `showTableSetting` | `boolean` | - | 是否显示表格设置 |
| `rowKey` | `string \| Function` | `''` | 行唯一标识 |
| `searchInfo` | `Object` | - | 额外请求参数 |
| `beforeFetch` | `Function` | - | 请求前处理参数 |
| `afterFetch` | `Function` | - | 请求后处理数据 |
| `showSummary` | `boolean` | - | 是否显示汇总 |
| `summaryFunc` | `Function` | - | 汇总函数 |
| `size` | `SizeType` | `'default'` | 表格大小 |

### 1.2 useTable 返回值

```typescript
const [registerTable, {
  reload,              // 刷新表格
  setLoading,          // 设置加载状态
  getDataSource,       // 获取数据源
  getColumns,          // 获取列配置
  setColumns,          // 设置列配置
  updateTableDataRecord, // 更新单条记录
  deleteTableDataRecord, // 删除单条记录
  insertTableDataRecord, // 插入单条记录
  clearSelectedRowKeys,  // 清空选择
  getSelectRows,         // 获取选中行
  getSelectRowKeys,      // 获取选中行 Key
  setTableData,          // 设置表格数据
  expandAll,             // 展开所有
  collapseAll,           // 收起所有
  setSelectedRows,       // 设置选中行
}] = useTable({
  api: fetchApi,
  columns: tableColumns,
  formConfig: { schemas: searchSchemas },
});
```

### 1.3 useListPage（推荐）

列表页推荐使用 `useListPage` 封装，自动处理导出、导入等公共逻辑：

```typescript
const { prefixCls, tableContext, onExportXls, onImportXls } = useListPage({
  designScope: 'user-list',  // 设计标识，用于缓存列配置
  tableProps: {
    title: '用户列表',
    api: listApi,
    columns: columns,
    formConfig: { schemas: searchFormSchema },
    actionColumn: { width: 120 },
    beforeFetch: (params) => {
      return Object.assign({ column: 'createTime', order: 'desc' }, params);
    },
  },
  exportConfig: {
    name: '用户列表',
    url: exportUrl,
  },
  importConfig: {
    url: importUrl,
  },
});

const [registerTable, { reload }, { rowSelection, selectedRows, selectedRowKeys }] = tableContext;
```

### 1.4 列配置示例

```typescript
export const columns: BasicColumn[] = [
  {
    title: '用户名',
    dataIndex: 'username',
    width: 120,
  },
  {
    title: '姓名',
    dataIndex: 'realname',
    width: 100,
  },
  {
    title: '状态',
    dataIndex: 'status',
    width: 80,
    customRender: ({ value }) => {
      return value === 1 ? '正常' : '冻结';
    },
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
    width: 180,
    sorter: true,
  },
];
```

### 1.5 操作列配置

```vue
<template #action="{ record }">
  <TableAction
    :actions="[
      { label: '编辑', onClick: handleEdit.bind(null, record) },
      { label: '删除', color: 'error', popConfirm: { title: '确认删除？', confirm: handleDelete.bind(null, record) } },
    ]"
    :dropDownActions="[
      { label: '详情', onClick: handleDetail.bind(null, record) },
    ]"
  />
</template>
```

### 1.6 注意事项

1. **actionColumn 宽度**：单独配置，建议 120px
   ```typescript
   actionColumn: { width: 120 }
   ```

2. **行选择**：通过 `rowSelection` 配置
   ```typescript
   const rowSelection = {
     type: 'checkbox',
     onChange: (selectedRowKeys, selectedRows) => { ... }
   };
   ```

3. **刷新数据**：操作成功后调用 `reload`
   ```typescript
   await deleteApi(record.id);
   reload();
   ```

4. **分页配置**：默认开启分页，可通过 `pagination: false` 关闭
   ```typescript
   pagination: false  // 关闭分页
   pagination: { pageSize: 20 }  // 自定义分页大小
   ```

5. **排序功能**：列配置中 `sorter: true` 开启排序，后端需支持排序参数
   ```typescript
   { title: '创建时间', dataIndex: 'createTime', sorter: true }
   ```

6. **表格设置**：`showTableSetting: true` 显示列设置按钮，用户可自定义显示列
   ```typescript
   showTableSetting: true
   ```

7. **行唯一标识**：必须设置 `rowKey`，否则行选择、编辑等功能异常
   ```typescript
   rowKey: 'id'  // 或使用函数
   rowKey: (record) => `${record.id}_${record.version}`
   ```

---

## 二、JVxeTable 可编辑表格

**导入方式**：

```typescript
import { JVxeTable } from '/@/components/jeecg/JVxeTable';
```

### 2.1 关键 Props

| Prop | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `columns` | `Array` | **必填** | 列配置 |
| `dataSource` | `Array` | **必填** | 数据源 |
| `rowKey` | `string` | `'id'` | 行唯一标识 |
| `toolbar` | `boolean` | `false` | 是否显示工具栏 |
| `toolbarConfig` | `Object` | - | 工具栏配置 |
| `rowNumber` | `boolean` | `false` | 是否显示行号 |
| `rowSelection` | `boolean` | `false` | 是否可选择行 |
| `rowSelectionType` | `'checkbox' \| 'radio'` | `'checkbox'` | 选择行类型 |
| `dragSort` | `boolean` | `false` | 是否可拖拽排序 |
| `sortKey` | `string` | `'orderNum'` | 排序字段 Key |
| `loading` | `boolean` | `false` | 是否加载中 |
| `height` | `number \| string` | `'auto'` | 表格高度 |
| `maxHeight` | `number` | - | 最大高度 |
| `disabled` | `boolean` | `false` | 是否禁用 |
| `disabledRows` | `Object` | `{}` | 禁用的行配置 |
| `editRules` | `Object` | `{}` | 校验规则 |
| `pagination` | `Object` | `{}` | 分页配置（设置即显示） |
| `size` | `'medium' \| 'small' \| 'mini'` | `'medium'` | 表格大小 |
| `bordered` | `boolean` | `false` | 是否显示边框 |
| `alwaysEdit` | `boolean` | `false` | 是否一直显示组件 |
| `keyboardEdit` | `boolean` | `false` | 是否开启键盘编辑 |
| `cacheColumnsKey` | `string` | `''` | 缓存列设置的 Key |
| `scrollY` | `Object` | `{ enabled: true }` | 纵向虚拟滚动 |
| `scrollX` | `Object` | `{ enabled: false }` | 横向虚拟滚动 |

### 2.2 使用示例

```vue
<template>
  <JVxeTable
    ref="vxeTableRef"
    :columns="columns"
    :dataSource="dataSource"
    :toolbar="true"
    :rowNumber="true"
    :rowSelection="true"
    :dragSort="true"
    :loading="loading"
    @valueChange="handleValueChange"
  />
</template>

<script lang="ts" setup>
  import { ref } from 'vue';
  import { JVxeTable } from '/@/components/jeecg/JVxeTable';

  const vxeTableRef = ref();
  const loading = ref(false);
  const dataSource = ref([
    { id: '1', name: '张三', age: 18 },
    { id: '2', name: '李四', age: 20 },
  ]);

  const columns = [
    { title: '姓名', key: 'name', type: 'input', width: '200px' },
    { title: '年龄', key: 'age', type: 'inputNumber', width: '100px' },
  ];

  function handleValueChange({ type, row, column, value }) {
    console.log('值变化:', type, row, column, value);
  }

  // 获取表格数据
  function getData() {
    return vxeTableRef.value.getTableData();
  }

  // 新增行
  function addRow() {
    vxeTableRef.value.addRows([{ name: '', age: 0 }]);
  }

  // 删除选中行
  function removeSelectedRows() {
    vxeTableRef.value.removeSelection();
  }
</script>
```

### 2.3 注意事项

1. **列类型**：`type` 支持 `input`、`inputNumber`、`select`、`date`、`checkbox`、`radio` 等
2. **拖拽排序**：有固定列时无法拖拽排序，仅可上下排序
3. **列缓存**：设置 `cacheColumnsKey` 可记住列设置，页面内唯一即可
4. **虚拟滚动**：默认开启纵向虚拟滚动，不支持展开行
5. **FormSchema 说明**：JVxeTable 不用于 BasicForm，不使用 FormSchema 配置。列配置通过 `columns` prop 直接传入，每列的 `type` 决定编辑组件类型
6. **数据获取**：通过 `ref` 调用 `getTableData()` 获取编辑后的数据
7. **行操作**：支持 `addRows()`、`removeSelection()`、`clearSelection()` 等方法
