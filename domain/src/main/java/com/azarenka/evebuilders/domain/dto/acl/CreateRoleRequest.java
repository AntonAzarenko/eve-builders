package com.azarenka.evebuilders.domain.dto.acl;

import jakarta.validation.constraints.NotBlank;

public record CreateRoleRequest(@NotBlank String code,
                                @NotBlank String name,
                                String description) {
}
