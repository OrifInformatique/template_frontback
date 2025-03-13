# Configuration Azure OAuth2 avec Spring Boot

## 1. Configuration dans `application.properties`

### Configuration du Provider Azure
```properties
# Identifiant du tenant Azure AD
custom.azure.account.tenant-id=<ajouter ici votre tenant-id>

# Configuration du provider Azure
spring.security.oauth2.client.provider.azure.issuer-uri=https://login.microsoftonline.com/${custom.azure.account.tenant-id}/v2.0
spring.security.oauth2.client.provider.azure.authorization-uri=https://login.microsoftonline.com/${custom.azure.account.tenant-id}/oauth2/v2.0/authorize
spring.security.oauth2.client.provider.azure.token-uri=https://login.microsoftonline.com/${custom.azure.account.tenant-id}/oauth2/v2.0/token
spring.security.oauth2.client.provider.azure.user-info-uri=https://graph.microsoft.com/oidc/userinfo
spring.security.oauth2.client.provider.azure.jwk-set-uri=https://login.microsoftonline.com/${custom.azure.account.tenant-id}/discovery/v2.0/keys
spring.security.oauth2.client.provider.azure.user-name-attribute=email
```

### Configuration du Client Azure
```properties
spring.security.oauth2.client.registration.azure.client-id=<ajouter ici votre tenant-id>
spring.security.oauth2.client.registration.azure.client-secret=<ajouter ici votre client-secret>
spring.security.oauth2.client.registration.azure.client-authentication-method=client_secret_post
spring.security.oauth2.client.registration.azure.authorization-grant-type=authorization_code
spring.security.oauth2.client.registration.azure.redirect-uri={baseUrl}/login/oauth2/code/{registrationId}
spring.security.oauth2.client.registration.azure.scope=openid,profile,email,User.Read
spring.security.oauth2.client.registration.azure.client-name=Azure
```

### Configuration du Client Azure plus sécurisé (nécéssite la création d'un fichier .env)
```properties
spring.security.oauth2.client.registration.azure.client-id=${AZURE_CLIENT_ID}
spring.security.oauth2.client.registration.azure.client-secret=${AZURE_CLIENT_SECRET}
```
le reste étant identique.

## 2. Configuration de Sécurité (`SecurityConfig.java`)

### Modifications Principales
1. **Gestion des Sessions** :
   ```java
   .sessionManagement(customizer -> 
       customizer.sessionCreationPolicy(SessionCreationPolicy.ALWAYS)
   )
   ```
   - Changé de `STATELESS` à `ALWAYS` pour maintenir la session pendant la redirection OAuth2

2. **Configuration OAuth2** :
   ```java
   .oauth2Login(oauth2 -> {
       oauth2
           .defaultSuccessUrl("/oauth2/success", true)
           .failureUrl("/oauth2/error")
           .userInfoEndpoint(userInfo -> 
               userInfo.userService(oauth2UserService())
           )
           .successHandler((request, response, authentication) -> {
               log.debug("OAuth2 authentication successful: {}", authentication);
               response.sendRedirect("/oauth2/success");
           })
           .permitAll();
   })
   ```
   - Configuration du endpoint de succès
   - Ajout de logs pour le débogage
   - Configuration du service utilisateur OAuth2

3. **Règles d'Autorisation** :
   ```java
   .authorizeHttpRequests(requests -> {
       requests
           .requestMatchers("/oauth2/authorization/**").permitAll()
           .requestMatchers("/oauth2/success").authenticated()
           .requestMatchers("/oauth2/error").permitAll()
           .requestMatchers("/login/oauth2/code/**").permitAll()
   })
   ```
   - Protection des endpoints OAuth2
   - `/oauth2/success` nécessite une authentification

## 3. Gestion des Tokens JWT (`UserAuthenticationProvider.java`)

### Création de Token
```java
public String createToken(UserDto user) {
    return JWT.create()
            .withSubject(user.getLogin())
            .withIssuedAt(now)
            .withExpiresAt(validity)  // 1 heure de validité
            .withClaim("firstName", user.getFirstName())
            .withClaim("lastName", user.getLastName())
            .withClaim("role", "ROLE_USER")
            .withClaim("permissions", List.of(
                // OAuth2 scopes
                "SCOPE_openid", 
                "SCOPE_profile", 
                "SCOPE_email", 
                "SCOPE_User.Read",
                // Item permissions
                "item:read",
                "item:write",
                "item:update",
                "item:delete"
            ))
            .sign(algorithm);
}
```
- Ajout des rôles et permissions par défaut pour les utilisateurs Azure
- Inclusion des scopes OAuth2 comme permissions
- Ajout des permissions pour les opérations CRUD sur les items

### Validation de Token
```java
public Authentication validateToken(String token) {
    // ... vérification du token ...
    List<SimpleGrantedAuthority> authorities = buildAuthorities(user.getRole(), user.getPermissions());
    return new UsernamePasswordAuthenticationToken(user, null, authorities);
}
```
- Ajout des autorités lors de la validation du token
- Utilisation de `buildAuthorities` pour créer les bonnes autorités

## 4. Flux d'Authentification

1. L'utilisateur accède à `/oauth2/authorization/azure`
2. Redirection vers la page de connexion Azure
3. Après connexion réussie, Azure redirige vers `/login/oauth2/code/azure`
4. Spring Security traite le code d'autorisation
5. Redirection vers `/oauth2/success`
6. Génération d'un JWT avec les rôles et permissions
7. Redirection finale vers le frontend avec le token JWT

