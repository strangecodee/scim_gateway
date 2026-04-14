#!/usr/bin/env pwsh
<#
.SYNOPSIS
    SCIM Gateway - Automated Deployment Script
    
.DESCRIPTION
    Automatically fetches updated code from Git repository, configures environment variables,
    builds the application, and starts it with production settings.
    
.PARAMETER Environment
    Deployment environment: dev, staging, or prod (default: prod)
    
.PARAMETER Branch
    Git branch to deploy (default: main)
    
.PARAMETER AutoRestart
    Automatically restart if already running (default: $true)
    
.PARAMETER SkipTests
    Skip tests during build (default: $true)
    
.EXAMPLE
    .\deploy.ps1
    .\deploy.ps1 -Environment prod -Branch main
    .\deploy.ps1 -Environment dev -Branch develop -SkipTests:$false
    
.AUTHOR
    SCIM Gateway Deployment Team
#>

param(
    [ValidateSet("dev", "staging", "prod")]
    [string]$Environment = "prod",
    
    [string]$Branch = "main",
    
    [string]$AutoRestart = "true",
    
    [string]$SkipTests = "true"
)

# ==========================================
# Configuration
# ==========================================
$ErrorActionPreference = "Stop"
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$LogDir = Join-Path $ScriptDir "logs"
$LogFile = Join-Path $LogDir "deploy-$(Get-Date -Format 'yyyyMMdd-HHmmss').log"
$JarPattern = "target/scim-app-*.jar"
$AppName = "scim-app"

# Colors for output
$ColorSuccess = "Green"
$ColorError = "Red"
$ColorWarning = "Yellow"
$ColorInfo = "Cyan"
$ColorDetail = "White"

# ==========================================
# Helper Functions
# ==========================================

function Write-Log {
    param(
        [string]$Message,
        [string]$Color = $ColorDetail,
        [switch]$NoNewLine
    )
    
    $timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    $logMessage = "[$timestamp] $Message"
    
    # Write to console
    if ($NoNewLine) {
        Write-Host $logMessage -ForegroundColor $Color -NoNewline
    } else {
        Write-Host $logMessage -ForegroundColor $Color
    }
    
    # Write to log file
    if (!(Test-Path $LogDir)) {
        New-Item -ItemType Directory -Path $LogDir -Force | Out-Null
    }
    Add-Content -Path $LogFile -Value $logMessage
}

function Write-Step {
    param([string]$Step, [int]$Current, [int]$Total)
    Write-Log "`n=========================================" -ColorInfo
    Write-Log "  Step $Current/$Total : $Step" -ColorInfo
    Write-Log "=========================================" -ColorInfo
}

function Test-Command {
    param([string]$Command)
    
    try {
        $null = Get-Command $Command -ErrorAction Stop
        return $true
    } catch {
        return $false
    }
}

function Get-RunningPid {
    $processes = Get-Process -Name "java" -ErrorAction SilentlyContinue | Where-Object {
        $_.CommandLine -like "*$AppName*" -or $_.CommandLine -like "*scim-app*"
    }
    
    if ($processes) {
        return $processes.Id
    }
    return $null
}

function Stop-RunningApp {
    $pid = Get-RunningPid
    
    if ($pid) {
        Write-Log "  Found running instance (PID: $pid)" -ColorWarning
        Write-Log "  Stopping application..." -ColorInfo
        
        try {
            # Try graceful shutdown first
            Invoke-RestMethod -Uri "http://localhost:8080/actuator/shutdown" -Method POST -ErrorAction SilentlyContinue | Out-Null
            Start-Sleep -Seconds 5
        } catch {
            # Force kill if graceful shutdown fails
            Stop-Process -Id $pid -Force
            Start-Sleep -Seconds 3
        }
        
        # Verify it's stopped
        $newPid = Get-RunningPid
        if ($newPid) {
            Write-Log "  Force stopping..." -ColorWarning
            Stop-Process -Id $newPid -Force
            Start-Sleep -Seconds 2
        }
        
        Write-Log "  Application stopped" -ColorSuccess
        return $true
    }
    
    return $false
}

