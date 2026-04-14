#!/usr/bin/env pwsh
# SCIM Gateway - Production Setup Helper
# This script helps you generate secure configuration for production

Write-Host "`n=========================================" -ForegroundColor Cyan
Write-Host "  SCIM Gateway Production Setup Helper" -ForegroundColor Cyan
Write-Host "=========================================" -ForegroundColor Cyan

# Function to generate random password
function Generate-Password {
    param(
        [int]$Length = 20
    )
    $chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!@#$%^&*"
    $password = -join ((1..$Length) | ForEach-Object { Get-Random -Input $chars.ToCharArray() })
    return $password
}

# Function to generate JWT secret
function Generate-JwtSecret {
    $bytes = New-Object byte[] 32
    $rng = [System.Security.Cryptography.RNGCryptoServiceProvider]::new()
    $rng.GetBytes($bytes)
    $rng.Dispose()
    return [Convert]::ToBase64String($bytes)
}

Write-Host "`n[1/4] Generating Secure Secrets..." -ForegroundColor Yellow

# Generate JWT Secret
$jwtSecret = Generate-JwtSecret
Write-Host "`n  JWT Secret Generated:" -ForegroundColor Green
Write-Host "  $jwtSecret" -ForegroundColor White
Write-Host "  (Keep this secret! Do not commit to git)" -ForegroundColor Gray

# Generate Admin Password
$adminPassword = Generate-Password -Length 20
Write-Host "`n  Admin Password Generated:" -ForegroundColor Green
Write-Host "  $adminPassword" -ForegroundColor White
Write-Host "  (Store in password manager)" -ForegroundColor Gray

Write-Host "`n[2/4] Configuration Checklist" -ForegroundColor Yellow

$checklist = @(
    @{Item="Generate JWT Secret"; Status="Done"; Icon="[OK]"},
    @{Item="Generate Admin Password"; Status="Done"; Icon="[OK]"},
    @{Item="Update application.properties"; Status="Pending"; Icon="[ ]"},
    @{Item="Configure MongoDB Atlas URI"; Status="Pending"; Icon="[ ]"},
    @{Item="Test with new credentials"; Status="Pending"; Icon="[ ]"},
    @{Item="Deploy to production"; Status="Pending"; Icon="[ ]"}
)

Write-Host "`n  Configuration Status:" -ForegroundColor White
foreach ($item in $checklist) {
    $color = if ($item.Status -eq "Done") { "Green" } else { "Yellow" }
    Write-Host "  $($item.Icon) $($item.Item)" -ForegroundColor $color
}

Write-Host "`n[3/4] Environment Variables Template" -ForegroundColor Yellow

Write-Host "`n  Copy these environment variables:" -ForegroundColor White
Write-Host "  ----------------------------------------" -ForegroundColor Gray
Write-Host "  # For Linux/Mac:" -ForegroundColor Gray
Write-Host "  export JWT_SECRET=`"$jwtSecret`"" -ForegroundColor Cyan
Write-Host "  export SCIM_ADMIN_PASSWORD=`"$adminPassword`"" -ForegroundColor Cyan
Write-Host "  export MONGODB_URI=`"mongodb+srv://user:pass@cluster.mongodb.net/scimdb`"" -ForegroundColor Cyan
Write-Host "  export SPRING_PROFILES_ACTIVE=`"prod`"" -ForegroundColor Cyan
Write-Host ""
Write-Host "  # For Windows (PowerShell):" -ForegroundColor Gray
Write-Host "  `$env:JWT_SECRET = `"$jwtSecret`"" -ForegroundColor Cyan
Write-Host "  `$env:SCIM_ADMIN_PASSWORD = `"$adminPassword`"" -ForegroundColor Cyan
Write-Host "  `$env:MONGODB_URI = `"mongodb+srv://user:pass@cluster.mongodb.net/scimdb`"" -ForegroundColor Cyan
Write-Host "  `$env:SPRING_PROFILES_ACTIVE = `"prod`"" -ForegroundColor Cyan
Write-Host "  ----------------------------------------" -ForegroundColor Gray

Write-Host "`n[4/4] Next Steps" -ForegroundColor Yellow

$nextSteps = @(
    "Save the generated secrets in a secure location (password manager)",
    "Create MongoDB Atlas production cluster (if not already done)",
    "Update MONGODB_URI with your actual MongoDB connection string",
    "Set environment variables on your production server",
    "Run tests: powershell -ExecutionPolicy Bypass -File test-comprehensive.ps1",
    "Deploy application: mvn clean package -DskipTests",
    "Start with production profile: java -jar target/scim-app-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod",
    "Monitor logs for first 24-48 hours",
    "Set up automated backups for MongoDB",
    "Configure monitoring and alerting"
)

Write-Host "`n  Immediate Actions Required:" -ForegroundColor White
for ($i = 0; $i -lt $nextSteps.Count; $i++) {
    Write-Host "  $($i + 1). $($nextSteps[$i])" -ForegroundColor White
}

Write-Host "`n=========================================" -ForegroundColor Cyan
Write-Host "  Security Reminders" -ForegroundColor Cyan
Write-Host "=========================================" -ForegroundColor Cyan

Write-Host "`n  [!] NEVER commit secrets to git" -ForegroundColor Red
Write-Host "  [!] Use environment variables or secrets manager" -ForegroundColor Yellow
Write-Host "  [!] Rotate JWT secret regularly (every 90 days)" -ForegroundColor Yellow
Write-Host "  [!] Change admin password periodically" -ForegroundColor Yellow
Write-Host "  [!] Enable HTTPS in production" -ForegroundColor Yellow
Write-Host "  [!] Restrict Swagger UI access in production" -ForegroundColor Yellow
Write-Host "  [!] Set up monitoring and alerting" -ForegroundColor Yellow

Write-Host "`n  Documentation:" -ForegroundColor Cyan
Write-Host "  - PRODUCTION-CONFIG-REVIEW.md (Full checklist)" -ForegroundColor White
Write-Host "  - DEPLOYMENT-GUIDE.md (Deployment steps)" -ForegroundColor White
Write-Host "  - IDP-INTEGRATION-GUIDE.md (IdP setup)" -ForegroundColor White

Write-Host "`n  Files Created:" -ForegroundColor Cyan
Write-Host "  - application-prod.properties.template (Production config template)" -ForegroundColor White
Write-Host "  - PRODUCTION-CONFIG-REVIEW.md (Security checklist)" -ForegroundColor White

Write-Host "`n=========================================`n" -ForegroundColor Cyan

Write-Host "Setup helper complete! Follow the steps above to secure your production deployment.`n"
