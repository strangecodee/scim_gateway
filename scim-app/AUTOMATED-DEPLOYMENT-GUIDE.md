# Automated Deployment Guide

## Overview

The `deploy.ps1` script automates the entire deployment process:
- ✅ Fetches latest code from Git
- ✅ Configures environment variables
- ✅ Builds the application
- ✅ Stops running instance (if any)
- ✅ Starts with production settings
- ✅ Health checks and verification

---

## Quick Start

### First Time Setup

#### 1. Create Environment File

```powershell
# Copy the example file
cp .env.example .env

# Edit with your values (use Notepad, VS Code, etc.)
notepad .env
```

#### 2. Fill in Required Values

Open `.env` and update:

```bash
# Generate JWT Secret
# Run: openssl rand -base64 32
JWT_SECRET=your-generated-secret-here

# Set strong admin password
SCIM_ADMIN_PASSWORD=your-strong-password-here

# Update MongoDB URI with your credentials
MONGODB_URI=mongodb+srv://user:password@cluster.mongodb.net/scimdb
```

#### 3. Run Deployment

```powershell
# Deploy to production (default)
.\deploy.ps1

# Or deploy to development
.\deploy.ps1 -Environment dev

# Or deploy specific branch
.\deploy.ps1 -Branch develop
```

---

## Usage Examples

### Basic Usage

```powershell
# Production deployment (default)
.\deploy.ps1
```

### Environment Options

```powershell
# Development environment
.\deploy.ps1 -Environment dev

# Staging environment
.\deploy.ps1 -Environment staging

# Production environment
.\deploy.ps1 -Environment prod
```

### Branch Options

```powershell
# Deploy main branch (default)
.\deploy.ps1 -Branch main

# Deploy develop branch
.\deploy.ps1 -Branch develop

# Deploy feature branch
.\deploy.ps1 -Branch feature/new-feature
```

### Advanced Options

```powershell
# Run tests during build (default: skips tests)
.\deploy.ps1 -SkipTests:$false

# Don't auto-restart if running
.\deploy.ps1 -AutoRestart:$false

# Combine options
.\deploy.ps1 -Environment dev -Branch develop -SkipTests:$false
```

---

## What the Script Does

### Step 1: Check Prerequisites
- ✅ Verifies Git is installed
- ✅ Verifies Maven is installed
- ✅ Verifies Java is installed

### Step 2: Fetch Latest Code
- ✅ Stashes any local changes
- ✅ Fetches from remote repository
- ✅ Checks out specified branch
- ✅ Pulls latest changes
- ✅ Shows commit log

### Step 3: Configure Environment
- ✅ Loads `.env` file if exists
- ✅ Sets `SPRING_PROFILES_ACTIVE`
- ✅ Validates critical variables
- ✅ Warns about missing values

### Step 4: Stop Running App
- ✅ Detects running instance
- ✅ Attempts graceful shutdown
- ✅ Force kills if needed
- ✅ Verifies stopped

### Step 5: Build Application
- ✅ Runs `mvn clean package`
- ✅ Skips tests by default
- ✅ Shows JAR file size
- ✅ Reports build errors

### Step 6: Verify Configuration
- ✅ Checks property files exist
- ✅ Validates environment config
- ✅ Reports missing files

### Step 7: Start Application
- ✅ Starts with correct profile
- ✅ Waits for startup (60s timeout)
- ✅ Checks port 8080 is listening
- ✅ Runs health check
- ✅ Shows access URLs

---

## Output Example

