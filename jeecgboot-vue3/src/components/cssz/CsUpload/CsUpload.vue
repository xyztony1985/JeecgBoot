<template>
  <div ref="containerRef" :class="`${prefixCls}-container`">
    <a-upload
      :headers="headers"
      :multiple="multiple"
      :action="uploadManagedUrl"
      :fileList="fileList"
      :disabled="disabled"
      v-bind="bindProps"
      @remove="onRemove"
      @change="onFileChange"
      @preview="onFilePreview"
    >
      <template v-if="isImageMode">
        <a-tooltip v-if="!isMaxCount" :title="disableTip" placement="top">
          <div :class="{ 'upload-trigger-disabled': !isValidBizCode }">
            <Icon icon="ant-design:plus-outlined" />
            <div class="ant-upload-text">{{ text }}</div>
          </div>
        </a-tooltip>
      </template>
      <a-tooltip v-else-if="buttonVisible" :title="disableTip" placement="top">
        <a-button :disabled="buttonDisabled">
          <Icon icon="ant-design:upload-outlined" />
          <span>{{ text }}</span>
        </a-button>
      </a-tooltip>
    </a-upload>
  </div>
</template>

<script lang="ts" setup>
  defineOptions({ name: 'CsUpload' });

  import { ref, computed, watch, nextTick, createApp, unref } from 'vue';
  import { Icon } from '/@/components/Icon';
  import { uploadManagedUrl, getFileInfo } from '/@/api/common/api';
  import { propTypes } from '/@/utils/propTypes';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { createImgPreview } from '/@/components/Preview/index';
  import { useAttrs } from '/@/hooks/core/useAttrs';
  import { useDesign } from '/@/hooks/web/useDesign';
  import { getHeaders } from '/@/utils/common/compUtils';
  import { useGlobSetting } from '/@/hooks/setting';
  import UploadItemActions from './components/UploadItemActions.vue';

  const { createMessage, createConfirm } = useMessage();
  const { prefixCls } = useDesign('cs-upload');
  const { apiUrl } = useGlobSetting();
  const attrs = useAttrs();
  const emit = defineEmits(['change', 'update:value']);

  const props = defineProps({
    /**
     * 绑定值，逗号分隔的 file_id 字符串
     */
    value: propTypes.string.def(''),
    /**
     * 上传按钮/提示文字
     */
    text: propTypes.string.def('上传'),
    /**
     * 上传类型：'all' / 'image' / 'file'
     */
    fileType: propTypes.string.def('all'),
    /**
     * 业务标识（必填），格式：{table_name}.{field_name}
     * 用于托管模式，文件信息存入 sys_attachment 表，返回逗号分隔的 file_id
     * 示例：'my_report.attachment'
     */
    bizCode: propTypes.string.require,
    /**
     * 最大上传数量，0 表示不限
     */
    maxCount: propTypes.number.def(0),
    /**
     * 是否显示上传按钮
     */
    buttonVisible: propTypes.bool.def(true),
    /**
     * 是否允许多文件上传
     */
    multiple: propTypes.bool.def(true),
    /**
     * 是否显示左右移动按钮（图片模式）
     */
    mover: propTypes.bool.def(true),
    /**
     * 是否显示下载按钮
     */
    download: propTypes.bool.def(true),
    /**
     * 删除时是否显示确认框
     */
    removeConfirm: propTypes.bool.def(false),
    /**
     * 上传前自定义校验函数
     */
    beforeUpload: propTypes.func,
    /**
     * 是否禁用
     */
    disabled: propTypes.bool.def(false),
    /**
     * 超出最大数量时是否替换最后一个文件
     */
    replaceLastOne: propTypes.bool.def(false),
  });

  const headers = getHeaders();
  const fileList = ref<any[]>([]);
  const uploadGoOn = ref<boolean>(true);
  const containerRef = ref();

  // bizCode 格式校验：{table_name}.{field_name}，只允许字母、数字、下划线、连字符
  const BIZ_CODE_PATTERN = /^[a-zA-Z0-9_-]+\.[a-zA-Z0-9_-]+$/;

  // 是否达到最大上传数
  const isMaxCount = computed(() => props.maxCount > 0 && fileList.value.length >= props.maxCount);

  // 是否为图片上传模式
  const isImageMode = computed(() => props.fileType === 'image');

  // 校验 bizCode 格式
  const isValidBizCode = computed(() => {
    if (!props.bizCode) return false;
    return BIZ_CODE_PATTERN.test(props.bizCode);
  });

  // 上传按钮是否禁用
  const buttonDisabled = computed(() => {
    if (props.disabled === true) {
      return true;
    }
    // bizCode 格式错误时禁用
    if (!isValidBizCode.value) {
      return true;
    }
    if (isMaxCount.value === true) {
      if (props.replaceLastOne === true) {
        return false;
      } else {
        return true;
      }
    }
    return false;
  });

  // 按钮禁用时的提示文本
  const disableTip = computed(() => {
    if (!buttonDisabled.value) return '';
    if (isMaxCount.value && !props.replaceLastOne) {
      return `已达到最大上传数量（${props.maxCount}）`;
    }
    if (!isValidBizCode.value) {
      return `bizCode 格式错误: "${props.bizCode}"，正确格式为: 表名.字段名（只允许字母、数字、下划线、连字符）`;
    }
    return '';
  });

  // 合并 props 和 attrs
  const bindProps = computed(() => {
    const bind: any = Object.assign({}, props, unref(attrs));

    bind.name = 'file';
    bind.listType = isImageMode.value ? 'picture-card' : 'text';
    bind.class = [bind.class, { 'upload-disabled': props.disabled }];

    // bizCode 格式错误时，禁用上传
    if (!isValidBizCode.value) {
      bind.disabled = true;
    }

    // 托管模式：action 固定为 /sys/file/upload
    bind.action = uploadManagedUrl;
    bind.data = {
      bizCode: props.bizCode,
      ...bind.data,
    };

    // 自定义 beforeUpload
    if (!bind.beforeUpload) {
      bind.beforeUpload = onBeforeUpload;
    }

    // 图片模式限制上传类型
    if (isImageMode.value && !bind.accept) {
      bind.accept = 'image/*';
    }

    return bind;
  });

  // 监听 value 变化，解析为 fileList
  watch(
    () => props.value,
    (val) => {
      parseFileIdsValue(val);
    },
    { immediate: true }
  );

  // 监听 fileList 变化，添加操作按钮监听器
  watch(fileList, () => nextTick(() => addActionsListener()), { immediate: true });

  const antUploadItemCls = 'ant-upload-list-item';

  // 添加操作按钮监听器（图片模式）
  function addActionsListener() {
    if (!isImageMode.value) {
      return;
    }
    const uploadItems = containerRef.value ? containerRef.value.getElementsByClassName(antUploadItemCls) : null;
    if (!uploadItems || uploadItems.length === 0) {
      return;
    }
    for (const uploadItem of uploadItems) {
      let hasActions = uploadItem.getAttribute('data-has-actions') === 'true';
      if (!hasActions) {
        uploadItem.addEventListener('mouseover', onAddActionsButton);
      }
    }
  }

  // 添加操作按钮（图片模式：下载、左右移动）
  function onAddActionsButton(event) {
    const getUploadItem = () => {
      for (const path of event.path) {
        if (path.classList.contains(antUploadItemCls)) {
          return path;
        } else if (path.classList.contains(`${prefixCls}-container`)) {
          return null;
        }
      }
      return null;
    };
    const uploadItem = getUploadItem();
    if (!uploadItem) {
      return;
    }
    const actions = uploadItem.getElementsByClassName('ant-upload-list-item-actions');
    if (!actions || actions.length === 0) {
      return;
    }
    // 创建操作按钮容器
    const div = document.createElement('div');
    div.className = 'upload-actions-container';
    createApp(UploadItemActions, {
      element: uploadItem,
      fileList: fileList,
      mover: props.mover,
      download: props.download,
      emitValue: emitValue,
    }).mount(div);
    actions[0].appendChild(div);
    uploadItem.setAttribute('data-has-actions', 'true');
    uploadItem.removeEventListener('mouseover', onAddActionsButton);
  }

  // 解析 file_id 字符串为 fileList
  function parseFileIdsValue(fileIds) {
    if (!fileIds || fileIds.length === 0) {
      fileList.value = [];
      return;
    }
    const idList = fileIds.split(',').filter((id) => id.trim());
    const list: any[] = [];
    for (const fileId of idList) {
      list.push({
        uid: uidGenerator(),
        name: fileId, // 初始显示 file_id，后续通过接口获取文件名
        status: 'done',
        url: `${apiUrl}/sys/file/view/${fileId}`,
        fileId: fileId,
        response: { status: 'history', fileId: fileId },
      });
    }
    fileList.value = list;
    // 异步获取原始文件名
    fetchFileNames(list);
  }

  // 异步获取文件原始文件名
  async function fetchFileNames(list: any[]) {
    for (const item of list) {
      if (item.fileId && item.name === item.fileId) {
        try {
          const info = await getFileInfo(item.fileId);
          if (info && info.fileName) {
            item.name = info.fileName;
          }
        } catch (e) {
          // 获取失败时保持 file_id 作为名称
        }
      }
    }
  }

  // 上传前校验
  function onBeforeUpload(file) {
    uploadGoOn.value = true;
    if (isImageMode.value) {
      if (file.type.indexOf('image') < 0) {
        createMessage.warning('请上传图片');
        uploadGoOn.value = false;
        return false;
      }
    }
    if (typeof props.beforeUpload === 'function') {
      return props.beforeUpload(file);
    }
    return true;
  }

  // 删除处理
  function onRemove() {
    if (props.removeConfirm) {
      return new Promise((resolve) => {
        createConfirm({
          title: '删除',
          content: `确定要删除这${isImageMode.value ? '张图片' : '个文件'}吗？`,
          iconType: 'warning',
          onOk: () => resolve(true),
          onCancel: () => resolve(false),
        });
      });
    }
    return true;
  }

  // 文件变化事件
  function onFileChange(info) {
    if (!info.file.status && uploadGoOn.value === false) {
      info.fileList.pop();
    }
    let fileListTemp = info.fileList;

    // 限制最大上传数
    if (props.maxCount > 0) {
      let count = fileListTemp.length;
      if (count >= props.maxCount) {
        let diffNum = props.maxCount - fileListTemp.length;
        if (diffNum >= 0) {
          fileListTemp = fileListTemp.slice(-props.maxCount);
        } else {
          return;
        }
      }
    }

    if (info.file.status === 'done') {
      let successFileList = [];
      if (info.file.response.success) {
        successFileList = fileListTemp.map((file) => {
          const result = file.response?.result;
          if (result) {
            // 新上传的文件：response.result 包含 fileId, fileName, fileSize
            file.fileId = result.fileId;
            file.url = `${apiUrl}/sys/file/view/${result.fileId}`;
            file.name = file.name || result.fileName;
          }
          return file;
        });
        createMessage.success(`${info.file.name} 上传成功`);
      } else {
        successFileList = fileListTemp.filter((item) => {
          return item.uid != info.file.uid;
        });
        createMessage.error(`${info.file.name} 上传失败.`);
      }
      fileListTemp = successFileList;
    } else if (info.file.status === 'error') {
      createMessage.error(`${info.file.name} 上传失败.`);
    }

    info.file.status && (fileList.value = fileListTemp);

    if (info.file.status === 'done' || info.file.status === 'removed') {
      handleFileIdChange();
    }
  }

  // 处理 file_id 变化，向外发送逗号分隔的 file_id 字符串
  function handleFileIdChange() {
    let uploadFiles = fileList.value;
    let fileIds = '';
    if (!uploadFiles || uploadFiles.length === 0) {
      fileIds = '';
    }
    let fileIdList: string[] = [];
    for (const item of uploadFiles) {
      if (item.status === 'done') {
        fileIdList.push(item.fileId);
      } else {
        return;
      }
    }
    if (fileIdList.length > 0) {
      fileIds = fileIdList.join(',');
    }
    emitValue(fileIds);
  }

  // 预览文件/图片
  function onFilePreview(file) {
    if (isImageMode.value) {
      createImgPreview({ imageList: [file.url], maskClosable: true });
    } else {
      window.open(file.url);
    }
  }

  function emitValue(value) {
    emit('change', value);
    emit('update:value', value);
  }

  function uidGenerator() {
    return '-' + parseInt(Math.random() * 10000 + 1, 10);
  }

  defineExpose({
    addActionsListener,
  });
