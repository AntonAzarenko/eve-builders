package com.azarenka.evebuilders.domain.db;

import org.springframework.security.core.GrantedAuthority;

public enum Role implements GrantedAuthority {

    ROLE_VIEWER,
    ROLE_BUILDER,
    ROLE_ADMIN,
    ROLE_SUPER_ADMIN,
    ROLE_COORDINATOR,
    ROLE_MANAGER,
    ROLE_MINER,
    ROLE_CEO;

    @Override
    public String getAuthority() {
        return name();
    }
}