```
=========================================
  SCIM Gateway - Automated Deployment
=========================================
Environment : prod
Branch      : main
Auto Restart: True
Skip Tests  : True

=========================================
  Step 1/7: Checking Prerequisites
=========================================
  [OK] Git
         git version 2.42.0.windows.2
  [OK] Maven
         Apache Maven 3.9.5
  [OK] Java
         openjdk 17.0.9 2023-10-17

=========================================
  Step 2/7: Fetching Latest Code from Git
=========================================
  Current branch: main
  Target branch : main
  Fetching from remote...
  Checking out branch: main
  Pulling latest changes...
  Code updated successfully
  Changes:
    abc1234 Fixed production configuration
    def5678 Updated security settings

=========================================
  Step 3/7: Configuring Environment Variables
=========================================
  Loading environment variables from .env file
  Set: JWT_SECRET
  Set: SCIM_ADMIN_PASSWORD
  Set: MONGODB_URI
  Profile: prod
  [OK] JWT_SECRET is set
  [OK] SCIM_ADMIN_PASSWORD is set
  [OK] MONGODB_URI is set

=========================================
  Step 4/7: Stopping Running Application
=========================================
  Found running instance (PID: 12345)
  Stopping application...
  Application stopped

=========================================
  Step 5/7: Building Application
=========================================
  Command: mvn clean package -DskipTests
  This may take a few minutes...
  Build successful!
  JAR: scim-app-0.0.1-SNAPSHOT.jar (45.23 MB)

=========================================
  Step 6/7: Verifying Configuration
=========================================
  [OK] Main config exists
  [OK] prod config exists

=========================================
  Step 7/7: Starting Application
=========================================
  Profile    : prod
  JAR File   : D:\linux\P1\SCIM\scim-app\scim-app\target\scim-app-0.0.1-SNAPSHOT.jar
  Application started (PID: 67890)
  
  Waiting for application to start...
  Application is ready!
  URL: http://localhost:8080
  Swagger: http://localhost:8080/swagger-ui.html
  Health: http://localhost:8080/actuator/health
  Status: UP

=========================================
  Deployment Complete!
=========================================
Environment : prod
Branch      : main
Profile     : prod
PID         : 67890
URL         : http://localhost:8080
Swagger     : http://localhost:8080/swagger-ui.html
Log File    : D:\linux\P1\SCIM\scim-app\scim-app\logs\deploy-20260413-123456.log
=========================================

Quick Commands:
  View logs   : Get-Content logs\deploy-20260413-123456.log -Tail 50 -Wait
  Stop app    : Stop-Process -Id 67890
  Test APIs   : .\test-comprehensive.ps1
  Check health: curl http://localhost:8080/actuator/health

Deployment successful! Application is running.
```

---

## Environment File Setup

### Generate Secrets

#### JWT Secret
```bash
# Using OpenSSL
openssl rand -base64 32

# Using PowerShell
-join ((1..32) | ForEach-Object { [char]((33..126) | Get-Random) }) | ForEach-Object { [Convert]::ToBase64String([System.Text.Encoding]::UTF8.GetBytes($_)) }
```

#### Admin Password
```bash
# Using password manager (recommended)

# Using PowerShell (20 chars)
-join ((33..126) | Get-Random -Count 20 | ForEach-Object {[char]$_})
```

### Example .env File

```bash
# Security
JWT_SECRET=cPVO8XANn5o1KOYZCKLtrDQEF8pGRb6madUSE9rB6lA=
SCIM_ADMIN_PASSWORD=tLmmIYs#pdaBD8RE9CfN

# Database
MONGODB_URI=mongodb+srv://anurag:cloud%40123@cluster0.nivrt0z.mongodb.net/scimdb?retryWrites=true&w=majority

# Profile
SPRING_PROFILES_ACTIVE=prod
```

---

## Troubleshooting

### Issue: "Prerequisites not found"

**Solution:**
```powershell
# Install Git
winget install Git.Git

# Install Maven
winget install Apache.Maven

# Install Java
winget install OpenJDK.17
```

### Issue: "Missing critical environment variables"

**Solution:**
```powershell
# Check if .env file exists
ls .env

# Create from example
cp .env.example .env

# Edit with your values
notepad .env
```

### Issue: "Build failed"

**Solution:**
```powershell
# Clean Maven cache
mvn clean

# Check Java version
java -version  # Must be 17+

# Check Maven version
mvn -version

# Try building manually
mvn clean package -DskipTests
```

### Issue: "Application won't start"

