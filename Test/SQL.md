# Listes des commandes SQL pour visualiser dans le conteneur le résiltat des API_REST.

## Requête basique : 

### Voir les DBs

```SQL
SHOW databases;
```

### Voir le contenu de la DB (Les tables)

```SQL
USE db;
```
### Voir le contenu d'une table

```SQL
SELECT * FROM items;
SELECT * FROM roles;
SELECT * FROM users;
SELECT * FROM users_roles;
```

## Afficher les items et leur auteur (du premier au dernier)

```SQL
SELECT items.id, items.description, items.name, users.last_name AS nom, users.first_name AS prenom
FROM items
INNER JOIN users ON items.author_id = users.id
ORDER BY items.id ASC;
```

## 