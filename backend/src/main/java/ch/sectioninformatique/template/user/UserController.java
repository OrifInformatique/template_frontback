package ch.sectioninformatique.template.user;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import ch.sectioninformatique.template.app.DeletionFilter;
import ch.sectioninformatique.template.auth.AuthClient;
import ch.sectioninformatique.template.auth.PasswordUpdateDto;
import ch.sectioninformatique.template.auth.RegisterDto;
import ch.sectioninformatique.template.security.BearerToken;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

/**
 * REST controller for managing users.
 * !! Authentication operations are handled in AuthController, this controller focuses on user management !!
 *
 * Endpoints follow the conventions of spring-auth:
 * - GET    /users/me            the authenticated user, with local roles and all permissions
 * - PUT    /users/me/password   change the password of the authenticated user, 204
 * - POST   /users               create a user, 201 + Location + user
 * - GET    /users?status=       list users (ACTIVE by default, DELETED, ALL)
 * - GET    /users/{id}          read a user (soft-deleted or not)
 * - PUT    /users/{id}          update profile, main role and app-specific roles, 200 + user
 * - DELETE /users/{id}          delete (soft by default, local by default), 204
 * - POST   /users/{id}/restore  restore a soft-deleted user, 200 + user
 *
 * Users are always identified by their ID, which is the spring-auth user ID.
 */
@RequestMapping("/users")
@RequiredArgsConstructor
@RestController
public class UserController {

    private final UserService userService;

    private final AuthClient authClient;

    /**
     * Returns the authenticated user: the user of the token, with its local
     * (app-specific) roles and all its permissions, global and local (same
     * content as the login response).
     *
     * @param currentUser the user read from the token
     * @return 200 with the authenticated user
     */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserDto> getCurrentUser(@AuthenticationPrincipal UserDto currentUser) {
        return ResponseEntity.ok(userService.addLocalRolesAndPermissions(currentUser, currentUser.getId()));
    }

    /**
     * Changes the password of the authenticated user (in spring-auth).
     *
     * @param request current and new passwords
     * @return 204 No Content
     */
    @PutMapping("/me/password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> updateOwnPassword(@RequestBody @Valid PasswordUpdateDto request) {
        // spring-auth answers 204 No Content: errors are mapped by AuthClient
        authClient.updatePassword(BearerToken.current(), request).block();
        return ResponseEntity.noContent().build();
    }

    /**
     * Creates a user, in spring-auth then locally (with its app-specific roles).
     *
     * @param request the new user
     * @return 201 with the created user and its URL in the Location header
     */
    @PostMapping
    @PreAuthorize("hasAuthority('user:write')")
    public ResponseEntity<UserDto> createUser(@RequestBody @Valid RegisterDto request) {
        UserDto created = userService.createUser(request, BearerToken.current());
        URI location = UriComponentsBuilder.fromPath("/users/{id}").buildAndExpand(created.getId()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    /**
     * Lists users, filtered by deletion state.
     *
     * @param status ACTIVE (default), DELETED or ALL, case-insensitive
     * @return 200 with the users
     */
    @GetMapping
    @PreAuthorize("hasAuthority('user:read')")
    public ResponseEntity<List<UserDto>> getUsers(@RequestParam(defaultValue = "ACTIVE") DeletionFilter status) {
        List<UserDto> users = switch (status) {
            case ACTIVE -> userService.allUsers();
            case DELETED -> userService.deletedUsers();
            case ALL -> userService.allWithDeletedUsers();
        };
        return ResponseEntity.ok(users);
    }

    /**
     * Reads a user, soft-deleted or not (its "deleted" field tells).
     *
     * @param id the user ID
     * @return 200 with the user, 404 if unknown
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('user:read')")
    public ResponseEntity<UserDto> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUser(id));
    }

    /**
     * Updates a user: profile (in spring-auth), main role (in spring-auth, if
     * given and different) and app-specific roles (locally, if given).
     *
     * @param id      the user ID
     * @param request the new user data
     * @return 200 with the updated user
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('user:update')")
    public ResponseEntity<UserDto> updateUser(@PathVariable Long id, @RequestBody @Valid UserUpdateDto request) {
        return ResponseEntity.ok(userService.updateUser(id, request, BearerToken.current()));
    }

    /**
     * Deletes a user.
     *
     * @param id        the user ID
     * @param permanent true to remove the user from the database, false (default)
     *                  for a soft, restorable deletion
     * @param scope     LOCAL (default): only the local user of this application;
     *                  GLOBAL: the user in spring-auth too
     * @return 204 No Content
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('user:delete')")
    public Mono<ResponseEntity<Void>> deleteUser(@PathVariable Long id,
            @RequestParam(defaultValue = "false") boolean permanent,
            @RequestParam(defaultValue = "LOCAL") DeletionScope scope) {
        ResponseEntity<Void> noContent = ResponseEntity.noContent().build();
        if (scope == DeletionScope.GLOBAL) {
            // spring-auth first, then the local user (see UserService)
            return userService.deleteGlobalAndLocal(BearerToken.current(), id, permanent)
                    .then(Mono.just(noContent));
        }
        if (permanent) {
            userService.deleteUserPermanent(id);
        } else {
            userService.deleteUser(id);
        }
        return Mono.just(noContent);
    }

    /**
     * Restores a soft-deleted user, in spring-auth (if deleted there) and locally.
     *
     * @param id the user ID
     * @return 200 with the restored user
     */
    @PostMapping("/{id}/restore")
    @PreAuthorize("hasAuthority('user:update')")
    public ResponseEntity<UserDto> restoreUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.restoreUser(id, BearerToken.current()));
    }
}
