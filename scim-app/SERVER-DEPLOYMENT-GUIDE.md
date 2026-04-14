# 🚀 Server Deployment Guide

## Overview

This guide shows you how to deploy SCIM Gateway on any Linux server by simply uploading the JAR file and `.env` configuration.

---

## 📦 What You Need to Upload

### Required Files (3 files)

1. **JAR File** - The compiled application
2. **.env** - Your configuration with secrets
3. **Server Scripts** - Start/stop/status scripts

### Option 1: Upload Pre-built Package

If you have the JAR file ready:

```
Upload these files to server:
├── scim-app-0.0.1-SNAPSHOT.jar    (The application)
├── .env                            (Your configuration)
├── server-start.sh                 (Start script)
├── server-stop.sh                  (Stop script)
└── server-status.sh                (Status script)
```

### Option 2: Build on Server

Upload source code and build on server:

```
Upload entire project:
├── src/
├── pom.xml
├── .env
├── server-start.sh
├── server-stop.sh
└── server-status.sh
```

---

## 🎯 Quick Start (3 Steps)

### Step 1: Build the JAR (Local Machine)

```bash
# On your Windows machine
cd d:\linux\P1\SCIM\scim-app\scim-app

# Build the JAR
mvn clean package -DskipTests

# JAR will be in: target/scim-app-0.0.1-SNAPSHOT.jar
```

### Step 2: Upload to Server

#### Using SCP (Linux/Mac)
```bash
# Create deployment directory on server
ssh user@your-server.com "mkdir -p /opt/scim-gateway"

# Upload JAR
scp target/scim-app-0.0.1-SNAPSHOT.jar user@your-server.com:/opt/scim-gateway/

# Upload .env
scp .env user@your-server.com:/opt/scim-gateway/

# Upload scripts
scp server-*.sh user@your-server.com:/opt/scim-gateway/
```

#### Using WinSCP (Windows)
1. Open WinSCP
2. Connect to your server
3. Navigate to `/opt/scim-gateway`
4. Upload files:
   - `scim-app-0.0.1-SNAPSHOT.jar`
   - `.env`
   - `server-start.sh`
   - `server-stop.sh`
   - `server-status.sh`

#### Using rsync
```bash
rsync -avz \
  target/scim-app-0.0.1-SNAPSHOT.jar \
  .env \
  server-*.sh \
  user@your-server.com:/opt/scim-gateway/
```

### Step 3: Start on Server

```bash
# SSH into server
ssh user@your-server.com

# Navigate to deployment directory
cd /opt/scim-gateway

# Make scripts executable
chmod +x *.sh

# Start the application
./server-start.sh
```

**That's it!** Your application is running! 🎉

---

## 📋 Detailed Server Setup

### Server Requirements

| Requirement | Minimum | Recommended |
|-------------|---------|-------------|
| **OS** | Ubuntu 20.04 / CentOS 8 | Ubuntu 22.04 LTS |
| **RAM** | 512 MB | 2 GB |
| **CPU** | 1 core | 2 cores |
| **Disk** | 500 MB | 2 GB |
| **Java** | JDK 17+ | JDK 17 LTS |
| **Network** | Port 8080 open | Port 443 (HTTPS) |

### Install Java on Server

#### Ubuntu/Debian
```bash
sudo apt update
sudo apt install -y openjdk-17-jdk
java -version
```

#### CentOS/RHEL
```bash
sudo yum install -y java-17-openjdk-devel
java -version
```

#### Amazon Linux 2
```bash
sudo amazon-linux-extras install java-openjdk17
java -version
```

---

## 🔧 Server Configuration

### 1. Create Deployment Directory

```bash
# Create directory
sudo mkdir -p /opt/scim-gateway
sudo chown $USER:$USER /opt/scim-gateway
cd /opt/scim-gateway
```

### 2. Upload Files

```bash
# From your local machine
scp target/scim-app-*.jar user@server:/opt/scim-gateway/
scp .env user@server:/opt/scim-gateway/
scp server-*.sh user@server:/opt/scim-gateway/
```

### 3. Configure .env

```bash
# On server
cd /opt/scim-gateway
nano .env
```

Update with your production values:

```bash
# Security
JWT_SECRET=your-production-jwt-secret-here
SCIM_ADMIN_PASSWORD=your-strong-password-here

# Database
MONGODB_URI=mongodb+srv://user:pass@cluster.mongodb.net/scimdb

# Profile
SPRING_PROFILES_ACTIVE=prod
```

### 4. Make Scripts Executable

```bash
chmod +x server-start.sh server-stop.sh server-status.sh
```

### 5. Start Application

```bash
./server-start.sh
```

---

## 📁 File Structure on Server

