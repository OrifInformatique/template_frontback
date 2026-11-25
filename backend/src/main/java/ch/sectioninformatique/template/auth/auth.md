# AUTH PACKAGE

## Table of Contents
- [AuthClient.java](#authclientjava)
- [AuthCodeDto.java](#authcodedtojava)
- [AuthController.java](#authcontrollerjava)
    - [getToken()](#gettoken)
    - [Security](#security)
- [AuthExceptions.java](#authexceptionsjava)
    - [InvalidCredentialsException](#invalidcredentialsexception)
    - [RegistrationFailedException](#registrationfailedexception)
    - [UserNotFoundException](#usernotfoundexception)
    - [UserAlreadyExistsException](#useralreadyexistsexception)
    - [PasswordUpdateFailedException](#passwordupdatefailedexception)
    - [OAuth2AuthenticationException](#oauth2authenticationexception)
    - [LoginAlreadyExistsException](#loginalreadyexistsexception)
    - [AuthCodeNotFoundException](#authcodenotfoundexception)
- [CredentialsDto.java](#credentialsdtojava)
- [PasswordUpdateDto.java](#passwordupdatedtojava)
- [RefreshUpdateDto.java](#refreshupdatedtojava)
- [OAuth2Controller.java](#oauth2controllerjava)
- [RegisterDto.java](#registerdtojava)
- [TokenResponseDto.java](#tokenresponsedtojava)

## *AuthClient.java*

`AuthClient` is a service class used to communicate with the external `spring-auth` authentication service.

It uses Spring's `WebClient` to send asynchronous HTTP requests for authentication, user management, and role management.

It contains :

* `login()`,
* `register()`,
* `refreshLogin()`,
* `updatePassword()`,
* `logout()`,
* `deleteGlobalUser()`,
* `promoteToManager()`,
* `revokeManager()`,
* `promoteToAdmin()`,
* `revokeAdmin()`,
* `downgradeAdmin()`,
* `getTokenWithAuthCode()`

### Authentication

`login()` sends the user's credentials to the authentication service and returns the authenticated user's information.

`register()` creates a new user through the authentication service.

`refreshLogin()` uses the `refresh_token` cookie to request a new access token.

`getTokenWithAuthCode()` exchanges a temporary authentication code for the user's authentication information and tokens.

`updatePassword()` sends the user's password update request to the authentication service.

`logout()` logs the user out (spring-auth answers `204`) and passes on the expiring `refresh_token` cookie.

### User management

`deleteGlobalUser()` deletes a user by id in the authentication service (`DELETE /users/{id}?permanent=...`): a soft deletion when `permanent` is `false`, a permanent one when it is `true`. The authentication service answers `204 No Content`, so the method returns a bodiless `ResponseEntity<Void>`; it sends the user's authentication token and converts authentication-service errors into application exceptions.

### Role management

The class provides methods to modify user roles :

* `promoteToManager()` → promotes a user to manager,
* `revokeManager()` → removes the manager role,
* `promoteToAdmin()` → promotes a user to administrator,
* `revokeAdmin()` → removes the administrator role,
* `downgradeAdmin()` → changes an administrator back to manager.

Errors returned by the authentication service are converted into `AppMessageKeyException` so they can be handled and localized by `GlobalExceptionHandler`.

### Language handling

The `uriWithOptionalLang()` method adds the current `lang` query parameter to requests sent to the authentication service.

This allows the external authentication service to return messages in the same language as the current request.

### Configuration

The authentication service URL is provided through the `SPRING_AUTH_URL` environment variable.

The Azure login and OAuth2 callback URLs are provided through :

* `AZURE_LOGIN_URL`,
* `AFTER_OAUTH2_LOGIN_URL`.

`AuthClient` therefore acts as the connection between `template_frontback` and the external authentication service, handling authentication requests, user management, role changes, cookies, and localized errors.


---

## *AuthCodeDto.java*

`AuthCodeDto` is a Data Transfer Object used to transfer a temporary authentication code.

It contains :

* `id`,
* `code`

The `id` identifies the user or authentication request associated with the code.

The `code` contains the temporary authentication code used during the authentication process.

`@Builder` from Lombok provides a builder for creating `AuthCodeDto` objects.

Because `AuthCodeDto` is a Java `record`, it is immutable and automatically provides its constructor, accessors, `equals()`, `hashCode()`, and `toString()` methods.


---

## *AuthController.java*

`AuthController` is a REST Controller for authentication, relayed to the external
authentication service (spring-auth) through `AuthClient`. It uses the same paths as
spring-auth (`/auth`).

| Endpoint | Response |
|---|---|
| `POST /auth/login` | `200`, the user with its access token, completed with its local roles and permissions; the refresh token in a cookie |
| `POST /auth/refresh` | `200`, a new access token from the `refresh_token` cookie (`401` without the cookie) |
| `POST /auth/logout` | `204`; spring-auth revokes the refresh token, its expiring `refresh_token` cookie is passed on, and the HTTP session is invalidated |

User creation (`POST /users`) and password change (`PUT /users/me/password`) are user
operations, in `UserController`. The Azure login is in `OAuth2Controller`.

## *OAuth2Controller.java*

`OAuth2Controller` handles the Azure OAuth2 login, delegated to spring-auth, under the same
paths as spring-auth's OAuth2 endpoints (`/oauth2`).

| Endpoint | Response |
|---|---|
| `GET /oauth2/login/azure` | `302` to spring-auth's Azure login, which will come back to `/oauth2/callback` (`AFTER_OAUTH2_LOGIN_URL`) |
| `GET /oauth2/callback?authCode=&userId=` | Browser redirect from spring-auth after the Azure login: exchanges the temporary auth code for the user and its tokens (`AuthClient.getTokenWithAuthCode()`), gets or creates the local user, keeps the user and the refresh token in the HTTP session, then `302` to the frontend (`AFTER_OAUTH2_LOGIN_FRONTEND_CALLBACK`) |
| `POST /oauth2/token` | `200`, the logged user with its access token, and the refresh token in a cookie (lifetime `SECURITY_JWT_TOKEN_REFRESH_TOKEN_LIFETIME`). **Single use**: the session data is removed, a second call answers `401` |

The HTTP session is only used to carry the result of the Azure login from the callback to
the frontend; it never authenticates a request (see `SecurityConfig`).

## *RegisterDto.java*

`RegisterDto` is the body of `POST /users` (user creation).

It contains :

* `firstName` and `lastName` : required, letters with inner spaces, apostrophes and hyphens (as in spring-auth),
* `login` : required email,
* `password` : 8 to 72 characters, as a character array,
* `mainRole` : optional main role (USER by default in spring-auth),
* `appSpecificRoles` : optional local roles

It is validated as spring-auth validates it, to answer with the invalid fields before calling
spring-auth. It is also used internally, without validation, to register locally a user
authenticated by spring-auth (e.g. an Azure account without last name).

Because `RegisterDto` is a Java `record`, it is immutable and automatically provides its constructor, accessors, `equals()`, `hashCode()`, and `toString()` methods.


---

## *TokenResponseDto.java*

`TokenResponseDto` is a Data Transfer Object used to return an access token after successful authentication or token refresh.

It contains :

* `accessToken`

The `accessToken` contains the token used by the client to authenticate API requests.

`@Data` from Lombok automatically generates getters, setters, `toString()`, `equals()`, and `hashCode()`.

`@AllArgsConstructor` generates a constructor containing all fields, while `@NoArgsConstructor` generates an empty constructor.


---