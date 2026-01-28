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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequestMapping("/users")
@RequiredArgsConstructor
@RestController
public class UserController {

    /** Service for handling user-related operations */
    private final UserService userService;

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
        userService.promoteToLocalAppRole(userId);
        return ResponseEntity.ok().body("User promoted to local app role successfully.");
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

        if (global) {
            return userService.deleteGlobalAndLocal(token, userId)
                    .map(message -> ResponseEntity.ok(Map.of("message", message)));
        } else {
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
        if (global) {
            return userService.deleteGlobalAndLocalPermanent(token, userId)
                    .map(message -> ResponseEntity.ok(Map.of("message", message)));
        } else {
            userService.deleteUserPermanent(userId);
            return Mono.just(ResponseEntity.ok(Map.of("message", "Local User deleted successfully")));
        }
    }

}
