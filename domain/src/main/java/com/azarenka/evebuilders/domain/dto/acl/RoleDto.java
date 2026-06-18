package com.azarenka.evebuilders.domain.dto.acl;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import com.azarenka.evebuilders.domain.dto.acl.PermissionDto;

public record RoleDto(Long id,
                      String code,
                      String name,
                      String description,
                      boolean systemRole,
                      Set<PermissionDto> permissions) {

    public RoleDto {
        permissions = permissions == null ? Set.of() : Collections.unmodifiableSet(new LinkedHashSet<>(permissions));
    }
}
