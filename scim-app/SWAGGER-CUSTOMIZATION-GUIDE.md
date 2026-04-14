# Swagger UI Customization Guide

## 📝 Quick Start

All Swagger UI content is now controlled by a single configuration file:

**File Location**: `src/main/resources/swagger-config.properties`

Edit this file to customize your API documentation without touching any Java code!

---

## 🎯 What You Can Customize

### 1. **API Information** (Top of the page)

```properties
# Change the title
swagger.api.title=Your Custom API Name

# Change the version
swagger.api.version=2.0.0

# Change the description (supports Markdown)
swagger.api.description=## Your Custom Description\n\nWrite anything here!
```

### 2. **Contact Information** (Bottom of the page)

```properties
# Your name/company
swagger.contact.name=Your Name

# Your email
swagger.contact.email=your.email@example.com

# Your website
swagger.contact.url=https://your-website.com
```

### 3. **License Information**

```properties
swagger.license.name=MIT
swagger.license.url=https://opensource.org/licenses/MIT
```

### 4. **API Tags** (Endpoint categories)

```properties
# Change tag description
swagger.tags.Authentication=🔒 Your custom auth description

# Add external documentation link
swagger.tags.Authentication.docs.url=https://your-docs.com/auth
swagger.tags.Authentication.docs.description=Auth Documentation
```

### 5. **Authentication Instructions**

```properties
# Customize the Authorize popup text
swagger.security.description=Your custom authentication instructions here
```

### 6. **Quick Start Examples**

```properties
# Add your own curl examples
swagger.examples.login.title=Your Example Title
swagger.examples.login.command=curl -X POST ...
```

---

## 🎨 Markdown Support

The description field supports full Markdown syntax:

```properties
# Headers
swagger.api.description=## Main Header

### Sub Header

#### Smaller Header

# Bold and Italic
**Bold Text** and *Italic Text*

# Lists
- Item 1\n- Item 2\n- Item 3

# Tables
| Column 1 | Column 2 |\n|----------|----------|\n| Data 1   | Data 2   |

# Code Blocks
```bash\ncurl http://localhost:8080\n```

