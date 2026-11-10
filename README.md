# 城市间物流网络联通规划系统

数据结构综合性实践（第二组）：双向循环链表 + 邻接表 + Prim/Kruskal + Dijkstra。

## 目录

```
logistics-network/
├── docs/                     # 文档
│   ├── 开发文档-v1.1.md       # 当前开发文档
│   ├── 开发文档.md            # v1.0，保留对比
│   └── 项目结构说明.md        # 本项目的结构、分层、数据流
├── logistics-server/         # 后端：Spring Boot + Maven
├── logistics-web/            # 前端：Vue 3 + Vite
├── scripts/                  # 启动、测试、基准脚本
├── _archive/                 # 旧脚手架，不提交（.gitignore 已忽略）
├── .editorconfig
├── .gitattributes
├── .gitignore
└── README.md
```

## 快速开始

一键启动前后端：

```powershell
cd logistics-network
.\scripts\start.cmd
```

停止：

```powershell
.\scripts\stop.cmd
```

也可以手动分开启动：

### 后端

```powershell
cd logistics-server
.\mvnw.cmd spring-boot:run
```

端口 8080。需要 JDK 25。

### 前端

```powershell
cd logistics-web
npm install
npm run dev
```

端口 5173，`/api` 会代理到 `http://localhost:8080`。

> VS Code 报“包名不匹配”或“Spring 包找不到”，先看 [环境与 IDE](docs/环境与IDE.md)。

## 文档

- [开发文档 v1.1](docs/开发文档-v1.1.md)：需求、排期、核心设计、接口、测试。
- [接口与分层约定](docs/接口与分层约定.md)：当前分工、Service 签名、DTO、异常和分支规则。
- [公共层说明](docs/公共层说明.md)：`Result`、`FieldError`、异常处理、认证骨架怎么读。
- [项目结构说明](docs/项目结构说明.md)：每个目录放什么、依赖方向、数据流。
- [API 契约](docs/api.md)：接口清单、字段、示例。
- [环境与 IDE](docs/环境与IDE.md)：VS Code 报错、Maven 依赖安装。
- [开发文档 v1.0](docs/开发文档.md)：旧版，保留对比。
- [开发文档审阅意见](docs/开发文档审阅意见.md)：v1.1 的修改依据。
- [作业要求核对](docs/report/作业要求核对.md)：按课程 PDF 逐项核对完成情况。
- [成员代码量统计](docs/report/成员代码量统计.md)：按 Git 提交统计三人功能/测试代码量。

## 当前状态

- 数据结构与算法已完成：链表、图、Prim、Kruskal、Dijkstra，53 个 JUnit 测试全部通过；另有 ServiceCheck 41 项、GraphCheck 29 项控制台自检。
- 30 城 `cities.txt` 已就绪。
- 后端接口已实现并联调通过：登录、城市 CRUD、排序链表、邻接表、MST、最短路径、重置；401/403 已验证。
- 前端页面已完成：登录、城市管理、链表视图、邻接表、MST、最短路径、404；接真实接口，G6 图按经纬度投影。
- 已初始化 Git 仓库（`main` 分支）；当前开发在 `feature/hyh`。
- 已提供 `logistics-network.code-workspace`，用 VS Code 打开它，后端才会被识别为 Maven 工程。

## 版本说明

文档 v1.1 已按本机环境改为 JDK 25 + Spring Boot 4.1.x。如果课程要求 JDK 21，改 `logistics-server/pom.xml` 的 `<java.version>` 和 `<parent>` 版本，代码不用改。

## 下一步

1. 联调：按 `docs/api.md` 对一遍 401/403、增删城市后 MST/路径更新。
2. 补 `cli/ConsoleRunner.java` 控制台四类输出，作为答辩兜底。
3. 预留 11-12 代码冻结；11-13~15 测试、报告、PPT、演练。
