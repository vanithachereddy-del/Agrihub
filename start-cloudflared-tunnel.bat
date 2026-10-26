@echo off
setlocal
set "CLOUDFLARED=%~dp0cloudflared\cloudflared.exe.exe"

if not exist "%CLOUDFLARED%" (
  echo cloudflared was not found at "%CLOUDFLARED%".
  echo Put cloudflared.exe in the cloudflared folder or update this script.
  exit /b 1
)

echo Starting a public Cloudflare Tunnel for AgriHub.
echo Make sure start-backend.bat is already running.
echo.
"%CLOUDFLARED%" tunnel --url http://localhost:8080
