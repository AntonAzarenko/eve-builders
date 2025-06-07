package com.azarenka.evebuilders.main.managment.dashboard;

import com.azarenka.evebuilders.component.StatCard;
import com.azarenka.evebuilders.component.View;
import com.azarenka.evebuilders.domain.OrderStatusEnum;
import com.azarenka.evebuilders.domain.db.Order;
import com.azarenka.evebuilders.main.managment.api.IDashBoardController;
import com.azarenka.evebuilders.main.menu.MenuManagerPageView;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.List;

import static com.azarenka.evebuilders.service.util.DecimalFormatter.formatIsk;
import static com.azarenka.evebuilders.service.util.DecimalFormatter.maybeToText;

@Route(value = "dashboard", layout = MenuManagerPageView.class)
@RolesAllowed({"ROLE_ADMIN", "ROLE_SUPER_ADMIN"})
public class DashboardView extends View {

    public DashboardView(@Autowired IDashBoardController controller) {
        HorizontalLayout statsLayout = new HorizontalLayout();
        statsLayout.addClassName("dashboard-stats");
        statsLayout.setSpacing(true);
        statsLayout.setPadding(true);

        List<Order> orders = controller.getOrders();

        long inProgress = orders.stream().filter(o -> o.getOrderStatus() == OrderStatusEnum.IN_PROGRESS).count();
        long done = orders.stream().filter(o -> o.getOrderStatus() == OrderStatusEnum.COMPLETED).count();
        BigDecimal total = orders.stream()
                .map(order -> order.getPrice().multiply(new BigDecimal(order.getCount())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalActive = orders.stream()
                .filter(order -> !(OrderStatusEnum.COMPLETED == order.getOrderStatus()))
                .map(order -> order.getPrice().multiply(new BigDecimal(order.getCount())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        long activeUsersCount = controller.getDistributedOrders().stream().map(e -> e.getUserName()).distinct().count();
        long countFullDistributed = orders.stream().filter(o -> o.getCount() == o.getInProgressCount()).count();
        long notStarted = orders.stream().filter(o -> o.getOrderStatus() == OrderStatusEnum.NEW).count();
        StatCard totalOrders = new StatCard("Всего заказов", String.valueOf(orders.size()),
                String.format("%s - %s", "В работе", inProgress));
        StatCard distributedCount = new StatCard("Распределены", String.valueOf(countFullDistributed),
                String.format("%s - %s", "Не начат", notStarted));
        StatCard doneCard = new StatCard("Завершено", String.valueOf(done), "footer");
        StatCard totalPrice = new StatCard("Сумма заказов", formatIsk(total), maybeToText(total));
        StatCard totalActivePrice = new StatCard("Сумма активных заказов",
                formatIsk(totalActive), maybeToText(totalActive));
        totalPrice.setWidth("400px");
        totalActivePrice.setWidth("400px");
        StatCard userWithOrderInProgress = new StatCard("Активные рабочие", String.valueOf(activeUsersCount), "footer");

        statsLayout.add(totalOrders, distributedCount, doneCard, totalPrice, totalActivePrice, userWithOrderInProgress);
        add(statsLayout);
    }
}
