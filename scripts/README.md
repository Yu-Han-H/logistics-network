# scripts

放启动、测试、基准脚本。当前先用手动命令，脚本后面补。

## 启动后端

```powershell
cd ..\logistics-server
.\mvnw.cmd spring-boot:run
```

## 启动前端

```powershell
cd ..\logistics-web
npm install
npm run dev
```

## 跑测试

```powershell
cd ..\logistics-server
.\mvnw.cmd test
```

## 说明

- 第一次运行后端要下载 Maven 和依赖，需要网络。
- 如果 `mvnw` 下载失败，装一个 Maven 3.10，把命令换成 `mvn spring-boot:run`。
- 前端 `npm install` 只需要跑一次。
