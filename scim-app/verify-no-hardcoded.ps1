# verify-no-hardcoded.ps1 - Script to verify no hardcoded URLs or secrets exist

Write-Host "`n=============================================" -ForegroundColor Cyan
Write-Host "  Checking for Hardcoded Values..." -ForegroundColor Cyan
Write-Host "=============================================" -ForegroundColor Cyan
Write-Host ""

$errors = 0

# Check for hardcoded URLs
Write-Host "1. Checking for hardcoded URLs..." -ForegroundColor Yellow
$urlMatches = Get-ChildItem -Path "src\main\java" -Recurse -Filter "*.java" | Select-String -Pattern 'https?://' | Where-Object { $_.Line -notmatch '^\s*\/\/' -and $_.Line -notmatch '@Value' }

if ($null -eq $urlMatches) {
    Write-Host "   ✅ No hardcoded URLs found" -ForegroundColor Green
} else {
    Write-Host "   ❌ Found $($urlMatches.Count) hardcoded URLs:" -ForegroundColor Red
    $urlMatches | ForEach-Object { Write-Host "      $($_.Filename):$($_.LineNumber)" -ForegroundColor Red }
    $errors++
}

Write-Host ""

# Check for hardcoded passwords
Write-Host "2. Checking for hardcoded passwords..." -ForegroundColor Yellow
$passMatches = Get-ChildItem -Path "src\main\java" -Recurse -Filter "*.java" | Select-String -Pattern 'password.*=.*[''"]' | Where-Object { $_.Line -notmatch '@Value' -and $_.Line -notmatch '\/\/' }

if ($null -eq $passMatches) {
    Write-Host "   ✅ No hardcoded passwords found" -ForegroundColor Green
} else {
    Write-Host "   ❌ Found $($passMatches.Count) hardcoded passwords:" -ForegroundColor Red
    $passMatches | ForEach-Object { Write-Host "      $($_.Filename):$($_.LineNumber)" -ForegroundColor Red }
    $errors++
}

Write-Host ""

# Check for hardcoded secrets
Write-Host "3. Checking for hardcoded secrets..." -ForegroundColor Yellow
$secretMatches = Get-ChildItem -Path "src\main\java" -Recurse -Filter "*.java" | Select-String -Pattern 'secret.*=.*[''"]' | Where-Object { $_.Line -notmatch '@Value' -and $_.Line -notmatch '\/\/' }

if ($null -eq $secretMatches) {
    Write-Host "   ✅ No hardcoded secrets found" -ForegroundColor Green
} else {
    Write-Host "   ❌ Found $($secretMatches.Count) hardcoded secrets:" -ForegroundColor Red
    $secretMatches | ForEach-Object { Write-Host "      $($_.Filename):$($_.LineNumber)" -ForegroundColor Red }
    $errors++
}

Write-Host ""

# Check for hardcoded emails
Write-Host "4. Checking for hardcoded emails..." -ForegroundColor Yellow
$emailMatches = Get-ChildItem -Path "src\main\java" -Recurse -Filter "*.java" | Select-String -Pattern '[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}' | Where-Object { $_.Line -notmatch '@Value' -and $_.Line -notmatch '\/\/' }

if ($null -eq $emailMatches) {
    Write-Host "   ✅ No hardcoded emails found" -ForegroundColor Green
} else {
    Write-Host "   ⚠️  Found $($emailMatches.Count) hardcoded emails (may be acceptable)" -ForegroundColor Yellow
    $emailMatches | ForEach-Object { Write-Host "      $($_.Filename):$($_.LineNumber)" -ForegroundColor Yellow }
}

Write-Host ""

# Check for localhost URLs
Write-Host "5. Checking for localhost URLs..." -ForegroundColor Yellow
$localhostMatches = Get-ChildItem -Path "src\main\java" -Recurse -Filter "*.java" | Select-String -Pattern 'localhost:' | Where-Object { $_.Line -notmatch '\/\/' }

if ($null -eq $localhostMatches) {
    Write-Host "   ✅ No hardcoded localhost URLs found" -ForegroundColor Green
} else {
    Write-Host "   ❌ Found $($localhostMatches.Count) hardcoded localhost URLs:" -ForegroundColor Red
    $localhostMatches | ForEach-Object { Write-Host "      $($_.Filename):$($_.LineNumber)" -ForegroundColor Red }
    $errors++
}

Write-Host ""
Write-Host "=============================================" -ForegroundColor Cyan

if ($errors -eq 0) {
    Write-Host "  ✅ ALL CHECKS PASSED!" -ForegroundColor Green
    Write-Host "  No hardcoded values found in source code" -ForegroundColor Green
} else {
    Write-Host "  ❌ FOUND $errors ISSUE(S)" -ForegroundColor Red
    Write-Host "  Please remove all hardcoded values" -ForegroundColor Red
}

Write-Host "=============================================" -ForegroundColor Cyan
Write-Host ""

exit $errors
