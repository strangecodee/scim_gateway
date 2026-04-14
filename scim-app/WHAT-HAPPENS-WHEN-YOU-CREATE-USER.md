# What Happens When You Create a User? - Complete Flow Guide

## 🎯 Quick Answer

When you create a user in your SCIM Gateway, **4 main things happen**:

1. ✅ **Validation** - Checks if `userName` is provided
2. 💾 **Save to MongoDB** - User stored in database with UUID and timestamps
3. 🔄 **Auto-Provisioning** (optional) - User sent to external apps/IdPs if enabled
4. 📝 **Response** - Created user returned with metadata

---

## 📊 Complete Step-by-Step Flow

### **Step 1: Client Sends Request**

```bash
curl -X POST http://localhost:8080/scim/v2/Users \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "userName": "john.doe@example.com",
    "name": {
      "givenName": "John",
      "familyName": "Doe"
    },
    "emails": [{
      "value": "john.doe@example.com",
      "primary": true,
      "type": "work"
    }],
    "active": true,
    "displayName": "John Doe"
  }'
```

---

### **Step 2: Spring Security Filter Intercepts Request**

**File**: `JwtAuthenticationFilter.java`

```
Request arrives
    ↓
Extract JWT from Authorization header
    ↓
Validate token (signature, expiration)
    ↓
Set authentication in SecurityContext
    ↓
Allow request to proceed
```

**If token is invalid**: Returns `401 Unauthorized`  
**If token is valid**: Continues to controller

---

### **Step 3: Controller Receives Request**

**File**: `ScimUserController.java` (Line 31-34)

```java
@PostMapping
public ResponseEntity<ScimUser> createUser(@RequestBody ScimUser user) {
    ScimUser created = userService.createUser(user);  // ← Calls service
    return ResponseEntity.status(HttpStatus.CREATED).body(created);  // Returns 201
}
```

**What happens here:**
- Spring converts JSON → `ScimUser` object
- Calls `ScimUserService.createUser()`
- Waits for result
- Returns HTTP 201 (Created) with user JSON

---

### **Step 4: Service Layer - Business Logic**

**File**: `ScimUserService.java` (Line 27-52)

#### **4A: Validate Required Fields**

```java
if (scimUser.getUserName() == null || scimUser.getUserName().trim().isEmpty()) {
    throw new RuntimeException("userName is required");  // ← Returns 400 error
}
```

**If userName is missing**: Throws error → `400 Bad Request`  
**If userName exists**: Continues

#### **4B: Convert SCIM User → MongoDB User**

```java
MongoUser mongoUser = fromScimUser(scimUser);
```

**Transformation:**
```
SCIM User (from request)           MongoDB User (internal)
┌─────────────────────────┐        ┌─────────────────────────┐
│ userName                │   →    │ userName                │
│ name.givenName          │   →    │ nameGiven               │
│ name.familyName         │   →    │ nameFamily              │
│ emails[]                │   →    │ emails[]                │
│ active                  │   →    │ active                  │
│ displayName             │   →    │ displayName             │
└─────────────────────────┘        └─────────────────────────┘
```

#### **4C: Generate UUID and Timestamps**

```java
// Generate unique ID
mongoUser.setId(UUID.randomUUID().toString());  
// Example: "550e8400-e29b-41d4-a716-446655440000"

// Set timestamps
String now = Instant.now().toString();  
// Example: "2026-04-13T09:30:45.123Z"

mongoUser.setMetaCreatedAt(now);
mongoUser.setMetaLastModified(now);
mongoUser.setMetaLocation("/scim/v2/Users/" + mongoUser.getId());
```

**Result:**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "userName": "john.doe@example.com",
  "meta": {
    "created": "2026-04-13T09:30:45.123Z",
    "lastModified": "2026-04-13T09:30:45.123Z",
    "location": "/scim/v2/Users/550e8400-e29b-41d4-a716-446655440000"
  }
}
```

#### **4D: Save to MongoDB**

**File**: `MongoUserAdapter.java` (Line 20-22)

```java
public MongoUser save(MongoUser user) {
    return mongoUserRepo.save(user);  // ← Spring Data MongoDB
}
```

**What happens:**
```
MongoUser object
    ↓
Spring Data MongoDB
    ↓
MongoDB Driver
    ↓
MongoDB Atlas Cloud
    ↓
Collection: "users"
    ↓
