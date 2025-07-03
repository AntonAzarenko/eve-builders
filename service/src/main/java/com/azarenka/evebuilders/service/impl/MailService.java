package com.azarenka.evebuilders.service.impl;

import com.azarenka.evebuilders.domain.db.Order;
import com.azarenka.evebuilders.domain.db.RequestOrder;
import com.azarenka.evebuilders.domain.db.Role;
import com.azarenka.evebuilders.domain.dto.UserDto;
import com.azarenka.evebuilders.service.api.IEveMailService;
import com.azarenka.evebuilders.service.api.IRequestOrderService;
import com.azarenka.evebuilders.service.api.IUserService;
import com.azarenka.evebuilders.service.api.integration.IEveMailIntegrationService;
import com.azarenka.evebuilders.service.impl.auth.SecurityUtils;
import com.azarenka.evebuilders.service.util.IOrderStatusToStringConverter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MailService implements IEveMailService, IOrderStatusToStringConverter {


    private static final String COORDINATOR_STATUS_SUBJECT = "Статус Заявки Обновлен";
    private static final String MESSAGE_FORMATTER = "Приложение-%s\n\tЗаявка:\n\tID='%s',\n\tНаименование=%s,\n\tЦена=%s\n\tСтатус=%s";
    private static final String CREATE_MESSAGE_FORMATTER = "Приложение'%s'\n\tЗаявка:\n\tID='%s',\n\tНаименование=%s,\n\tЦена=%s\n\tСтатус=%s";

    private static final String SERVER = "<font size=\"14\" color=\"#bfffffff\"></font><font size=\"14\" color=\"#ffd98d00\"><a href=\"https://industry.scan-stakan.com/login\">ПРИЛОЖЕНИЕ</a></font><font size=\"14\" color=\"#bfffffff\"> </font>";
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
        var recipient = userService.getByUsername(requestOrder.getCreatedBy()).get();
        integrationService.sendMail(userToken, sender.getCharacterId(), recipient.getCharacterId(),
                COORDINATOR_STATUS_SUBJECT, String.format(MESSAGE_FORMATTER, SERVER, requestOrder.getId(),
                requestOrder.getItemName(), requestOrder.getPrice(), convertStatus(requestOrder.getRequestStatus())));
    }

    @Override
    public void sendMailToAdmin(RequestOrder requestOrder) {
        var userName = SecurityUtils.getUserName();
        var sender = userService.getByUsername(userName).get();
        var userToken = userService.getUserToken();
        List<UserDto> admins =
            userService.getUsersDto().stream().filter(u -> u.getRoles().contains(Role.ROLE_ADMIN)).toList();
        admins.forEach(admin -> {
            integrationService.sendMail(userToken, sender.getCharacterId(), admin.getCharacterId(),
                COORDINATOR_STATUS_SUBJECT, String.format(MESSAGE_FORMATTER, SERVER, requestOrder.getId(),
                requestOrder.getItemName(), requestOrder.getPrice(),convertStatus(requestOrder.getRequestStatus())));
        });
    }

    @Override
    public void sendMailToCoordinator(RequestOrder requestOrder) {
        var userName = SecurityUtils.getUserName();
        var sender = userService.getByUsername(userName).get();
        var userToken = userService.getUserToken();
        var recipient = userService.getByUsername(requestOrder.getCreatedBy()).get();
        integrationService.sendMail(userToken, sender.getCharacterId(), recipient.getCharacterId(),
            COORDINATOR_STATUS_SUBJECT, String.format(MESSAGE_FORMATTER, SERVER, requestOrder.getId(),
                requestOrder.getItemName(), requestOrder.getPrice(),convertStatus(requestOrder.getRequestStatus())));
    }
}

