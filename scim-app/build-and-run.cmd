@echo off
setlocal enabledelayedexpansion

echo =========================================
echo   SCIM Gateway - Build and Run
echo =========================================
echo.

cd /d "%~dp0"

echo Step 1: Checking Java...
java -version 2>&1
if errorlevel 1 (
    echo ERROR: Java not found! Please install Java 17+
    pause
    exit /b 1
)
echo.

echo Step 2: Building application with Maven...
call mvnw.cmd clean package -DskipTests
if errorlevel 1 (
    echo ERROR: Build failed!
    pause
    exit /b 1
)
echo.

echo Step 3: Loading environment variables from .env...
for /f "tokens=* delims=" %%i in (.env) do (
    set %%i
)

echo Profile: %SPRING_PROFILES_ACTIVE%
echo MongoDB URI: %MONGODB_URI%
echo.

echo Step 4: Starting application...
echo =========================================
echo.

java -jar target\scim-app-0.0.1-SNAPSHOT.jar ^
    --spring.profiles.active=%SPRING_PROFILES_ACTIVE% ^
    --spring.data.mongodb.uri=%MONGODB_URI% ^
    --jwt.secret=%JWT_SECRET% ^
    --auth.default.password=%SCIM_ADMIN_PASSWORD%

pause