# Links
[Click Here](https://example.com)
```

**Important**: Use `\n` for new lines in the properties file!

---

## 📋 Common Customizations

### Change API Title

```properties
swagger.api.title=My Awesome API - Documentation
```

### Change Developer Name

```properties
swagger.contact.name=John Doe
```

### Update Support Email

```properties
swagger.contact.email=support@mycompany.com
```

### Change GitHub URL

```properties
swagger.contact.url=https://github.com/yourusername
swagger.support.github=https://github.com/yourusername/yourrepo
```

### Customize Features List

```properties
swagger.features.user-management=⭐ **Feature 1** | Description here
swagger.features.group-management=🚀 **Feature 2** | Another description
swagger.features.filtering=💡 **Feature 3** | More details
```

### Add New API Tag

```properties
# Add to swagger-config.properties
swagger.tags.YourTag=🎯 Your tag description
swagger.tags.YourTag.docs.url=https://docs.example.com
swagger.tags.YourTag.docs.description=External Docs
```

---

## 🔧 Advanced Customizations

### Change Server URL

```properties
swagger.server.url=https://api.yourdomain.com
swagger.server.description=Production Server
```

### Customize UI Behavior

```properties
# Enable/disable features
swagger.ui.try-it-out-enabled=true
swagger.ui.filter-enabled=true
swagger.ui.deep-linking=true

# Sorting
swagger.ui.operations-sorter=method  # or 'alpha'
swagger.ui.tags-sorter=alpha
```

### Multiple Servers (Development/Production)

```properties
# Development
swagger.server.dev.url=http://localhost:8080
swagger.server.dev.description=Development Server

# Production
swagger.server.prod.url=https://api.yourdomain.com
swagger.server.prod.description=Production Server
```

---

## 🌐 Applying Changes

### Option 1: Hot Reload (Recommended)

The application automatically reloads when you save the file:

1. Edit `swagger-config.properties`
2. Save the file
3. Refresh browser at http://localhost:8080/swagger-ui.html

### Option 2: Restart Application

If hot reload doesn't work:

```bash
# Stop the application (Ctrl+C)
# Then restart
mvn spring-boot:run
```

---

## 📊 Configuration Structure

```
swagger-config.properties
├── API Information
│   ├── swagger.api.title
│   ├── swagger.api.version
│   └── swagger.api.description
├── Contact Information
│   ├── swagger.contact.name
│   ├── swagger.contact.email
│   └── swagger.contact.url
├── License
│   ├── swagger.license.name
│   └── swagger.license.url
├── Authentication
│   ├── swagger.security.scheme-name
│   └── swagger.security.description
├── API Tags
│   ├── swagger.tags.<TagName>
│   └── swagger.tags.<TagName>.docs.*
├── Features
│   └── swagger.features.*
├── Examples
│   └── swagger.examples.*
└── UI Settings
    └── swagger.ui.*
```

---

## 💡 Tips & Best Practices

### 1. **Use Emojis in Tags**
```properties
swagger.tags.Authentication=🔐 Authentication
swagger.tags.Users=👤 Users
swagger.tags.Reports=📊 Reports
```

### 2. **Keep Descriptions Concise**
- Use clear, brief descriptions
- Avoid technical jargon
- Include examples where helpful

### 3. **Organize with Markdown**
```properties
swagger.api.description=## Overview

This API provides...

### Key Features
- Feature 1
- Feature 2

### Quick Start
See examples below...
```

### 4. **Test Changes Incrementally**
1. Make one change at a time
2. Save and refresh browser
3. Verify the change appears correctly
4. Move to next change

### 5. **Backup Before Major Changes**
```bash
# Backup the config file
cp swagger-config.properties swagger-config.properties.backup
```

---

## 🎨 Example: Complete Rebranding

```properties
# ==========================================
# My Custom API Documentation
# ==========================================

# API Info
swagger.api.title=CloudID - Identity Management API
swagger.api.version=2.0.0
swagger.api.description=## ☁️ CloudID Platform

Enterprise identity management solution for modern applications.

### Why CloudID?
- 🚀 Lightning fast provisioning
- 🔒 Bank-grade security
- 🌍 Global availability

### Get Started
Follow the examples below to integrate CloudID into your application.

# Contact
swagger.contact.name=Jane Smith
swagger.contact.email=jane.smith@cloudid.com
swagger.contact.url=https://cloudid.com

# License
swagger.license.name=MIT
swagger.license.url=https://opensource.org/licenses/MIT

# Server
swagger.server.url=https://api.cloudid.com
swagger.server.description=CloudID Production API

# Custom Features
swagger.features.real-time-sync=⚡ **Real-time Sync** | Instant synchronization across all apps
swagger.features.analytics=📈 **Analytics** | Detailed usage reports and insights
swagger.features.sso=🔑 **Single Sign-On** | One login for all applications
```

---

## 🐛 Troubleshooting

### Changes Not Appearing?

1. **Check file syntax**
   - No spaces before property names
   - Use `=` between key and value
   - Use `\n` for new lines

2. **Verify file location**
   - Must be in `src/main/resources/`
   - File name: `swagger-config.properties`

3. **Restart application**
   ```bash
   # Stop and restart
   mvn spring-boot:run
   ```

4. **Clear browser cache**
   - Press `Ctrl+F5` (Windows) or `Cmd+Shift+R` (Mac)

### Markdown Not Rendering?

- Ensure you're using `\n` not actual new lines
- Check for proper Markdown syntax
- Avoid unescaped special characters

---

## 📚 Resources

- [Markdown Guide](https://www.markdownguide.org/)
- [OpenAPI Specification](https://swagger.io/specification/)
- [Swagger UI Documentation](https://swagger.io/docs/open-source-tools/swagger-ui/)

---

## 🎯 Quick Reference

| What to Change | Property | Example |
|----------------|----------|---------|
| API Title | `swagger.api.title` | `My API - Docs` |
| API Version | `swagger.api.version` | `2.0.0` |
| Your Name | `swagger.contact.name` | `John Doe` |
| Email | `swagger.contact.email` | `john@example.com` |
| Website | `swagger.contact.url` | `https://example.com` |
| Server URL | `swagger.server.url` | `https://api.example.com` |
| Auth Instructions | `swagger.security.description` | Custom text |
| Tag Description | `swagger.tags.<Name>` | `👤 Users` |

---

**Happy Customizing! 🎨**

Edit `swagger-config.properties` and see your changes instantly!
