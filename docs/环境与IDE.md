# 环境与 VS Code 报错处理

> 截图里的两个报错，一个是“项目没被当成 Maven 工程”，一个是“依赖还没下载”。不是代码写错了。

## 1. 两个报错分别是什么

### 报错 A：`The declared package "com.logistics" does not match the expected package "main.java.com.logistics"`

**不是依赖问题。** 这是 VS Code 把 `logistics-server/src` 当成了源码根目录，所以它认为包名应该是 `main.java.com.logistics`。

正确源码根是：

```
logistics-server/src/main/java
```

出现这个报错，通常是因为：

- 打开的是仓库根目录 `logistics-network`，Java 扩展没把子目录 `logistics-server` 当成 Maven 工程。
- 或者以前有过 `.vscode/settings.json`，里面写了 `"java.project.sourcePaths": ["src"]`，Java 语言服务器缓存了旧配置。

### 报错 B：`The import org.springframework.boot cannot be resolved`

**这个是依赖没下载。** `pom.xml` 里写了 Spring Boot，但本机还没有 `~/.m2` 仓库，Maven 也没跑过，所以 Java 扩展找不到 Spring 的 jar。

红波浪线在依赖下载完、Maven 工程导入后会消失。

## 2. 先修项目识别（报错 A）

### 方法 1：用工作区文件打开（推荐）

根目录已经放了：

```
logistics-network.code-workspace
```

VS Code 菜单：**文件 → 从文件打开工作区**，选它。

这个工作区把三个目录分别作为根：

- `logistics-server`（有 `pom.xml`，Java 扩展能识别为 Maven 工程）
- `logistics-web`
- `docs`

打开后，Java 扩展会把 `src/main/java` 正确识别为源码根，`com.logistics` 就不再报包名错误。

### 方法 2：直接打开后端目录

VS Code 菜单：**文件 → 打开文件夹**，选：

```
C:\Users\RAIN\logistics-network\logistics-server
```

这样 Java 扩展只看到 `pom.xml` 和 `src/main/java`，也会正确识别。

### 然后清一次 Java 缓存

不管用哪种方法，打开后按 `Ctrl+Shift+P`，执行：

```
Java: Clean Java Language Server Workspace
```

选择 **Restart and delete**。等右下角 `Java: Ready`。

> 这一步会清掉旧的 `java.project.sourcePaths: ["src"]` 缓存，包名错误一般就没了。

## 3. 再装依赖（报错 B）

### 第 0 步：确认 JAVA_HOME

在终端运行：

```powershell
echo $env:JAVA_HOME
```

如果输出为空，当前窗口先设置：

```powershell
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-25.0.4.101-hotspot"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
```

永久生效：在“系统环境变量”里新建 `JAVA_HOME`，值填上面的 JDK 目录；再把 `%JAVA_HOME%\bin` 加到 `Path`。改完新开终端。

### 第一步：在 VS Code 终端里跑 Maven Wrapper

打开终端，进入后端目录：

```powershell
cd C:\Users\RAIN\logistics-network\logistics-server
.\mvnw.cmd -v
```

- 第一次会下载 Maven 3.10.0 和依赖，需要网络。
- 看到 `Apache Maven 3.10.0` 就说明 Maven 能用了。

然后下载项目依赖：

```powershell
.\mvnw.cmd dependency:go-offline
```

或者直接启动：

```powershell
.\mvnw.cmd spring-boot:run
```

第一次会比较慢，属正常。

### 第二步：让 VS Code 重新导入

Maven 下载完后，按 `Ctrl+Shift+P`：

```
Maven: Reload Projects
```

或者：

```
Java: Reload Projects
```

等 `Java: Ready`，Spring 的红波浪线应该消失。

### 如果 `mvnw` 下载失败

说明网络或证书有问题。两种做法：

**做法 1：手动下载 Maven**

1. 浏览器打开：  
   https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.10.0/apache-maven-3.10.0-bin.zip
2. 解压到，例如：`C:\Users\RAIN\Tools\apache-maven-3.10.0`
3. 把 `C:\Users\RAIN\Tools\apache-maven-3.10.0\bin` 加到系统 `PATH`
4. 新开终端，运行 `mvn -v` 确认
5. 再运行 `mvn dependency:go-offline` 或 `mvn spring-boot:run`

**做法 2：换网络**

- 学校/公司网络可能拦了 Maven Central。
- 用手机热点试一次。
- 或者配置学校/公司的 Maven 镜像（`settings.xml` 的 `<mirrors>`）。

## 4. 验证是否好了

在 `logistics-server` 目录：

```powershell
.\mvnw.cmd -v
.\mvnw.cmd test
```

- `mvnw -v` 能打印 Maven 版本。
- `mvn test` 能编译通过（现在只有启动类，很快）。

在 VS Code 里：

- `LogisticsApplication.java` 不再有红波浪线。
- 左侧 **Java Projects** 里能看到 `logistics-server`。
- `src/main/java` 被识别为源码根，而不是 `src`。

## 5. 不要做的事

- 不要为了让红波浪线消失，把 `LogisticsApplication.java` 的包名改成 `main.java.com.logistics`。那是错的。
- 不要手动建 `.classpath` / `.project`。Maven 工程让 Java 扩展自动导入。
- 不要在 `settings.json` 里写 `"java.project.sourcePaths": ["src"]`。这就是报错 A 的来源。
- 不要把 `pom.xml` 删掉或把项目改成普通 Java 项目。Spring 依赖全靠它。

## 6. 一句话总结

- 包名错：打开 `logistics-network.code-workspace` 或 `logistics-server` 目录，再 `Java: Clean Java Language Server Workspace`。
- 依赖错：在 `logistics-server` 里跑 `.\mvnw.cmd -v`，再 `.\mvnw.cmd dependency:go-offline`，最后 `Maven: Reload Projects`。
