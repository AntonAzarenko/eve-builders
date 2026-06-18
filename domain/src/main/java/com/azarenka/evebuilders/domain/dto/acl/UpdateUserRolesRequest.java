package com.azarenka.evebuilders.domain.dto.acl;

import java.util.LinkedHashSet;
import java.util.Set;

public record UpdateUserRolesRequest(Set<String> roleCodes) {

    public UpdateUserRolesRequest {
        roleCodes = roleCodes == null ? Set.of() : Set.copyOf(new LinkedHashSet<>(roleCodes));
    }
}
