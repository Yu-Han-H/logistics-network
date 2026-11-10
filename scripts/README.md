# scripts

启动、停止、测试和基准脚本。日常启动直接用 `start.cmd`。

## 一键启动

双击 [start.cmd](start.cmd)，或在 PowerShell 里执行：

```powershell
cd C:\Users\RAIN\logistics-network
.\scripts\start.cmd
```

它会：

1. 检查 `java`、`node`、`npm`；
2. 首次运行时执行 `npm ci` 安装前端依赖；
3. 在独立窗口启动后端（端口 8080）和前端（端口 5173）；
4. 等待两个端口就绪后打印访问地址。

可选参数：

```powershell
# 启动后自动打开浏览器
.\scripts\start.ps1 -OpenBrowser

# 跳过 npm 安装
.\scripts\start.ps1 -SkipInstall

# 只检查脚本路径和命令，不真正启动
.\scripts\start.ps1 -DryRun
```

## 一键停止

```powershell
.\scripts\stop.cmd
```

它按启动时记录的 PID 结束后端和前端进程树；PID 文件放在 `%TEMP%\logistics-network-run`，不写入仓库。

## 手动启动（备用）

```powershell
cd ..\logistics-server
.\mvnw.cmd spring-boot:run
```

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

```powershell
cd ..\logistics-web
npm run build
```

## 说明

- 第一次运行后端要下载 Maven 和依赖，需要网络，启动窗口会停留较久。
- 如果 `mvnw` 下载失败，装一个 Maven 3.10，把命令换成 `mvn spring-boot:run`。
- 前端 `npm install` 只需要跑一次。
- 端口被占用时，`start.ps1` 会跳过对应服务，不会强行杀掉已有进程。
