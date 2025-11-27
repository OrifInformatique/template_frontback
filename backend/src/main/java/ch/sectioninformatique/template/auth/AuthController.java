package ch.sectioninformatique.template.auth;

import java.net.URI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ch.sectioninformatique.template.app.exceptions.AppException;
import ch.sectioninformatique.template.user.UserDto;
import ch.sectioninformatique.template.user.UserService;
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
public class AuthController {

    /** Service for handling user-related operations */
    private final UserService userService;

    /** Client to send authentication requests to the spring-auth application */
    @Autowired
    private final AuthClient authClient;

    /**
     * Handles POST requests to "/login"
     * Accepts login credentials (login and password) as a request body, validated
     * for correctness
     * Calls the authentication client to perform login with provided credentials
     * Returns a reactive Mono<ResponseEntity<UserDto>>containing the login response (e.g., token or
     * status message)
     * 
     * @param credentialsDto
     * @return Mono<ResponseEntity<UserDto>> with login response
     */
    @PostMapping("/login")
    public Mono<ResponseEntity<UserDto>> login(@RequestBody @Valid CredentialsDto credentialsDto) {
        return ResponseEntity.ok(authClient.login(credentialsDto)).getBody();
    }

    /**
     * Handles POST requests to "/register"
     * Accepts user registration data as a request body, validated for correctness
     * Calls the authentication client to perform registration with provided user data
     * On successful registration, also registers the user locally in the system
     * Returns a reactive Mono<ResponseEntity<UserDto>> containing the registration
     * response or error message
     * 
     * @param user
     * @return Mono<ResponseEntity<UserDto>> with registration response
     */
    @PostMapping("/register")
    public Mono<ResponseEntity<UserDto>> register(@RequestBody @Valid RegisterDto user) {
        return authClient.register(user)
                .flatMap(response -> {
                    // On successful registration, also register user locally
                    userService.register(user);

                    // Return HTTP 200 OK with the response body
                    return Mono.just(response);
                })
                .onErrorResume(ex -> Mono.error(new AppException(ex.getMessage(), HttpStatus.BAD_REQUEST)));
    }

    /**
     * Handles GET requests to "/refresh"
     * Accepts Bearer token as header
     * 
     * On successful refresh send the UserDto with the new token 
     * 
     * @param token
     * @return Mono<ResponseEntity<UserDto>> with new token
     */
    @GetMapping("/refresh")
    public Mono<ResponseEntity<UserDto>> refreshLogin(@RequestHeader("Authorization") String token) {
        return authClient.refreshLogin(token)
                .flatMap(response -> {
                    UserDto userDto = response.getBody();

                    if (userDto != null) {
                        return Mono.<ResponseEntity<UserDto>>just(ResponseEntity.ok(userDto));
                    } else {
                        return Mono.<ResponseEntity<UserDto>>just(
                                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
                    }
                })
                .onErrorResume(ex -> Mono.error(new AppException(ex.getMessage(), HttpStatus.BAD_REQUEST)));
    }

    /**
     * Handles PUT requests to "/update-password"
     * Accepts new password data as a request body, validated for correctness
     * Calls the authentication client to set the new password for the user
     * Returns a reactive Mono<ResponseEntity<MessageResponseDto>> containing the
     * response message
     * 
     * @param token The authorization token from the request header
     * @param updatePasswordDto The UpdatePasswordDto containing the old and new passwords
     * @return Mono<ResponseEntity<MessageResponseDto>> with set password response
     */
    @PutMapping("/update-password")
    public Mono<ResponseEntity<MessageResponseDto>> updatePassword(@RequestHeader("Authorization") String token,
            @RequestBody @Valid PasswordUpdateDto updatePasswordDto) {
        return authClient.updatePassword(token, updatePasswordDto)
                .map(responseEntity -> {
                    MessageResponseDto messageResponse = responseEntity.getBody();
                    if (messageResponse != null) {
                        return ResponseEntity.ok(messageResponse);
                    } else {
                        // Handle null body just in case
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
                    }
                });
    }

    /**
     * Handles GET requests to "/oauth2/login"
     * Redirects the client to the OAuth2 authorization endpoint for Azure
     * This initiates the OAuth2 login flow
     * After successful login, the user will be redirected back to the application
     * 
     * @return ResponseEntity with redirection to OAuth2 login URL
     */
    @GetMapping("/oauth2/login")
    public ResponseEntity<Object> testCallOAuth2() {

        // Redirect frontend to spring-auth OAuth2 login endpoint
        URI uri = URI.create("http://localhost:8081/oauth2/authorization/azure");
        return ResponseEntity.status(HttpStatus.FOUND).location(uri).build();
    }
}
