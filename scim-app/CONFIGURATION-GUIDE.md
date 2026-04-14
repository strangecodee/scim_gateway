# SCIM Gateway Configuration Guide

## 🔧 Configuration Overview

All sensitive data and URLs are now **fully configurable** via:
1. **Environment Variables** (Recommended for production)
2. **Application Properties** (For development)
3. **Command Line Arguments** (For deployment)

---

## 📋 Configuration Properties

### Database Configuration

| Property | Environment Variable | Default | Description |
|----------|---------------------|---------|-------------|
| `spring.data.mongodb.uri` | `MONGODB_URI` | MongoDB Atlas URI | MongoDB connection string |

**Example**:
```bash
export MONGODB_URI=mongodb+srv://user:pass@cluster.mongodb.net/scimdb
```

### Authentication Configuration

| Property | Environment Variable | Default | Description |
|----------|---------------------|---------|-------------|
| `auth.default.username` | `AUTH_USERNAME` | `admin` | Default admin username |
| `auth.default.password` | `AUTH_PASSWORD` | `admin123` | Default admin password |
| `auth.enabled` | `AUTH_ENABLED` | `true` | Enable/disable authentication |

** IMPORTANT**: Change default credentials in production!

### JWT Configuration

| Property | Environment Variable | Default | Description |
|----------|---------------------|---------|-------------|
| `jwt.secret` | `JWT_SECRET` | Auto-generated | JWT signing secret (min 256 bits) |
| `jwt.expiration` | `JWT_EXPIRATION` | `86400000` | Token validity in milliseconds (24h) |

**Generate secure JWT secret**:
```bash
openssl rand -base64 32
```

### Server Configuration

| Property | Environment Variable | Default | Description |
|----------|---------------------|---------|-------------|
| `server.port` | `SERVER_PORT` | `8080` | HTTP server port |
| `spring.profiles.active` | `SPRING_PROFILES_ACTIVE` | `dev` | Active Spring profile |

### External API Configuration

| Property | Environment Variable | Default | Description |
|----------|---------------------|---------|-------------|
| `api.external.base-url` | `EXTERNAL_API_BASE_URL` | Empty | Base URL for external APIs |
| `api.default-timeout` | `API_TIMEOUT` | `30000` | API timeout in milliseconds |
| `api.default-retries` | `API_RETRIES` | `3` | Default retry attempts |

---

## 🚀 Quick Setup

### Option 1: Using .env File (Recommended)

1. **Copy the example file**:
   ```bash
   cp .env.example .env
   ```

2. **Edit `.env`** with your values:
   ```bash
   AUTH_USERNAME=your-username
   AUTH_PASSWORD=your-secure-password
   JWT_SECRET=your-secret-key
   MONGODB_URI=your-mongodb-uri
   ```

3. **Load environment variables**:
   ```bash
   # Windows PowerShell
   Get-Content .env | ForEach-Object { 
       $name, $value = $_.Split('=')
       [Environment]::SetEnvironmentVariable($name, $value, 'User')
   }
   
   # Linux/Mac
   export $(cat .env | xargs)
   ```

### Option 2: Direct Environment Variables

```bash
# Linux/Mac
export AUTH_USERNAME=myadmin
export AUTH_PASSWORD=supersecretpassword
export JWT_SECRET=$(openssl rand -base64 32)
export MONGODB_URI=mongodb+srv://user:pass@cluster.mongodb.net/scimdb

# Windows PowerShell
$env:AUTH_USERNAME="myadmin"
$env:AUTH_PASSWORD="supersecretpassword"
$env:JWT_SECRET="your-secret-key"
$env:MONGODB_URI="mongodb+srv://user:pass@cluster.mongodb.net/scimdb"
```

### Option 3: Command Line Arguments

```bash
java -jar scim-app.jar \
  --auth.username=myadmin \
  --auth.password=supersecretpassword \
  --jwt.secret=your-secret-key \
  --spring.data.mongodb.uri=mongodb+srv://...
```

### Option 4: Application Properties (Development Only)

Edit `src/main/resources/application.properties`:
```properties
auth.default.username=myadmin
auth.default.password=supersecretpassword
jwt.secret=your-secret-key
```

---

## 🔐 Security Best Practices

### Production Checklist

✅ **Change Default Credentials**
```bash
export AUTH_USERNAME=admin_$(date +%s)
export AUTH_PASSWORD=$(openssl rand -base64 16)
```

✅ **Use Strong JWT Secret**
```bash
export JWT_SECRET=$(openssl rand -base64 32)
```

✅ **Disable Credentials Endpoint**
```bash
export AUTH_ENABLED=true
```

✅ **Use Environment Variables** (NOT properties files)
- Never commit `.env` to version control
- Use secrets management (AWS Secrets Manager, HashiCorp Vault, etc.)

