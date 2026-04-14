#!/usr/bin/env pwsh
# SCIM Gateway - Quick IdP Connection Test
# This script helps you test your SCIM Gateway for IdP integration

$baseUrl = "http://localhost:8080"

Write-Host "`n=========================================" -ForegroundColor Cyan
Write-Host "  SCIM Gateway IdP Connection Test" -ForegroundColor Cyan
Write-Host "=========================================" -ForegroundColor Cyan

# Test 1: Gateway Health
Write-Host "`n[1/6] Testing Gateway Health..." -ForegroundColor Yellow
try {
    $info = Invoke-RestMethod -Uri "$baseUrl/api/info" -UseBasicParsing
    Write-Host "   ✓ Gateway is running" -ForegroundColor Green
    Write-Host "   Name: $($info.name)" -ForegroundColor White
    Write-Host "   Version: $($info.version)" -ForegroundColor White
    Write-Host "   Status: $($info.status)" -ForegroundColor Green
} catch {
    Write-Host "   ✗ Gateway is NOT running!" -ForegroundColor Red
    Write-Host "   Start it with: mvn spring-boot:run" -ForegroundColor Red
    exit 1
}

# Test 2: Get JWT Token
Write-Host "`n[2/6] Getting JWT Token..." -ForegroundColor Yellow
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
    
    $token = $tokenResponse.token
    Write-Host "   ✓ Token obtained successfully" -ForegroundColor Green
    Write-Host "   Token: $($token.Substring(0, 50))..." -ForegroundColor Gray
    Write-Host "   Expires In: $($tokenResponse.expiresIn / 1000 / 3600) hours" -ForegroundColor White
} catch {
    Write-Host "   ✗ Failed to get token!" -ForegroundColor Red
    Write-Host "   Error: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# Test 3: SCIM Discovery Endpoints (Public)
Write-Host "`n[3/6] Testing SCIM Discovery Endpoints (Public)..." -ForegroundColor Yellow

$endpoints = @(
    @{Url="/scim/v2/ServiceProviderConfig"; Name="ServiceProviderConfig"},
    @{Url="/scim/v2/ResourceTypes"; Name="ResourceTypes"},
    @{Url="/scim/v2/Schemas"; Name="Schemas"}
)

foreach ($ep in $endpoints) {
    try {
        $response = Invoke-RestMethod -Uri "$baseUrl$($ep.Url)" -UseBasicParsing
        Write-Host "   ✓ $($ep.Name) - OK" -ForegroundColor Green
    } catch {
        Write-Host "   ✗ $($ep.Name) - FAILED" -ForegroundColor Red
    }
}

# Test 4: Create Test User
Write-Host "`n[4/6] Creating Test User..." -ForegroundColor Yellow

$userBody = @{
    userName = "idp-test@example.com"
    name = @{
        givenName = "IdP"
        familyName = "Test"
    }
    emails = @(@{
        value = "idp-test@example.com"
        primary = $true
        type = "work"
    })
    active = $true
    displayName = "IdP Test User"
} | ConvertTo-Json -Depth 10

$headers = @{
    "Authorization" = "Bearer $token"
    "Content-Type" = "application/json"
}

try {
    $user = Invoke-RestMethod -Uri "$baseUrl/scim/v2/Users" `
        -Method POST `
        -Headers $headers `
        -Body $userBody `
        -UseBasicParsing
    
    Write-Host "   ✓ User created successfully" -ForegroundColor Green
    Write-Host "   ID: $($user.id)" -ForegroundColor White
    Write-Host "   Username: $($user.userName)" -ForegroundColor White
    $testUserId = $user.id
} catch {
    Write-Host "   ✗ Failed to create user!" -ForegroundColor Red
    Write-Host "   Error: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# Test 5: Retrieve User
Write-Host "`n[5/6] Retrieving Test User..." -ForegroundColor Yellow

try {
    $retrievedUser = Invoke-RestMethod -Uri "$baseUrl/scim/v2/Users/$testUserId" `
        -Headers $headers `
        -UseBasicParsing
    
    Write-Host "   ✓ User retrieved successfully" -ForegroundColor Green
    Write-Host "   Username: $($retrievedUser.userName)" -ForegroundColor White
    Write-Host "   Display Name: $($retrievedUser.displayName)" -ForegroundColor White
    Write-Host "   Active: $($retrievedUser.active)" -ForegroundColor White
} catch {
    Write-Host "   ✗ Failed to retrieve user!" -ForegroundColor Red
}

# Test 6: Generate IdP Configuration Info
Write-Host "`n[6/6] Generating IdP Configuration Information..." -ForegroundColor Yellow

Write-Host "`n=========================================" -ForegroundColor Cyan
Write-Host "  IdP Configuration Details" -ForegroundColor Cyan
Write-Host "=========================================" -ForegroundColor Cyan

Write-Host "`nFor Azure AD / Microsoft Entra ID:" -ForegroundColor Yellow
Write-Host "  Tenant URL:     $baseUrl/scim/v2" -ForegroundColor White
Write-Host "  Secret Token:   $token" -ForegroundColor White

Write-Host "`nFor Okta:" -ForegroundColor Yellow
Write-Host "  Base URL:       $baseUrl/scim/v2" -ForegroundColor White
Write-Host "  API Token:      $token" -ForegroundColor White
Write-Host "  Auth Method:    OAuth Bearer Token" -ForegroundColor White

Write-Host "`nFor Keycloak:" -ForegroundColor Yellow
Write-Host "  SCIM Endpoint:  $baseUrl/scim/v2" -ForegroundColor White
Write-Host "  Bearer Token:   $token" -ForegroundColor White

Write-Host "`nFor OneLogin:" -ForegroundColor Yellow
Write-Host "  Base URL:       $baseUrl/scim/v2" -ForegroundColor White
Write-Host "  Authorization:  Bearer $token" -ForegroundColor White

Write-Host "`n=========================================" -ForegroundColor Cyan
Write-Host "  SCIM Endpoints for IdP" -ForegroundColor Cyan
Write-Host "=========================================" -ForegroundColor Cyan

Write-Host "`nPublic Endpoints (No Auth Required):" -ForegroundColor Yellow
Write-Host "  ServiceProviderConfig: $baseUrl/scim/v2/ServiceProviderConfig" -ForegroundColor White
Write-Host "  ResourceTypes:         $baseUrl/scim/v2/ResourceTypes" -ForegroundColor White
Write-Host "  Schemas:               $baseUrl/scim/v2/Schemas" -ForegroundColor White

Write-Host "`nAuthenticated Endpoints (Require Bearer Token):" -ForegroundColor Yellow
Write-Host "  Users:                 $baseUrl/scim/v2/Users" -ForegroundColor White
Write-Host "  Groups:                $baseUrl/scim/v2/Groups" -ForegroundColor White

Write-Host "`n=========================================" -ForegroundColor Cyan
Write-Host "  Test Summary" -ForegroundColor Cyan
Write-Host "=========================================" -ForegroundColor Cyan

Write-Host "`n✅ Your SCIM Gateway is ready for IdP integration!" -ForegroundColor Green
Write-Host "`nNext Steps:" -ForegroundColor Yellow
Write-Host "  1. Copy the configuration details above" -ForegroundColor White
Write-Host "  2. Go to your IdP (Azure AD, Okta, Keycloak, etc.)" -ForegroundColor White
Write-Host "  3. Create a new SCIM application" -ForegroundColor White
Write-Host "  4. Paste the Base URL and Token" -ForegroundColor White
Write-Host "  5. Test the connection" -ForegroundColor White
Write-Host "  6. Enable automatic provisioning" -ForegroundColor White

Write-Host "`nFor detailed instructions, see: IDP-INTEGRATION-GUIDE.md" -ForegroundColor Cyan
Write-Host ""

# Cleanup: Delete test user
Write-Host "Cleaning up test user..." -ForegroundColor Gray
try {
    Invoke-RestMethod -Uri "$baseUrl/scim/v2/Users/$testUserId" `
        -Method DELETE `
        -Headers $headers `
        -UseBasicParsing | Out-Null
    Write-Host "[OK] Test user deleted" -ForegroundColor Green
} catch {
    Write-Host "[WARN] Test user deletion failed" -ForegroundColor Yellow
}

Write-Host ""
