# Spring Packbase
<p>
    <img src="https://sectioninformatique.ch/wp-content/uploads/2021/09/Logo_Orif__70.jpg" width="271" height="70" alt="Logo ORIF"></a> 
    <img src="https://docs.spring.io/spring-boot/_/img/spring-logo.svg" width="271" height="70" alt="Logo Spring"></a><br><br>
    <img src="https://img.shields.io/badge/release-v0.1.1-blue" alt="Latest Release"></a>
</p>

Base package to start a REST API app with Spring-boot.


## Where to start ?

This project is built with a begginer oriented approach.

For the sake of good practice, this documentation is in **ENGLISH**.

### Copy the project using git

This documentation assumes that you know already how to use `git`.

First, make sure that you have `git` on your machine.

Open up your favorite terminal emulator and type in `git clone https://github.com/OrifInformatique/spring-packbase.git`.

### Make the project work on your machine

#### External dependencies

You will need 4 external dependencies:

- Java / openJDK 21
- Maven 3.9.9
- Spring-boot 3.3.3
- MariaDB 11.5.2

Make sure that your windows or WSL environnment variable contains the path to Java.

#### Application proprieties
The application is now almost ready!
The last thing that needs to be done is to link the database to your app.

To do that:
1. Copy the file `application.properties-dist` in the root of the project. Be careful to NOT modify this file itself.
2. Rename you copied file to `application.proprieties`.
3. Open the file in you IDE of choice and uncomment the `### Database connection ###` section.
4. Insert your database url and credentials.

This step is important because you do not want your **top secret** password to be public on github.

## What's next ?

Some additional documentation will be soon available in the `docs/` folder.

In the mean time, feel free to explore the code and create your own branch to
learn about Spring-boot !

The item module is here as an exemple of how to create your own.

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

## License

[MIT](https://github.com/OrifInformatique/spring-packbase/blob/master/LICENSE)

---
