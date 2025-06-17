package com.azarenka.evebuilders.main.request.create;

import com.azarenka.evebuilders.domain.db.Fit;
import com.azarenka.evebuilders.domain.db.RequestOrder;
import com.azarenka.evebuilders.domain.sqllite.InvGroup;
import com.azarenka.evebuilders.domain.sqllite.InvType;
import com.azarenka.evebuilders.main.request.api.ICreateRequestController;
import com.azarenka.evebuilders.service.api.IEveMaterialDataService;
import com.azarenka.evebuilders.service.api.IFitLoaderService;
import com.azarenka.evebuilders.service.api.IRequestOrderService;
import com.azarenka.evebuilders.service.util.ImageService;
import com.vaadin.flow.component.html.Image;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Objects;

@Controller
public class CreateRequestController implements ICreateRequestController {

    @Autowired
    private IEveMaterialDataService dataService;
    @Autowired
    private ImageService imageService;
    @Autowired
    private IRequestOrderService requestOrderService;
    @Autowired
    private IFitLoaderService fitLoaderService;

    @Override
    public List<Fit> gitAllFits() {
        return fitLoaderService.getAll();
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
    public Image getBigImageByParameters(InvType invType, String size) {
        return imageService.createImage64(invType, size);
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
}
