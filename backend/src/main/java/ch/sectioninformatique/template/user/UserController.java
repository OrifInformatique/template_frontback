package ch.sectioninformatique.template.user;

import java.util.List;

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

import lombok.RequiredArgsConstructor;

@RequestMapping("/users")
@RequiredArgsConstructor
@RestController
public class UserController {

    /** Service for handling user-related operations */
    private final UserService userService;

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
            userService.promoteToLocalAppRole(userId);
            return ResponseEntity.ok().body("User promoted to local app role successfully.");
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
     * Retrieves all users in the system including soft-deleted ones.
     * This endpoint:
     * - Requires the 'user:read' authority
     * - Returns a list of all users
     * - Is typically used by administrators
     *
     * @return ResponseEntity containing a list of all deleted users
     */
    @GetMapping("/all/deleted")
    @PreAuthorize("hasAuthority('user:read')")
    public ResponseEntity<List<User>> allDeletedUsers() {
        List<User> users = userService.allDeletedUsers();
        return ResponseEntity.ok(users); 
    }

        /**
     * Retrieves all soft-deleted users in the system.
     * This endpoint:
     * - Requires the 'user:read' authority
     * - Returns a list of all soft-deleted users
     * - Is typically used by administrators
     *
     * @return ResponseEntity containing a list of all soft-deleted users
     */
    @GetMapping("/deleted")
    @PreAuthorize("hasAuthority('user:read')")
    public ResponseEntity<List<User>> deletedUsers() {
        List<User> users = userService.deletedUsers();
        return ResponseEntity.ok(users);
    }

}
