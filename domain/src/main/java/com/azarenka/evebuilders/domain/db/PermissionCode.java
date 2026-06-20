package com.azarenka.evebuilders.domain.db;


public final class PermissionCode {

    public static final String ADMIN_VIEW = "ADMIN_VIEW";
    public static final String USERS_VIEW = "USERS_VIEW";
    public static final String USERS_EDIT = "USERS_EDIT";
    public static final String ROLES_VIEW = "ROLES_VIEW";
    public static final String ROLES_CREATE = "ROLES_CREATE";
    public static final String ROLES_EDIT = "ROLES_EDIT";
    public static final String ROLES_DELETE = "ROLES_DELETE";
    public static final String ROLES_PERMISSIONS_EDIT = "ROLES_PERMISSIONS_EDIT";
    public static final String PERMISSIONS_VIEW = "PERMISSIONS_VIEW";
    public static final String PERMISSIONS_ASSIGN = "PERMISSIONS_ASSIGN";

    private PermissionCode() {
    }
}
