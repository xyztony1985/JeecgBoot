# CSSZ System 项目

## 项目简介

本项目是由 **两个 JeecgBoot 项目整合** 而成的独立运行系统：

1. **jeecg-system-biz** - JeecgBoot 系统管理业务模块
   - 来源：`jeecg-boot/jeecg-module-system/jeecg-system-biz`
   - 作用：提供系统管理的核心业务功能（用户管理、角色管理、部门管理、字典管理等）

2. **jeecg-system-cloud-start** - JeecgBoot 微服务启动模块
   - 来源：`jeecg-boot/jeecg-server-cloud/jeecg-system-cloud-start`
   - 作用：提供微服务架构的启动配置和基础设施

通过整合这两个项目，形成了一个**既可作为单体应用运行，又具备微服务架构能力**的独立系统模块。

---

## 代码结构及来源说明

```
system/
├── pom.xml                                    # 【整合】项目依赖配置
│
├── src/main/java/org/jeecg/
│   │
│   ├── SystemApplication.java                 # 【来自 jeecg-system-cloud-start】
│   │                                          #   - 原类名为 SystemCloudApplication
│   │                                          #   - 重命名为 SystemApplication
│   │
│   ├── config/                                # 【整合】配置类目录
│   │   ├── init/                              # 【来自 jeecg-system-cloud-start】
│   │   │   ├── CodeGenerateDbConfig.java      #   - 代码生成器数据库配置
│   │   │   ├── CodeTemplateInitListener.java  #   - 代码模板初始化监听器
│   │   │   ├── ShiroCacheClearRunner.java     #   - Shiro 缓存清理运行器
│   │   │   ├── SystemInitListener.java        #   - 系统初始化监听器
│   │   │   ├── TomcatFactoryConfig.java       #   - Tomcat 工厂配置
│   │   │   └── UndertowConfiguration.java     #   - Undertow 配置
│   │   │
│   │   ├── jimureport/                        # 【来自 jeecg-system-biz】
│   │   │   ├── JimuDragExternalServiceImpl.java
│   │   │   └── JimuReportTokenService.java
│   │   │
│   │   └── firewall/                          # 【来自 jeecg-system-biz】
│   │
│   └── modules/                               # 【全部来自 jeecg-system-biz】
│       ├── system/                            # 系统管理核心模块
│       ├── message/                           # 消息管理模块
│       ├── quartz/                            # 定时任务管理模块
│       ├── monitor/                           # 系统监控模块
│       ├── openapi/                           # 开放 API 管理模块
│       ├── oss/                               # 对象存储管理模块
│       ├── cas/                               # CAS 单点登录模块
│       ├── ngalain/                           # Ng-Alain 前端适配模块
│       ├── api/                               # 系统 API 接口模块
│       ├── airag/                             # AI 大模型管理模块
│       └── aop/                               # 切面编程模块
│
└── src/main/resources/
    │
    ├── application.yml                        # 【整合】主配置文件
    │                                            #   - 合并了两个项目的配置
    │                                            #   - 默认激活 dev profile
    │                                            #   - 禁用 Nacos（方便本地开发）
    ├── application-dev.yml
    │
    ├── jeecg/                                 # 【来自 jeecg-system-biz】
    ├── static/                                # 【来自 jeecg-system-biz】
    └── templates/                             # 【来自 jeecg-system-biz】
```

---

## 核心文件来源对照表

### Java 源代码

| 文件路径 | 来源项目 | 说明 |
|---------|---------|------|
| `SystemApplication.java` | jeecg-system-cloud-start | 原类名 SystemCloudApplication，重命名为 SystemApplication |
| `config/init/*` | jeecg-system-cloud-start | 系统初始化配置 |
| `config/jimureport/*` | jeecg-system-biz | 积木报表配置 |
| `config/firewall/*` | jeecg-system-biz | SQL 注入防火墙 |
| `modules/*` | jeecg-system-biz | 所有业务模块 |

### 配置文件

| 文件路径 | 来源项目 | 说明 |
|---------|---------|------|
| `application.yml` | **整合** | 主配置，合并两个项目 |
| `application-dev.yml` | jeecg-system-cloud-start | 开发环境配置 |
| `application-prod.yml` | jeecg-system-cloud-start | 生产环境配置 |
| `application-test.yml` | jeecg-system-cloud-start | 测试环境配置 |
| `config/application-liteflow.yml` | jeecg-system-cloud-start | LiteFlow 配置 |
| `jeecg/code-template-online/*` | jeecg-system-biz | 代码生成模板 |
| `static/*` | jeecg-system-biz | 静态资源 |
| `templates/*` | jeecg-system-biz | 模板文件 |
| `*.properties` | jeecg-system-biz | 属性配置文件 |

### 项目配置 (pom.xml)

| 配置项 | 来源 | 说明 |
|--------|------|------|
| 父 POM | 新建 | 继承 `com.cssz.boot:parent` |
| `jeecg-system-local-api` | jeecg-system-biz | 系统本地 API |
| `jeecg-online` | jeecg-system-biz | 在线开发模块 |
| `jeecg-boot-module-airag` | jeecg-system-biz | AI 大模型模块 |
| `jeecg-boot-starter-cloud` | jeecg-system-cloud-start | 微服务启动器 |
| `jeecg-boot-starter-job` | jeecg-system-cloud-start | XXL-Job 启动器 |
| `hibernate-core` | jeecg-system-biz | Hibernate ORM |
| `weixin4j` | jeecg-system-biz | 微信 SDK |
| `jimureport-*` | jeecg-system-biz | 积木报表 |

---

## 项目特点

### 1. 独立运行
- 不依赖原 jeecg-system-biz 和 jeecg-system-cloud-start 项目
- 可单独编译、打包、部署和运行

### 2. 架构灵活
- **单体模式**：直接运行作为独立应用
- **微服务模式**：可接入 Nacos 注册中心，作为微服务集群的一部分

### 3. 依赖精简
- 继承 `cssz-boot-parent` 统一管理依赖版本

### 4. 配置优化
- 添加了 `spring.profiles.active: dev` 默认激活开发环境
- 添加了 `allow-bean-definition-overriding: true` 解决 bean 冲突
- 禁用了 Nacos 服务（默认），方便本地开发测试

---

## 如何运行

### 1. 编译项目
```bash
cd cssz-boot/system
mvn clean compile -Pdev
```

### 2. 运行项目
```bash
mvn spring-boot:run -Pdev
```

### 3. 访问服务
- 本地地址：http://localhost:8080/jeecg-boot
- Swagger 文档：http://localhost:8080/jeecg-boot/doc.html

---

## 注意事项

1. **数据库配置**：默认使用 MySQL，数据库名为 `jeecg-boot`，请确保数据库已创建
2. **Redis 配置**：默认连接本地 Redis（127.0.0.1:6379）
3. **Nacos 配置**：默认禁用，如需启用请修改 `application.yml` 中的 `spring.cloud.nacos.*.enabled` 为 `true`
