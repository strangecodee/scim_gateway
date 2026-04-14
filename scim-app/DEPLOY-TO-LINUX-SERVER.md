# 🚀 Deploy SCIM Gateway to Linux Server - Port 8181

## ✅ Build Complete!

**JAR File:** `scim-app-0.0.1-SNAPSHOT.jar` (37.25 MB)  
**Archive:** `scim-gateway-server-deploy.zip` (33.57 MB)  
**Port:** 8181  

---

## 📦 Files Ready for Upload

All files are in: `deploy-package/`

```
deploy-package/
├── scim-app-0.0.1-SNAPSHOT.jar   (37.25 MB)
├── .env                           (0.6 KB)
├── server-start.sh                (5.0 KB)
├── server-status.sh               (2.4 KB)
└── server-stop.sh                 (1.6 KB)
```

**Archive:** `scim-gateway-server-deploy.zip` (33.57 MB)

---

## 🎯 Deploy to Linux Server (3 Steps)

### Step 1: Upload to Server

Choose ONE of these methods:

#### Method A: Using WinSCP (Easiest - Windows)

1. **Download WinSCP**: https://winscp.net
2. **Connect to your server**:
   - Host: Your server IP (e.g., 192.168.1.100)
   - Username: your username
   - Password: your password
   - Port: 22 (SSH)

3. **Create deployment directory**:
   - Navigate to `/opt`
   - Create folder: `scim-gateway`

4. **Upload files**:
   - Upload ALL files from `deploy-package/` to `/opt/scim-gateway/`

#### Method B: Using SCP (Command Line)

```bash
# Create directory on server
ssh user@your-server.com "mkdir -p /opt/scim-gateway"

# Upload entire package
scp -r deploy-package/* user@your-server.com:/opt/scim-gateway/
```

**Or upload the ZIP:**

```bash
# Upload ZIP
scp scim-gateway-server-deploy.zip user@your-server.com:/opt/

# SSH to server and extract
ssh user@your-server.com
cd /opt
unzip scim-gateway-server-deploy.zip -d scim-gateway
cd scim-gateway
```

#### Method C: Using rsync

```bash
rsync -avz deploy-package/ user@your-server.com:/opt/scim-gateway/
```

---

### Step 2: Configure on Server

SSH into your server:

```bash
ssh user@your-server.com
cd /opt/scim-gateway
```

#### 2.1: Make Scripts Executable

```bash
chmod +x *.sh
```

#### 2.2: Verify .env Configuration

```bash
cat .env
```

**Expected content:**
```bash
JWT_SECRET=cPVO8XANn5o1KOYZCKLtrDQEF8pGRb6madUSE9rB6lA=
SCIM_ADMIN_PASSWORD=admin123
MONGODB_URI=mongodb+srv://anurag:cloud%40123@cluster0.nivrt0z.mongodb.net/scimdb?retryWrites=true&w=majority
SPRING_PROFILES_ACTIVE=dev
```

⚠️ **For production, update:**
- Change `JWT_SECRET` to a new secure value
- Change `SCIM_ADMIN_PASSWORD` to a strong password
- Change `SPRING_PROFILES_ACTIVE` to `prod`

#### 2.3: Verify Java Installation

Your server has **JDK 21** - Perfect! ✅

JDK 21 is the latest LTS version and fully compatible with this application (built for Java 17).

```bash
# Verify Java version
java -version
```

**Expected output:**
```
openjdk version "21.x.x"
OpenJDK Runtime Environment ...
OpenJDK 64-Bit Server VM ...
```

✅ **No need to install Java - you already have JDK 21!**

---

### Step 3: Start Application

```bash
# Start the application
./server-start.sh
```

**Expected output:**
```
=========================================
  SCIM Gateway - Server Startup
=========================================

[1/4] Loading environment variables...
[OK] Environment loaded

[2/4] Verifying configuration...
[OK] All required variables set

[3/4] Finding application JAR...
[OK] Found: ./scim-app-0.0.1-SNAPSHOT.jar

[4/4] Checking for running instances...

=========================================
  Starting SCIM Gateway
=========================================
Profile: dev
JAR: ./scim-app-0.0.1-SNAPSHOT.jar
Log: scim-gateway.log
PID File: scim-gateway.pid
=========================================

[OK] Application started (PID: 12345)

Waiting for application to start...
  Waiting... (2/60 seconds)

=========================================
  Application Started Successfully!
=========================================
URL: http://localhost:8181
Swagger: http://localhost:8181/swagger-ui.html
Health: http://localhost:8181/actuator/health
PID: 12345
Log: scim-gateway.log
=========================================
```

