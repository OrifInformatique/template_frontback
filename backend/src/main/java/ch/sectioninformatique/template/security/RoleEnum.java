package ch.sectioninformatique.template.security;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

import static ch.sectioninformatique.template.security.PermissionEnum.*;

public enum RoleEnum {
    USER(EnumSet.of(ITEM_READ, ITEM_WRITE)),
    ADMIN(EnumSet.of(USER_READ, USER_WRITE, ITEM_READ, ITEM_WRITE)),
    SUPER_ADMIN(EnumSet.of(ITEM_READ, ITEM_WRITE, ITEM_DELETE, USER_READ, USER_WRITE, USER_DELETE));

    private final Set<PermissionEnum> permissions;

    RoleEnum(Set<PermissionEnum> permissions) {
        this.permissions = permissions;
    }

    public Set<PermissionEnum> getPermissions() {
        return permissions;
    }

    public Set<SimpleGrantedAuthority> getGrantedAuthorities() {
        Set<SimpleGrantedAuthority> permissions = getPermissions().stream()
                .map(permission -> new SimpleGrantedAuthority(permission.getPermission()))
                .collect(Collectors.toSet());
        permissions.add(new SimpleGrantedAuthority("ROLE_" + this.name()));
        return permissions;
    }
}
