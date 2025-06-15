package com.azarenka.evebuilders.common.util;

import com.azarenka.evebuilders.domain.db.Role;
import com.azarenka.evebuilders.service.impl.auth.SecurityUtils;

import java.util.Arrays;
import java.util.Objects;

public class BuilderPermission {

    public static boolean hasEditFitPermission() {
        return Objects.requireNonNull(SecurityUtils.getUserRoles())
                .contains(Role.ROLE_ADMIN) || SecurityUtils.getUserRoles().contains(Role.ROLE_SUPER_ADMIN);
    }

    public static boolean hasAdminPermission() {
        return Arrays.stream(new Role[]{Role.ROLE_ADMIN, Role.ROLE_SUPER_ADMIN})
                .anyMatch(Objects.requireNonNull(SecurityUtils.getUserRoles())::contains);
    }

    public static boolean hasCoordinatorPermission() {
        return Arrays.stream(new Role[]{Role.ROLE_COORDINATOR})
                .anyMatch(Objects.requireNonNull(SecurityUtils.getUserRoles())::contains);
    }

    public static boolean hasUserPermission() {
        return Arrays.stream(new Role[]{Role.ROLE_USER})
                .anyMatch(Objects.requireNonNull(SecurityUtils.getUserRoles())::contains);
    }
}
