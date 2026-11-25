package ch.sectioninformatique.template.auth;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ch.sectioninformatique.template.security.BearerToken;
import ch.sectioninformatique.template.user.UserDto;
import ch.sectioninformatique.template.user.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Authentication endpoints, relayed to spring-auth (same paths as spring-auth):
 * - POST /auth/login    log in with login and password, 200 + user (+ refresh token cookie)
 * - POST /auth/refresh  new access token from the refresh token cookie
 * - POST /auth/logout   revoke the refresh token, 204 (+ expiring cookie)
 *
 * User creation and password change are user operations (POST /users,
 * PUT /users/me/password, in UserController); the Azure login is in
 * OAuth2Controller (/oauth2).
 */
@RequestMapping("/auth")
@RequiredArgsConstructor
@RestController
@SuppressWarnings("null")
public class AuthController {

    private final UserService userService;

    private final AuthClient authClient;

    /**
     * Logs a user in through spring-auth and completes the user with its local
     * roles and permissions.
     *
     * @param credentialsDto login and password
     * @return 200 with the user and its access token, the refresh token in a cookie
     */
    @PostMapping("/login")
    public ResponseEntity<UserDto> login(@RequestBody @Valid CredentialsDto credentialsDto) {
        ResponseEntity<UserDto> response = authClient.login(credentialsDto).block();

        // spring-auth only knows the global roles and permissions: add the local ones
        userService.addLocalRolesAndPermissions(response.getBody(), response.getBody().getId());
        return response;
    }

    /**
     * Gets a new access token from the refresh token cookie.
     *
     * @param refreshToken the refresh token cookie
     * @return 200 with the new access token, the new refresh token in a cookie
     */
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponseDto> refreshLogin(@CookieValue("refresh_token") String refreshToken) {
        return authClient.refreshLogin(refreshToken)
                .map(response -> ResponseEntity.status(response.getStatusCode())
                        .headers(response.getHeaders())
                        .body(response.getBody()))
                .block();
    }

    /**
     * Logs the authenticated user out: spring-auth revokes its refresh token,
     * and the HTTP session (Azure login) is invalidated.
     *
     * @param request current request, for the session
     * @return 204, with the cookie expiring the refresh token
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        // Read before the session and security context are cleared
        String token = BearerToken.current();

        var session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        // Errors of spring-auth are mapped by AuthClient, with its status
        return authClient.logout(token).block();
    }
}