## 5. Dépendances Maven Requises

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-client</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-oauth2-jose</artifactId>
</dependency>
```

## 6. Configuration de Logging

```properties
logging.level.org.springframework.security=DEBUG
logging.level.org.springframework.security.oauth2.client=DEBUG
logging.level.ch.sectioninformatique.template=DEBUG
```
- Activation des logs détaillés pour le débogage OAuth2

## 7. Gestion des Tokens JWT

### Création du Token
Le token JWT est créé avec les informations suivantes :
```java
JWT.create()
    .withSubject(user.getLogin())
    .withIssuedAt(now)
    .withExpiresAt(validity)  // 1 heure de validité
    .withClaim("firstName", user.getFirstName())
    .withClaim("lastName", user.getLastName())
    .withClaim("role", "ROLE_USER")
    .withClaim("permissions", List.of(
        // OAuth2 scopes
        "SCOPE_openid", 
        "SCOPE_profile", 
        "SCOPE_email", 
        "SCOPE_User.Read",
        // Item permissions
        "item:read",
        "item:write",
        "item:update",
        "item:delete"
    ))
```

### Validation du Token
Il existe deux niveaux de validation du token :

1. **Validation Simple** (`validateToken`) :
   - Vérifie la signature du token
   - Extrait les informations du token
   - Crée un nouvel objet `UserDto` à partir des claims du token
   - Utilisé pour les requêtes GET

2. **Validation Forte** (`validateTokenStrongly`) :
   - Vérifie la signature du token
   - Récupère l'utilisateur depuis la base de données
   - Ajoute les permissions par défaut
   - Conserve le token dans l'objet `UserDto`
   - Utilisé pour toutes les autres requêtes (POST, PUT, DELETE)

### Stockage du Token
Le token est stocké à plusieurs niveaux :

1. **Côté Client** :
   ```javascript
   localStorage.setItem('token', jwt);
   localStorage.setItem('loginType', 'azure');
   ```

2. **Côté Serveur** :
   ```java
   // Dans UserDto
   user.setToken(token);  // Stocké dans l'objet utilisateur
   ```

### Cycle de Vie du Token
1. **Création** : Lors de l'authentification réussie (login ou OAuth2)
2. **Transmission** : Dans le header HTTP `Authorization: Bearer <token>`
3. **Validation** : À chaque requête via le `JwtAuthFilter`
4. **Expiration** : Après 1 heure (configurable)

### Sécurité
- Le secret du token est encodé en Base64
- Le token est vérifié à chaque requête
- Les permissions sont validées à chaque action
- Le token est invalidé après expiration

## 8. Permissions des Items

### Permissions Automatiques
Les utilisateurs authentifiés via OAuth2 reçoivent automatiquement les permissions suivantes :

- `item:read` : Permet de lire les items
- `item:write` : Permet de créer des items
- `item:update` : Permet de modifier les items
- `item:delete` : Permet de supprimer les items

Ces permissions sont ajoutées automatiquement dans deux endroits :

1. **Création du Token JWT** :
```java
.withClaim("permissions", List.of(
    // OAuth2 scopes
    "SCOPE_openid", 
    "SCOPE_profile", 
    "SCOPE_email", 
    "SCOPE_User.Read",
    // Item permissions
    "item:read",
    "item:write",
    "item:update",
    "item:delete"
))
```

2. **Validation du Token** :
```java
public Authentication validateTokenStrongly(String token) {
    // ... vérification du token ...
    UserDto user = userService.findByLogin(decoded.getSubject());
    
    // Ajout des permissions par défaut
    List<String> permissions = new ArrayList<>(List.of(
        // OAuth2 scopes et permissions items
        "SCOPE_openid", "SCOPE_profile", "SCOPE_email", "SCOPE_User.Read",
        "item:read", "item:write", "item:update", "item:delete"
    ));

    // Fusion avec les permissions existantes
    if (user.getPermissions() != null) {
        permissions.addAll(user.getPermissions());
    }

    user.setPermissions(permissions);
    // ... création des autorités ...
}
```

### Configuration du DTO
Le `UserDto` est configuré pour gérer les permissions avec des valeurs par défaut :

```java
@Data
@Builder(toBuilder = true)
public class UserDto {
    // ... autres champs ...
    
    @Builder.Default
    private String role = "ROLE_USER";
    
    @Builder.Default
    private List<String> permissions = new ArrayList<>();
}
```

### Endpoints Protégés
Ces permissions sont nécessaires pour accéder aux endpoints suivants :

```java
@PreAuthorize("hasAuthority('item:read')")
@GetMapping("/items")
@GetMapping("/items/{id}")

@PreAuthorize("hasAuthority('item:write')")
@PostMapping("/items")

@PreAuthorize("hasAuthority('item:update')")
@PutMapping("/items/{id}")

@PreAuthorize("hasAuthority('item:delete')")
@DeleteMapping("/items/{id}")
```

### Validation des Permissions
Les permissions sont validées à deux niveaux :

1. **Niveau Token** : Lors de la validation du token JWT
2. **Niveau Méthode** : Via les annotations `@PreAuthorize`

En cas d'accès non autorisé, une erreur 403 (Forbidden) est retournée. 