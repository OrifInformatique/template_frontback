package ch.sectioninformatique.template.auth;

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ch.sectioninformatique.template.user.UserDto;
import ch.sectioninformatique.template.user.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

/**
 * Controller handling user authentication and registration.
 * This controller provides endpoints for user login and registration,
 * managing the authentication process and user creation.
 */
@RequestMapping("/auth")
@RequiredArgsConstructor
@RestController
@SuppressWarnings("null")
public class AuthController {

    /** Service for handling user-related operations */
    private final UserService userService;

    /** Client to send authentication requests to the spring-auth application */
    @Autowired
    private AuthClient authClient;

    // Logger for debugging and monitoring the authentication flow.
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    /**
     * Handles POST requests to "/login"
     * Accepts login credentials (login and password) as a request body, validated
     * for correctness
     * Calls the authentication client to perform login with provided credentials
     * Returns a reactive Mono<ResponseEntity<UserDto>>containing the login response
     * (e.g., token or
     * status message)
     * 
     * @param credentialsDto
     * @return Mono<ResponseEntity<UserDto>> with login response
     */
    @PostMapping("/login")
    public ResponseEntity<UserDto> login(@RequestBody @Valid CredentialsDto credentialsDto) {
        return authClient.login(credentialsDto)
                .onErrorResume(ex -> Mono.error(ex))
                .block();
    }

    /**
     * Handles POST requests to "/register"
     * Accepts user registration data as a request body, validated for correctness
     * Calls the authentication client to perform registration with provided user
     * data
     * On successful registration, also registers the user locally in the system
     * Returns a reactive Mono<ResponseEntity<UserDto>> containing the registration
     * response or error message
     * 
     * @param user
     * @return Mono<ResponseEntity<UserDto>> with registration response
     */
    @PostMapping("/register")
    @PreAuthorize("hasAuthority('user:write')")
    public ResponseEntity<UserDto> register(@RequestHeader("Authorization") String token,
                                            @RequestBody @Valid RegisterDto user) {
                                                
        return authClient.register(token, user)
                .flatMap(response -> {
                    // On successful registration, also register user locally
                    userService.register(user);

                    // Return HTTP 200 OK with the response body
                    return Mono.just(response);
                })
                .block();
    }

    /**
     * Handles POST requests to "/refresh"
     * Accepts refresh token from cookie
     * 
     * On successful refresh send the new access token
     * 
     * @param refreshToken The refresh token from the cookie
     * @return ResponseEntity<TokenResponseDto> with new token
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
     * Handles PUT requests to "/update-password"
     * Accepts new password data as a request body, validated for correctness
     * Calls the authentication client to set the new password for the user
     * Returns a ResponseEntity<MessageResponseDto> containing the response message
     * 
     * @param token             The authorization token from the request header
     * @param updatePasswordDto The PasswordUpdateDto containing the old and new
     *                          passwords
     * @return ResponseEntity<MessageResponseDto> with set password response
     */
    @PutMapping("/update-password")
    public ResponseEntity<MessageResponseDto> updatePassword(@RequestHeader("Authorization") String token,
            @RequestBody @Valid PasswordUpdateDto updatePasswordDto) {
        return authClient.updatePassword(token, updatePasswordDto)
                .map(responseEntity -> responseEntity.getBody())
                .map(messageResponse -> ResponseEntity.ok(messageResponse))
                .block();
    }

    /**
     * Handles GET requests to "auth/redirect-after-login"
     * This endpoint is called after a successful OAuth2 login
     * 
     * @param token the JWT token generated by the authentication provider
     * @return
     */
    @GetMapping("/redirect-after-login")
    public void afterLogin(@CookieValue(value = "Set-Cookie", required = false) String token) {
            // This endpoint can be used to perform any post-login processing if needed
            // For now, it simply serves as a redirect target after successful OAuth2 login

            if (token != null) {
                // Process the token, e.g., log it or store it
                log.debug("Token: " + token);
            }
    }

    /**
     * Handles GET requests to "/auth/login/azure"
     * 
     * This endpoint is used to initiate the OAuth2 login process by redirecting
     * to the spring-auth Azure login endpoint.
     * The login process is handled by the spring-auth application, which will manage the
     * authentication flow with Azure and redirect to redirectUrl after successful login.
     * 
     * @param redirectUrl  The URL to redirect to after successful authentication (optional).
     *                     If not provided, uses the Referer header. If neither is available,
     *                     no redirect URL is used.
     * @param request      The HTTP request object
     *
     * @return ResponseEntity<Void> with redirect to the spring-auth Azure login endpoint or an error response if the redirection fails.
     */
    @GetMapping("/login/azure")
    public ResponseEntity<Void> OAuth2AzureLogin(@RequestParam(required = false) String redirectUrl,
                                                 HttpServletRequest request) {

        // If redirectUrl is not provided, try to get it from the Referer header
        if (redirectUrl == null || redirectUrl.isBlank()) {
            String referer = request.getHeader("Referer");
            if (referer != null && !referer.isBlank()) {
                redirectUrl = referer;
                log.debug("Retrieved redirect URL from Referer header: {}", referer);
            } else {
                log.debug("No redirect URL provided");
            }
        } else {
            log.debug("Received redirect URL from request parameter: {}", redirectUrl);
        }

        URI loginUri = authClient.buildAzureLoginUri(redirectUrl);
        return ResponseEntity.status(HttpStatus.FOUND).location(loginUri).build();
    }

    /**
     * Handles POST requests to "/logout"
     * Sends the logout request to the authentication provider
     * Passes the authorization token from the request header
     * Returns the response from the authentication provider
     * 
     * @param token The authorization token from the request header
     * @return ResponseEntity with logout response from authentication provider
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String token, HttpServletRequest request) {

        var session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        SecurityContextHolder.clearContext();

        return authClient.logout(token)
                .map(response -> ResponseEntity.status(response.getStatusCode())
                        .headers(response.getHeaders())
                        .body(response.getBody()))
                .onErrorResume(ex -> Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).build()))
                .block();
    }
}