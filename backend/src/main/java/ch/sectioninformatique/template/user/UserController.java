package ch.sectioninformatique.template.user;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ch.sectioninformatique.template.app.exceptions.AppException;
import ch.sectioninformatique.template.auth.AuthClient;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

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
    public Mono<ResponseEntity<?>> delete(@RequestHeader("Authorization") String token, @PathVariable Long userId,
            @PathVariable boolean global) {

        if (global) {
            // Call authClient to delete user from global auth service
            // then delete locally if successful
            return authClient.deleteGlobalUser(token, userId)
                    .flatMap(response -> {

                        // Extract body from ResponseEntity
                        Map<String, String> body = response.getBody();
                        if (body != null && body.containsKey("deletedUserLogin")) {

                            // Delete user locally
                            userService.deleteUserByLogin(body.get("deletedUserLogin"));

                            // Include both message in response
                            return Mono.just((ResponseEntity<?>) ResponseEntity.ok(Map.of(
                                    "message", body.get("message"))));

                        } else {
                            return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                    .body(Map.of("message", "Failed to delete user: missing response data")));
                        }
                    })
                    .onErrorResume(ex -> Mono.error(new AppException(ex.getMessage(), HttpStatus.BAD_REQUEST)));
        } else {
            // Delete user locally only
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
    public Mono<ResponseEntity<?>> deletePermanent(@RequestHeader("Authorization") String token,
            @PathVariable Long userId,
            @PathVariable boolean global) {
        if (global) {
            // Call authClient to delete user from the global auth service
            return authClient.deleteGlobalUserPermanent(token, userId)
                    .flatMap(response -> {

                        // Extract body from ResponseEntity
                        Map<String, String> body = response.getBody();
                        if (body != null && body.containsKey("deletedUserLogin")) {

                            // Delete user locally
                            userService.deleteUserPermanentByLogin(body.get("deletedUserLogin"));

                            // Include both message in response
                            return Mono.just((ResponseEntity<?>) ResponseEntity.ok(Map.of(
                                    "message", body.get("message"))));

                        } else {
                            return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                    .body(Map.of("message", "Failed to delete user: missing response data")));
                        }
                    })
                    .onErrorResume(ex -> Mono.error(new AppException(ex.getMessage(), HttpStatus.BAD_REQUEST)));
        } else {
            userService.deleteUserPermanent(userId);
            return Mono.just(ResponseEntity.ok(Map.of("message", "Local User deleted successfully")));
        }
    }

}