function Test-Port {
    param([int]$Port = 8080)
    
    try {
        $tcpClient = New-Object System.Net.Sockets.TcpClient
        $result = $tcpClient.BeginConnect("localhost", $Port, $null, $null)
        $wait = $result.AsyncWaitHandle.WaitOne(1000)
        
        if ($wait) {
            $tcpClient.EndConnect($result) | Out-Null
            $tcpClient.Close()
            return $true
        }
        return $false
    } catch {
        return $false
    }
}

# ==========================================
# Deployment Steps
# ==========================================

Write-Log "`n" 
Write-Log "=========================================" -ColorInfo
Write-Log "  SCIM Gateway - Automated Deployment" -ColorInfo
Write-Log "=========================================" -ColorInfo
Write-Log "Environment : $Environment" -ColorDetail
Write-Log "Branch      : $Branch" -ColorDetail
Write-Log "Auto Restart: $AutoRestart" -ColorDetail
Write-Log "Skip Tests  : $SkipTests" -ColorDetail

# Step 1: Prerequisites Check
Write-Step "Checking Prerequisites" 1 7

$prerequisites = @(
    @{Name="Git"; Command="git"},
    @{Name="Maven"; Command="mvn"},
    @{Name="Java"; Command="java"}
)

$allOk = $true
foreach ($req in $prerequisites) {
    if (Test-Command $req.Command) {
        $version = & $req.Command --version 2>&1 | Select-Object -First 1
        Write-Log "  [OK] $($req.Name)" -ColorSuccess
        Write-Log "       $version" -ColorDetail
    } else {
        Write-Log "  [FAIL] $($req.Name) - Not found!" -ColorError
        $allOk = $false
    }
}

if (-not $allOk) {
    Write-Log "`n  Please install missing prerequisites and try again." -ColorError
    exit 1
}

# Step 2: Fetch Latest Code
Write-Step "Fetching Latest Code from Git" 2 7

Write-Log "  Current branch: $(git branch --show-current)" -ColorInfo
Write-Log "  Target branch : $Branch" -ColorInfo

try {
    # Stash any local changes
    $hasChanges = git status --porcelain
    if ($hasChanges) {
        Write-Log "  Stashing local changes..." -ColorWarning
        git stash push -m "Auto-stash before deployment" | Out-Null
    }
    
    # Fetch and pull
    Write-Log "  Fetching from remote..." -ColorInfo
    git fetch origin | Out-Null
    
    Write-Log "  Checking out branch: $Branch" -ColorInfo
    git checkout $Branch | Out-Null
    
    Write-Log "  Pulling latest changes..." -ColorInfo
    $pullResult = git pull origin $Branch 2>&1
    
    if ($LASTEXITCODE -eq 0) {
        if ($pullResult -like "*Already up to date*") {
            Write-Log "  Already up to date" -ColorSuccess
        } else {
            Write-Log "  Code updated successfully" -ColorSuccess
            Write-Log "  Changes:" -ColorInfo
            git log --oneline HEAD@{1}..HEAD | ForEach-Object {
                Write-Log "    $_" -ColorDetail
            }
        }
    } else {
        Write-Log "  Pull failed: $pullResult" -ColorError
        exit 1
    }
} catch {
    Write-Log "  Git operation failed: $_" -ColorError
    exit 1
}

# Step 3: Configure Environment Variables
Write-Step "Configuring Environment Variables" 3 7

# Check if .env file exists
$envFile = Join-Path $ScriptDir ".env"
if (Test-Path $envFile) {
    Write-Log "  Loading environment variables from .env file" -ColorSuccess
    
    # Load .env file
    Get-Content $envFile | ForEach-Object {
        if ($_ -match '^([^#][^=]+)=(.+)$') {
            $key = $matches[1].Trim()
            $value = $matches[2].Trim()
            [Environment]::SetEnvironmentVariable($key, $value, "Process")
            Write-Log "  Set: $key" -ColorDetail
        }
    }
} else {
    Write-Log "  .env file not found, using defaults" -ColorWarning
    Write-Log "  Create .env file with your production secrets!" -ColorWarning
}

