# 弹窗与抽屉组件使用规范

> 本文档涵盖 BasicModal 弹窗和 BasicDrawer 抽屉的使用规范。

---

## 一、BasicModal 弹窗

**导入方式**：

```typescript
import { BasicModal, useModal, useModalInner } from '/@/components/Modal';
```

### 1.1 关键 Props

| Prop | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `title` | `string` | - | 标题 |
| `visible` / `open` | `boolean` | - | 是否显示 |
| `width` | `string \| number` | `520` | 宽度 |
| `draggable` | `boolean` | `true` | 是否可拖拽 |
| `canFullscreen` | `boolean` | `true` | 是否可全屏 |
| `defaultFullscreen` | `boolean` | - | 默认全屏 |
| `confirmLoading` | `boolean` | - | 确认按钮加载中 |
| `destroyOnClose` | `boolean` | - | 关闭时销毁 |
| `maskClosable` | `boolean` | `true` | 点击蒙层关闭 |
| `showCancelBtn` | `boolean` | `true` | 显示取消按钮 |
| `showOkBtn` | `boolean` | `true` | 显示确认按钮 |
| `okText` | `string` | `'确定'` | 确认按钮文字 |
| `cancelText` | `string` | `'取消'` | 取消按钮文字 |
| `helpMessage` | `string \| string[]` | - | 帮助信息 |
| `loading` | `boolean` | - | 内容加载中 |
| `enableComment` | `boolean` | `false` | 开启评论区域 |

### 1.2 useModal 使用（父组件）

```typescript
const [registerModal, { openModal, closeModal, setModalProps }] = useModal();

// 打开弹窗，传递数据
function handleOpen() {
  openModal(true, { record: data });
}
```

### 1.3 useModalInner 使用（子组件）

```typescript
const [registerModalInner, { setModalProps, closeModal }] = useModalInner(async (data) => {
  const { record } = data;
  await setFieldsValue(record);
});
```

### 1.4 模板示例

```vue
<!-- 父组件 -->
<template>
  <div>
    <a-button @click="handleOpen">打开弹窗</a-button>
    <EditModal @register="registerModal" @success="reload" />
  </div>
</template>

<script lang="ts" setup>
  import { ref } from 'vue';
  import { useModal } from '/@/components/Modal';
  import EditModal from './EditModal.vue';

  const [registerModal, { openModal }] = useModal();

  function handleOpen() {
    openModal(true, { record: { id: '1', name: '测试' } });
  }

  function reload() {
    console.log('刷新列表');
  }
</script>

<!-- 子组件（弹窗内容）EditModal.vue -->
<template>
  <BasicModal
    v-bind="$attrs"
    @register="registerModal"
    title="编辑"
    @ok="handleSubmit"
    destroyOnClose
  >
    <BasicForm @register="registerForm" />
  </BasicModal>
</template>

<script lang="ts" setup>
  import { BasicForm, useForm } from '/@/components/Form';
  import { BasicModal, useModalInner } from '/@/components/Modal';

  const emit = defineEmits(['success', 'register']);

  const [registerForm, { setFieldsValue, validate }] = useForm({
    schemas: [
      { field: 'name', label: '名称', component: 'Input' },
    ],
    showActionButtonGroup: false,
  });

  const [registerModal, { closeModal, setModalProps }] = useModalInner(async (data) => {
    const { record } = data;
    await setFieldsValue(record);
  });

  async function handleSubmit() {
    try {
      const values = await validate();
      setModalProps({ confirmLoading: true });
      // 调用保存接口
      console.log('保存:', values);
      closeModal();
      emit('success');
    } finally {
      setModalProps({ confirmLoading: false });
    }
  }
</script>
```

---

## 二、BasicDrawer 抽屉

**导入方式**：

```typescript
import { BasicDrawer, useDrawer, useDrawerInner } from '/@/components/Drawer';
```

### 2.1 关键 Props

| Prop | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `title` | `string` | `''` | 标题 |
| `visible` / `open` | `boolean` | - | 是否显示 |
| `width` | `string \| number` | `256` | 宽度 |
| `isDetail` | `boolean` | - | 是否为详情模式 |
| `loading` | `boolean` | - | 加载中 |
| `destroyOnClose` | `boolean` | - | 关闭时销毁 |
| `maskClosable` | `boolean` | `true` | 点击蒙层关闭 |
| `showDetailBack` | `boolean` | `true` | 显示详情返回按钮 |
| `showFooter` | `boolean` | - | 显示底部 |
| `footerHeight` | `string \| number` | `60` | 底部高度 |

