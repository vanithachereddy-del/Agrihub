@echo off
setlocal

start "AgriHub Backend" cmd /k ""%~dp0start-backend.bat""
timeout /t 8 /nobreak >nul
start "AgriHub Cloudflare Tunnel" cmd /k ""%~dp0start-cloudflared-tunnel.bat""
