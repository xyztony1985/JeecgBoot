# 表单组件使用规范

> 本文档涵盖 BasicForm 表单容器及所有表单控件组件的使用规范。

---

## 一、BasicForm 表单容器

**导入方式**：

```typescript
import { BasicForm, useForm } from '/@/components/Form';
```

### 1.1 关键 Props

| Prop | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `schemas` | `FormSchema[]` | `[]` | 表单配置项数组 |
| `model` | `Object` | `{}` | 表单数据对象 |
| `labelWidth` | `number \| string` | `0` | 标签宽度 |
| `layout` | `'horizontal' \| 'vertical' \| 'inline'` | `'vertical'` | 表单布局 |
| `autoAdvancedCol` | `number` | `3` | 超过该列数自动折叠 |
| `alwaysShowLines` | `number` | `1` | 不受折叠影响的行数 |
| `showAdvancedButton` | `boolean` | - | 是否显示收起/展开按钮 |
| `showActionButtonGroup` | `boolean` | `true` | 是否显示操作按钮 |
| `showResetButton` | `boolean` | `true` | 是否显示重置按钮 |
| `showSubmitButton` | `boolean` | `true` | 是否显示提交按钮 |
| `autoSearch` | `boolean` | `false` | 改变后自动查询（搜索表单场景） |
| `disabled` | `boolean` | - | 禁用表单 |
| `labelCol` | `Object` | - | 标签栅格布局 |
| `wrapperCol` | `Object` | - | 控件栅格布局 |
| `rowProps` | `Object` | `{ gutter: 8 }` | 行栅格配置 |

### 1.2 useForm 返回值

```typescript
const [registerForm, {
  validate,           // 表单校验
  resetFields,        // 重置表单
  setFieldsValue,     // 设置表单值
  updateSchema,       // 动态更新 Schema
  setProps,           // 设置表单 Props
  clearValidate,      // 清除校验状态
  getFieldsValue,     // 获取表单值
  submit,             // 提交表单
}] = useForm({
  schemas: formSchemas,
  labelWidth: 90,
  showActionButtonGroup: false,
});
```

### 1.3 FormSchema 配置

```typescript
export const formSchemas: FormSchema[] = [
  {
    field: 'name',
    label: '名称',
    component: 'Input',
    required: true,
    componentProps: {
      placeholder: '请输入名称',
      maxlength: 50,
    },
  },
  {
    field: 'status',
    label: '状态',
    component: 'JDictSelectTag',
    componentProps: {
      dictCode: 'status',
      placeholder: '请选择状态',
    },
  },
  {
    field: 'createTime',
    label: '创建时间',
    component: 'RangePicker',
    componentProps: {
      valueFormat: 'YYYY-MM-DD',
    },
  },
];
```

### 1.4 支持的组件列表

| component 值 | 说明 |
|-------------|------|
| `Input` | 输入框 |
| `InputNumber` | 数字输入框 |
| `Select` | 下拉选择 |
| `RadioGroup` | 单选组 |
| `CheckboxGroup` | 复选组 |
| `Switch` | 开关 |
| `DatePicker` | 日期选择 |
| `RangePicker` | 日期范围选择 |
| `TimePicker` | 时间选择 |
| `Textarea` | 文本域 |
| `JDictSelectTag` | 字典选择 |
| `JSearchSelect` | 异步搜索选择 |
| `JTreeSelect` | 树选择 |
| `JTreeDict` | 树字典 |
| `JSelectUser` | 用户选择 |
| `JSelectDepart` | 部门选择 |
| `JSelectUserByDepartment` | 按部门选择用户 |
| `JEditor` | 富文本编辑器 |
| `JMarkdownEditor` | Markdown 编辑器 |
| `JSwitch` | 开关（增强版） |
| `JCheckbox` | 复选框（字典版） |
| `JPopup` | Popup 弹窗选择 |
| `JCategorySelect` | 分类选择 |
| `JAreaLinkage` | 地区联动 |
| `JCascader` | 级联选择 |
| `JCodeEditor` | 代码编辑器 |

---

## 二、字典与选择类组件

### 2.1 JDictSelectTag 字典选择

**导入方式**：`import { JDictSelectTag } from '/@/components/Form';`

