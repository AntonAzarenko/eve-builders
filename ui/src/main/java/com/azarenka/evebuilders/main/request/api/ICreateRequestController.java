package com.azarenka.evebuilders.main.request.api;

import com.azarenka.evebuilders.domain.db.Fit;
import com.azarenka.evebuilders.domain.db.RequestOrder;
import com.azarenka.evebuilders.domain.sqllite.InvGroup;
import com.azarenka.evebuilders.domain.sqllite.InvType;
import com.azarenka.evebuilders.service.api.IFitLoaderService;
import com.vaadin.flow.component.html.Image;

import java.util.List;

public interface ICreateRequestController {

    List<Fit> gitAllFits();

    List<InvGroup> getInvGroupsById(Integer id);

    List<InvType> getTypesByGroupIds(List<Integer> groupIds);

    List<InvType> getTypesByGroupId(Integer groupId);

    Image getBigImageByParameters(InvType invType, String size);

    //TODO adjust logic to return Object with error messages instead of boolean
    boolean uploadFit(String text);

    RequestOrder createRequest(RequestOrder order);

    void removeRequest(String id);

    IFitLoaderService getFitLoaderService();

    List<RequestOrder> getRequestOrders();
}
