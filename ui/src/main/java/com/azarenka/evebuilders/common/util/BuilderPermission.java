package com.azarenka.evebuilders.common.util;

import com.azarenka.evebuilders.domain.db.PermissionCode;
import com.azarenka.evebuilders.service.impl.auth.eve.AccessControlSecurity;

public class BuilderPermission {

    public static boolean hasEditFitPermission() {
        return accessControl().can("CONTRACTS_EDIT");
    }

    public static boolean hasAdminPermission() {
        return accessControl().can(PermissionCode.ADMIN_VIEW);
    }

    public static boolean hasCoordinatorPermission() {
        return accessControl().canAny("CORPORATION_VIEW", "CORPORATION_CONTRACT_VIEW");
    }

    public static boolean hasBuilderPermission() {
        return accessControl().canAny("DASHBOARD_VIEW", "CONTRACTS_ACCEPT", "CONTRACTS_DISCARD");
    }

    public static boolean hasMinerPermission() {
        return accessControl().can("DASHBOARD_VIEW");
    }

    public static boolean hasCeoPermission() {
        return accessControl().canAny("CORPORATION_VIEW", "CORPORATION_CONTRACT_VIEW", "CORPORATION_CONTRACT_EDIT");
    }

    private static AccessControlSecurity accessControl() {
        return SpringContextHolder.getBean(AccessControlSecurity.class);
    }
}