| Prop | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `value` | `string \| number \| array` | - | 绑定值 |
| `dictCode` | `string` | - | 字典编码 |
| `type` | `string` | `'list'` | 组件类型：`list`(下拉)、`radio`(单选)、`radioButton`(单选按钮) |
| `placeholder` | `string` | - | 占位文字 |
| `stringToNumber` | `boolean` | `false` | 值转为数字 |
| `useDicColor` | `boolean` | `false` | 使用字典颜色 |
| `showChooseOption` | `boolean` | `true` | 显示"请选择"选项 |
| `options` | `Array` | `[]` | 备用选项（无 dictCode 时使用） |
| `onlySearchByLabel` | `boolean` | `false` | 搜索时只搜索 label |

```vue
<template>
  <div>
    <!-- 下拉选择 -->
    <JDictSelectTag v-model:value="formData.status" dictCode="status" placeholder="请选择状态" />

    <!-- 单选按钮 -->
    <JDictSelectTag v-model:value="formData.gender" dictCode="gender" type="radio" />

    <!-- 单选按钮样式 -->
    <JDictSelectTag v-model:value="formData.type" dictCode="type" type="radioButton" />

    <!-- 使用字典颜色 -->
    <JDictSelectTag v-model:value="formData.level" dictCode="level" :useDicColor="true" />
  </div>
</template>

<script lang="ts" setup>
  import { reactive } from 'vue';
  import { JDictSelectTag } from '/@/components/Form';

  const formData = reactive({
    status: '',
    gender: '',
    type: '',
    level: '',
  });
</script>
```

```typescript
// FormSchema 中使用
{
  field: 'status',
  label: '状态',
  component: 'JDictSelectTag',
  componentProps: { dictCode: 'status', placeholder: '请选择状态' },
}
```

### 1.5 注意事项

1. `dictCode` 必须对应后台 `sys_dict` 表中的字典编码
2. `type` 取值：`list`（下拉）、`radio`（单选）、`radioButton`（单选按钮样式）
3. 绑定值类型取决于字典值的类型，可通过 `stringToNumber` 转为数字

---

### 2.2 JSearchSelect 异步搜索选择

**导入方式**：`import { JSearchSelect } from '/@/components/Form';`

| Prop | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `value` | `string \| number` | - | 绑定值 |
| `dict` | `string` | - | 字典编码 |
| `dictOptions` | `Array` | `[]` | 静态选项 |
| `async` | `boolean` | `false` | 是否异步搜索 |
| `placeholder` | `string` | - | 占位文字 |
| `pageSize` | `number` | `10` | 分页大小 |
| `multiple` | `boolean` | `false` | 是否多选 |
| `immediateChange` | `boolean` | `false` | 有值后立即触发 change |
| `params` | `Object` | `{}` | 额外查询参数 |
| `useDicColor` | `boolean` | `false` | 使用字典颜色 |

```vue
<template>
  <div>
    <!-- 同步搜索（本地过滤） -->
    <JSearchSelect v-model:value="formData.userId" dict="sys_user,realname,id" placeholder="请选择用户" />

    <!-- 异步搜索（远程搜索 + 滚动加载） -->
    <JSearchSelect v-model:value="formData.productId" dict="product,name,id" :async="true" :pageSize="20" placeholder="请搜索产品" />

    <!-- 多选模式 -->
    <JSearchSelect v-model:value="formData.userIds" dict="sys_user,realname,id" :multiple="true" placeholder="请选择用户" />
  </div>
</template>

<script lang="ts" setup>
  import { reactive } from 'vue';
  import { JSearchSelect } from '/@/components/Form';

  const formData = reactive({
    userId: '',
    productId: '',
    userIds: [],
  });
</script>
```

```typescript
// FormSchema 中使用
{
  field: 'userId',
  label: '用户',
  component: 'JSearchSelect',
  componentProps: { dict: 'sys_user,realname,id', placeholder: '请选择用户' },
}
```

### 2.2.1 注意事项

1. `dict` 格式：`表名,文本字段,值字段`
2. `async=true` 时启用远程搜索，适合大数据量场景
3. 多选时绑定值为数组，单选时为字符串

---

### 2.3 JTreeSelect 树选择

**导入方式**：`import { JTreeSelect } from '/@/components/Form';`

