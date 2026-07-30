# 典型页面模式与通用规范

> 本文档涵盖 CRUD 列表页、编辑抽屉等典型页面的标准模板，以及通用开发规范。

---

## 一、典型 CRUD 页面结构

```
views/
└── module/
    └── entity/
        ├── index.vue          # 列表页（BasicTable + 搜索表单）
        ├── XxxDrawer.vue      # 编辑抽屉（BasicDrawer + BasicForm）
        └── xxx.data.ts        # 列定义、表单 Schema
```

---

## 二、列表页标准模板

```vue
<template>
  <div>
    <BasicTable @register="registerTable" :rowSelection="rowSelection">
      <!-- 表格标题区域 -->
      <template #tableTitle>
        <a-button type="primary" preIcon="ant-design:plus-outlined" @click="handleCreate">新增</a-button>
        <a-button type="primary" preIcon="ant-design:export-outlined" @click="onExportXls">导出</a-button>
        <a-dropdown v-if="selectedRowKeys.length > 0">
          <template #overlay>
            <a-menu>
              <a-menu-item key="1" @click="batchDelete">
                <Icon icon="ant-design:delete-outlined" />批量删除
              </a-menu-item>
            </a-menu>
          </template>
          <a-button>批量操作 <Icon icon="mdi:chevron-down" /></a-button>
        </a-dropdown>
      </template>

      <!-- 操作列 -->
      <template #action="{ record }">
        <TableAction
          :actions="[
            { label: '编辑', onClick: handleEdit.bind(null, record) },
            { label: '删除', color: 'error', popConfirm: { title: '确认删除？', confirm: handleDelete.bind(null, record) } },
          ]"
        />
      </template>
    </BasicTable>

    <!-- 编辑抽屉 -->
    <EditDrawer @register="registerDrawer" @success="handleSuccess" />
  </div>
</template>

<script lang="ts" setup>
  import { BasicTable, TableAction } from '/@/components/Table';
  import { useDrawer } from '/@/components/Drawer';
  import { useListPage } from '/@/hooks/system/useListPage';
  import { useMessage } from '/@/hooks/web/useMessage';
  import EditDrawer from './EditDrawer.vue';
  import { columns, searchFormSchema } from './data';
  import { listApi, deleteApi, batchDeleteApi, exportUrl } from './api';

  const { createMessage } = useMessage();
  const [registerDrawer, { openDrawer }] = useDrawer();

  const { tableContext, onExportXls } = useListPage({
    designScope: 'xxx-list',
    tableProps: {
      api: listApi,
      columns,
      formConfig: { schemas: searchFormSchema },
      actionColumn: { width: 120 },
    },
    exportConfig: { name: '数据列表', url: exportUrl },
  });

  // tableContext 返回类型：[registerTable, tableMethods, tableSelectionContext]
  // - registerTable: 注册函数，用于 BasicTable 的 @register
  // - tableMethods: 表格方法对象，包含 reload、setLoading、getDataSource 等
  // - tableSelectionContext: 行选择上下文，包含 rowSelection、selectedRowKeys、selectedRows
  const [registerTable, { reload }, { rowSelection, selectedRowKeys }] = tableContext;

  function handleCreate() {
    openDrawer(true, { isUpdate: false });
  }

  function handleEdit(record) {
    openDrawer(true, { record, isUpdate: true });
  }

  async function handleDelete(record) {
    await deleteApi({ id: record.id }, reload);
  }

  async function batchDelete() {
    await batchDeleteApi({ ids: selectedRowKeys.value }, () => {
      selectedRowKeys.value = [];
      reload();
    });
  }

  function handleSuccess() {
    reload();
  }
</script>
```

---

## 三、编辑抽屉标准模板

