package ch.sectioninformatique.template.test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ch.sectioninformatique.template.user.UserDto;
import ch.sectioninformatique.template.user.UserService;
import lombok.RequiredArgsConstructor;

/**
 * REST controller for managing items in the system.
 * This controller provides endpoints for CRUD operations on items,
 * with appropriate security checks and authorization requirements.
 * All responses are automatically converted to JSON format.
 */
@RequestMapping("/test")
@RequiredArgsConstructor
@RestController
public class testController {

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
        return ResponseEntity.ok(currentUser);
    }

    /**
     * Promotes a user to the test user role.
     * This endpoint:
     * - Requires the 'user:update' authority
     * - Validates the user exists and isn't already an test user
     * - Returns success/error message
     *
     * @param userId The ID of the user to promote
     * @return ResponseEntity with success message or error details
     */
    @PreAuthorize("hasAuthority('user:update')")
    @PutMapping("/{userId}/promote-test")
    public ResponseEntity<?> promoteToTestUser(@PathVariable Long userId) {
        try {
            userService.promoteToTestUser(userId);
            return ResponseEntity.ok().body("User promoted to test user successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}
