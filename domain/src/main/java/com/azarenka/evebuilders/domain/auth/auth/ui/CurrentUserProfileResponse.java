package com.azarenka.evebuilders.domain.auth.auth.ui;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public record CurrentUserProfileResponse(String userId,
                                         String eveCharacterId,
                                         String characterName,
                                         String corporationId,
                                         String corporationName,
                                         String allianceName,
                                         String language,
                                         String theme,
                                         Set<String> roles,
                                         Set<String> permissions,
                                         boolean superAdmin,
                                         int distributedOrders,
                                         int completedOrders,
                                         int builtShips,
                                         int fleetParticipations,
                                         Integer ordersRank,
                                         Integer shipsRank,
                                         Integer fleetRank) {

    public CurrentUserProfileResponse {
        roles = roles == null ? Set.of() : Collections.unmodifiableSet(new LinkedHashSet<>(roles));
        permissions = permissions == null ? Set.of() : Collections.unmodifiableSet(new LinkedHashSet<>(permissions));
    }
}
