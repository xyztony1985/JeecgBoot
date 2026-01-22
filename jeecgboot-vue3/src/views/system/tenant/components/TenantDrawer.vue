<template>
  <BasicDrawer v-bind="$attrs" @register="registerDrawer" :title="title" width="700px" @ok="handleSubmit" destroyOnClose showFooter>
    <BasicForm @register="registerForm" />
  </BasicDrawer>
</template>
<script lang="ts" setup>
  import { ref, computed, unref } from 'vue';
  import { BasicDrawer, useDrawerInner } from '/@/components/Drawer';
  import { BasicForm, useForm } from '/@/components/Form';
  import { formSchema } from '../tenant.data';
  import { saveOrUpdateTenant, getTenantById } from '../tenant.api';

  const emit = defineEmits(['register', 'success']);
  const isUpdate = ref(true);
  // 表单配置
  const [registerForm, { resetFields, setFieldsValue, validate, updateSchema }] = useForm({
    schemas: formSchema,
    showActionButtonGroup: false,
  });

  const [registerDrawer, { setDrawerProps, closeDrawer }] = useDrawerInner(async (data) => {
    // 重置表单
    await resetFields();
    setDrawerProps({ confirmLoading: false });
    isUpdate.value = !!data?.isUpdate;
    if (unref(isUpdate)) {
      // 编辑模式下禁用id字段
      updateSchema({ field: 'id', dynamicDisabled: true });
      // 获取详情
      data.record = await getTenantById({ id: data.record.id });
      // 表单赋值
      await setFieldsValue({
        ...data.record,
      });
    } else {
      updateSchema({ field: 'id', dynamicDisabled: false });
    }
  });

  const title = computed(() => (!unref(isUpdate) ? '新增租户' : '编辑租户'));

  async function handleSubmit() {
    try {
      const values = await validate();
      setDrawerProps({ confirmLoading: true });
      // 提交表单
      await saveOrUpdateTenant(values, isUpdate.value);
      // 关闭抽屉
      closeDrawer();
      emit('success');
    } finally {
      setDrawerProps({ confirmLoading: false });
    }
  }
</script>
