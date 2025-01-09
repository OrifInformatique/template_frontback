package ch.sectioninformatique.template.security;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

import static ch.sectioninformatique.template.security.PermissionEnum.*;

public enum RoleEnum {
    USER(EnumSet.of(USER_READ, USER_WRITE)),
    SUPER_ADMIN(EnumSet.of(COURSE_READ, COURSE_WRITE, STUDENT_READ, STUDENT_WRITE, USER_READ, USER_WRITE)),
    STUDENT(EnumSet.of(STUDENT_READ)),
    ADMIN(EnumSet.of(COURSE_READ, COURSE_WRITE, STUDENT_READ, STUDENT_WRITE)),
    ADMINTRAINEE(EnumSet.of(COURSE_READ, STUDENT_READ));

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
