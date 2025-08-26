package com.azarenka.evebuilders.main.managment.dashboard;

import static com.azarenka.evebuilders.service.util.DecimalFormatter.formatIsk;
import static com.azarenka.evebuilders.service.util.DecimalFormatter.maybeToText;

import com.azarenka.evebuilders.component.StatCard;
import com.azarenka.evebuilders.component.View;
import com.azarenka.evebuilders.domain.enums.OrderStatusEnum;
import com.azarenka.evebuilders.domain.db.DistributedOrder;
import com.azarenka.evebuilders.domain.db.Order;
import com.azarenka.evebuilders.main.managment.api.IDashBoardController;
import com.azarenka.evebuilders.main.menu.MenuManagerPage;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.router.Route;

import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.List;

import jakarta.annotation.security.RolesAllowed;

@Route(value = "dashboard", layout = MenuManagerPage.class)
@RolesAllowed({"ROLE_ADMIN", "ROLE_SUPER_ADMIN"})
public class DashboardView extends View {

    private final FlexLayout cards = new FlexLayout();
    private final IDashBoardController controller;

    private final List<Order> orders;
    private final List<DistributedOrder> distributedOrders;

    public DashboardView(@Autowired IDashBoardController controller) {
        this.controller = controller;
        orders = controller.getOrders();
        distributedOrders = controller.getDistributedOrders();
        initContent();
        add(cards);
    }

    private void initContent() {
        cards.setWidthFull();
        cards.getStyle().set("gap", "12px");
        cards.getStyle().set("flex-wrap", "wrap");
        cards.add(initAllOrdersCard(), initInProgressCard(), initCompletedCard(), initTotalPriceCard(),
            initTotalActivePrice());
        if (hasOrdersForProcessing()) {
            cards.add(initRequiresProcessingCard());
        }
    }

    private StatCard initRequiresProcessingCard() {
        long countOrdersForProcessing = getCountOrdersForProcessing();
        var statCard = new StatCard("Заказы требующие обработки", String.valueOf(countOrdersForProcessing), "");
        statCard.addClickListener(event -> UI.getCurrent().navigate(ProcessDistributedOrderView.class));
        return statCard;
    }

    private Component initTotalPriceCard() {
        var total = orders.stream()
            .map(order -> order.getPrice().multiply(new BigDecimal(order.getCount())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        var statCard = new StatCard("Сумма заказов", formatIsk(total), maybeToText(total));
        statCard.setWidth("400px");
        return statCard;
    }

    private Component initTotalActivePrice() {
        var totalActive = orders.stream()
            .filter(order -> OrderStatusEnum.COMPLETED != order.getOrderStatus())
            .filter(order -> OrderStatusEnum.ARCHIVED != order.getOrderStatus())
            .map(order -> order.getPrice().multiply(new BigDecimal(order.getCount())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        var statCard =
            new StatCard("Сумма активных заказов", formatIsk(totalActive), maybeToText(totalActive));
        statCard.setWidth("400px");
        return statCard;
    }

    private Component initCompletedCard() {
        var done = getCompletedCount() + getArchivedCount();
        return new StatCard("Завершено", String.valueOf(done), String.format("%s - %s", "В архиве", orders.stream()
            .filter(o -> o.getOrderStatus() == OrderStatusEnum.ARCHIVED).count()));
    }

    private StatCard initAllOrdersCard() {
        var inProgress = getInProgressCount() + getDistributedCount();
        return new StatCard("Всего заказов", String.valueOf(orders.size()),
            String.format("%s - %s", "В работе", inProgress));
    }

    private StatCard initInProgressCard() {
        var inProgress = getInProgressCount() + getDistributedCount();
        return new StatCard("В работе", String.valueOf(inProgress),
            String.format("%s - %s", "Распределены", orders.stream()
                .filter(o -> o.getOrderStatus() == OrderStatusEnum.DISTRIBUTED).count()));
    }

    private long getCompletedCount() {
        return orders.stream().filter(o -> o.getOrderStatus() == OrderStatusEnum.COMPLETED).count();
    }

    private long getArchivedCount() {
        return orders.stream().filter(o -> o.getOrderStatus() == OrderStatusEnum.ARCHIVED).count();
    }

    private long getInProgressCount() {
        return orders.stream()
            .filter(o -> o.getOrderStatus() == OrderStatusEnum.IN_PROGRESS).count();
    }

    private long getDistributedCount() {
        return orders.stream()
            .filter(o -> o.getOrderStatus() == OrderStatusEnum.DISTRIBUTED).count();
    }

    private boolean hasOrdersForProcessing() {
        return getCountOrdersForProcessing() > 0;
    }

    private long getCountOrdersForProcessing() {
        return distributedOrders.stream()
            .filter(distributedOrder -> distributedOrder.getOrderStatus().equals(OrderStatusEnum.WAITING_FOR_APPROVAL))
            .count();
    }
}
