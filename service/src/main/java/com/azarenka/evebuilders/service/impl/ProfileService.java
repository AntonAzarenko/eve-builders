package com.azarenka.evebuilders.service.impl;

import com.azarenka.evebuilders.domain.auth.auth.ui.CurrentUserProfileResponse;
import com.azarenka.evebuilders.domain.db.DistributedOrder;
import com.azarenka.evebuilders.domain.db.OrderFilter;
import com.azarenka.evebuilders.domain.db.User;
import com.azarenka.evebuilders.domain.dto.UserFleetStat;
import com.azarenka.evebuilders.domain.dto.UserStat;
import com.azarenka.evebuilders.domain.enums.Metric;
import com.azarenka.evebuilders.domain.enums.OrderStatusEnum;
import com.azarenka.evebuilders.service.api.IAccessControlService;
import com.azarenka.evebuilders.service.api.IDistributedOrderService;
import com.azarenka.evebuilders.service.api.IFlitStatisticService;
import com.azarenka.evebuilders.service.api.IProfileService;
import com.azarenka.evebuilders.service.api.IStatisticService;
import com.azarenka.evebuilders.service.api.IUserService;
import com.azarenka.evebuilders.service.impl.auth.eve.SecurityUtils;
import com.azarenka.evebuilders.service.impl.intergarion.EveCharacterService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ProfileService implements IProfileService {

    @Autowired
    private IUserService userService;
    @Autowired
    private IAccessControlService accessControlService;
    @Autowired
    private IDistributedOrderService distributedOrderService;
    @Autowired
    private IStatisticService statisticService;
    @Autowired
    private IFlitStatisticService flitStatisticService;
    @Autowired
    private EveCharacterService eveCharacterService;

    @Override
    @Transactional(readOnly = true)
    public CurrentUserProfileResponse getCurrentProfile() {
        User user = getCurrentUser();
        var authProfile = accessControlService.getAuthProfile(user.getUid());
        List<DistributedOrder> userOrders = distributedOrderService.getAllByUserName(new OrderFilter());

        List<UserStat> ordersLeaderboard = statisticService.fetchLeaderboard(Metric.ORDERS_ALL, null, null, true);
        List<UserStat> shipsLeaderboard = statisticService.fetchLeaderboard(Metric.SHIPS_MADE, null, null, true);
        //Set<UserFleetStat> fleetStats = flitStatisticService.getFleetStats();

        UserStat currentOrders = findUserStat(ordersLeaderboard, user.getUsername());
        UserStat currentShips = findUserStat(shipsLeaderboard, user.getUsername());
       // UserFleetStat currentFleet = findFleetStat(fleetStats, user.getUsername());

        int distributedOrders = currentOrders == null ? userOrders.size() : currentOrders.value();
        int completedOrders = (int) userOrders.stream()
            .filter(order -> order.getOrderStatus() == OrderStatusEnum.COMPLETED)
            .count();
        int builtShips = currentShips == null ? 0 : currentShips.value();
        //int fleetParticipations = currentFleet == null ? 0 : Math.toIntExact(currentFleet.value());

        String corporationId = user.getCharacterInfo() == null
            ? null
            : eveCharacterService.getParameter(user.getCharacterInfo(), "corporation_id", String.class);

        return new CurrentUserProfileResponse(
            user.getUid(),
            user.getCharacterId(),
            user.getUsername(),
            corporationId,
            user.getCorporationName(),
            user.getAllianceName(),
            normalizeNullable(user.getLanguage()),
            normalizeTheme(user.getTheme()),
            authProfile.roles(),
            authProfile.permissions(),
            authProfile.superAdmin(),
            distributedOrders,
            completedOrders,
            builtShips,
            0,
            currentOrders == null ? null : currentOrders.rank(),
            currentShips == null ? null : currentShips.rank(),
            0
        );
    }

    @Override
    public void updateLanguage(String language) {
        userService.updateLanguage(normalizeLanguage(language));
    }

    @Override
    public void updateTheme(String themeName) {
        userService.updateTheme(normalizeTheme(themeName));
    }

    private User getCurrentUser() {
        String userName = SecurityUtils.getUserName();
        Optional<User> user = userService.getByUsername(userName);
        return user.orElseThrow(() -> new IllegalStateException("Current user not found: " + userName));
    }

    private UserStat findUserStat(List<UserStat> stats, String username) {
        if (stats == null || username == null) {
            return null;
        }
        return stats.stream()
            .filter(stat -> username.equalsIgnoreCase(stat.username()))
            .findFirst()
            .orElse(null);
    }

    private UserFleetStat findFleetStat(Set<UserFleetStat> stats, String username) {
        if (stats == null || username == null) {
            return null;
        }
        return stats.stream()
            .filter(stat -> username.equalsIgnoreCase(stat.displayName()))
            .findFirst()
            .orElse(null);
    }

    private String normalizeLanguage(String language) {
        if (language == null || language.isBlank()) {
            return null;
        }
        return language.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeTheme(String themeName) {
        if (themeName == null || themeName.isBlank()) {
            return "light";
        }
        return themeName.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeNullable(String value) {
        return value == null ? "" : value;
    }
}
