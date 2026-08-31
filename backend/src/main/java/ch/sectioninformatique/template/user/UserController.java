package ch.sectioninformatique.template.user;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ch.sectioninformatique.template.auth.AuthClient;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

/**
 * REST controller for managing user operations.
 * !! Authentication operations are handled in AuthController, this controller focuses on user management !!
 * 
 * This controller handles various user-related endpoints including:
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

    private final MessageSource messageSource;

    /**
     * Client for making user-related HTTP requests to the authentication service
     */
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
     * Retrieves all users in the system excluding soft-deleted ones.
     * This endpoint:
     * - Requires the 'user:read' authority
     * - Returns a list of all users
     * - Is typically used by administrators
     *
     * @return ResponseEntity containing a list of all users who are not soft-deleted
     */
    @GetMapping("/all")
    @PreAuthorize("hasAuthority('user:read')")
    public ResponseEntity<List<UserDto>> allUsers() {
        List<UserDto> users = userService.allUsers();
        return ResponseEntity.ok(users);
    }

    /**
     * Retrieves all users in the system including soft-deleted ones.
     * This endpoint:
     * - Requires the 'user:read' authority
     * - Returns a list of all users
     * - Is typically used by administrators
     *
     * @return ResponseEntity containing a list of all users including soft-deleted ones
     */
    @GetMapping("/all-with-deleted")
    @PreAuthorize("hasAuthority('user:read')")
    public ResponseEntity<List<UserDto>> allWithDeletedUsers() {
        List<UserDto> users = userService.allWithDeletedUsers();
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
    public ResponseEntity<List<UserDto>> deletedUsers() {
        List<UserDto> users = userService.deletedUsers();
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
            return userService.deleteGlobalAndLocal(token, userId)
                    .map(message -> ResponseEntity.ok(Map.of("message", message)));
        } else {
            userService.deleteUser(userId);
            String message = messageSource.getMessage(
                    "user.deleted.local",
                    null,
                    LocaleContextHolder.getLocale());
            return Mono.just(ResponseEntity.ok(Map.of("message", message)));
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
            return userService.deleteGlobalAndLocalPermanent(token, userId)
                    .map(message -> ResponseEntity.ok(Map.of("message", message)));
        } else {
            // Permanently delete user from local database only
            userService.deleteUserPermanent(userId);
            String message = messageSource.getMessage(
                    "user.deleted.local",
                    null,
                    LocaleContextHolder.getLocale());
            return Mono.just(ResponseEntity.ok(Map.of("message", message)));
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
        return authClient.promoteToManager(token, userId)
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
        return authClient.revokeManager(token, userId)
                .flatMap(response -> {
                    return Mono.just(response);
                });
    }

    /**
     * Promotes a user to admin role in both the authentication service and
     * locally.
     * This endpoint:
     * - Requires the 'user:update' authority
     * - Promotes the user to admin in the global auth service
     * - Returns the response from the auth service
     * 
     * @param token  The authorization token (Bearer token) for authentication
     * @param userId The ID of the user to promote to admin role
     * @return Mono containing ResponseEntity with the promotion result
     */
    @PutMapping(path = "/{userId}/promote-admin")
    @PreAuthorize("hasAuthority('user:update')")
    public Mono<ResponseEntity<String>> promoteToAdmin(@RequestHeader("Authorization") String token,
            @PathVariable Long userId) {
        // Call auth service to promote user to admin globally
        return authClient.promoteToAdmin(token, userId)
                .flatMap(response -> {
                    return Mono.just(response);
                });
    }

    /**
     * Revokes admin role from a user in both the authentication service and
     * locally.
     * This endpoint:
     * - Requires the 'user:update' authority
     * - Revokes the admin role in the global auth service
     * - Returns the response from the auth service
     * 
     * @param token  The authorization token (Bearer token) for authentication
     * @param userId The ID of the user whose admin role will be revoked
     * @return Mono containing ResponseEntity with the revocation result
     */
    @PutMapping(path = "/{userId}/revoke-admin")
    @PreAuthorize("hasAuthority('user:update')")
    public Mono<ResponseEntity<String>> revokeAdmin(@RequestHeader("Authorization") String token,
            @PathVariable Long userId) {
        // Call auth service to revoke admin role from user globally
        return authClient.revokeAdmin(token, userId)
                .flatMap(response -> {
                    return Mono.just(response);
                });
    }

    /**
     * Downgrades an admin user to manager role in both the authentication service and
     * locally.
     * This endpoint removes admin privileges while maintaining manager-level access.
     * This endpoint:
     * - Requires the 'user:update' authority
     * - Downgrades the admin to manager in the global auth service
     * - Returns the response from the auth service
     * 
     * @param token  The authorization token (Bearer token) for authentication
     * @param userId The ID of the admin user to be downgraded to manager role
     * @return Mono containing ResponseEntity with the downgrade result
     */
    @PutMapping(path = "/{userId}/downgrade-admin")
    @PreAuthorize("hasAuthority('user:update')")
    public Mono<ResponseEntity<String>> downgradeAdmin(@RequestHeader("Authorization") String token,
            @PathVariable Long userId) {
        // Call auth service to downgrade admin to manager globally
        return authClient.downgradeAdmin(token, userId)
                .flatMap(response -> {
                    return Mono.just(response);
                });
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
            userService.promoteToLocalAppRole(userId);
            String message = messageSource.getMessage(
                "user.promoted.local",
                null,
                LocaleContextHolder.getLocale());
            return ResponseEntity.ok().body(message);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('user:update')")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody UserDto user, @RequestHeader("Authorization") String token ) {

        userService.updateUser(id, user);
        String message = authClient.updateUser(token, id, user).block().getBody();
        return ResponseEntity.ok().body(message);
    }

    @PutMapping("/{id}/restore")
    @PreAuthorize("hasAuthority('user:update')")
    public ResponseEntity<?> restoreUser(@PathVariable Long id) {

        userService.restoreUser(id);
        return ResponseEntity.ok().body("User restored successfully.");
    }


}