```vue
<template>
  <BasicDrawer
    v-bind="$attrs"
    @register="registerDrawer"
    :title="getTitle"
    :width="adaptiveWidth"
    @ok="handleSubmit"
    destroyOnClose
  >
    <BasicForm @register="registerForm" />
  </BasicDrawer>
</template>

<script lang="ts" setup>
  import { ref, computed, unref } from 'vue';
  import { BasicForm, useForm } from '/@/components/Form';
  import { BasicDrawer, useDrawerInner } from '/@/components/Drawer';
  import { formSchema } from './data';
  import { saveOrUpdateApi } from './api';
  import { useDrawerAdaptiveWidth } from '/@/hooks/jeecg/useAdaptiveWidth';

  const emit = defineEmits(['success', 'register']);
  const isUpdate = ref(true);
  const rowId = ref('');

  const [registerForm, { resetFields, setFieldsValue, validate, updateSchema }] = useForm({
    labelWidth: 90,
    schemas: formSchema,
    showActionButtonGroup: false,
    baseColProps: { span: 12 },
  });

  const [registerDrawer, { setDrawerProps, closeDrawer }] = useDrawerInner(async (data) => {
    await resetFields();
    isUpdate.value = !!data?.isUpdate;
    setDrawerProps({ confirmLoading: false });

    if (unref(isUpdate)) {
      rowId.value = data.record.id;
      await setFieldsValue({ ...data.record });
    }
  });

  const getTitle = computed(() => unref(isUpdate) ? '编辑' : '新增');
  const { adaptiveWidth } = useDrawerAdaptiveWidth();

  async function handleSubmit() {
    try {
      const values = await validate();
      setDrawerProps({ confirmLoading: true });
      await saveOrUpdateApi({ ...values, id: rowId.value }, unref(isUpdate));
      closeDrawer();
      emit('success');
    } finally {
      setDrawerProps({ confirmLoading: false });
    }
  }
</script>
```

---

## 四、数据配置文件模板 (data.ts)

```typescript
import { FormSchema } from '/@/components/Form';
import { BasicColumn } from '/@/components/Table';

// 表格列配置
export const columns: BasicColumn[] = [
  { title: '名称', dataIndex: 'name', width: 150 },
  { title: '编码', dataIndex: 'code', width: 120 },
  {
    title: '状态',
    dataIndex: 'status',
    width: 100,
    customRender: ({ value }) => value === 1 ? '启用' : '禁用',
  },
  { title: '创建时间', dataIndex: 'createTime', width: 180, sorter: true },
];

// 搜索表单配置
export const searchFormSchema: FormSchema[] = [
  { field: 'name', label: '名称', component: 'Input' },
  {
    field: 'status',
    label: '状态',
    component: 'JDictSelectTag',
    componentProps: { dictCode: 'status' },
  },
  {
    field: 'createTime',
    label: '创建时间',
    component: 'RangePicker',
    componentProps: { valueFormat: 'YYYY-MM-DD' },
  },
];

// 编辑表单配置
export const formSchema: FormSchema[] = [
  {
    field: 'name',
    label: '名称',
    component: 'Input',
    required: true,
    componentProps: { placeholder: '请输入名称', maxlength: 50 },
  },
  {
    field: 'code',
    label: '编码',
    component: 'Input',
    required: true,
    componentProps: { placeholder: '请输入编码' },
  },
  {
    field: 'status',
    label: '状态',
    component: 'JDictSelectTag',
    defaultValue: '1',
    componentProps: { dictCode: 'status' },
  },
  {
    field: 'remark',
    label: '备注',
    component: 'InputTextArea',
    componentProps: { rows: 4 },
  },
];
```

---

## 五、通用开发规范

### 5.1 路径别名

统一使用 `/@/` 导入项目内模块：

```typescript
// ✅ 正确
import { BasicTable } from '/@/components/Table';
import { defHttp } from '/@/utils/http/axios';

// ❌ 错误
import { BasicTable } from '@/components/Table';
```

### 5.2 组件命名

组件文件使用 PascalCase，`defineOptions` 使用 kebab-case：

```typescript
defineOptions({ name: 'system-user' });
```

### 5.3 API 调用

统一使用 `defHttp`：

```typescript
import { defHttp } from '/@/utils/http/axios';

enum Api {
  List = '/xxx/list',
  Delete = '/xxx/delete',
}

export const listApi = (params) => defHttp.get({ url: Api.List, params });
export const deleteApi = (params) => defHttp.post({ url: Api.Delete, params });
export const saveOrUpdateApi = (params, isUpdate) => {
  const url = isUpdate ? '/xxx/edit' : '/xxx/add';
  return defHttp.post({ url, params });
};
```

### 5.4 权限控制

使用 `v-auth` 指令或 `hasPermission` 方法：

```vue
<a-button v-auth="'system:user:add'">新增</a-button>
```

```typescript
const { hasPermission } = usePermission();
if (hasPermission('system:user:add')) { ... }
```

### 5.5 淘汰清单

> **警告**：以下组件已淘汰，**新开发功能禁止使用**。

| 组件 | 替代方案 |
|------|---------|
| `JImageUpload` | 改用 `JUpload` + `fileType="image"` |
| `BasicUpload` | 改用 `JUpload` |

旧模式（无 `bizCode`）的上传方式已淘汰，详见 [附件上传使用规范](./attachment-guide.md)。
