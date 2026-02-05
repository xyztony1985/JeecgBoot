---
name: "jeecg-form-creator"
description: "生成 JeecgBoot Vue3 表单代码，使用 BasicForm、useForm 和 FormSchema。当用户需要创建表单、添加表单页面或生成表单字段时调用。"
---

# JeecgBoot 表单生成器

此 skill 用于生成符合 JeecgBoot Vue3 项目规范的表单代码。

## 使用场景

- 创建新表单页面
- 添加表单抽屉或弹窗
- 生成 FormSchema 字段定义
- 创建搜索表单或数据录入表单

## 输出结构

生成的代码包含：
1. BasicForm 组件配合 `useForm` 钩子的使用
2. FormSchema 数组字段定义
3. 正确的导入语句（从 `/@/components/Form`）
4. Jeecg 自定义组件（JAreaLinkage、JSelectUser 等）
5. 表单验证规则

## FormSchema 字段类型

| 组件 | 用途 | Props |
|-----------|-------|-------|
| Input | 文本输入 | `component: 'Input'` |
| InputTextArea | 多行文本 | `component: 'InputTextArea'` |
| InputNumber | 数字输入 | `component: 'InputNumber'` |
| Select | 下拉选择 | `component: 'Select'`, `componentProps: { options: [] }` |
| RadioGroup | 单选按钮 | `component: 'RadioGroup'` |
| CheckboxGroup | 复选框组 | `component: 'CheckboxGroup'` |
| DatePicker | 日期选择 | `component: 'DatePicker'` |
| RangePicker | 日期范围 | `component: 'RangePicker'` |
| Switch | 开关 | `component: 'Switch'` |
| JAreaLinkage | 省市区联动 | `component: 'JAreaLinkage'` |
| JSelectUser | 用户选择器 | `component: 'JSelectUser'` |
| JSelectDept | 部门选择器 | `component: 'JSelectDept'` |
| JUpload | 文件上传 | `component: 'JUpload'` |
| JEditor | 富文本编辑器 | `component: 'JEditor'` |
| JCodeEditor | 代码编辑器 | `component: 'JCodeEditor'` |
| JDictSelectTag | 字典选择 | `component: 'JDictSelectTag'`, `componentProps: { dictCode: '' }` |
| JTreeSelect | 树形选择 | `component: 'JTreeSelect'` |

## 代码模板

```vue
<template>
  <BasicForm @register="registerForm" />
</template>

<script lang="ts" setup>
  import { BasicForm, useForm } from '/@/components/Form';
  import type { FormSchema } from '/@/components/Form';
  
  defineOptions({ name: '{{component-name}}' });
  
  const formSchemas: FormSchema[] = [
    // 字段将在此处生成
  ];
  
  const [registerForm, { validate, setFieldsValue, getFieldsValue, resetFields }] = useForm({
    labelWidth: 120,
    schemas: formSchemas,
    showActionButtonGroup: true,
    actionColOptions: { span: 24 },
  });
</script>
```

## 字段定义示例

### 基础输入框
```typescript
{
  field: 'username',
  label: '用户名',
  component: 'Input',
  required: true,
  rules: [{ required: true, message: '请输入用户名' }],
}
```

### 下拉选择
```typescript
{
  field: 'status',
  label: '状态',
  component: 'Select',
  componentProps: {
    options: [
      { label: '启用', value: 1 },
      { label: '禁用', value: 0 },
    ],
  },
}
```

### 字典字段
```typescript
{
  field: 'sex',
  label: '性别',
  component: 'JDictSelectTag',
  componentProps: {
    dictCode: 'sex',
  },
}
```

### 用户选择器
```typescript
{
  field: 'userId',
  label: '选择用户',
  component: 'JSelectUser',
  componentProps: {
    multi: false,
  },
}
```

### 日期范围
```typescript
{
  field: 'dateRange',
  label: '日期范围',
  component: 'RangePicker',
  componentProps: {
    showTime: true,
    format: 'YYYY-MM-DD HH:mm:ss',
  },
}
```

## 使用说明

1. 根据用户需求识别表单字段
2. 将字段类型映射到合适的组件
3. 生成带验证的 FormSchema 数组
4. 包含 Jeecg 自定义组件的必要导入
5. 提供符合 Vue3 + TS 标准的完整组件代码
6. 使用 `/@/` 路径别名导入
7. 遵循 project_rules.md 中的命名规范
