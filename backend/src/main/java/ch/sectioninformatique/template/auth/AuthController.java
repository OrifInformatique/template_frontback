package ch.sectioninformatique.template.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
     * Returns a reactive Mono<String> containing the login response (e.g., token or
     * status message)
     * 
     * @param credentialsDto
     * @return Mono<String> with login response
     */
    @PostMapping("/login")
    public Mono<String> login(@RequestBody @Valid CredentialsDto credentialsDto) {
        /** TODO : Return datas in JSON format and not in String format */
        return ResponseEntity.ok(authClient.login(credentialsDto)).getBody();
    }

    /**
     * Handles POST requests to "/register"
     * Accepts user registration data as a request body, validated for correctness
     * Calls the authentication client to perform registration with provided user data
     * On successful registration, also registers the user locally in the system
     * Returns a reactive Mono<ResponseEntity<String>> containing the registration
     * response or error message
     * 
     * @param user
     * @return Mono<ResponseEntity<String>> with registration response
     */
    @PostMapping("/register")
    public Mono<ResponseEntity<String>> register(@RequestBody @Valid SignUpDto user) {
        /** TODO : Return datas in JSON format and not in String format */
        return authClient.register(user)
                .flatMap(response -> {
                    // On successful registration, also register user locally
                    RegisterDto userRegister = new RegisterDto(user.firstName(), user.lastName(), user.login());
                    userService.register(userRegister);

                    // Return HTTP 200 OK with the response body
                    return Mono.just(ResponseEntity.ok(response));
                })
                .onErrorResume(ex -> {
                    // Handle errors here (e.g., registration failure)
                    // You can customize the error message or status code based on exception type
                    String errorMessage = "Registration failed: " + ex.getMessage();
                    return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage));
                });
    }
}
