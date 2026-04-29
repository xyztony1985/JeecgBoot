<template>
  <div class="cust-onl-form">
    <a-spin :spinning="spinLoading">
      <online-form
        ref="onlineFormCompRef"
        :id="formId"
        :disabled="disabled"
        :form-template="formTemplate"
        :isTree="isTreeForm"
        :pidField="pidFieldName"
        :taskId="taskId"
        @rendered="renderSuccess"
        @success="handleSuccess"
        @validate="validateBack"
        @close="handleClose"
      >
        <template #bottom>
          <div style="width: 100%; text-align: center; margin-top: 5px" v-if="!disabled && !spinLoading && showSubmitButton">
            <a-button preIcon="ant-design:check" style="width: 126px" type="primary" @click="handleSubmit" :loading="buttonLoading"> 提 交 </a-button>
          </div>
          <!-- 任务办理意见 -->
          <TaskOpinionList class="task-opinion" :taskId="taskOriginalId" :procInsId="procInsId" :processTabType="processTabType" />
        </template>
      </online-form>
    </a-spin>
  </div>
</template>

<script>
  /**
   * 工作流online表单调--中转的意义在于在此查询表单信息
   */
  import OnlineForm from './OnlineForm.vue';
  import { defineComponent, ref, watch, nextTick, computed } from 'vue';
  import { defHttp } from '/@/utils/http/axios';
  import { getRefPromise } from '../../hooks/auto/useAutoForm';
  import TaskOpinionList from "@/views/super/online/cgform/auto/comp/TaskOpinionList.vue";
  import {useGlobSetting} from "@/hooks/setting";

  export default defineComponent({
    name: 'ProcessOnlineForm',
    inheritAttrs: false,
    components: {
      TaskOpinionList,
      OnlineForm,
    },
    props: {
      dataId: {
        type: String,
        default: '',
      },
      tableName: {
        type: String,
        default: '',
      },
      taskId: {
        type: String,
        default: '',
      },
      taskOriginalId: {
        type: String,
        default: '',
      },
      procInsId: {
        type: String,
        default: '',
      },
      processTabType: {
        type: String,
        default: '',
      },
      disabled: {
        type: Boolean,
        default: false,
      },
    },
    emits: ['success','validate'],
    setup(props, { emit }) {
      const onlineFormCompRef = ref();
      const formId = ref('');
      const formTemplate = ref(1);
      const isTreeForm = ref(false);
      const pidFieldName = ref('');
      const spinLoading = ref(false);

      //监听表名改变 重新加载表单
      watch(
        () => props.tableName,
        (val) => {
          if (!val) {
            return;
          }
          loadFormItems();
        },
        { immediate: true }
      );
      //update-begin-author:liusq---date:2026-01-20--for: 增加判断，新弹窗online表单不显示提交按钮
      const showSubmitButton = computed(() => {
        return !useGlobSetting().useNewTaskModal;
      });
      //update-end-author:liusq---date:2026-01-20--for: 增加判断，新弹窗online表单不显示提交按钮
      //加载表单
      async function loadFormItems() {
        spinLoading.value = true;
        const url = `/online/cgform/api/getFormItemBytbname/${props.tableName}`;
        const params = { taskId: props.taskId };
        try {
          let result = await defHttp.get({ url, params });
          console.log('动态表单查询结果是：', result);
          formId.value = result.head.id;
          formTemplate.value = Number(result.head.formTemplate || 1);
          isTreeForm.value = result.head.isTree === 'Y';
          pidFieldName.value = result.head.treeParentIdField || '';
          await nextTick(async () => {
            let myForm = await getRefPromise(onlineFormCompRef);
            myForm.createRootProperties(result);
          });
        } catch (e) {
          console.error('流程表单查询异常', e);
        }
      }

      //渲染完成 装载数据
      async function renderSuccess() {
        let myForm = await getRefPromise(onlineFormCompRef);
        spinLoading.value = false;
        myForm.show(true, {
          id: props.dataId,
        });
      }

      //表单提交
      const buttonLoading = ref(false);
      async function handleSubmit(showTip = true) {
        buttonLoading.value = true;
        onlineFormCompRef.value.handleSubmit(showTip);
      }
      function handleSuccess(data = null) {
        buttonLoading.value = false;
        emit('success', data);
      }
      //表单校验
      function handleValidate() {
        onlineFormCompRef.value.handleValidate();
      }
      //表单校验失败
      function validateBack(data = null) {
        emit('validate', data);
      }
      function handleClose() {
        buttonLoading.value = false;
      }

      return {
        onlineFormCompRef,
        formId,
        formTemplate,
        isTreeForm,
        pidFieldName,
        renderSuccess,
        handleSuccess,
        handleClose,
        handleSubmit,
        handleValidate,
        validateBack,
        buttonLoading,
        spinLoading,
        showSubmitButton,
      };
    },
  });
</script>

<style scoped>
  .cust-onl-form .ant-input-disabled {
    background-color: #fff;
    color: #000;
  }
  .cust-onl-form .ant-select-disabled .ant-select-selection {
    background: #fff;
    color: #000;
  }
  .cust-onl-form .ant-input-number-disabled {
    background-color: #fff;
    color: #000;
  }
</style>
