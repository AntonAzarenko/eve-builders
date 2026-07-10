package com.azarenka.evebuilders.service.impl;

import static com.azarenka.evebuilders.service.impl.MailMessageFormatterConstants.MESSAGE_ADMIN_COORDINATOR_FORMATTER;

import com.azarenka.evebuilders.domain.db.Order;
import com.azarenka.evebuilders.domain.db.RequestOrder;
import com.azarenka.evebuilders.domain.db.Role;
import com.azarenka.evebuilders.domain.dto.UserDto;
import com.azarenka.evebuilders.service.api.IEveMailService;
import com.azarenka.evebuilders.service.api.IRequestOrderService;
import com.azarenka.evebuilders.service.api.IUserService;
import com.azarenka.evebuilders.service.api.integration.IEveMailIntegrationService;
import com.azarenka.evebuilders.service.impl.auth.eve.SecurityUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MailService implements IEveMailService {

    private static final String COORDINATOR_STATUS_SUBJECT = "Статус Заявки Обновлен";
    private static final String SERVER = "https://industry.scan-stakan.com/login";
    @Autowired
    private IEveMailIntegrationService integrationService;
    @Autowired
    private IUserService userService;
    @Autowired
    private IRequestOrderService requestOrderService;

    @Override
    public void sendMailToCoordinator(Order order) {
        var userName = SecurityUtils.getUserName();
        var sender = userService.getByUsername(userName).get();
        var userToken = userService.getUserToken();
        var requestOrder = requestOrderService.getRequestById(order.getRequestId());
        var coordinators = getUsersByRole(Role.ROLE_COORDINATOR);
        coordinators.forEach(coordinator -> {
            integrationService.sendMail(userToken, sender.getCharacterId(), coordinator.getCharacterId(),
                COORDINATOR_STATUS_SUBJECT,
                String.format(MESSAGE_ADMIN_COORDINATOR_FORMATTER, SERVER, requestOrder.getId(),
                    requestOrder.getItemName(), requestOrder.getPrice(), requestOrder.getCount(),
                    requestOrder.getRequestStatus()));
        });
    }

    @Override
    public void sendMailToAdmin(RequestOrder requestOrder) {
        var userName = SecurityUtils.getUserName();
        var sender = userService.getByUsername(userName).get();
        var userToken = userService.getUserToken();
        var admins = getUsersByRole(Role.ROLE_ADMIN);
        admins.forEach(admin -> {
            integrationService.sendMail(userToken, sender.getCharacterId(), admin.getCharacterId(),
                COORDINATOR_STATUS_SUBJECT,
                String.format(MESSAGE_ADMIN_COORDINATOR_FORMATTER, SERVER, requestOrder.getId(),
                    requestOrder.getItemName(), requestOrder.getPrice(), requestOrder.getCount(),
                    requestOrder.getRequestStatus()));
        });
    }

    @Override
    public void sendMailToCoordinator(RequestOrder requestOrder) {
        var userName = SecurityUtils.getUserName();
        var sender = userService.getByUsername(userName).get();
        var userToken = userService.getUserToken();
        var coordinators = getUsersByRole(Role.ROLE_COORDINATOR);
        coordinators.forEach(coordinator -> {
            integrationService.sendMail(userToken, sender.getCharacterId(), coordinator.getCharacterId(),
                COORDINATOR_STATUS_SUBJECT,
                String.format(MESSAGE_ADMIN_COORDINATOR_FORMATTER, SERVER, requestOrder.getId(),
                    requestOrder.getItemName(), requestOrder.getPrice(), requestOrder.getCount(),
                    requestOrder.getRequestStatus()));
        });
    }

    private List<UserDto> getUsersByRole(Role role) {
        return userService.getUsersDto().stream().filter(u -> u.getRoles().contains(role)).toList();
    }
}
