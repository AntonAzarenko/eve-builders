package com.azarenka.evebuilders.main.request;

import com.azarenka.evebuilders.domain.db.Fit;
import com.azarenka.evebuilders.domain.db.Order;
import com.azarenka.evebuilders.domain.db.RequestOrder;
import com.azarenka.evebuilders.domain.db.RequestOrderStatusEnum;
import com.azarenka.evebuilders.domain.enums.OrderStatusEnum;
import com.azarenka.evebuilders.domain.sqllite.InvGroup;
import com.azarenka.evebuilders.domain.sqllite.InvType;
import com.azarenka.evebuilders.main.request.api.IRequestController;
import com.azarenka.evebuilders.service.api.IEveMaterialDataService;
import com.azarenka.evebuilders.service.api.IFitLoaderService;
import com.azarenka.evebuilders.service.api.IOrderService;
import com.azarenka.evebuilders.service.api.IRequestOrderService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Controller
public class RequestController implements IRequestController {

    @Autowired
    private IEveMaterialDataService dataService;
    @Autowired
    private IRequestOrderService requestOrderService;
    @Autowired
    private IFitLoaderService fitLoaderService;
    @Autowired
    private IOrderService orderService;

    @Override
    public List<Fit> gitAllFits() {
        return fitLoaderService.getAll();
    }

    @Override
    public List<Fit> gitAllFitsByUser() {
        return fitLoaderService.gitAllFitsByUser();
    }

    @Override
    public List<InvGroup> getInvGroupsById(Integer id) {
        return dataService.getInvGroupsById(id);
    }

    @Override
    public List<InvType> getTypesByGroupIds(List<Integer> groupIds) {
        return dataService.getTypesByGroupIds(groupIds);
    }

    @Override
    public List<InvType> getTypesByGroupId(Integer groupId) {
        return dataService.getTypesByGroupId(groupId);
    }

    @Override
    public boolean uploadFit(String text) {
        return fitLoaderService.upload(text);
    }

    @Override
    public RequestOrder createRequest(RequestOrder order) {
        if (Objects.nonNull(order.getId())) {
            return requestOrderService.update(order);
        } else {
            return requestOrderService.save(order);
        }
    }

    @Override
    public void removeRequest(String id) {
        requestOrderService.removeOrder(id);
    }

    @Override
    public IFitLoaderService getFitLoaderService() {
        return fitLoaderService;
    }

    @Override
    public List<RequestOrder> getRequestOrders() {
        return requestOrderService.getAllRequestOrders();
    }

    @Override
    public Fit getFitById(String id) {
        return fitLoaderService.getFitById(id);
    }

    @Override
    @Transactional
    public void updateRequest(RequestOrder requestOrder) {
        RequestOrder updated = requestOrderService.update(requestOrder);
        if (updated.getRequestStatus().equals(RequestOrderStatusEnum.ARCHIVED)) {
            Order byOrderNumber = orderService.getByRequestId(updated.getId());
            if (Objects.nonNull(byOrderNumber) && byOrderNumber.getOrderStatus() == OrderStatusEnum.COMPLETED) {
                orderService.updateStatus(OrderStatusEnum.ARCHIVED, byOrderNumber.getId());
            }
        }
    }

    @Override
    public boolean deleteFit(Fit fit) {
        return fitLoaderService.removeFit(fit);
    }
}