**Solution:**
```powershell
# Check if port 8080 is in use
netstat -ano | findstr :8080

# Kill process using port 8080
Stop-Process -Id <PID> -Force

# Check logs
Get-Content logs\deploy-*.log -Tail 100

# Verify MongoDB connection
# Test your MongoDB URI manually
```

### Issue: "Port already in use"

**Solution:**
```powershell
# Find process on port 8080
netstat -ano | findstr :8080

# Kill it
Stop-Process -Id <PID> -Force

# Or use different port
$env:SERVER_PORT=8081
.\deploy.ps1
```

---

## Advanced Usage

### CI/CD Integration

#### GitHub Actions Example
```yaml
name: Deploy to Production

on:
  push:
    branches: [main]

jobs:
  deploy:
    runs-on: windows-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Setup Java
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'
      
      - name: Setup Maven
        uses: stCarolas/setup-maven@v4.5
        with:
          maven-version: 3.9.5
      
      - name: Create .env file
        run: |
          echo "JWT_SECRET=${{ secrets.JWT_SECRET }}" > .env
          echo "SCIM_ADMIN_PASSWORD=${{ secrets.ADMIN_PASSWORD }}" >> .env
          echo "MONGODB_URI=${{ secrets.MONGODB_URI }}" >> .env
          echo "SPRING_PROFILES_ACTIVE=prod" >> .env
      
      - name: Deploy
        run: .\deploy.ps1 -Environment prod -Branch main
```

### Scheduled Deployment

```powershell
# Windows Task Scheduler - Deploy every day at 2 AM
$action = New-ScheduledTaskAction -Execute "PowerShell.exe" -Argument "-File D:\linux\P1\SCIM\scim-app\scim-app\deploy.ps1"
$trigger = New-ScheduledTaskTrigger -Daily -At 2am
Register-ScheduledTask -TaskName "SCIM Gateway Deploy" -Action $action -Trigger $trigger
```

---

## Monitoring After Deployment

### View Logs
```powershell
# Tail deployment log
Get-Content logs\deploy-*.log -Tail 50 -Wait

# Tail application log (if configured)
Get-Content logs\application.log -Tail 50 -Wait
```

### Check Health
```powershell
# Simple health check
curl http://localhost:8080/actuator/health

# Detailed check
Invoke-RestMethod http://localhost:8080/actuator/health | ConvertTo-Json -Depth 10
```

### Run Tests
```powershell
# Comprehensive API tests
.\test-comprehensive.ps1

# Production config test
.\test-production-config.ps1
```

---

## Best Practices

### Before Deployment
1. ✅ Always review changes in Git
2. ✅ Ensure `.env` file is configured
3. ✅ Backup database if major changes
4. ✅ Notify team before production deploy
5. ✅ Run tests locally first

### After Deployment
1. ✅ Verify health check passes
2. ✅ Run comprehensive tests
3. ✅ Check application logs
4. ✅ Monitor for 10-15 minutes
5. ✅ Test critical user flows

### Security
1. ✅ Never commit `.env` file
2. ✅ Use strong passwords
3. ✅ Rotate JWT secrets regularly
4. ✅ Use secrets manager in production
5. ✅ Enable HTTPS in production

---

## Script Parameters Reference

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `-Environment` | string | prod | Environment: dev, staging, prod |
| `-Branch` | string | main | Git branch to deploy |
| `-AutoRestart` | bool | true | Auto-stop running instance |
| `-SkipTests` | bool | true | Skip tests during build |

---

## Additional Scripts

- **deploy.ps1** - Automated deployment (this script)
- **setup-production.ps1** - Generate secure secrets
- **test-comprehensive.ps1** - Run all API tests
- **test-production-config.ps1** - Test production configuration

---

## Need Help?

- Check logs: `Get-Content logs\deploy-*.log -Tail 100`
- Review docs: See PRODUCTION-DEPLOYMENT-SUMMARY.md
- Test APIs: Run test-comprehensive.ps1
- Check config: Review application.properties

---

## Version History

| Version | Date | Changes |
|---------|------|---------|
| 1.0.0 | 2026-04-13 | Initial release with full automation |
