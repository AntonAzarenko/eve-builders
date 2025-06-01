package com.azarenka.evebuilders.service.impl;

import com.azarenka.evebuilders.domain.dto.MaterialType;
import com.azarenka.evebuilders.domain.sqllite.*;
import com.azarenka.evebuilders.repository.litesql.EveIconRepository;
import com.azarenka.evebuilders.repository.litesql.IDgmAttributesRepository;
import com.azarenka.evebuilders.repository.litesql.InvGroupRepository;
import com.azarenka.evebuilders.repository.litesql.InvTypesRepository;
import com.azarenka.evebuilders.service.api.IEveMaterialDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EveMaterialsDataService implements IEveMaterialDataService {

    @Autowired
    private InvGroupRepository invGroupRepository;
    @Autowired
    private InvTypesRepository invTypesRepository;
    @Autowired
    private EveIconRepository eveIconRepository;
    @Autowired
    private IDgmAttributesRepository dgmAttributesRepository;

    @Override
    public List<InvType> getTypesByGroupId(Integer groupId) {
        return invTypesRepository.findByGroupId(groupId);
    }

    @Override
    public List<InvType> getTypesByGroupIds(List<Integer> groupIds) {
        return invTypesRepository.findByGroupIdIn(groupIds);
    }

    @Override
    public InvType getInvTypeByTypeName(String name) {
        List<InvType> byTypeName = invTypesRepository.findByTypeName(name);
        return byTypeName.isEmpty() ? null : invTypesRepository.findByTypeName(name).get(0);
    }

    @Override
    public EveIcon getEveIconById(Integer id) {
        return eveIconRepository.findByIconId(id);
    }

    @Override
    public List<Integer> getAttributeIdByTypeId(Integer typeId) {
        return dgmAttributesRepository.findAttributeIdByTypeId(typeId);
    }

    @Override
    public List<MaterialInfo> getMaterialsForType(String moduleName) {
        return invTypesRepository.findManufacturingMaterials(moduleName);
    }

    @Override
    public MaterialType getTypeByName(String typeName) {
        MaterialClassificationInfo info = invTypesRepository.findGroupAndCategoryByTypeName(typeName);
        if (info == null) return MaterialType.UNKNOWN;

        String group = Optional.ofNullable(info.getGroupName()).orElse("").toLowerCase();
        String category = Optional.ofNullable(info.getCategoryName()).orElse("").toLowerCase();

        if (category.contains("mineral")) return MaterialType.MINERAL;
        else if (group.contains("ice")) return MaterialType.ICE_PRODUCT;
        else if (group.contains("gas")) return MaterialType.GAS;
        else if (group.contains("moon")) return MaterialType.MOON_MATERIAL;
        else if (group.contains("simple reaction")) return MaterialType.SIMPLE_REACTION;
        else if (group.contains("composite") || group.contains("advanced")) return MaterialType.COMPOSITE_REACTION;
        else if (group.contains("planet")) return MaterialType.PLANETARY;
        else if (group.contains("components")) return MaterialType.COMPONENT;
        else if (category.contains("ship")) return MaterialType.SHIP;
        else if (category.contains("fuel block")) return MaterialType.FUEL;
        else if (category.contains("module")) return MaterialType.MODULE;
        else if (group.contains("material")) return MaterialType.MATERIAL;
        else return MaterialType.UNKNOWN;
    }

    @Override
    public List<InvGroup> getInvGroupsById(int id) {
        return invGroupRepository.findAllByCategoryID(id);
    }
}
