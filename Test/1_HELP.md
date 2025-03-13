### Backend (Spring Boot)
- Lancer le serveur Spring Boot
    ```bash
    ./mvnw spring-boot:run
    ```
- Vérifier que l'API est accessible sur le port 8080
    ```bash

    ```
### Frontend (React)
- Lancer l'application React avec npm run serve
    ```bash
    npm install
    npm run serve
    ```

- Vérifier que l'interface est accessible sur le port 4000
    ```bash
    curl http://localhost:4000
    ```

### Base de données (Docker)
- Lancer Docker Compose avec le fichier compose.yml
    ```bash
    docker-compose up -d
    ```

- Vérifier que le conteneur MariaDB démarre correctement
    ```bash
    docker ps | grep mariadb
    ```

- Vérifier que les migrations sont appliquées correctement
    ```bash
    docker exec -it fcc-SB-mariadb maiadb -u root -p db -e"SHOW TABLES;"
    ``` 

## A déplacer !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    - Vérifier que la base de données est accessible
    ```bash
        docker exec -it fcc-SB-mariadb mariadb -u root -p
    ```
    imsérer le mot de passe
    - Connectez vous à la bonne db
    ```sql
        use db;
    ```
    - Controllez le contenu d'une table
    ```sql
        SELECT * FROM users;
    ```