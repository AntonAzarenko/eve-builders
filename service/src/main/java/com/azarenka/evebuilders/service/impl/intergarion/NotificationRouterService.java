package com.azarenka.evebuilders.service.impl.intergarion;

import com.azarenka.evebuilders.domain.db.DistributedOrder;
import com.azarenka.evebuilders.domain.db.Order;
import com.azarenka.evebuilders.domain.dto.ShipOrderDto;
import com.azarenka.evebuilders.repository.database.IOrderRepository;
import com.azarenka.evebuilders.service.api.integration.IDiscordIntegrationService;
import com.azarenka.evebuilders.service.api.integration.INotificationService;
import com.azarenka.evebuilders.service.api.integration.ITelegramIntegrationService;
import com.azarenka.evebuilders.service.impl.intergarion.message.OrderUpdateMessageBuilder;
import com.azarenka.evebuilders.service.util.DiscordMessageCreatorService;
import com.azarenka.evebuilders.service.util.TelegramMessageCreatorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class NotificationRouterService implements INotificationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationRouterService.class);

    private final ITelegramIntegrationService telegramIntegrationService;
    private final IDiscordIntegrationService discordIntegrationService;
    private final IOrderRepository orderRepository;
    private final String provider;
    private final boolean telegramEnabled;
    private final boolean discordEnabled;
    private final String threadPingId;
    private final String threadRequestId;

    public NotificationRouterService(
        ITelegramIntegrationService telegramIntegrationService,
        IDiscordIntegrationService discordIntegrationService,
        IOrderRepository orderRepository,
        @Value("${app.notifications.provider:telegram}") String provider,
        @Value("${app.notifications.telegram.enabled:true}") boolean telegramEnabled,
        @Value("${app.notifications.discord.enabled:false}") boolean discordEnabled,
        @Value("${app.telegram_thread_ping_id}") String threadPingId,
        @Value("${app.telegram_thread_request_id}") String threadRequestId
    ) {
        this.telegramIntegrationService = telegramIntegrationService;
        this.discordIntegrationService = discordIntegrationService;
        this.orderRepository = orderRepository;
        this.provider = provider;
        this.telegramEnabled = telegramEnabled;
        this.discordEnabled = discordEnabled;
        this.threadPingId = threadPingId;
        this.threadRequestId = threadRequestId;
    }

    @Override
    public void sendOrderCreated(Order order) {
        dispatch(
            "order_created",
            () -> telegramIntegrationService.sendMessage(TelegramMessageCreatorService.createOrderMessage(order), threadPingId),
            () -> discordIntegrationService.sendToOrderChannel(DiscordMessageCreatorService.createOrderMessage(order))
        );
    }

    @Override
    public void sendOrderUpdated(Order order) {
        Order previous = null;
        if (order != null) {
            previous = orderRepository.findById(order.getId()).orElse(null);
        }
        String message = OrderUpdateMessageBuilder.build(previous, order);
        dispatch(
            "order_updated",
            () -> telegramIntegrationService.sendInfoMessage(message, threadPingId),
            () -> discordIntegrationService.sendToOrderChannel(message)
        );
    }

    @Override
    public void sendOrderRemoved(String orderNumber) {
        dispatch(
            "order_removed",
            () -> telegramIntegrationService.sendInfoMessage(String.format("Заказ %s был удален", orderNumber), threadPingId),
            () -> discordIntegrationService.sendToOrderChannel(DiscordMessageCreatorService.createOrderRemovedMessage(orderNumber))
        );
    }

    @Override
    public void sendOrderTaken(ShipOrderDto orderDto, int count, String userName) {
        dispatch(
            "order_taken",
            () -> telegramIntegrationService.sendMessage(
                TelegramMessageCreatorService.createTakeOrderMessage(orderDto, count, userName),
                threadRequestId
            ),
            () -> discordIntegrationService.sendToRequestChannel(
                DiscordMessageCreatorService.createTakeOrderMessage(orderDto, count, userName)
            )
        );
    }

    @Override
    public void sendProgressUpdated(DistributedOrder distributedOrder, int readyCount, String userName) {
        dispatch(
            "progress_updated",
            () -> telegramIntegrationService.sendMessage(
                TelegramMessageCreatorService.createFinishOrderMessage(distributedOrder, readyCount, userName),
                threadRequestId
            ),
            () -> discordIntegrationService.sendToRequestChannel(
                DiscordMessageCreatorService.createFinishOrderMessage(distributedOrder, readyCount, userName)
            )
        );
    }

    @Override
    public void sendWaitingForApproval(DistributedOrder distributedOrder, String userName) {
        dispatch(
            "waiting_for_approval",
            () -> telegramIntegrationService.sendMessage(
                TelegramMessageCreatorService.createWaitingForApprovalMessage(distributedOrder, userName),
                threadRequestId
            ),
            () -> discordIntegrationService.sendToRequestChannel(
                DiscordMessageCreatorService.createWaitingForApprovalMessage(distributedOrder, userName)
            )
        );
    }

    @Override
    public void sendOrderDiscarded(DistributedOrder distributedOrder, String userName) {
        dispatch(
            "order_discarded",
            () -> telegramIntegrationService.sendMessage(
                TelegramMessageCreatorService.createDiscardOrderMessage(distributedOrder, userName),
                threadRequestId
            ),
            () -> discordIntegrationService.sendToRequestChannel(
                DiscordMessageCreatorService.createDiscardOrderMessage(distributedOrder, userName)
            )
        );
    }

    private void dispatch(String eventType, Runnable telegramAction, Runnable discordAction) {
        String normalizedProvider = provider.toLowerCase(Locale.ROOT).trim();
        switch (normalizedProvider) {
            case "telegram" -> sendTelegram(eventType, telegramAction);
            case "discord" -> sendDiscord(eventType, discordAction);
            case "both" -> {
                sendTelegram(eventType, telegramAction);
                sendDiscord(eventType, discordAction);
            }
            case "none" -> LOGGER.debug("Notification skipped. Provider=none, Event={}", eventType);
            default -> {
                LOGGER.warn("Unknown notification provider '{}'. Fallback to telegram for Event={}", provider, eventType);
                sendTelegram(eventType, telegramAction);
            }
        }
    }

    private void sendTelegram(String eventType, Runnable action) {
        if (!telegramEnabled) {
            LOGGER.debug("Notification skipped. Provider=telegram, Event={}, Reason=telegram_disabled", eventType);
            return;
        }
        try {
            action.run();
            LOGGER.debug("Notification sent. Provider=telegram, Event={}, Status=ok", eventType);
        } catch (Exception ex) {
            LOGGER.error("Notification failed. Provider=telegram, Event={}, Status=error, Error={}",
                eventType, ex.getMessage(), ex);
        }
    }

    private void sendDiscord(String eventType, Runnable action) {
        if (!discordEnabled) {
            LOGGER.debug("Notification skipped. Provider=discord, Event={}, Reason=discord_disabled", eventType);
            return;
        }
        try {
            action.run();
            LOGGER.debug("Notification sent. Provider=discord, Event={}, Status=ok", eventType);
        } catch (Exception ex) {
            LOGGER.error("Notification failed. Provider=discord, Event={}, Status=error, Error={}",
                eventType, ex.getMessage(), ex);
        }
    }
}