| Prop | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `value` | `string` | `''` | 绑定值 |
| `dict` | `string` | `'id'` | 字典配置，格式：`表名,文本字段,值字段` |
| `parentCode` | `string` | `''` | 根节点父 ID |
| `pidField` | `string` | `'pid'` | 父 ID 字段名 |
| `pidValue` | `string` | `''` | 父 ID 值 |
| `hasChildField` | `string` | `''` | 是否有子节点字段 |
| `condition` | `string` | `''` | 查询条件 |
| `multiple` | `boolean` | `false` | 是否多选 |
| `url` | `string` | `''` | 自定义接口地址 |
| `params` | `Object` | `{}` | 额外参数 |
| `treeCheckAble` | `boolean` | `false` | 显示复选框 |
| `hiddenNodeKey` | `string` | `''` | 隐藏的节点 Key |

```vue
<template>
  <div>
    <!-- 基础用法 -->
    <JTreeSelect v-model:value="formData.parentId" dict="sys_org,name,id" pidField="parent_id" placeholder="请选择上级部门" />

    <!-- 多选 + 复选框 -->
    <JTreeSelect v-model:value="formData.orgIds" dict="sys_org,name,id" :multiple="true" :treeCheckAble="true" placeholder="请选择部门" />

    <!-- 隐藏指定节点（如编辑时隐藏自己及子孙） -->
    <JTreeSelect v-model:value="formData.parentId" dict="sys_org,name,id" :hiddenNodeKey="currentId" />
  </div>
</template>

<script lang="ts" setup>
  import { reactive, ref } from 'vue';
  import { JTreeSelect } from '/@/components/Form';

  const currentId = ref('123');
  const formData = reactive({
    parentId: '',
    orgIds: [],
  });
</script>
```

```typescript
// FormSchema 中使用
{
  field: 'parentId',
  label: '上级部门',
  component: 'JTreeSelect',
  componentProps: { dict: 'sys_org,name,id', pidField: 'parent_id', placeholder: '请选择上级部门' },
}
```

### 2.3.1 注意事项

1. `dict` 格式：`表名,文本字段,值字段`
2. 编辑时可用 `hiddenNodeKey` 隐藏当前节点及其子孙节点，防止选择自己作为上级
3. `treeCheckAble=true` 时显示复选框，适合多选场景

---

### 2.4 JTreeDict 树字典

**导入方式**：`import { JTreeDict } from '/@/components/Form';`

| Prop | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `value` | `string` | `''` | 绑定值 |
| `field` | `string` | `'id'` | 值字段 |
| `parentCode` | `string` | `''` | 父级编码 |
| `async` | `boolean` | `false` | 是否异步加载 |

```vue
<template>
  <JTreeDict v-model:value="formData.categoryId" parentCode="A01" placeholder="请选择分类" />
</template>

<script lang="ts" setup>
  import { reactive } from 'vue';
  import { JTreeDict } from '/@/components/Form';

  const formData = reactive({
    categoryId: '',
  });
</script>
```

```typescript
// FormSchema 中使用
{
  field: 'categoryId',
  label: '分类',
  component: 'JTreeDict',
  componentProps: { parentCode: 'A01', placeholder: '请选择分类' },
}
```

### 2.4.1 注意事项

1. `parentCode` 指定父级编码，用于过滤数据范围
2. `async=true` 时启用异步加载，适合大数据量场景
3. 绑定值为字符串类型

---

### 2.5 JCategorySelect 分类选择

**导入方式**：`import { JCategorySelect } from '/@/components/Form';`

| Prop | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `value` | `string \| array` | - | 绑定值 |
| `placeholder` | `string` | `'请选择'` | 占位文字 |
| `disabled` | `boolean` | `false` | 是否禁用 |
| `condition` | `string` | `''` | 查询条件 |
| `multiple` | `boolean \| string` | `false` | 是否多选 |
| `pid` | `string` | `''` | 父 ID |
| `pcode` | `string` | `''` | 父编码 |
| `back` | `string` | `''` | 返回字段 |

```vue
<template>
  <JCategorySelect v-model:value="formData.categoryId" pcode="A01" placeholder="请选择分类" />
</template>

<script lang="ts" setup>
  import { reactive } from 'vue';
  import { JCategorySelect } from '/@/components/Form';

  const formData = reactive({
    categoryId: '',
  });
</script>
```

```typescript
// FormSchema 中使用
{
  field: 'categoryId',
  label: '分类',
  component: 'JCategorySelect',
  componentProps: { pcode: 'A01', placeholder: '请选择分类' },
}
```

