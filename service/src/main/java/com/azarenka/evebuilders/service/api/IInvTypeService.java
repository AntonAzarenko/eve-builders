package com.azarenka.evebuilders.service.api;

import com.azarenka.evebuilders.domain.sqllite.InvType;

import java.util.List;

public interface IInvTypeService {

    List<InvType> getTypesByGroupId(Integer groupId);

    InvType getInvTypeByModuleName(String moduleName);

    List<InvType> getInvTypesByTypeNames(List<String> typeNames);
}
