# SCIM Gateway - Complete API Test Suite
Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "  SCIM Gateway - Complete API Test Suite" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

$baseUri = "http://localhost:8080/scim/v2"
$passed = 0
$failed = 0

# Helper function
function Test-Endpoint {
    param($Name, $Uri, $Method, $Body = $null, $ExpectedStatus = 200)
    Write-Host "`nTest: $Name" -ForegroundColor Yellow
    try {
        $params = @{Uri = $Uri; Method = $Method; ContentType = "application/json"}
        if ($Body) { $params["Body"] = $Body }
        
        $response = Invoke-RestMethod @params
        Write-Host "✓ PASS - $Name" -ForegroundColor Green
        $script:passed++
        return $response
    } catch {
        Write-Host "✗ FAIL - $Name : $($_.Exception.Message)" -ForegroundColor Red
        $script:failed++
        return $null
    }
}

# ============================================
# TEST 1: SCIM Core - User Operations
# ============================================
Write-Host "`n--- User Operations ---" -ForegroundColor Cyan

# Test 1.1: Create User 1
Write-Host "`nTest 1.1: Create User 1" -ForegroundColor Yellow
$user1Json = '{"userName":"john.doe","emails":[{"value":"john@company.com","primary":true,"type":"work"}],"displayName":"John Doe","name":{"givenName":"John","familyName":"Doe"},"active":true,"title":"Software Engineer"}'
$user1 = Test-Endpoint "Create User 1" "$baseUri/Users" POST $user1Json
if ($user1) {
    Write-Host "  ID: $($user1.id)" -ForegroundColor Gray
    Write-Host "  UserName: $($user1.userName)" -ForegroundColor Gray
    Write-Host "  Email: $($user1.emails[0].value)" -ForegroundColor Gray
}

# Test 1.2: Create User 2
Write-Host "`nTest 1.2: Create User 2" -ForegroundColor Yellow
$user2Json = '{"userName":"jane.smith","emails":[{"value":"jane@company.com","primary":true}],"displayName":"Jane Smith","active":true}'
$user2 = Test-Endpoint "Create User 2" "$baseUri/Users" POST $user2Json

# Test 1.3: Get User by ID
if ($user1) {
    Write-Host "`nTest 1.3: Get User by ID" -ForegroundColor Yellow
    $getUser = Test-Endpoint "Get User $($user1.id)" "$baseUri/Users/$($user1.id)" GET
}

# Test 1.4: List All Users
Write-Host "`nTest 1.4: List All Users" -ForegroundColor Yellow
$listUsers = Test-Endpoint "List All Users" "$baseUri/Users" GET
if ($listUsers) {
    Write-Host "  Total Results: $($listUsers.totalResults)" -ForegroundColor Gray
    Write-Host "  Items Per Page: $($listUsers.itemsPerPage)" -ForegroundColor Gray
}

# Test 1.5: Filter Users
Write-Host "`nTest 1.5: Filter Users (userName eq)" -ForegroundColor Yellow
$filterUri = "$baseUri/Users?filter=userName%20eq%20%22john.doe%22"
$filterResult = Test-Endpoint "Filter Users" $filterUri GET
if ($filterResult) {
    Write-Host "  Filtered Results: $($filterResult.totalResults)" -ForegroundColor Gray
}

# Test 1.6: Patch User (Update active status)
if ($user1) {
    Write-Host "`nTest 1.6: Patch User (Deactivate)" -ForegroundColor Yellow
    $patchJson = '{"Operations":[{"op":"replace","path":"active","value":false}]}'
    $patchedUser = Test-Endpoint "Patch User" "$baseUri/Users/$($user1.id)" PATCH $patchJson
    if ($patchedUser) {
        Write-Host "  Active Status: $($patchedUser.active)" -ForegroundColor Gray
    }
    
    # Reactivate for further tests
    $patchJson2 = '{"Operations":[{"op":"replace","path":"active","value":true}]}'
    Invoke-RestMethod -Uri "$baseUri/Users/$($user1.id)" -Method PATCH -Body $patchJson2 -ContentType "application/json" | Out-Null
}

# Test 1.7: Delete User (Soft Delete)
if ($user2) {
    Write-Host "`nTest 1.7: Delete User (Soft Delete)" -ForegroundColor Yellow
    try {
        $null = Invoke-RestMethod -Uri "$baseUri/Users/$($user2.id)" -Method Delete
        Write-Host "✓ PASS - Delete User" -ForegroundColor Green
        $passed++
    } catch {
        Write-Host "✗ FAIL - Delete User" -ForegroundColor Red
        $failed++
    }
}

# ============================================
# TEST 2: SCIM Core - Group Operations
# ============================================
Write-Host "`n--- Group Operations ---" -ForegroundColor Cyan

# Test 2.1: Create Group
Write-Host "`nTest 2.1: Create Group" -ForegroundColor Yellow
$groupJson = '{"displayName":"Engineering Team"}'
$group = Test-Endpoint "Create Group" "$baseUri/Groups" POST $groupJson

# Test 2.2: List Groups
Write-Host "`nTest 2.2: List Groups" -ForegroundColor Yellow
$listGroups = Test-Endpoint "List Groups" "$baseUri/Groups" GET