```
/opt/scim-gateway/
├── scim-app-0.0.1-SNAPSHOT.jar    # Application JAR
├── .env                            # Environment configuration
├── server-start.sh                 # Start script
├── server-stop.sh                  # Stop script
├── server-status.sh                # Status check script
├── scim-gateway.log                # Application log (auto-created)
└── scim-gateway.pid                # PID file (auto-created)
```

---

## 🎛️ Management Commands

### Start Application

```bash
cd /opt/scim-gateway
./server-start.sh
```

### Stop Application

```bash
./server-stop.sh
```

### Check Status

```bash
./server-status.sh
```

### View Logs

```bash
# View all logs
cat scim-gateway.log

# Follow logs in real-time
tail -f scim-gateway.log

# View last 100 lines
tail -n 100 scim-gateway.log

# Search for errors
grep -i error scim-gateway.log
```

### Restart Application

```bash
./server-stop.sh
./server-start.sh
```

---

## 🔐 Security Configuration

### 1. Set Correct Permissions

```bash
# Restrict .env file (contains secrets)
chmod 600 .env

# Scripts should be executable only by owner
chmod 700 server-*.sh

# JAR file readable
chmod 644 scim-app-*.jar
```

### 2. Create Dedicated User (Recommended)

```bash
# Create system user
sudo useradd -r -s /bin/false scimgateway

# Change ownership
sudo chown -R scimgateway:scimgateway /opt/scim-gateway

# Run as scimgateway user
sudo -u scimgateway ./server-start.sh
```

### 3. Configure Firewall

```bash
# Ubuntu (UFW)
sudo ufw allow 8080/tcp
sudo ufw status

# CentOS (firewalld)
sudo firewall-cmd --permanent --add-port=8080/tcp
sudo firewall-cmd --reload

# AWS Security Group
# Add inbound rule: TCP 8080 from your IP or 0.0.0.0/0
```

### 4. Enable HTTPS (Production)

#### Using Nginx as Reverse Proxy

```bash
# Install Nginx
sudo apt install nginx

# Create Nginx config
sudo nano /etc/nginx/sites-available/scim-gateway
```

```nginx
server {
    listen 80;
    server_name your-domain.com;

    # Redirect to HTTPS
    return 301 https://$server_name$request_uri;
}

server {
    listen 443 ssl http2;
    server_name your-domain.com;

    ssl_certificate /etc/letsencrypt/live/your-domain.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/your-domain.com/privkey.pem;

    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

```bash
# Enable site
sudo ln -s /etc/nginx/sites-available/scim-gateway /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl restart nginx

# Get SSL certificate (Let's Encrypt)
sudo apt install certbot python3-certbot-nginx
sudo certbot --nginx -d your-domain.com
```

---

## 🔄 Auto-Start on Boot

### Using systemd (Recommended)

Create service file:

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
ExecStop=/bin/kill -TERM $MAINPID
Restart=on-failure
RestartSec=10
StartLimitInterval=60
StartLimitBurst=3

# Security
NoNewPrivileges=true
PrivateTmp=true

# Logging
StandardOutput=journal
StandardError=journal
SyslogIdentifier=scim-gateway

[Install]
WantedBy=multi-user.target
```

Enable and start:

```bash
# Reload systemd
sudo systemctl daemon-reload

# Enable auto-start
sudo systemctl enable scim-gateway

# Start service
sudo systemctl start scim-gateway

# Check status
sudo systemctl status scim-gateway

# View logs
sudo journalctl -u scim-gateway -f
```

**Commands:**

```bash
# Start
sudo systemctl start scim-gateway

# Stop
sudo systemctl stop scim-gateway

# Restart
sudo systemctl restart scim-gateway

# Status
sudo systemctl status scim-gateway

# Logs
sudo journalctl -u scim-gateway -u scim-gateway -n 100 -f
```

---

## 📊 Monitoring

### Health Check

```bash
# Check health endpoint
curl http://localhost:8080/actuator/health

# Detailed health (with auth)
curl -u admin:password http://localhost:8080/actuator/health
```

### Setup Monitoring Script

```bash
# Create monitoring script
nano /opt/scim-gateway/monitor.sh
```

```bash
#!/bin/bash
# Health check monitor

LOG_FILE="/opt/scim-gateway/monitor.log"
URL="http://localhost:8080/actuator/health"

RESPONSE=$(curl -s -o /dev/null -w "%{http_code}" $URL)

if [ "$RESPONSE" = "200" ]; then
    echo "$(date): ✅ Healthy" >> $LOG_FILE
else
    echo "$(date): ❌ Unhealthy (HTTP $RESPONSE)" >> $LOG_FILE
    # Send alert email (configure mail)
    # echo "SCIM Gateway is down!" | mail -s "Alert" admin@example.com
fi
```