# Set environment from parameters
[Environment]::SetEnvironmentVariable("SPRING_PROFILES_ACTIVE", $Environment, "Process")
Write-Log "  Profile: $Environment" -ColorSuccess

# Verify critical variables
$criticalVars = @("JWT_SECRET", "SCIM_ADMIN_PASSWORD", "MONGODB_URI")
$missingVars = @()

foreach ($var in $criticalVars) {
    $value = [Environment]::GetEnvironmentVariable($var, "Process")
    if ($value -and $value -notlike "*CHANGE*") {
        Write-Log "  [OK] $var is set" -ColorSuccess
    } else {
        Write-Log "  [WARN] $var not set or using default" -ColorWarning
        $missingVars += $var
    }
}

if ($missingVars.Count -gt 0) {
    Write-Log "`n  Missing critical environment variables:" -ColorError
    $missingVars | ForEach-Object { Write-Log "    - $_" -ColorError }
    Write-Log "`n  Create .env file or set them manually!" -ColorWarning
    
    $continue = Read-Host "`n  Continue anyway? (y/n)"
    if ($continue -ne "y") {
        exit 1
    }
}

# Step 4: Stop Running Application
Write-Step "Stopping Running Application" 4 7

if ($AutoRestart -eq "true" -or $AutoRestart -eq "1") {
    $wasRunning = Stop-RunningApp
    if (-not $wasRunning) {
        Write-Log "  No running instance found" -ColorInfo
    }
} else {
    $pid = Get-RunningPid
    if ($pid) {
        Write-Log "  Application is running (PID: $pid) and AutoRestart is disabled" -ColorError
        Write-Log "  Stop it manually or use -AutoRestart `$true" -ColorError
        exit 1
    } else {
        Write-Log "  No running instance found" -ColorSuccess
    }
}

# Step 5: Build Application
Write-Step "Building Application" 5 7

$buildCmd = "mvn clean package"
if ($SkipTests -eq "true" -or $SkipTests -eq "1") {
    $buildCmd += " -DskipTests"
}

Write-Log "  Command: $buildCmd" -ColorInfo
Write-Log "  This may take a few minutes..." -ColorInfo

try {
    $buildOutput = & mvn clean package $(if($SkipTests -eq "true" -or $SkipTests -eq "1"){"-DskipTests"}) 2>&1 | Out-String
    
    if ($LASTEXITCODE -eq 0) {
        Write-Log "  Build successful!" -ColorSuccess
        
        # Find JAR file
        $jarFile = Get-ChildItem -Path "target" -Filter "scim-app-*.jar" | 
                   Where-Object { $_.Name -notlike "*-sources.jar" -and $_.Name -notlike "*-javadoc.jar" } |
                   Select-Object -First 1
        
        if ($jarFile) {
            $jarSize = [math]::Round($jarFile.Length / 1MB, 2)
            Write-Log "  JAR: $($jarFile.Name) ($jarSize MB)" -ColorSuccess
            $global:JarPath = $jarFile.FullName
        }
    } else {
        Write-Log "  Build failed!" -ColorError
        Write-Log $buildOutput -ColorError
        exit 1
    }
} catch {
    Write-Log "  Build error: $_" -ColorError
    exit 1
}

# Step 6: Verify Configuration
Write-Step "Verifying Configuration" 6 7

Write-Log "  Checking application properties..." -ColorInfo

$configChecks = @(
    @{File="src/main/resources/application.properties"; Name="Main config"},
    @{File="src/main/resources/application-$Environment.properties"; Name="$Environment config"}
)

