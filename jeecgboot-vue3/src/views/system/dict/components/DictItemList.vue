<!-- 遗留问题：
 - 未充分测试，可能有问题
 - 拖动排序后，不保存，再进来还保持拖动后的顺序，正常应该是保持原始顺序
 - 列表的颜色字段，期望只显示圆形色块，不显示颜色值，未完成
  -->
<template>
  <BasicDrawer
    v-bind="$attrs"
    @register="registerDrawer"
    title="字典列表"
    width="900px"
    :showFooter="true"
    @ok="handleSave"
  >
    <BasicTable
      ref="tableRef"
      @register="registerTable"
      :dataSource="dataSource"
      :columns="tableColumns"
      :pagination="false"
      :rowClassName="getRowClassName"
    >
      <template v-slot:bodyCell="{column, record, index}">
        <template v-if="column.dataIndex === 'itemText'">
          <a-input v-model:value="record.itemText" placeholder="名称" :status="getFieldError(record, 'itemText')" @change="clearFieldError(record, 'itemText')" />
        </template>
        <template v-if="column.dataIndex === 'itemValue'">
          <a-input v-model:value="record.itemValue" placeholder="数据值" :status="getFieldError(record, 'itemValue')" @change="clearFieldError(record, 'itemValue')" />
        </template>
        <template v-if="column.dataIndex === 'itemColor'">
          <a-select
            v-model:value="record.itemColor"
            placeholder="选择颜色"
            allowClear
            :dropdownMatchSelectWidth="false"
            popupClassName="color-select-dropdown"
          >
            <a-select-option v-for="(color, idx) in colorOptions" :key="idx" :value="color.value">
              <div class="color-option-item">
                <div class="color-swatch" :style="{ background: color.value }"></div>
                <span>{{ color.value }}</span>
              </div>
            </a-select-option>
          </a-select>
        </template>
        <template v-if="column.dataIndex === 'sortOrder'">
          <div class="drag-handle" title="拖拽排序">
            <span style="margin-right: 8px;">☰</span>
            <span>{{ record.sortOrder }}</span>
          </div>
        </template>
        <template v-if="column.dataIndex === 'status'">
          <a-switch v-model:checked="record.status" :checked-value="1" :un-checked-value="0" />
        </template>
        <template v-if="column.dataIndex === 'description'">
          <a-input v-model:value="record.description" placeholder="描述" />
        </template>
        <template v-if="column.dataIndex === 'action'">
          <TableAction :actions="getTableAction(record)" />
        </template>
      </template>
    </BasicTable>
    <a-button type="dashed" :icon="h(PlusOutlined)" @click="handleAdd" class="w100%" ref="btnAdd">新增</a-button>
  </BasicDrawer>
</template>

