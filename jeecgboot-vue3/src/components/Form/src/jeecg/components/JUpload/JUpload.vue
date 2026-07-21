<template>
  <div ref="containerRef" :class="`${prefixCls}-container`">
    <a-upload
      :headers="headers"
      :multiple="multiple"
      :action="uploadUrl"
      :fileList="fileList"
      :disabled="disabled"
      v-bind="bindProps"
      @remove="onRemove"
      @change="onFileChange"
      @preview="onFilePreview"
    >
      <template v-if="isImageMode">
        <div v-if="!isMaxCount">
          <Icon icon="ant-design:plus-outlined" />
          <div class="ant-upload-text">{{ text }}</div>
        </div>
      </template>
      <a-button v-else-if="buttonVisible" :disabled="buttonDisabled">
        <Icon icon="ant-design:upload-outlined" />
        <span>{{ text }}</span>
      </a-button>
    </a-upload>
  </div>
</template>

<script lang="ts" setup>
  import { ref, reactive, computed, watch, nextTick, createApp,unref } from 'vue';
  import { Icon } from '/@/components/Icon';
  import { getToken } from '/@/utils/auth';
  import { uploadUrl, uploadManagedUrl } from '/@/api/common/api';
  import { propTypes } from '/@/utils/propTypes';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { createImgPreview } from '/@/components/Preview/index';
  import { useAttrs } from '/@/hooks/core/useAttrs';
  import { useDesign } from '/@/hooks/web/useDesign';
  import { UploadTypeEnum } from './upload.data';
  import { getFileAccessHttpUrl, getHeaders } from '/@/utils/common/compUtils';
  import UploadItemActions from './components/UploadItemActions.vue';
  import { split } from '/@/utils/index';
  import { useGlobSetting } from '/@/hooks/setting';

  const { createMessage, createConfirm } = useMessage();
  const { prefixCls } = useDesign('j-upload');
  const { apiUrl } = useGlobSetting();
  const attrs = useAttrs();
  const emit = defineEmits(['change', 'update:value']);
  const props = defineProps({
    value: propTypes.oneOfType([propTypes.string, propTypes.array]),
    text: propTypes.string.def('上传'),
    fileType: propTypes.string.def(UploadTypeEnum.all),
    /*这个属性用于控制文件上传的业务路径*/
    bizPath: propTypes.string.def('temp'),
    /**
     * 业务标识，格式：{table_name}.{field_name}
     * 传入即为托管模式，返回 file_id，文件信息存入 sys_attachment 表
     * 不传则为旧模式，返回文件路径（默认，兼容旧方式）
     * 示例：'my_report.attachment'
     */
    bizCode: propTypes.string.def(''),
    /**
     * 是否返回url，
     * true：仅返回url
     * false：返回fileName filePath fileSize
     */
    returnUrl: propTypes.bool.def(true),
    // 最大上传数量
    maxCount: propTypes.number.def(0),
    buttonVisible: propTypes.bool.def(true),
    multiple: propTypes.bool.def(true),
    // 是否显示左右移动按钮
    mover: propTypes.bool.def(true),
    // 是否显示下载按钮
    download: propTypes.bool.def(true),
    // 删除时是否显示确认框
    removeConfirm: propTypes.bool.def(false),
    beforeUpload: propTypes.func,
    disabled: propTypes.bool.def(false),
    // 替换前一个文件，用于超出最大数量依然允许上传
    replaceLastOne: propTypes.bool.def(false),
  });

  const headers = getHeaders();
  const fileList = ref<any[]>([]);
  const uploadGoOn = ref<boolean>(true);
  // refs
  const containerRef = ref();
  // 是否达到了最大上传数量
  const isMaxCount = computed(() => props.maxCount > 0 && fileList.value.length >= props.maxCount);
  // 当前是否是上传图片模式
  const isImageMode = computed(() => props.fileType === UploadTypeEnum.image);
  // 上传按钮是否禁用
  const buttonDisabled = computed(()=>{
    if(props.disabled === true){
      return true;
    }
    if(isMaxCount.value === true){
      if(props.replaceLastOne === true){
        return false
      }else{
        return true;
      }
    }
    return false
  });
  // bizCode 格式校验：{table_name}.{field_name}，只允许字母、数字、下划线、连字符
  const BIZ_CODE_PATTERN = /^[a-zA-Z0-9_-]+\.[a-zA-Z0-9_-]+$/;

  // 校验 bizCode 格式
  const isValidBizCode = computed(() => {
    if (!props.bizCode) return false;
    return BIZ_CODE_PATTERN.test(props.bizCode);
  });

  // 监听 bizCode 格式错误，给出警告
  watch(
    () => props.bizCode,
    (val) => {
      if (val && !BIZ_CODE_PATTERN.test(val)) {
        createMessage.warning(`bizCode 格式错误: "${val}"，正确格式为: 表名.字段名（只允许字母、数字、下划线、连字符）`);
      }
    },
    { immediate: true }
  );

  // 合并 props 和 attrs
  const bindProps = computed(() => {
    // 代码逻辑说明: [issue/455]上传组件传入accept限制上传文件类型无效
    const bind: any = Object.assign({}, props, unref(attrs));

    bind.name = 'file';
    bind.listType = isImageMode.value ? 'picture-card' : 'text';
    bind.class = [bind.class, { 'upload-disabled': props.disabled }];
    
    // bizCode 有值但格式错误时，禁用上传
    const bizCodeInvalid = props.bizCode && !isValidBizCode.value;
    if (bizCodeInvalid) {
      bind.disabled = true;
    }
    
    // 托管模式：action 切换为 /sys/file/upload；旧模式：保持 /sys/common/upload
    bind.action = isValidBizCode.value ? uploadManagedUrl : uploadUrl;
    bind.data = {
      ...(isValidBizCode.value ? { bizCode: props.bizCode } : { biz: props.bizPath }),
      ...bind.data,
    };
    // 代码逻辑说明: 自定义beforeUpload return false，并不能中断上传过程
    if (!bind.beforeUpload) {
      bind.beforeUpload = onBeforeUpload;
    }
    // 如果当前是图片上传模式，就只能上传图片
    if (isImageMode.value && !bind.accept) {
      bind.accept = 'image/*';
    }
    return bind;
  });

  watch(
    () => props.value,
    (val) => {
      if (Array.isArray(val)) {
        if (props.returnUrl) {
          parsePathsValue(val.join(','));
        } else {
          parseArrayValue(val);
        }
      } else {
        // 代码逻辑说明: [issues/5327]Upload组件returnUrl为false时上传的字段值返回了一个'[object Object]' ------------
        if (props.returnUrl) {
          parsePathsValue(val);
        } else {
          val && parseArrayValue(JSON.parse(val));
        }
      }
    },
    { immediate: true }
  );

  watch(fileList, () => nextTick(() => addActionsListener()), { immediate: true });

  const antUploadItemCls = 'ant-upload-list-item';

  // Listener
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

  // 添加可左右移动的按钮
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
    // 添加操作按钮
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

  // 解析数据库存储的逗号分割
  function parsePathsValue(paths) {
    if (!paths || paths.length == 0) {
      fileList.value = [];
      return;
    }
    let list: any[] = [];
    // 代码逻辑说明: 【issues/7990】图片参数中包含逗号会错误的识别成多张图
    const result = split(paths);
    for (const item of result) {
      let url;
      if (props.bizCode) {
        // 托管模式：item 是 file_id，使用 view 接口预览
        url = `${apiUrl}/sys/file/view/${item}`;
      } else {
        url = getFileAccessHttpUrl(item);
      }
      list.push({
        uid: uidGenerator(),
        name: getFileName(item),
        status: 'done',
        url: url,
        response: { status: 'history', message: item },
      });
    }
    fileList.value = list;
  }

  // 解析数组值
  function parseArrayValue(array) {
    if (!array || array.length == 0) {
      fileList.value = [];
      return;
    }
    let list: any[] = [];
    for (const item of array) {
      let url;
      if (props.bizCode) {
        // 托管模式：item.fileId 是 file_id，使用 view 接口预览
        url = `${apiUrl}/sys/file/view/${item.fileId}`;
      } else {
        url = getFileAccessHttpUrl(item.filePath);
      }
      list.push({
        uid: uidGenerator(),
        name: item.fileName,
        url: url,
        status: 'done',
        response: { status: 'history', message: props.bizCode ? item.fileId : item.filePath },
      });
    }
    fileList.value = list;
  }

  // 文件上传之前的操作
  function onBeforeUpload(file) {
    uploadGoOn.value = true;
    if (isImageMode.value) {
      if (file.type.indexOf('image') < 0) {
        createMessage.warning('请上传图片');
        uploadGoOn.value = false;
        return false;
      }
    }
    // 扩展 beforeUpload 验证
    if (typeof props.beforeUpload === 'function') {
      return props.beforeUpload(file);
    }
    return true;
  }

  // 删除处理事件
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

  // upload组件change事件
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
          if (file.response) {
            let reUrl = file.response.message;
            if (props.bizCode) {
              // 托管模式：response.message 是 file_id
              file.fileId = reUrl;
              file.url = `${apiUrl}/sys/file/view/${reUrl}`;
            } else {
              file.url = getFileAccessHttpUrl(reUrl);
            }
          }
          return file;
        });
      }else{
        successFileList = fileListTemp.filter(item=>{
          return item.uid!=info.file.uid;
        });
        createMessage.error(`${info.file.name} 上传失败.`);
      }
      fileListTemp = successFileList;
    } else if (info.file.status === 'error') {
      createMessage.error(`${info.file.name} 上传失败.`);
    }
    // beforeUpload 返回false，则没有status
    info.file.status && (fileList.value = fileListTemp);
    if (info.file.status === 'done' || info.file.status === 'removed') {
      //returnUrl为true时仅返回文件路径
      if (props.returnUrl) {
        handlePathChange();
      } else {
        //returnUrl为false时返回文件名称、文件路径及文件大小
        let newFileList: any[] = [];
        for (const item of fileListTemp) {
          if (item.status === 'done') {
            let fileJson: Record<string, any> = {
              fileName: item.name,
              filePath: item.response.message,
              fileSize: item.size,
            };
            if (props.bizCode) {
              fileJson.fileId = item.fileId || item.response.message;
            }
            newFileList.push(fileJson);
          }else{
            return;
          }
        }
        // 代码逻辑说明: [issues/5327]Upload组件returnUrl为false时上传的字段值返回了一个'[object Object]' ------------
        emitValue(JSON.stringify(newFileList));
      }
    }
  }

  function handlePathChange() {
    let uploadFiles = fileList.value;
    let path = '';
    if (!uploadFiles || uploadFiles.length == 0) {
      path = '';
    }
    let pathList: string[] = [];
    for (const item of uploadFiles) {
      if (item.status === 'done') {
        if (props.bizCode) {
          // 托管模式：返回 file_id
          pathList.push(item.fileId || item.response.message);
        } else {
          pathList.push(item.response.message);
        }
      } else {
        return;
      }
    }
    if (pathList.length > 0) {
      path = pathList.join(',');
    }
    emitValue(path);
  }

  // 预览文件、图片
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

  function getFileName(path) {
    if (path.lastIndexOf('\\') >= 0) {
      let reg = new RegExp('\\\\', 'g');
      path = path.replace(reg, '/');
    }
    return path.substring(path.lastIndexOf('/') + 1);
  }

  defineExpose({
    addActionsListener,
  });
</script>

<style lang="less">
  //noinspection LessUnresolvedVariable
  @prefix-cls: ~'@{namespace}-j-upload';

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

        /* update-begin-author:taoyan date:2022-5-24 for:VUEN-1093详情界面 图片下载按钮显示不全*/
        .upload-download-handler {
          right: 6px !important;
        }
        /* update-end-author:taoyan date:2022-5-24 for:VUEN-1093详情界面 图片下载按钮显示不全*/
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
