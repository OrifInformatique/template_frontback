package ch.sectioninformatique.template.security;

import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import static ch.sectioninformatique.template.security.PermissionEnum.USER_DELETE;
import static ch.sectioninformatique.template.security.PermissionEnum.USER_READ;
import static ch.sectioninformatique.template.security.PermissionEnum.USER_UPDATE;
import static ch.sectioninformatique.template.security.PermissionEnum.USER_WRITE;
import static ch.sectioninformatique.template.security.PermissionEnum.ITEM_READ;
import static ch.sectioninformatique.template.security.PermissionEnum.ITEM_WRITE;
import static ch.sectioninformatique.template.security.PermissionEnum.ITEM_UPDATE;
import static ch.sectioninformatique.template.security.PermissionEnum.ITEM_DELETE;

/**
 * Enumeration of the <b>main roles</b>, which are defined and managed by the
 * external <b>spring-auth</b> authentication provider.
 *
 * <p>These roles are <b>not</b> persisted or managed by this application: they
 * are transmitted in the {@code mainRole} claim of the JWT issued by spring-auth.
 * This enum is only a read-only mirror of that vocabulary, kept here so the
 * backend can translate the {@code mainRole} claim into Spring Security
 * authorities for {@code @PreAuthorize} checks.
 *
 * <p>Roles local to this application live in {@link LocalRoleEnum} instead.
 *
 * The roles are hierarchical:
 * - USER: Basic access to resources
 * - MANAGER: Extended access to user management
 * - ADMIN: Full access to all system features
 */
public enum MainRoleEnum {
    /**
     * Basic user role with limited permissions.
     * Can only read user and item information.
     */
    USER(EnumSet.of(
            USER_READ,
            ITEM_READ)),

    /**
     * Manager role with extended permissions.
     * Can manage users, but cannot delete them.
     */
    MANAGER(EnumSet.of(
            USER_READ,
            USER_WRITE,
            USER_UPDATE,
            ITEM_READ,
            ITEM_WRITE,
            ITEM_UPDATE)),

    /**
     * Administrator role with full system access.
     * Has all permissions including deletion of users.
     */
    ADMIN(EnumSet.of(
            USER_READ,
            USER_WRITE,
            USER_UPDATE,
            USER_DELETE,
            ITEM_READ,
            ITEM_WRITE,
            ITEM_UPDATE,
            ITEM_DELETE));

    /** Set of permissions associated with this role */
    private final Set<PermissionEnum> permissions;

    /**
     * Constructs a new MainRoleEnum with the specified permissions.
     *
     * @param permissions The set of permissions to be associated with this role
     */
    MainRoleEnum(Set<PermissionEnum> permissions) {
        this.permissions = permissions;
    }

    /**
     * Returns the set of permissions associated with this role.
     *
     * @return The set of permissions for this role
     */
    public Set<PermissionEnum> getPermissions() {
        return permissions;
    }

    /**
     * Converts the role's permissions into Spring Security GrantedAuthority
     * objects.
     * This method creates SimpleGrantedAuthority objects for each permission and
     * adds a role-based authority (e.g., "ROLE_USER").
     *
     * @return Set of SimpleGrantedAuthority objects representing the role's
     *         permissions
     */
    public Set<SimpleGrantedAuthority> getGrantedAuthorities() {
        Set<SimpleGrantedAuthority> authorities = getPermissions().stream()
                .map(permission -> new SimpleGrantedAuthority(permission.getPermission()))
                .collect(Collectors.toSet());
        authorities.add(new SimpleGrantedAuthority("ROLE_" + this.name()));
        return authorities;
    }
}
