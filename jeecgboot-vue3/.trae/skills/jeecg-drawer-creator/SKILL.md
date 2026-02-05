---
name: "jeecg-drawer-creator"
description: "生成 JeecgBoot Vue3 抽屉组件代码，使用 BasicDrawer 和表单/详情展示。当用户需要创建编辑抽屉、详情抽屉或弹窗组件时调用。"
---

# JeecgBoot 抽屉生成器

此 skill 用于生成符合 JeecgBoot Vue3 项目规范的抽屉组件代码。

## 使用场景

- 创建编辑/新增抽屉
- 创建详情展示抽屉
- 创建表单弹窗
- 创建确认操作弹窗

## 输出结构

生成的代码包含：
1. BasicDrawer 组件配合 `useDrawer` 钩子的使用
2. 表单或详情内容区域
3. 底部操作按钮（确定/取消）
4. 数据加载和提交逻辑

## 代码模板 - 编辑抽屉

```vue
<template>
  <BasicDrawer
    v-bind="$attrs"
    @register="registerDrawer"
    :title="getTitle"
    width="600"
    @ok="handleSubmit"
  >
    <BasicForm @register="registerForm" />
  </BasicDrawer>
</template>

<script lang="ts" setup>
  import { computed, ref, unref } from 'vue';
  import { BasicDrawer, useDrawerInner } from '/@/components/Drawer';
  import { BasicForm, useForm } from '/@/components/Form';
  import type { FormSchema } from '/@/components/Form';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { getDetail, save, update } from './api';
  
  defineOptions({ name: '{{component-name}}' });
  
  const emit = defineEmits(['success', 'register']);
  
  const { createMessage } = useMessage();
  
  const isUpdate = ref(false);
  const recordId = ref<string>('');
  
  // 表单配置
  const formSchemas: FormSchema[] = [
    {
      field: 'username',
      label: '用户名',
      component: 'Input',
      required: true,
      rules: [{ required: true, message: '请输入用户名' }],
    },
    // 更多字段...
  ];
  
  const [registerForm, { validate, setFieldsValue, resetFields }] = useForm({
    labelWidth: 100,
    schemas: formSchemas,
    showActionButtonGroup: false,
  });
  
  const [registerDrawer, { setDrawerProps, closeDrawer }] = useDrawerInner(async (data) => {
    resetFields();
    setDrawerProps({ confirmLoading: false });
    
    isUpdate.value = !!data?.isUpdate;
    
    if (unref(isUpdate)) {
      recordId.value = data.record.id;
      // 加载详情数据
      const detail = await getDetail(data.record.id);
      setFieldsValue(detail);
    }
  });
  
  const getTitle = computed(() => (unref(isUpdate) ? '编辑' : '新增'));
  
  async function handleSubmit() {
    try {
      const values = await validate();
      setDrawerProps({ confirmLoading: true });
      
      if (unref(isUpdate)) {
        await update({ ...values, id: recordId.value });
      } else {
        await save(values);
      }
      
      createMessage.success('操作成功');
      closeDrawer();
      emit('success');
    } finally {
      setDrawerProps({ confirmLoading: false });
    }
  }
</script>
```

## 代码模板 - 详情抽屉

```vue
<template>
  <BasicDrawer
    v-bind="$attrs"
    @register="registerDrawer"
    title="详情"
    width="500"
    :showFooter="false"
  >
    <Description :column="1" :data="detailData" :schema="detailSchema" />
  </BasicDrawer>
</template>

<script lang="ts" setup>
  import { ref } from 'vue';
  import { BasicDrawer, useDrawerInner } from '/@/components/Drawer';
  import { Description } from '/@/components/Description';
  import { getDetail } from './api';
  
  defineOptions({ name: '{{component-name}}' });
  
  const detailData = ref({});
  
  const detailSchema = [
    { field: 'username', label: '用户名' },
    { field: 'realname', label: '真实姓名' },
    { field: 'email', label: '邮箱' },
    { field: 'phone', label: '电话' },
    { field: 'createTime', label: '创建时间' },
  ];
  
  const [registerDrawer] = useDrawerInner(async (data) => {
    const detail = await getDetail(data.record.id);
    detailData.value = detail;
  });
</script>
```

## 代码模板 - Modal 弹窗

```vue
<template>
  <BasicModal
    v-bind="$attrs"
    @register="registerModal"
    :title="getTitle"
    width="600"
    @ok="handleSubmit"
  >
    <BasicForm @register="registerForm" />
  </BasicModal>
</template>

<script lang="ts" setup>
  import { computed, ref, unref } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { BasicForm, useForm } from '/@/components/Form';
  import type { FormSchema } from '/@/components/Form';
  import { useMessage } from '/@/hooks/web/useMessage';
  
  defineOptions({ name: '{{component-name}}' });
  
  const emit = defineEmits(['success', 'register']);
  
  const { createMessage } = useMessage();
  
  const isUpdate = ref(false);
  
  const formSchemas: FormSchema[] = [
    // 表单字段...
  ];
  
  const [registerForm, { validate, setFieldsValue, resetFields }] = useForm({
    labelWidth: 100,
    schemas: formSchemas,
    showActionButtonGroup: false,
  });
  
  const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
    resetFields();
    setModalProps({ confirmLoading: false });
    isUpdate.value = !!data?.isUpdate;
    
    if (unref(isUpdate)) {
      setFieldsValue(data.record);
    }
  });
  
  const getTitle = computed(() => (unref(isUpdate) ? '编辑' : '新增'));
  
  async function handleSubmit() {
    try {
      const values = await validate();
      setModalProps({ confirmLoading: true });
      // 提交逻辑...
      createMessage.success('操作成功');
      closeModal();
      emit('success');
    } finally {
      setModalProps({ confirmLoading: false });
    }
  }
</script>
```

## Drawer/Modal 属性

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| title | string | - | 标题 |
| width | string/number | 600 | 宽度 |
| showFooter | boolean | true | 显示底部按钮 |
| showOkBtn | boolean | true | 显示确定按钮 |
| showCancelBtn | boolean | true | 显示取消按钮 |
| okText | string | 确定 | 确定按钮文字 |
| cancelText | string | 取消 | 取消按钮文字 |
| confirmLoading | boolean | false | 确认按钮加载状态 |
| closable | boolean | true | 显示关闭按钮 |
| maskClosable | boolean | true | 点击遮罩关闭 |

## useDrawerInner 方法

| 方法 | 说明 |
|------|------|
| setDrawerProps | 设置抽屉属性 |
| closeDrawer | 关闭抽屉 |
| openDrawer | 打开抽屉（父组件调用） |

## 使用说明

1. 确定抽屉类型（编辑/详情/确认）
2. 配置表单字段或详情展示项
3. 实现数据加载和提交逻辑
4. 在父组件中使用 `useDrawer` 注册并打开抽屉
5. 使用 `/@/` 路径别名导入
6. 遵循 project_rules.md 中的命名规范

## 父组件使用示例

```vue
<template>
  <div>
    <a-button @click="handleAdd">新增</a-button>
    <UserDrawer @register="registerDrawer" @success="handleSuccess" />
  </div>
</template>

<script setup>
  import { useDrawer } from '/@/components/Drawer';
  import UserDrawer from './UserDrawer.vue';
  
  const [registerDrawer, { openDrawer }] = useDrawer();
  
  function handleAdd() {
    openDrawer(true, { isUpdate: false });
  }
  
  function handleEdit(record) {
    openDrawer(true, { isUpdate: true, record });
  }
  
  function handleSuccess() {
    // 刷新列表
  }
</script>
```
