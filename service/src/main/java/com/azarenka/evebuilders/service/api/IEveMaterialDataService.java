package com.azarenka.evebuilders.service.api;

import com.azarenka.evebuilders.domain.dto.MaterialType;
import com.azarenka.evebuilders.domain.sqllite.EveIcon;
import com.azarenka.evebuilders.domain.sqllite.InvGroup;
import com.azarenka.evebuilders.domain.sqllite.InvType;
import com.azarenka.evebuilders.domain.sqllite.MaterialInfo;

import java.util.List;

public interface IEveMaterialDataService {

    List<InvGroup> getInvGroupsById(int id);

    List<InvType> getTypesByGroupId(Integer groupId);

    List<InvType> getTypesByGroupIds(List<Integer> groupIds);

    InvType getInvTypeByTypeName(String name);

    EveIcon getEveIconById(Integer id);

    List<Integer> getAttributeIdByTypeId(Integer typeId);

    List<MaterialInfo> getMaterialsForType(String moduleName);

    MaterialType getTypeByName(String typeName);
}
