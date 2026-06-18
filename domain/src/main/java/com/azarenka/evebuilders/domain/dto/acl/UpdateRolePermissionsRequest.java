package com.azarenka.evebuilders.domain.dto.acl;

import java.util.LinkedHashSet;
import java.util.Set;

public record UpdateRolePermissionsRequest(Set<String> permissionCodes) {

    public UpdateRolePermissionsRequest {
        permissionCodes = permissionCodes == null ? Set.of() : Set.copyOf(new LinkedHashSet<>(permissionCodes));
    }
}
