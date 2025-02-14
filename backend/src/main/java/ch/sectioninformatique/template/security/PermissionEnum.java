package ch.sectioninformatique.template.security;

/**
 * PermissionEnum class represents the permissions available in the application.
 */
public enum PermissionEnum {
    USER_READ("user:read"),
    USER_WRITE("user:write"),
    USER_UPDATE("user:update"),
    USER_DELETE("user:delete"),
    ITEM_READ("item:read"),
    ITEM_WRITE("item:write"),
    ITEM_UPDATE("item:update"),
    ITEM_DELETE("item:delete");

    private final String permission;

    PermissionEnum(String permission) {
        this.permission = permission;
    }

    public String getPermission() {
        return permission;
    }
}
