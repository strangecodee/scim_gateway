# Identity Provider (IdP) Integration Guide

## Overview

Your SCIM Gateway can integrate with Identity Providers in **two ways**:

1. **As a SCIM Service Provider (SP)** - IdPs push users TO your gateway
2. **As a SCIM Client** - Your gateway pushes users TO external IdPs/applications

---

## Option 1: Connect IdP TO Your SCIM Gateway (Most Common)

### Supported Identity Providers

Your SCIM Gateway works with any SCIM 2.0 compliant IdP:

- ✅ **Azure AD / Microsoft Entra ID**
- ✅ **Okta**
- ✅ **Keycloak**
- ✅ **OneLogin**
- ✅ **Ping Identity**
- ✅ **JumpCloud**
- ✅ **Any SCIM 2.0 compliant IdP**

### General Setup Steps

#### Step 1: Get Your Gateway Information

**Base URL**: `http://localhost:8080` (dev) or `https://your-domain.com` (prod)

**SCIM Endpoint**: `http://localhost:8080/scim/v2`

**Authentication**:
- **Username**: `admin`
- **Password**: `admin123` (change in production!)
- **Auth Type**: Bearer Token (JWT)

#### Step 2: Get JWT Token for IdP

```bash
# Login to get token
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'

# Response:
{
  "token": "eyJhbGciOiJIUzM4NCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 86400000,
  "username": "admin"
}
```

**Copy the token** - you'll need it for IdP configuration.

#### Step 3: Test SCIM Discovery Endpoints

Your gateway exposes these public endpoints for IdPs:

```bash
# Service Provider Config
curl http://localhost:8080/scim/v2/ServiceProviderConfig

# Resource Types
curl http://localhost:8080/scim/v2/ResourceTypes

# Schemas
curl http://localhost:8080/scim/v2/Schemas
```

---

## Azure AD / Microsoft Entra ID Integration

### Configuration in Azure Portal

1. **Go to Azure Portal** → **Microsoft Entra ID** → **Enterprise Applications**

2. **Create New Application**:
   - Click **+ New application**
   - Select **Create your own application**
   - Name: `SCIM Gateway`
   - Select: **Integrate any other application you don't find in the gallery**
   - Click **Create**

3. **Configure Provisioning**:
   - Go to your new application
   - Click **Provisioning** in left menu
   - Set **Provisioning Mode** to **Automatic**

4. **Enter SCIM Details**:
   - **Tenant URL**: `http://localhost:8080/scim/v2`
     - For production: `https://your-domain.com/scim/v2`
   - **Secret Token**: Your JWT token (from Step 2 above)
     - Format: `eyJhbGciOiJIUzM4NCJ9...`

5. **Test Connection**:
   - Click **Test Connection**
   - Azure will verify the SCIM endpoint

6. **Configure Attribute Mappings**:
   - Go to **Mappings** → **Provision Microsoft Entra ID Users**
   - Default mappings usually work:
     - `userPrincipalName` → `userName`
     - `displayName` → `displayName`
     - `mail` → `emails[type eq "work"].value`
     - `givenName` → `name.givenName`
     - `surname` → `name.familyName`
     - `accountEnabled` → `active`

7. **Enable Provisioning**:
   - Set **Provisioning Status** to **On**
   - Click **Save**

8. **Verify**:
   - Go to **Provisioning Logs**
   - Check for successful user provisioning

---

## Okta Integration

### Configuration in Okta

1. **Create Application**:
   - Go to **Applications** → **Applications**
   - Click **Create App Integration**
   - Select: **SCIM 2.0**
   - Click **Next**

2. **General Settings**:
   - App name: `SCIM Gateway`
   - App logo: (optional)
   - Click **Next**

3. **SCIM Client Configuration**:
   - **Base URL**: `http://localhost:8080/scim/v2`
   - **API Token**: Your JWT token
   
4. **Authentication**:
   - Select **OAuth Bearer Token**
   - Paste your JWT token

5. **Test Connection**:
   - Click **Test API Credentials**
   - Should return success

