# 🚀 SCIM Gateway - Automated Deployment System

## Overview

Complete automated deployment system that fetches code from Git, configures environment variables, builds, and runs the SCIM Gateway application with zero manual intervention.

---

## ✨ Features

✅ **One-Command Deployment** - Deploy with a single command  
✅ **Automatic Git Pull** - Fetches latest code from repository  
✅ **Environment Configuration** - Loads secrets from `.env` file  
✅ **Smart Restart** - Detects and stops running instances  
✅ **Build Automation** - Compiles with Maven, skips tests option  
✅ **Health Checks** - Verifies application is running  
✅ **Detailed Logging** - All actions logged with timestamps  
✅ **Multi-Environment** - Support for dev, staging, prod  
✅ **Rollback Ready** - Git-based deployment for easy rollback  

---

## 📁 Files Created

| File | Purpose |
|------|---------|
| **deploy.ps1** | Main deployment script (PowerShell) |
| **deploy.cmd** | Windows CMD wrapper for easy execution |
| **.env.example** | Template for environment variables |
| **test-deploy.ps1** | Test script to verify setup |
| **AUTOMATED-DEPLOYMENT-GUIDE.md** | Complete documentation |
| **DEPLOYMENT-README.md** | This file |

---

## 🎯 Quick Start (3 Steps)

### Step 1: Create Environment File

```powershell
# Copy the template
cp .env.example .env

# Edit with your values
notepad .env
```

### Step 2: Configure Secrets

Open `.env` and fill in:

```bash
# Security
JWT_SECRET=cPVO8XANn5o1KOYZCKLtrDQEF8pGRb6madUSE9rB6lA=
SCIM_ADMIN_PASSWORD=tLmmIYs#pdaBD8RE9CfN

# Database
MONGODB_URI=mongodb+srv://anurag:cloud%40123@cluster0.nivrt0z.mongodb.net/scimdb?retryWrites=true&w=majority

# Profile
SPRING_PROFILES_ACTIVE=prod
```

**Need to generate secrets?** Run:
```powershell
.\setup-production.ps1
```

### Step 3: Deploy!

```powershell
# PowerShell
.\deploy.ps1

# OR from CMD
deploy.cmd
```

**That's it!** The script will:
1. Pull latest code from Git
2. Load your environment variables
3. Stop any running instance
4. Build the application
5. Start with production settings
6. Verify it's running

---

## 📖 Usage Examples

### Basic Deployment

```powershell
# Deploy to production (default)
.\deploy.ps1
```

### Different Environments

```powershell
# Development
.\deploy.ps1 -Environment dev

# Staging
.\deploy.ps1 -Environment staging

# Production
.\deploy.ps1 -Environment prod
```

### Different Branches

```powershell
# Main branch (default)
.\deploy.ps1 -Branch main

# Develop branch
.\deploy.ps1 -Branch develop

# Feature branch
.\deploy.ps1 -Branch feature/new-feature
```

### Advanced Options

```powershell
# Run tests during build
.\deploy.ps1 -SkipTests false

# Don't auto-restart if running
.\deploy.ps1 -AutoRestart false

# Combine options
.\deploy.ps1 -Environment dev -Branch develop -SkipTests false
```

### From Command Prompt

```cmd
# Simple deployment
deploy.cmd

# With parameters
deploy.cmd -Environment dev -Branch develop
```

---

## 🔧 What Happens During Deployment

### Step 1: Check Prerequisites ✓
- Verifies Git, Maven, and Java are installed
- Shows version information

### Step 2: Fetch Latest Code ✓
- Stashes any local changes
- Fetches from remote repository
- Checks out specified branch
- Pulls latest changes
- Shows commit log

### Step 3: Configure Environment ✓
- Loads `.env` file if exists
- Sets `SPRING_PROFILES_ACTIVE`
- Validates critical variables (JWT_SECRET, SCIM_ADMIN_PASSWORD, MONGODB_URI)
- Warns about missing values

### Step 4: Stop Running App ✓
- Detects if application is already running
- Attempts graceful shutdown via `/actuator/shutdown`
- Force kills if graceful shutdown fails
- Verifies application is stopped

### Step 5: Build Application ✓
- Runs `mvn clean package`
- Skips tests by default (faster)
- Shows JAR file size
- Reports any build errors

### Step 6: Verify Configuration ✓
- Checks application.properties exists
- Verifies environment-specific config
- Reports any missing files

