package com.azarenka.evebuilders.repository.database.acl;

import java.util.Set;

public record UserRoleSyncResult(Set<String> missingRoles, long insertedCount, long deletedCount) {
}
