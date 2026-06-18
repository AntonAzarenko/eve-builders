package com.azarenka.evebuilders.service.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class AccessControlCacheConfig {

    public static final String USER_ROLES_CACHE = "accessControl.userRoles";
    public static final String ROLE_PERMISSIONS_CACHE = "accessControl.rolePermissions";
    public static final String DIRECT_PERMISSIONS_CACHE = "accessControl.directPermissions";
    public static final String FINAL_PERMISSIONS_CACHE = "accessControl.finalPermissions";
    public static final String FINAL_PERMISSION_CODES_CACHE = "accessControl.finalPermissionCodes";
    public static final String AUTH_PROFILE_CACHE = "accessControl.authProfile";
    public static final String SUPER_ADMIN_CACHE = "accessControl.superAdmin";

    @Bean
    public CacheManager accessControlCacheManager() {
        return new ConcurrentMapCacheManager(
            USER_ROLES_CACHE,
            ROLE_PERMISSIONS_CACHE,
            DIRECT_PERMISSIONS_CACHE,
            FINAL_PERMISSIONS_CACHE,
            FINAL_PERMISSION_CODES_CACHE,
            AUTH_PROFILE_CACHE,
            SUPER_ADMIN_CACHE
        );
    }
}