6. **Attribute Mapping**:
   - Map Okta attributes to SCIM attributes:
     ```
     userName ← login
     displayName ← displayName
     name.givenName ← firstName
     name.familyName ← lastName
     emails[type eq "work"].value ← email
     active ← status (active=suspended)
     ```

7. **Provisioning**:
   - Go to **Provisioning** → **Integration**
   - Enable: **Create Users**, **Update User Attributes**, **Deactivate Users**
   - Click **Save**

8. **Assign Users**:
   - Go to **Assignments** → **Assign** → **Assign to People**
   - Select users to provision

---

## Keycloak Integration

### Configuration in Keycloak

1. **Install SCIM Extension**:
   ```bash
   # Download Keycloak SCIM extension
   # https://github.com/Captain-Pocke/keycloak-scim
   ```

2. **Configure SCIM Client**:
   - Go to your Realm
   - **Clients** → **Create**
   - Client ID: `scim-gateway`
   - Client Protocol: `scim`

3. **Set SCIM Endpoint**:
   - **Base URL**: `http://localhost:8080/scim/v2`
   - **Authentication**: Bearer Token
   - **Token**: Your JWT token

4. **User Federation**:
   - Go to **User Federation**
   - Add SCIM provider
   - Configure sync settings

---

## Option 2: Connect Your Gateway TO External IdPs

Your gateway can also **push users to external IdPs** that support SCIM.

### Register External Application

#### Via Swagger UI (Easiest)

1. Open: `http://localhost:8080/swagger-ui.html`
2. Go to **Application Registry** section
3. Click **POST /scim/v2/apps**
4. Enter application details:

```json
{
  "name": "Azure AD Production",
  "baseUrl": "https://your-tenant.provisioning.azure-api.net",
  "apiKey": "your-scim-token-from-azure",
  "enabled": true,
  "autoProvision": true,
  "fieldMappings": {
    "userName": "userPrincipalName",
    "displayName": "displayName",
    "emails": "mail",
    "active": "accountEnabled"
  }
}
```

#### Via API

```bash
# Register external IdP
curl -X POST http://localhost:8080/scim/v2/apps \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Okta Production",
    "baseUrl": "https://your-org.okta.com/scim/v2",
    "apiKey": "okta-api-token",
    "enabled": true,
    "autoProvision": true,
    "fieldMappings": {
      "userName": "login",
      "emails": "email"
    }
  }'
```

### Provision Users to External IdP

```bash
# Provision specific user to specific app
curl -X POST http://localhost:8080/scim/v2/apps/provision/{userId}/to/{appId} \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# Provision user to ALL registered apps
curl -X POST http://localhost:8080/scim/v2/apps/provision/{userId}/to-all \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# Sync all users to all apps
curl -X POST http://localhost:8080/scim/v2/apps/sync-all \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Monitor Provisioning Jobs

```bash
# Get all provisioning jobs
curl http://localhost:8080/scim/v2/apps/jobs \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# Get jobs by status
curl http://localhost:8080/scim/v2/apps/jobs/status/FAILED \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

## Production Checklist

### Security

- [ ] Change default password (`admin123`) in `application.properties`
- [ ] Set strong JWT secret (use `openssl rand -base64 32`)
- [ ] Enable HTTPS/TLS
- [ ] Configure CORS for specific domains
- [ ] Set up firewall rules
- [ ] Rotate JWT tokens regularly

### Configuration

- [ ] Update `auth.default.password` to strong password
- [ ] Set `jwt.secret` to random 256-bit key
- [ ] Configure MongoDB Atlas connection string
- [ ] Set correct `app.info.name` and URLs
- [ ] Configure logging level

### Monitoring

- [ ] Set up health check monitoring: `http://localhost:8080/actuator/health`
- [ ] Monitor provisioning job failures
- [ ] Set up alerts for failed provisions
- [ ] Configure log aggregation

---

## Testing Your Integration

### Test 1: Verify Gateway is Running

```bash
# Check API info
curl http://localhost:8080/api/info

# Expected: 200 OK with app details
```

### Test 2: Get Authentication Token

```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

### Test 3: Test SCIM Endpoint

```bash
# Get ServiceProviderConfig (public)
curl http://localhost:8080/scim/v2/ServiceProviderConfig

