# 开发路由浏览器 - 技术设计文档

## 1. 架构设计

### 1.1 核心思路

采用**混合方案**：在运行时通过 `import.meta.glob` 扫描 views 目录，使用**单个父路由 + 多个子路由**的方式动态生成路由，减少路由对象层级，提升性能。同时提供浏览器页面展示所有可访问的页面列表。

### 1.2 方案选择理由

经过对比分析，选择方案E（混合优化方案）的原因：

| 维度 | 选择理由 |
|------|----------|
| **实现复杂度** | 低，代码集中在 `permission.ts`，无需开发 Vite 插件 |
| **开发体验** | 好，自动包含新文件，无需手动维护路由列表 |
| **运行时性能** | 可接受，使用单个父路由优化，只在开发模式执行一次 |
| **路径一致性** | 完全一致，开发环境路径与生产环境一致 |
| **维护成本** | 低，符合现有架构，无需大改 |

### 1.3 路由注册流程

```
应用启动
  ↓
创建 Router 实例（basicRoutes，包含 DEV_PAGES_ROUTE）
  ↓
权限守卫拦截
  ↓
调用 buildRoutesAction()
  ↓
从后端获取菜单数据
  ↓
[开发模式] 调用 generateDevRoutes()
  ↓
返回单个父路由 + 多个子路由
  ↓
合并路由并注册
```

## 2. 核心实现

### 2.1 开发路由生成函数

**文件：** `src/store/modules/permission.ts`

```typescript
import { dynamicPages } from '/@/utils/dynamicPages';

/**
 * 生成开发模式下的动态路由
 * 使用单个父路由包裹所有开发页面，减少路由对象层级
 */
function generateDevRoutes(): AppRouteRecordRaw[] {
  // 收集所有有效的页面路径
  const pageRoutes: AppRouteRecordRaw[] = [];
  const existingPaths = new Set<string>();

  Object.keys(dynamicPages).forEach((key) => {
    // 提取相对路径：../views/demo/cssz/MyPage.vue -> demo/cssz/MyPage
    const relativePath = key
      .replace(/^\.\.\/views\//, '')
      .replace(/\.(vue|tsx)$/, '');

    // 跳过 components 目录
    if (relativePath.includes('/components/') || relativePath.startsWith('components/')) {
      return;
    }

    const routePath = `/${relativePath}`;

    // 避免重复
    if (existingPaths.has(routePath)) {
      return;
    }
    existingPaths.add(routePath);

    // 生成路由名称：demo-cssz-MyPage
    const routeName = `dev-page-${relativePath.replace(/\//g, '-')}`;

    pageRoutes.push({
      path: routePath,
      name: routeName,
      component: dynamicPages[key] as any,
      meta: {
        title: relativePath.split('/').pop() || 'Dev Page',
        hideMenu: true,
        ignoreAuth: false, // 需要登录才能访问开发页面
      },
    });
  });

  // 如果没有页面，返回空数组
  if (pageRoutes.length === 0) {
    return [];
  }

  // 返回单个父路由，包裹所有开发页面
  return [{
    path: '',
    name: 'dev-pages-parent',
    component: LAYOUT,
    meta: { title: '页面浏览器' },
    children: pageRoutes,
  }];
}
```

### 2.2 在 buildRoutesAction 中调用

**文件：** `src/store/modules/permission.ts`

```typescript
case PermissionModeEnum.BACK:
  // ... 原有逻辑：从后端获取菜单数据 ...

  routes = [PAGE_NOT_FOUND_ROUTE, ...routeList, ...staticRoutesList];

  // 开发模式：追加开发路由（使用单个父路由优化）
  if (import.meta.env.DEV) {
    const devRoutes = generateDevRoutes();
    routes.push(...devRoutes);
  }
  break;