```bash
chmod +x monitor.sh

# Add to cron (every 5 minutes)
crontab -e
*/5 * * * * /opt/scim-gateway/monitor.sh
```

---

## 🔄 Update Deployment

### Zero-Downtime Update

```bash
# 1. Upload new JAR
scp target/scim-app-0.0.2-SNAPSHOT.jar user@server:/opt/scim-gateway/

# 2. SSH to server
ssh user@server
cd /opt/scim-gateway

# 3. Stop old version
./server-stop.sh

# 4. Backup old JAR
mv scim-app-0.0.1-SNAPSHOT.jar scim-app-0.0.1-SNAPSHOT.jar.backup

# 5. Rename new JAR
mv scim-app-0.0.2-SNAPSHOT.jar scim-app-0.0.1-SNAPSHOT.jar

# 6. Start new version
./server-start.sh

# 7. Verify
./server-status.sh
curl http://localhost:8080/actuator/health
```

### Using systemd

```bash
# Upload new JAR with different name
scp scim-app-0.0.2-SNAPSHOT.jar user@server:/opt/scim-gateway/app.jar

# Restart service (will use new JAR)
sudo systemctl restart scim-gateway

# Check status
sudo systemctl status scim-gateway
```

---

## 🐛 Troubleshooting

### Application Won't Start

```bash
# Check Java version
java -version

# Check if port 8080 is in use
netstat -tuln | grep 8080

# Check logs
tail -f scim-gateway.log

# Check .env file
cat .env

# Test manually
java -jar scim-app-*.jar --spring.profiles.active=prod
```

### High Memory Usage

```bash
# Check memory
free -h

# Check Java process
ps aux | grep java

# Limit JVM memory
JAVA_OPTS="-Xmx512m -Xms256m"
nohup java $JAVA_OPTS -jar app.jar &
```

### Database Connection Issues

```bash
# Test MongoDB connection
mongosh "your-mongodb-uri"

# Check network
ping cluster.mongodb.net

# Check firewall
sudo ufw status
```

### Permission Issues

```bash
# Fix permissions
sudo chown -R $USER:$USER /opt/scim-gateway
chmod 600 .env
chmod +x *.sh
```

---

## 📝 Complete Deployment Example

```bash
# ===============================
# ON YOUR LOCAL MACHINE
# ===============================

# Build the application
cd d:\linux\P1\SCIM\scim-app\scim-app
mvn clean package -DskipTests

# Upload to server
scp target/scim-app-0.0.1-SNAPSHOT.jar root@192.168.1.100:/opt/scim-gateway/
scp .env root@192.168.1.100:/opt/scim-gateway/
scp server-*.sh root@192.168.1.100:/opt/scim-gateway/

# ===============================
# ON THE SERVER (SSH)
# ===============================

ssh root@192.168.1.100

# Navigate to deployment directory
cd /opt/scim-gateway

# Make scripts executable
chmod +x *.sh

# Start the application
./server-start.sh

# Check status
./server-status.sh

# View logs
tail -f scim-gateway.log
```

---

## ✅ Verification Checklist

After deployment, verify:

- [ ] Application is running: `./server-status.sh`
- [ ] Health check passes: `curl http://localhost:8080/actuator/health`
- [ ] Can login: `curl -X POST http://localhost:8080/auth/login ...`
- [ ] Swagger UI accessible: `http://your-server:8080/swagger-ui.html`
- [ ] Logs are being written: `tail scim-gateway.log`
- [ ] No errors in logs: `grep -i error scim-gateway.log`
- [ ] Firewall configured: Port 8080 open
- [ ] .env file secured: `chmod 600 .env`

---

## 📞 Quick Reference

| Task | Command |
|------|---------|
| **Start** | `./server-start.sh` |
| **Stop** | `./server-stop.sh` |
| **Status** | `./server-status.sh` |
| **Logs** | `tail -f scim-gateway.log` |
| **Health** | `curl http://localhost:8080/actuator/health` |
| **Restart** | `./server-stop.sh && ./server-start.sh` |
| **Update** | Upload new JAR, stop, replace, start |

---

## 🎉 You're Done!

Your SCIM Gateway is now running on the server!

**Access URLs:**
- Application: `http://your-server:8080`
- Swagger UI: `http://your-server:8080/swagger-ui.html`
- Health Check: `http://your-server:8080/actuator/health`

**Next Steps:**
1. ✅ Configure HTTPS
2. ✅ Set up auto-start with systemd
3. ✅ Configure monitoring
4. ✅ Set up backups
5. ✅ Connect your IdP

---

**Happy Deploying!** 🚀
