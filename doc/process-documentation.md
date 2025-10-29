# Application Documentation

## Overview
This document describes the structure and processes of the application, including backend setup, configuration files, and folder organization.

![Frontend and Backend Architecture](frontend_backend_auth_architecture.png)

---

## 1. Backend

### 1.1 General Information

#### 1.1.1 Tools Used
1. Java / OpenJDK 21
2. Spring Boot 3.3.5
3. Maven 3.9
4. MariaDB 11.4
5. Docker Desktop

---

### 1.2 Root Files

- **pom.xml**: Defines project dependencies, plugins, and build configurations.  
- **init.sql**: SQL script to create and initialize the database schema.  
- **Dockerfile**: Defines Docker image build stages and application setup.  
- **compose.yml**: Configures Docker environment and additional services.  
- **application.properties**: Spring Boot global configuration properties.  
- **.env**: Environment variables for local development or deployment.  

---

### 1.3 Root Folders

- **src**: Contains the application’s source code and related resources.  
- **target**: Contains compiled classes and build artifacts.  
- **docs**: Auto-generated REST documentation (HTML format) for GitHub.  
- **doc**: Manually created documentation related to the application (designs, requirements, etc.).  

---

### 1.4 src

#### 1.4.1 main
Contains the core functional processes of the application, including:

- **java**: Application source code (controllers, services, entities, etc.)  
- **resources**: Configuration files, static resources, templates  

#### 1.4.2 test
Contains tests for the application processes, including unit and integration tests:

- **java**: Test classes corresponding to the application source code  
- **resources**: Test-specific configuration or resources  

---

### 1.5 main/java

#### 1.5.1 app
Contains error and exception handling classes used throughout the application.  

#### 1.5.2 auth
Handles authorization processes, including login, token generation, and access control.  

#### 1.5.3 item
Manages stock and inventory functionalities, including CRUD operations for items.  

#### 1.5.4 security
Contains security-related classes, such as JWT filters, password encoding, and authentication management.  

#### 1.5.5 users
Manages user-related functionalities, including user profiles, roles, and permissions.  

#### 1.5.6 TemplateApplication.java
The main Spring Boot application entry point containing the `main()` method. The project should be run from this class.