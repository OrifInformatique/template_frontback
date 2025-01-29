package ch.sectioninformatique.template.security;

public enum PermissionEnum {
    /* ADMIN_READ("admin:read"),
    ADMIN_WRITE("admin:write"), */
    ITEM_READ("item:read"),
    ITEM_WRITE("item:write"),
    ITEM_UPDATE("item:update"),
    ITEM_DELETE("item:delete"),
    USER_READ("user:read"),
    USER_WRITE("user:write"),
    USER_UPDATE("user:update"),
    USER_DELETE("user:delete");

    private final String permission;

    PermissionEnum(String permission) {
        this.permission = permission;
    }

    public String getPermission() {
        return this.permission;
    }
}