foreach ($check in $configChecks) {
    $filePath = Join-Path $ScriptDir $check.File
    if (Test-Path $filePath) {
        Write-Log "  [OK] $($check.Name) exists" -ColorSuccess
    } else {
        Write-Log "  [WARN] $($check.Name) not found" -ColorWarning
    }
}

# Step 7: Start Application
Write-Step "Starting Application" 7 7

Write-Log "  Profile    : $Environment" -ColorInfo
Write-Log "  JAR File   : $global:JarPath" -ColorInfo
Write-Log "  Log File   : $LogFile" -ColorInfo

try {
    # Start application in background
    $startInfo = New-Object System.Diagnostics.ProcessStartInfo
    $startInfo.FileName = "java"
    $startInfo.Arguments = "-jar `"$global:JarPath`" --spring.profiles.active=$Environment"
    $startInfo.WorkingDirectory = $ScriptDir
    $startInfo.UseShellExecute = $false
    $startInfo.RedirectStandardOutput = $true
    $startInfo.RedirectStandardError = $true
    
    $process = [System.Diagnostics.Process]::Start($startInfo)
    $process.PriorityClass = [System.Diagnostics.ProcessPriorityClass]::Normal
    
    Write-Log "  Application started (PID: $($process.Id))" -ColorSuccess
    
    # Wait for application to be ready
    Write-Log "`n  Waiting for application to start..." -ColorInfo
    $maxWait = 60
    $waited = 0
    $ready = $false
    
    while ($waited -lt $maxWait) {
        Start-Sleep -Seconds 2
        $waited += 2
        
        if (Test-Port -Port 8080) {
            $ready = $true
            break
        }
        
        Write-Log "    Waiting... ($waited/$maxWait seconds)" -ColorInfo -NoNewLine
        Write-Log "`r" -NoNewLine
    }
    
    if ($ready) {
        Write-Log "`n  Application is ready!" -ColorSuccess
        Write-Log "  URL: http://localhost:8080" -ColorDetail
        Write-Log "  Swagger: http://localhost:8080/swagger-ui.html" -ColorDetail
        Write-Log "  Health: http://localhost:8080/actuator/health" -ColorDetail
        
        # Quick health check
        try {
            $health = Invoke-RestMethod -Uri "http://localhost:8080/actuator/health" -UseBasicParsing
            Write-Log "  Status: $($health.status)" -ColorSuccess
        } catch {
            Write-Log "  Health check failed (may need a moment)" -ColorWarning
        }
    } else {
        Write-Log "`n  Application may still be starting..." -ColorWarning
        Write-Log "  Check logs: $LogFile" -ColorInfo
    }
    
} catch {
    Write-Log "  Failed to start application: $_" -ColorError
    exit 1
}

# ==========================================
# Deployment Summary
# ==========================================
Write-Log "`n=========================================" -ColorInfo
Write-Log "  Deployment Complete!" -ColorInfo
Write-Log "=========================================" -ColorInfo
Write-Log "Environment : $Environment" -ColorDetail
Write-Log "Branch      : $Branch" -ColorDetail
Write-Log "Profile     : $Environment" -ColorDetail
Write-Log "PID         : $($process.Id)" -ColorDetail
Write-Log "URL         : http://localhost:8080" -ColorDetail
Write-Log "Swagger     : http://localhost:8080/swagger-ui.html" -ColorDetail
Write-Log "Log File    : $LogFile" -ColorDetail
Write-Log "=========================================" -ColorInfo

Write-Log "`nQuick Commands:" -ColorInfo
Write-Log "  View logs   : Get-Content $LogFile -Tail 50 -Wait" -ColorDetail
Write-Log "  Stop app    : Stop-Process -Id $($process.Id)" -ColorDetail
Write-Log "  Test APIs   : .\test-comprehensive.ps1" -ColorDetail
Write-Log "  Check health: curl http://localhost:8080/actuator/health" -ColorDetail

Write-Log "`n" 
Write-Log "Deployment successful! Application is running." -ColorSuccess
Write-Log ""
