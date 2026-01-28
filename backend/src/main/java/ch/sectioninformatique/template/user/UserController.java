package ch.sectioninformatique.template.user;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ch.sectioninformatique.template.app.exceptions.AppException;
import ch.sectioninformatique.template.auth.AuthClient;
import ch.sectioninformatique.template.auth.CredentialsDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

/**
 * REST controller for managing user operations.
 * This controller handles various user-related endpoints including:
 * - User authentication and authorization
 * - User promotion to different roles
 * - Retrieval of user lists (active, deleted, or all)
 * - User deletion (soft and permanent, local and global)
 * 
 * All endpoints require appropriate authentication and authorization levels.
 */
@RequestMapping("/users")
@RequiredArgsConstructor
@RestController
public class UserController {

    /** Service for handling user-related operations */
    private final UserService userService;

    /** Client to send authentication requests to the spring-auth application */
    @Autowired
    private final AuthClient authClient;

    /**
     * Client for making user-related HTTP requests to the authentication service
     */
    @Autowired
    private final UserClient userClient;

    /**
     * Retrieves the currently authenticated user's informations.
     * This endpoint:
     * - Requires user authentication
     * - Returns the user's informations
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
     * Promotes a user to a local app role.
     * This endpoint:
     * - Requires the 'user:update' authority
     * - Validates the user exists and has not already the role
     * - Returns success/error message
     *
     * @param userId The ID of the user to promote
     * @return ResponseEntity with success message or error details
     */
    @PutMapping("/{userId}/promote-local-app-role")
    @PreAuthorize("hasAuthority('user:update')")
    public ResponseEntity<?> promoteToLocalAppRole(@PathVariable Long userId) {
        try {
            userService.promoteToLocalAppRole(userId);
            return ResponseEntity.ok().body("User promoted to local app role successfully.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Retrieves all users in the system excluding soft-deleted ones.
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
    @GetMapping("/all-with-deleted")
    @PreAuthorize("hasAuthority('user:read')")
    public ResponseEntity<List<User>> allWithDeletedUsers() {
        List<User> users = userService.allWithDeletedUsers();
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

    /**
     * Handles soft DELETE requests to "/{userId}/{global}"
     * Soft deletes a user either locally or from the global auth service, based
     * on the 'global' flag
     * If 'global' is false, deletes the user from the local database
     * If 'global' is true, deletes the user from the local database and calls the
     * authClient to delete the user from the global auth service
     * Returns a ResponseEntity with success message or error details
     * 
     * @param userId The ID of the user to delete
     * @param global Flag indicating whether to delete locally or globally
     * @return ResponseEntity with deletion result message
     */
    @DeleteMapping("/{userId}/{global}")
    @PreAuthorize("hasAuthority('user:delete')")
    public Mono<ResponseEntity<?>> delete(@RequestHeader("Authorization") String token, @PathVariable Long userId,
            @PathVariable boolean global) {

        // Determine deletion scope based on global flag
        if (global) {
            // Call authClient to delete user from global auth service
            // then delete locally if successful
            return authClient.deleteGlobalUser(token, userId)
                    .flatMap(response -> {

                        // Extract body from ResponseEntity to get deleted user info
                        Map<String, String> body = response.getBody();
                        if (body != null && body.containsKey("deletedUserLogin")) {

                            // Delete user from local database using the login from auth service
                            userService.deleteUserByLogin(body.get("deletedUserLogin"));

                            // Return success response with message from auth service
                            return Mono.just((ResponseEntity<?>) ResponseEntity.ok(Map.of(
                                    "message", body.get("message"))));

                        } else {
                            // Handle missing response data from auth service
                            return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                    .body(Map.of("message", "Failed to delete user: missing response data")));
                        }
                    })
                    // Handle any errors from the auth service call
                    .onErrorResume(ex -> Mono.error(new AppException(ex.getMessage(), HttpStatus.BAD_REQUEST)));
        } else {
            // Delete user from local database only
            userService.deleteUser(userId);
            return Mono.just(ResponseEntity.ok(Map.of("message", "Local User deleted successfully")));
        }
    }

    /**
     * Handles permanent DELETE requests to "/{userId}/{global}/permanent"
     * Permanently deletes a user either locally or from the global auth service,
     * based on the 'global' flag
     * If 'global' is false, permanently deletes the user from the local database
     * If 'global' is true, permanently deletes the user from the local database and
     * calls the authClient to permanently delete the user from the auth service
     * Returns a ResponseEntity with success message or error details
     * 
     * @param userId The ID of the user to permanently delete
     * @param global Flag indicating whether to delete locally or globally
     * @return ResponseEntity with permanent deletion result message
     */
    @DeleteMapping("/{userId}/{global}/permanent")
    @PreAuthorize("hasAuthority('user:delete')")
    public Mono<ResponseEntity<?>> deletePermanent(@RequestHeader("Authorization") String token,
            @PathVariable Long userId,
            @PathVariable boolean global) {
        // Determine permanent deletion scope based on global flag
        if (global) {
            // Call authClient to permanently delete user from the global auth service
            return authClient.deleteGlobalUserPermanent(token, userId)
                    .flatMap(response -> {

                        // Extract body from ResponseEntity to get deleted user info
                        Map<String, String> body = response.getBody();
                        if (body != null && body.containsKey("deletedUserLogin")) {

                            // Permanently delete user from local database using the login
                            userService.deleteUserPermanentByLogin(body.get("deletedUserLogin"));

                            // Return success response with message from auth service
                            return Mono.just((ResponseEntity<?>) ResponseEntity.ok(Map.of(
                                    "message", body.get("message"))));

                        } else {
                            // Handle missing response data from auth service
                            return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                    .body(Map.of("message", "Failed to delete user: missing response data")));
                        }
                    })
                    // Handle any errors from the auth service call
                    .onErrorResume(ex -> Mono.error(new AppException(ex.getMessage(), HttpStatus.BAD_REQUEST)));
        } else {
            // Permanently delete user from local database only
            userService.deleteUserPermanent(userId);
            return Mono.just(ResponseEntity.ok(Map.of("message", "Local User deleted successfully")));
        }
    }

    /**
     * Promotes a user to manager role in both the authentication service and
     * locally.
     * This endpoint:
     * - Requires the 'user:update' authority
     * - First promotes the user in the global auth service
     * - Returns the response from the auth service
     * 
     * @param token  The authorization token (Bearer token) for authentication
     * @param userId The ID of the user to promote to manager role
     * @return Mono containing ResponseEntity with the promotion result
     */
    @PutMapping(path = "/{userId}/promote-manager")
    @PreAuthorize("hasAuthority('user:update')")
    public Mono<ResponseEntity<String>> promoteToManager(@RequestHeader("Authorization") String token,
            @PathVariable Long userId) {
        // Call auth service to promote user to manager globally
        return userClient.promoteToManager(token, userId)
                .flatMap(response -> {
                    return Mono.just(response);
                });
    }

    /**
     * Revokes manager role from a user in both the authentication service and
     * locally.
     * This endpoint:
     * - Requires the 'user:update' authority
     * - Revokes the manager role in the global auth service
     * - Returns the response from the auth service
     * 
     * @param token  The authorization token (Bearer token) for authentication
     * @param userId The ID of the user whose manager role will be revoked
     * @return Mono containing ResponseEntity with the revocation result
     */
    @PutMapping(path = "/{userId}/revoke-manager")
    @PreAuthorize("hasAuthority('user:update')")
    public Mono<ResponseEntity<String>> revokeManager(@RequestHeader("Authorization") String token,
            @PathVariable Long userId) {
        // Call auth service to revoke manager role from user globally
        return userClient.revokeManager(token, userId)
                .flatMap(response -> {
                    return Mono.just(response);
                });
    }

}
