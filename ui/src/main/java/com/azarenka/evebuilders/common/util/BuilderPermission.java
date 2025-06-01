package com.azarenka.evebuilders.common.util;

import com.azarenka.evebuilders.domain.db.Role;
import com.azarenka.evebuilders.service.impl.auth.SecurityUtils;

import java.util.Objects;

public class BuilderPermission {

    public static boolean hasEditFitPermission() {
        return Objects.requireNonNull(SecurityUtils.getUserRoles())
                .contains(Role.ROLE_ADMIN) || SecurityUtils.getUserRoles().contains(Role.ROLE_SUPER_ADMIN);
    }
}
