package com.azarenka.evebuilders.service.impl.inventory;

import com.azarenka.evebuilders.domain.sqllite.InvType;
import com.azarenka.evebuilders.repository.litesql.InvTypesRepository;
import com.azarenka.evebuilders.service.api.IInvTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
public class InvTypesService implements IInvTypeService {

    @Autowired
    private InvTypesRepository invTypesRepository;

    @Override
    public List<InvType> getTypesByGroupId(Integer groupId) {
        return invTypesRepository.findByGroupId(groupId);
    }

    @Override
    public InvType getInvTypeByModuleName(String moduleName) {
        return invTypesRepository.findByTypeNameIgnoreCase(moduleName).orElse(null);
    }

    @Override
    public List<InvType> getInvTypesByTypeNames(List<String> typeNames) {
        List<InvType> invTypes = new ArrayList<>();
        typeNames.forEach(typeName -> {
            InvType invType = getInvTypeByModuleName(typeName);
            if (Objects.nonNull(invType)) {
                invTypes.add(invType);
            }
        });
        return invTypes;
    }
}