```

### 2.3 开发浏览器页面

**文件：** `src/views/dev/DevPages.vue`

```vue
<template>
  <div class="dev-pages-container">
    <a-card title="开发页面浏览器" :bordered="false">
      <template #extra>
        <a-tag color="blue">共 {{ filteredPages.length }} 个页面</a-tag>
      </template>

      <a-input-search
        v-model:value="searchText"
        placeholder="搜索页面路径或文件名"
        style="width: 400px; margin-bottom: 16px"
        allow-clear
      />

      <a-table
        :columns="columns"
        :data-source="filteredPages"
        :pagination="{ pageSize: 50, showSizeChanger: true }"
        :scroll="{ y: 600 }"
        size="small"
        row-key="path"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'action'">
            <a-button type="link" size="small" @click="openInNewTab(record.path)">
              新页面
            </a-button>
            <a-button type="link" size="small" @click="openDrawer(record.path)">
              抽屉
            </a-button>
          </template>
        </template>
      </a-table>
    </a-card>

    <a-drawer
      v-model:open="drawerVisible"
      :title="drawerTitle"
      placement="right"
      width="80%"
      :destroy-on-close="true"
    >
      <iframe
        v-if="drawerVisible"
        :src="drawerSrc"
        class="preview-iframe"
        frameborder="0"
      />
    </a-drawer>
  </div>
</template>

