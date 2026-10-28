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

## 文档

- [开发文档 v1.1](docs/开发文档-v1.1.md)：需求、排期、核心设计、接口、测试。
- [项目结构说明](docs/项目结构说明.md)：每个目录放什么、依赖方向、数据流。
- [API 契约](docs/api.md)：接口清单和示例。
- [开发文档 v1.0](docs/开发文档.md)：旧版，保留对比。

## 当前状态

- 后端骨架已搭好：Maven Wrapper、启动类、包结构、`application.yml`。
- 前端骨架已搭好：Vite、路由、登录页、5 个业务页占位、axios 封装。
- `demo/` 已移到 `_archive/spring-initializr-demo/`，旧 VS Code Java 模板已移到 `_archive/logistics-server-vscode-template/`。
- 还没有 Git 仓库。建议在根目录执行 `git init`，首次提交前确认 `.gitignore` 生效。

## 版本说明

文档 v1.1 写的是 JDK 21 + Spring Boot 3.2.x；本机实际是 JDK 25 + Spring Boot 4.1.2，骨架按本机版本生成。如果课程要求 JDK 21，改 `logistics-server/pom.xml` 的 `<java.version>` 和 `<parent>` 版本，代码不用改。

## 下一步

1. 梁敬谦把 30 城 `cities.txt` 放进 `logistics-server/src/main/resources/data/`。
2. 后端先写 `structure/` 和 `algorithm/`，再写 `service/`，最后写 `controller/`。
3. 前端按 `docs/api.md` 的契约做 mock，不要等后端。
4. 每天同步一次，接口变了先改文档。
