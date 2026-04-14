# SCIM Gateway - Comprehensive API Test Suite
$baseUrl = "http://localhost:8080"
$passed = 0
$failed = 0
$total = 0

Write-Host "`n=========================================" -ForegroundColor Cyan
Write-Host "  SCIM Gateway Comprehensive API Tests" -ForegroundColor Cyan
Write-Host "=========================================" -ForegroundColor Cyan
Write-Host "Base URL: $baseUrl`n" -ForegroundColor White

function Test-API {
    param(
        [string]$Name,
        [string]$Method = "GET",
        [string]$Url,
        [string]$Body = $null,
        [string]$Token = $null,
        [int]$ExpectedStatus = 200
    )
    
    $script:total++
    Write-Host "[$script:total] $Name" -ForegroundColor Yellow
    
    try {
        $params = @{
            Uri = $Url
            Method = $Method
            UseBasicParsing = $true
            TimeoutSec = 10
        }
        
        # Add Authorization header if token is provided
        if ($Token) {
            $params["Headers"] = @{
                "Authorization" = "Bearer $Token"
            }
        }
        
        if ($Body) {
            $params["Body"] = $Body
            $params["ContentType"] = "application/json"
        }
        
        $response = Invoke-RestMethod @params
        $statusCode = 200
        
        if ($statusCode -eq $ExpectedStatus) {
            Write-Host "    [PASS] Status: $statusCode" -ForegroundColor Green
            $script:passed++
            return $response
        } else {
            Write-Host "    [FAIL] Expected: $ExpectedStatus, Got: $statusCode" -ForegroundColor Red
            $script:failed++
            return $null
        }
    }
    catch {
        $statusCode = $_.Exception.Response.StatusCode.value__
        
        if ($statusCode -eq $ExpectedStatus) {
            Write-Host "    [PASS] Status: $statusCode" -ForegroundColor Green
            $script:passed++
        } else {
            Write-Host "    [FAIL] Expected: $ExpectedStatus, Got: $statusCode" -ForegroundColor Red
            Write-Host "    Error: $($_.Exception.Message)" -ForegroundColor Red
            $script:failed++
        }
        return $null
    }
}

# =========================================
# 1. BASIC ENDPOINTS
# =========================================
Write-Host "`n=== 1. BASIC ENDPOINTS ===" -ForegroundColor Magenta

Test-API -Name "API Info" -Url "$baseUrl/api/info" -ExpectedStatus 200
Test-API -Name "Auth Credentials" -Url "$baseUrl/auth/credentials" -ExpectedStatus 200
Test-API -Name "SCIM ServiceProviderConfig" -Url "$baseUrl/scim/v2/ServiceProviderConfig" -ExpectedStatus 200
Test-API -Name "SCIM ResourceTypes (List)" -Url "$baseUrl/scim/v2/ResourceTypes" -ExpectedStatus 200
Test-API -Name "SCIM Schemas (List)" -Url "$baseUrl/scim/v2/Schemas" -ExpectedStatus 200

# =========================================
# 2. AUTHENTICATION
# =========================================
Write-Host "`n=== 2. AUTHENTICATION ===" -ForegroundColor Magenta

$loginBody = @{
    username = "admin"
    password = "admin123"
} | ConvertTo-Json

$loginResponse = Test-API -Name "Login (Get Token)" -Method "POST" -Url "$baseUrl/auth/login" -Body $loginBody -ExpectedStatus 200
$token = if ($loginResponse) { $loginResponse.token } else { $null }

if ($token) {
    Write-Host "    Token: $($token.Substring(0, 40))..." -ForegroundColor Gray
}

# =========================================
# 3. SCIM USER CRUD OPERATIONS
# =========================================
Write-Host "`n=== 3. SCIM USER CRUD OPERATIONS ===" -ForegroundColor Magenta

$user1Body = @{
    userName = "john.doe@example.com"
    name = @{
        givenName = "John"
        familyName = "Doe"
    }
    emails = @(@{
        value = "john.doe@example.com"
        primary = $true
        type = "work"
    })
    active = $true
    displayName = "John Doe"
} | ConvertTo-Json -Depth 10

$user1Response = Test-API -Name "Create User 1 (john.doe)" -Method "POST" -Url "$baseUrl/scim/v2/Users" -Body $user1Body -Token $token -ExpectedStatus 200
$user1Id = if ($user1Response) { $user1Response.id } else { $null }

$user2Body = @{
    userName = "jane.smith@example.com"
    name = @{
        givenName = "Jane"
        familyName = "Smith"
    }
    emails = @(@{
        value = "jane.smith@example.com"
        primary = $true
        type = "work"
    })
    active = $true
    displayName = "Jane Smith"
} | ConvertTo-Json -Depth 10

$user2Response = Test-API -Name "Create User 2 (jane.smith)" -Method "POST" -Url "$baseUrl/scim/v2/Users" -Body $user2Body -Token $token -ExpectedStatus 200
$user2Id = if ($user2Response) { $user2Response.id } else { $null }

Test-API -Name "List Users" -Url "$baseUrl/scim/v2/Users" -Token $token -ExpectedStatus 200

Test-API -Name "Filter Users (eq operator)" -Url "$baseUrl/scim/v2/Users?filter=userName eq `"john.doe@example.com`"" -Token $token -ExpectedStatus 200

Test-API -Name "Filter Users (co operator)" -Url "$baseUrl/scim/v2/Users?filter=userName co `"john`"" -Token $token -ExpectedStatus 200

Test-API -Name "List Users with Pagination" -Url "$baseUrl/scim/v2/Users?startIndex=1&count=5" -Token $token -ExpectedStatus 200