### 2.5.1 注意事项

1. `pcode` 或 `pid` 指定父级，用于过滤数据范围
2. `multiple=true` 时绑定值为数组
3. 与 `JTreeDict` 类似，但数据源不同

---

### 2.6 JCascader 级联选择

**导入方式**：`import { JCascader } from '/@/components/Form';`

```vue
<template>
  <JCascader v-model:value="formData.areaCode" placeholder="请选择地区" />
</template>

<script lang="ts" setup>
  import { reactive } from 'vue';
  import { JCascader } from '/@/components/Form';

  const formData = reactive({
    areaCode: '',
  });
</script>
```

```typescript
// FormSchema 中使用
{
  field: 'areaCode',
  label: '地区',
  component: 'JCascader',
  componentProps: { placeholder: '请选择地区' },
}
```

### 2.6.1 注意事项

1. 绑定值为字符串类型
2. 通常与 `JAreaLinkage` 配合使用

---

## 三、人员与组织类组件

### 3.1 JSelectUser 用户选择

**导入方式**：`import { JSelectUser } from '/@/components/Form';`

| Prop | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `value` | `string \| array` | - | 绑定值（逗号分隔的用户名） |
| `labelKey` | `string` | `'realname'` | 显示字段 |
| `rowKey` | `string` | `'username'` | 值字段 |
| `params` | `Object` | `{}` | 查询参数 |
| `excludeUserIdList` | `Array` | `[]` | 排除的用户 ID 列表 |

```vue
<template>
  <div>
    <!-- 单选用户 -->
    <JSelectUser v-model:value="formData.userId" placeholder="请选择用户" />

    <!-- 排除指定用户 -->
    <JSelectUser v-model:value="formData.managerId" :excludeUserIdList="[currentUserId]" placeholder="请选择负责人" />
  </div>
</template>

<script lang="ts" setup>
  import { reactive, ref } from 'vue';
  import { JSelectUser } from '/@/components/Form';

  const currentUserId = ref('admin');
  const formData = reactive({
    userId: '',
    managerId: '',
  });
</script>
```

```typescript
// FormSchema 中使用
{
  field: 'userId',
  label: '用户',
  component: 'JSelectUser',
  componentProps: { placeholder: '请选择用户' },
}
```

### 3.1.1 注意事项

1. 绑定值为逗号分隔的用户名（由 `rowKey` 决定字段）
2. `excludeUserIdList` 用于排除特定用户（如当前用户）
3. 显示字段由 `labelKey` 控制，默认 `realname`

---

### 3.2 JSelectUserByDepartment 按部门选择用户

**导入方式**：`import { JSelectUserByDepartment } from '/@/components/Form';`

```vue
<template>
  <JSelectUserByDepartment v-model:value="formData.userIds" placeholder="请选择用户" />
</template>

<script lang="ts" setup>
  import { reactive } from 'vue';
  import { JSelectUserByDepartment } from '/@/components/Form';

  const formData = reactive({
    userIds: [],
  });
</script>
```

```typescript
// FormSchema 中使用
{
  field: 'userIds',
  label: '用户',
  component: 'JSelectUserByDepartment',
  componentProps: { placeholder: '请选择用户' },
}
```

### 3.2.1 注意事项

1. 绑定值为数组类型
2. 先选择部门，再选择用户，适合需要按部门筛选的场景

---

## 四、编辑器类组件

### 4.1 JEditor 富文本编辑器

**导入方式**：`import { JEditor } from '/@/components/Form';`

| Prop | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `value` | `string` | `''` | 绑定值（HTML） |
| `disabled` | `boolean` | `false` | 是否禁用 |
| `autoFocus` | `boolean` | `true` | 是否自动聚焦 |

```vue
<template>
  <JEditor v-model:value="formData.content" :disabled="false" />
</template>

<script lang="ts" setup>
  import { reactive } from 'vue';
  import { JEditor } from '/@/components/Form';

  const formData = reactive({
    content: '',
  });
</script>
```

```typescript
// FormSchema 中使用
{
  field: 'content',
  label: '内容',
  component: 'JEditor',
  componentProps: { disabled: false },
}
```

### 4.1.1 注意事项

1. 绑定值为 HTML 字符串
2. `disabled=true` 时为只读模式
3. 富文本编辑器较重，表单中建议单独一行（`baseColProps: { span: 24 }`）

