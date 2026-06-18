package com.azarenka.evebuilders.service.api;

import com.azarenka.evebuilders.domain.acl.Role;
import com.azarenka.evebuilders.domain.acl.RolePermission;
import com.azarenka.evebuilders.domain.acl.UserPermission;
import com.azarenka.evebuilders.domain.acl.UserRole;
import com.azarenka.evebuilders.domain.auth.auth.ui.AuthProfile;
import com.azarenka.evebuilders.domain.db.Permission;

import java.util.List;
import java.util.Set;

public interface IAccessControlService {

    Set<Role> getUserRoles(String userId);

    Set<Permission> getRolePermissionsForUser(String userId);

    Set<Permission> getDirectPermissions(String userId);

    Set<Permission> getFinalPermissions(String userId);

    Set<String> getFinalPermissionCodes(String userId);

    boolean hasPermission(String userId, String permissionCode);

    boolean hasAnyPermission(String userId, Set<String> permissionCodes);

    boolean hasAllPermissions(String userId, Set<String> permissionCodes);

    boolean isSuperAdmin(String userId);

    AuthProfile getAuthProfile(String userId);

    List<Role> getAllRoles();

    List<Permission> getAllPermissions();

    Role createRole(Role role);

    Role updateRole(Role role);

    void deleteRole(Long roleId);

    RolePermission assignPermissionToRole(Long roleId, Long permissionId);

    void removePermissionFromRole(Long roleId, Long permissionId);

    UserRole assignRoleToUser(String userId, Long roleId);

    void removeRoleFromUser(String userId, Long roleId);

    UserPermission assignDirectPermissionToUser(String userId, Long permissionId);

    void removeDirectPermissionFromUser(String userId, Long permissionId);

    void deletePermission(Long permissionId);
}
