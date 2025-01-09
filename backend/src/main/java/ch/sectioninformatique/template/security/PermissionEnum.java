package ch.sectioninformatique.template.security;

public enum PermissionEnum {
    STUDENT_READ("student:read"),
    STUDENT_WRITE("student:write"),
    COURSE_READ("course:read"),
    COURSE_WRITE("course:write"),
    USER_READ("user:read"),
    USER_WRITE("user:write");

    private final String permission;

    PermissionEnum(String permission) {
        this.permission = permission;
    }

    public String getPermission() {
        return this.permission;
    }
}