### Step 7: Start Application ✓
- Starts with correct Spring profile
- Waits for startup (60 second timeout)
- Checks if port 8080 is listening
- Runs health check
- Shows access URLs

---

## 📊 Sample Output

```
=========================================
  SCIM Gateway - Automated Deployment
=========================================
Environment : prod
Branch      : main
Auto Restart: true
Skip Tests  : true

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
```

---

## 🔐 Environment Variables

### Required Variables

| Variable | Description | Example |
|----------|-------------|---------|
| `JWT_SECRET` | JWT signing key (32+ chars) | `cPVO8XANn5o1KOYZCKLtrDQEF8pGRb6madUSE9rB6lA=` |
| `SCIM_ADMIN_PASSWORD` | Admin password (16+ chars) | `tLmmIYs#pdaBD8RE9CfN` |
| `MONGODB_URI` | MongoDB connection string | `mongodb+srv://user:pass@cluster.mongodb.net/scimdb` |

### Optional Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `SPRING_PROFILES_ACTIVE` | Active profile | `prod` |
| `SERVER_PORT` | Server port | `8080` |
| `JWT_EXPIRATION` | Token expiration (ms) | `86400000` |

### Generate Secrets

```powershell
# JWT Secret
openssl rand -base64 32

# Admin Password (PowerShell)
-join ((33..126) | Get-Random -Count 20 | ForEach-Object {[char]$_})

# Or use the setup script
.\setup-production.ps1
```

---

## 🧪 Testing the Setup

Before deploying, test your configuration:

```powershell
# Test deployment script setup
.\test-deploy.ps1

# Test production configuration
.\test-production-config.ps1

# Run comprehensive API tests
.\test-comprehensive.ps1
```

---

## 📝 Logs

All deployments are logged with timestamps:

```
logs/
  deploy-20260413-123456.log
  deploy-20260413-124500.log
  deploy-20260413-130000.log
```

### View Logs

```powershell
# Latest deployment log
Get-Content logs\deploy-*.log -Tail 100

# Follow logs in real-time
Get-Content logs\deploy-*.log -Tail 50 -Wait
```

---

## 🔍 Monitoring After Deployment

### Check Health

```powershell
# Simple health check
curl http://localhost:8080/actuator/health

# Detailed health
Invoke-RestMethod http://localhost:8080/actuator/health | ConvertTo-Json
```

### Check Application

```powershell
# Get info
Invoke-RestMethod http://localhost:8080/api/info

# List users (requires auth)
$token = (Invoke-RestMethod http://localhost:8080/auth/login -Method POST -Body '{"username":"admin","password":"your-password"}' -ContentType 'application/json').token
Invoke-RestMethod http://localhost:8080/scim/v2/Users -Headers @{Authorization="Bearer $token"}
```

---

## 🛠️ Troubleshooting

### Issue: "Missing critical environment variables"

**Solution:**
```powershell
# Check if .env exists
ls .env

# Create from template
cp .env.example .env

# Edit with your values
notepad .env
```

### Issue: "Build failed"

**Solution:**
```powershell
# Check Java version
java -version  # Must be 17+

# Check Maven
mvn -version

# Try manual build
mvn clean package -DskipTests

# Check for compilation errors
mvn compile
```

### Issue: "Application won't start"

**Solution:**
```powershell
# Check if port 8080 is in use
netstat -ano | findstr :8080

# Kill process on port 8080
Stop-Process -Id <PID> -Force

# Check deployment logs
Get-Content logs\deploy-*.log -Tail 100

# Verify MongoDB connection
# Test your MongoDB URI in MongoDB Compass or mongosh
```

### Issue: "Port already in use"

**Solution:**
```powershell
# Find and kill process
netstat -ano | findstr :8080
Stop-Process -Id <PID> -Force

# Or deploy to different port
$env:SERVER_PORT=8081
.\deploy.ps1
```

### Issue: "Git pull failed"

**Solution:**
```powershell
# Check Git status
git status

# Stash local changes
git stash

# Try again
.\deploy.ps1
```

---

## 🔄 CI/CD Integration

