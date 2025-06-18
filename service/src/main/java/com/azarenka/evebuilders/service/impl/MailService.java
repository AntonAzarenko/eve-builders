package com.azarenka.evebuilders.service.impl;

import com.azarenka.evebuilders.domain.db.Order;
import com.azarenka.evebuilders.domain.db.RequestOrderStatusEnum;
import com.azarenka.evebuilders.service.api.IEveMailService;
import com.azarenka.evebuilders.service.api.IRequestOrderService;
import com.azarenka.evebuilders.service.api.IUserService;
import com.azarenka.evebuilders.service.api.integration.IEveMailIntegrationService;
import com.azarenka.evebuilders.service.impl.auth.SecurityUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MailService implements IEveMailService {

    private static final String COORDINATOR_STATUS_SUBJECT = "Статус Заявки Обновлен";
    private static final String MESSAGE_FORMATTER = "Заявка:\n\tID='%s',\n\tНаименование=%s,\n\tСтатус=%s";

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
        var requestById = requestOrderService.getRequestById(order.getRequestId());
        var recipient = userService.getByUsername(requestById.getCreatedBy()).get();
        integrationService.sendMail(userToken, sender.getCharacterId(), recipient.getCharacterId(),
                COORDINATOR_STATUS_SUBJECT, String.format(MESSAGE_FORMATTER, requestById.getId(),
                        requestById.getItemName(), convertStatus(requestById.getRequestStatus())));
    }

    private String convertStatus(RequestOrderStatusEnum statusEnum) {
        switch (statusEnum) {
            case SUBMITTED -> {
                return "На рассмотрении";
            }
            case APPROVED -> {
                return "Принят";
            }
            case COMPLETED -> {
                return "Завершен";
            }
            case SUSPENDED -> {
                return "Приостановлен";
            }
            case REJECT -> {
                return "Отклонен";
            }
            default -> {
                return StringUtils.EMPTY;
            }
        }
    }
}
