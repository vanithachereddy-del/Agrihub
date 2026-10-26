$ErrorActionPreference = 'Stop'

$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$backendDir = Join-Path $projectRoot 'agrihub-backend'
$cloudflared = Join-Path $projectRoot 'cloudflared\cloudflared.exe.exe'

if (-not (Test-Path $cloudflared)) {
    Write-Host "cloudflared was not found here:" -ForegroundColor Red
    Write-Host $cloudflared
    exit 1
}

Write-Host "Starting AgriHub backend on http://localhost:8080 ..." -ForegroundColor Cyan
Start-Process powershell -ArgumentList @(
    '-NoExit',
    '-ExecutionPolicy', 'Bypass',
    '-Command',
    "cd '$backendDir'; .\mvnw.cmd spring-boot:run"
)

Write-Host "Waiting a few seconds for Spring Boot to start..." -ForegroundColor Cyan
Start-Sleep -Seconds 10

Write-Host ""
Write-Host "Starting Cloudflare Tunnel for http://localhost:8080" -ForegroundColor Green
Write-Host "Copy the https://...trycloudflare.com link shown below." -ForegroundColor Yellow
Write-Host "That one link is for BOTH frontend pages and backend APIs." -ForegroundColor Yellow
Write-Host ""

& $cloudflared tunnel --url http://localhost:8080
