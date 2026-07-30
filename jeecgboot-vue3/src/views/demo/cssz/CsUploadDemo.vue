<template>
  <PageWrapper title="CsUpload 组件示例">
    <a-card title="基础用法" :bordered="false">
      <a-form :label-col="{ span: 4 }" :wrapper-col="{ span: 18 }">
        <a-form-item label="上传文件">
          <CsUpload v-model:value="formData.files" :bizCode="bizCode" />
        </a-form-item>
        <a-form-item label="当前值">
          <a-textarea :value="formData.files" readonly :rows="2" />
        </a-form-item>
      </a-form>
    </a-card>

    <a-card title="图片模式" :bordered="false" style="margin-top: 16px">
      <a-form :label-col="{ span: 4 }" :wrapper-col="{ span: 18 }">
        <a-form-item label="上传图片">
          <CsUpload
            v-model:value="formData.images"
            :bizCode="bizCode"
            fileType="image"
            :maxCount="3"
          />
        </a-form-item>
        <a-form-item label="当前值">
          <a-textarea :value="formData.images" readonly :rows="2" />
        </a-form-item>
      </a-form>
    </a-card>

    <a-card title="限制上传数量" :bordered="false" style="margin-top: 16px">
      <a-form :label-col="{ span: 4 }" :wrapper-col="{ span: 18 }">
        <a-form-item label="最多上传2个">
          <CsUpload
            v-model:value="formData.limitedFiles"
            :bizCode="bizCode"
            :maxCount="2"
          />
        </a-form-item>
        <a-form-item label="当前值">
          <a-textarea :value="formData.limitedFiles" readonly :rows="2" />
        </a-form-item>
      </a-form>
    </a-card>

    <a-card title="禁用状态" :bordered="false" style="margin-top: 16px">
      <a-form :label-col="{ span: 4 }" :wrapper-col="{ span: 18 }">
        <a-form-item label="禁用上传">
          <CsUpload
            v-model:value="formData.disabledFiles"
            :bizCode="bizCode"
            disabled
          />
        </a-form-item>
      </a-form>
    </a-card>

    <a-card title="删除确认" :bordered="false" style="margin-top: 16px">
      <a-form :label-col="{ span: 4 }" :wrapper-col="{ span: 18 }">
        <a-form-item label="删除时确认">
          <CsUpload
            v-model:value="formData.confirmFiles"
            :bizCode="bizCode"
            :removeConfirm="true"
          />
        </a-form-item>
      </a-form>
    </a-card>

    <a-card title="自定义按钮文字" :bordered="false" style="margin-top: 16px">
      <a-form :label-col="{ span: 4 }" :wrapper-col="{ span: 18 }">
        <a-form-item label="自定义文字">
          <CsUpload
            v-model:value="formData.customText"
            :bizCode="bizCode"
            text="选择附件"            
          />
        </a-form-item>
      </a-form>
    </a-card>

    <a-card title="FormSchema 写法" :bordered="false" style="margin-top: 16px">
      <BasicForm @register="registerForm" />
    </a-card>
  </PageWrapper>
</template>

<script lang="ts" setup>
  import { reactive } from 'vue';
  import { PageWrapper } from '/@/components/Page';
  import { BasicForm, FormSchema, useForm } from '/@/components/Form';

  // bizCode 格式：表名.字段名
  const bizCode = 'demo_test.attachment';

  const formData = reactive({
    files: '',
    images: '',
    limitedFiles: '',
    disabledFiles: '1234567890,0987654321', // 模拟已有数据
    confirmFiles: '',
    customText: '',
  });

  // FormSchema 写法示例
  const formSchemas: FormSchema[] = [
    {
      field: 'files',
      label: '普通文件',
      component: 'CsUpload',
      componentProps: { bizCode },
    },
    {
      field: 'images',
      label: '图片文件',
      component: 'CsUpload',
      componentProps: { bizCode, fileType: 'image', maxCount: 3 },
    },
    {
      field: 'limited',
      label: '限制数量',
      component: 'CsUpload',
      componentProps: { bizCode, maxCount: 2 },
    },
  ];

  const [registerForm] = useForm({
    schemas: formSchemas,
    showActionButtonGroup: false,
    // baseColProps: { span: 12 },
    // labelWidth: 120,
  });
</script>

<style scoped>
  :deep(.ant-card) {
    margin-bottom: 16px;
  }
</style>
