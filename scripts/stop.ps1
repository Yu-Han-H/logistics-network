[CmdletBinding()]
param()

$ErrorActionPreference = 'Stop'

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$runDir = Join-Path $env:TEMP 'logistics-network-run'
$backendPidFile = Join-Path $runDir 'backend.pid'
$frontendPidFile = Join-Path $runDir 'frontend.pid'

function Stop-ProcessTree {
    param([string]$Name, [string]$PidFile)

    if (-not (Test-Path -LiteralPath $PidFile)) {
        Write-Host "$Name：没有找到 PID 文件。" -ForegroundColor Yellow
        return
    }

    $processId = (Get-Content -LiteralPath $PidFile -Raw).Trim()
    if (-not $processId) {
        Remove-Item -LiteralPath $PidFile -Force -ErrorAction SilentlyContinue
        return
    }

    $process = Get-Process -Id $processId -ErrorAction SilentlyContinue
    if ($process) {
        Write-Host "停止 $Name（PID $processId）..." -ForegroundColor Yellow
        & taskkill.exe /PID $processId /T /F | Out-Null
    } else {
        Write-Host "$Name：进程 $processId 已不存在。" -ForegroundColor Yellow
    }

    Remove-Item -LiteralPath $PidFile -Force -ErrorAction SilentlyContinue
}

Write-Host '=== 停止物流网络系统 ===' -ForegroundColor Cyan
Stop-ProcessTree -Name '后端' -PidFile $backendPidFile
Stop-ProcessTree -Name '前端' -PidFile $frontendPidFile
Write-Host '已尝试停止前后端进程。' -ForegroundColor Green
