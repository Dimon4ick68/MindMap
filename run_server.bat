@echo off
echo Starting MindApp Server (Port 8081)...
echo ---------------------------------------
cd server
call mvnw spring-boot:run
pause