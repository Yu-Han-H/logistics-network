[CmdletBinding()]
param(
    [switch]$SkipInstall,
    [switch]$OpenBrowser,
    [switch]$DryRun,
    [int]$BackendPort = 8080,
    [int]$FrontendPort = 5173
)

$ErrorActionPreference = 'Stop'

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$root = Split-Path -Parent $scriptDir
$serverDir = Join-Path $root 'logistics-server'
$webDir = Join-Path $root 'logistics-web'
$runDir = Join-Path $env:TEMP 'logistics-network-run'
$backendPidFile = Join-Path $runDir 'backend.pid'
$frontendPidFile = Join-Path $runDir 'frontend.pid'

function Test-PortInUse {
    param([int]$Port)
    $connection = Get-NetTCPConnection -State Listen -LocalPort $Port -ErrorAction SilentlyContinue
    return [bool]$connection
}

function Wait-Port {
    param([int]$Port, [int]$TimeoutSeconds = 60)
    $deadline = (Get-Date).AddSeconds($TimeoutSeconds)
    while ((Get-Date) -lt $deadline) {
        if (Test-PortInUse -Port $Port) {
            return $true
        }
        Start-Sleep -Milliseconds 500
    }
    return $false
}

Write-Host '=== 城市间物流网络联通规划系统 · 一键启动 ===' -ForegroundColor Cyan

foreach ($command in @('java', 'node', 'npm')) {
    if (-not (Get-Command $command -ErrorAction SilentlyContinue)) {
        throw "找不到命令：$command。请先安装 JDK 25 / Node 24，并确认已加入 PATH。"
    }
}

if (-not (Test-Path -LiteralPath (Join-Path $serverDir 'mvnw.cmd'))) {
    throw "找不到 Maven Wrapper：$serverDir\mvnw.cmd"
}

if (-not (Test-Path -LiteralPath (Join-Path $webDir 'package.json'))) {
    throw "找不到前端工程：$webDir\package.json"
}

if ($DryRun) {
    Write-Host "[DryRun] 后端目录：$serverDir" -ForegroundColor Yellow
    Write-Host "[DryRun] 前端目录：$webDir" -ForegroundColor Yellow
    Write-Host "[DryRun] 后端命令：mvnw.cmd spring-boot:run（端口 $BackendPort）" -ForegroundColor Yellow
    Write-Host "[DryRun] 前端命令：npm run dev -- --host 127.0.0.1 --port $FrontendPort" -ForegroundColor Yellow
    exit 0
}

New-Item -ItemType Directory -Force -Path $runDir | Out-Null

if (-not $SkipInstall -and -not (Test-Path -LiteralPath (Join-Path $webDir 'node_modules'))) {
    Write-Host '首次运行，安装前端依赖...' -ForegroundColor Yellow
    Push-Location $webDir
    try {
        if (Test-Path -LiteralPath (Join-Path $webDir 'package-lock.json')) {
            npm ci
        } else {
            npm install
        }
        if ($LASTEXITCODE -ne 0) {
            throw '前端依赖安装失败。'
        }
    } finally {
        Pop-Location
    }
}

$backendStarted = $false
$frontendStarted = $false

if (Test-PortInUse -Port $BackendPort) {
    Write-Host "端口 $BackendPort 已被占用，跳过后端启动。" -ForegroundColor Yellow
} else {
    Remove-Item -LiteralPath $backendPidFile -Force -ErrorAction SilentlyContinue
    $backendCommand = "`"`"$serverDir\mvnw.cmd`" spring-boot:run`""
    $backend = Start-Process -FilePath 'cmd.exe' `
        -ArgumentList '/k', $backendCommand `
        -WorkingDirectory $serverDir `
        -PassThru
    $backend.Id | Set-Content -LiteralPath $backendPidFile -Encoding ASCII
    $backendStarted = $true
    Write-Host "后端已启动（PID $($backend.Id)），端口 $BackendPort。" -ForegroundColor Green
}

if (Test-PortInUse -Port $FrontendPort) {
    Write-Host "端口 $FrontendPort 已被占用，跳过前端启动。" -ForegroundColor Yellow
} else {
    Remove-Item -LiteralPath $frontendPidFile -Force -ErrorAction SilentlyContinue
    $frontendCommand = "npm run dev -- --host 127.0.0.1 --port $FrontendPort"
    $frontend = Start-Process -FilePath 'cmd.exe' `
        -ArgumentList '/k', $frontendCommand `
        -WorkingDirectory $webDir `
        -PassThru
    $frontend.Id | Set-Content -LiteralPath $frontendPidFile -Encoding ASCII
    $frontendStarted = $true
    Write-Host "前端已启动（PID $($frontend.Id)），端口 $FrontendPort。" -ForegroundColor Green
}

if ($backendStarted) {
    Write-Host '等待后端端口就绪（首次运行要下载 Maven 依赖，可能较慢）...' -ForegroundColor Yellow
    if (-not (Wait-Port -Port $BackendPort -TimeoutSeconds 180)) {
        Write-Warning "后端 180 秒内未监听端口 $BackendPort，请查看后端窗口日志。"
    }
}

if ($frontendStarted) {
    Write-Host '等待前端端口就绪...' -ForegroundColor Yellow
    if (-not (Wait-Port -Port $FrontendPort -TimeoutSeconds 60)) {
        Write-Warning "前端 60 秒内未监听端口 $FrontendPort，请查看前端窗口日志。"
    }
}

Write-Host ''
Write-Host '启动完成：' -ForegroundColor Cyan
Write-Host "  前端：http://localhost:$FrontendPort" -ForegroundColor White
Write-Host "  后端：http://localhost:$BackendPort" -ForegroundColor White
Write-Host "  停止：$scriptDir\stop.cmd" -ForegroundColor White
Write-Host ''

if ($OpenBrowser) {
    Start-Process "http://localhost:$FrontendPort"
}
