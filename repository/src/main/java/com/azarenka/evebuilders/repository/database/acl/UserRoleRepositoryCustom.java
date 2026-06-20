package com.azarenka.evebuilders.repository.database.acl;

import java.util.Set;

public interface UserRoleRepositoryCustom {

    UserRoleSyncResult syncUserRoles(String userId, Set<String> roleCodes);
}
