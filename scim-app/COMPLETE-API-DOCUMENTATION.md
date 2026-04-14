# 📚 COMPLETE API DOCUMENTATION - SCIM Gateway

**Base URL:** `http://localhost:8181`  
**Version:** 1.0.0  
**Specification:** SCIM 2.0 (RFC 7643, RFC 7644)  
**Authentication:** JWT Bearer Token / Basic Auth  

---

## 🔐 AUTHENTICATION APIS

### 1. Login - Get JWT Token
```http
POST /auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123"
}
```
**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "username": "admin",
  "expiresIn": 86400000
}
```

### 2. Validate Token
```http
POST /auth/validate
Content-Type: application/json

{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```
**Response:**
```json
{
  "valid": true,
  "username": "admin",
  "message": "Token is valid"
}
```

### 3. Get Credentials (Dev Only)
```http
GET /auth/credentials
```
**Response:**
```json
{
  "username": "admin",
  "password": "********",
  "note": "Use these credentials with /auth/login endpoint",
  "warning": "Change default credentials in production"
}
```

---

## 👥 SCIM USER MANAGEMENT APIS

### 4. Create User
```http
POST /scim/v2/Users
Content-Type: application/json
Authorization: Bearer {token}

{
  "userName": "john.doe@example.com",
  "name": {
    "givenName": "John",
    "familyName": "Doe"
  },
  "emails": [
    {
      "value": "john.doe@example.com",
      "type": "work",
      "primary": true
    }
  ],
  "phoneNumbers": [
    {
      "value": "+1-555-123-4567",
      "type": "work"
    }
  ],
  "active": true,
  "externalId": "EMP-001"
}
```
**Response:** `201 Created` - Returns created user with metadata

### 5. Get User by ID
```http
GET /scim/v2/Users/{id}
Authorization: Bearer {token}
```
**Response:** `200 OK` - Returns user object

### 6. List/Search Users
```http
GET /scim/v2/Users?filter=userName eq "john"&sortBy=userName&sortOrder=ascending&startIndex=1&count=20
Authorization: Bearer {token}
```
**Query Parameters:**
- `filter` (optional): SCIM filter expression
  - Examples: `userName eq "john"`, `active eq true`, `email eq "test@example.com"`
- `sortBy` (optional): Attribute to sort by
- `sortOrder` (optional): `ascending` or `descending` (default: ascending)
- `startIndex` (optional): 1-based index (default: 1)
- `count` (optional): Results per page (default: 20)

**Response:**
```json
{
  "schemas": ["urn:ietf:params:scim:api:messages:2.0:ListResponse"],
  "totalResults": 50,
  "startIndex": 1,
  "itemsPerPage": 20,
  "Resources": [
    {
      "id": "uuid",
      "userName": "john.doe@example.com",
      "emails": [...],
      "active": true,
      "meta": {
        "resourceType": "User",
        "created": "2026-04-14T10:39:07Z",
        "lastModified": "2026-04-14T10:39:07Z",
        "location": "/scim/v2/Users/uuid"
      }
    }
  ]
}
```

### 7. Update User (PUT - Full Replace)
```http
PUT /scim/v2/Users/{id}
Content-Type: application/json
Authorization: Bearer {token}

{
  "userName": "john.doe@example.com",
  "emails": [...],
  "active": true
}
```
**Response:** `200 OK` - Returns updated user

### 8. Patch User (Partial Update)
```http
PATCH /scim/v2/Users/{id}
Content-Type: application/json
Authorization: Bearer {token}

{
  "schemas": ["urn:ietf:params:scim:api:messages:2.0:PatchOp"],
  "Operations": [
    {
      "op": "replace",
      "path": "emails",
      "value": [{"value": "new.email@example.com", "primary": true}]
    }
  ]
}
```
**Supported Operations:**
- `add`: Add new attribute
- `replace`: Replace existing attribute
- `remove`: Remove attribute

**Response:** `200 OK` - Returns patched user

### 9. Delete User
```http
DELETE /scim/v2/Users/{id}
Authorization: Bearer {token}
```
**Response:** `204 No Content`  
**Note:** Soft delete - sets `active` to `false`

---

## 👥 SCIM GROUP MANAGEMENT APIS

### 10. Create Group
```http
POST /scim/v2/Groups
Content-Type: application/json
Authorization: Bearer {token}

{
  "displayName": "Engineering Team",
  "members": [
    {
      "value": "user-uuid-1",
      "display": "john.doe@example.com"
    },
    {
      "value": "user-uuid-2",
      "display": "jane.smith@example.com"
    }
  ]
}
```
**Response:** `201 Created` - Returns created group

### 11. Get Group by ID
```http
GET /scim/v2/Groups/{id}
Authorization: Bearer {token}
```
**Response:** `200 OK` - Returns group object

### 12. List/Search Groups
```http
GET /scim/v2/Groups?filter=displayName co "Engineering"&startIndex=1&count=20
Authorization: Bearer {token}
```
**Response:** List of groups with pagination

### 13. Update Group (PUT)
```http
PUT /scim/v2/Groups/{id}
Content-Type: application/json
Authorization: Bearer {token}

{
  "displayName": "Updated Team Name",
  "members": [...]
}
```
**Response:** `200 OK` - Returns updated group

### 14. Patch Group
```http
PATCH /scim/v2/Groups/{id}
Content-Type: application/json
Authorization: Bearer {token}

{
  "Operations": [
    {
      "op": "add",
      "path": "members",
      "value": [{"value": "user-uuid-3", "display": "new.member@example.com"}]
    }
  ]
}
```
**Response:** `200 OK` - Returns patched group

### 15. Delete Group
```http
DELETE /scim/v2/Groups/{id}
Authorization: Bearer {token}
```
**Response:** `204 No Content`

---

## 🏢 APPLICATION REGISTRY & PROVISIONING APIS

### 16. List All Applications
```http
GET /scim/v2/apps
Authorization: Bearer {token}
```
**Response:** Array of Application objects

### 17. Get Application by ID
```http
GET /scim/v2/apps/{id}
Authorization: Bearer {token}
```
**Response:** Application object

### 18. Register New Application
```http
POST /scim/v2/apps
Content-Type: application/json
Authorization: Bearer {token}

{
  "name": "Salesforce",
  "baseUrl": "https://your-instance.my.salesforce.com/services/scim/v2",
  "apiKey": "your-api-key-here",
  "enabled": true,
  "fieldMappings": {
    "userName": "email",
    "displayName": "name.givenName"
  },
  "syncIntervalMinutes": 5,
  "maxRetries": 3,
  "autoProvision": true
}
```
**Response:** `201 Created` - Returns created application

### 19. Update Field Mappings
```http
PUT /scim/v2/apps/{id}/mappings
Content-Type: application/json
Authorization: Bearer {token}

{
  "userName": "email",
  "displayName": "fullName",
  "active": "status"
}
```
**Response:** `200 OK`

### 20. Delete Application
```http
DELETE /scim/v2/apps/{id}
Authorization: Bearer {token}
```
**Response:** `204 No Content`

### 21. Provision User to Application
```http
POST /scim/v2/apps/provision/{userId}/to/{appId}
Authorization: Bearer {token}
```
**Response:**
```json
{
  "status": "provisioning_started",
  "userId": "user-uuid",
  "appId": "app-uuid",
  "message": "User provisioning job created (async)"
}
```
**Note:** Async operation - check job status

### 22. Deprovision User (Placeholder)
```http
POST /scim/v2/apps/deprovision/{userId}/from/{appId}
Authorization: Bearer {token}
```
**Response:** Not implemented yet

### 23. List All Provisioning Jobs
```http
GET /scim/v2/apps/jobs
Authorization: Bearer {token}
```
**Response:** Array of ProvisioningJob objects

### 24. Get Jobs by Status
```http
GET /scim/v2/apps/jobs/status/{status}
Authorization: Bearer {token}
```
**Status Values:**
- `PENDING`
- `RETRYING`
- `SUCCESS`
- `FAILED`

**Response:** Array of jobs with specified status

### 25. Get Jobs for User
```http
GET /scim/v2/apps/jobs/user/{userId}
Authorization: Bearer {token}
```
**Response:** Array of provisioning jobs for specific user

### 26. Get Job Details
```http
GET /scim/v2/apps/jobs/{jobId}
Authorization: Bearer {token}
```
**Response:** ProvisioningJob object with full details

---

## 📋 SCIM STANDARD APIS

### 27. Get Service Provider Configuration
```http
GET /scim/v2/ServiceProviderConfig
```
**Response:**
```json
{
  "schemas": ["urn:ietf:params:scim:schemas:core:2.0:ServiceProviderConfig"],
  "documentationUri": "https://github.com/strangecodee",
  "patch": {"supported": true},
  "bulk": {"supported": false},
  "filter": {"supported": true, "maxResults": 100},
  "changePassword": {"supported": false},
  "sort": {"supported": true},
  "etag": {"supported": false},
  "authenticationSchemes": [
    {
      "name": "OAuth Bearer Token",
      "type": "oauthbearertoken",
      "primary": true
    },
    {
      "name": "Basic Authentication",
      "type": "basic",
      "primary": false
    }
  ]
}
```

### 28. List Resource Types
```http
GET /scim/v2/ResourceTypes
```
**Response:** Array of resource types (User, Group)

### 29. Get Resource Type by Name
```http
GET /scim/v2/ResourceTypes/{name}
```
**Examples:**
- `/scim/v2/ResourceTypes/User`
- `/scim/v2/ResourceTypes/Group`

### 30. List Schemas
```http
GET /scim/v2/Schemas
```
**Response:** Array of SCIM schemas (User schema, Group schema)

### 31. Get Schema by ID
```http
GET /scim/v2/Schemas/{id}
```
**Examples:**
- `/scim/v2/Schemas/urn:ietf:params:scim:schemas:core:2.0:User`
- `/scim/v2/Schemas/urn:ietf:params:scim:schemas:core:2.0:Group`

---

## ℹ️ API INFO & HEALTH

### 32. Get API Information
```http
GET /api/info
```
**Response:**
```json
{
  "name": "SCIM Gateway",
  "version": "1.0.0",
  "description": "SCIM 2.0 compliant identity management gateway",
  "vendor": "Anurag Maurya",
  "service": "scim-app",
  "status": "running"
}
```

### 33. Health Check (Actuator)
```http
GET /actuator/health
```
**Response:**
```json
{
  "status": "UP",
  "components": {
    "mongo": {"status": "UP"},
    "diskSpace": {"status": "UP"}
  }
}
```

### 34. Simple User Create (Non-SCIM)
```http
POST /users
Content-Type: application/json

{
  "userName": "testuser",
  "email": "test@example.com",
  "active": true
}
```
**Response:** MongoUser object

---

## 🔧 DEVELOPMENT & DOCUMENTATION

### 35. Swagger UI
```
GET /swagger-ui.html
```
Interactive API documentation and testing interface

### 36. OpenAPI Specification
```
GET /api-docs
```
Returns OpenAPI 3.0 specification in JSON format

---

## 📊 API SUMMARY

| Category | Endpoints | Auth Required |
|----------|-----------|---------------|
| **Authentication** | 3 | No (login) / Yes (validate) |
| **User Management** | 6 | Yes |
| **Group Management** | 6 | Yes |
| **Application Registry** | 11 | Yes |
| **SCIM Standard** | 5 | No (mostly public) |
| **Info & Health** | 4 | No |
| **Documentation** | 2 | No |
| **TOTAL** | **37** | - |

---

## 🔑 AUTHENTICATION

### How to Use:

1. **Get Token:**
```bash
POST /auth/login
{
  "username": "admin",
  "password": "admin123"
}
```

2. **Use Token in Requests:**
```http
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

3. **Or Use Basic Auth:**
```http
Authorization: Basic YWRtaW46YWRtaW4xMjM=
```

---

## 📝 SCIM FILTER EXAMPLES

### User Filters:
- `userName eq "john"`
- `active eq true`
- `email eq "test@example.com"`
- `userName co "john"` (contains)
- `userName sw "john"` (starts with)

### Group Filters:
- `displayName eq "Engineering"`
- `displayName co "Team"`

---

## 🚀 QUICK START

### 1. Login
```bash
curl -X POST http://localhost:8181/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

### 2. Create User
```bash
curl -X POST http://localhost:8181/scim/v2/Users \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {token}" \
  -d '{
    "userName": "test@example.com",
    "emails": [{"value": "test@example.com", "primary": true}],
    "active": true
  }'
```

### 3. List Users
```bash
curl http://localhost:8181/scim/v2/Users \
  -H "Authorization: Bearer {token}"
```

---

## 📚 ADDITIONAL RESOURCES

- **Swagger UI:** http://localhost:8181/swagger-ui.html
- **API Docs:** http://localhost:8181/api-docs
- **SCIM 2.0 RFC 7643:** https://tools.ietf.org/html/rfc7643
- **SCIM 2.0 RFC 7644:** https://tools.ietf.org/html/rfc7644

---

**Last Updated:** 2026-04-14  
**API Version:** 1.0.0  
**Total Endpoints:** 37
