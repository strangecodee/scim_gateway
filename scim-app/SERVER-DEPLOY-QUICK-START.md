# 🚀 Server Deployment - Quick Reference

## YES! Upload JAR + .env and Run!

You can absolutely upload the JAR and `.env` file to your server and run it with a single command!

---

## 📦 What to Upload (5 Files)

1. **scim-app-0.0.1-SNAPSHOT.jar** - The application
2. **.env** - Your configuration
3. **server-start.sh** - Start script
4. **server-stop.sh** - Stop script  
5. **server-status.sh** - Status check

---

## ⚡ Quick Deploy (3 Steps)

### Step 1: Build JAR (Windows)

```powershell
cd d:\linux\P1\SCIM\scim-app\scim-app
mvn clean package -DskipTests
```

**JAR location:** `target/scim-app-0.0.1-SNAPSHOT.jar`

### Step 2: Upload to Server

#### Using WinSCP (Easiest)
1. Download WinSCP: https://winscp.net
2. Connect to your server
3. Create folder: `/opt/scim-gateway`
4. Upload these 5 files:
   - `target/scim-app-0.0.1-SNAPSHOT.jar`
   - `.env`
   - `server-start.sh`
   - `server-stop.sh`
   - `server-status.sh`

#### Using SCP (Command Line)
```bash
scp target/scim-app-0.0.1-SNAPSHOT.jar user@your-server:/opt/scim-gateway/
scp .env user@your-server:/opt/scim-gateway/
scp server-*.sh user@your-server:/opt/scim-gateway/
```

### Step 3: Run on Server

```bash
# SSH to server
ssh user@your-server.com

# Go to directory
cd /opt/scim-gateway

# Make scripts executable
chmod +x *.sh

# Start!
./server-start.sh
```

**Done!** Your application is running! 🎉

---

## 🎛️ Server Commands

```bash
# Start application
./server-start.sh

# Stop application
./server-stop.sh

# Check status
./server-status.sh

# View logs
tail -f scim-gateway.log

# Check health
curl http://localhost:8080/actuator/health
```

---

## 📁 Server File Structure

```
/opt/scim-gateway/
├── scim-app-0.0.1-SNAPSHOT.jar   ← Your JAR
├── .env                           ← Your config
├── server-start.sh                ← Start script
├── server-stop.sh                 ← Stop script
├── server-status.sh               ← Status script
├── scim-gateway.log               ← Auto-created log
└── scim-gateway.pid               ← Auto-created PID file
```

---

## 🔧 Configure .env on Server

```bash
# Edit .env
nano .env
```

**Required values:**
```bash
JWT_SECRET=your-secret-here
SCIM_ADMIN_PASSWORD=your-password-here
MONGODB_URI=mongodb+srv://user:pass@cluster.mongodb.net/scimdb
SPRING_PROFILES_ACTIVE=prod
```

---

## 🎯 Automated Packaging (Optional)

Want to create a deployment package automatically?

```powershell
# On Windows
.\package-for-server.ps1
```

This creates a ZIP file with everything ready to upload!

**Output:** `scim-gateway-deploy-20260413-163000.zip`

---

## 📋 Server Requirements

| Requirement | Details |
|-------------|---------|
| **OS** | Linux (Ubuntu, CentOS, etc.) |
| **Java** | JDK 17+ |
| **RAM** | 512 MB minimum |
| **Port** | 8080 available |
| **Network** | MongoDB Atlas accessible |

### Install Java on Server

**Ubuntu:**
```bash
sudo apt update
sudo apt install -y openjdk-17-jdk
```

**CentOS:**
```bash
sudo yum install -y java-17-openjdk-devel
```

---

## 🔐 Security Checklist

- [ ] `.env` file permissions: `chmod 600 .env`
- [ ] Scripts executable: `chmod +x *.sh`
- [ ] Firewall configured: Port 8080 open
- [ ] MongoDB URI correct
- [ ] Strong JWT secret
- [ ] Strong admin password

---

## 📊 Verify Deployment

```bash
# 1. Check status
./server-status.sh

# 2. Check health
curl http://localhost:8080/actuator/health

# 3. Test login
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"your-password"}'

# 4. Access Swagger UI
# http://your-server:8080/swagger-ui.html
```

---

## 🔄 Update Application

```bash
# 1. Upload new JAR
scp target/scim-app-0.0.2-SNAPSHOT.jar user@server:/opt/scim-gateway/

# 2. On server
cd /opt/scim-gateway

# 3. Stop old version
./server-stop.sh

# 4. Replace JAR
mv scim-app-0.0.1-SNAPSHOT.jar backup.jar
mv scim-app-0.0.2-SNAPSHOT.jar scim-app-0.0.1-SNAPSHOT.jar

# 5. Start new version
./server-start.sh
```

---

## 🐛 Troubleshooting

### "Java not found"
```bash
# Install Java
sudo apt install -y openjdk-17-jdk

# Verify
java -version
```

### "Port 8080 already in use"
```bash
# Find what's using port 8080
netstat -tuln | grep 8080

# Kill the process
kill -9 <PID>
```

### "Application won't start"
```bash
# Check logs
tail -f scim-gateway.log

# Check .env
cat .env

# Test manually
java -jar scim-app-*.jar --spring.profiles.active=prod
```

### "Can't connect to database"
```bash
# Test MongoDB connection
# Check your MONGODB_URI in .env
# Verify network access in MongoDB Atlas
```

---

## 📚 Full Documentation

- **[SERVER-DEPLOYMENT-GUIDE.md](SERVER-DEPLOYMENT-GUIDE.md)** - Complete guide with systemd, HTTPS, monitoring
- **[AUTOMATED-DEPLOYMENT-GUIDE.md](AUTOMATED-DEPLOYMENT-GUIDE.md)** - Windows automated deployment
- **[PRODUCTION-DEPLOYMENT-SUMMARY.md](PRODUCTION-DEPLOYMENT-SUMMARY.md)** - Production setup

---

## ✅ Deployment Checklist

- [ ] Java 17+ installed on server
- [ ] JAR file uploaded
- [ ] `.env` configured with production values
- [ ] Scripts uploaded and made executable
- [ ] Application started: `./server-start.sh`
- [ ] Health check passes
- [ ] Can access Swagger UI
- [ ] Logs showing no errors

---

## 🎉 Summary

**Yes, it's that simple!**

1. ✅ Build JAR on Windows
2. ✅ Upload JAR + `.env` + scripts to server
3. ✅ Run `./server-start.sh`

**Your SCIM Gateway is live!** 🚀

---

**Access your application:**
- URL: `http://your-server:8080`
- Swagger: `http://your-server:8080/swagger-ui.html`
- Health: `http://your-server:8080/actuator/health`
