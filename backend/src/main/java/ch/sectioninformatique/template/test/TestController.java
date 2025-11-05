package ch.sectioninformatique.template.test;

import java.net.URI;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ch.sectioninformatique.template.user.User;
import ch.sectioninformatique.template.user.UserDto;
import ch.sectioninformatique.template.user.UserService;
import lombok.RequiredArgsConstructor;

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
public class TestController {

    /** Service for handling user-related operations */
    private final UserService userService;

    @Autowired
    private Environment environment;

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
    public ResponseEntity<UserDto> authenticatedUser() {
        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        UserDto currentUser = (UserDto) authentication.getPrincipal();
        UserDto localUser = userService.me(currentUser);
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

        userService.promoteToTestAdmin(userId);
        return ResponseEntity.ok().body(Map.of("message", "User promoted to test admin successfully"));

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