package ch.sectioninformatique.template.user;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * UserController class is the REST controller for the User entity.
 */
@RequestMapping("/users")
@RestController
public class UserController {
    private final UserService userService;

    public UserController(UserService userService)
    {
        this.userService = userService;
    }

    /**
     * Get the current authenticated user.
     * 
     * @return The current authenticated user
     */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<User> authenticatedUser() {
        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();
        
        User currentUser = (User) authentication.getPrincipal();
        return ResponseEntity.ok(currentUser);
    }

    /**
     * Get all users.
     * 
     * @return The list of all users
     */
    @GetMapping("/all")
    @PreAuthorize("hasAuthority('user:read')")
    public ResponseEntity<List<User>> allUsers() {
        List <User> users = userService.allUsers();
        return ResponseEntity.ok(users);
    }

    /**
     * Promote a user to admin.
     * 
     * @param userId - The ID of the user to promote
     * @return The response entity
     */
    @PreAuthorize("hasAuthority('user:update')")
    @PutMapping("/{userId}/promote-admin")
    public ResponseEntity<?> promoteToAdmin(@PathVariable Long userId) {
        try {
            userService.promoteToAdmin(userId);
            return ResponseEntity.ok().body("User promoted to admin successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Revoke an admin role.
     * 
     * @param userId - The ID of the user to revoke
     * @return The response entity
     */
    @PreAuthorize("hasAuthority('user:update')")
    @PutMapping("/{userId}/revoke-admin")
    public ResponseEntity<?> revokeAdminRole(@PathVariable Long userId) {
        try {
            userService.revokeAdminRole(userId);
            return ResponseEntity.ok().body("Admin role revoked successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Promote a user to super admin.
     * 
     * @param userId - The ID of the user to promote
     * @return The response entity
     */
    @PreAuthorize("hasAuthority('user:update') && hasRole('SUPER_ADMIN')")
    @PutMapping("/{userId}/promote-super-admin")
    public ResponseEntity<?> promoteToSuperAdmin(@PathVariable Long userId) {
        try {
            userService.promoteToSuperAdmin(userId);
            return ResponseEntity.ok().body("Super admin role assigned successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Revoke a super admin role.
     * 
     * @param userId - The ID of the user to revoke
     * @return The response entity
     */
    @PreAuthorize("hasAuthority('user:update') && hasRole('SUPER_ADMIN')")
    @PutMapping("/{userId}/revoke-super-admin")
    public ResponseEntity<?> revokeSuperAdminRole(@PathVariable Long userId) {
        try {
            userService.revokeSuperAdminRole(userId);
            return ResponseEntity.ok().body("Super admin role revoked successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Downgrade a super admin role.
     * 
     * @param userId - The ID of the user to downgrade
     * @return The response entity
     */
    @PreAuthorize("hasAuthority('user:update') && hasRole('SUPER_ADMIN')")
    @PutMapping("/{userId}/downgrade-super-admin")
    public ResponseEntity<?> downgradeSuperAdminRole(@PathVariable Long userId) {
        try {
            userService.downgradeSuperAdminRole(userId);
            return ResponseEntity.ok().body("Super admin role downgraded successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Delete a user.
     * 
     * @param userId - The ID of the user to delete
     * @return The response entity
     */
    @PreAuthorize("hasAuthority('user:delete')")
    @DeleteMapping("/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable Long userId) {
        try {
            userService.deleteUser(userId);
            return ResponseEntity.ok().body("User deleted successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