Document inserted:
{
  "_id": "550e8400-e29b-41d4-a716-446655440000",
  "userName": "john.doe@example.com",
  "nameGiven": "John",
  "nameFamily": "Doe",
  "emails": [...],
  "active": true,
  "metaCreated": "2026-04-13T09:30:45.123Z",
  "metaLastModified": "2026-04-13T09:30:45.123Z"
}
```

#### **4E: Convert Back to SCIM User**

```java
return toScimUser(saved);
```

**MongoDB → SCIM response format:**
```json
{
  "schemas": ["urn:ietf:params:scim:schemas:core:2.0:User"],
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "userName": "john.doe@example.com",
  "name": {
    "givenName": "John",
    "familyName": "Doe"
  },
  "emails": [{
    "value": "john.doe@example.com",
    "primary": true,
    "type": "work"
  }],
  "displayName": "John Doe",
  "active": true,
  "meta": {
    "resourceType": "User",
    "created": "2026-04-13T09:30:45.123Z",
    "lastModified": "2026-04-13T09:30:45.123Z",
    "location": "http://localhost:8080/scim/v2/Users/550e8400-e29b-41d4-a716-446655440000"
  }
}
```

---

### **Step 5: Auto-Provisioning (If Enabled)**

**This happens asynchronously (in background)**

#### **Check if any apps have autoProvision=true**

```java
List<Application> apps = appRepository.findByAutoProvisionTrueAndEnabledTrue();
```

**Scenario A: No apps registered**  
→ Skip provisioning  
→ User only exists in your gateway

**Scenario B: Apps registered with autoProvision=true**  
→ For each app, create provisioning job

#### **Create Provisioning Job**

```java
ProvisioningJob job = new ProvisioningJob();
job.setId(UUID.randomUUID().toString());
job.setUserId(userId);
job.setApplicationId(appId);
job.setOperation(ProvisioningJob.Operation.CREATE);
job.setStatus(ProvisioningJob.Status.PENDING);
job.setAttempts(0);
job.setMaxRetries(3);  // Default
job.setCreatedAt(Instant.now().toString());
```

**Job saved to MongoDB:**
```json
{
  "id": "job-uuid-here",
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "applicationId": "app-uuid-here",
  "operation": "CREATE",
  "status": "PENDING",
  "attempts": 0,
  "maxRetries": 3,
  "createdAt": "2026-04-13T09:30:45.123Z"
}
```

#### **Execute Provisioning (Background Thread)**

**File**: `ProvisioningService.java` - `executeProvisioning()` method

```
For each application with autoProvision:
    ↓
Get app configuration (baseUrl, apiKey, fieldMappings)
    ↓
Apply field mappings (transform user attributes)
    ↓
Send HTTP POST to external app's SCIM endpoint
    ↓
External app responds
    ↓
Update job status (SUCCESS or FAILED)
    ↓
If failed, retry up to maxRetries times
```

**Example: Provisioning to Azure AD**

```java
// Get app config
Application app = appRepository.findById(appId);
// app.baseUrl = "https://tenant.provisioning.azure-api.net"
// app.apiKey = "azure-token-here"

// Apply field mappings
ScimUser mappedUser = fieldMappingService.applyMapping(user, app);

// Send to Azure AD
ExternalScimClient.ScimResponse response = externalClient.createUser(
    app.getBaseUrl(),
    app.getApiKey(),
    mappedUser
);

// Update job status
if (response.isSuccess()) {
    job.setStatus(ProvisioningJob.Status.SUCCESS);
    job.setResponseData(response.getUser().toString());
} else {
    job.setStatus(ProvisioningJob.Status.FAILED);
    job.setErrorMessage(response.getError().getDetail());
}
```

**External SCIM Request:**
```bash
POST https://tenant.provisioning.azure-api.net/scim/v2/Users
Authorization: Bearer azure-token-here
Content-Type: application/json

{
  "userName": "john.doe@example.com",
  "name": {
    "givenName": "John",
    "familyName": "Doe"
  },
  "emails": [{
    "value": "john.doe@example.com",
    "primary": true
  }],
  "active": true
}
```

**Job Status Updates:**

| Attempt | Status | Timestamp |
|---------|--------|-----------|
| 1 | RETRYING | 2026-04-13T09:30:45.200Z |
| 1 | FAILED (500 error) | 2026-04-13T09:30:46.500Z |
| 2 | RETRYING | 2026-04-13T09:35:46.500Z |
| 2 | SUCCESS | 2026-04-13T09:35:47.800Z |

---

### **Step 6: Response Sent to Client**

**HTTP Response:**
```
HTTP/1.1 201 Created
Content-Type: application/json
Location: http://localhost:8080/scim/v2/Users/550e8400-e29b-41d4-a716-446655440000

