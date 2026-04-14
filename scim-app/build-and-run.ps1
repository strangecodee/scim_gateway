# SCIM Gateway - Build and Run
Write-Host "=========================================" -ForegroundColor Cyan
Write-Host "  SCIM Gateway - Build and Run" -ForegroundColor Cyan
Write-Host "=========================================" -ForegroundColor Cyan
Write-Host ""

Set-Location $PSScriptRoot

# Step 1: Check Java
Write-Host "Step 1: Checking Java..." -ForegroundColor Yellow
try {
    $javaVersion = & java -version 2>&1
    Write-Host $javaVersion -ForegroundColor Green
} catch {
    Write-Host "ERROR: Java not found! Please install Java 17+" -ForegroundColor Red
    Read-Host "Press Enter to exit"
    exit 1
}
Write-Host ""

# Step 2: Build with Maven
Write-Host "Step 2: Building application with Maven..." -ForegroundColor Yellow
& .\mvnw.cmd clean package -DskipTests
if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR: Build failed!" -ForegroundColor Red
    Read-Host "Press Enter to exit"
    exit 1
}
Write-Host ""

# Step 3: Load environment variables
Write-Host "Step 3: Loading environment variables..." -ForegroundColor Yellow
$envFile = Get-Content ".env" | Where-Object { $_ -notmatch '^\s*#' -and $_ -match '=' }
foreach ($line in $envFile) {
    $name, $value = $line.Split('=', 2)
    [Environment]::SetEnvironmentVariable($name, $value, "Process")
}

Write-Host "Profile: $env:SPRING_PROFILES_ACTIVE" -ForegroundColor Green
Write-Host "MongoDB: $env:MONGODB_URI" -ForegroundColor Green
Write-Host ""

# Step 4: Run application
Write-Host "Step 4: Starting application..." -ForegroundColor Yellow
Write-Host "=========================================" -ForegroundColor Cyan
Write-Host ""

& java -jar "target\scim-app-0.0.1-SNAPSHOT.jar" `
    --spring.profiles.active="$env:SPRING_PROFILES_ACTIVE" `
    --spring.data.mongodb.uri="$env:MONGODB_URI" `
    --jwt.secret="$env:JWT_SECRET" `
    --auth.default.password="$env:SCIM_ADMIN_PASSWORD"