---

## ✅ Verify Deployment

### Check Status

```bash
./server-status.sh
```

### Check Health

```bash
curl http://localhost:8181/actuator/health
```

**Expected response:**
```json
{"status":"UP"}
```

### Test Login

```bash
curl -X POST http://localhost:8181/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

**Expected response:**
```json
{
  "token": "eyJhbGciOiJIUzM4NCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 86400000,
  "username": "admin"
}
```

### Access Swagger UI

Open in browser:
```
http://your-server-ip:8181/swagger-ui.html
```

**Example:** `http://192.168.1.100:8181/swagger-ui.html`

---

## 🎛️ Server Management Commands

```bash
# Start application
./server-start.sh

# Stop application
./server-stop.sh

# Check status
./server-status.sh

# View logs (real-time)
tail -f scim-gateway.log

# View last 100 log lines
tail -n 100 scim-gateway.log

# Search for errors in logs
grep -i error scim-gateway.log

# Restart application
./server-stop.sh
./server-start.sh
```

---

## 🔧 Firewall Configuration

### Ubuntu (UFW)

```bash
sudo ufw allow 8181/tcp
sudo ufw status
```

### CentOS (firewalld)

```bash
sudo firewall-cmd --permanent --add-port=8181/tcp
sudo firewall-cmd --reload
```

### AWS Security Group

1. Go to EC2 Console → Security Groups
2. Add Inbound Rule:
   - Type: Custom TCP
   - Port: 8181
   - Source: 0.0.0.0/0 (or your IP)

### Verify Port is Open

```bash
# From server
netstat -tuln | grep 8181

# From your local machine
telnet your-server-ip 8181
```

---

## 📊 Monitor Application

### Check Process

```bash
# Find Java process
ps aux | grep scim-app

# Check memory usage
ps -p $(cat scim-gateway.pid) -o pid,vsz,rss,%cpu,%mem,etime,cmd
```

### View Logs

```bash
# Real-time logs
tail -f scim-gateway.log

# Last 50 lines
tail -n 50 scim-gateway.log

# Search for specific text
grep "ERROR" scim-gateway.log
```

### Health Monitoring

```bash
# Simple health check
curl -s http://localhost:8181/actuator/health

# Create monitoring script
cat > monitor.sh << 'EOF'
#!/bin/bash
RESPONSE=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8181/actuator/health)
if [ "$RESPONSE" = "200" ]; then
    echo "$(date): ✅ Healthy"
else
    echo "$(date): ❌ Unhealthy (HTTP $RESPONSE)"
fi
EOF

chmod +x monitor.sh
./monitor.sh
```

---

## 🔄 Update Application

When you have a new version:

### 1. Build New JAR (Windows)

```powershell
cd d:\linux\P1\SCIM\scim-app\scim-app
mvn clean package -DskipTests
```

### 2. Upload New JAR

```bash
scp target/scim-app-0.0.1-SNAPSHOT.jar user@server:/opt/scim-gateway/scim-app-0.0.1-SNAPSHOT.jar.new
```

### 3. Update on Server

```bash
ssh user@server
cd /opt/scim-gateway

# Stop current version
./server-stop.sh

# Backup old JAR
mv scim-app-0.0.1-SNAPSHOT.jar scim-app-0.0.1-SNAPSHOT.jar.backup

# Use new JAR
mv scim-app-0.0.1-SNAPSHOT.jar.new scim-app-0.0.1-SNAPSHOT.jar

# Start new version
./server-start.sh

# Verify
./server-status.sh
curl http://localhost:8181/actuator/health
```

---

## 🐛 Troubleshooting

### Issue: "Java not found"

```bash
# Check if Java is installed
which java
java -version

# Install Java
sudo apt install -y openjdk-17-jdk  # Ubuntu
sudo yum install -y java-17-openjdk-devel  # CentOS
```

