<template>
  <PageWrapper title="附件托管模式演示" content="对比旧模式（bizPath）与托管模式（bizCode）的上传差异">
    <a-card title="旧模式（bizPath）" :bordered="false" style="margin-bottom: 16px">
      <a-alert message="旧模式：业务表直接存储文件路径，逗号分隔" type="info" show-icon style="margin-bottom: 16px" />
      <BasicForm @register="registerOld" />
      <a-divider>返回值</a-divider>
      <a-descriptions :column="1" bordered size="small">
        <a-descriptions-item label="表单值（路径字符串）">
          <span style="word-break: break-all">{{ oldFormValues || '暂无' }}</span>
        </a-descriptions-item>
      </a-descriptions>
    </a-card>

    <a-card title="托管模式（bizCode）" :bordered="false" style="margin-bottom: 16px">
      <a-alert message="托管模式：业务表存储 file_id，逗号分隔；文件信息存入 sys_attachment 表" type="success" show-icon style="margin-bottom: 16px" />
      <BasicForm @register="registerManaged" />
      <a-divider>返回值</a-divider>
      <a-descriptions :column="1" bordered size="small">
        <a-descriptions-item label="表单值（file_id 字符串）">
          <span style="word-break: break-all">{{ managedFormValues || '暂无' }}</span>
        </a-descriptions-item>
      </a-descriptions>
    </a-card>

    <a-card title="托管模式（returnUrl=false）" :bordered="false" style="margin-bottom: 16px">
      <a-alert message="returnUrl=false 时，返回 JSON 数组，包含 fileName、filePath、fileSize、fileId" type="warning" show-icon style="margin-bottom: 16px" />
      <BasicForm @register="registerManagedJson" />
      <a-divider>返回值</a-divider>
      <a-descriptions :column="1" bordered size="small">
        <a-descriptions-item label="表单值（JSON 数组）">
          <pre style="word-break: break-all; white-space: pre-wrap; margin: 0">{{ managedJsonValues || '暂无' }}</pre>
        </a-descriptions-item>
      </a-descriptions>
    </a-card>

    <a-card title="文件预览（托管模式）" :bordered="false">
      <a-alert message="使用 /sys/file/view/{fileId} 接口预览，支持所有存储方式" type="info" show-icon style="margin-bottom: 16px" />
      <a-form layout="inline">
        <a-form-item label="file_id">
          <a-input v-model:value="previewFileId" placeholder="输入 file_id 预览文件" style="width: 400px" />
        </a-form-item>
        <a-form-item>
          <a-button type="primary" :disabled="!previewFileId" @click="handlePreview">预览</a-button>
        </a-form-item>
      </a-form>
      <div v-if="previewFileId" style="margin-top: 16px">
        <p>预览地址：<a :href="previewUrl" target="_blank">{{ previewUrl }}</a></p>
        <img v-if="isImage" :src="previewUrl" style="max-width: 400px; max-height: 300px; border: 1px solid #ddd; border-radius: 4px" />
      </div>
    </a-card>
  </PageWrapper>
</template>

<script lang="ts" setup>
  import { ref, computed } from 'vue';
  import { PageWrapper } from '/@/components/Page';
  import { FormSchema, useForm, BasicForm } from '/@/components/Form';
  import { UploadTypeEnum } from '/@/components/Form/src/jeecg/components/JUpload';
  import { buildBizCode } from '/@/utils/common/fileHelper';

  // bizCode 示例：假设业务表为 demo_attachment，字段为 file
  const demoBizCode = buildBizCode('demo_attachment', 'file');

  // ========== 旧模式 ==========
  const oldFormValues = ref('');

  const oldSchemas: FormSchema[] = [
    {
      field: 'filePath',
      component: 'JUpload',
      label: '上传文件',
      helpMessage: '旧模式：返回文件路径，存储在业务表中',
      componentProps: {
        bizPath: 'demo/old',
        onChange: (val) => {
          oldFormValues.value = val;
        },
      },
    },
    {
      field: 'imagePath',
      component: 'JUpload',
      label: '上传图片',
      componentProps: {
        bizPath: 'demo/old/image',
        fileType: UploadTypeEnum.image,
        onChange: (val) => {
          oldFormValues.value = val;
        },
      },
    },
  ];

  const [registerOld] = useForm({
    labelWidth: 120,
    schemas: oldSchemas,
    actionColOptions: { span: 24 },
    compact: true,
    showResetButton: false,
    showSubmitButton: false,
    showAdvancedButton: false,
  });

  // ========== 托管模式（returnUrl=true） ==========
  const managedFormValues = ref('');

  const managedSchemas: FormSchema[] = [
    {
      field: 'fileId',
      component: 'JUpload',
      label: '上传文件',
      helpMessage: `托管模式：bizCode=${demoBizCode}，返回 file_id`,
      componentProps: {
        bizCode: demoBizCode,
        onChange: (val) => {
          managedFormValues.value = val;
        },
      },
    },
    {
      field: 'imageId',
      component: 'JUpload',
      label: '上传图片',
      componentProps: {
        bizCode: buildBizCode('demo_attachment', 'image'),
        fileType: UploadTypeEnum.image,
        onChange: (val) => {
          managedFormValues.value = val;
        },
      },
    },
  ];

  const [registerManaged] = useForm({
    labelWidth: 120,
    schemas: managedSchemas,
    actionColOptions: { span: 24 },
    compact: true,
    showResetButton: false,
    showSubmitButton: false,
    showAdvancedButton: false,
  });

  // ========== 托管模式（returnUrl=false） ==========
  const managedJsonValues = ref('');

  const managedJsonSchemas: FormSchema[] = [
    {
      field: 'fileJson',
      component: 'JUpload',
      label: '上传文件（JSON）',
      helpMessage: 'returnUrl=false，返回包含 fileId 的 JSON 数组',
      componentProps: {
        bizCode: demoBizCode,
        returnUrl: false,
        onChange: (val) => {
          managedJsonValues.value = val;
        },
      },
    },
  ];

  const [registerManagedJson] = useForm({
    labelWidth: 120,
    schemas: managedJsonSchemas,
    actionColOptions: { span: 24 },
    compact: true,
    showResetButton: false,
    showSubmitButton: false,
    showAdvancedButton: false,
  });

  // ========== 文件预览 ==========
  const previewFileId = ref('');

  const previewUrl = computed(() => {
    if (!previewFileId.value) return '';
    return `/sys/file/view/${previewFileId.value}`;
  });

  const isImage = computed(() => {
    // 简单判断：如果 URL 是图片类型则显示 img 标签
    // 实际使用时可根据 file_id 调用 /sys/file/info 获取 file_type 判断
    return false;
  });

  function handlePreview() {
    if (previewFileId.value) {
      window.open(previewUrl.value, '_blank');
    }
  }
</script>
