# Application Documentation

## Table of Contents
- [Overview](#overview)
- [1. Backend](#1-backend)
  - [1.1 General Information](#11-general-information)
  - [1.2 Root Files](#12-root-files)
  - [1.3 Root Folders](#13-root-folders)
  - [1.4 Source Structure (`src`)](#14-source-structure-src)
    - [1.4.1 `main`](#141-main)
    - [1.4.2 `test`](#142-test)
  - [1.5 Main Java Modules (`main/java`)](#15-main-java-modules-mainjava)
  - [1.6 Auth Module (`main/java/auth`)](#16-auth-module-mainjavaauth)
  - [1.7 Users Module (`main/java/users`)](#17-users-module-mainjavausers)

## Overview
This document describes the structure and processes of the backend application, including configuration files, folder organization, and module responsibilities.  

![Frontend and Backend Architecture](frontend_backend_auth_architecture.png)  
*Illustrates interactions between the frontend, backend, and `spring-auth` modules.*

---

## 1. Backend

### 1.1 General Information

**Tools & Dependencies:**
- Java / OpenJDK 21  
- Spring Boot 3.3.5  
- Maven 3.9  
- MariaDB 11.4  
- Docker Desktop  

> **Note:** Detailed setup and run instructions are provided in the project’s README.

---

### 1.2 Root Files

| File | Description |
|------|-------------|
| `pom.xml` | Defines project dependencies, plugins, and build configurations. |
| `init.sql` | SQL script to create and initialize the database schema. |
| `Dockerfile` | Defines Docker image build stages and application setup. |
| `compose.yml` | Configures Docker environment and additional services. |
| `application.properties` | Spring Boot global configuration properties. |
| `.env` | Environment variables for local development or deployment. |

---

### 1.3 Root Folders

| Folder | Description |
|--------|-------------|
| `src` | Contains the application’s source code and resources. |
| `target` | Contains compiled classes and build artifacts. |
| `docs` | Auto-generated REST API documentation (HTML format). |
| `doc` | Manually created documentation (designs, requirements, diagrams). |

---

### 1.4 Source Structure (`src`)

#### 1.4.1 `main`
Contains the core functional processes of the application:  

- **`java`** – Application source code (controllers, services, entities, etc.)  
- **`resources`** – Configuration files, static resources, templates  

#### 1.4.2 `test`
Contains test classes for unit and integration tests:  

- **`java`** – Test classes corresponding to the application source code  
- **`resources`** – Test-specific configuration or resources  

> *Testing frameworks, execution instructions, and coverage details will be documented once the Java part is complete.*

---

### 1.5 Main Java Modules (`main/java`)

| Module | Responsibility |
|--------|----------------|
| `app` | Error and exception handling used throughout the application. |
| `auth` | Handles authorization processes: login, regidter. (Some fonctionalities moved to `spring-auth` branch) |
| `item` | Manages stock and inventory functionalities. (not implemented yet) |
| `security` | Security-related classes: JWT filters, password encoding, authentication management. |
| `users` | Manages user profiles, roles, and permissions. |
| `TemplateApplication.java` | Main Spring Boot entry point containing `main()` method. Run the project from this class. |

---

### 1.6 Auth Module (`main/java/auth`)

| File | Description |
|------|-------------|
| `AuthController.java` | Authentication endpoints (fonctionalities moved to `spring-auth` branch). |
| `CredentialsDto.java` | Data transfer object for login credentials. |
| `OAuth2Controller.java` | Handles OAuth2 and Azure operations (currently only success scenario, fonctionalities moved to `spring-auth`). |
| `PasswordConfig.java` | Manages password encryption (fonctionalities moved to `spring-auth`). |
| `SignUpDto.java` | Data transfer object for registration functionalities. |

> **Reference:** For full authentication implementation, see the [`spring-auth`](https://github.com/OrifInformatique/spring-auth) branch/repository.

---

### 1.7 Users Module (`main/java/users`)

| File | Description |
|------|-------------|
| `User.java` | User entity class representing a user in the system. |
| `UserController.java` | Contains user-related REST endpoints (CRUD, profile management, etc.). | 
| `UserDto.java` | Data transfer object for communication between the backend and frontend. |
| `UserMapper.java` | Handles conversion between `User` entities and `UserDto` objects. |
| `UserRepository.java` | Interface for database operations related to users. |
| `UserSeeder.java` | Seeds the database with test users for development and testing. |
| `UserService.java` | Business logic and services managing user functionalities (creation, update, role assignment, etc.). |