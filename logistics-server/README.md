# logistics-server

城市间物流网络联通规划系统的后端。Spring Boot + Maven。

## 环境

- JDK 25（本机已安装；`java -version` 确认）
- Maven 用项目自带的 `mvnw`，不需要全局安装 Maven
- 端口：8080

> 文档 v1.1 和本项目骨架统一用 JDK 25 + Spring Boot 4.1.2。如果课程或实验室要求 JDK 21，改 `pom.xml` 的 `<java.version>` 和 `<parent>` 版本即可，Java 代码不用动。

## 启动

```powershell
# Windows PowerShell
.\mvnw.cmd spring-boot:run
```

```bash
# Git Bash / macOS / Linux
./mvnw spring-boot:run
```

启动后访问 `http://localhost:8080`。目前只有启动类，接口还没写。

## 测试

```powershell
.\mvnw.cmd test
```

## 目录

```
src/main/java/com/logistics/
├── LogisticsApplication.java   # 启动类
├── controller/                 # 接收请求，不写算法
├── service/                    # 业务编排，唯一写入口
├── model/                      # City / Edge / User
├── dto/                        # 对外返回对象
├── structure/                  # 手写数据结构：CityNode、链表、图
├── algorithm/
│   ├── mst/                    # Prim、Kruskal、UnionFind
│   └── path/                   # Dijkstra
├── common/                     # Result、状态码、全局异常
├── config/                     # 编码、拦截器注册
├── auth/                       # 角色注解
├── exception/                  # 自定义异常
├── util/                       # DistanceUtil
├── cli/                        # ConsoleRunner，答辩兜底
└── benchmark/                  # 性能对比，不进生产接口

src/main/resources/
├── application.yml
└── data/cities.txt             # 30 城数据，UTF-8，3 列
```

## 数据文件

`src/main/resources/data/cities.txt` 由梁敬谦提供（D3），格式：

```
# name longitude latitude
北京 116.41 39.90
上海 121.47 31.23
```

- UTF-8，无 BOM
- 30 条数据
- id = 1000 + 有效数据行号

## 下一步

1. 把 `cities.txt` 放进来。
2. 先写 `structure/` 和 `algorithm/`，再写 `service/`，最后写 `controller/`。
3. 每写一个类，配一个测试。