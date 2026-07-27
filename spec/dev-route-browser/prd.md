# 开发路由浏览器 - 产品需求文档

## 背景

当前项目权限模式为 `BACK`（后端菜单驱动），所有路由完全依赖后端 `getBackMenuAndPerms` 返回的菜单数据。开发过程中，每新建一个页面，必须先去菜单管理中配置路由才能预览，流程繁琐。

## 目标

- 开发模式下，无需在菜单管理中配置路由即可预览新开发的页面
- 提供统一的开发路由浏览器页面，列出所有可访问的页面
- 生产环境不受影响，开发路由不会被打包

## 功能需求

### 1. 自动注册开发路由

- 开发模式下，自动为 `views` 目录下的所有 `.vue` 和 `.tsx` 文件注册路由
- 路由路径与文件路径对应，如 `views/demo/cssz/MyPage.vue` 对应 `/demo/cssz/MyPage`
- 支持最多二级目录嵌套：`views/demo/page.vue`（一级）、`views/demo/sub/page.vue`（二级）
- 不支持三级及以上目录（如 `views/demo/sub/sub2/page.vue` 不会被注册）
- 支持动态路由参数：文件名中包含 `:param` 时保留为路由参数，如 `views/demo/User_:id.vue` 对应 `/demo/User_:id`
- 跳过 `components/` 等非页面目录
- 避免与后端菜单已配置的路由冲突

### 2. 开发路由浏览器页面

- 提供 `/dev/pages` 页面，列出所有可访问的页面
- 支持按文件路径或文件名搜索过滤
- 点击页面项可直接跳转到对应路由
- 显示页面总数统计

### 3. 访问权限

- `/dev/pages` 页面无需登录即可访问
- 仅开发模式（`import.meta.env.DEV`）下可用
- 生产环境不注册开发路由，不显示浏览器页面

## 使用场景

1. 开发者新建页面文件 `src/views/demo/cssz/MyNewPage.vue`
2. 访问 `http://localhost:xxxx/#/dev/pages` 浏览所有页面
3. 在列表中找到新页面，点击"访问"按钮直接预览
4. 或直接通过 URL `http://localhost:xxxx/#/demo/cssz/MyNewPage` 访问

## 方案选择

选择**混合优化方案**，理由：
- 无需开发 Vite 插件，实现复杂度低
- 自动包含新文件，开发体验好
- 使用单个父路由优化，性能可接受
- 路径与生产环境一致，维护成本低

**核心优化点：**
- 使用单个父路由包裹所有开发页面，减少路由对象层级
- 浏览器页面直接使用 `dynamicPages` 对象，避免重复扫描
- 路由名称使用文件路径生成，避免冲突
- 在 `basicRoutes` 中注册浏览器路由，确保无需登录

## 验收标准

- [ ] 访问 `/dev/pages` 能正常显示开发浏览器页面
- [ ] 页面列出所有 `views` 目录下的 `.vue` 和 `.tsx` 文件
- [ ] 搜索功能正常工作（支持路径和文件名搜索）
- [ ] 点击"访问"按钮能正确跳转到对应页面
- [ ] 新开发页面能通过开发浏览器直接访问
- [ ] 开发页面需要登录后才能访问
- [ ] 生产构建后，开发路由不会被打包，`/dev/pages` 页面不存在

## 验收用例（仅开发环境）

### 用例1：通过浏览器页面访问新页面
1. 新建文件 `src/views/demo/test/NewPage.vue`
2. 访问 `http://localhost:xxxx/#/dev/pages`
3. 在搜索框输入"NewPage"
4. 点击搜索结果中的"访问"按钮
5. 验证：页面正确跳转到 `/demo/test/NewPage`，页面正常渲染

### 用例2：通过 URL 直接访问开发页面
1. 新建文件 `src/views/demo/cssz/MyPage.vue`
2. 直接访问 `http://localhost:xxxx/#/demo/cssz/MyPage`
3. 验证：页面正常渲染，无需在菜单管理中配置路由

### 用例3：二级目录页面
1. 新建文件 `src/views/demo/sub/SubPage.vue`
2. 访问 `http://localhost:xxxx/#/dev/pages`
3. 验证：列表中显示 `demo/sub/SubPage`
4. 点击"访问"按钮，验证：页面正确跳转到 `/demo/sub/SubPage`

### 用例4：未登录访问开发页面
1. 退出登录状态
2. 直接访问 `http://localhost:xxxx/#/demo/test/NewPage`
3. 验证：自动跳转到登录页
4. 登录后，自动跳转回原页面

### 用例5：动态路由参数
1. 新建文件 `src/views/demo/User_:id.vue`
2. 访问 `http://localhost:xxxx/#/dev/pages`
3. 验证：列表中显示 `demo/User_:id`
4. 访问 `http://localhost:xxxx/#/demo/User_123`
5. 验证：页面正常渲染，可通过 `route.params.id` 获取参数值
