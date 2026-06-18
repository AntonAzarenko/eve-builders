package com.azarenka.evebuilders.domain.auth.auth.ui;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public record AuthProfile(String userId,
                          String username,
                          Set<String> roles,
                          Set<String> permissions,
                          boolean superAdmin) {

    public AuthProfile {
        roles = roles == null ? Set.of() : Collections.unmodifiableSet(new LinkedHashSet<>(roles));
        permissions = permissions == null ? Set.of() : Collections.unmodifiableSet(new LinkedHashSet<>(permissions));
    }
}
