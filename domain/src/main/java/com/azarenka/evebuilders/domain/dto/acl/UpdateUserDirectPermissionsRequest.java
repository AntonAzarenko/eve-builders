package com.azarenka.evebuilders.domain.dto.acl;

import java.util.LinkedHashSet;
import java.util.Set;

public record UpdateUserDirectPermissionsRequest(Set<String> permissionCodes) {

    public UpdateUserDirectPermissionsRequest {
        permissionCodes = permissionCodes == null ? Set.of() : Set.copyOf(new LinkedHashSet<>(permissionCodes));
    }
}
