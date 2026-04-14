@echo off
echo =========================================
echo   Environment Diagnostic
echo =========================================
echo.

echo Current Directory:
cd
echo.

echo Java Version:
java -version 2>&1
echo.

echo Maven Check:
if exist "mvnw.cmd" (
    echo mvnw.cmd found
) else (
    echo mvnw.cmd NOT found
)
echo.

echo JAR File Check:
dir target\*.jar 2>&1
echo.

echo .env File Check:
if exist ".env" (
    echo .env found
    echo Contents (secrets hidden):
    findstr /v "SECRET PASSWORD" .env
) else (
    echo .env NOT found
)
echo.

echo =========================================
echo   Diagnostic Complete
echo =========================================
pause
