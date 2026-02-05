# JeecgBoot Vue3 项目开发规范

## 一、技术栈

- **框架**: Vue 3.5.22 + TypeScript 5.9.3
- **构建工具**: Vite 6.3.6
- **UI组件库**: Ant Design Vue 4.2.6
- **状态管理**: Pinia 2.1.7
- **HTTP客户端**: Axios (defHttp)
- **CSS预处理器**: Less

## 二、命名规范

| 类型 | 规范 | 示例 |
|------|------|------|
| 接口/类型 | PascalCase, 无I前缀 | `UserInfo`, `FormSchema` |
| 枚举 | PascalCase, 成员UPPER_SNAKE_CASE | `MenuTypeEnum.SIDEBAR` |
| 常量 | UPPER_SNAKE_CASE | `MAX_PAGE_SIZE` |
| 变量/函数 | camelCase | `fetchData()`, `isLoading` |
| 组件文件 | PascalCase | `UserList.vue` |
| 组件name | kebab-case | `defineOptions({ name: 'system-user' })` |
| 布尔变量 | is/has/should前缀 | `isLoading`, `hasPermission` |

## 三、路径别名

```typescript
// ✅ 使用项目定义的别名
import { useMessage } from '/@/hooks/web/useMessage';
import { defHttp } from '/@/utils/http/axios';
import type { UserInfo } from '#/api';

// 别名映射: /@/* -> src/* , /#/* -> types/*
```

## 四、Vue组件规范

```vue
<script lang="ts" setup>
  // 1. 导入语句
  import { ref, computed } from 'vue';
  import { BasicTable } from '/@/components/Table';
  
  // 2. 组件选项
  defineOptions({ name: 'system-user' });
  
  // 3. Props/Emits
  interface Props { userId: string; }
  const props = defineProps<Props>();
  
  // 4. 响应式数据
  const loading = ref(false);
  
  // 5. 方法
  function fetchData() { }
</script>
```

## 五、样式规范

```less
// ✅ 嵌套不超过3层
.user-list {
  padding: 16px;
  .header {
    display: flex;
    .title { font-size: 16px; }
  }
}

// ✅ scoped + 深度选择器
<style lang="less" scoped>
  :deep(.ant-table) { border-radius: 4px; }
</style>
```

## 六、API调用规范

```typescript
// ✅ 使用defHttp
import { defHttp } from '/@/utils/http/axios';

enum Api {
  GetUserList = '/sys/user/list',
}

export function getUserList(params: UserParams) {
  return defHttp.get<UserListResult>({
    url: Api.GetUserList,
    params,
  });
}
```

## 七、组件开发规范

```typescript
// ✅ 表单使用BasicForm
import { BasicForm, useForm } from '/@/components/Form';
const [registerForm, { validate }] = useForm({
  schemas: formSchemas,
});

// ✅ 表格使用BasicTable
import { BasicTable, useTable } from '/@/components/Table';
const [registerTable, { reload }] = useTable({
  api: fetchUserList,
  columns: tableColumns,
});

// ✅ 权限控制
<a-button v-auth="'system:user:add'">新增</a-button>
import { usePermission } from '/@/hooks/web/usePermission';
const { hasPermission } = usePermission();
```

## 八、文件组织

```
src/
├── api/           # API接口 (按模块划分)
├── components/    # 全局组件
│   ├── Basic/     # 基础组件
│   ├── Form/      # 表单组件
│   ├── Table/     # 表格组件
│   └── jeecg/     # Jeecg业务组件
├── hooks/         # 组合式函数
├── utils/         # 工具函数
└── views/         # 页面视图
    └── system/    # 系统管理
        └── user/  # 用户管理
            ├── index.vue
            ├── UserDrawer.vue
            └── user.data.ts
```

## 九、提交规范

```
<type>(<scope>): <subject>

type类型: feat|fix|perf|style|docs|test|refactor|build|ci|chore|revert

示例:
feat(system-user): 添加用户批量导入功能
fix(api-user): 修复登录超时处理问题
```

## 十、质量检查

```bash
# 代码检查
pnpm exec eslint src --ext .vue,.ts,.tsx
pnpm exec vue-tsc --noEmit

# 开发命令
pnpm dev      # 启动开发
pnpm build    # 生产构建
```

## 十一、关键约束

- ❌ 禁止使用 `any` 类型
- ❌ 禁止硬编码敏感信息
- ❌ 禁止在生产代码中保留 `console.log`
- ✅ API调用统一使用 `defHttp`
- ✅ 权限检查使用 `v-auth` 或 `hasPermission()`
- ✅ 组件样式使用 `scoped`