### 2.2 useDrawer 使用（父组件）

```typescript
const [registerDrawer, { openDrawer, closeDrawer, setDrawerProps }] = useDrawer();

function handleCreate() {
  openDrawer(true, { isUpdate: false, showFooter: true });
}

function handleEdit(record) {
  openDrawer(true, { record, isUpdate: true, showFooter: true });
}
```

### 2.3 useDrawerInner 使用（子组件）

```typescript
const [registerDrawerInner, { setDrawerProps, closeDrawer }] = useDrawerInner(async (data) => {
  const { isUpdate, record } = data;
  await resetFields();
  if (isUpdate) {
    await setFieldsValue({ ...record });
  }
});
```

### 2.4 模板示例

```vue
<!-- 父组件 -->
<template>
  <div>
    <BasicTable @register="registerTable" />
    <EditDrawer @register="registerDrawer" @success="handleSuccess" />
  </div>
</template>

<script lang="ts" setup>
  import { BasicTable, useTable } from '/@/components/Table';
  import { useDrawer } from '/@/components/Drawer';
  import EditDrawer from './EditDrawer.vue';

  const [registerTable] = useTable({
    api: listApi,
    columns: columns,
  });

  const [registerDrawer, { openDrawer }] = useDrawer();

  function handleCreate() {
    openDrawer(true, { isUpdate: false });
  }

  function handleEdit(record) {
    openDrawer(true, { record, isUpdate: true });
  }

  function handleSuccess() {
    reload();
  }
</script>

<!-- 子组件（抽屉内容）EditDrawer.vue -->
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
  import { useDrawerAdaptiveWidth } from '/@/hooks/jeecg/useAdaptiveWidth';

  const emit = defineEmits(['success', 'register']);
  const isUpdate = ref(true);

  const [registerForm, { resetFields, setFieldsValue, validate }] = useForm({
    schemas: [
      { field: 'name', label: '名称', component: 'Input', required: true },
    ],
    showActionButtonGroup: false,
  });

  const [registerDrawer, { setDrawerProps, closeDrawer }] = useDrawerInner(async (data) => {
    await resetFields();
    isUpdate.value = !!data?.isUpdate;
    setDrawerProps({ confirmLoading: false });

    if (unref(isUpdate)) {
      await setFieldsValue({ ...data.record });
    }
  });

  const getTitle = computed(() => unref(isUpdate) ? '编辑' : '新增');
  const { adaptiveWidth } = useDrawerAdaptiveWidth();

  async function handleSubmit() {
    try {
      const values = await validate();
      setDrawerProps({ confirmLoading: true });
      // 调用保存接口
      console.log('保存:', values);
      closeDrawer();
      emit('success');
    } finally {
      setDrawerProps({ confirmLoading: false });
    }
  }
</script>
```

### 2.5 自适应宽度

```typescript
import { useDrawerAdaptiveWidth } from '/@/hooks/jeecg/useAdaptiveWidth';

const { adaptiveWidth } = useDrawerAdaptiveWidth();
```

```vue
<BasicDrawer :width="adaptiveWidth" ... />
```

---

## 三、注意事项

1. **destroyOnClose**：建议开启，避免数据残留
   ```vue
   <BasicDrawer destroyOnClose ... />
   <BasicModal destroyOnClose ... />
   ```

2. **confirmLoading**：提交时设置加载中状态，防止重复提交
   ```typescript
   setDrawerProps({ confirmLoading: true });
   try {
     await saveOrUpdateApi(values);
     closeDrawer();
     emit('success');
   } finally {
     setDrawerProps({ confirmLoading: false });
   }
   ```

3. **数据传递**：通过 `openDrawer/openModal` 的第二个参数传递
   ```typescript
   openDrawer(true, { record, isUpdate: true, showFooter: true });
   ```

4. **详情模式**：通过 `showFooter: false` 隐藏底部按钮，实现只读详情
   ```typescript
   openDrawer(true, { record, isUpdate: true, showFooter: false });
   ```

5. **表单联动**：在 `useDrawerInner` 回调中使用 `updateSchema` 动态控制字段显隐
   ```typescript
   const [registerDrawerInner] = useDrawerInner(async (data) => {
     updateSchema([
       { field: 'password', ifShow: !data.isUpdate },
     ]);
   });
   ```