<script lang="ts" setup>
  import { ref, unref, nextTick, onBeforeUnmount, h, useTemplateRef } from 'vue';
  import { BasicDrawer, useDrawerInner } from '/src/components/Drawer';
  import { BasicTable, useTable, TableAction } from '/src/components/Table';
  import { useDesign } from '/@/hooks/web/useDesign';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { PlusOutlined } from '@ant-design/icons-vue';
  import Sortable from 'sortablejs';
  import { dictItemColumns, dictItemSearchFormSchema } from '../dict.data';
  import { itemList, deleteItem, saveOrUpdateDictItem } from '../dict.api';
  import { Colors } from '/@/utils/dict/DictColors.js';

  const { prefixCls } = useDesign('row-invalid');
  const { createMessage } = useMessage();
  const dictId = ref('');
  const saving = ref(false);
  const dataSource = ref<any[]>([]);
  const originalData = ref<Map<string, any>>(new Map()); // 保存原始数据快照
  const deletedItems = ref<any[]>([]); // 存储已删除的已有数据
  const tableRef = ref();
  let sortableInstance: Sortable | null = null;

  // 错误状态管理
  const fieldErrors = new Map<any, Set<string>>();

  function getFieldError(record: any, field: string): 'error' | undefined {
    const errors = fieldErrors.get(record);
    return errors?.has(field) ? 'error' : undefined;
  }

  function setFieldError(record: any, field: string) {
    if (!fieldErrors.has(record)) {
      fieldErrors.set(record, new Set());
    }
    fieldErrors.get(record)!.add(field);
  }

  function clearFieldError(record: any, field: string) {
    const errors = fieldErrors.get(record);
    if (errors) {
      errors.delete(field);
    }
  }

  function clearAllErrors() {
    fieldErrors.forEach(errors => errors.clear());
  }

  const colorOptions = Colors.map((color) => ({
    value: color[0],
  }));

  // 表格列配置
  const tableColumns = [
    ...dictItemColumns,
    {
      title: '描述',
      dataIndex: 'description',
      width: 150,
    },
    {
      title: '操作',
      dataIndex: 'action',
      width: 60,
    },
  ];

  const [registerDrawer, { closeDrawer }] = useDrawerInner(async (data) => {
    dictId.value = data.id;
    setProps({ searchInfo: { dictId: unref(dictId) } });
    await reload();
    nextTick(() => {
      initDragSort();
    });
  });

  const [registerTable, { reload, setProps, getTableRef }] = useTable({
    rowKey: 'id',
    api: itemList,
    columns: dictItemColumns,
    formConfig: {
      baseColProps: { span: 8 },
      schemas: dictItemSearchFormSchema,
      autoSubmitOnEnter: true,
    },
    striped: true,
    useSearchForm: true,
    bordered: true,
    showIndexColumn: false,
    canResize: false,
    immediate: false,
    actionColumn: {
      width: 60,
      title: '操作',
      dataIndex: 'action',
      fixed: undefined,
    },
    afterFetch: (data) => {
      dataSource.value = data;
      // 保存原始数据快照，用于后续变更检测
      originalData.value.clear();
      data.forEach((item) => {
        if (item.id) {
          originalData.value.set(item.id, { ...item });
        }
      });
      return data;
    },
  });

  /**
   * 初始化拖拽排序
   */
  function initDragSort() {
    nextTick(() => {
      const tableInstanceRef = getTableRef();
      const tableEl = tableInstanceRef?.value?.$el ?? tableRef.value?.$el;
      if (!tableEl) return;
      const tbody = tableEl.querySelector('tbody.ant-table-tbody');
      if (!tbody) return;

      // 销毁旧实例
      if (sortableInstance) {
        sortableInstance.destroy();
        sortableInstance = null;
      }

      // 创建新实例
      sortableInstance = Sortable.create(tbody, {
        animation: 150,
        handle: '.drag-handle',
        onEnd: (evt) => {
          const { oldIndex, newIndex } = evt;
          if (oldIndex === undefined || newIndex === undefined || oldIndex === newIndex) return;
          // 重新排序
          const list = [...dataSource.value];
          const moved = list.splice(oldIndex, 1)[0];
          list.splice(newIndex, 0, moved);
          // 重置 sortOrder
          list.forEach((item, idx) => {
            item.sortOrder = idx + 1;
          });
          // 使用 splice 更新数组，确保触发响应式更新
          dataSource.value.splice(0, dataSource.value.length, ...list);
        },
      });
    });
  }

  // 组件卸载时清理
  onBeforeUnmount(() => {
    if (sortableInstance) {
      sortableInstance.destroy();
      sortableInstance = null;
    }
  });

  const btnRef = useTemplateRef('btnAdd');
  /**
   * 新增行
   */
  function handleAdd() {
    const newItem = {
      id: '', // 新增行不赋值 id
      itemText: '',
      itemValue: '',
      itemColor: null,
      sortOrder: dataSource.value.length + 1,
      status: 1,
      description: '',
      dictId: dictId.value,
    };
    dataSource.value.push(newItem);
    nextTick(() => {
      initDragSort();
      // 滚动到底部，保持新增按钮可见
      const addBtn = btnRef.value.$el; //document.querySelector('.add-row-btn') as HTMLElement;
      if (addBtn) {
        addBtn.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
      }
    });
  }

  /**
   * 删除行（前端标记）
   */
  function handleDelete(record) {
    const idx = dataSource.value.indexOf(record);
    if (idx === -1) return;

    if (record.id) {
      // 已有数据：记录到待删除列表，从表格中移除
      deletedItems.value.push(record);
    }
    // 从表格中移除
    dataSource.value.splice(idx, 1);
    // 重置 sortOrder
    dataSource.value
      .filter((item) => !item._deleted)
      .forEach((item, i) => {
        item.sortOrder = i + 1;
      });
    nextTick(() => initDragSort());
  }

  /**
   * 检测数据是否发生变更
   */
  function isItemChanged(original: any, current: any): boolean {
    const fields = ['itemText', 'itemValue', 'itemColor', 'sortOrder', 'status', 'description'];
    for (const field of fields) {
      if (original[field] !== current[field]) {
        return true;
      }
    }
    return false;
  }

  /**
   * 保存所有数据（抽屉底部保存按钮触发）
   */
  async function handleSave() {
    clearAllErrors();

    const itemsToSave: any[] = [];
    let hasError = false;

    for (const item of dataSource.value) {
      const isNewRow = !item.id; // id 为空表示新增行
      const itemTextEmpty = !item.itemText || item.itemText.trim() === '';
      const itemValueEmpty = !item.itemValue || item.itemValue.trim() === '';

      // 新增行：名称和数据值都未填写，跳过
      if (isNewRow && itemTextEmpty && itemValueEmpty) {
        continue;
      }

      // 部分填写的行，验证必填项
      let rowHasError = false;
      if (itemTextEmpty) {
        setFieldError(item, 'itemText');
        rowHasError = true;
      }
      if (itemValueEmpty) {
        setFieldError(item, 'itemValue');
        rowHasError = true;
      }

      if (rowHasError) {
        hasError = true;
      } else {
        itemsToSave.push(item);
      }
    }

    if (hasError) {
      createMessage.warning('请填写完整的必填项');
      return;
    }

    saving.value = true;
    try {
      // 先执行删除操作
      for (const item of deletedItems.value) {
        await deleteItem({ id: item.id }, () => {});
      }

      // 再执行新增和修改操作
      for (const item of itemsToSave) {
        const isUpdate = !!item.id; // id 有值表示修改，无值表示新增
        const params: any = {
          itemText: item.itemText,
          itemValue: item.itemValue,
          itemColor: item.itemColor,
          sortOrder: item.sortOrder,
          status: item.status,
          description: item.description,
          dictId: dictId.value,
        };
        if (isUpdate) {
          params.id = item.id;
          // 检测是否有变更
          const original = originalData.value.get(item.id);
          if (original && !isItemChanged(original, item)) {
            continue; // 无变更，跳过更新
          }
        }
        await saveOrUpdateDictItem(params, isUpdate);
      }
      createMessage.success('保存成功');
      closeDrawer();
      await reload();
      nextTick(() => initDragSort());
    } catch (error: any) {
      console.error('保存失败:', error);
      const errorMsg = error?.message || error?.data?.message || '保存失败';
      createMessage.error(errorMsg);
    } finally {
      saving.value = false;
    }
  }

  /**
   * 操作栏
   */
  function getTableAction(record) {
    return [
      {
        label: '删除',
        onClick: handleDelete.bind(null, record),
      },
    ];
  }

  function getRowClassName(record) {
    return record.status == 0 ? prefixCls : '';
  }
</script>

<style scoped lang="less">
  @prefix-cls: ~'@{namespace}-row-invalid';

  :deep(.@{prefix-cls}) {
    background: #f4f4f4;
    color: #bababa;
  }

  .drag-handle {
    cursor: move;
    user-select: none;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #999;
  }

  .color-option-item {
    display: flex;
    align-items: center;
    gap: 8px;
    white-space: nowrap;
  }

  .color-swatch {
    width: 20px;
    height: 20px;
    border-radius: 50%;
    flex-shrink: 0;
    border: 1px solid #d9d9d9;
  }
</style>

<style lang="less">
/* 颜色选择器下拉面板样式 */
  .color-select-dropdown {
    .rc-virtual-list-holder-inner {
      display: grid !important;
      grid-template-columns: 1fr 1fr;
      gap: 2px 8px;
    }

    .ant-select-item {
      padding: 6px 8px;
    }
  }
</style>