### Issue: "Port 8181 already in use"

```bash
# Find what's using port 8181
netstat -tuln | grep 8181
sudo lsof -i :8181

# Kill the process
sudo kill -9 <PID>

# Or change port in application.properties
```

### Issue: "Application won't start"

```bash
# Check logs
tail -f scim-gateway.log

# Check .env file
cat .env

# Check if JAR exists
ls -lh scim-app-*.jar

# Try manual start
java -jar scim-app-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev
```

### Issue: "Can't connect to MongoDB"

```bash
# Test MongoDB connection
# Check MONGODB_URI in .env
cat .env | grep MONGODB_URI

# Test network connectivity
ping cluster0.nivrt0z.mongodb.net

# Check MongoDB Atlas IP whitelist
# Add your server IP in Atlas console
```

### Issue: "Permission denied"

```bash
# Fix permissions
chmod +x *.sh
chmod 600 .env
chmod 644 scim-app-*.jar

# Or run as root (not recommended)
sudo ./server-start.sh
```

---

## 🔐 Production Hardening

### 1. Secure .env File

```bash
chmod 600 .env
chown root:root .env
```

### 2. Create Dedicated User

```bash
# Create system user
sudo useradd -r -s /bin/false scimgateway

# Change ownership
sudo chown -R scimgateway:scimgateway /opt/scim-gateway

# Run as scimgateway
sudo -u scimgateway ./server-start.sh
```

### 3. Auto-Start on Boot (systemd)

```bash
sudo nano /etc/systemd/system/scim-gateway.service
```

```ini
[Unit]
Description=SCIM Gateway Application
After=network.target

[Service]
Type=simple
User=scimgateway
WorkingDirectory=/opt/scim-gateway
EnvironmentFile=/opt/scim-gateway/.env
ExecStart=/usr/bin/java -jar /opt/scim-gateway/scim-app-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
Restart=on-failure
RestartSec=10

[Install]
WantedBy=multi-user.target
```

```bash
# Enable and start
sudo systemctl daemon-reload
sudo systemctl enable scim-gateway
sudo systemctl start scim-gateway
sudo systemctl status scim-gateway
```

### 4. Enable HTTPS (Nginx Reverse Proxy)

```bash
sudo apt install nginx
sudo nano /etc/nginx/sites-available/scim-gateway
```

```nginx
server {
    listen 80;
    server_name your-domain.com;
    return 301 https://$server_name$request_uri;
}

server {
    listen 443 ssl http2;
    server_name your-domain.com;

    ssl_certificate /etc/letsencrypt/live/your-domain.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/your-domain.com/privkey.pem;

    location / {
        proxy_pass http://localhost:8181;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }
}
```

```bash
sudo ln -s /etc/nginx/sites-available/scim-gateway /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl restart nginx
```

---

## 📋 Deployment Checklist

- [ ] Java 17+ installed on server
- [ ] Files uploaded to `/opt/scim-gateway/`
- [ ] Scripts made executable: `chmod +x *.sh`
- [ ] `.env` configured with correct values
- [ ] Firewall port 8181 opened
- [ ] Application started: `./server-start.sh`
- [ ] Health check passes: `curl http://localhost:8181/actuator/health`
- [ ] Swagger UI accessible: `http://server-ip:8181/swagger-ui.html`
- [ ] Login works with admin credentials
- [ ] Logs show no errors: `tail -f scim-gateway.log`

---

## 🎉 Success!

Your SCIM Gateway is now running on Linux server!

**Access URLs:**
- Application: `http://your-server-ip:8181`
- Swagger UI: `http://your-server-ip:8181/swagger-ui.html`
- Health Check: `http://your-server-ip:8181/actuator/health`

**Default Credentials:**
- Username: `admin`
- Password: `admin123` (change in production!)

---

## 📞 Quick Reference

| Task | Command |
|------|---------|
| **Start** | `./server-start.sh` |
| **Stop** | `./server-stop.sh` |
| **Status** | `./server-status.sh` |
| **Logs** | `tail -f scim-gateway.log` |
| **Health** | `curl http://localhost:8181/actuator/health` |
| **Restart** | `./server-stop.sh && ./server-start.sh` |

---

**Need help?** Check logs: `tail -f scim-gateway.log`