<script lang="ts" setup>
  import { ref, computed } from 'vue';
  import { dynamicPages } from '/@/utils/dynamicPages';

  defineOptions({ name: 'dev-pages' });

  const searchText = ref('');
  const drawerVisible = ref(false);
  const drawerTitle = ref('');
  const drawerSrc = ref('');

  const allPages = Object.keys(dynamicPages)
    .map((key) => {
      const path = key.replace(/^\.\.\/views\//, '').replace(/\.(vue|tsx)$/, '');
      const fileName = path.split('/').pop() || '';
      return { path: `/${path}`, fileName, fullPath: key };
    })
    .filter((page) => {
      return !page.path.includes('/components/') && !page.path.startsWith('/components/');
    });

  const columns = [
    { title: '文件路径', dataIndex: 'path', key: 'path', width: 500 },
    { title: '文件名', dataIndex: 'fileName', key: 'fileName', width: 300 },
    { title: '打开方式', key: 'action', width: 150 },
  ];

  const filteredPages = computed(() => {
    if (!searchText.value) return allPages;
    const keyword = searchText.value.toLowerCase();
    return allPages.filter(
      (page) => page.path.toLowerCase().includes(keyword) || page.fileName.toLowerCase().includes(keyword),
    );
  });

  function openInNewTab(path: string) {
    window.open(path, '_blank');
  }

  function openDrawer(path: string) {
    drawerTitle.value = path;
    // 添加预览参数，让 LAYOUT 只渲染内容区域
    drawerSrc.value = `${path}?preview=true`;
    drawerVisible.value = true;
  }
</script>
```

### 2.4 路由配置

**文件：** `src/router/routes/staticRouter.ts`

```typescript
import type { AppRouteRecordRaw } from '/@/router/types';
import { LAYOUT } from '/@/router/constant';

export const AI_ROUTE: AppRouteRecordRaw = {
  path: '',
  name: 'ai-parent',
  component: LAYOUT,
  meta: {
    title: 'ai',
  },
  children: [
    {
      path: '/ai',
      name: 'ai',
      component: () => import('/@/views/dashboard/ai/index.vue'),
      meta: {
        title: 'AI助手',
      },
    },
  ],
};

/**
 * 开发模式：页面浏览器路由（基础路由，无需登录即可访问）
 * 不使用 LAYOUT 包裹，作为独立页面渲染
 */
export const DEV_PAGES_ROUTE: AppRouteRecordRaw = {
  path: '/dev/pages',
  name: 'dev-pages-browser',
  component: () => import('/@/views/dev/DevPages.vue'),
  meta: {
    title: '页面浏览器',
    ignoreAuth: true, // 无需登录即可访问浏览器页面
  },
};

export const staticRoutesList = [AI_ROUTE];
```

**文件：** `src/router/routes/index.ts`

```typescript
import type { AppRouteRecordRaw, AppRouteModule } from '/@/router/types';

import { PAGE_NOT_FOUND_ROUTE, REDIRECT_ROUTE } from '/@/router/routes/basic';

import { mainOutRoutes } from './mainOut';
import { PageEnum } from '/@/enums/pageEnum';
import { t } from '/@/hooks/web/useI18n';
import { LAYOUT } from '/@/router/constant';
import { DEV_PAGES_ROUTE } from './staticRouter';

const modules = import.meta.glob('./modules/**/*.ts', { eager: true });

const routeModuleList: AppRouteModule[] = [];

// 加入到路由集合中
Object.keys(modules).forEach((key) => {
  const mod = (modules as Recordable)[key].default || {};
  const modList = Array.isArray(mod) ? [...mod] : [mod];
  routeModuleList.push(...modList);
});

export const asyncRoutes = [PAGE_NOT_FOUND_ROUTE, ...routeModuleList];

export const RootRoute: AppRouteRecordRaw = {
  path: '/',
  name: 'Root',
  redirect: PageEnum.BASE_HOME,
  meta: {
    title: 'Root',
  },
};

export const LoginRoute: AppRouteRecordRaw = {
  path: '/login',
  name: 'Login',
  component: () => import('/@/views/system/loginmini/MiniLogin.vue'),
  meta: {
    title: t('routes.basic.login'),
  },
};

export const Oauth2LoginRoute: AppRouteRecordRaw = {
  path: '/oauth2-app/login',
  name: 'oauth2-app-login',
  component: () => import('/@/views/system/loginmini/OAuth2Login.vue'),
  meta: {
    title: t('routes.oauth2.login'),
  },
};

export const TokenLoginRoute: AppRouteRecordRaw = {
  path: '/tokenLogin',
  name: 'TokenLoginRoute',
  component: () => import('/@/views/sys/login/TokenLoginPage.vue'),
  meta: {
    title: '带token登录页面',
    ignoreAuth: true,
  },
};

// Basic routing without permission
export const basicRoutes = [
  LoginRoute,
  RootRoute,
  ...mainOutRoutes,
  REDIRECT_ROUTE,
  TokenLoginRoute,
  Oauth2LoginRoute,
  // 开发模式下添加开发页面浏览器路由（无需登录即可访问）
  ...(import.meta.env.DEV ? [DEV_PAGES_ROUTE] : []),
  // 通配符路由必须放在最后，否则会拦截所有后续路由
  PAGE_NOT_FOUND_ROUTE,
];
```

### 2.5 权限守卫白名单

**文件：** `src/router/guard/permissionGuard.ts`

```typescript
// 白名单路径列表（无需登录即可访问）
const whitePathList: PageEnum[] = [LOGIN_PATH, OAUTH2_LOGIN_PAGE_PATH, SYS_FILES_PATH, TOKEN_LOGIN];

// 开发模式：添加开发页面浏览器到白名单
if (import.meta.env.DEV) {
  whitePathList.push('/dev/pages' as PageEnum);
}
```

## 3. 关键设计决策

### 3.1 路由结构优化

**选择：** 使用单个父路由 + 多个子路由

**原因：**
- 减少路由对象层级，提升路由匹配性能
- 所有开发页面共享同一个 LAYOUT 实例
- 避免为每个文件创建独立的路由对象

**对比：**
```typescript
// ❌ 方案A：每个文件独立路由对象
[
  { path: '', component: LAYOUT, children: [{ path: '/page1', ... }] },
  { path: '', component: LAYOUT, children: [{ path: '/page2', ... }] },
  // ... N 个路由对象
]

// ✅ 方案E：单个父路由包裹所有页面
[
  {
    path: '',
    component: LAYOUT,
    children: [
      { path: '/page1', ... },
      { path: '/page2', ... },
      // ... 所有页面作为子路由
    ]
  }
]
```

### 3.2 浏览器页面位置

**选择：** 将 `DEV_PAGES_ROUTE` 放在 `basicRoutes` 而非 `staticRoutesList`

**原因：**
- `basicRoutes` 在应用启动时注册，无需登录即可访问
- `staticRoutesList` 在登录后通过 `buildRoutesAction` 注册，需要认证
- 开发浏览器是开发工具，应该无需登录即可访问

### 3.3 通配符路由顺序

**选择：** 将 `PAGE_NOT_FOUND_ROUTE` 放在 `basicRoutes` 数组末尾

**原因：**
- Vue Router 按数组顺序匹配路由
- 通配符 `/:path(.*)*` 会匹配所有路径
- 如果放在前面，会拦截所有后续路由
- 必须放在所有具体路由之后

### 3.4 避免重复扫描

**选择：** 浏览器页面直接使用 `dynamicPages` 对象

**原因：**
- `dynamicPages` 已经在 `src/utils/dynamicPages.ts` 中定义
- 避免在浏览器页面中重复调用 `import.meta.glob`
- 保持数据源单一，减少维护成本

### 3.5 路由命名规则

**选择：** 使用 `dev-page-{path}` 格式

**原因：**
- 避免与后端菜单路由冲突
- 便于调试时识别开发路由
- 使用文件路径生成，保证唯一性

**示例：**
- 文件：`views/demo/cssz/MyPage.vue`
- 路由名称：`dev-page-demo-cssz-MyPage`
- 路由路径：`/demo/cssz/MyPage`

## 4. 涉及文件清单

| 文件路径 | 修改类型 | 说明 |
|---------|---------|------|
| `src/store/modules/permission.ts` | 修改 | 新增 `generateDevRoutes` 函数，在 `buildRoutesAction` 中追加开发路由 |
| `src/views/dev/DevPages.vue` | 新建 | 开发路由浏览器页面，支持新页面和抽屉两种打开方式 |
| `src/router/routes/staticRouter.ts` | 修改 | 新增 `DEV_PAGES_ROUTE` 定义（独立页面，不使用 LAYOUT） |
| `src/router/routes/index.ts` | 修改 | 导入 `DEV_PAGES_ROUTE`，添加到 `basicRoutes`，调整路由顺序 |
| `src/router/guard/permissionGuard.ts` | 修改 | 将 `/dev/pages` 加入白名单 |
| `src/layouts/default/index.vue` | 修改 | 支持预览模式（`?preview=true`），隐藏布局元素 |

## 5. 使用方式

### 5.1 访问开发浏览器

1. 启动开发服务器：`pnpm dev`
2. 访问 `http://localhost:xxxx/#/dev/pages`
3. 在浏览器页面中搜索或浏览所有页面
4. 通过"打开方式"列的按钮访问页面：
   - **新页面**：在新标签页中打开
   - **抽屉**：在当前页面的右侧抽屉中预览（无布局，仅显示组件本身）

### 5.2 直接访问页面

1. 新建页面文件：`src/views/demo/cssz/MyNewPage.vue`
2. 直接访问 URL：`http://localhost:xxxx/#/demo/cssz/MyNewPage`
3. 无需在菜单管理中配置路由

### 5.3 注意事项

- 开发浏览器和开发路由仅在开发模式下可用
- 生产环境不会注册开发路由，`/dev/pages` 页面也不存在
- 开发页面需要登录后才能访问（与正常页面一致）
- 跳过 `components/` 目录下的文件

## 6. 验证方式

### 6.1 功能验证

1. **浏览器页面显示**
   - [ ] 访问 `/dev/pages` 能正常显示开发浏览器页面
   - [ ] 页面列出所有 `views` 目录下的 `.vue` 和 `.tsx` 文件
   - [ ] 搜索功能正常工作（支持路径和文件名搜索）
   - [ ] 点击"访问"按钮能正确跳转到对应页面

2. **路由访问**
   - [ ] 新开发页面能通过 URL 直接访问
   - [ ] 开发页面需要登录后才能访问
   - [ ] 未登录时访问开发页面，跳转到登录页

3. **环境隔离**
   - [ ] 生产构建后，`/dev/pages` 页面不存在
   - [ ] 生产构建后，开发路由不会被注册
   - [ ] `import.meta.env.DEV` 在生产中为 false，相关代码被移除

### 6.2 性能验证

1. **路由注册性能**
   - [ ] 开发模式下，路由注册耗时 < 100ms
   - [ ] 浏览器页面加载耗时 < 500ms

2. **构建产物**
   - [ ] 生产构建后，开发相关代码不被打包
   - [ ] 构建产物大小无明显增加

## 7. 后续优化方向

### 7.1 可选功能

- **路由分组**：按目录结构分组显示页面
- **收藏功能**：支持收藏常用开发页面
- **快速导航**：支持键盘快捷键快速导航
- **路由信息**：显示文件大小、最后修改时间等信息

### 7.2 性能优化

- **虚拟滚动**：页面数量过多时使用虚拟滚动
- **懒加载**：浏览器页面列表懒加载
- **缓存优化**：缓存页面列表，避免重复计算
