# SCIM 2.0 Gateway - Complete Documentation

##  Table of Contents
1. [Overview](#overview)
2. [Architecture](#architecture)
3. [Quick Start](#quick-start)
4. [Authentication](#authentication)
5. [API Reference](#api-reference)
6. [SCIM User Management](#scim-user-management)
7. [SCIM Group Management](#scim-group-management)
8. [Application Registry](#application-registry)
9. [Provisioning Engine](#provisioning-engine)
10. [Field Mapping](#field-mapping)
11. [Job Tracking](#job-tracking)
12. [Testing with Swagger](#testing-with-swagger)
13. [Configuration](#configuration)
14. [Production Deployment](#production-deployment)

---

##  Overview

The **SCIM 2.0 Gateway** is a comprehensive identity management system built with Spring Boot 4.0.5 that implements the SCIM (System for Cross-domain Identity Management) 2.0 specification (RFC 7643 & RFC 7644).

### Key Features

 **SCIM 2.0 Compliant** - Full RFC 7643/7644 implementation  
 **User Lifecycle Management** - Create, Read, Update, Delete users  
 **Group Management** - Manage user groups and memberships  
 **Advanced Filtering** - SCIM filter expressions (eq, ne, co, sw, ew, gt, ge, lt, le)  
 **Multi-Application Support** - Register and manage external apps  
 **Automated Provisioning** - Async user provisioning to external systems  
 **Field Mapping Engine** - Transform attributes per application  
 **JWT Authentication** - Secure token-based auth  
 **MongoDB Atlas** - Cloud-native database persistence  
 **Swagger/OpenAPI** - Interactive API documentation  
 **Job Tracking** - Monitor provisioning status and retries  

### Technology Stack

- **Framework**: Spring Boot 4.0.5
- **Language**: Java 21 (LTS)
- **Database**: MongoDB Atlas (cloud)
- **Authentication**: JWT (JSON Web Tokens)
- **API Documentation**: SpringDoc OpenAPI 3.0
- **Build Tool**: Maven
- **Specification**: SCIM 2.0 (RFC 7643, 7644)

---

##  Architecture

### Layered Architecture

```

           Presentation Layer                    
       
    SCIM         Auth        Provisioning 
  Controllers   Controller    Controller  
       

                      

           Service Layer                         
     
  ScimUser   ScimGroup  Provisioning      
  Service    Service    Service           
     
     
  Field      Application Jwt              
  Mapping    Registry   Service           
     

                      

           Data Access Layer                     
     
  MongoUser       Application/Job           
  Repository      Repositories              
     

                      

           Database Layer                        
              MongoDB Atlas                      

```

### Data Flow

**User Creation Flow:**
1. Client sends POST request to `/scim/v2/Users`
2. ScimUserController validates request
3. ScimUserService applies business logic
4. MongoUserAdapter saves to MongoDB
5. Response with created user returned

**Provisioning Flow:**
1. Client triggers provisioning via `/scim/v2/apps/provision/{userId}/to/{appId}`
2. ProvisioningService creates async job
3. FieldMappingService transforms user attributes
4. ExternalScimClient calls external API
5. Job status updated in MongoDB

---

##  Quick Start

### Prerequisites

- Java 21 (LTS) or higher
- Maven 3.6+
- MongoDB Atlas account (or local MongoDB)
- Internet connection (for MongoDB Atlas)

### Setup Instructions

#### 1. Clone and Navigate

```bash
cd d:\linux\P1\SCIM\scim-app\scim-app
```

#### 2. Configure MongoDB

Edit `src/main/resources/application-dev.properties`:

```properties
spring.data.mongodb.uri=mongodb+srv://anurag:cloud%40123@cluster0.nivrt0z.mongodb.net/scimdb?retryWrites=true&w=majority&authSource=admin
```

**Note**: Password must be URL-encoded (`@` becomes `%40`)

#### 3. Configure JWT Secret

Edit `src/main/resources/application.properties`:

```properties
jwt.secret=mzgfstNYLvwe0qYr52P1IfCGxjgGHmENXJKntcSArvw=
jwt.expiration=86400000
```

#### 4. Build and Run

```bash
.\mvnw.cmd spring-boot:run
```

#### 5. Access Applications

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **API Docs**: http://localhost:8080/api-docs
- **Health Check**: http://localhost:8080/actuator/health

---

##  Authentication

### JWT Token-Based Authentication

The SCIM Gateway uses JWT (JSON Web Tokens) for secure API authentication.

### How It Works

```
                                    
 Client                      Server                       API   
                                    
                                                               
       POST /auth/login                                        
       {username, password}                                    
     >                              
                                                               
       Validate credentials                                    
       Generate JWT token                                      
                                                               
       {token, tokenType,                                      
        expiresIn, username}                                   
     <                              
                                                               
       GET /scim/v2/Users                                      
       Authorization: Bearer                                   
       <token>                                                 
     >
                                                               
                                   Validate token signature    
                                   Check expiration            
                                   Extract claims              
                                                               
       200 OK {user data}                                      
     <
```

### Getting a Token

**Endpoint**: `POST /auth/login`

**Request**:
```json
{
  "username": "admin",
  "password": "admin123"
}
```

**Response**:
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsInJvbGUiOiJhZG1pbiIsInNjb3BlIjoic2NpbTpmdWxsIiwiaWF0IjoxNzEyNzQ2NDMwLCJleHAiOjE3MTI4MzI4MzB9.Xy8...",
  "tokenType": "Bearer",
  "expiresIn": 86400000,
  "username": "admin"
}
```

### Using the Token

Add to request headers:
```
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
```

**Example with curl**:
```bash
curl -X GET http://localhost:8080/scim/v2/Users \
  -H "Authorization: Bearer eyJhbGciOiJIUzUxMiJ9..."
```

### Token Validation

**Endpoint**: `POST /auth/validate`

**Request**:
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9..."
}
```

**Response (Valid)**:
```json
{
  "valid": true,
  "username": "admin",
  "message": "Token is valid"
}
```

**Response (Invalid)**:
```json
{
  "valid": false,
  "message": "Invalid or expired token"
}
```

### Default Credentials

- **Username**: `admin`
- **Password**: `admin123`

To change credentials, edit `AuthController.java`:
```java
private static final String DEFAULT_USERNAME = "your_username";
private static final String DEFAULT_PASSWORD = "your_password";
```

---

##  API Reference

### Base URL
```
http://localhost:8080
```

### API Endpoints Overview

#### Authentication Endpoints

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/auth/login` | Generate JWT token | No |
| POST | `/auth/validate` | Validate token | No |
| GET | `/auth/credentials` | Get test credentials | No |

#### SCIM User Endpoints

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/scim/v2/Users` | Create user | No |
| GET | `/scim/v2/Users` | List users | No |
| GET | `/scim/v2/Users/{id}` | Get user by ID | No |
| PUT | `/scim/v2/Users/{id}` | Update user (full) | No |
| PATCH | `/scim/v2/Users/{id}` | Patch user (partial) | No |
| DELETE | `/scim/v2/Users/{id}` | Delete user | No |

#### SCIM Group Endpoints

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/scim/v2/Groups` | Create group | No |
| GET | `/scim/v2/Groups` | List groups | No |
| GET | `/scim/v2/Groups/{id}` | Get group by ID | No |
| PUT | `/scim/v2/Groups/{id}` | Update group | No |
| DELETE | `/scim/v2/Groups/{id}` | Delete group | No |

#### Application Registry Endpoints

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/scim/v2/apps` | List all apps | No |
| GET | `/scim/v2/apps/{id}` | Get app details | No |
| POST | `/scim/v2/apps` | Register new app | No |
| PUT | `/scim/v2/apps/{id}/mappings` | Update field mappings | No |
| DELETE | `/scim/v2/apps/{id}` | Delete app | No |

#### Provisioning Endpoints

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/scim/v2/apps/provision/{userId}/to/{appId}` | Provision user to app | No |
| POST | `/scim/v2/apps/deprovision/{userId}/from/{appId}` | Deprovision user | No |
| GET | `/scim/v2/apps/jobs` | List all jobs | No |
| GET | `/scim/v2/apps/jobs/status/{status}` | Filter jobs by status | No |
| GET | `/scim/v2/apps/jobs/user/{userId}` | Get jobs for user | No |
| GET | `/scim/v2/apps/jobs/{jobId}` | Get job details | No |

#### SCIM Discovery Endpoints

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/scim/v2/ServiceProviderConfig` | SCIM server config | No |
| GET | `/scim/v2/ResourceTypes` | Available resource types | No |
| GET | `/scim/v2/Schemas` | SCIM schemas | No |

---

##  SCIM User Management

### Creating a User

**Endpoint**: `POST /scim/v2/Users`

**Request**:
```json
{
  "userName": "john.doe",
  "emails": [
    {
      "value": "john.doe@example.com",
      "primary": true
    }
  ],
  "name": {
    "givenName": "John",
    "familyName": "Doe"
  },
  "displayName": "John Doe",
  "active": true,
  "phoneNumbers": [
    {
      "value": "+1-555-123-4567",
      "type": "work"
    }
  ]
}
```

**Response** (201 Created):
```json
{
  "schemas": ["urn:ietf:params:scim:schemas:core:2.0:User"],
  "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "userName": "john.doe",
  "emails": [
    {
      "value": "john.doe@example.com",
      "primary": true
    }
  ],
  "name": {
    "givenName": "John",
    "familyName": "Doe"
  },
  "displayName": "John Doe",
  "active": true,
  "meta": {
    "resourceType": "User",
    "created": "2026-04-09T13:07:12.000Z",
    "lastModified": "2026-04-09T13:07:12.000Z",
    "location": "/scim/v2/Users/a1b2c3d4-e5f6-7890-abcd-ef1234567890"
  }
}
```

### Listing Users with Pagination

**Endpoint**: `GET /scim/v2/Users`

**Query Parameters**:
- `startIndex` (int, default: 1) - 1-based index of first result
- `count` (int, default: 20) - Number of results per page
- `filter` (string, optional) - SCIM filter expression
- `sortBy` (string, optional) - Attribute to sort by
- `sortOrder` (string, default: "ascending") - Sort order

**Example**:
```
GET /scim/v2/Users?startIndex=1&count=10&sortBy=userName&sortOrder=ascending
```

**Response**:
```json
{
  "schemas": ["urn:ietf:params:scim:api:messages:2.0:ListResponse"],
  "totalResults": 25,
  "itemsPerPage": 10,
  "startIndex": 1,
  "Resources": [
    {
      "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
      "userName": "john.doe",
      "displayName": "John Doe",
      "active": true
    }
  ]
}
```

### Filtering Users

**SCIM Filter Operators**:
- `eq` - Equal to
- `ne` - Not equal to
- `co` - Contains
- `sw` - Starts with
- `ew` - Ends with
- `gt` - Greater than
- `ge` - Greater than or equal
- `lt` - Less than
- `le` - Less than or equal

**Examples**:

1. **Exact match**:
   ```
   GET /scim/v2/Users?filter=userName eq "john.doe"
   ```

2. **Contains**:
   ```
   GET /scim/v2/Users?filter=displayName co "John"
   ```

3. **Active users only**:
   ```
   GET /scim/v2/Users?filter=active eq true
   ```

4. **Email domain**:
   ```
   GET /scim/v2/Users?filter=emails.value sw "john@"
   ```

### Updating a User (PUT - Full Replace)

**Endpoint**: `PUT /scim/v2/Users/{id}`

**Request**:
```json
{
  "userName": "john.doe",
  "emails": [
    {
      "value": "john.newemail@example.com",
      "primary": true
    }
  ],
  "displayName": "John Doe Updated",
  "active": true
}
```

### Patching a User (PATCH - Partial Update)

**Endpoint**: `PATCH /scim/v2/Users/{id}`

**Operations**:
- `add` - Add attribute
- `remove` - Remove attribute
- `replace` - Replace attribute value

**Example 1 - Update single field**:
```json
{
  "Operations": [
    {
      "op": "replace",
      "path": "active",
      "value": false
    }
  ]
}
```

**Example 2 - Add email**:
```json
{
  "Operations": [
    {
      "op": "add",
      "path": "emails",
      "value": {
        "value": "john.personal@example.com",
        "type": "home"
      }
    }
  ]
}
```

**Example 3 - Multiple operations**:
```json
{
  "Operations": [
    {
      "op": "replace",
      "path": "displayName",
      "value": "John D."
    },
    {
      "op": "replace",
      "path": "active",
      "value": true
    }
  ]
}
```

### Deleting a User

**Endpoint**: `DELETE /scim/v2/Users/{id}`

**Response**: 204 No Content

---

##  SCIM Group Management

### Creating a Group

**Endpoint**: `POST /scim/v2/Groups`

**Request**:
```json
{
  "displayName": "Engineering Team",
  "members": [
    {
      "value": "user-id-1",
      "display": "john.doe"
    },
    {
      "value": "user-id-2",
      "display": "jane.smith"
    }
  ]
}
```

**Response** (201 Created):
```json
{
  "schemas": ["urn:ietf:params:scim:schemas:core:2.0:Group"],
  "id": "group-id-123",
  "displayName": "Engineering Team",
  "members": [
    {
      "value": "user-id-1",
      "display": "john.doe"
    },
    {
      "value": "user-id-2",
      "display": "jane.smith"
    }
  ],
  "meta": {
    "resourceType": "Group",
    "created": "2026-04-09T13:07:12.000Z",
    "lastModified": "2026-04-09T13:07:12.000Z"
  }
}
```

---

##  Application Registry

The Application Registry allows you to manage external applications that users can be provisioned to.

### Registering an Application

**Endpoint**: `POST /scim/v2/apps`

**Request**:
```json
{
  "name": "Salesforce",
  "baseUrl": "https://api.salesforce.com",
  "apiKey": "sf-api-key-12345",
  "description": "CRM System for sales team",
  "enabled": true,
  "autoProvision": false,
  "maxRetries": 3
}
```

**Response** (201 Created):
```json
{
  "id": "app-id-456",
  "name": "Salesforce",
  "baseUrl": "https://api.salesforce.com",
  "apiKey": "sf-api-key-12345",
  "description": "CRM System for sales team",
  "enabled": true,
  "autoProvision": false,
  "maxRetries": 3,
  "fieldMappings": {},
  "createdAt": "2026-04-09T13:07:12.000Z",
  "updatedAt": "2026-04-09T13:07:12.000Z"
}
```

### Updating Field Mappings

**Endpoint**: `PUT /scim/v2/apps/{appId}/mappings`

Field mappings define how SCIM user attributes map to application-specific fields.

**Request**:
```json
{
  "userName": "login",
  "emails": "contactEmail",
  "displayName": "fullName",
  "name.givenName": "firstName",
  "name.familyName": "lastName",
  "active": "isEnabled"
}
```

**How Field Mapping Works**:

```
SCIM User                    External App
                    
userName > login
emails[0].value > contactEmail
displayName > fullName
name.givenName > firstName
name.familyName > lastName
active > isEnabled
```

---

##  Provisioning Engine

The provisioning engine automatically creates, updates, or deletes user accounts in external applications.

### Provisioning a User

**Endpoint**: `POST /scim/v2/apps/provision/{userId}/to/{appId}`

**Example**:
```
POST /scim/v2/apps/provision/user-123/to/app-456
```

**What Happens**:

1. **Job Created**: ProvisioningJob saved to MongoDB with status `PENDING`
2. **User Fetched**: SCIM user retrieved from database
3. **Field Mapping**: User attributes transformed based on app's field mappings
4. **External API Call**: Transformed user sent to external application
5. **Status Updated**: Job marked as `SUCCESS` or `FAILED`
6. **Retry Logic**: If failed, job will be retried (max 3 times by default)

**Response**:
```json
{
  "status": "provisioning_started",
  "userId": "user-123",
  "appId": "app-456",
  "message": "User provisioning job created (async)"
}
```

### Provisioning Flow Diagram

```
                  
 Client           SCIM Gateway           External App  
                  
                                                  
      POST /provision                             
     >                        
                                                  
                           Create Job (PENDING)   
                                                  
      200 OK               Fetch User             
     <                        
                                                  
                           Transform Fields       
                                                  
                           POST /scim/v2/Users    
                          >
                                                  
                           201 Created            
                          <
                                                  
                           Update Job (SUCCESS)   
                                                  
```

### Deprovisioning a User

**Endpoint**: `POST /scim/v2/apps/deprovision/{userId}/from/{appId}`

Deletes user account from external application.

---

##  Field Mapping

### How Field Mapping Works

The FieldMappingService transforms SCIM user attributes into application-specific field names using dot notation.

### Supported Path Formats

1. **Simple fields**: `userName`, `displayName`, `active`
2. **Nested fields**: `name.givenName`, `name.familyName`
3. **Array fields**: `emails[0].value`, `phoneNumbers[0].value`

### Example Transformation

**SCIM User**:
```json
{
  "userName": "john.doe",
  "name": {
    "givenName": "John",
    "familyName": "Doe"
  },
  "emails": [
    {"value": "john@example.com", "primary": true}
  ],
  "active": true
}
```

**Field Mappings**:
```json
{
  "userName": "loginName",
  "name.givenName": "firstName",
  "name.familyName": "lastName",
  "emails[0].value": "emailAddress",
  "active": "enabled"
}
```

**Transformed User**:
```json
{
  "loginName": "john.doe",
  "firstName": "John",
  "lastName": "Doe",
  "emailAddress": "john@example.com",
  "enabled": true
}
```

---

##  Job Tracking

### Monitoring Provisioning Jobs

**List All Jobs**:
```
GET /scim/v2/apps/jobs
```

**Filter by Status**:
```
GET /scim/v2/apps/jobs/status/PENDING
GET /scim/v2/apps/jobs/status/SUCCESS
GET /scim/v2/apps/jobs/status/FAILED
```

**Get Jobs for User**:
```
GET /scim/v2/apps/jobs/user/{userId}
```

**Get Job Details**:
```
GET /scim/v2/apps/jobs/{jobId}
```

### Job Status Values

- `PENDING` - Job created, waiting to be processed
- `RETRYING` - Job being retried after failure
- `SUCCESS` - Job completed successfully
- `FAILED` - Job failed after max retries

### Job Response Example

```json
{
  "id": "job-id-789",
  "userId": "user-123",
  "applicationId": "app-456",
  "operation": "CREATE",
  "status": "SUCCESS",
  "attempts": 1,
  "maxRetries": 3,
  "createdAt": "2026-04-09T13:07:12.000Z",
  "updatedAt": "2026-04-09T13:07:15.000Z",
  "completedAt": "2026-04-09T13:07:15.000Z",
  "responseData": "Status: 201"
}
```

---

##  Testing with Swagger

### Accessing Swagger UI

Open your browser to: **http://localhost:8080/swagger-ui.html**

### Swagger Interface Overview

The Swagger UI provides:
- **Interactive API documentation**
- **Try it out** feature for testing endpoints
- **Request/response schemas**
- **Authentication support**
- **Filter and search** capabilities

### Step-by-Step Testing Guide

#### 1. Get Authentication Token

1. Scroll to **"Authentication"** section
2. Click on **POST /auth/login**
3. Click **"Try it out"**
4. Enter request body:
   ```json
   {
     "username": "admin",
     "password": "admin123"
   }
   ```
5. Click **"Execute"**
6. Copy the `token` value from response

#### 2. Authorize with Token

1. Click the ** Authorize** button (top right)
2. Enter: `Bearer your-token-here`
3. Click **"Authorize"**
4. Click **"Close"**

#### 3. Test User Creation

1. Find **"SCIM Users"** section
2. Click **POST /scim/v2/Users**
3. Click **"Try it out"**
4. Enter user data:
   ```json
   {
     "userName": "testuser",
     "emails": [{"value": "test@example.com", "primary": true}],
     "displayName": "Test User",
     "active": true
   }
   ```
5. Click **"Execute"**
6. View response with created user

#### 4. Test User Listing

1. Click **GET /scim/v2/Users**
2. Click **"Try it out"**
3. Set parameters:
   - `startIndex`: 1
   - `count`: 20
4. Click **"Execute"**
5. View paginated results

#### 5. Test Filtering

1. Click **GET /scim/v2/Users**
2. Click **"Try it out"**
3. Set `filter` parameter:
   ```
   userName eq "testuser"
   ```
4. Click **"Execute"**
5. View filtered results

#### 6. Test User Patching

1. Click **PATCH /scim/v2/Users/{id}**
2. Enter user ID in path parameter
3. Click **"Try it out"**
4. Enter patch operations:
   ```json
   {
     "Operations": [
       {
         "op": "replace",
         "path": "active",
         "value": false
       }
     ]
   }
   ```
5. Click **"Execute"**

---

##  Configuration

### Application Properties

**File**: `src/main/resources/application.properties`

```properties
# Application Settings
spring.application.name=scim-app
server.port=8080

# MongoDB Configuration
spring.data.mongodb.uri=mongodb+srv://user:pass@cluster.mongodb.net/scimdb

# SCIM Configuration
scim.max-results=100
scim.default-count=20
scim.base-url=/scim/v2

# Provisioning Configuration
scim.provisioning.sync-cron=0 0/5 * * * *
scim.provisioning.max-retries=3
scim.provisioning.job-retention-days=7

# JWT Configuration
jwt.secret=your-secret-key-here
jwt.expiration=86400000

# Swagger Configuration
springdoc.api-docs.path=/api-docs
springdoc.swagger-ui.path=/swagger-ui.html
springdoc.swagger-ui.operationsSorter=method
springdoc.swagger-ui.tagsSorter=alpha
springdoc.swagger-ui.tryItOutEnabled=true
springdoc.swagger-ui.filter=true

# Thread Pool Configuration
spring.task.scheduling.pool.size=5
spring.task.execution.pool.core-size=5
spring.task.execution.pool.max-size=10
```

### Spring Profiles

**Development (dev)**:
- Uses MongoDB Atlas
- In-memory user adapter for testing
- Security disabled for easy testing

**Production (prod)**:
- Uses PostgreSQL (commented out by default)
- Full security enabled
- Strict authentication

### Switching Profiles

Edit `application.properties`:
```properties
spring.profiles.active=dev
# spring.profiles.active=prod
```

---

##  Production Deployment

### Security Hardening

1. **Enable Authentication**:
   Edit `SecurityConfig.java` to require valid JWT tokens for all endpoints.

2. **Change Default Credentials**:
   ```java
   private static final String DEFAULT_USERNAME = "your_secure_username";
   private static final String DEFAULT_PASSWORD = "your_secure_password";
   ```

3. **Use Strong JWT Secret**:
   Generate a cryptographically secure secret:
   ```bash
   openssl rand -base64 32
   ```

4. **Enable HTTPS**:
   Configure SSL in `application.properties`:
   ```properties
   server.ssl.enabled=true
   server.ssl.key-store=classpath:keystore.p12
   server.ssl.key-store-password=yourpassword
   ```

### Database Migration

For production, consider using PostgreSQL instead of MongoDB:

1. Uncomment PostgreSQL dependencies in `pom.xml`
2. Configure PostgreSQL connection in `application-prod.properties`
3. Update repository interfaces for JPA

### Environment Variables

Set sensitive configuration via environment variables:

```bash
export SPRING_DATA_MONGODB_URI=mongodb+srv://user:pass@cluster.mongodb.net/scimdb
export JWT_SECRET=your-secret-key-here
export SERVER_PORT=8080
```

### Docker Deployment

Create `Dockerfile`:
```dockerfile
FROM eclipse-temurin:17-jdk-alpine
VOLUME /tmp
COPY target/scim-app-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
```

Build and run:
```bash
docker build -t scim-gateway .
docker run -p 8080:8080 \
  -e SPRING_DATA_MONGODB_URI=mongodb+srv://... \
  -e JWT_SECRET=your-secret \
  scim-gateway
```

### Monitoring

Add Spring Boot Actuator for health checks and metrics:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

Access endpoints:
- Health: http://localhost:8080/actuator/health
- Metrics: http://localhost:8080/actuator/metrics
- Info: http://localhost:8080/actuator/info

---

##  Common Use Cases

### Use Case 1: Synchronize Users to Multiple Apps

```bash
# 1. Register applications
curl -X POST http://localhost:8080/scim/v2/apps \
  -H "Content-Type: application/json" \
  -d '{"name":"Salesforce","baseUrl":"https://api.salesforce.com","apiKey":"sf-key"}'

curl -X POST http://localhost:8080/scim/v2/apps \
  -H "Content-Type: application/json" \
  -d '{"name":"Slack","baseUrl":"https://slack.com/api","apiKey":"slack-key"}'

# 2. Create user
curl -X POST http://localhost:8080/scim/v2/Users \
  -H "Content-Type: application/json" \
  -d '{"userName":"newuser","emails":[{"value":"new@example.com"}]}'

# 3. Provision to both apps
curl -X POST http://localhost:8080/scim/v2/apps/provision/{userId}/to/salesforce-app-id
curl -X POST http://localhost:8080/scim/v2/apps/provision/{userId}/to/slack-app-id
```

### Use Case 2: Bulk User Import

```bash
# Import users from CSV/JSON file
for user in $(cat users.json); do
  curl -X POST http://localhost:8080/scim/v2/Users \
    -H "Content-Type: application/json" \
    -d "$user"
done
```

### Use Case 3: Automated Provisioning on User Creation

Implement a Spring Event Listener to auto-provision users when created:

```java
@EventListener
public void onUserCreated(UserCreatedEvent event) {
    List<Application> apps = appRepository.findByAutoProvisionTrueAndEnabledTrue();
    for (Application app : apps) {
        provisioningService.provisionUserToApp(event.getUserId(), app.getId());
    }
}
```

---

##  Troubleshooting

### MongoDB Connection Issues

**Problem**: "Connection refused to localhost:27017"

**Solution**: 
- Check MongoDB Atlas connection string
- Ensure password is URL-encoded (`@`  `%40`)
- Verify network access in MongoDB Atlas (whitelist your IP)

### JWT Token Expired

**Problem**: 401 Unauthorized when using token

**Solution**:
- Token expires after 24 hours
- Generate new token via `/auth/login`
- Adjust `jwt.expiration` in properties

### Swagger Not Loading

**Problem**: 404 on /swagger-ui.html

**Solution**:
- Ensure application is running
- Check SpringDoc dependencies in pom.xml
- Verify `springdoc.swagger-ui.enabled=true`

### Provisioning Job Failing

**Problem**: Jobs stuck in FAILED status

**Solution**:
- Check external API URL and API key
- Review field mappings for errors
- Check job error message: `GET /scim/v2/apps/jobs/{jobId}`
- Jobs auto-retry up to `maxRetries` times

---

##  Support

For issues or questions:
- Check this documentation
- Review Swagger UI for endpoint details
- Inspect application logs for errors
- Check MongoDB Atlas for data issues

---

##  License

This project uses Apache 2.0 License.

---

**Version**: 1.0.0  
**Last Updated**: April 9, 2026  
**Spring Boot**: 4.0.5  
**Java**: 21.0.10 (LTS)  
