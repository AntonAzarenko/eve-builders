package com.azarenka.evebuilders.service.impl;

import com.azarenka.evebuilders.domain.db.DistributedOrder;
import com.azarenka.evebuilders.domain.db.Order;
import com.azarenka.evebuilders.domain.enums.GroupTypeEnum;
import com.azarenka.evebuilders.domain.dto.UserStat;
import com.azarenka.evebuilders.domain.enums.Metric;
import com.azarenka.evebuilders.repository.database.IOrderRepository;
import com.azarenka.evebuilders.domain.enums.OrderStatusEnum;
import com.azarenka.evebuilders.service.api.IDistributedOrderService;
import com.azarenka.evebuilders.service.api.IStatisticService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
public class StatisticService implements IStatisticService {
    private static final int INACTIVE_DAYS = 30;

    @Autowired
    private IDistributedOrderService distributedOrderService;
    @Autowired
    private IOrderRepository orderRepository;

    @Override
    public List<UserStat> fetchLeaderboard(Metric metric, LocalDate from, LocalDate to, boolean includeInactive) {
        List<DistributedOrder> all = safe(distributedOrderService.getAllOrders());
        Map<String, String> categoryByOrderNumber = safe(orderRepository.findAll()).stream()
            .collect(Collectors.toMap(Order::getOrderNumber, Order::getCategory, (a, b) -> a));

        Map<String, LocalDate> lastActivity = all.stream()
            .collect(Collectors.groupingBy(
                DistributedOrder::getUserName,
                Collectors.mapping(
                    d -> maxDate(nonNull(d.getAppliedDate()), nonNull(d.getFinishedDate())),
                    Collectors.collectingAndThen(Collectors.toList(), StatisticService::maxDateOfList)
                )
            ));

        LocalDate today = LocalDate.now();
        LocalDate inactiveThreshold = today.minusDays(INACTIVE_DAYS);

        Predicate<String> activePredicate = user -> {
            LocalDate la = lastActivity.get(user);
            return la != null && !la.isBefore(inactiveThreshold);
        };

        Map<String, Integer> valuesByUser = switch (metric) {
            case ORDERS -> countOrdersTaken(all, from, to);
            case SHIPS_MADE -> sumProducedByCategory(all, categoryByOrderNumber, from, to, Set.of(GroupTypeEnum.SHIPS.name()));
            case MODULES_MADE -> sumProducedByCategory(all, categoryByOrderNumber, from, to, Set.of(GroupTypeEnum.MODULES.name()));
        };

        List<UserStat> rows = valuesByUser.entrySet().stream()
            .map(e -> {
                String user = nvl(e.getKey(), "—");
                boolean active = Optional.ofNullable(lastActivity.get(user))
                    .map(d -> !d.isBefore(inactiveThreshold))
                    .orElse(false);
                return new UserStat(
                    0,
                    user,
                    displayNameFromUser(user),
                    e.getValue(),
                    metric,
                    active
                );
            })
            .filter(us -> includeInactive || us.active())
            .filter(us -> us.value() > 0)
            .collect(Collectors.toList());

        rows.sort((a, b) -> {
            int cmp = Integer.compare(b.value(), a.value()); // по убыванию
            if (cmp != 0) {
                return cmp;
            }
            LocalDate la = lastActivity.get(a.username());
            LocalDate lb = lastActivity.get(b.username());
            cmp = nullSafeDateCompare(lb, la);               // «свежее» выше
            if (cmp != 0) {
                return cmp;
            }
            return a.displayName().compareToIgnoreCase(b.displayName());
        });

        int[] rank = {1};
        List<UserStat> ranked = rows.stream()
            .map(us -> new UserStat(rank[0]++, us.username(), us.displayName(), us.value(), us.metricLabel(),
                us.active()))
            .toList();

        return ranked;
    }

    private Map<String, Integer> countOrdersTaken(List<DistributedOrder> all, LocalDate from, LocalDate to) {
        return all.stream()
            .filter(d -> between(nonNull(d.getAppliedDate()), from, to))
            .collect(Collectors.groupingBy(
                DistributedOrder::getUserName,
                Collectors.reducing(0, d -> 1, Integer::sum) // считаем записи
            ));
    }

    private Map<String, Integer> sumProducedByCategory(List<DistributedOrder> all, Map<String, String> categoryByOrderNumber,
                                                       LocalDate from, LocalDate to, Set<String> categories) {
        return all.stream()
            .filter(d -> isFinalStatus(d.getOrderStatus()))
            .filter(d -> categories.contains(resolveCategory(d, categoryByOrderNumber)))
            .filter(d -> between(nonNull(d.getFinishedDate()), from, to))
            .collect(Collectors.groupingBy(
                DistributedOrder::getUserName,
                Collectors.reducing(0, d -> nonNull(d.getCountReady()), Integer::sum)
            ));
    }

    private String resolveCategory(DistributedOrder distributedOrder, Map<String, String> categoryByOrderNumber) {
        String fromOrder = categoryByOrderNumber.get(distributedOrder.getOrderNumber());
        if (fromOrder != null && !fromOrder.isBlank()) {
            return fromOrder;
        }
        return distributedOrder.getCategory();
    }

    private boolean isFinalStatus(OrderStatusEnum s) {
        if (s == null) {
            return false;
        }
        return switch (s.name()) {
            case "COMPLETED" -> true;
            default -> false;
        };
    }

    private boolean between(LocalDate date, LocalDate from, LocalDate to) {
        if (date == null) {
            return false;
        }
        if (from != null && date.isBefore(from)) {
            return false;
        }
        if (to != null && date.isAfter(to)) {
            return false;
        }
        return true;
    }

    private Integer nonNull(Integer v) {
        return v == null ? 0 : v;
    }

    private LocalDate nonNull(LocalDate d) {
        return d == null ? LocalDate.MIN : d;
    }

    private LocalDate maxDate(LocalDate a, LocalDate b) {
        return a.isAfter(b) ? a : b;
    }

    private static LocalDate maxDateOfList(List<LocalDate> dates) {
        LocalDate max = null;
        for (LocalDate d : dates) {
            if (d == null || d.equals(LocalDate.MIN)) {
                continue;
            }
            if (max == null || d.isAfter(max)) {
                max = d;
            }
        }
        return max;
    }

    private int nullSafeDateCompare(LocalDate a, LocalDate b) {
        if (a == null && b == null) {
            return 0;
        }
        if (a == null) {
            return -1;
        }
        if (b == null) {
            return 1;
        }
        return a.compareTo(b);
    }

    private <T> List<T> safe(List<T> list) {
        return list == null ? List.of() : list;
    }

    private String displayNameFromUser(String username) {
        return username == null ? "—" : username;
    }

    private String nvl(String s, String def) {
        return s == null || s.isBlank() ? def : s;
    }
}
