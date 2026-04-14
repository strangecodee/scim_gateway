#!/usr/bin/env pwsh
# SCIM Gateway - Package for Server Deployment
# Creates a deployment package with JAR, .env, and scripts

param(
    [string]$OutputDir = "deploy-package",
    [switch]$SkipBuild
)

$ErrorActionPreference = "Stop"
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path

Write-Host "`n=========================================" -ForegroundColor Cyan
Write-Host "  SCIM Gateway - Server Package Builder" -ForegroundColor Cyan
Write-Host "=========================================" -ForegroundColor Cyan

# Step 1: Build JAR if needed
if (-not $SkipBuild) {
    Write-Host "`n[1/4] Building Application..." -ForegroundColor Yellow
    
    if (Test-Path "pom.xml") {
        Write-Host "  Running: mvn clean package -DskipTests" -ForegroundColor Gray
        mvn clean package -DskipTests | Out-Null
        
        if ($LASTEXITCODE -ne 0) {
            Write-Host "  [ERROR] Build failed!" -ForegroundColor Red
            exit 1
        }
        
        Write-Host "  [OK] Build successful" -ForegroundColor Green
    } else {
        Write-Host "  [WARN] pom.xml not found, skipping build" -ForegroundColor Yellow
    }
} else {
    Write-Host "`n[1/4] Skipping build (-SkipBuild specified)" -ForegroundColor Yellow
}

# Step 2: Find JAR file
Write-Host "`n[2/4] Finding JAR file..." -ForegroundColor Yellow

$jarFile = Get-ChildItem -Path "target" -Filter "scim-app-*.jar" -ErrorAction SilentlyContinue |
           Where-Object { $_.Name -notlike "*-sources.jar" -and $_.Name -notlike "*-javadoc.jar" } |
           Select-Object -First 1

if (-not $jarFile) {
    Write-Host "  [ERROR] No JAR file found in target directory!" -ForegroundColor Red
    Write-Host "  Build first or place JAR in target directory" -ForegroundColor Yellow
    exit 1
}

Write-Host "  [OK] Found: $($jarFile.Name)" -ForegroundColor Green
$jarSizeMB = [math]::Round($jarFile.Length / 1MB, 2)
Write-Host "       Size: $jarSizeMB MB" -ForegroundColor Gray

# Step 3: Create deployment package
Write-Host "`n[3/4] Creating deployment package..." -ForegroundColor Yellow

# Create output directory
if (Test-Path $OutputDir) {
    Write-Host "  Removing old package..." -ForegroundColor Gray
    Remove-Item $OutputDir -Recurse -Force
}

New-Item -ItemType Directory -Path $OutputDir -Force | Out-Null
Write-Host "  Created directory: $OutputDir" -ForegroundColor Gray

# Copy files
Write-Host "  Copying files..." -ForegroundColor Gray

# Copy JAR
Copy-Item $jarFile.FullName $OutputDir
Write-Host "    [OK] JAR file" -ForegroundColor Green

# Copy .env if exists
if (Test-Path ".env") {
    Copy-Item ".env" $OutputDir
    Write-Host "    [OK] .env file" -ForegroundColor Green
} else {
    if (Test-Path ".env.example") {
        Copy-Item ".env.example" "$OutputDir/.env"
        Write-Host "    [WARN] .env.example copied as .env (configure it!)" -ForegroundColor Yellow
    }
}

# Copy server scripts
$scripts = @("server-start.sh", "server-stop.sh", "server-status.sh")
foreach ($script in $scripts) {
    if (Test-Path $script) {
        Copy-Item $script $OutputDir
        Write-Host "    [OK] $script" -ForegroundColor Green
    }
}

# Copy deployment guide
if (Test-Path "SERVER-DEPLOYMENT-GUIDE.md") {
    Copy-Item "SERVER-DEPLOYMENT-GUIDE.md" $OutputDir
    Write-Host "    [OK] SERVER-DEPLOYMENT-GUIDE.md" -ForegroundColor Green
}

# Step 4: Create archive
Write-Host "`n[4/4] Creating archive..." -ForegroundColor Yellow

$archiveName = "scim-gateway-deploy-$(Get-Date -Format 'yyyyMMdd-HHmmss').zip"
Compress-Archive -Path "$OutputDir\*" -DestinationPath $archiveName -Force

$archiveSizeMB = [math]::Round((Get-Item $archiveName).Length / 1MB, 2)
Write-Host "  [OK] Archive created: $archiveName" -ForegroundColor Green
Write-Host "       Size: $archiveSizeMB MB" -ForegroundColor Gray

# Summary
Write-Host "`n=========================================" -ForegroundColor Cyan
Write-Host "  Package Ready!" -ForegroundColor Cyan
Write-Host "=========================================" -ForegroundColor Cyan

Write-Host "`nFiles in package:" -ForegroundColor Yellow
Get-ChildItem $OutputDir | ForEach-Object {
    if ($_.PSIsContainer) {
        Write-Host "  $($_.Name) (directory)" -ForegroundColor White
    } else {
        $sizeKB = [math]::Round($_.Length / 1KB, 1)
        Write-Host "  $($_.Name) ($sizeKB KB)" -ForegroundColor White
    }
}

Write-Host "`nArchive:" -ForegroundColor Yellow
Write-Host "  $archiveName ($archiveSizeMB MB)" -ForegroundColor Green

Write-Host "`nUpload Commands:" -ForegroundColor Yellow
Write-Host "  SCP: scp $archiveName user@your-server:/opt/" -ForegroundColor Cyan
Write-Host "  SSH: ssh user@your-server" -ForegroundColor Cyan
Write-Host "  Extract: unzip $archiveName -d /opt/scim-gateway" -ForegroundColor Cyan
Write-Host "  Deploy: cd /opt/scim-gateway && chmod +x *.sh && ./server-start.sh" -ForegroundColor Cyan

Write-Host "`n=========================================`n" -ForegroundColor Cyan
