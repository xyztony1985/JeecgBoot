# 全局组件导入机制

前端项目通过多层机制实现组件的全局可用，使各页面使用组件时无需手动 `import`。

## 机制总览

| 层级 | 机制 | 关键文件 | 覆盖范围 |
|------|------|---------|---------|
| 1 | Vite 自动按需导入 | `build/vite/plugin/index.ts` | 所有 ant-design-vue 组件 + iconify 图标 |
| 2 | 手动全局注册 | `src/components/registerGlobComp.ts` | Icon、AIcon、Button、Tinymce 等核心组件 |
| 3 | 第三方异步注册 | `src/settings/registerThirdComp.ts` | JVxeTable、Picker、dayjs |
| 4 | 外部包按需加载 | `src/utils/monorepo/registerPackages.ts` | @jeecg/aiflow 等外部模块 |

以上注册在 `src/main.ts` 的 `bootstrap()` 中按顺序调用。

## 1. Vite 自动按需导入（ant-design-vue）

通过 `unplugin-vue-components` 插件 + `AntDesignVueResolver` 实现，配置位于 `build/vite/plugin/index.ts`：

```ts
Components({
  resolvers: [
    AntDesignVueResolver({
      importStyle: false,
      exclude: ['AButton', 'Button'], // 排除 antd Button，让 a-button 指向 BasicButton
    }),
    IconsResolver({ prefix: 'iconify' }),
  ],
  dirs: [],   // 不扫描本地目录
  dts: false, // 不生成类型声明
})
```

**效果**：模板中直接使用 `<a-input>`、`<a-select>`、`<a-table>` 等 ant-design-vue 组件，构建时自动按需导入，无需手动 `import`。

**例外**：`AButton` 被排除，项目用自定义 `BasicButton` 替代。

## 2. 手动全局注册

入口文件：`src/components/registerGlobComp.ts`，由 `main.ts` 调用 `registerGlobComp(app)`。

注册的组件：

| 组件名 | 来源 | 说明 |
|--------|------|------|
| `Icon` | `./Icon` | 通用图标 |
| `AIcon` | `./jeecg/AIcon.vue` | Jeecg 自定义图标 |
| `JUploadButton` | `./Button` | 上传按钮 |
| `ASpaceCompact` | `Space.Compact`（ant-design-vue） | Resolver 无法自动解析子组件，需手动注册 |
| `Tinymce` | `./Tinymce/src/Editor.vue` | 富文本编辑器，异步加载 |
| `Button`（BasicButton） | `./Button` | 通过 `app.use(Button)` 注册，覆盖 antd 原生按钮 |
| Online 组件 | `views/super/online/...` | `JOnlineSearchSelect`、`SuperQuery` |

## 3. 第三方异步全局注册

入口文件：`src/settings/registerThirdComp.ts`，由 `main.ts` 调用 `registerThirdComp(app)`。

| 组件/属性 | 说明 |
|-----------|------|
| `JVxeTable` | 可编辑表格，首次渲染时才加载 vxe-table |
| `Picker` | 聊天表情包（emoji-mart-vue-fast） |
| `$dayjs` | 挂载为全局属性，可通过 `this.$dayjs` 或 `inject('$dayjs')` 使用 |

## 4. 外部包按需加载

入口文件：`src/utils/monorepo/registerPackages.ts`，由 `main.ts` 调用 `registerPackages(app)`。

管理外部 monorepo 包（如 `@jeecg/aiflow`）的按需加载。当路由匹配到组件但本地找不到时，通过 `loadPackageComponent()` 从外部包中查找并加载。

## 如何将组件加入全局注册

根据组件特性选择合适的方式：

### 方式一：直接注册（轻量同步组件）

适用于体积小、几乎每个页面都可能用到的组件。

在 `src/components/registerGlobComp.ts` 的 `registerGlobComp` 函数中添加：

```ts
import MyComp from '/@/components/MyComp.vue';

// 在 registerGlobComp 函数内添加
app.component('MyComp', MyComp);
```

模板中即可直接使用 `<MyComp />`，无需 import。

### 方式二：异步注册（重型组件）

适用于体积大、并非所有页面都使用的组件（如富文本编辑器、可编辑表格）。

```ts
import { createAsyncComponent } from '/@/utils/factory/createAsyncComponent';

// 在 registerGlobComp 函数内添加
app.component(
  'MyHeavyComp',
  createAsyncComponent(() => import('/@/components/MyHeavyComp.vue'), {
    loading: true, // 加载中显示 Spin
  })
);
```

`createAsyncComponent` 基于 Vue 的 `defineAsyncComponent`，额外提供：
- 加载中自动显示 `Spin` 组件
- 超时控制（默认 30s）
- 网络错误自动重试（最多 3 次）

### 方式三：withInstall + app.use()（需要插件化的组件）

适用于需要同时注册组件和全局属性的场景。先用 `withInstall` 包装，再通过 `app.use()` 注册。

```ts
// 1. 在组件的 index.ts 中导出时包装
import { withInstall } from '/@/utils';
import MyComp from './src/MyComp.vue';

export const MyComp = withInstall(MyComp);
// 可选：同时挂载别名
// export const MyComp = withInstall(MyComp, '$myComp');
```

```ts
// 2. 在 registerGlobComp.ts 中注册
import { MyComp } from '/@/components/MyComp';

app.use(MyComp);
```

`withInstall`（定义在 `src/utils/index.ts`）会给组件添加 `install` 方法，使其成为 Vue 插件：

```ts
// withInstall 内部实现
comp.install = (app: App) => {
  app.component(comp.name || comp.displayName, component);
  if (alias) {
    app.config.globalProperties[alias] = component;
  }
};
```

### 方式四：插件对象批量注册（一组相关组件）

适用于需要一次性注册多个相关组件的场景。创建一个带 `install` 方法的对象：

```ts
// src/components/MyModule/index.ts
import type { App } from 'vue';
import CompA from './CompA.vue';
import CompB from './CompB.vue';

export const registerMyModule = {
  install(app: App) {
    app.component('CompA', CompA);
    app.component('CompB', CompB);
  },
};
```

```ts
// 在 registerGlobComp.ts 中注册
import { registerMyModule } from '/@/components/MyModule';

app.use(registerMyModule);
```

项目中 Online 模块组件（`registerOnlineComp`）就是这种方式。

### 选择指南

| 场景 | 推荐方式 | 示例 |
|------|---------|------|
| 轻量通用组件 | 方式一：`app.component()` | Icon、AIcon |
| 体积大的功能组件 | 方式二：`createAsyncComponent` | Tinymce、JVxeTable |
| 需要插件化/别名 | 方式三：`withInstall` + `app.use()` | Button（BasicButton） |
| 一组相关组件批量注册 | 方式四：插件对象 | Online 模块组件 |
| ant-design-vue 组件 | 无需操作，自动按需导入 | a-input、a-select 等 |

## 初始化顺序

`src/main.ts` 中 `bootstrap()` 的调用顺序：

```
1. createApp(App)
2. setupStore(app)           // 配置存储
3. setupI18n(app)            // 多语言
4. initAppConfigStore()      // 内部系统配置
5. registerPackages(app)     // ← 外部包注册
6. registerGlobComp(app)     // ← 全局组件注册
7. setupRouter(app)          // 路由
8. registerThirdComp(app)    // ← 第三方异步组件
9. app.mount()               // 挂载
```