---

### 4.2 JMarkdownEditor Markdown 编辑器

**导入方式**：`import { JMarkdownEditor } from '/@/components/Form';`

| Prop | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `value` | `string` | `''` | 绑定值（Markdown） |
| `disabled` | `boolean` | `false` | 是否禁用 |

```vue
<template>
  <JMarkdownEditor v-model:value="formData.markdown" />
</template>

<script lang="ts" setup>
  import { reactive } from 'vue';
  import { JMarkdownEditor } from '/@/components/Form';

  const formData = reactive({
    markdown: '',
  });
</script>
```

```typescript
// FormSchema 中使用
{
  field: 'markdown',
  label: '内容',
  component: 'JMarkdownEditor',
  componentProps: { disabled: false },
}
```

### 4.2.1 注意事项

1. 绑定值为 Markdown 字符串
2. `disabled=true` 时为只读模式
3. 相比 JEditor 更轻量，适合技术文档场景

---

### 4.3 JCodeEditor 代码编辑器

**导入方式**：`import { JCodeEditor } from '/@/components/Form';`

```vue
<template>
  <JCodeEditor v-model:value="formData.code" />
</template>

<script lang="ts" setup>
  import { reactive } from 'vue';
  import { JCodeEditor } from '/@/components/Form';

  const formData = reactive({
    code: '',
  });
</script>
```

```typescript
// FormSchema 中使用
{
  field: 'code',
  label: '代码',
  component: 'JCodeEditor',
  componentProps: { disabled: false },
}
```

### 4.3.1 注意事项

1. 绑定值为代码字符串
2. 支持语法高亮，适合 JSON、SQL、代码片段等场景
3. 编辑器较重，建议单独一行（`baseColProps: { span: 24 }`）

---

## 五、开关与复选类组件

### 5.1 JSwitch 开关

**导入方式**：`import { JSwitch } from '/@/components/Form';`

| Prop | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `value` | `string \| number` | - | 绑定值 |
| `options` | `Array` | `['Y', 'N']` | 取值选项 |
| `labelOptions` | `Array` | `['是', '否']` | 文本选项 |
| `query` | `boolean` | `false` | `true` 为下拉模式，`false` 为开关模式 |
| `disabled` | `boolean` | `false` | 是否禁用 |

```vue
<!-- 开关模式（默认） -->
<JSwitch v-model:value="formData.status" />

<!-- 自定义选项 -->
<JSwitch v-model:value="formData.flag" :options="['1', '0']" :labelOptions="['启用', '禁用']" />

<!-- 下拉模式 -->
<JSwitch v-model:value="formData.type" :query="true" :options="['A', 'B']" :labelOptions="['类型A', '类型B']" />
```

---

### 5.2 JCheckbox 复选框（字典版）

**导入方式**：`import { JCheckbox } from '/@/components/Form';`

| Prop | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `value` | `string \| number` | - | 绑定值（逗号分隔） |
| `dictCode` | `string` | - | 字典编码 |
| `options` | `Array` | `[]` | 选项（无 dictCode 时使用） |
| `useDicColor` | `boolean` | `false` | 使用字典颜色 |

```vue
<template>
  <div>
    <!-- 使用字典 -->
    <JCheckbox v-model:value="formData.hobbies" dictCode="hobby" />

    <!-- 使用静态选项 -->
    <JCheckbox
      v-model:value="formData.roles"
      :options="[
        { label: '管理员', value: 'admin' },
        { label: '普通用户', value: 'user' },
      ]"
    />
  </div>
</template>

<script lang="ts" setup>
  import { reactive } from 'vue';
  import { JCheckbox } from '/@/components/Form';

  const formData = reactive({
    hobbies: '',
    roles: '',
  });
</script>
```

```typescript
// FormSchema 中使用
{
  field: 'hobbies',
  label: '爱好',
  component: 'JCheckbox',
  componentProps: { dictCode: 'hobby' },
}
```

### 5.2.1 注意事项

1. 绑定值为逗号分隔的字符串
2. `dictCode` 和 `options` 二选一，优先使用 `dictCode`
3. `options` 格式：`[{ label: '文本', value: '值' }]`

---

## 六、弹窗选择类组件

### 6.1 JPopup Popup 弹窗选择

**导入方式**：`import { JPopup } from '/@/components/Form';`

