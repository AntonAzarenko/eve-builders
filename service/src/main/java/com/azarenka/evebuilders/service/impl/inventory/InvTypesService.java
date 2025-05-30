package com.azarenka.evebuilders.service.impl.inventory;

import com.azarenka.evebuilders.domain.sqllite.InvType;
import com.azarenka.evebuilders.repository.litesql.InvTypesRepository;
import com.azarenka.evebuilders.service.api.IInvTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class InvTypesService implements IInvTypeService {

    @Autowired
    private InvTypesRepository invTypesRepository;

    @Override
    public List<InvType> getTypesByGroupId(Integer groupId) {
        return invTypesRepository.findByGroupId(groupId);
    }
}