# Test 2.3: Patch Group (Add Member)
if ($group -and $user1) {
    Write-Host "`nTest 2.3: Patch Group (Add Member)" -ForegroundColor Yellow
    $patchGroupJson = @"
{
    "Operations": [{
        "op": "add",
        "path": "members",
        "value": [{"value": "$($user1.id)", "display": "$($user1.displayName)"}]
    }]
}
"@
    $patchedGroup = Test-Endpoint "Patch Group" "$baseUri/Groups/$($group.id)" PATCH $patchGroupJson
}

# ============================================
# TEST 3: SCIM Configuration Endpoints
# ============================================
Write-Host "`n--- SCIM Configuration ---" -ForegroundColor Cyan

# Test 3.1: ServiceProviderConfig
Write-Host "`nTest 3.1: Get ServiceProviderConfig" -ForegroundColor Yellow
$config = Test-Endpoint "ServiceProviderConfig" "$baseUri/ServiceProviderConfig" GET
if ($config) {
    Write-Host "  Patch: $($config.patch.supported)" -ForegroundColor Gray
    Write-Host "  Filter: $($config.filter.supported)" -ForegroundColor Gray
    Write-Host "  Sort: $($config.sort.supported)" -ForegroundColor Gray
}

# Test 3.2: ResourceTypes
Write-Host "`nTest 3.2: Get ResourceTypes" -ForegroundColor Yellow
$resourceTypes = Test-Endpoint "ResourceTypes" "$baseUri/ResourceTypes" GET
if ($resourceTypes) {
    Write-Host "  Available Types: $($resourceTypes.Count)" -ForegroundColor Gray
}

# Test 3.3: Get User ResourceType
Write-Host "`nTest 3.3: Get User ResourceType" -ForegroundColor Yellow
$userType = Test-Endpoint "User ResourceType" "$baseUri/ResourceTypes/User" GET

# Test 3.4: Schemas
Write-Host "`nTest 3.4: Get Schemas" -ForegroundColor Yellow
$schemas = Test-Endpoint "Schemas" "$baseUri/Schemas" GET
if ($schemas) {
    Write-Host "  Available Schemas: $($schemas.Count)" -ForegroundColor Gray
}

# ============================================
# TEST 4: Provisioning & App Management
# ============================================
Write-Host "`n--- Provisioning & Apps ---" -ForegroundColor Cyan

# Test 4.1: Register Application
Write-Host "`nTest 4.1: Register Application" -ForegroundColor Yellow
$appJson = '{"name":"Test App","baseUrl":"https://app.example.com","apiKey":"test-api-key-123","enabled":true,"autoProvision":true,"maxRetries":3}'
$app = Test-Endpoint "Register App" "$baseUri/apps" POST $appJson

# Test 4.2: Get All Apps
Write-Host "`nTest 4.2: Get All Apps" -ForegroundColor Yellow
$apps = Test-Endpoint "Get All Apps" "$baseUri/apps" GET

# Test 4.3: Update Field Mappings
if ($app) {
    Write-Host "`nTest 4.3: Update Field Mappings" -ForegroundColor Yellow
    $mappingsJson = '{"userName": "userName", "email": "emails[0].value", "displayName": "displayName"}'
    try {
        $null = Invoke-RestMethod -Uri "$baseUri/apps/$($app.id)/mappings" -Method PUT -Body $mappingsJson -ContentType "application/json"
        Write-Host "✓ PASS - Update Field Mappings" -ForegroundColor Green
        $passed++
    } catch {
        Write-Host "✗ FAIL - Update Field Mappings" -ForegroundColor Red
        $failed++
    }
}

# Test 4.4: Provision User to App
if ($app -and $user1) {
    Write-Host "`nTest 4.4: Provision User to App" -ForegroundColor Yellow
    try {
        $provResult = Invoke-RestMethod -Uri "$baseUri/apps/provision/user/$($user1.id)/app/$($app.id)" -Method POST
        Write-Host "✓ PASS - Provision User to App" -ForegroundColor Green
        Write-Host "  Response: $provResult" -ForegroundColor Gray
        $passed++
    } catch {
        Write-Host "✗ FAIL - Provision User to App: $($_.Exception.Message)" -ForegroundColor Red
        $failed++
    }
}

# Test 4.5: Get User Provisioning Jobs
if ($user1) {
    Write-Host "`nTest 4.5: Get User Provisioning Jobs" -ForegroundColor Yellow
    $jobs = Test-Endpoint "Get User Jobs" "$baseUri/apps/jobs/user/$($user1.id)" GET
    if ($jobs) {
        Write-Host "  Total Jobs: $($jobs.Count)" -ForegroundColor Gray
    }
}

# ============================================
# TEST SUMMARY
# ============================================
Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "  Test Results Summary" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Passed: $passed" -ForegroundColor Green
Write-Host "  Failed: $failed" -ForegroundColor $(if ($failed -gt 0) {"Red"} else {"Green"})
Write-Host "  Total: $($passed + $failed)" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

if ($failed -eq 0) {
    Write-Host "🎉 All Tests Passed!" -ForegroundColor Green
} else {
    Write-Host "⚠️  Some tests failed. Review the output above." -ForegroundColor Yellow
}
