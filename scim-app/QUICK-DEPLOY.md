# Quick Deployment Reference Card

##  5-Minute Deployment

### Option 1: Simple JAR (Fastest)

```bash
# 1. Build
mvn clean package -DskipTests

# 2. Transfer to server
scp target/scim-app.jar user@server:/opt/scim-gateway/

# 3. Run
ssh user@server
cd /opt/scim-gateway
export AUTH_USERNAME=admin
export AUTH_PASSWORD=secure123
export JWT_SECRET=$(openssl rand -base64 32)
export MONGODB_URI=mongodb://localhost:27017/scimdb
java -jar scim-app.jar
```

### Option 2: Docker (Recommended)

```bash
# 1. Build & Run
docker-compose up -d

# 2. Check
docker-compose ps
docker-compose logs -f
```

### Option 3: Production Systemd

```bash
# 1. Run deployment script
sudo ./deploy.sh

# 2. Verify
sudo systemctl status scim-gateway
curl http://localhost:8080/actuator/health
```

---

##  Essential Commands

### Service Management

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
sudo journalctl -u scim-gateway -f
```

### Docker Management

```bash
# Start
docker-compose up -d

# Stop
docker-compose down

# Logs
docker-compose logs -f scim-gateway

# Restart
docker-compose restart

# Update
docker-compose pull && docker-compose up -d --build
```

### Quick Tests

```bash
# Health check
curl http://localhost:8080/actuator/health

# Get JWT token
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'

# Test SCIM endpoint
curl http://localhost:8080/scim/v2/Users \
  -H "Authorization: Bearer YOUR_TOKEN"

# Swagger UI
# Open: http://localhost:8080/swagger-ui.html
```

---

##  Directory Structure

```
/opt/scim-gateway/
├── scim-app.jar              # Application
├── .env                      # Configuration (NEVER commit!)
├── config/                   # Config files
├── logs/                     # Application logs
│   ├── application.log
│   └── application.2024-01-01.log
└── backups/                  # Database backups
    └── scimdb_backup_20240101.tar.gz
```

---

##  Must-Configure Variables

```bash
# MINIMUM REQUIRED
MONGODB_URI=mongodb://localhost:27017/scimdb
AUTH_USERNAME=admin
AUTH_PASSWORD=ChangeThisPassword123!
JWT_SECRET=$(openssl rand -base64 32)
SERVER_PORT=8080
```

---

##  Security Checklist

```
 Change default credentials
 Generate strong JWT secret
 Enable HTTPS (Let's Encrypt)
 Configure firewall
 Use non-root user
 Set file permissions (750)
 Enable automatic updates
 Configure backups
```

---

##  Quick Troubleshooting

| Problem | Solution |
|---------|----------|
| App won't start | `journalctl -u scim-gateway -n 50` |
| Port in use | `sudo lsof -i :8080` |
| DB connection failed | `mongosh $MONGODB_URI` |
| High memory | Adjust `JAVA_OPTS="-Xms1g -Xmx2g"` |
| Can't access | Check firewall: `sudo ufw status` |
| SSL errors | `openssl s_client -connect localhost:443` |

---

##  Monitoring

```bash
# Check service
sudo systemctl status scim-gateway

# View logs
sudo journalctl -u scim-gateway --since today

# Resource usage
top -p $(pgrep -f scim-app.jar)

# Disk usage
du -sh /opt/scim-gateway/logs/

# Memory usage
free -h
```

---

##  Backup Commands

```bash
# Manual backup
mongodump --uri="$MONGODB_URI" --out=/tmp/backup

# Restore
mongorestore --uri="$MONGODB_URI" /tmp/backup/scimdb

# Automated (add to crontab)
0 2 * * * /opt/scim-gateway/backup.sh
```

---

##  Useful URLs

| Service | URL |
|---------|-----|
| Swagger UI | http://localhost:8080/swagger-ui.html |
| API Docs | http://localhost:8080/api-docs |
| Health Check | http://localhost:8080/actuator/health |
| SCIM Users | http://localhost:8080/scim/v2/Users |
| Login | http://localhost:8080/auth/login |

---

##  Support

- **Documentation**: See `DEPLOYMENT-GUIDE.md`
- **Configuration**: See `CONFIGURATION-GUIDE.md`
- **API Reference**: See `SCIM-GATEWAY-DOCUMENTATION.md`
- **Logs**: `/var/log/scim-gateway/` or `journalctl -u scim-gateway`

---

**Quick Tip**: Always test in development before deploying to production!