</script>

<style lang="less">
  //noinspection LessUnresolvedVariable
  @prefix-cls: ~'@{namespace}-cs-upload';

  .@{prefix-cls} {
    &-container {
      position: relative;

      .upload-disabled {
        .ant-upload-list-item {
          .anticon-close {
            display: none;
          }

          .anticon-delete {
            display: none;
          }
        }
      }

      // bizCode 格式错误时的禁用样式（图片模式）
      .upload-trigger-disabled {
        opacity: 0.5;
        cursor: not-allowed;
      }

      .ant-upload-text-icon {
        color: @primary-color;
      }

      .ant-upload-list-item {
        .upload-actions-container {
          position: absolute;
          top: -31px;
          left: -18px;
          z-index: 11;
          width: 84px;
          height: 84px;
          line-height: 28px;
          text-align: center;
          pointer-events: none;

          a {
            opacity: 0.9;
            margin: 0 5px;
            cursor: pointer;
            transition: opacity 0.3s;

            .anticon {
              color: #fff;
              font-size: 16px;
            }

            &:hover {
              opacity: 1;
            }
          }

          .upload-mover-handler,
          .upload-download-handler {
            position: absolute;
            pointer-events: auto;
          }

          .upload-mover-handler {
            width: 100%;
            bottom: 0;
          }

          .upload-download-handler {
            top: -4px;
            right: -4px;
          }
        }
      }
    }
  }
</style>
