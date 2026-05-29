# CSSZ Base

基于 JeecgBoot Base Core 的基础库（Library），供其他项目引入使用。

## 📦 项目信息

- **GroupId**: com.cssz.boot
- **ArtifactId**: base
- **Version**: 2.0.0
- **Packaging**: jar
- **类型**: 基础库（非可执行应用）

## 🎯 项目定位

**CSSZ Base 是一个基础包（Library）**，不是独立运行的应用程序。它封装了 JeecgBoot Base Core 的核心功能，并提供了一些通用的工具类和常量，方便其他项目快速集成和使用。

### 主要特点

- ✅ **纯库项目**：无启动类、无可执行配置
- ✅ **依赖传递**：自动引入 jeecg-boot-base-core 及其所有依赖
- ✅ **开箱即用**：提供常用工具类和常量定义
- ✅ **易于扩展**：可根据业务需求添加自定义功能

## 📋 依赖说明

本项目引用了以下核心依赖：
- `org.jeecgframework.boot3:jeecg-boot-base-core:3.9.1`

通过引入 cssz-base，其他项目可以间接使用：
- MyBatis Plus 数据访问
- Shiro 权限控制
- JWT 认证
- 文件存储（MinIO、阿里云 OSS）
- 短信服务
- AI 集成
- 代码生成器
- 等等...

## 🚀 如何使用

### 方式一：安装到本地 Maven 仓库（推荐用于开发测试）

```bash
cd d:\Code\JeecgBoot\cssz-boot\cssz-base
mvn clean install -DskipTests
```

然后在其他项目的 `pom.xml` 中添加依赖：

```
<dependencies>
    <!-- 引入 base -->
    <dependency>
        <groupId>com.cssz.boot</groupId>
        <artifactId>base</artifactId>
        <version>2.0.0</version>
    </dependency>
</dependencies>
```

### 方式二：部署到私有 Maven 仓库（团队共享）

如果有 Nexus 或 Artifactory 等私有仓库：

```bash
mvn clean deploy -DskipTests
```

### 在其他项目中使用

```
package com.example.controller;

import com.cssz.constant.CsszBaseConstant;
import com.cssz.util.CsszBaseUtil;
import org.jeecg.common.api.vo.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestController {

    @GetMapping("/hello")
    public Result<String> hello() {
        // 使用 cssz-base 提供的常量
        String code = CsszBaseConstant.SUCCESS_CODE;
        
        // 使用 cssz-base 提供的工具类
        String str = null;
        boolean isEmpty = CsszBaseUtil.isEmpty(str); // true
        
        // 使用 JeecgBoot 的功能
        return Result.OK("Hello from cssz-base!");
    }
}
```

## 📁 项目结构

```
cssz-base/
├── src/
│   ├── main/
│   │   ├── java/com/cssz/
│   │   │   ├── constant/          # 常量定义
│   │   │   │   └── CsszBaseConstant.java
│   │   │   └── util/              # 工具类
│   │   │       └── CsszBaseUtil.java
│   │   └── resources/             # 资源文件（可选）
│   └── test/                      # 测试代码（可选）
├── pom.xml                        # Maven 配置
├── README.md                      # 项目说明
└── JAR_USAGE_GUIDE.md            # 详细使用指南
```

## 🔧 编译和打包

### 编译项目

```bash
mvn clean compile
```

### 打包为 JAR

```bash
mvn clean package -DskipTests
```

生成的文件：
- `target/base-2.0.0.jar` - 主 JAR 包
- `target/base-2.0.0-sources.jar` - 源码包
- `target/base-2.0.0-javadoc.jar` - 文档包

### 安装到本地仓库

```bash
mvn clean install -DskipTests
```

## ⚙️ 配置说明

作为基础库，cssz-base **不包含**应用级配置文件（如 application.yml）。

使用此库的项目需要自行配置：
- 数据库连接
- Redis 配置
- JeecgBoot 相关配置
- 应用端口等

参考 JeecgBoot 官方文档进行配置。

## 📝 扩展开发

### 添加工具类

在 `src/main/java/com/cssz/util/` 目录下创建新的工具类：

```
package com.cssz.util;

public class MyCustomUtil {
    public static String doSomething(String input) {
        // 实现你的逻辑
        return "Result: " + input;
    }
}
```

### 添加常量

在 `src/main/java/com/cssz/constant/` 目录下创建新的常量类：

```
package com.cssz.constant;

public class MyCustomConstant {
    public static final String MY_VALUE = "my_value";
    
    private MyCustomConstant() {
        throw new UnsupportedOperationException("Utility class");
    }
}
```

### 添加 Service 层

如果需要封装业务逻辑，可以添加 Service 类：

```
package com.cssz.service;

import org.springframework.stereotype.Service;

@Service
public class MyCustomService {
    public String process(String data) {
        // 业务逻辑处理
        return "Processed: " + data;
    }
}
```

## ⚠️ 注意事项

1. **这不是一个可执行应用**：没有 main 方法，不能直接运行
2. **版本管理**：当前是 SNAPSHOT 版本，适合开发阶段；生产环境建议使用正式版本号（如 1.0.0）
3. **依赖传递**：引入 cssz-base 会自动引入 jeecg-boot-base-core 的所有依赖
4. **配置责任**：使用该库的项目需要自行配置数据库、Redis 等
5. **兼容性**：确保使用的项目与 JDK 17+ 兼容

## 🔄 版本历史

- **2.0.0** (2026-05-28)
  - 正式版本发布
  - 基于 jeecg-boot-base-core 3.9.1
  - 提供基础工具类和常量

## 📞 常见问题

### Q1: 为什么我的项目找不到 cssz-base？
**A**: 确保已执行 `mvn install`，并检查本地仓库路径 `~/.m2/repository/com/cssz/cssz-base/`

### Q2: 如何查看 cssz-base 提供了哪些功能？
**A**: 
- 查看源码目录结构
- 使用 IDE 的代码提示功能
- 执行 `mvn javadoc:jar` 生成文档

### Q3: 可以修改 cssz-base 的源码吗？
**A**: 可以，但建议：
- Fork 到自己的仓库
- 修改后重新 install/deploy
- 或者通过继承和扩展的方式使用

### Q4: 如何处理依赖冲突？
**A**: 使用 `mvn dependency:tree` 查看依赖树，必要时使用 `<exclusions>` 排除冲突的依赖。

## 📄 许可证

请参考 JeecgBoot 官方许可证

---

**最后更新**: 2026-05-28  
**版本**: 2.0.0  
**状态**: ✅ 可作为基础库被其他项目引入