### GitHub Actions

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
      
      - name: Create .env file
        run: |
          echo "JWT_SECRET=${{ secrets.JWT_SECRET }}" > .env
          echo "SCIM_ADMIN_PASSWORD=${{ secrets.ADMIN_PASSWORD }}" >> .env
          echo "MONGODB_URI=${{ secrets.MONGODB_URI }}" >> .env
          echo "SPRING_PROFILES_ACTIVE=prod" >> .env
      
      - name: Deploy
        run: .\deploy.ps1 -Environment prod -Branch main
```

### Azure DevOps

```yaml
trigger:
  - main

pool:
  vmImage: 'windows-latest'

steps:
  - task: JavaToolInstaller@0
    inputs:
      versionSpec: '17'
      jdkArchitectureOption: 'x64'
      jdkSourceOption: 'PreInstalled'

  - task: Maven@3
    inputs:
      mavenPomFile: 'pom.xml'
      goals: 'clean package'
      options: '-DskipTests'

  - powershell: |
      .\deploy.ps1 -Environment prod -Branch main
    displayName: 'Deploy Application'
```

---

## 📚 Documentation

| Document | Purpose |
|----------|---------|
| **[AUTOMATED-DEPLOYMENT-GUIDE.md](AUTOMATED-DEPLOYMENT-GUIDE.md)** | Complete deployment guide |
| **[PRODUCTION-DEPLOYMENT-SUMMARY.md](PRODUCTION-DEPLOYMENT-SUMMARY.md)** | Production setup summary |
| **[PRODUCTION-CONFIG-REVIEW.md](PRODUCTION-CONFIG-REVIEW.md)** | Security checklist |
| **[IDP-INTEGRATION-GUIDE.md](IDP-INTEGRATION-GUIDE.md)** | IdP integration |
| **[SCIM-GATEWAY-DOCUMENTATION.md](SCIM-GATEWAY-DOCUMENTATION.md)** | Full API documentation |

---

## 🎯 Best Practices

### Before Deployment
1. ✅ Review Git changes before deploying
2. ✅ Ensure `.env` file is configured correctly
3. ✅ Backup database for major changes
4. ✅ Notify team before production deployment
5. ✅ Run tests locally: `.\test-comprehensive.ps1`

### After Deployment
1. ✅ Verify health check: `curl http://localhost:8080/actuator/health`
2. ✅ Run API tests: `.\test-comprehensive.ps1`
3. ✅ Check logs for errors
4. ✅ Monitor for 10-15 minutes
5. ✅ Test critical user flows

### Security
1. ✅ Never commit `.env` to Git
2. ✅ Use strong passwords (16+ characters)
3. ✅ Rotate JWT secrets every 90 days
4. ✅ Use secrets manager in production
5. ✅ Enable HTTPS in production

---

## 🚀 Advanced Usage

### Scheduled Deployment

```powershell
# Deploy every day at 2 AM (Windows Task Scheduler)
$action = New-ScheduledTaskAction -Execute "PowerShell.exe" -Argument "-File D:\linux\P1\SCIM\scim-app\scim-app\deploy.ps1"
$trigger = New-ScheduledTaskTrigger -Daily -At 2am
Register-ScheduledTask -TaskName "SCIM Gateway Deploy" -Action $action -Trigger $trigger
```

### Multi-Server Deployment

```powershell
# Deploy to multiple servers
$servers = @("server1", "server2", "server3")
foreach ($server in $servers) {
    Invoke-Command -ComputerName $server -ScriptBlock {
        cd "D:\scim-app"
        .\deploy.ps1 -Environment prod
    }
}
```

### Custom Java Options

Edit `deploy.ps1` and modify the Java startup arguments:

```powershell
$startInfo.Arguments = "-Xmx512m -Xms256m -jar `"$global:JarPath`" --spring.profiles.active=$Environment"
```

---

## 📞 Support

### Common Commands

```powershell
# View deployment logs
Get-Content logs\deploy-*.log -Tail 100

# Check application status
curl http://localhost:8080/actuator/health

# Stop application
Stop-Process -Name java -Force

# Test all APIs
.\test-comprehensive.ps1

# Regenerate secrets
.\setup-production.ps1
```

### Getting Help

1. Check deployment logs: `logs\deploy-*.log`
2. Review troubleshooting section above
3. Check application logs in console
4. Run tests: `.\test-comprehensive.ps1`
5. Review documentation in this directory

---

## 🎉 You're All Set!

Your automated deployment system is ready. Just:

1. Create `.env` file with your secrets
2. Run `.\deploy.ps1`
3. Watch it work! 🚀

**Happy Deploying!** 🎊
