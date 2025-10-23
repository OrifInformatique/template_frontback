package ch.sectioninformatique.template.test;

import java.net.URI;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ch.sectioninformatique.template.auth.AuthClient;
import ch.sectioninformatique.template.auth.CredentialsDto;
import ch.sectioninformatique.template.auth.RegisterDto;
import ch.sectioninformatique.template.auth.SignUpDto;
import ch.sectioninformatique.template.user.User;
import ch.sectioninformatique.template.user.UserDto;
import ch.sectioninformatique.template.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

/**
 * Controller for test endpoints.
 * This controller provides various endpoints for testing purposes,
 * including:
 * - A hello world endpoint to verify the application is running
 * - An endpoint to retrieve the currently authenticated user's information
 * - An endpoint to promote a user to a test admin role
 * - An endpoint to retrieve all users in the system
 * - Endpoints to test login and registration functionalities via the AuthClient
 */
@RequestMapping("/tests")
@RequiredArgsConstructor
@RestController
public class testController {

    /** Service for handling user-related operations */
    private final UserService userService;

    @Autowired
    private Environment environment;

    private final AuthClient authClient;

    /**
     * Returns system information and environment variables.
     * This endpoint is used to verify that the application is running
     * and to display configuration information.
     *
     * @return A formatted string containing system information
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/")
    public String getHello() {
        return "<strong>Hello World !</strong><br>" +
                "<strong>JAVA_HOME : </strong>" + environment.getProperty("JAVA_HOME") + "<br>" +
                "<strong>Spring active profile : </strong>" + environment.getProperty("spring.profiles.active") + "<br>"
                +
                "<strong>Database used : </strong>" + environment.getProperty("spring.datasource.url");
    }

    /**
     * Retrieves the currently authenticated user's information.
     * This endpoint:
     * - Requires user authentication
     * - Returns the user's profile information
     * - Is accessible to all authenticated users
     *
     * @return ResponseEntity containing the current user's DTO
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/me")
    public ResponseEntity<User> authenticatedUser() {
        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        UserDto currentUser = (UserDto) authentication.getPrincipal();
        User localUser = userService.me(currentUser);
        return ResponseEntity.ok(localUser);
    }

    /**
     * Promotes a user to the test admin role.
     * This endpoint:
     * - Requires the 'user:update' authority
     * - Validates the user exists and isn't already an test admin
     * - Returns success/error message
     *
     * @param userId The ID of the user to promote
     * @return ResponseEntity with success message or error details
     */
    @PutMapping("/{userId}/promote-test")
    @PreAuthorize("hasAuthority('user:update')")
    public ResponseEntity<?> promoteToTestAdmin(@PathVariable Long userId) {
        try {
            userService.promoteToTestAdmin(userId);
            return ResponseEntity.ok().body("User promoted to test user successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Retrieves all users in the system.
     * This endpoint:
     * - Requires the 'user:read' authority
     * - Returns a list of all users
     * - Is typically used by administrators
     *
     * @return ResponseEntity containing a list of all users
     */
    @GetMapping("/all")
    @PreAuthorize("hasAuthority('user:read')")
    public ResponseEntity<List<User>> allUsers() {
        List<User> users = userService.allUsers();
        return ResponseEntity.ok(users);
    }

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
    public Mono<String> testCall(@RequestBody @Valid CredentialsDto credentialsDto) {

        return ResponseEntity.ok(authClient.login(credentialsDto.login(), credentialsDto.password())).getBody();
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
    public Mono<ResponseEntity<String>> testCallRegister(@RequestBody @Valid SignUpDto user) {
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
