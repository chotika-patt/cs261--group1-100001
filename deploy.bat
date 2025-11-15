@echo off
echo ==========================================
echo 1) Building Maven project (skip tests)
echo ==========================================
call mvn clean package -DskipTests
if %errorlevel% neq 0 (
    echo  Maven build failed!
    pause
    exit /b %errorlevel%
)

echo.
echo ==========================================
echo 2) Stopping old Docker containers
echo ==========================================
call docker-compose down
if %errorlevel% neq 0 (
    echo  Warning: Docker down failed or no containers running
)

echo.
echo ==========================================
echo 3) Building Docker images (no cache)
echo ==========================================
call docker-compose build --no-cache
if %errorlevel% neq 0 (
    echo  Docker build failed!
    pause
    exit /b %errorlevel%
)

echo.
echo ==========================================
echo 4) Starting Docker containers
echo ==========================================
call docker-compose up -d
if %errorlevel% neq 0 (
    echo  Docker up failed!
    pause
    exit /b %errorlevel%
)

echo.
echo ==========================================
echo Deployment Complete!
echo ==========================================
pause
