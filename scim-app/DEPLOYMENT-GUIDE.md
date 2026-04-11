# On-Premises Deployment Guide

## 📋 Table of Contents

1. [Prerequisites](#prerequisites)
2. [Server Requirements](#server-requirements)
3. [Deployment Options](#deployment-options)
4. [Option 1: JAR Deployment](#option-1-jar-deployment)
5. [Option 2: Docker Deployment](#option-2-docker-deployment)
6. [Option 3: Systemd Service](#option-3-systemd-service-linux)
7. [Database Setup](#database-setup)
8. [Configuration](#configuration)
9. [SSL/HTTPS Setup](#sslhttps-setup)
10. [Reverse Proxy (Nginx)](#reverse-proxy-nginx)
11. [Monitoring & Logging](#monitoring--logging)
12. [Backup & Recovery](#backup--recovery)
13. [Troubleshooting](#troubleshooting)

---

## Prerequisites

### Required Software

- **Java**: JDK 21.0.10 or higher
- **Build Tool**: Maven 3.8+
- **Database**: MongoDB 6.0+ (local or Atlas)
- **OS**: Linux (Ubuntu 20.04+, CentOS 8+), Windows Server 2019+, or macOS

### Optional Software

- **Docker**: 20.10+ (for containerized deployment)
- **Nginx**: 1.18+ (for reverse proxy)
- **Systemd**: For service management (Linux)

---

## Server Requirements

### Minimum Requirements

```
CPU: 2 cores
RAM: 4 GB
Disk: 20 GB
Network: 100 Mbps
```

### Recommended Requirements

```
CPU: 4 cores
RAM: 8 GB
Disk: 50 GB SSD
Network: 1 Gbps
```

---

## Deployment Options

Choose the deployment method that best fits your infrastructure:

| Method | Best For | Complexity |
|--------|----------|------------|
| **JAR File** | Simple deployments, testing | Easy |
| **Docker** | Containerized environments, scaling | Medium |
| **Systemd Service** | Production Linux servers | Medium |

---

## Option 1: JAR Deployment

### Step 1: Build the Application

```bash
# On your development machine
cd d:\linux\P1\SCIM\scim-app\scim-app

# Clean and package (skip tests for faster build)
mvn clean package -DskipTests

# The JAR will be in target/ directory
ls -lh target/*.jar
```

### Step 2: Transfer to Server

```bash
# Linux/Mac (SCP)
scp target/scim-app-*.jar user@your-server:/opt/scim-gateway/

# Windows (PowerShell)
scp target\scim-app-*.jar user@your-server:/opt/scim-gateway/

# Or use SFTP/FTP tools like FileZilla
```

### Step 3: Install Java on Server

```bash
# Ubuntu/Debian
sudo apt update
sudo apt install -y openjdk-21-jdk

# CentOS/RHEL
sudo yum install -y java-21-openjdk-devel

# Verify installation
java -version
# Should show: openjdk version "21.0.10"
```

### Step 4: Create Configuration

```bash
# Create directory structure
sudo mkdir -p /opt/scim-gateway
sudo mkdir -p /opt/scim-gateway/config
sudo mkdir -p /opt/scim-gateway/logs

# Create environment file
sudo nano /opt/scim-gateway/.env
```

Add your configuration:

```bash
# /opt/scim-gateway/.env

# Database
MONGODB_URI=mongodb://localhost:27017/scimdb

# Authentication
AUTH_USERNAME=admin
AUTH_PASSWORD=YourSecurePassword123!
AUTH_ENABLED=true

# JWT
JWT_SECRET=$(openssl rand -base64 32)
JWT_EXPIRATION=86400000

# Server
SERVER_PORT=8080
SPRING_PROFILES_ACTIVE=prod

# Application
APP_NAME="SCIM Gateway"
APP_VERSION=1.0.0
APP_VENDOR="Your Company"
DOC_BASE_URL=https://your-company.com/docs
SUPPORT_EMAIL=support@your-company.com
```

### Step 5: Run the Application

```bash
cd /opt/scim-gateway

# Load environment variables
export $(cat .env | xargs)

# Run the application
java -jar scim-app-*.jar \
  --spring.data.mongodb.uri=$MONGODB_URI \
  --auth.default.username=$AUTH_USERNAME \
  --auth.default.password=$AUTH_PASSWORD \
  --jwt.secret=$JWT_SECRET \
  --server.port=$SERVER_PORT

# Or use the environment file directly
java -jar scim-app-*.jar
```

---

## Option 2: Docker Deployment

### Step 1: Create Dockerfile

Create `Dockerfile` in project root:

```dockerfile
# Build stage
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Run stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Add non-root user
RUN addgroup -g 1001 scimgateway && \
    adduser -D -u 1001 -G scimgateway scimgateway

# Copy JAR from builder
COPY --from=builder /app/target/*.jar app.jar

# Set permissions
RUN chown -R scimgateway:scimgateway /app

# Switch to non-root user
USER scimgateway

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Step 2: Create docker-compose.yml

```yaml
version: '3.8'

services:
  scim-gateway:
    build: .
    container_name: scim-gateway
    restart: unless-stopped
    ports:
      - "8080:8080"
    environment:
      - MONGODB_URI=${MONGODB_URI}
      - AUTH_USERNAME=${AUTH_USERNAME}
      - AUTH_PASSWORD=${AUTH_PASSWORD}
      - JWT_SECRET=${JWT_SECRET}
      - AUTH_ENABLED=${AUTH_ENABLED:-true}
      - SERVER_PORT=8080
      - SPRING_PROFILES_ACTIVE=prod
    env_file:
      - .env
    volumes:
      - ./logs:/app/logs
    networks:
      - scim-network
    healthcheck:
      test: ["CMD", "wget", "--no-verbose", "--tries=1", "--spider", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 40s

  mongodb:
    image: mongo:6.0
    container_name: scim-mongodb
    restart: unless-stopped
    ports:
      - "27017:27017"
    environment:
      - MONGO_INITDB_ROOT_USERNAME=${MONGO_USERNAME:-admin}
      - MONGO_INITDB_ROOT_PASSWORD=${MONGO_PASSWORD:-admin123}
      - MONGO_INITDB_DATABASE=scimdb
    volumes:
      - mongodb-data:/data/db
      - mongodb-config:/data/configdb
    networks:
      - scim-network

networks:
  scim-network:
    driver: bridge

volumes:
  mongodb-data:
  mongodb-config:
```

### Step 3: Build and Run

```bash
# Build Docker image
docker-compose build

# Start services
docker-compose up -d

# Check logs
docker-compose logs -f scim-gateway

# Verify running
docker-compose ps
```

### Step 4: Management Commands

```bash
# Stop services
docker-compose down

# Restart services
docker-compose restart

# View logs
docker-compose logs -f

# Update application
docker-compose pull
docker-compose up -d --build

# Backup MongoDB
docker exec scim-mongodb mongodump --out /data/backup
```

---

## Option 3: Systemd Service (Linux)

### Step 1: Create Service File

```bash
sudo nano /etc/systemd/system/scim-gateway.service
```

Add the following:

```ini
[Unit]
Description=SCIM Gateway Service
Documentation=https://your-company.com/docs
After=network.target mongodb.service
Wants=mongodb.service

[Service]
Type=simple
User=scimgateway
Group=scimgateway
WorkingDirectory=/opt/scim-gateway
EnvironmentFile=/opt/scim-gateway/.env
ExecStart=/usr/bin/java -jar /opt/scim-gateway/scim-app.jar \
  --spring.data.mongodb.uri=${MONGODB_URI} \
  --auth.default.username=${AUTH_USERNAME} \
  --auth.default.password=${AUTH_PASSWORD} \
  --jwt.secret=${JWT_SECRET} \
  --server.port=${SERVER_PORT}
SuccessExitStatus=143
Restart=on-failure
RestartSec=10
StandardOutput=journal
StandardError=journal
SyslogIdentifier=scim-gateway

# Security hardening
NoNewPrivileges=true
PrivateTmp=true
ProtectSystem=strict
ProtectHome=true
ReadWritePaths=/opt/scim-gateway/logs

[Install]
WantedBy=multi-user.target
```

### Step 2: Create User and Set Permissions

```bash
# Create dedicated user
sudo useradd --system --no-create-home --shell /bin/false scimgateway

# Set ownership
sudo chown -R scimgateway:scimgateway /opt/scim-gateway
sudo chmod 750 /opt/scim-gateway

# Create log directory
sudo mkdir -p /var/log/scim-gateway
sudo chown scimgateway:scimgateway /var/log/scim-gateway
```

### Step 3: Enable and Start Service

```bash
# Reload systemd
sudo systemctl daemon-reload

# Enable service (start on boot)
sudo systemctl enable scim-gateway

# Start service
sudo systemctl start scim-gateway

# Check status
sudo systemctl status scim-gateway

# View logs
sudo journalctl -u scim-gateway -f

# Follow logs in real-time
sudo journalctl -u scim-gateway -f --no-pager
```

### Step 4: Service Management

```bash
# Start
sudo systemctl start scim-gateway

# Stop
sudo systemctl stop scim-gateway

# Restart
sudo systemctl restart scim-gateway

# Reload configuration
sudo systemctl reload scim-gateway

# Check status
sudo systemctl status scim-gateway

# View logs (last 100 lines)
sudo journalctl -u scim-gateway -n 100

# View logs (since today)
sudo journalctl -u scim-gateway --since today
```

---

## Database Setup

### Option 1: Local MongoDB Installation

```bash
# Ubuntu/Debian
wget -qO - https://www.mongodb.org/static/pgp/server-6.0.asc | sudo apt-key add -
echo "deb [ arch=amd64,arm64 ] https://repo.mongodb.org/apt/ubuntu focal/mongodb-org/6.0 multiverse" | sudo tee /etc/apt/sources.list.d/mongodb-org-6.0.list
sudo apt update
sudo apt install -y mongodb-org
sudo systemctl enable mongod
sudo systemctl start mongod

# Verify
mongosh --eval "db.runCommand({ ping: 1 })"
```

### Option 2: MongoDB Atlas (Cloud)

1. Create account at https://www.mongodb.com/cloud/atlas
2. Create a cluster
3. Create database user
4. Get connection string
5. Update `MONGODB_URI` in `.env`

### Option 3: MongoDB on Separate Server

```bash
# On database server
sudo apt install -y mongodb-org

# Configure remote access
sudo nano /etc/mongod.conf

# Update bindIp
net:
  port: 27017
  bindIp: 0.0.0.0  # Allow remote connections

# Restart MongoDB
sudo systemctl restart mongod

# On application server
MONGODB_URI=mongodb://username:password@db-server-ip:27017/scimdb
```

---

## Configuration

### Production Environment File

Create `/opt/scim-gateway/.env`:

```bash
# ==========================================
# Database Configuration
# ==========================================
MONGODB_URI=mongodb://scimuser:SecurePass123@localhost:27017/scimdb?authSource=admin

# ==========================================
# Authentication (CHANGE THESE!)
# ==========================================
AUTH_USERNAME=scim-admin
AUTH_PASSWORD=$(openssl rand -base64 16)
AUTH_ENABLED=true

# ==========================================
# JWT Configuration
# ==========================================
JWT_SECRET=$(openssl rand -base64 32)
JWT_EXPIRATION=86400000

# ==========================================
# Server Configuration
# ==========================================
SERVER_PORT=8080
SPRING_PROFILES_ACTIVE=prod

# ==========================================
# Application Information
# ==========================================
APP_NAME="SCIM Gateway"
APP_VERSION=1.0.0
APP_VENDOR="Your Company Name"
DOC_BASE_URL=https://docs.yourcompany.com/scim-gateway
SUPPORT_EMAIL=support@yourcompany.com

# ==========================================
# Performance Tuning
# ==========================================
# JVM Options
JAVA_OPTS="-Xms2g -Xmx4g -XX:+UseG1GC -XX:MaxGCPauseMillis=200"

# Thread Pool
SPRING_TASK_SCHEDULING_POOL_SIZE=10
SPRING_TASK_EXECUTION_POOL_CORE_SIZE=10
SPRING_TASK_EXECUTION_POOL_MAX_SIZE=20

# ==========================================
# Logging
# ==========================================
LOG_LEVEL=INFO
LOG_FILE=/var/log/scim-gateway/application.log
LOG_MAX_SIZE=100MB
LOG_MAX_DAYS=30
```

### JVM Tuning

For production, optimize JVM settings:

```bash
# Edit startup script or systemd service
JAVA_OPTS="-Xms2g \                          # Initial heap size
           -Xmx4g \                          # Maximum heap size
           -XX:+UseG1GC \                    # Use G1 garbage collector
           -XX:MaxGCPauseMillis=200 \        # Target GC pause time
           -XX:+HeapDumpOnOutOfMemoryError \ # Dump heap on OOM
           -XX:HeapDumpPath=/opt/scim-gateway/logs \
           -Djava.security.egd=file:/dev/./urandom"
```

---

## SSL/HTTPS Setup

### Option 1: Let's Encrypt (Free)

```bash
# Install Certbot
sudo apt install -y certbot

# Generate certificate (standalone mode - stop app first)
sudo systemctl stop scim-gateway
sudo certbot certonly --standalone -d scim.yourcompany.com

# Certificates location
# /etc/letsencrypt/live/scim.yourcompany.com/fullchain.pem
# /etc/letsencrypt/live/scim.yourcompany.com/privkey.pem

# Convert to PKCS12 for Spring Boot
openssl pkcs12 -export \
  -in /etc/letsencrypt/live/scim.yourcompany.com/fullchain.pem \
  -inkey /etc/letsencrypt/live/scim.yourcompany.com/privkey.pem \
  -out /opt/scim-gateway/keystore.p12 \
  -name scim-gateway \
  -passout pass:YourKeystorePassword

# Update .env
SSL_KEYSTORE=/opt/scim-gateway/keystore.p12
SSL_KEYSTORE_PASSWORD=YourKeystorePassword
```

### Option 2: Self-Signed Certificate (Testing Only)

```bash
keytool -genkeypair \
  -alias scim-gateway \
  -keyalg RSA \
  -keysize 2048 \
  -storetype PKCS12 \
  -keystore keystore.p12 \
  -validity 3650 \
  -storepass YourKeystorePassword

# Move to application directory
mv keystore.p12 /opt/scim-gateway/
```

### Option 3: Add SSL to Application Properties

```properties
# Add to application.properties or .env
server.ssl.enabled=true
server.ssl.key-store=${SSL_KEYSTORE:classpath:keystore.p12}
server.ssl.key-store-password=${SSL_KEYSTORE_PASSWORD}
server.ssl.key-store-type=PKCS12
server.ssl.key-alias=scim-gateway
```

---

## Reverse Proxy (Nginx)

### Install Nginx

```bash
# Ubuntu/Debian
sudo apt install -y nginx

# CentOS/RHEL
sudo yum install -y nginx
```

### Configure Nginx

```bash
sudo nano /etc/nginx/sites-available/scim-gateway
```

Add configuration:

```nginx
upstream scim_backend {
    server 127.0.0.1:8080;
    keepalive 32;
}

server {
    listen 80;
    server_name scim.yourcompany.com;
    
    # Redirect HTTP to HTTPS
    return 301 https://$server_name$request_uri;
}

server {
    listen 443 ssl http2;
    server_name scim.yourcompany.com;
    
    # SSL Configuration
    ssl_certificate /etc/letsencrypt/live/scim.yourcompany.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/scim.yourcompany.com/privkey.pem;
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers HIGH:!aNULL:!MD5;
    ssl_prefer_server_ciphers on;
    ssl_session_cache shared:SSL:10m;
    ssl_session_timeout 10m;
    
    # Security Headers
    add_header Strict-Transport-Security "max-age=31536000; includeSubDomains" always;
    add_header X-Frame-Options DENY always;
    add_header X-Content-Type-Options nosniff always;
    add_header X-XSS-Protection "1; mode=block" always;
    
    # Logging
    access_log /var/log/nginx/scim-gateway-access.log;
    error_log /var/log/nginx/scim-gateway-error.log;
    
    # Proxy Settings
    location / {
        proxy_pass http://scim_backend;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_set_header Connection "";
        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
    }
    
    # Swagger UI
    location /swagger-ui/ {
        proxy_pass http://scim_backend;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
    
    # API Docs
    location /api-docs {
        proxy_pass http://scim_backend;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
    }
    
    # Block access to sensitive files
    location ~ /\. {
        deny all;
    }
}
```

### Enable Site

```bash
# Create symbolic link
sudo ln -s /etc/nginx/sites-available/scim-gateway /etc/nginx/sites-enabled/

# Test configuration
sudo nginx -t

# Reload Nginx
sudo systemctl reload nginx

# Check status
sudo systemctl status nginx
```

---

## Monitoring & Logging

### Application Logs

```bash
# Configure logback-spring.xml
cat > src/main/resources/logback-spring.xml << 'EOF'
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <property name="LOG_PATH" value="${LOG_FILE:-/var/log/scim-gateway}"/>
    <property name="LOG_PATTERN" value="%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"/>
    
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>${LOG_PATTERN}</pattern>
        </encoder>
    </appender>
    
    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>${LOG_PATH}/application.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>${LOG_PATH}/application.%d{yyyy-MM-dd}.%i.log</fileNamePattern>
            <maxHistory>30</maxHistory>
            <timeBasedFileNamingAndTriggeringPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedFNATP">
                <maxFileSize>100MB</maxFileSize>
            </timeBasedFileNamingAndTriggeringPolicy>
        </rollingPolicy>
        <encoder>
            <pattern>${LOG_PATTERN}</pattern>
        </encoder>
    </appender>
    
    <root level="${LOG_LEVEL:-INFO}">
        <appender-ref ref="CONSOLE"/>
        <appender-ref ref="FILE"/>
    </root>
</configuration>
EOF
```

### Systemd Journal Logs

```bash
# View recent logs
sudo journalctl -u scim-gateway -n 50

# Follow logs in real-time
sudo journalctl -u scim-gateway -f

# Logs from today
sudo journalctl -u scim-gateway --since today

# Logs from specific time
sudo journalctl -u scim-gateway --since "2024-01-01 00:00:00" --until "2024-01-01 23:59:59"

# Export logs to file
sudo journalctl -u scim-gateway --since today > /tmp/scim-gateway-logs.txt
```

### Health Check Endpoint

Add Spring Boot Actuator to `pom.xml`:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

Configure in `application.properties`:

```properties
management.endpoints.web.exposure.include=health,info,metrics
management.endpoint.health.show-details=always
```

Test health:

```bash
curl http://localhost:8080/actuator/health
```

### Monitoring with Prometheus & Grafana (Optional)

```bash
# Add dependencies
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>

# Configure
management.endpoints.web.exposure.include=prometheus
```

---

## Backup & Recovery

### Database Backup

```bash
#!/bin/bash
# backup.sh - MongoDB backup script

BACKUP_DIR="/opt/scim-gateway/backups"
DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_FILE="$BACKUP_DIR/scimdb_backup_$DATE"

# Create backup directory
mkdir -p $BACKUP_DIR

# Backup MongoDB
mongodump --uri="$MONGODB_URI" --out=$BACKUP_FILE

# Compress backup
tar -czf $BACKUP_FILE.tar.gz $BACKUP_FILE

# Remove uncompressed backup
rm -rf $BACKUP_FILE

# Delete backups older than 30 days
find $BACKUP_DIR -name "*.tar.gz" -mtime +30 -delete

echo "Backup completed: $BACKUP_FILE.tar.gz"
```

Make executable and schedule:

```bash
chmod +x backup.sh

# Add to crontab (daily at 2 AM)
crontab -e
0 2 * * * /opt/scim-gateway/backup.sh >> /var/log/scim-gateway/backup.log 2>&1
```

### Application Backup

```bash
#!/bin/bash
# backup-app.sh - Application backup script

BACKUP_DIR="/opt/scim-gateway/backups"
DATE=$(date +%Y%m%d_%H%M%S)

# Backup application files
tar -czf $BACKUP_DIR/app_backup_$DATE.tar.gz \
  --exclude='logs' \
  --exclude='backups' \
  /opt/scim-gateway/

echo "Application backup completed"
```

### Restore from Backup

```bash
# Stop application
sudo systemctl stop scim-gateway

# Restore database
tar -xzf scimdb_backup_20240101_020000.tar.gz
mongorestore --uri="$MONGODB_URI" scimdb_backup_20240101_020000/scimdb

# Restore application (if needed)
tar -xzf app_backup_20240101.tar.gz -C /

# Start application
sudo systemctl start scim-gateway
```

---

## Troubleshooting

### Application Won't Start

```bash
# Check Java version
java -version

# Check if port is in use
sudo lsof -i :8080
sudo netstat -tlnp | grep 8080

# Check logs
sudo journalctl -u scim-gateway -n 100

# Test configuration
java -jar scim-app.jar --debug

# Check file permissions
ls -la /opt/scim-gateway/
```

### Database Connection Issues

```bash
# Test MongoDB connection
mongosh $MONGODB_URI

# Check MongoDB status
sudo systemctl status mongod

# Check MongoDB logs
sudo journalctl -u mongod -n 50

# Verify credentials
echo $MONGODB_URI
```

### High Memory Usage

```bash
# Check JVM memory
jcmd $(pgrep -f scim-app.jar) VM.flags

# Check system memory
free -h
top -p $(pgrep -f scim-app.jar)

# Adjust JVM settings in .env
JAVA_OPTS="-Xms1g -Xmx2g"
```

### Slow Performance

```bash
# Check CPU usage
top

# Check disk I/O
iostat -x 1

# Enable slow query logging in MongoDB
mongosh
db.setProfilingLevel(1, { slowms: 100 })
```

### SSL Certificate Issues

```bash
# Verify certificate
openssl s_client -connect scim.yourcompany.com:443

# Check certificate expiry
openssl x509 -enddate -noout -in /etc/letsencrypt/live/scim.yourcompany.com/fullchain.pem

# Renew Let's Encrypt certificate
sudo certbot renew
sudo systemctl reload nginx
```

---

## Deployment Checklist

### Pre-Deployment

- [ ] Java 21.0.10 installed
- [ ] MongoDB installed and configured
- [ ] Application built successfully (`mvn clean package`)
- [ ] Environment variables configured
- [ ] Default credentials changed
- [ ] JWT secret generated
- [ ] SSL certificate obtained
- [ ] Nginx configured (if using reverse proxy)

### Post-Deployment

- [ ] Application starts successfully
- [ ] Health check endpoint responds
- [ ] Database connection verified
- [ ] Authentication working
- [ ] JWT token generation working
- [ ] Swagger UI accessible
- [ ] SCIM endpoints functional
- [ ] Logs being written
- [ ] Backup script configured
- [ ] Monitoring configured

### Security

- [ ] Default credentials changed
- [ ] JWT secret is strong (32+ characters)
- [ ] HTTPS enabled
- [ ] Firewall configured
- [ ] Non-root user created
- [ ] File permissions set correctly
- [ ] Sensitive files not publicly accessible
- [ ] Regular updates scheduled

---

## Quick Deployment Script

Save as `deploy.sh`:

```bash
#!/bin/bash
set -e

echo "🚀 SCIM Gateway Deployment Script"
echo "=================================="

# Configuration
APP_DIR="/opt/scim-gateway"
APP_USER="scimgateway"
APP_PORT=8080

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Functions
log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if running as root
if [ "$EUID" -ne 0 ]; then
    log_error "Please run as root or with sudo"
    exit 1
fi

# Install Java if not present
if ! command -v java &> /dev/null; then
    log_info "Installing Java 21..."
    apt update && apt install -y openjdk-21-jdk
fi

# Create user if not exists
if ! id -u $APP_USER &> /dev/null; then
    log_info "Creating user: $APP_USER"
    useradd --system --no-create-home --shell /bin/false $APP_USER
fi

# Create directories
log_info "Creating directories..."
mkdir -p $APP_DIR/{config,logs,backups}

# Copy application
log_info "Deploying application..."
cp scim-app.jar $APP_DIR/
chown -R $APP_USER:$APP_USER $APP_DIR

# Generate JWT secret if not set
if ! grep -q "JWT_SECRET=" $APP_DIR/.env 2>/dev/null; then
    log_warn "Generating JWT secret..."
    echo "JWT_SECRET=$(openssl rand -base64 32)" >> $APP_DIR/.env
fi

# Create systemd service
log_info "Creating systemd service..."
cat > /etc/systemd/system/scim-gateway.service << EOF
[Unit]
Description=SCIM Gateway Service
After=network.target

[Service]
Type=simple
User=$APP_USER
Group=$APP_USER
WorkingDirectory=$APP_DIR
EnvironmentFile=$APP_DIR/.env
ExecStart=/usr/bin/java -jar $APP_DIR/scim-app.jar
Restart=on-failure
RestartSec=10

[Install]
WantedBy=multi-user.target
EOF

# Enable and start service
log_info "Starting service..."
systemctl daemon-reload
systemctl enable scim-gateway
systemctl restart scim-gateway

# Wait for startup
sleep 5

# Check status
if systemctl is-active --quiet scim-gateway; then
    log_info "✅ Deployment successful!"
    log_info "Application is running on port $APP_PORT"
    log_info "Swagger UI: http://localhost:$APP_PORT/swagger-ui.html"
    log_info "Health Check: http://localhost:$APP_PORT/actuator/health"
else
    log_error "❌ Deployment failed. Check logs:"
    log_error "journalctl -u scim-gateway -n 50"
    exit 1
fi
```

Make executable and run:

```bash
chmod +x deploy.sh
sudo ./deploy.sh
```

---

**Last Updated**: April 9, 2026  
**Version**: 1.0.0
