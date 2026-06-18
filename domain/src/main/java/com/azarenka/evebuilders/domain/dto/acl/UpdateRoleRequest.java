package com.azarenka.evebuilders.domain.dto.acl;

import jakarta.validation.constraints.NotBlank;

public record UpdateRoleRequest(@NotBlank String name,
                                String description) {
}
