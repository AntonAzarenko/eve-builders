package com.azarenka.evebuilders.domain.dto.acl;

public record PermissionDto(Long id,
                            String code,
                            String name,
                            String description,
                            String groupName) {
}