# Create user (requires auth)
curl -X POST http://localhost:8080/scim/v2/Users \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "userName": "test@example.com",
    "name": {
      "givenName": "Test",
      "familyName": "User"
    },
    "emails": [{
      "value": "test@example.com",
      "primary": true,
      "type": "work"
    }],
    "active": true
  }'
```

### Test 4: IdP Webhook Test

After configuring your IdP, create a test user in the IdP and verify:

1. Check gateway logs: `tail -f logs/app.log`
2. Verify user created: `curl http://localhost:8080/scim/v2/Users -H "Authorization: Bearer TOKEN"`
3. Check MongoDB for new user

---

## Troubleshooting

### Issue: IdP Cannot Connect

**Check**:
- Gateway is running on port 8080
- Firewall allows inbound connections
- Base URL is correct (include `/scim/v2`)
- JWT token is valid and not expired

**Test**:
```bash
curl http://localhost:8080/scim/v2/ServiceProviderConfig
```

### Issue: 401 Unauthorized

**Check**:
- JWT token is correct
- Token format: `Authorization: Bearer <token>`
- Token not expired (default: 24 hours)

**Solution**: Get new token via `/auth/login`

### Issue: 403 Forbidden

**Check**:
- You're using authenticated endpoints without token
- Public endpoints don't need token:
  - `/scim/v2/ServiceProviderConfig`
  - `/scim/v2/ResourceTypes`
  - `/scim/v2/Schemas`

### Issue: Provisioning Fails

**Check**:
```bash
# Check failed jobs
curl http://localhost:8080/scim/v2/apps/jobs/status/FAILED \
  -H "Authorization: Bearer TOKEN"

# Check application config
curl http://localhost:8080/scim/v2/apps \
  -H "Authorization: Bearer TOKEN"
```

**Verify**:
- External app `baseUrl` is correct
- External app `apiKey` is valid
- Network connectivity to external app
- Field mappings are correct

---

## Advanced: Custom SCIM Extensions

If your IdP requires custom attributes:

1. **Extend ScimUser model**:
```java
public class CustomScimUser extends ScimUser {
    private Map<String, Object> customAttributes;
}
```

2. **Update field mappings** per application

3. **Configure IdP** to send custom schema URNs

---

## Next Steps

1. ✅ Choose your IdP (Azure AD, Okta, Keycloak, etc.)
2. ✅ Get JWT token from your gateway
3. ✅ Configure IdP with gateway SCIM endpoint
4. ✅ Test with a single user
5. ✅ Enable automatic provisioning
6. ✅ Monitor and troubleshoot

---

## Need Help?

- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **API Docs**: `http://localhost:8080/api-docs`
- **Health Check**: `http://localhost:8080/actuator/health`
- **Documentation**: See `SCIM-GATEWAY-DOCUMENTATION.md`

---

## Quick Reference

### Your Gateway Endpoints

| Endpoint | Auth Required | Purpose |
|----------|--------------|---------|
| `/api/info` | No | Gateway information |
| `/auth/login` | No | Get JWT token |
| `/auth/credentials` | No | Show default credentials |
| `/scim/v2/ServiceProviderConfig` | No | SCIM capabilities |
| `/scim/v2/ResourceTypes` | No | Available resource types |
| `/scim/v2/Schemas` | No | SCIM schemas |
| `/scim/v2/Users` | Yes | User CRUD |
| `/scim/v2/Groups` | Yes | Group CRUD |
| `/scim/v2/apps` | Yes | Application registry |
| `/scim/v2/apps/jobs` | Yes | Provisioning jobs |

### Authentication Header

```
Authorization: Bearer eyJhbGciOiJIUzM4NCJ9...
```

### Common IdP Base URLs

- **Azure AD**: `https://<tenant>.provisioning.azure-api.net/scim/v2`
- **Okta**: `https://<org>.okta.com/scim/v2`
- **OneLogin**: `https://<subdomain>.onelogin.com/scim/v2`
- **Keycloak**: `https://<host>/auth/realms/<realm>/scim/v2`