if ($user1Id) {
    Test-API -Name "Get User by ID" -Url "$baseUrl/scim/v2/Users/$user1Id" -Token $token -ExpectedStatus 200
}

# =========================================
# 4. SCIM GROUP OPERATIONS
# =========================================
Write-Host "`n=== 4. SCIM GROUP OPERATIONS ===" -ForegroundColor Magenta

$groupBody = @{
    displayName = "Engineering Team"
    members = @()
} | ConvertTo-Json -Depth 10

$groupResponse = Test-API -Name "Create Group" -Method "POST" -Url "$baseUrl/scim/v2/Groups" -Body $groupBody -Token $token -ExpectedStatus 200
$groupId = if ($groupResponse) { $groupResponse.id } else { $null }

Test-API -Name "List Groups" -Url "$baseUrl/scim/v2/Groups" -Token $token -ExpectedStatus 200

Test-API -Name "Filter Groups" -Url "$baseUrl/scim/v2/Groups?filter=displayName eq `"Engineering Team`"" -Token $token -ExpectedStatus 200

# =========================================
# 5. APPLICATION REGISTRY
# =========================================
Write-Host "`n=== 5. APPLICATION REGISTRY ===" -ForegroundColor Magenta

Test-API -Name "List Applications" -Url "$baseUrl/scim/v2/apps" -Token $token -ExpectedStatus 200

$appBody = @{
    name = "Test Application"
    baseUrl = "https://test.example.com"
    apiKey = "test-api-key-123"
    enabled = $true
    autoProvision = $true
    fieldMappings = @{
        userName = "email"
        displayName = "fullName"
    }
} | ConvertTo-Json -Depth 10

$appResponse = Test-API -Name "Create Application" -Method "POST" -Url "$baseUrl/scim/v2/apps" -Body $appBody -Token $token -ExpectedStatus 200
$appId = if ($appResponse) { $appResponse.id } else { $null }

if ($appId) {
    Test-API -Name "Get Application by ID" -Url "$baseUrl/scim/v2/apps/$appId" -Token $token -ExpectedStatus 200
}

# =========================================
# 6. PROVISIONING JOBS
# =========================================
Write-Host "`n=== 6. PROVISIONING JOBS ===" -ForegroundColor Magenta

Test-API -Name "List All Jobs" -Url "$baseUrl/scim/v2/apps/jobs" -Token $token -ExpectedStatus 200

Test-API -Name "Filter Jobs by Status (FAILED)" -Url "$baseUrl/scim/v2/apps/jobs/status/FAILED" -Token $token -ExpectedStatus 200

Test-API -Name "Filter Jobs by Status (SUCCESS)" -Url "$baseUrl/scim/v2/apps/jobs/status/SUCCESS" -Token $token -ExpectedStatus 200

# =========================================
# 7. SCHEMA DISCOVERY
# =========================================
Write-Host "`n=== 7. SCHEMA DISCOVERY ===" -ForegroundColor Magenta

Test-API -Name "Get User Schema" -Url "$baseUrl/scim/v2/Schemas/urn:ietf:params:scim:schemas:core:2.0:User" -Token $token -ExpectedStatus 200

Test-API -Name "Get Group Schema" -Url "$baseUrl/scim/v2/Schemas/urn:ietf:params:scim:schemas:core:2.0:Group" -Token $token -ExpectedStatus 200

Test-API -Name "Get User ResourceType" -Url "$baseUrl/scim/v2/ResourceTypes/User" -Token $token -ExpectedStatus 200

Test-API -Name "Get Group ResourceType" -Url "$baseUrl/scim/v2/ResourceTypes/Group" -Token $token -ExpectedStatus 200

# =========================================
# 8. ERROR HANDLING
# =========================================
Write-Host "`n=== 8. ERROR HANDLING ===" -ForegroundColor Magenta

Test-API -Name "Invalid Login (expect 401)" -Method "POST" -Url "$baseUrl/auth/login" -Body '{"username":"wrong","password":"wrong"}' -ExpectedStatus 401

Test-API -Name "Validate Invalid Token (expect 401)" -Method "POST" -Url "$baseUrl/auth/validate" -Body '{"token":"invalid-token"}' -ExpectedStatus 401

# =========================================
# 9. ADVANCED FEATURES
# =========================================
Write-Host "`n=== 9. ADVANCED FEATURES ===" -ForegroundColor Magenta

Test-API -Name "ResourceType - User Details" -Url "$baseUrl/scim/v2/ResourceTypes/User" -Token $token -ExpectedStatus 200

Test-API -Name "ResourceType - Group Details" -Url "$baseUrl/scim/v2/ResourceTypes/Group" -Token $token -ExpectedStatus 200

# =========================================
# SUMMARY
# =========================================
Write-Host "`n=========================================" -ForegroundColor Cyan
Write-Host "  Test Summary" -ForegroundColor Cyan
Write-Host "=========================================" -ForegroundColor Cyan
Write-Host "  Total Tests: $total" -ForegroundColor White
Write-Host "  Passed: $passed" -ForegroundColor Green
Write-Host "  Failed: $failed" -ForegroundColor Red

if ($total -gt 0) {
    $percentage = [math]::Round(($passed / $total) * 100, 2)
    Write-Host "  Success Rate: $percentage%" -ForegroundColor Yellow
}

Write-Host "=========================================`n" -ForegroundColor Cyan

if ($failed -eq 0) {
    Write-Host "✅ All tests passed!" -ForegroundColor Green -BackgroundColor DarkGreen
    Write-Host ""
} else {
    Write-Host "⚠ $failed test(s) failed" -ForegroundColor Red -BackgroundColor DarkRed
    Write-Host ""
}
