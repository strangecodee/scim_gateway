#!/usr/bin/env pwsh
# SCIM Gateway - Quick Production Test
# Tests the application with production-like configuration

Write-Host "`n=========================================" -ForegroundColor Cyan
Write-Host "  SCIM Gateway Production Config Test" -ForegroundColor Cyan
Write-Host "=========================================" -ForegroundColor Cyan

$baseUrl = "http://localhost:8080"

# Check if app is running
Write-Host "`n[1/3] Checking Application Status..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/api/info" -UseBasicParsing -ErrorAction Stop
    Write-Host "  [OK] Application is running" -ForegroundColor Green
    Write-Host "  Name: $($response.name)" -ForegroundColor White
    Write-Host "  Version: $($response.version)" -ForegroundColor White
} catch {
    Write-Host "  [ERROR] Application is not running!" -ForegroundColor Red
    Write-Host "  Start it with: mvn spring-boot:run" -ForegroundColor Yellow
    exit 1
}

# Test authentication with current credentials
Write-Host "`n[2/3] Testing Authentication..." -ForegroundColor Yellow

$loginBody = @{
    username = "admin"
    password = "admin123"
} | ConvertTo-Json

try {
    $tokenResponse = Invoke-RestMethod -Uri "$baseUrl/auth/login" `
        -Method POST `
        -Body $loginBody `
        -ContentType "application/json" `
        -UseBasicParsing
    
    Write-Host "  [OK] Authentication working" -ForegroundColor Green
    Write-Host "  Token: $($tokenResponse.token.Substring(0, 40))..." -ForegroundColor Gray
} catch {
    Write-Host "  [FAIL] Authentication failed!" -ForegroundColor Red
    Write-Host "  Error: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# Test SCIM endpoints
Write-Host "`n[3/3] Testing SCIM Endpoints..." -ForegroundColor Yellow

$endpoints = @(
    @{Url="/scim/v2/ServiceProviderConfig"; Name="ServiceProviderConfig"; Auth=$false},
    @{Url="/scim/v2/Users"; Name="Users List"; Auth=$true}
)

foreach ($ep in $endpoints) {
    try {
        $params = @{
            Uri = "$baseUrl$($ep.Url)"
            UseBasicParsing = $true
        }
        
        if ($ep.Auth -and $tokenResponse.token) {
            $params["Headers"] = @{
                "Authorization" = "Bearer $($tokenResponse.token)"
            }
        }
        
        Invoke-RestMethod @params | Out-Null
        Write-Host "  [OK] $($ep.Name)" -ForegroundColor Green
    } catch {
        Write-Host "  [FAIL] $($ep.Name)" -ForegroundColor Red
    }
}

# Display production configuration info
Write-Host "`n=========================================" -ForegroundColor Cyan
Write-Host "  Production Configuration Info" -ForegroundColor Cyan
Write-Host "=========================================" -ForegroundColor Cyan

Write-Host "`nCurrent Configuration:" -ForegroundColor Yellow
Write-Host "  Profile: dev (change to prod for production)" -ForegroundColor White
Write-Host "  JWT Secret: Using default (MUST change!)" -ForegroundColor Red
Write-Host "  Admin Password: Using default (MUST change!)" -ForegroundColor Red
Write-Host "  Database: MongoDB localhost (use Atlas for prod)" -ForegroundColor Yellow

Write-Host "`nFor Production Deployment:" -ForegroundColor Yellow
Write-Host "  1. Set environment variables:" -ForegroundColor White
Write-Host "     `$env:JWT_SECRET = `"cPVO8XANn5o1KOYZCKLtrDQEF8pGRb6madUSE9rB6lA=`"" -ForegroundColor Cyan
Write-Host "     `$env:SCIM_ADMIN_PASSWORD = `"tLmmIYs#pdaBD8RE9CfN`"" -ForegroundColor Cyan
Write-Host "     `$env:MONGODB_URI = `"your-mongodb-atlas-uri`"" -ForegroundColor Cyan
Write-Host "     `$env:SPRING_PROFILES_ACTIVE = `"prod`"" -ForegroundColor Cyan

Write-Host "`n  2. Build for production:" -ForegroundColor White
Write-Host "     mvn clean package -DskipTests" -ForegroundColor Cyan

Write-Host "`n  3. Run with production profile:" -ForegroundColor White
Write-Host "     java -jar target/scim-app-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod" -ForegroundColor Cyan

Write-Host "`n=========================================" -ForegroundColor Cyan
Write-Host "  Status: READY FOR TESTING" -ForegroundColor Green
Write-Host "=========================================`n" -ForegroundColor Cyan
