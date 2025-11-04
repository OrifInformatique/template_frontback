# Application Documentation

## Table of Contents
- [Documentation Tools](#documentation-tools)
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
  - [1.8 Tests Controlleurs (`main/java/test`)](#18-tests-controlleurs-mainjavatest)
  - [1.9 Security Module (`main/java/security`)](#19-security-module-mainjavasecurity)
  - [1.10 Error and Exception Managment (`main/java/app`)](#110-error-and-exception-managment-mainjavaapp)
  - [1.11 Main Test Modules (`main/test`)](#111-main-test-modules-maintest)
- [Related Documentation](#related-documentation)
---

## Documentation Tools
Recomended Mermaid Preview Tool [Markdown Preview Mermaid Support](https://marketplace.visualstudio.com/items?itemName=bierner.markdown-mermaid).

---
## Overview

This document describes the **structure, components, and processes** of the backend application, including configuration files, folder organization, and module responsibilities.  

This backend powers a **multi-user test system**, providing:
- Secure authentication and authorization (delegated to spring-auth)
- User and role management  
- (Future) Stock and inventory tracking  

![Frontend and Backend Architecture](frontend_backend_auth_architecture.png)  
*Illustrates interactions between the frontend and backend modules, as well as the `spring-auth` app.*

---


## 1. Backend

### 1.1 General Information

**Tools & Dependencies:**
- Java / OpenJDK 21  
- Spring Boot 3.3.5  
- Maven 3.9  
- MariaDB 11.4  
- Docker Desktop  

> **Note:** Detailed setup and run instructions are provided in the project’s main [`README.md`](../README.md).

---

### 1.2 Root Files

| File | Description |
|------|-------------|
| `pom.xml` | Defines project dependencies, plugins, and build configurations. |
| `init.sql` | SQL script to create and initialize the database schema. |
| `Dockerfile` | Defines Docker image build stages and application setup. |
| `compose.yml` | Configures Docker environment and additional services. |
| `application.properties` | Global configuration properties for Spring Boot. |
| `.env` | Environment variables for local development and deployment. |

---

### 1.3 Root Folders

| Folder | Description |
|--------|-------------|
| `src` | Contains the application’s source code and resources. |
| `target` | Compiled classes and build artifacts. |
| `docs` | Auto-generated REST API documentation (HTML format). |
| `doc` | Manually created documentation (designs, requirements, diagrams). |

---

### 1.4 Source Structure (`src`)

#### 1.4.1 `main`
Contains the core functionality of the application.  

- **`java`** – Source code (controllers, services, entities, configurations, etc.)  
- **`resources`** – Configuration files, static resources, and templates  

#### 1.4.2 `test`
Contains test classes for unit and integration tests.  

- **`java`** – Test classes corresponding to the application’s source code  
- **`resources`** – Test-specific configuration or data  

> *Testing frameworks, execution instructions, and coverage details will be added once the Java modules are finalized.*

---

### 1.5 Main Java Modules (`main/java`)

| Module | Responsibility |
|--------|----------------|
| `app` | Global error and exception handling used throughout the application. |
| `auth` | Handles authorization processes such as login and registration. (Some functionalities moved to the [`spring-auth`](https://github.com/OrifInformatique/spring-auth) repository.) |
| `item` | Manages stock and inventory functionalities (not implemented yet). |
| `security` | Security-related classes: JWT filters, password encoding, and authentication management. |
| `users` | Manages user profiles, roles, and permissions. |
| `TemplateApplication.java` | Main Spring Boot entry point containing the `main()` method. Run the project from this class. |

---

### 1.6 Auth Module (`main/java/auth`)
```mermaid
sequenceDiagram
    participant Client
    participant AuthController
    participant AuthClient
    participant spring-auth

    Client->>AuthController: /auth/login with credentials
    AuthController->>AuthClient: authClient.login(credentialsDto)).getBody()
    AuthClient->>spring-auth: Send request to spring-auth/auth/login
    spring-auth->>Client: Response with UserDto
```
*Sequence Diagram showing an example of the authentication flow.*

| File | Description |
|------|-------------|
| `AuthClient.java` | Client service for authentication operations which send requests to `spring-auth`. |
| `AuthController.java` | Controller handling user authentication and registration which send requests to `AuthClient.java`. |
| `CredentialsDto.java` | Data Transfer Object (DTO) for login credentials. |
| `RegisterDto.java` | DTO for registration functionalities. |

> **Reference:** For full authentication implementation, see the [`spring-auth`](https://github.com/OrifInformatique/spring-auth) repository.

---

### 1.7 Users Module (`main/java/users`)

```mermaid
classDiagram
    class User {
        +long id
        +String firstName
        +String lastName
        +String login
        +Date createdAt
        +Date updatedAt
        +Role mainRole
        +Set<Role> appSpecificRoles
        +Collection<GrantedAuthority> getAuthorities()
        +String getUsername() 
        +boolean isAccountNonExpired()
        +boolean isAccountNonLocked()
        +boolean isCredentialsNonExpired()
        +boolean isEnabled()
        +Role getMainRole()
        +void setMainRole(Role role)
        +void addAppSpecificRoles(Role role)
        +List<String> getAppSpecificRolesString()
        +Set<Role> getAllRoles()
    }

    class Role {
        +long id
        +RoleEnum name
        +String description
        +Date createdAt
        +Date updatedAt
        +Set<User> users
        +Set<SimpleGrantedAuthority> getGrantedAuthorities()
    }

    class UserDto {
        <<DTO>>
        +Long id
        +String firstName
        +String lastName
        +String login
        +String token
        +String refreshToken
        +String mainRole = "USER"
        +List<String> appSpecificRoles = new ArrayList<>()
        +List<String> permissions = new ArrayList<>()
    }

    class RegisterDto {
        <<DTO>>
        +String firstName
        +String lastName
        +String login
    }

    class UserMapper {
        <<interface / singleton>>
        +UserDto toUserDto(User user)
        +User signUpToUser(RegisterDto registerDto)
        +List<String> authoritiesToPermissions(Collection<GrantedAuthority> authorities)
    }

    %% Relationships
    User --> "0..1" Role : mainRole
    User --> "0..*" Role : appSpecificRoles
    Role --> "0..*" User : users
    UserMapper ..> User : uses
    UserMapper ..> UserDto : creates
    UserMapper ..> RegisterDto : uses
    User ..|> UserDetails
```
*Class Diagram showing the `User`, `Role`, `UserDto`, and `SignUpDto` structure.*
```mermaid
sequenceDiagram
    participant Client
    participant SecurityLayer
    participant TestController
    participant UserService
    participant UserRepository
    participant UserMapper

    Client->>SecurityLayer: /tests/me
    SecurityLayer->>TestController: Authorized UserDto extracted from token
    TestController->>UserService: UserService.me()
    UserService->>UserRepository: userRepository.findByLogin(currentUser.getLogin())
    UserRepository->>TestController: Local User
    TestController->>Client: UserDto in response

    Client->>SecurityLayer: /tests/all
    SecurityLayer->>TestController: Authorized UserDto extracted from token
    TestController->>UserService: UserService.all()
    UserService->>UserRepository: UserRepository.findAll()
    UserRepository->>TestController: List of Users
    TestController->>Client: Response containing the list of users

    Client->>SecurityLayer: /tests/{userId}/promote-test
    SecurityLayer->>TestController: Authorized UserDto with `user:update` authority extracted from token
    TestController->>UserService: userService.promoteToTestAdmin(userId)
    UserService->>UserRepository: UserRepository.findById(userId)
    UserRepository->>UserService: Found User
    UserService->>UserRepository: UserRepository.save(user) with updated role
    UserService->>UserMapper: UserMapper.toUserDto(user)
    UserMapper->>TestController: UserDto
    TestController->>Client: Response "User promoted to test admin successfully"
```
*Sequence Diagram showing an example of the user management flow.*

| File | Description |
|------|-------------|
| `User.java` | Entity class representing a user in the system. |
| `UserDto.java` | DTO for communication between backend and frontend. |
| `UserMapper.java` | Handles conversion between `User` entities and `UserDto` objects. |
| `UserRepository.java` | Interface for database operations related to users. |
| `UserSeeder.java` | Seeds the database with test users for development. |
| `UserService.java` | Business logic for user functionalities (creation, update, role assignment, etc.). |

---
### 1.8 Tests Controlleurs (`main/java/test`)

| File | Description |
|------|-------------|
| `TestControlleur.java` | Controlleur to test fonctionalities. |
---

### 1.9 Security Module (`main/java/security`)

```mermaid
sequenceDiagram
    participant Client
    participant JwtAuthFilter
    participant UserAuthenticationProvider
    participant UserService
    participant UserController
    participant UserAuthenticationEntryPoint

    Client->>JwtAuthFilter: HTTP request with JWT token
    note right of JwtAuthFilter: Filter intercepts request
    JwtAuthFilter->>UserAuthenticationProvider: validateToken(token)
    note right of UserAuthenticationProvider: Check token & optionally fetch user
    UserAuthenticationProvider->>UserService: Lookup user in DB
    alt User exists
        UserService->>UserAuthenticationProvider: Return user info
    else User not found
        UserService->>UserAuthenticationProvider: Create new Azure user
    end
    UserAuthenticationProvider->>JwtAuthFilter: Return Authentication object
    alt Authentication fails
        JwtAuthFilter->>UserAuthenticationEntryPoint: 401 Unauthorized
        UserAuthenticationEntryPoint->>Client: Return 401 response
    else Authentication succeeds
        JwtAuthFilter->>UserController: Forward request
        note right of UserController: Controller handles action
        UserController->>UserService: Perform business logic
        UserService->>UserController: Return result
        UserController->>Client: Return HTTP response
    end
```
*Sequence Diagram showing JWT authentication and request handling flow.*

| File | Description |
|------|-------------|
| `JwtAuthFilter.java` | Authentication filter that processes tokens for incoming requests. |
| `PermissionEnum.java` | Enumeration defining available permissions. |
| `Role.java` | Role entity class representing a user role. |
| `RoleEnum.java` | Enumeration defining roles and their permissions. |
| `RoleRepository.java` | Interface for database operations related to roles. |
| `RoleSeeder.java` | Seeds the database with predefined roles. |
| `SecurityConfig.java` | Security configuration defining the filter chain and access rules. |
| `UserAuthenticationEntryPoint.java` | Handles unauthenticated access by returning a 401 response. |
| `UserAuthenticationProvider.java` | Authentication provider for validating user credentials. |
| `WebClientConfig.java` | Web client configuration for communication with other apps. |
| `WebConfig.java` | Web configuration for general web-related settings. |

---
### 1.10 Error and Exception Managment (`main/java/app`)

| File | Description |
|------|-------------|
| `errors/ErrorDto.java` | Record serving as Data Transfer object for Errors. |
| `exceptions/AppException.java` | Custome exception class for application specifique errors. |
| `exceptions/RestExceptionHandler.java` | Global exception handler for REST API endpoints. |

---
### 1.11 Main Test Modules (`main/test`)
| File | Description |
|------|-------------|
| `security` | Tests related to the security and authentification fonctionalities of the app. |
| `test` | Tests related to the test endpoints of the app. |
| `user` | Tests related to the user managment fonctionalities of the app. |
| `TemplateApplicationTests.java` | Load the context of the app. |
| `TestConfigurationDebug.java` | Test the existence of the environnment variables. |
---

## Related Documentation

- [Project README](../README.md)  
- [API Documentation (`docs/index.html`)](../docs/index.html)  
- [Frontend Repository](../frontend/README.md)  
---

**Author:** Ken D. Cacciabue
**Last Updated:** 04.11.2025