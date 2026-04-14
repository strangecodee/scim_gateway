@echo off
echo =========================================
echo   SCIM Gateway - Starting Application
echo =========================================
echo.

cd /d "%~dp0"

echo Loading environment variables from .env...
for /f "tokens=* delims=" %%i in (.env) do (
    set %%i
)

echo.
echo Profile: %SPRING_PROFILES_ACTIVE%
echo MongoDB: %MONGODB_URI%
echo.

echo Starting application...
java -jar target\scim-app-0.0.1-SNAPSHOT.jar ^
    --spring.profiles.active=%SPRING_PROFILES_ACTIVE% ^
    --spring.data.mongodb.uri=%MONGODB_URI% ^
    --jwt.secret=%JWT_SECRET% ^
    --auth.default.password=%SCIM_ADMIN_PASSWORD%

pause
