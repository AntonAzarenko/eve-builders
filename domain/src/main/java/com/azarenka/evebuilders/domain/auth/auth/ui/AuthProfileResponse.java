package com.azarenka.evebuilders.domain.auth.auth.ui;

import java.util.LinkedHashSet;
import java.util.Set;

public record AuthProfileResponse(String userId,
                                  String username,
                                  Set<String> roles,
                                  Set<String> permissions,
                                  boolean superAdmin) {

    public AuthProfileResponse {
        roles = roles == null ? Set.of() : Set.copyOf(new LinkedHashSet<>(roles));
        permissions = permissions == null ? Set.of() : Set.copyOf(new LinkedHashSet<>(permissions));
    }
}