| Prop | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `code` | `string` | `''` | Online 报表编码 |
| `value` | `string` | `''` | 绑定值 |
| `fieldConfig` | `Array` | `[]` | **必填**，字段映射配置 |
| `multi` | `boolean` | `false` | 是否多选 |
| `width` | `number` | `1200` | 弹窗宽度 |
| `placeholder` | `string` | `'请选择'` | 占位文字 |
| `param` | `Object` | `{}` | 额外参数 |
| `spliter` | `string` | `','` | 分隔符 |
| `groupId` | `string` | `''` | 分组 ID |
| `showAdvancedButton` | `boolean` | `true` | 显示高级搜索按钮 |

**fieldConfig 配置**：

```typescript
fieldConfig: [
  { source: 'id', target: 'productId' },     // Popup 返回字段 → 目标表单字段
  { source: 'name', target: 'productName' },
]
```

```vue
<template>
  <JPopup
    v-model:value="formData.productId"
    code="product_list"
    :fieldConfig="[
      { source: 'id', target: 'productId' },
      { source: 'name', target: 'productName' },
    ]"
    placeholder="请选择产品"
  />
</template>

<script lang="ts" setup>
  import { reactive } from 'vue';
  import { JPopup } from '/@/components/Form';

  const formData = reactive({
    productId: '',
    productName: '',
  });
</script>
```

```typescript
// FormSchema 中使用
{
  field: 'productId',
  label: '产品',
  component: 'JPopup',
  componentProps: {
    code: 'product_list',
    fieldConfig: [
      { source: 'id', target: 'productId' },
      { source: 'name', target: 'productName' },
    ],
    placeholder: '请选择产品',
  },
}
```

### 6.1.1 注意事项

1. `code` 对应后台 Online 报表编码
2. `fieldConfig` 为必填，定义字段映射关系
3. `multi=true` 时绑定值为逗号分隔的字符串，分隔符由 `spliter` 控制

---

### 6.2 JPrompt 提示弹窗

**导入方式**：`import { useJPrompt } from '/@/components/jeecg/JPrompt';`

```typescript
const [registerJPrompt, { openJPrompt }] = useJPrompt();

function handleRename(record) {
  openJPrompt({
    title: '重命名',
    content: '请输入新名称',
    onOk: async (value) => {
      await renameApi(record.id, value);
      reload();
    },
  });
}
```

```vue
<JPrompt @register="registerJPrompt" />
```

---

## 七、地区类组件

### 7.1 JAreaLinkage 地区联动

**导入方式**：`import { JAreaLinkage } from '/@/components/Form';`

```vue
<template>
  <JAreaLinkage v-model:value="formData.areaCode" placeholder="请选择地区" />
</template>

<script lang="ts" setup>
  import { reactive } from 'vue';
  import { JAreaLinkage } from '/@/components/Form';

  const formData = reactive({
    areaCode: '',
  });
</script>
```

```typescript
// FormSchema 中使用
{
  field: 'areaCode',
  label: '地区',
  component: 'JAreaLinkage',
  componentProps: { placeholder: '请选择地区' },
}
```

### 7.1.1 注意事项

1. 绑定值为地区编码字符串
2. 省市区三级联动，数据内置，无需后台接口
3. 通常与 `JCascader` 配合使用

---

## 八、注意事项

1. **搜索表单**：查询条件使用 `RangePicker` 时，建议设置 `valueFormat`
   ```typescript
   componentProps: { valueFormat: 'YYYY-MM-DD' }
   ```

2. **字典组件**：`JDictSelectTag` 的 `dictCode` 对应后台字典编码
   ```typescript
   dictCode: 'status'  // 对应 sys_dict 表中的 dict_code
   ```

3. **动态显隐**：使用 `updateSchema` 动态控制字段
   ```typescript
   updateSchema([
     { field: 'password', ifShow: !isUpdate.value },
     { field: 'status', show: true },
   ]);
   ```

4. **表单校验**：提交前必须调用 `validate`
   ```typescript
   const values = await validate();
   ```

5. **编辑抽屉中表单配置**：建议设置 `showActionButtonGroup: false` 和 `baseColProps: { span: 12 }`
   ```typescript
   useForm({
     labelWidth: 90,
     schemas: formSchema,
     showActionButtonGroup: false,
     baseColProps: { span: 12 },
   });
   ```
