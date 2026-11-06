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

## 当前状态

- 数据结构与算法已完成：链表、图、Prim、Kruskal、Dijkstra，53 个测试全部通过（含性能对比、登录/权限、Spring 上下文）。
- 30 城 `cities.txt` 已就绪。
- 后端接口已实现并联调通过：登录、城市 CRUD、排序链表、邻接表、MST、最短路径、重置；401/403 已验证。
- 前端骨架已搭好：Vite、路由、登录页、5 个业务页占位、axios 封装；页面逻辑还没写。
- 已初始化 Git 仓库（`main` 分支）；当前开发在 `feature/hyh`。
- 已提供 `logistics-network.code-workspace`，用 VS Code 打开它，后端才会被识别为 Maven 工程。

## 版本说明

文档 v1.1 已按本机环境改为 JDK 25 + Spring Boot 4.1.x。如果课程要求 JDK 21，改 `logistics-server/pom.xml` 的 `<java.version>` 和 `<parent>` 版本，代码不用改。

## 下一步

1. 黄予函：写前端 6 个页面，直接接真实接口（`docs/api.md` 已和后端对齐）。
2. 后端收尾：提交 Auth 代码；把 `structure/Main.java`、`GraphCheck.java` 移到 `cli` 或 `test`。
3. 联调：按 `docs/api.md` 对一遍 401/403、增删城市后 MST/路径更新。
4. 预留 11-12 代码冻结；11-13~15 测试、报告、PPT、演练。