✅ **Enable HTTPS**
```properties
server.ssl.enabled=true
server.ssl.key-store=classpath:keystore.p12
server.ssl.key-store-password=${SSL_PASSWORD}
```

---

## 📝 Configuration Examples

### Development Setup

```bash
# .env.development
MONGODB_URI=mongodb+srv://dev:dev123@dev-cluster.mongodb.net/scimdb-dev
AUTH_USERNAME=dev
AUTH_PASSWORD=dev123
JWT_SECRET=dev-secret-key-for-testing-only
AUTH_ENABLED=true
SERVER_PORT=8080
SPRING_PROFILES_ACTIVE=dev
```

### Production Setup

```bash
# .env.production (NEVER commit this!)
MONGODB_URI=mongodb+srv://prod-user:${DB_PASSWORD}@prod-cluster.mongodb.net/scimdb-prod
AUTH_USERNAME=scim-admin
AUTH_PASSWORD=${APP_PASSWORD}
JWT_SECRET=${JWT_SIGNING_KEY}
AUTH_ENABLED=true
SERVER_PORT=443
SPRING_PROFILES_ACTIVE=prod
API_TIMEOUT=60000
API_RETRIES=5
```

### Docker Deployment

```bash
docker run -p 8080:8080 \
  -e MONGODB_URI=mongodb+srv://user:pass@cluster.mongodb.net/scimdb \
  -e AUTH_USERNAME=admin \
  -e AUTH_PASSWORD=securepassword \
  -e JWT_SECRET=$(openssl rand -base64 32) \
  -e AUTH_ENABLED=true \
  scim-gateway:latest
```

### Docker Compose

```yaml
version: '3.8'
services:
  scim-gateway:
    image: scim-gateway:latest
    ports:
      - "8080:8080"
    environment:
      - MONGODB_URI=${MONGODB_URI}
      - AUTH_USERNAME=${AUTH_USERNAME}
      - AUTH_PASSWORD=${AUTH_PASSWORD}
      - JWT_SECRET=${JWT_SECRET}
      - AUTH_ENABLED=true
    env_file:
      - .env
```

---

## 🔍 Testing Configuration

### Verify Environment Variables

**Windows PowerShell**:
```powershell
# Check if variables are set
echo $env:AUTH_USERNAME
echo $env:AUTH_PASSWORD
echo $env:JWT_SECRET
echo $env:MONGODB_URI
```

**Linux/Mac**:
```bash
# Check if variables are set
echo $AUTH_USERNAME
echo $AUTH_PASSWORD
echo $JWT_SECRET
echo $MONGODB_URI
```

### Test Authentication

```bash
# Login with configured credentials
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d "{
    \"username\": \"$AUTH_USERNAME\",
    \"password\": \"$AUTH_PASSWORD\"
  }"
```

### Test Database Connection

```bash
# Check application logs for MongoDB connection
# Look for: "Monitor thread successfully connected to server"
```

---

## ⚙️ Advanced Configuration

### Multiple Users (Future Enhancement)

Currently supports single admin user. For multiple users:

1. **Create User entity** in MongoDB
2. **Update AuthController** to query database
3. **Add password encryption** (BCrypt)
4. **Implement user registration** endpoint

### OAuth2 Integration (Future)

For enterprise SSO:

```properties
spring.security.oauth2.client.registration.google.client-id=${GOOGLE_CLIENT_ID}
spring.security.oauth2.client.registration.google.client-secret=${GOOGLE_CLIENT_SECRET}
```

### LDAP/Active Directory (Future)

```properties
spring.ldap.urls=${LDAP_URL:ldap://ldap.example.com}
spring.ldap.base=${LDAP_BASE:dc=example,dc=com}
spring.ldap.username=${LDAP_USERNAME}
spring.ldap.password=${LDAP_PASSWORD}
```

---

## 🐛 Troubleshooting

### Problem: "Authentication failed"

**Solution**:
```bash
# Verify credentials are set
echo $AUTH_USERNAME
echo $AUTH_PASSWORD

# Restart application after changing
```

### Problem: "JWT token generation failed"

**Solution**:
```bash
# Ensure JWT secret is at least 256 bits (32 bytes)
echo -n "$JWT_SECRET" | wc -c

# Regenerate if too short
export JWT_SECRET=$(openssl rand -base64 32)
```

### Problem: "MongoDB connection timeout"

**Solution**:
```bash
# Check MongoDB URI format (password must be URL-encoded)
echo $MONGODB_URI

# Test connection
mongo "mongodb+srv://..."
```

---

## 📚 References

- [Spring Boot Externalized Configuration](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config)
- [Environment Variables vs Properties](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config.files-and-profiles)
- [JWT Best Practices](https://datatracker.ietf.org/doc/html/rfc8725)

---

**Last Updated**: April 9, 2026  
**Version**: 1.0.0
