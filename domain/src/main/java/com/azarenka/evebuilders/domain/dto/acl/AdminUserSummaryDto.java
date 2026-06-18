package com.azarenka.evebuilders.domain.dto.acl;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public record AdminUserSummaryDto(String userId,
                                  String username,
                                  String characterName,
                                  String corporationName,
                                  Set<String> roles,
                                  Set<String> directPermissions,
                                  boolean superAdmin) {

    public AdminUserSummaryDto {
        roles = roles == null ? Set.of() : Collections.unmodifiableSet(new LinkedHashSet<>(roles));
        directPermissions = directPermissions == null ? Set.of() : Collections.unmodifiableSet(new LinkedHashSet<>(directPermissions));
    }
}
