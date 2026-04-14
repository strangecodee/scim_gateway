# 🚀 Quick Deploy - JDK 21 Server

## ✅ Your Server Setup

- **Java**: JDK 21 ✅ (Already installed - Perfect!)
- **Port**: 8181
- **JAR**: scim-app-0.0.1-SNAPSHOT.jar (37.25 MB)
- **Package**: scim-gateway-server-deploy.zip (33.57 MB)

---

## 📦 Deploy in 3 Commands

### 1. Upload to Server

**Using WinSCP:**
- Upload `scim-gateway-server-deploy.zip` to `/opt/`

**Using SCP:**
```bash
scp scim-gateway-server-deploy.zip user@your-server:/opt/
```

### 2. Extract & Setup

```bash
ssh user@your-server.com
cd /opt
unzip scim-gateway-server-deploy.zip -d scim-gateway
cd scim-gateway
chmod +x *.sh
```

### 3. Start!

```bash
./server-start.sh
```

**Done!** 🎉

---

## ✅ Verify

```bash
# Check status
./server-status.sh

# Health check
curl http://localhost:8181/actuator/health

# Access Swagger UI
# http://your-server-ip:8181/swagger-ui.html
```

---

## 🎛️ Management

```bash
# Start
./server-start.sh

# Stop
./server-stop.sh

# Status
./server-status.sh

# Logs
tail -f scim-gateway.log
```

---

## 🔧 Configuration (.env)

```bash
JWT_SECRET=cPVO8XANn5o1KOYZCKLtrDQEF8pGRb6madUSE9rB6lA=
SCIM_ADMIN_PASSWORD=admin123
MONGODB_URI=mongodb+srv://anurag:cloud%40123@cluster0.nivrt0z.mongodb.net/scimdb?retryWrites=true&w=majority
SPRING_PROFILES_ACTIVE=dev
```

**For production:**
- Change JWT_SECRET
- Change SCIM_ADMIN_PASSWORD
- Set SPRING_PROFILES_ACTIVE=prod

---

## 🔐 Firewall

```bash
# Ubuntu
sudo ufw allow 8181/tcp

# CentOS
sudo firewall-cmd --permanent --add-port=8181/tcp
sudo firewall-cmd --reload
```

---

## 📋 Checklist

- [ ] Upload ZIP to server
- [ ] Extract: `unzip scim-gateway-server-deploy.zip -d scim-gateway`
- [ ] Make executable: `chmod +x *.sh`
- [ ] Configure `.env` (if needed)
- [ ] Start: `./server-start.sh`
- [ ] Verify: `curl http://localhost:8181/actuator/health`
- [ ] Open firewall port 8181
- [ ] Access: `http://server-ip:8181/swagger-ui.html`

---

## 🐛 Troubleshooting

### "Java not found"
```bash
java -version  # Should show JDK 21
```

### "Port 8181 in use"
```bash
netstat -tuln | grep 8181
sudo kill -9 <PID>
```

### "Won't start"
```bash
tail -f scim-gateway.log
```

---

## 📍 Access URLs

- **Application**: `http://your-server-ip:8181`
- **Swagger UI**: `http://your-server-ip:8181/swagger-ui.html`
- **Health**: `http://your-server-ip:8181/actuator/health`

**Default Login:**
- Username: `admin`
- Password: `admin123`

---

**Ready to deploy!** 🚀
