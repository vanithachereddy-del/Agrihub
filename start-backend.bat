@echo off
setlocal
cd /d "%~dp0agrihub-backend"
call mvnw.cmd spring-boot:run
