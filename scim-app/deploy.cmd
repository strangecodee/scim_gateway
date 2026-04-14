@echo off
REM SCIM Gateway - Quick Deploy Wrapper
REM This allows running: deploy.cmd (from CMD)
REM It calls the PowerShell script with all parameters

echo.
echo =========================================
echo   SCIM Gateway - Quick Deploy
echo =========================================
echo.

REM Check if PowerShell is available
where powershell >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] PowerShell not found!
    pause
    exit /b 1
)

REM Forward all arguments to PowerShell script
set SCRIPT_DIR=%~dp0
powershell -ExecutionPolicy Bypass -File "%SCRIPT_DIR%deploy.ps1" %*

REM Capture exit code
set EXIT_CODE=%errorlevel%

if %EXIT_CODE% equ 0 (
    echo.
    echo [OK] Deployment completed successfully!
) else (
    echo.
    echo [ERROR] Deployment failed with code: %EXIT_CODE%
)

echo.
pause
exit /b %EXIT_CODE%
