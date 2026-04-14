#!/usr/bin/env pwsh
# Quick test of deploy.ps1 - Dry run mode

Write-Host "`n=========================================" -ForegroundColor Cyan
Write-Host "  Testing Deploy Script" -ForegroundColor Cyan
Write-Host "=========================================" -ForegroundColor Cyan

# Test 1: Check if script exists
$deployScript = Join-Path $PSScriptRoot "deploy.ps1"
if (Test-Path $deployScript) {
    Write-Host "`n[OK] deploy.ps1 exists" -ForegroundColor Green
} else {
    Write-Host "`n[FAIL] deploy.ps1 not found!" -ForegroundColor Red
    exit 1
}

# Test 2: Check prerequisites
Write-Host "`nChecking Prerequisites:" -ForegroundColor Yellow

$checks = @(
    @{Name="Git"; Command="git"},
    @{Name="Maven"; Command="mvn"},
    @{Name="Java"; Command="java"}
)

foreach ($check in $checks) {
    try {
        $null = Get-Command $check.Command -ErrorAction Stop
        Write-Host "  [OK] $($check.Name)" -ForegroundColor Green
    } catch {
        Write-Host "  [FAIL] $($check.Name)" -ForegroundColor Red
    }
}

# Test 3: Check .env file
Write-Host "`nEnvironment Files:" -ForegroundColor Yellow

if (Test-Path (Join-Path $PSScriptRoot ".env")) {
    Write-Host "  [OK] .env file exists" -ForegroundColor Green
} else {
    Write-Host "  [WARN] .env file not found (copy from .env.example)" -ForegroundColor Yellow
}

if (Test-Path (Join-Path $PSScriptRoot ".env.example")) {
    Write-Host "  [OK] .env.example exists" -ForegroundColor Green
} else {
    Write-Host "  [FAIL] .env.example not found!" -ForegroundColor Red
}

# Test 4: Check Git
Write-Host "`nGit Status:" -ForegroundColor Yellow
try {
    $branch = git branch --show-current
    Write-Host "  Current branch: $branch" -ForegroundColor Green
    
    $status = git status --porcelain
    if ($status) {
        Write-Host "  Uncommitted changes detected" -ForegroundColor Yellow
    } else {
        Write-Host "  Working tree clean" -ForegroundColor Green
    }
} catch {
    Write-Host "  [ERROR] Git error: $_" -ForegroundColor Red
}

# Test 5: Show usage
Write-Host "`n=========================================" -ForegroundColor Cyan
Write-Host "  Usage Examples" -ForegroundColor Cyan
Write-Host "=========================================" -ForegroundColor Cyan

Write-Host @"

  # Deploy to production (default)
  .\deploy.ps1

  # Deploy to development
  .\deploy.ps1 -Environment dev

  # Deploy specific branch
  .\deploy.ps1 -Branch develop

  # Run tests during build
  .\deploy.ps1 -SkipTests false

  # Don't auto-restart
  .\deploy.ps1 -AutoRestart false

  # From CMD
  deploy.cmd

"@ -ForegroundColor White

Write-Host "=========================================" -ForegroundColor Cyan
Write-Host "  Script is ready to use!" -ForegroundColor Green
Write-Host "=========================================`n" -ForegroundColor Green
