## 快速开始

快速把项目跑起来，是成功的第一步。

以下将按默认配置，启动项目。

默认配置是指

### 环境准备

- Node.js 20+

    > 下载地址：<https://nodejs.org/zh-cn/download>

- pnpm 9+

    ```bash
    # 已有 Node.js 环境后，全局安装 pnpm
    npm install pnpm -g
    ```

### 启动项目

- 安装依赖

    ```bash
    cd JeecgBoot/jeecgboot-vue3

    pnpm install
    ```

- 配置接口地址 `.env.development`

    ```bash
    VITE_PROXY = [["/jeecgboot","http://localhost:8080/jeecg-boot"],["/upload","http://localhost:3300/upload"]]
    VITE_GLOB_DOMAIN_URL=http://localhost:8080/jeecg-boot
    ```

    > 说明：把`http://localhost:8080/jeecg-boot` 换成自己地址，其他不用改。


- 运行项目

    ```bash
    pnpm dev
    ```

- 访问 <http://localhost:3100/>，默认账号密码：`admin / 123456`


## 部署打包

```bash
pnpm build
```

## 技术文档

*   官方文档：[https://help.jeecg.com](https://help.jeecg.com)
*   [快速入门](http://jeecg.com/doc/quickstart) 
*   [常见问题](http://help.jeecg.com/qa) 
*   [Vue3 文档](https://cn.vuejs.org/)
*   [Vben文档](https://doc.vvbin.cn)
*   [Ant-Design-Vue](https://www.antdv.com/docs/vue/introduce-cn/)
*   [TypeScript](https://www.typescriptlang.org/)
*   [Vue-router](https://router.vuejs.org/zh)
*   [Es6](https://es6.ruanyifeng.com/)
*   [Vitejs](https://cn.vitejs.dev/guide/)
*   [Pinia(vuex替代方案)](https://pinia.esm.dev/introduction.html)
*   [Vue-RFCS](https://github.com/vuejs/rfcs)
*   [vxetable文档](https://vxetable.cn)
