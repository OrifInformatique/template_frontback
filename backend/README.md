# Spring template
<p>
    <div style="text-align: center;">
        <img src="https://sectioninformatique.ch/wp-content/uploads/2021/09/Logo_Orif__70.jpg" width=40% height=40% alt="Orif logo"></a>
        <img src="src/main/resources/static/images/spring_boot.svg" width="271" height="70" alt="Spring Boot logo">
    </div>
</p>

Template to start developing a new REST API application with Spring Boot.

## Getting Started
These instructions will get you a copy of the project up and running on your local machine for development and testing purposes.

### Prerequisites
The Spring Boot version currently used in this project is 3.3.5.

The project's environment must contain these tools. Make sure that your Windows or WSL environnment variables contain the path to Java.

- [Java / openJDK 21](https://adoptium.net/fr/temurin/releases/)
- [Maven 3.9](https://maven.apache.org/docs/history.html)
- [MariaDB 10.4](https://mariadb.org/mariadb/all-releases/). A MySQL equivalent can also be installed with [Laragon](https://laragon.org/download/)

### Application properties
Link your database to your app :

1. DON'T modify the `application.properties-dist` file but make a copy of it in the root of the project.
2. Rename your copied file to `application.properties`.
3. Open the file in you IDE of choice and uncomment the `### Database connection ###` section.
4. Insert your database url and credentials.

The `application.properties` file is git ignored. This is important because you do not want your **top secret** password to be public on github.

## What's next ?

Some additional documentation will be soon available in the `docs/` folder.

In the mean time, feel free to explore the code and create your own experiment branch to
learn about Spring Boot !

The item module is here as an exemple of how to create your own modules.

Good luck !

## Command cheat-sheet

This app is mainly accessed through the terminal, therefore, a lot of commands
have to be memorized.

Here is a list of the most common commands you'll likely use !

**Run the app**

`mvn spring-boot:run`

**Downloads all the dependencies without building or compiling the app**

`mvn dependency:resolve`

**Same as `mvn dependency:resolve` but displays the dependency tree**

`mvn dependency:tree`

**Removes the previously built artefacts and .JAR file**

`mvn clean`

**Build the project into a .JAR file**

`mvn package`

**Check if the project's structure is valid**

`mvn validate`

## Microsoft Entra / Azure AD oAuth2 grant flow

### Simplified sequence diagram
<p>
    <img src="src/main/resources/static/images/oauth2_sequence_diagram.png" alt="simplified oAuth 2 sequence diagram">
</p>


## Sources

[Microsoft oAuth2 grant flow](https://learn.microsoft.com/en-us/entra/identity-platform/v2-oauth2-auth-code-flow)

---
