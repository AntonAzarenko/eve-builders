package com.azarenka.evebuilders.service.util;

import com.azarenka.evebuilders.domain.ModuleSlot;
import com.azarenka.evebuilders.domain.dto.file.*;
import com.azarenka.evebuilders.domain.sqllite.InvGroup;
import com.azarenka.evebuilders.domain.sqllite.InvType;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class StaticMaterialLoader {

    private final Map<Integer, TypeInfo> byTypeId = new HashMap<>();
    private final Map<String, TypeInfo> byTypeName = new HashMap<>();
    private Map<Integer, InvType> types = new HashMap<>();
    private Map<Integer, InvGroup> groups = new HashMap<>();
    private List<ActivityMaterial> materials = new ArrayList<>();
    private List<ActivityProduct> products = new ArrayList<>();
    private List<TypeDogmaAttribute> attributes = new ArrayList<>();

    private Map<Integer, List<Integer>> typeEffects = new HashMap<>();
    private Map<Integer, List<TypeDogmaAttribute>> attrMap = new HashMap<>();
    private Map<String, Integer> nameToTypeID = new HashMap<>();

    private final SlotResolver slotResolver = new SlotResolver();

    public StaticMaterialLoader() {
        Map<Integer, InvGroup> groupMap = groups;
        loadFiles();
        Map<Integer, List<MaterialEntry>> materialsByBlueprint = new HashMap<>();
        for (ActivityMaterial m : materials) {
            if (m.getActivityID() != 1 && m.getActivityID() != 11) continue;
            InvType materialType = types.get(m.getMaterialTypeID());
            if (materialType == null) continue;
            MaterialEntry entry = new MaterialEntry();
            entry.setMaterialTypeID(m.getMaterialTypeID());
            entry.setMaterialTypeName(materialType.getTypeName());
            entry.setQuantity(m.getQuantity());
            materialsByBlueprint
                    .computeIfAbsent(m.getTypeID(), k -> new ArrayList<>())
                    .add(entry);
        }
        for (ActivityProduct p : products) {
            if (p.getActivityID() != 1 && p.getActivityID() != 11) continue;
            InvType productType = types.get(p.getProductTypeID());
            InvType blueprintType = types.get(p.getTypeID());
            if (productType == null || blueprintType == null) continue;
            InvGroup group = groupMap.get(productType.getGroupId());
            TypeInfo info = new TypeInfo();
            info.setTypeID(productType.getTypeID());
            info.setTypeName(productType.getTypeName());
            info.setGroupName(group != null ? group.getGroupName() : "Unknown");
            info.setCategoryID(group != null ? group.getCategoryID() : -1);
            info.setOutputQuantity(p.getQuantity());
            List<MaterialEntry> mats = materialsByBlueprint.getOrDefault(p.getTypeID(), List.of());
            info.setMaterials(new ArrayList<>(mats));
            byTypeId.put(info.getTypeID(), info);
            byTypeName.put(info.getTypeName(), info);
        }
    }

    private void loadFiles() {
        try {
            var invTypes = loadCsvList("invTypes.csv", InvType.class);
            this.nameToTypeID = invTypes.stream()
                    .filter(t -> t.getTypeName() != null)
                    .collect(Collectors.toMap(
                            InvType::getTypeName,
                            InvType::getTypeID,
                            (existing, replacement) -> existing
                    ));
            types = loadCsv(invTypes, InvType.class);
            groups = loadCsv("invGroups.csv", InvGroup.class);
            materials = loadCsvList("industryActivityMaterials.csv", ActivityMaterial.class);
            products = loadCsvList("industryActivityProducts.csv", ActivityProduct.class);
            attributes = loadCsvList("dgmTypeAttributes.csv", TypeDogmaAttribute.class);
            typeEffects = loadCsvList("dgmTypeEffects.csv", TypeDogmaEffect.class).stream()
                    .collect(Collectors.groupingBy(e -> e.typeID,
                            Collectors.mapping(e -> e.effectID, Collectors.toList())));
            this.attrMap = attributes.stream().collect(Collectors.groupingBy(a -> a.typeID));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public TypeInfo getByTypeId(int id) {
        return byTypeId.get(id);
    }

    public TypeInfo getByTypeName(String name) {
        return byTypeName.get(name);
    }

    public Collection<TypeInfo> getAll() {
        return byTypeId.values();
    }

    public ModuleSlot resolveSlotByTypeName(String typeName) {
        return slotResolver.resolveSlotByTypeName(typeName);
    }

    private <T> Map<Integer, T> loadCsv(String fileName, Class<T> type) throws IOException {
        List<T> list = loadCsvList(fileName, type);
        Map<Integer, T> map = new HashMap<>();

        for (T obj : list) {
            int id;
            if (type == InvGroup.class) {
                id = ((InvGroup) obj).getGroupID();
            } else if (type == InvType.class) {
                id = ((InvType) obj).getTypeID();
            } else {
                throw new IllegalArgumentException("Unsupported type for ID extraction: " + type.getSimpleName());
            }
            map.put(id, obj);
        }

        return map;
    }

    private <T> Map<Integer, T> loadCsv(List<T> list, Class<T> type) throws IOException {
        Map<Integer, T> map = new HashMap<>();

        for (T obj : list) {
            int id;
            if (type == InvGroup.class) {
                id = ((InvGroup) obj).getGroupID();
            } else if (type == InvType.class) {
                id = ((InvType) obj).getTypeID();
            } else {
                throw new IllegalArgumentException("Unsupported type for ID extraction: " + type.getSimpleName());
            }
            map.put(id, obj);
        }

        return map;
    }

    private <T> List<T> loadCsvList(String fileName, Class<T> type) throws IOException {
        CsvMapper mapper = new CsvMapper();
        CsvSchema schema = CsvSchema.emptySchema().withHeader();
        Resource resource = new ClassPathResource("data/" + fileName);

        try (InputStream in = resource.getInputStream()) {
            MappingIterator<T> it = mapper.readerFor(type).with(schema).readValues(in);
            return it.readAll();
        }
    }

    private class SlotResolver {

        private static final int SLOT_ATTRIBUTE_ID = 1374;

        private static final Map<Integer, ModuleSlot> SLOT_FLAGS = Map.ofEntries(
                Map.entry(11, ModuleSlot.HIGH_SLOT),
                Map.entry(12, ModuleSlot.HIGH_SLOT),
                Map.entry(13, ModuleSlot.HIGH_SLOT),
                Map.entry(19, ModuleSlot.MIDDLE_SLOT),
                Map.entry(20, ModuleSlot.MIDDLE_SLOT),
                Map.entry(21, ModuleSlot.MIDDLE_SLOT),
                Map.entry(27, ModuleSlot.LOW_SLOT),
                Map.entry(28, ModuleSlot.LOW_SLOT),
                Map.entry(92, ModuleSlot.RIG),
                Map.entry(125, ModuleSlot.SUBSYSTEM)
        );

        public ModuleSlot resolveSlotByTypeName(String typeName) {
            if (typeName == null) return ModuleSlot.UNKNOWN;

            TypeInfo type = byTypeName.get(typeName);
            if (type == null) return ModuleSlot.UNKNOWN;

            int typeID = type.getTypeID();

            // 1. По dogma attribute 1374
            List<TypeDogmaAttribute> attrs = attrMap.get(typeID);
            if (attrs != null) {
                for (TypeDogmaAttribute attr : attrs) {
                    if (attr.attributeID == SLOT_ATTRIBUTE_ID && attr.valueFloat != null) {
                        int flag = attr.valueFloat.intValue();
                        ModuleSlot slot = SLOT_FLAGS.get(flag);
                        if (slot != null) return slot;
                    }
                }
            }

            // 2. Fallback по effectID
            List<Integer> effects = typeEffects.getOrDefault(typeID, List.of());
            if (effects.contains(11)) return ModuleSlot.HIGH_SLOT;
            if (effects.contains(13)) return ModuleSlot.MIDDLE_SLOT;
            if (effects.contains(12)) return ModuleSlot.LOW_SLOT;
            if (effects.contains(2663)) return ModuleSlot.RIG;
            if (effects.contains(3772)) return ModuleSlot.SUBSYSTEM;

            return switch (type.getCategoryID()) {
                case 8 -> ModuleSlot.CHARGE;
                case 18 -> ModuleSlot.DRONE_BAY;
                default -> ModuleSlot.CARGO;
            };
        }
    }
}
