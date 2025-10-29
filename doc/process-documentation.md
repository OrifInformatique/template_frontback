# Application Documentation

## Overview
This document describes the structure and processes of the application, including backend setup, configuration files, and folder organization.

## 1. Backend

### 1.1 General Information

#### 1.1.1 Tools Used
1. Java / OpenJDK 21
2. Spring Boot 3.3.5
3. Maven 3.9
4. MariaDB 11.4
5. Docker Desktop

### 1.2 Root Files

- **pom.xml**: Defines project dependencies, plugins, and build configurations.
- **init.sql**: SQL script to create and initialize the database schema.
- **Dockerfile**: Defines Docker image build stages and application setup.
- **compose.yml**: Configures Docker environment and additional services.
- **application.properties**: Spring Boot global configuration properties.
- **.env**: Environment variables for local development or deployment.

### 1.3 Root Folders

- **src**: Contains the application’s source code and functionalities.
- **target**: Contains compiled classes and build artifacts.
- **docs**: Auto-generated REST documentation (HTML format) for GitHub.
- **doc**: Various manually created documentation related to the application.
