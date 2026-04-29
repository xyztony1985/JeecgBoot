## 快速开始

快速把项目成功跑起来，至关重要。

以下将按默认配置，启动项目。

默认配置是指 [配置文件](jeecg-module-system/jeecg-system-start/src/main/resources/application-dev.yml) 保持默认值，包括不限于MySQL、Redis等基础设施。

### 环境准备

- Docker，推荐 Windows + WSL+ Ubuntu 24.04

- MySql 5.7+、Redis 3.2+

  ```bash
  # docker 运行 mysql
  docker run -d --name mysql \
    -p 3306:3306 \
    -e MYSQL_ROOT_PASSWORD=root \
    -v /app/mysql:/var/lib/mysql \
    mysql:8.0 \
    --character-set-server=utf8mb4 \
    --collation-server=utf8mb4_unicode_ci \
    --default-time-zone='Asia/Shanghai'

  # docker 运行 redis
  docker run -d --name redis \
    -p 6379:6379 \
    -v /app/redis:/usr/local/etc/redis \
    redis:8.0-alpine
  ```

- 初始化数据库

  > 执行 [数据库初始化脚本](db/jeecgboot-mysql-5.7.sql)

- JDK 17+、Maven 3.9+

  > 自己搞定

- IDE：[IntelliJ IDEA](https://www.jetbrains.com/idea/download/) 或 [VSCode](https://code.visualstudio.com/)

### 启动项目

- 安装依赖

  ```bash
  mvn clean install -Dmaven.test.skip=true
  ```

- 启动项目

  ```bash
  # 启动类
  # jeecg-module-system/jeecg-system-start/src/main/java/org/jeecg/JeecgSystemApplication.java

  cd /d jeecg-module-system\jeecg-system-start
  mvn spring-boot:run
  ```

- 访问 <http://localhost:8080/jeecg-boot/doc.html>，正常打开，说明启动成功



## 目录结构

```
项目结构
├─jeecg-boot-parent（父POM： 项目依赖、modules组织）
│  ├─jeecg-boot-base-core（共通模块： 工具类、config、权限、查询过滤器、注解等）
│  ├─jeecg-module-demo    示例代码
│  ├─jeecg-module-system  System系统管理目录
│  │  ├─jeecg-system-biz    System系统管理权限等功能
│  │  ├─jeecg-system-start  System单体启动项目(8080）
│  │  ├─jeecg-system-api    System系统管理模块对外api
│  │  │  ├─jeecg-system-cloud-api   System模块对外提供的微服务接口
│  │  │  ├─jeecg-system-local-api   System模块对外提供的单体接口
│  ├─jeecg-server-cloud           --微服务模块
     ├─jeecg-cloud-gateway       --微服务网关模块(9999)
     ├─jeecg-cloud-nacos       --Nacos服务模块(8848)
     ├─jeecg-system-cloud-start  --System微服务启动项目(7001)
     ├─jeecg-demo-cloud-start    --Demo微服务启动项目(7002)
     ├─jeecg-visual
        ├─jeecg-cloud-monitor       --微服务监控模块 (9111)
        ├─jeecg-cloud-xxljob        --微服务xxljob定时任务服务端 (9080)
        ├─jeecg-cloud-sentinel     --sentinel服务端 (9000)
        ├─jeecg-cloud-test           -- 微服务测试示例（各种例子）
           ├─jeecg-cloud-test-more         -- 微服务测试示例（feign、熔断降级、xxljob、分布式锁）
           ├─jeecg-cloud-test-rabbitmq     -- 微服务测试示例（rabbitmq）
           ├─jeecg-cloud-test-seata          -- 微服务测试示例（seata分布式事务）
           ├─jeecg-cloud-test-shardingsphere    -- 微服务测试示例（分库分表）
```

## 技术文档

- [快速入门](http://www.jeecg.com/doc/quickstart)  
- [代码生成使用](https://help.jeecg.com/java/codegen/online) 
- [开发文档](https://help.jeecg.com)  