{
  "schemas": ["urn:ietf:params:scim:schemas:core:2.0:User"],
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "userName": "john.doe@example.com",
  "name": {
    "givenName": "John",
    "familyName": "Doe"
  },
  "emails": [{
    "value": "john.doe@example.com",
    "primary": true,
    "type": "work"
  }],
  "displayName": "John Doe",
  "active": true,
  "meta": {
    "resourceType": "User",
    "created": "2026-04-13T09:30:45.123Z",
    "lastModified": "2026-04-13T09:30:45.123Z",
    "location": "http://localhost:8080/scim/v2/Users/550e8400-e29b-41d4-a716-446655440000",
    "version": "W/\"1234567890\""
  }
}
```

---

## 🔍 Visual Flow Diagram

```
┌─────────────┐
│   Client    │  (IdP, Admin, API consumer)
└──────┬──────┘
       │
       │ POST /scim/v2/Users
       │ Authorization: Bearer <token>
       │ Body: { userName, name, emails, ... }
       ▼
┌──────────────────────────┐
│  Spring Security Filter  │  Validates JWT token
└──────────┬───────────────┘
           │ Token valid?
           ▼
┌──────────────────────────┐
│  ScimUserController      │  Receives request
│  (Line 31-34)            │  Returns 201 response
└──────────┬───────────────┘
           │
           ▼
┌──────────────────────────┐
│  ScimUserService         │  Business logic
│  (Line 27-52)            │
│                          │
│  1. Validate userName    │  ← 400 if missing
│  2. Convert to MongoUser │
│  3. Generate UUID        │
│  4. Set timestamps       │
└──────────┬───────────────┘
           │
           ▼
┌──────────────────────────┐
│  MongoUserAdapter        │  Data access layer
│  (Line 20-22)            │
└──────────┬───────────────┘
           │
           ▼
┌──────────────────────────┐
│   MongoDB Atlas          │  Persistent storage
│   Collection: "users"    │  Document inserted
└──────────┬───────────────┘
           │
           │ User saved!
           ▼
┌──────────────────────────┐
│  Check Auto-Provisioning │  Async background task
└──────────┬───────────────┘
           │
    ┌──────┴──────┐
    │             │
    ▼             ▼
No Apps       Apps Found
    │             │
    │             │ Create ProvisioningJob
    │             │ Send to external SCIM endpoint
    │             │ Update job status
    │             │
    │             └──────────────┐
    │                            │
    ▼                            ▼
┌──────────────────────────────────────┐
│         Return 201 Response          │
│   { id, userName, name, emails... }  │
└──────────────────────────────────────┘
           │
           ▼
┌─────────────┐
│   Client    │  Receives created user
└─────────────┘
```

---

## 📦 Data Transformations

### **1. Request → Internal Format**

**Client Sends (SCIM JSON):**
```json
{
  "userName": "john.doe@example.com",
  "name": {
    "givenName": "John",
    "familyName": "Doe"
  },
  "emails": [{
    "value": "john.doe@example.com",
    "type": "work",
    "primary": true
  }]
}
```

**Becomes (Java ScimUser):**
```java
ScimUser {
  userName: "john.doe@example.com"
  name: Name {
    givenName: "John"
    familyName: "Doe"
  }
  emails: List<Email> {
    Email {
      value: "john.doe@example.com"
      type: "work"
      primary: true
    }
  }
}
```

**Becomes (MongoUser for DB):**
```java
MongoUser {
  id: "550e8400-e29b-41d4-a716-446655440000"
  userName: "john.doe@example.com"
  nameGiven: "John"
  nameFamily: "Doe"
  emails: List<Email> { ... }
  metaCreated: "2026-04-13T09:30:45.123Z"
  metaLastModified: "2026-04-13T09:30:45.123Z"
  metaLocation: "/scim/v2/Users/550e8400-e29b-41d4-a716-446655440000"
}
```

**Stored in MongoDB (BSON Document):**
```json
{
  "_id": "550e8400-e29b-41d4-a716-446655440000",
  "userName": "john.doe@example.com",
  "nameGiven": "John",
  "nameFamily": "Doe",
  "emails": [
    {
      "value": "john.doe@example.com",
      "type": "work",
      "primary": true
    }
  ],
  "active": true,
  "metaCreated": "2026-04-13T09:30:45.123Z",
  "metaLastModified": "2026-04-13T09:30:45.123Z",
  "metaLocation": "/scim/v2/Users/550e8400-e29b-41d4-a716-446655440000"
}
```

---

## ⚡ Performance & Timing

### **Synchronous (Client Waits)**
- Validation: ~1ms
- Object conversion: ~2ms
- UUID generation: <1ms
- MongoDB save: ~50-100ms (network to Atlas)
- **Total: ~100-150ms**

### **Asynchronous (Background)**
- Provisioning job creation: ~10ms
- External API call: ~500-2000ms (doesn't block client)
- Retry logic: Spans minutes (if failures occur)

**Client receives response in ~100-150ms**, provisioning happens in background!

---

## 🔐 Security Checks

### **Before User Creation:**

1. **JWT Token Validation** (JwtAuthenticationFilter)
   - Token exists?
   - Signature valid?
   - Not expired?
   - ❌ Invalid → `401 Unauthorized`

2. **Username Validation** (ScimUserService)
   - `userName` field present?
   - Not empty/whitespace?
   - ❌ Missing → `400 Bad Request`

3. **Duplicate Check** (MongoDB)
   - Username unique constraint
   - ❌ Duplicate → `409 Conflict`

---

## 🎛️ Configuration Points

### **In application.properties:**

```properties
# Max results per page
scim.max-results=100

# Default page size
scim.default-count=20

# SCIM base URL
scim.base-url=/scim/v2
```

### **MongoDB Connection:**

```properties
spring.data.mongodb.uri=mongodb+srv://user:pass@cluster.mongodb.net/scimdb
```

---

## 🐛 Common Issues

### **Issue 1: "userName is required" Error**

**Cause:** Missing userName in request  
**Fix:** Include userName field

```json
{
  "userName": "john@example.com",  // ← Required!
  "name": { ... }
}
```

### **Issue 2: 401 Unauthorized**

**Cause:** Invalid or missing JWT token  
**Fix:** Get new token via `/auth/login`

### **Issue 3: MongoDB Connection Error**

**Cause:** Wrong connection string or network issue  
**Fix:** Check `spring.data.mongodb.uri` in application.properties

### **Issue 4: Provisioning Fails**

**Cause:** External app unreachable or wrong API key  
**Check:** 
```bash
curl http://localhost:8080/scim/v2/apps/jobs/status/FAILED \
  -H "Authorization: Bearer TOKEN"
```

---

## 📊 Monitoring User Creation

### **Check MongoDB:**

```javascript
// Connect to MongoDB
use scimdb

// View all users
db.users.find().pretty()

// Count users
db.users.count()

// Find specific user
db.users.findOne({ userName: "john.doe@example.com" })
```

### **Check Provisioning Jobs:**

```bash
# All jobs
curl http://localhost:8080/scim/v2/apps/jobs \
  -H "Authorization: Bearer TOKEN"

# Failed jobs
curl http://localhost:8080/scim/v2/apps/jobs/status/FAILED \
  -H "Authorization: Bearer TOKEN"

# Jobs for specific user
curl http://localhost:8080/scim/v2/apps/jobs/user/{userId} \
  -H "Authorization: Bearer TOKEN"
```

### **Check Application Logs:**

```bash
# Watch logs in real-time
tail -f logs/spring-boot.log

# Search for user creation
grep "createUser" logs/spring-boot.log

# Search for provisioning errors
grep "FAILED" logs/spring-boot.log
```

---

## 🎯 Summary

When you create a user:

| Step | What Happens | Time | Sync/Async |
|------|--------------|------|------------|
| 1 | JWT validation | ~5ms | Sync |
| 2 | Validate userName | ~1ms | Sync |
| 3 | Convert to MongoUser | ~2ms | Sync |
| 4 | Generate UUID + timestamps | <1ms | Sync |
| 5 | Save to MongoDB | ~50-100ms | Sync |
| 6 | Convert back to SCIM | ~2ms | Sync |
| 7 | Return 201 response | ~5ms | Sync |
| 8 | Create provisioning jobs | ~10ms | Async |
| 9 | Provision to external apps | ~500-2000ms | Async |

**Client gets response in ~100-150ms**  
**Provisioning continues in background**

---

## 🔗 Related Files

- **Controller**: `ScimUserController.java` (Line 31-34)
- **Service**: `ScimUserService.java` (Line 27-52)
- **Adapter**: `MongoUserAdapter.java` (Line 20-22)
- **Repository**: `MongoUserRepository.java`
- **Model**: `MongoUser.java`
- **Provisioning**: `ProvisioningService.java`
- **External Client**: `ExternalScimClient.java`
